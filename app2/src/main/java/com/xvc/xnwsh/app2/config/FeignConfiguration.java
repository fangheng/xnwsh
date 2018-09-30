package com.xvc.xnwsh.app2.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.xvc.xnwsh.app2")
public class FeignConfiguration {

}
