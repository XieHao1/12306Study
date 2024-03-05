/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.index12306.framework.starter.bases;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Application context holder.
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    /*
     * ApplicationContextAware注解作用
     * ApplicationContextAware接口是Spring框架提供的一种方式，允许bean在运行时获取对应用程序上下文（ApplicationContext）的引用。
     * 当一个 bean实现了ApplicationContextAware接口时,Spring在将该bean加载到容器中时,会自动调用setApplicationContext()方法,
     * 并将应用程序上下文作为参数传递给该方法
     */

    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.CONTEXT = applicationContext;
    }

    /**
     * Get ioc container bean by type.
     * 通过 bean 类型从应用程序上下文中获取 bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT.getBean(clazz);
    }

    /**
     * Get ioc container bean by name.
     * 通过 bean 名称从应用程序上下文中获取 bean 实例。
     */
    public static Object getBean(String name) {
        return CONTEXT.getBean(name);
    }

    /**
     * Get ioc container bean by name and type.
     * 通过 bean 名称和类型从应用程序上下文中获取 bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT.getBean(name, clazz);
    }

    /**
     * Get a set of ioc container beans by type.
     * 获取应用程序上下文中指定类型的所有 bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT.getBeansOfType(clazz);
    }

    /**
     * Find whether the bean has annotations.
     * 在指定的 bean 上查找指定类型的注解。
     */
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        return CONTEXT.findAnnotationOnBean(beanName, annotationType);
    }

    /**
     * Get application context.
     * 获取应用程序上下文的实例。
     */
    public static ApplicationContext getInstance() {
        return CONTEXT;
    }
}
