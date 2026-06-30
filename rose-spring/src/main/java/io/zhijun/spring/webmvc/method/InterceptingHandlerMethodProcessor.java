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
package io.zhijun.spring.webmvc.method;

import io.zhijun.spring.web.HandlerMethodAdvice;
import io.zhijun.spring.webmvc.HandlerMethodArgumentResolverAdvice;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * 拦截式 HandlerMethod 处理器 — 在参数解析 / 方法执行 / 返回值处理阶段回调
 * {@link HandlerMethodAdvice} 和 {@link HandlerMethodArgumentResolverAdvice}。
 * <p>
 * 职责：作为 {@link HandlerMethodArgumentResolver} 和 {@link HandlerMethodReturnValueHandler}
 * 插入 {@link RequestMappingHandlerAdapter} 首位，同时注册为 {@link HandlerInterceptor}。
 *
 * @see HandlerMethodAdvice
 * @see HandlerMethodArgumentResolverAdvice
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodReturnValueHandler
 * @since 1.0.0
 */
public class InterceptingHandlerMethodProcessor
        implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware,
        HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler,
        HandlerInterceptor, WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(InterceptingHandlerMethodProcessor.class);

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";

    /**
     * HandlerMethod 参数存储的请求属性名前缀
     */
    private static final String ARGUMENTS_ATTRIBUTE_NAME_PREFIX = "HM.ARGS:";

    private ApplicationContext applicationContext;

    private List<HandlerMethodAdvice> handlerMethodAdvices;

    private List<HandlerMethodArgumentResolverAdvice> handlerMethodArgumentResolverAdvices;

    /**
     * 延迟填充的缓存：MethodParameter -> 委托的 HandlerMethodArgumentResolver
     */
    private final Map<MethodParameter, HandlerMethodArgumentResolver> parameterResolverCache = new HashMap<>(256);

    /**
     * 延迟填充的缓存：MethodParameter -> 委托的 HandlerMethodReturnValueHandler
     */
    private final Map<MethodParameter, HandlerMethodReturnValueHandler> returnTypeHandlerCache = new HashMap<>(256);

    /**
     * 适配器的完整解析器列表（不含自身），用于延迟查找
     */
    private List<HandlerMethodArgumentResolver> allResolvers;

    /**
     * 适配器的完整处理器列表（不含自身），用于延迟查找
     */
    private List<HandlerMethodReturnValueHandler> allHandlers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!event.getApplicationContext().equals(this.applicationContext)) {
            return;
        }
        initHandlerMethodAdvices();
        initHandlerMethodArgumentResolverAdvices();
        initRequestMappingHandlerAdapters();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameterResolverCache.containsKey(parameter)) {
            return true;
        }
        HandlerMethodArgumentResolver resolver = resolveArgumentResolver(parameter);
        if (resolver != null) {
            parameterResolverCache.put(parameter, resolver);
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        if (returnTypeHandlerCache.containsKey(returnType)) {
            return true;
        }
        HandlerMethodReturnValueHandler handler = resolveReturnValueHandler(returnType);
        if (handler != null) {
            returnTypeHandlerCache.put(returnType, handler);
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HandlerMethodArgumentResolver resolver = getDelegateResolver(parameter);
        HandlerMethod handlerMethod = resolveHandlerMethod(webRequest);

        // 1. beforeResolveArgument 回调
        beforeResolveArgument(parameter, handlerMethod, mavContainer, webRequest, binderFactory);

        // 2. 委托给实际解析器
        Object argument = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        // 3. 存储已解析的参数
        storeArgument(webRequest, handlerMethod, parameter.getParameterIndex(), argument);

        // 4. afterResolveArgument 回调
        afterResolveArgument(parameter, argument, handlerMethod, mavContainer, webRequest, binderFactory);

        // 5. 如果这是最后一个参数，触发 beforeExecute
        if (handlerMethod != null && isLastParameter(handlerMethod, parameter)) {
            beforeExecute(webRequest, handlerMethod, resolveArguments(webRequest, handlerMethod));
        }

        return argument;
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {
        HandlerMethodReturnValueHandler handler = returnTypeHandlerCache.get(returnType);
        if (handler == null) {
            handler = resolveReturnValueHandler(returnType);
            if (handler != null) {
                returnTypeHandlerCache.put(returnType, handler);
            }
        }

        HandlerMethod handlerMethod = resolveHandlerMethod(webRequest);
        if (handlerMethod != null) {
            afterExecute(webRequest, handlerMethod, returnValue);
        }

        if (handler != null) {
            handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.getMethod().getParameterCount() == 0) {
                beforeExecute(new ServletWebRequest(request), handlerMethod, new Object[0]);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception error) throws Exception {
        if (error != null && handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ServletWebRequest webRequest = new ServletWebRequest(request);
            Object[] arguments = resolveArguments(webRequest, handlerMethod);
            afterExecute0(webRequest, handlerMethod, arguments, null, error);
        }
    }

    // ========== 初始化 ==========

    private void initHandlerMethodAdvices() {
        Map<String, HandlerMethodAdvice> beans = applicationContext.getBeansOfType(HandlerMethodAdvice.class);
        List<HandlerMethodAdvice> advices = new ArrayList<>(beans.values());
        sort(advices);
        this.handlerMethodAdvices = advices;
        if (logger.isTraceEnabled()) {
            logger.trace("已初始化 {} 个 HandlerMethodAdvice", advices.size());
        }
    }

    private void initHandlerMethodArgumentResolverAdvices() {
        Map<String, HandlerMethodArgumentResolverAdvice> beans =
                applicationContext.getBeansOfType(HandlerMethodArgumentResolverAdvice.class);
        List<HandlerMethodArgumentResolverAdvice> advices = new ArrayList<>(beans.values());
        sort(advices);
        this.handlerMethodArgumentResolverAdvices = advices;
        if (logger.isTraceEnabled()) {
            logger.trace("已初始化 {} 个 HandlerMethodArgumentResolverAdvice", advices.size());
        }
    }

    private void initRequestMappingHandlerAdapters() {
        Map<String, RequestMappingHandlerAdapter> adapters =
                applicationContext.getBeansOfType(RequestMappingHandlerAdapter.class);
        for (RequestMappingHandlerAdapter adapter : adapters.values()) {
            List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(adapter.getArgumentResolvers());
            List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(adapter.getReturnValueHandlers());

            // 保存完整列表（不含自身），供延迟查找使用
            this.allResolvers = new ArrayList<>(resolvers);
            this.allHandlers = new ArrayList<>(handlers);

            // 将当前实例插入首位
            resolvers.add(0, this);
            handlers.add(0, this);

            adapter.setArgumentResolvers(resolvers);
            adapter.setReturnValueHandlers(handlers);
        }
    }

    // ========== 解析器/处理器的延迟查找 ==========

    @Nullable
    private HandlerMethodArgumentResolver resolveArgumentResolver(MethodParameter parameter) {
        for (HandlerMethodArgumentResolver resolver : allResolvers) {
            if (resolver.supportsParameter(parameter)) {
                return resolver;
            }
        }
        return null;
    }

    @Nullable
    private HandlerMethodReturnValueHandler resolveReturnValueHandler(MethodParameter returnType) {
        for (HandlerMethodReturnValueHandler handler : allHandlers) {
            if (handler.supportsReturnType(returnType)) {
                return handler;
            }
        }
        return null;
    }

    private HandlerMethodArgumentResolver getDelegateResolver(MethodParameter parameter) {
        HandlerMethodArgumentResolver resolver = parameterResolverCache.get(parameter);
        if (resolver == null) {
            resolver = resolveArgumentResolver(parameter);
            if (resolver != null) {
                parameterResolverCache.put(parameter, resolver);
            }
        }
        return resolver;
    }

    // ========== Advice 回调 ==========

    private void beforeResolveArgument(MethodParameter parameter, @Nullable HandlerMethod handlerMethod,
                                       ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                       WebDataBinderFactory binderFactory) throws Exception {
        for (HandlerMethodArgumentResolverAdvice advice : handlerMethodArgumentResolverAdvices) {
            advice.beforeResolveArgument(parameter, mavContainer, webRequest, binderFactory);
        }
        if (handlerMethod != null) {
            for (HandlerMethodAdvice advice : handlerMethodAdvices) {
                advice.beforeResolveArgument(parameter, handlerMethod, webRequest);
            }
        }
    }

    private void afterResolveArgument(MethodParameter parameter, Object argument, @Nullable HandlerMethod handlerMethod,
                                      ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) throws Exception {
        for (HandlerMethodArgumentResolverAdvice advice : handlerMethodArgumentResolverAdvices) {
            advice.afterResolveArgument(parameter, argument, mavContainer, webRequest, binderFactory);
        }
        if (handlerMethod != null) {
            for (HandlerMethodAdvice advice : handlerMethodAdvices) {
                advice.afterResolveArgument(parameter, argument, handlerMethod, webRequest);
            }
        }
    }

    private void beforeExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, Object[] arguments) throws Exception {
        for (HandlerMethodAdvice advice : handlerMethodAdvices) {
            advice.beforeExecuteMethod(handlerMethod, arguments, webRequest);
        }
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, @Nullable Object returnValue) throws Exception {
        afterExecute0(webRequest, handlerMethod, resolveArguments(webRequest, handlerMethod), returnValue, null);
    }

    private void afterExecute0(NativeWebRequest webRequest, HandlerMethod handlerMethod, Object[] arguments,
                               @Nullable Object returnValue, @Nullable Throwable error) throws Exception {
        for (HandlerMethodAdvice advice : handlerMethodAdvices) {
            advice.afterExecuteMethod(handlerMethod, arguments, returnValue, error, webRequest);
        }
    }

    // ========== 工具方法 ==========

    @Nullable
    private static HandlerMethod resolveHandlerMethod(NativeWebRequest webRequest) {
        if (webRequest instanceof ServletWebRequest) {
            HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod) {
                return (HandlerMethod) handler;
            }
        }
        return null;
    }

    private static boolean isLastParameter(HandlerMethod handlerMethod, MethodParameter parameter) {
        return parameter.getParameterIndex() == handlerMethod.getMethod().getParameterCount() - 1;
    }

    private static void storeArgument(NativeWebRequest webRequest, @Nullable HandlerMethod handlerMethod,
                                       int index, Object argument) {
        if (handlerMethod == null || argument == null) {
            return;
        }
        Object[] arguments = resolveArguments(webRequest, handlerMethod);
        if (index < arguments.length) {
            arguments[index] = argument;
        }
    }

    static Object[] resolveArguments(NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        String attrName = getArgumentsAttributeName(method);
        if (webRequest instanceof ServletWebRequest) {
            HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
            Object[] arguments = (Object[]) request.getAttribute(attrName);
            if (arguments == null) {
                arguments = new Object[method.getParameterCount()];
                request.setAttribute(attrName, arguments);
            }
            return arguments;
        }
        return new Object[method.getParameterCount()];
    }

    private static String getArgumentsAttributeName(Method method) {
        return ARGUMENTS_ATTRIBUTE_NAME_PREFIX + method;
    }
}
