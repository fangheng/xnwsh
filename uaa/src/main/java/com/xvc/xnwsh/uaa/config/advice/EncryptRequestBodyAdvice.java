package com.xvc.xnwsh.uaa.config.advice;

import com.xvc.xnwsh.uaa.config.annotation.Decrypt;
import com.xvc.xnwsh.uaa.config.auto.SecurityProperties;
import com.xvc.xnwsh.uaa.web.rest.util.RSAUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import static com.xvc.xnwsh.uaa.config.advice.DecryptHttpInputMessage.decryptBody;
import static com.xvc.xnwsh.uaa.config.advice.EncryptRequestBodyAdvice.RSAENCRYPT_REQUEST_DATA_KEY;

/**
 * 请求处理类（目前仅对requestbody有效）
 * 对加了@Decrypt注解的方法的数据进行解密操作
 */
@ControllerAdvice
public class EncryptRequestBodyAdvice implements RequestBodyAdvice {

    private Logger logger = LoggerFactory.getLogger(EncryptRequestBodyAdvice.class);

    protected static final String RSAENCRYPT_REQUEST_DATA_KEY = "encryptData";

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        DecryptHttpInputMessage decryptHttpInputMessage = null;
        try{
         boolean isDecrypt = false;
         //判断当前请求是否需要解密
         boolean setUpDecrypt = parameter.getMethod().isAnnotationPresent(Decrypt.class);
         //判断数据是否为正常数据请求而不需要解密
         InputStream inputMessageBody = inputMessage.getBody();
         HttpHeaders httpHeaders = inputMessage.getHeaders();
         String messageBody = IOUtils.toString(inputMessageBody,securityProperties.getCharset());
            decryptHttpInputMessage = new DecryptHttpInputMessage(httpHeaders, messageBody,securityProperties.getCharset());

        boolean normalRestIsDecrypt = decryptBody(messageBody, true) == null ? false : true;
         if (setUpDecrypt && normalRestIsDecrypt) {
              isDecrypt = true;
         }
        if (isDecrypt){
            logger.info(parameter.getMethod().getName() + "request data being Decrypt");
            return new DecryptHttpInputMessage(httpHeaders, messageBody, securityProperties.getPrivateKey(),securityProperties.getCharset());
        }else{
            return decryptHttpInputMessage;
        }
    } catch (Exception e){
            logger.error("解密失败",e);
            return  decryptHttpInputMessage;
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}

 class DecryptHttpInputMessage implements HttpInputMessage {
    private HttpHeaders headers;
    private InputStream body;
    public DecryptHttpInputMessage(HttpHeaders httpHeaders, String messageBody, String privateKey, String charset) throws Exception {
            if (StringUtils.isEmpty(privateKey)) {
                throw new IllegalArgumentException("privateKey is null");
            }
            //获取请求内容
            this.headers = httpHeaders;
            String content = decryptBody(messageBody, false);
            String decryptBodys = new String(RSAUtils.decryptByPrivateKey(Base64.decodeBase64(content), privateKey), charset);
            this.body = IOUtils.toInputStream(decryptBodys, charset);
    }

     //因为流只能消费一次,所以将数据保存下来再次生成流
     public DecryptHttpInputMessage(HttpHeaders httpHeaders, String messageBody,String charset) throws Exception {
         this.headers = httpHeaders;
         this.body = IOUtils.toInputStream(messageBody, charset);
     }

     /**
     * requestBody提取出数据
     * @param requestData
     * @param validate 是否只进行验证
     * @return
     * @throws JSONException
     */
    public static String decryptBody(String requestData, boolean validate)throws JSONException{
        Assert.hasLength(requestData,"request data must not be null");
        JSONObject jsonObject = new JSONObject(requestData);
        String decryRequestData;
        boolean isDecrypt = jsonObject.has(RSAENCRYPT_REQUEST_DATA_KEY);
        if (validate && !isDecrypt){
            return null;
        }else
            decryRequestData = jsonObject.getString(RSAENCRYPT_REQUEST_DATA_KEY);
        return decryRequestData;
    }

    @Override
    public InputStream getBody() throws IOException {
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}
