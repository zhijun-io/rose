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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Composite {@link BeanListener} that collects all {@link BeanListener} beans
 * from the {@link ConfigurableListableBeanFactory} and dispatches events to each.
 * <p>
 * Registration as a Spring bean via {@link #registerBean(BeanDefinitionRegistry)}.
 * <p>
 * (借鉴 microsphere-spring {@code BeanListeners})
 *
 * @see BeanListener
 */
public class BeanListeners implements BeanListener {

    private static final Logger logger = LoggerFactory.getLogger(BeanListeners.class);

    private static final String BEAN_NAME = "beanEventListeners";

    private final List<NamedBeanHolder<BeanListener>> listeners;

    private final List<String> readyBeanNames;

    public BeanListeners(ConfigurableListableBeanFactory beanFactory) {
        this.readyBeanNames = getReadyBeanNames(beanFactory);
        this.listeners = resolveBeanListeners(beanFactory, BeanListener.class);
    }

    private static <T> List<NamedBeanHolder<T>> resolveBeanListeners(
            ConfigurableListableBeanFactory beanFactory, Class<T> listenerType) {
        List<NamedBeanHolder<T>> holders = new ArrayList<>();
        for (String name : beanFactory.getBeanNamesForType(listenerType)) {
            holders.add(new NamedBeanHolder<>(name, beanFactory.getBean(name, listenerType)));
        }
        AnnotationAwareOrderComparator.sort(holders);
        return holders;
    }

    static List<String> getReadyBeanNames(ConfigurableListableBeanFactory beanFactory) {
        String[] singletonNames = beanFactory.getSingletonNames();
        return new ArrayList<>(Arrays.asList(singletonNames));
    }

    void setReadyBeanNames(List<String> readyBeanNames) {
        this.readyBeanNames.clear();
        this.readyBeanNames.addAll(readyBeanNames);
    }

    void registerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(BeanListeners.class, () -> this);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(BEAN_NAME, builder.getBeanDefinition());
    }

    static BeanListeners getBean(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, BeanListeners.class);
    }

    private boolean isIgnored(String beanName) {
        return this.readyBeanNames.contains(beanName) || BEAN_NAME.equals(beanName);
    }

    @Override
    public boolean supports(String beanName) {
        return true;
    }

    // ---- Dispatch all lifecycle events ----

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        dispatch(beanName, l -> l.onBeanDefinitionReady(beanName, mergedBeanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        dispatch(beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Constructor<?> constructor, Object[] args) {
        dispatch(beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, constructor, args));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Object factoryBean, Method factoryMethod, Object[] args) {
        dispatch(beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, factoryBean, factoryMethod, args));
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        dispatch(beanName, l -> l.onAfterBeanInstantiated(beanName, mergedBeanDefinition, bean));
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        dispatch(beanName, l -> l.onBeanPropertyValuesReady(beanName, bean, pvs));
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        dispatch(beanName, l -> l.onBeforeBeanInitialize(beanName, bean));
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        dispatch(beanName, l -> l.onAfterBeanInitialized(beanName, bean));
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        dispatch(beanName, l -> l.onBeanReady(beanName, bean));
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        dispatch(beanName, l -> l.onBeforeBeanDestroy(beanName, bean));
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        dispatch(beanName, l -> l.onAfterBeanDestroy(beanName, bean));
    }

    private void dispatch(String beanName, java.util.function.Consumer<BeanListener> action) {
        if (isIgnored(beanName)) {
            return;
        }
        for (NamedBeanHolder<BeanListener> holder : this.listeners) {
            BeanListener listener = holder.getBeanInstance();
            try {
                if (!listener.supports(beanName)) {
                    continue;
                }
                action.accept(listener);
            } catch (Exception ex) {
                logger.warn("BeanListener [{}] failed for bean [{}]",
                        holder.getBeanName(), beanName, ex);
            }
        }
    }
}
