package com.xvc.xnwsh.uaa.web.rest;

import com.xvc.xnwsh.uaa.config.Constants;
import com.codahale.metrics.annotation.Timed;
import com.xvc.xnwsh.uaa.config.annotation.Decrypt;
import com.xvc.xnwsh.uaa.config.annotation.Encrypt;
import com.xvc.xnwsh.uaa.domain.User;
import com.xvc.xnwsh.uaa.repository.UserRepository;
import com.xvc.xnwsh.uaa.security.AuthoritiesConstants;
import com.xvc.xnwsh.uaa.service.MailService;
import com.xvc.xnwsh.uaa.service.UserService;
import com.xvc.xnwsh.uaa.service.dto.UserDTO;
import com.xvc.xnwsh.uaa.web.rest.errors.BadRequestAlertException;
import com.xvc.xnwsh.uaa.web.rest.errors.EmailAlreadyUsedException;
import com.xvc.xnwsh.uaa.web.rest.errors.LoginAlreadyUsedException;
import com.xvc.xnwsh.uaa.web.rest.util.Base64Utils;
import com.xvc.xnwsh.uaa.web.rest.util.HeaderUtil;
import com.xvc.xnwsh.uaa.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

import io.swagger.annotations.ApiOperation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.security.auth.x500.X500Principal;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    public UserResource(UserRepository userRepository, UserService userService, MailService mailService) {

        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * POST  /users  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException 400 (Bad Request) if the login or email is already in use
     */
    @PostMapping("/users")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) throws URISyntaxException {
        log.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        } else if (userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userService.createUser(userDTO);
            mailService.sendCreationEmail(newUser);
            return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin()))
                .headers(HeaderUtil.createAlert( "userManagement.created", newUser.getLogin()))
                .body(newUser);
        }
    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param userDTO the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already in use
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already in use
     */
    @PutMapping("/users")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to update User : {}", userDTO);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        Optional<UserDTO> updatedUser = userService.updateUser(userDTO);

        return ResponseUtil.wrapOrNotFound(updatedUser,
            HeaderUtil.createAlert("userManagement.updated", userDTO.getLogin()));
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsers(Pageable pageable) {
        final Page<UserDTO> page = userService.getAllManagedUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @return a string list of the all of the roles
     */
    @GetMapping("/users/authorities")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<UserDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(
            userService.getUserWithAuthoritiesByLogin(login)
                .map(UserDTO::new));
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        userService.deleteUser(login);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert( "userManagement.deleted", login)).build();
    }


    @RequestMapping(value = "/rsaWeb")
    public String rsaWeb(){
        return "rsa";
    }


    @ApiOperation(value = "模拟登陆")
   // @Decrypt
    @RequestMapping(value = "testDecrypt")
    public void testDecrypt(@RequestBody User user){
        System.out.println(user.getFirstName());
    }

    @ApiOperation(value = "获取加密用户数据")
    @Encrypt
    @RequestMapping(value = "testEncrypt")
    public void testEncrypt(@RequestBody User user){
        user.setFirstName("admin");
        user.setPassword("123456");
        System.out.println(user.getFirstName());
    }


    //-------------------------------------------------
    private File keystoreFile;
    private String keyStoreType;
    private char[] password;
    private String alias;
    private File exportedFile;
    public static KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            Key key=keystore.getKey(alias,password);
            if(key instanceof PrivateKey) {
                Certificate cert=keystore.getCertificate(alias);
                PublicKey publicKey=cert.getPublicKey();
                return new KeyPair(publicKey,(PrivateKey)key);
            }
        } catch (UnrecoverableKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyStoreException e) {
        }
        return null;
    }

    /**
     * 写入jks文件
     * @throws Exception
     */
    public void export() throws Exception{
        KeyStore keystore=KeyStore.getInstance(keyStoreType);
        BASE64Encoder encoder=new BASE64Encoder();
        keystore.load(new FileInputStream(keystoreFile),password);
        KeyPair keyPair=getPrivateKey(keystore,alias,password);
        PrivateKey privateKey=keyPair.getPrivate();
        String encoded= encoder.encode(privateKey.getEncoded());
        FileWriter fw=new FileWriter(exportedFile);
        fw.write("—–BEGIN PRIVATE KEY—–\n");
        fw.write(encoded);
        fw.write("\n");
        fw.write("—–END PRIVATE KEY—–");
        fw.close();
    }

    /**
     * 读取私钥
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
          try {
            ks = KeyStore.getInstance("JKS");
            FileInputStream fin;
            fin = new FileInputStream(keyStoreFile);
            ks.load(fin, storeFilePass.toCharArray());
            // 先打开文件,通过别名和密码得到私钥
            prikey = (PrivateKey) ks.getKey(keyAlias, keyAliasPass.toCharArray());

          }catch (Exception e){
          e.printStackTrace();
          }
        return prikey;
    }

    /**
     * 读取公钥
     * @param keyStoreFile
     * @param storeFilePass
     * @param keyAlias
     * @return
     * @throws Exception
     */
    public static PublicKey getPubKey(String keyStoreFile, String storeFilePass, String keyAlias) throws Exception{
        KeyStore ks;
        PublicKey pubkey = null;
        try {
            ks = KeyStore.getInstance("JKS");
            FileInputStream fin;
            fin = new FileInputStream(keyStoreFile);
            //读取公钥
            ks.load(fin, storeFilePass.toCharArray());
            Certificate cert = ks.getCertificate(keyAlias);
            pubkey = cert.getPublicKey();
        }catch (Exception e){
            e.printStackTrace();
        }
        return pubkey;
    }



        /**
         * 生成证书文件
        * CN(Common Name名字与姓氏)
        * OU(Organization Unit组织单位名称)
        * O(Organization组织名称)
        * ST(State州或省份名称)
        * C(Country国家名称)
        * L(Locality城市或区域名称)
        * */
        public static X509Certificate getCert() throws Exception {
            //证书颁发者
            String CertificateIssuer = "C=中国,ST=湖北,L=武汉,O=人民组织,OU=人民单位,CN=人民颁发";
            //证书使用者
            String CertificateUser = "C=中国,ST=湖北,L=武汉,O=人民组织,OU=人民单位,CN=";
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
             X509V3CertificateGenerator x509V3CertificateGenerator = new X509V3CertificateGenerator();
             //设置证书序列号
             x509V3CertificateGenerator.setSerialNumber(BigInteger.TEN);
             //设置证书颁发者
             x509V3CertificateGenerator.setIssuerDN(new X500Principal(CertificateIssuer));
             //设置证书使用者
             x509V3CertificateGenerator.setSubjectDN(new X500Principal(CertificateUser + "sun"));
             //设置证书有效期
             x509V3CertificateGenerator.setNotAfter(new Date(System.currentTimeMillis() + 1000 * 365 * 24 * 3600));
             x509V3CertificateGenerator.setNotBefore(new Date(System.currentTimeMillis()));
            //设置证书签名算法
            x509V3CertificateGenerator.setSignatureAlgorithm("SHA256withRSA");
            x509V3CertificateGenerator.setPublicKey(publicKey);
            //临时bc方法添加都环境变量
            Security.addProvider(new BouncyCastleProvider());
            //私钥生成
            PrivateKey privateKey = keyPair.getPrivate();
            String privateKeyStr = Base64Utils.encode(privateKey.getEncoded());
            System.out.println("公钥"+Base64Utils.encode(publicKey.getEncoded()));
            System.out.println("私钥"+privateKeyStr);
            X509Certificate x509Certificate = x509V3CertificateGenerator.generateX509Certificate(privateKey, "BC");
           //写入文件
           String path = "/home/v9527/文档/data/RSA加密/前端加密文件/证书/证书test/";
           String name = "mycer.cer";
           FileOutputStream fos = new FileOutputStream(path + name);
           fos.write(x509Certificate.getEncoded());
           fos.flush();
           fos.close();
           return x509Certificate;
         }


    public void execCommand(String[] arstringCommand) {
        for (int i = 0; i < arstringCommand.length; i++) {
            System.out.print(arstringCommand[i] + " ");
        }
        try {
            Runtime.getRuntime().exec(arstringCommand);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void execCommand(String arstringCommand) {
        try {
            Runtime.getRuntime().exec(arstringCommand);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


  static   String filePath = "/home/v9527/文档/data/RSA加密/前端加密文件/证书/证书test/";
  static   String cerName = "test.cer";
  static   String jksName = "keystore.crrc";

    /**
     * 生成密钥库
     */
    public void genkey() {
        String[] arstringCommand = new String[] {
            "keytool",
            "-genkey", // -genkey表示生成密钥
            "-validity", // -validity指定证书有效期(单位：天)，这里是36000天
            "36500",
            "-keysize",//     指定密钥长度
            "1024",
            "-alias", // -alias指定别名
            "crrckeystore",
            "-keyalg",  // -keyalg 指定密钥的算法 (如 RSA DSA（如果不指定默认采用DSA）)
            "RSA",
            "-keystore", // -keystore指定存储路径
            filePath + jksName,
            "-dname",// CN=(名字与姓氏), OU=(组织单位名称), O=(组织名称), L=(城市或区域名称),
            // ST=(州或省份名称), C=(单位的两字母国家代码)"
            "CN=CRRC, OU=CRRC, O=CRRC, L=株洲市, ST=湖南省, C=CRRC",
            "-storepass", // 指定密钥库的密码(获取keystore信息所需的密码)
            "crrcwithcienet",
            "-keypass",// 指定别名条目的密码(私钥的密码)
            "crrcwithcienet",
            "-v"// -v 显示密钥库中的证书详细信息
        };
        execCommand(arstringCommand);
    }


    /**
     * 新增证书文件
     */
    public void toExport() {
        String[] arstringCommand = new String[] {
            "keytool",
            "-genkeypair", // - export指定为新增操作
            "-keystore", // -keystore指定keystore文件路径
            filePath + jksName,
            "-alias", // -alias指定别名，这里是testcer
            "testcer",
            "-file",//-指向导出证书路径
            filePath + cerName,
            "-storepass",// 指定密钥库的密码
            "123456"
        };
        execCommand(arstringCommand);
    }


    public static void main(String args[]) throws Exception{
        UserResource export=new UserResource(null,null,null);
        String filePath = "/home/v9527/文档/data/RSA加密/前端加密文件/证书/证书test/keystore.crrc";

        //写入私钥
//        export.keystoreFile=new File(filePath);
//        export.keyStoreType="JKS";
//        export.password="password".toCharArray();
//        export.alias="rollbor";
//        export.exportedFile=new File(filePath);
//        export.export();

        //读取私钥
//        PrivateKey privateKey = getPrivateKey(filePath, "crrcwithcienet", "crrckeystore", "crrcwithcienet");
//        String privcKeyStr = Base64Utils.encode(privateKey.getEncoded());
//        System.out.println("私钥"+privcKeyStr);
//
//        //读取公钥
//        PublicKey pubKey = getPubKey(filePath, "crrcwithcienet", "crrckeystore");
//        String pubKeyStr = Base64Utils.encode(pubKey.getEncoded());
//        System.out.println("公钥"+pubKeyStr);


        //生成证书
        //  getCert();

        //------------生成密钥
        //生成密钥库
      //   export.genkey();
        //导出证书
      //  export.toExport();
    }





}
