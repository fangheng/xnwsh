package com.xvc.xnwsh.uaa.web.rest.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.json.JSONObject;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * RSA公钥/私钥/签名工具包
 * </p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 * </p>
 */
public class RSAUtils {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器 大小96-1024位
        keyPairGen.initialize(1024,new SecureRandom());
        //生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥字符串
        String publicKeyStr = Base64Utils.encode(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyStr = Base64Utils.encode(privateKey.getEncoded());
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKeyStr);
        keyMap.put(PRIVATE_KEY, privateKeyStr);
        return keyMap;
    }

    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param data 已加密数据
     * @param privateKey 私钥(BASE64编码)
     *
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = null;
        //判断是pkcs1私钥还是pkcs8私钥
        if(privateKey.indexOf("BEGIN RSA PRIVATE KEY") > 0) {//如果是pkcs1私钥
            privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                        .replace("-----END RSA PRIVATE KEY-----", "").replace("\n", "");
            byte[] keyBytes = Base64Utils.decode(privateKey);
            RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(keyBytes));
            RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
            privateK = keyFactory.generatePrivate(rsaPrivKeySpec);

        } else {//如果是pkcs8私钥
            privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "").replace("\n", "");
            byte[] keyBytes = Base64Utils.decode(privateKey);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        }

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Base64Utils.encode(signature.sign());
    }

    /**
     * <p>
     * 校验数字签名
     * </p>
     *
     * @param data 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign 数字签名
     *
     * @return
     * @throws Exception
     *
     */
    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign));
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data 源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data 源数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 获取私钥
     * </p>
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64Utils.encode(key.getEncoded());
    }

    /**
     * <p>
     * 获取公钥
     * </p>
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return Base64Utils.encode(key.getEncoded());
    }


    /**
     * 读取证书中私钥
     * @param keyStoreFile
     * @param storeFilePass
     * @param keyAlias
     * @param keyAliasPass
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String keyStoreFile, String storeFilePass, String keyAlias, String keyAliasPass) throws Exception{
        KeyStore ks;
        PrivateKey prikey = null;
        ks = KeyStore.getInstance("JKS");
        FileInputStream fin;
        fin = new FileInputStream(keyStoreFile);
        ks.load(fin, storeFilePass.toCharArray());
        prikey = (PrivateKey) ks.getKey(keyAlias, keyAliasPass.toCharArray());
        return prikey;
    }


    public static void main(String[] args) throws Exception {
      String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMZf7Zl30Q1cowWarKWe2WvcPsKaIR/meoaLClmcf6qiFc93xnkol7pce6NfBjPrnnq0NBR5Qy3/rBFtya911SIhu/XEzfMRj2EFkBGilCUBa0J+ERFd0jOWN3PNgiUrcQg6oufz4amI5eGf0SrWdLJ/U6e88KE7qOauirq8m1dwIDAQAB";

    String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIxl/tmXfRDVyjBZqspZ7Za9w+wpohH+Z6hosKWZx/qqIVz3fGeSiXulx7o18GM+ueerQ0FHlDLf+sEW3Jr3XVIiG79cTN8xGPYQWQEaKUJQFrQn4REV3SM5Y3c82CJStxCDqi5/PhqYjl4Z/RKtZ0sn9Tp7zwoTuo5q6KurybV3AgMBAAECgYEAi+dPT1CvpckEU63mjQGTybSLuZe7EqcqcBzyefKF3nkOCe/sHeFd8sK+Bgad5qeo5Pw6ZqCHQzMCUssPRbTdXfMYW9ygSnzX9lhU+q4enliDJ7gEmuXc60k6gIXS1Hj62FNV0yz2OT1NWu2WEjxIRA6CNrVq6D1cXquYm/Oi6mECQQC/b+AZ+q13V0OikROpEGAyuglMxbSk4iFzdPfbRjZDWoascmjp5CXXrlIQhS637A78hlzUS/Iv4ogfi31aE68JAkEAu7+YBVU/Wt3+qfqwCNPnrTpr21U9hry8rbTx7CcTxTqeDzPrnLsv6H8MqRZMdQNQFo3WQz1jm5qC/2+vZW7gfwJBAKFY5YR/5xwijHrD09I2Xx3h9lyidXJStObeutgxASMbdU56zznydIKoBbquxYV2i9sCU9MKf4EkaJ5NrsTwuiECQAMr2lKEvRw40btnS5/qjGhFq1q6Ft72wy0f+FIn2tKjCdZx5xHvq4B+3OTiRopXcvvei3b5S3xuYLYV2d2ZwNsCQEThXCpdGYeNRJ+uJwv9KSO7+q6N0jnm6MhQ6OkZyZFFtjRnki5tudazuXLbTseUtO2iE3xucLXmxE+u+uhyAlc=";

     //-------------------------动态生成公钥与私钥----------------------------------
    System.out.println("-----生成公钥与私钥-----");
    Map<String, Object> keyPair = genKeyPair();
    String publicKey1 = String.valueOf(keyPair.get("RSAPublicKey"));
    String privateKey1 = String.valueOf(keyPair.get("RSAPrivateKey"));
    System.err.println("------生成公钥:"+ publicKey);
    System.err.println("------生成私钥:"+ privateKey);

    //--------------------------数据加密及解密-----------------------------------
    System.err.println("-----数据加密及解密-----");
    String inputStr = "{\"firstName\":\"admin\"}";
    byte[] data = inputStr.getBytes();
     //公钥加密
    System.err.println("利用公钥进行加密...");
    byte[] encryptByPublicData = encryptByPublicKey(data,publicKey);
    String base64encryptByPublicData= Base64Utils.encode(encryptByPublicData);
    System.err.println(base64encryptByPublicData);
    //私密解密
    byte[] decodedData = RSAUtils.decryptByPrivateKey(Base64Utils.decode(base64encryptByPublicData), privateKey);
    String outputStr = new String(decodedData);
    System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + outputStr);

    //--------------------------私钥签名——公钥验证签名---------------------------------------
    System.err.println("私钥签名——公钥验证签名");
    //私钥加密
    byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);
    // 产生签名
    String sign = RSAUtils.sign(encodedData, privateKey);
    System.err.println("签名:" + sign);
    // 验证签名
    boolean status = RSAUtils.verify(encodedData, publicKey, sign);
    System.err.println("状态:" + status);


    }


}
