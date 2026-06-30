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
package io.zhijun.spring.config;

import io.zhijun.spring.beans.factory.AutowireCandidateResolvingListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import static io.zhijun.spring.beans.factory.BeanRegistrar.registerBean;
import static io.zhijun.spring.beans.factory.BeanRegistrar.registerBeanDefinition;

/**
 * Listens for autowire candidate resolution events to collect configuration properties.
 * <p>
 * Registers itself and the {@link ConfigurationPropertyRepository} as Spring beans on initialization.
 *
 * <h3>Autowire Candidate Resolving Example</h3>
 * <pre>{@code
 * @Component
 * public class MyAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {
 *     @Override
 *     public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
 *         System.out.println("Suggested value for dependency " + descriptor + ": " + suggestedValue);
 *     }
 *
 *     @Override
 *     public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
 *         System.out.println("Lazy proxy created for " + descriptor + " in bean " + beanName);
 *     }
 * }
 * }</pre>
 *
 * <h3>Bean Registration</h3>
 * <pre>{@code
 * BeanDefinitionRegistry registry = ...;
 * registerBean(registry, CollectingConfigurationPropertyListener.BEAN_NAME, new CollectingConfigurationPropertyListener());
 * }</pre>
 *
 * @see AutowireCandidateResolvingListener
 * @see ConfigurationPropertyRepository
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class CollectingConfigurationPropertyListener implements AutowireCandidateResolvingListener, BeanFactoryAware {

    public static final String BEAN_NAME = "collectingConfigurationPropertyListener";

    private BeanFactory beanFactory;

    private ConfigurationPropertyRepository repository;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        registerBeanDefinition((BeanDefinitionRegistry) beanFactory, ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        registerBean((BeanDefinitionRegistry) beanFactory, BEAN_NAME, this);
    }

    private ConfigurationPropertyRepository getRepository() {
        if (repository == null) {
            repository = this.beanFactory.getBean(ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        }
        return repository;
    }

}
