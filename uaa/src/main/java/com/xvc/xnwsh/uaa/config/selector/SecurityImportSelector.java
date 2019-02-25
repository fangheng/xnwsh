package com.xvc.xnwsh.uaa.config.selector;

import com.xvc.xnwsh.uaa.config.annotation.EnableSecurity;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import java.util.List;

public class SecurityImportSelector implements ImportSelector {

    /**
     * 获取自动配置的类全名及包,这里用到了Spring框架原有的一个工具类 SpringFactoriesLoader
     * @param annotationMetadata
     * @return
     */
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(this.getSpringFactoriesLoaderFactoryClass(), null);
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you are using a custom packaging, make sure that file is correct.");
        return StringUtils.toStringArray(configurations);
    }

    protected Class<?> getSpringFactoriesLoaderFactoryClass() {
        return EnableSecurity.class;
    }
}
