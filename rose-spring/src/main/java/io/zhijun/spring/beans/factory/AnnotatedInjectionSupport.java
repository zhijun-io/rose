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
package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationAttributes;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import static java.util.Arrays.copyOf;

/**
 * Support classes extracted from {@link AnnotatedInjectionBeanPostProcessor} to reduce file size (SRP).
 */

abstract class AnnotationInjectedElement<M extends Member> extends InjectedElement {

    private final AnnotationAttributes attributes;

    private final boolean required;

    protected AnnotationInjectedElement(M member, PropertyDescriptor pd, AnnotationAttributes attributes, boolean required) {
        super(member, pd);
        this.attributes = attributes;
        this.required = required;
    }

    public final AnnotationAttributes getAttributes() {
        return attributes;
    }

    public final boolean isRequired() {
        return required;
    }

    @SuppressWarnings("unchecked")
    public final M getInjectionPoint() {
        return (M) getMember();
    }
}

class AnnotatedFieldElement extends AnnotationInjectedElement<Field> {

    private final AnnotatedInjectionBeanPostProcessor parent;

    private volatile boolean cached = false;

    private volatile Object cachedFieldValue;

    protected AnnotatedFieldElement(AnnotatedInjectionBeanPostProcessor parent, Field field, AnnotationAttributes attributes, boolean required) {
        super(field, null, attributes, required);
        this.parent = parent;
    }

    @Override
    protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        Field field = getInjectionPoint();
        Object value;
        if (this.cached) {
            try {
                value = parent.resolvedCachedArgument(beanName, this.cachedFieldValue);
            } catch (NoSuchBeanDefinitionException ex) {
                // Unexpected removal of target bean for cached argument -> re-resolve
                value = resolveFieldValue(field, bean, beanName, pvs);
            }
        } else {
            value = resolveFieldValue(field, bean, beanName, pvs);
        }
        org.springframework.util.ReflectionUtils.makeAccessible(field);
        org.springframework.util.ReflectionUtils.setField(field, bean, value);
    }

    @Nullable
    private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        Object value = parent.resolveInjectedFieldValue(bean, beanName, pvs, this);
        if (value == null) {
            boolean required = isRequired();
            DependencyDescriptor desc = new DependencyDescriptor(field, required);
            desc.setContainingClass(bean.getClass());
            Set<String> injectedBeanNames = new java.util.LinkedHashSet<>(1);
            value = parent.resolveDependency(desc, beanName, injectedBeanNames);
            cacheFieldValue(field, desc, beanName, injectedBeanNames, value, required);
        }
        return value;
    }

    private void cacheFieldValue(Field field, DependencyDescriptor desc, String beanName, Set<String> injectedBeanNames, Object value, boolean required) {
        synchronized (this) {
            if (!this.cached) {
                Object cachedFieldValue = null;
                if (value != null || required) {
                    cachedFieldValue = desc;
                    parent.registerDependentBeans(beanName, injectedBeanNames);
                    if (injectedBeanNames.size() == 1) {
                        String autowiredBeanName = injectedBeanNames.iterator().next();
                        if (parent.getBeanFactory().containsBean(autowiredBeanName) &&
                                parent.getBeanFactory().isTypeMatch(autowiredBeanName, field.getType())) {
                            cachedFieldValue = new ShortcutDependencyDescriptor(
                                    desc, autowiredBeanName, field.getType());
                        }
                    }
                }
                this.cachedFieldValue = cachedFieldValue;
                this.cached = true;
            }
        }
    }
}

class AnnotatedMethodElement extends AnnotationInjectedElement<Method> {

    private final AnnotatedInjectionBeanPostProcessor parent;

    private volatile boolean cached = false;

    private volatile Object[] cachedMethodArguments;

    protected AnnotatedMethodElement(AnnotatedInjectionBeanPostProcessor parent, Method method, PropertyDescriptor pd, AnnotationAttributes attributes, boolean required) {
        super(method, pd, attributes, required);
        this.parent = parent;
    }

    @Override
    protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        if (checkPropertySkipping(pvs)) {
            return;
        }
        Method method = getInjectionPoint();
        Object[] arguments;
        if (this.cached) {
            try {
                arguments = resolveCachedArguments(beanName);
            } catch (NoSuchBeanDefinitionException ex) {
                // Unexpected removal of target bean for cached argument -> re-resolve
                arguments = resolveMethodArguments(method, bean, beanName, pvs);
            }
        } else {
            arguments = resolveMethodArguments(method, bean, beanName, pvs);
        }
        if (arguments != null) {
            org.springframework.util.ReflectionUtils.makeAccessible(method);
            org.springframework.util.ReflectionUtils.invokeMethod(method, bean, arguments);
        }
    }

    @Nullable
    private Object[] resolveCachedArguments(@Nullable String beanName) {
        Object[] cachedMethodArguments = this.cachedMethodArguments;
        if (cachedMethodArguments == null) {
            return null;
        }
        Object[] arguments = new Object[cachedMethodArguments.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = parent.resolvedCachedArgument(beanName, cachedMethodArguments[i]);
        }
        return arguments;
    }

    @Nullable
    private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        Object[] arguments = parent.resolveInjectedMethodArguments(bean, beanName, pvs, this);
        if (arguments == null) {
            boolean required = isRequired();
            int argumentCount = method.getParameterCount();
            arguments = new Object[argumentCount];
            DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
            Set<String> injectedBeanNames = new java.util.LinkedHashSet<>(argumentCount);
            for (int i = 0; i < arguments.length; i++) {
                MethodParameter methodParam = new MethodParameter(method, i);
                DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, required);
                currDesc.setContainingClass(bean.getClass());
                descriptors[i] = currDesc;
                Object arg = parent.resolveDependency(currDesc, beanName, injectedBeanNames);
                if (arg == null && !required) {
                    arguments = null;
                    break;
                }
                arguments[i] = arg;
            }
            synchronized (this) {
                if (!this.cached) {
                    if (arguments != null) {
                        DependencyDescriptor[] cachedMethodArguments = copyOf(descriptors, arguments.length);
                        parent.registerDependentBeans(beanName, injectedBeanNames);
                        if (injectedBeanNames.size() == argumentCount) {
                            Iterator<String> it = injectedBeanNames.iterator();
                            Class<?>[] paramTypes = method.getParameterTypes();
                            for (int i = 0; i < paramTypes.length; i++) {
                                String autowiredBeanName = it.next();
                                if (parent.getBeanFactory().containsBean(autowiredBeanName) &&
                                        parent.getBeanFactory().isTypeMatch(autowiredBeanName, paramTypes[i])) {
                                    cachedMethodArguments[i] = new ShortcutDependencyDescriptor(
                                            descriptors[i], autowiredBeanName, paramTypes[i]);
                                }
                            }
                        }
                        this.cachedMethodArguments = cachedMethodArguments;
                    } else {
                        this.cachedMethodArguments = null;
                    }
                    this.cached = true;
                }
            }
        }
        return arguments;
    }
}

@SuppressWarnings("serial")
class ShortcutDependencyDescriptor extends DependencyDescriptor {

    private final String shortcut;

    private final Class<?> requiredType;

    public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut, Class<?> requiredType) {
        super(original);
        this.shortcut = shortcut;
        this.requiredType = requiredType;
    }

    @Override
    public Object resolveShortcut(BeanFactory beanFactory) {
        return beanFactory.getBean(this.shortcut, this.requiredType);
    }
}
