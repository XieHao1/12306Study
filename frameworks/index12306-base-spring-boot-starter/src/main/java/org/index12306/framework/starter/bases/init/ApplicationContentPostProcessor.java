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

package org.index12306.framework.starter.bases.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用初始化后置处理器，防止Spring事件被多次执行
 */
@RequiredArgsConstructor
//用于自动生成一个包含所有被 final 修饰的成员变量的构造函数
//当应用程序启动完成、初始化完成，并且可以开始处理请求时，Spring Boot会发布ApplicationReadyEvent事件。
//这个事件通常用于在应用程序完全启动并处于可用状态时执行一些额外的任务，比如初始化一些数据、启动定时任务等。
public class ApplicationContentPostProcessor implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext applicationContext;

    /**
     * 执行标识，确保Spring事件 {@link ApplicationReadyEvent} 有且执行一次
     */
    private final AtomicBoolean executeOnlyOnce = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!executeOnlyOnce.compareAndSet(false, true)) {
            return;
        }
        applicationContext.publishEvent(new ApplicationInitializingEvent(this));
    }
}
