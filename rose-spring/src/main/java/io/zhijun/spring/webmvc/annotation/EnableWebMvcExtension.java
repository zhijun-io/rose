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
package io.zhijun.spring.webmvc.annotation;

import io.zhijun.spring.context.OverrideAnnotationAttributes;
import io.zhijun.spring.webmvc.ReversedProxyHandlerMapping;
import io.zhijun.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.zhijun.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 启用注解以扩展 Spring WebMVC 特性。
 *
 * @see EnableWebMvc
 * @see WebMvcExtensionBeanDefinitionRegistrar
 * @see WebMvcExtensionConfiguration
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@OverrideAnnotationAttributes
@Import(value = WebMvcExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebMvcExtension {

    /**
     * 是否拦截 HandlerMethod 执行。
     * <p>
     * 启用时注册 {@code InterceptingHandlerMethodProcessor}，
     * 在参数解析和方法执行前后回调 {@code HandlerMethodAdvice}。
     *
     * @return {@code true} 为默认值
     */
    boolean interceptHandlerMethods() default true;

    /**
     * 是否自动将容器中所有 {@link HandlerInterceptor} Bean 注册到
     * {@link InterceptorRegistry}。
     * <p>
     * 启用时优先于 {@link #handlerInterceptors()}。
     *
     * @return {@code false} 为默认值
     * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
     */
    boolean registerHandlerInterceptors() default false;

    /**
     * 指定需要注册到 {@link InterceptorRegistry} 的
     * {@link HandlerInterceptor} 类型。
     *
     * @return 拦截器类型数组，默认空
     * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
     */
    Class<? extends HandlerInterceptor>[] handlerInterceptors() default {};

    /**
     * 是否在请求属性中存储 {@code @RequestBody} 参数。
     * <p>
     * 启用时注册 {@link StoringRequestBodyArgumentAdvice}。
     *
     * @return {@code false} 为默认值
     */
    boolean storeRequestBodyArgument() default false;

    /**
     * 是否在请求属性中存储 {@code @ResponseBody} 返回值。
     * <p>
     * 启用时注册 {@link StoringResponseBodyReturnValueAdvice}。
     *
     * @return {@code false} 为默认值
     */
    boolean storeResponseBodyReturnValue() default false;

    /**
     * 是否启用 {@link ReversedProxyHandlerMapping}。
     *
     * @return {@code false} 为默认值
     */
    boolean reversedProxyHandlerMapping() default false;
}
