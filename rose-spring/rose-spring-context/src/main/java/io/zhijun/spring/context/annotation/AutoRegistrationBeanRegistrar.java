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

package io.zhijun.spring.context.annotation;
import io.zhijun.spring.context.AnnotatedBeanCapableImportBeanDefinitionRegistrar;

import io.zhijun.spring.context.config.AutoRegistrationBean;
import io.zhijun.spring.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

import static io.zhijun.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.zhijun.spring.context.annotation.EnableAutoRegistrationBean.BEANS_AUTO_REGISTERED_PROEPRTY_NAME;
import static io.zhijun.spring.context.config.AutoRegistrationBean.getAutoRegisteredPropertyName;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * {@link AnnotatedBeanCapableImportCandidate} class for {@link EnableAutoRegistrationBean}
 *
 * @see EnableAutoRegistrationBean
 * @see AutoRegistrationBean
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
class AutoRegistrationBeanRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAutoRegistrationBean> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableAutoRegistrationBean> annotationAttributes) {
        List<AutoRegistrationBean> autoRegistrationBeans = loadFactories(AutoRegistrationBean.class, super.classLoader);
        registerAutoRegisteredBeans(autoRegistrationBeans, registry);
    }

    @Override
    protected boolean isEnabled(AnnotationMetadata metadata) {
        if (!isEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace("The @EnableAutoRegistrationBean was disabled by property[{} = false]",
                        BEANS_AUTO_REGISTERED_PROEPRTY_NAME);
            }
            return false;
        }
        return super.isEnabled(metadata);
    }

    private boolean isEnabled() {
        return this.environment.getProperty(BEANS_AUTO_REGISTERED_PROEPRTY_NAME, boolean.class, true);
    }

    private void registerAutoRegisteredBeans(List<AutoRegistrationBean> autoRegistrationBeans, BeanDefinitionRegistry registry) {
        for (AutoRegistrationBean autoRegistrationBean : autoRegistrationBeans) {
            registerAutoRegisteredBean(autoRegistrationBean, registry);
        }
    }

    private void registerAutoRegisteredBean(AutoRegistrationBean autoRegistrationBean, BeanDefinitionRegistry registry) {
        String beanName = autoRegistrationBean.getBeanName();
        if (registry.containsBeanDefinition(beanName)) {
            if (logger.isWarnEnabled()) {
                logger.warn("The BeanDefinition[{}] was registered already!", autoRegistrationBean.getDescription());
            }
            return;
        }

        if (!autoRegistrationBean.isAutoRegistered(this.environment)) {
            if (logger.isTraceEnabled()) {
                logger.trace("The Bean[{}] is not auto registered because of the property[{} = false]",
                        autoRegistrationBean.getDescription(), getAutoRegisteredPropertyName(beanName));
            }
            return;
        }

        Class<AutoRegistrationBean> beanType = (Class<AutoRegistrationBean>) autoRegistrationBean.getBeanType();
        String scope = autoRegistrationBean.getScope();
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(beanType, () -> autoRegistrationBean)
                .setScope(scope);

        autoRegistrationBean.customize(beanDefinitionBuilder);

        registerBeanDefinition(registry, beanName, beanDefinitionBuilder.getBeanDefinition());
    }
}
