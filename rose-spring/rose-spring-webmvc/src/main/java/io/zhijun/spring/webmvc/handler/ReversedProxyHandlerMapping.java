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
package io.zhijun.spring.webmvc.handler;

import io.zhijun.spring.webmvc.annotation.EnableWebMvcExtension;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * 反向代理 HandlerMapping — 根据请求头中的 ID 直接定位 handler，
 * 避免 URL 匹配开销。适用于 Spring Cloud Gateway / Zuul 等反向代理场景。
 * <p>
 * 请求头名：{@link #ID_HEADER_NAME}，值为 {@link #setHandlerCache(Map) handlerCache}
 * 对应的 key。
 * <p>
 * 优先级为 {@link #DEFAULT_ORDER}（{@link #HIGHEST_PRECEDENCE} + 1），高于
 * 默认的 {@link RequestMappingHandlerMapping}（0）。
 *
 * @see HandlerMapping
 * @see AbstractHandlerMapping
 * @see EnableWebMvcExtension#reversedProxyHandlerMapping()
 * @since 1.0.0
 */
public class ReversedProxyHandlerMapping extends AbstractHandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(ReversedProxyHandlerMapping.class);

    /**
     * 请求头名称，标识 WebEndpointMapping ID
     */
    public static final String ID_HEADER_NAME = "microsphere_wem_id";

    /**
     * 默认优先级
     */
    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;

    /**
     * ID -> handler 缓存
     */
    private Map<Integer, Object> handlerCache = Collections.emptyMap();

    public ReversedProxyHandlerMapping() {
        setOrder(DEFAULT_ORDER);
    }

    /**
     * 设置 handler 缓存。
     *
     * @param handlerCache ID -> handler（例如 {@link HandlerMethod}、{@link Controller}）的映射
     */
    public void setHandlerCache(Map<Integer, Object> handlerCache) {
        this.handlerCache = handlerCache != null ? handlerCache : Collections.emptyMap();
        if (logger.isDebugEnabled()) {
            logger.debug("设置 handlerCache，大小：{}", handlerCache != null ? handlerCache.size() : 0);
        }
    }

    @Override
    @Nullable
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        int id = request.getIntHeader(ID_HEADER_NAME);
        if (id == -1) {
            if (logger.isTraceEnabled()) {
                logger.trace("请求头 [{}] 不存在", ID_HEADER_NAME);
            }
            return null;
        }
        Object handler = handlerCache.get(id);
        if (handler == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("handlerCache 中未找到 ID [{}]", id);
            }
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("通过 ID [{}] 找到 handler [{}]", id, handler);
        }
        // 返回原始 handler，AbstractHandlerMapping.getHandler() 会负责包装 HandlerExecutionChain
        return handler;
    }
}
