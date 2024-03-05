package org.index12306.framework.starter.bases.config;

import org.index12306.framework.starter.bases.ApplicationContextHolder;
import org.index12306.framework.starter.bases.init.ApplicationContentPostProcessor;
import org.index12306.framework.starter.bases.safa.FastJsonSafeMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 应用基础自动装配
 */
public class ApplicationBaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextHolder congoApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContentPostProcessor congoApplicationContentPostProcessor(ApplicationContext applicationContext) {
        return new ApplicationContentPostProcessor(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    //只有在 Spring 容器中不存在 FastJsonSafeMode 类型的 Bean 时，才会创建并注册该 Bean
    @ConditionalOnProperty(value = "framework.fastjson.safa-mode", havingValue = "true")
    //只有当配置属性 framework.fastjson.safa-mode 的值为 "true" 时，才会创建并注册这个 Bean
    public FastJsonSafeMode congoFastJsonSafeMode() {
        //Fastjson 的 "autoType" 特性是指在反序列化过程中，允许将 JSON 字符串自动转换为指定的 Java 类型。它提供了一种方便的方式，
        //使得开发人员可以直接将 JSON 数据转换为相应的 Java 对象，而无需手动指定目标类。
        return new FastJsonSafeMode();
    }
}
