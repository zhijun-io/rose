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
package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Logs bean lifecycle events at INFO level.
 *
 * @see BeanListener
 */
public class LoggingBeanListener implements BeanListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingBeanListener.class);

    @Override
    public boolean supports(String beanName) {
        return true;
    }

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        log("onBeanDefinitionReady", beanName, "definition", mergedBeanDefinition);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        log("onBeforeBeanInstantiate", beanName, "definition", mergedBeanDefinition);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args) {
        log("onBeforeBeanInstantiate", beanName, "definition", mergedBeanDefinition, "constructor", constructor, "args", Arrays.toString(args));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args) {
        log("onBeforeBeanInstantiate", beanName, "definition", mergedBeanDefinition, "factoryBean", factoryBean, "factoryMethod", factoryMethod, "args", Arrays.toString(args));
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        log("onAfterBeanInstantiated", beanName, "definition", mergedBeanDefinition, "instance", bean);
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        log("onBeanPropertyValuesReady", beanName, "instance", bean, "PropertyValues", pvs);
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        log("onBeforeBeanInitialize", beanName, "instance", bean);
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        log("onAfterBeanInitialized", beanName, "instance", bean);
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        log("onBeanReady", beanName, "instance", bean);
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        log("onBeforeBeanDestroy", beanName, "instance", bean);
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        log("onAfterBeanDestroy", beanName, "instance", bean);
    }

    private static void log(String event, Object... args) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder(event).append(" - ");
            for (int i = 0; i < args.length; i += 2) {
                if (i > 0) sb.append(" , ");
                sb.append(args[i]).append(" : ").append(args[i + 1]);
            }
            logger.info(sb.toString());
        }
    }
}
