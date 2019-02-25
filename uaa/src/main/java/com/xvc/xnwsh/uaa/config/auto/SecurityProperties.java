package com.xvc.xnwsh.uaa.config.auto;

import com.fasterxml.jackson.module.afterburner.util.ClassName;
import com.xvc.xnwsh.uaa.web.rest.util.Base64Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;


@ConfigurationProperties(prefix = "spring.encrypt.key-store")
public class SecurityProperties {
    private final Logger log = LoggerFactory.getLogger(SecurityProperties.class);
    private final static String KEY_STORE_TYPE = "JKS";
    private final static String KEY_STORE_DIRECTORY_PATH ="/config/";
    private String keyAlias;
    private String storeName;
    private String storePass;
    private String keyPass;
    private String privateKey;
    private String charset = "UTF-8";
    private boolean debug = false;


    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStorePass() {
        return storePass;
    }

    public void setStorePass(String storePass) {
        this.storePass = storePass;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    //从密钥库读取证书并获取私钥加载至内存中
    @Bean
    public String extractPrivKey() {
        KeyStore ks;
        String privateKeyStr = null;
        try {
            ks = KeyStore.getInstance(KEY_STORE_TYPE);
            InputStream keyStoreInStream = ClassName.class.getResourceAsStream(KEY_STORE_DIRECTORY_PATH + this.storeName);
            ks.load(keyStoreInStream, this.storePass.toCharArray());
            PrivateKey  privKey = (PrivateKey) ks.getKey(this.keyAlias, this.keyPass.toCharArray());
            privateKeyStr = Base64Utils.encode(privKey.getEncoded());
        }catch (Exception e){
            log.error("could not get private key",e);
        }
        return this.privateKey = privateKeyStr;
    }

}
