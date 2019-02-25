package com.xvc.xnwsh.uaa.config.annotation;

import com.xvc.xnwsh.uaa.config.selector.SecurityImportSelector;
import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

/**
 * 引入自动配置
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SecurityImportSelector.class})
public @interface EnableSecurity {

}
