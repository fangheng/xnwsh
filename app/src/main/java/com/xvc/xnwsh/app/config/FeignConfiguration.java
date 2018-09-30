package com.xvc.xnwsh.app.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.xvc.xnwsh.app")
public class FeignConfiguration {

}
