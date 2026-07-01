package io.zhijun.spring.webmvc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 响应内容缓存 Filter，包装 {@link HttpServletResponse} 为 {@link ContentCachingResponseWrapper}。
 * <p>配合 {@link #getResponseContentAsString(ServletRequest, ServletResponse)} 获取响应内容。</p>
 */
public class ContentCachingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ContentCachingFilter.class);

    public static final String RESPONSE_CONTENT_ATTRIBUTE = "_ContentCachingFilter_";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 从响应中获取缓存的字符串内容。
     *
     * @param request  当前请求
     * @param response 当前响应（可能是 {@link ContentCachingResponseWrapper}）
     * @return 响应字符串内容，如不可用则返回 {@code null}
     */
    public static String getResponseContentAsString(ServletRequest request, ServletResponse response) {
        if (!(response instanceof ContentCachingResponseWrapper)) {
            return null;
        }
        String cached = (String) request.getAttribute(RESPONSE_CONTENT_ATTRIBUTE);
        if (cached != null) {
            return cached;
        }
        ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
        int contentSize = wrapper.getContentSize();
        if (contentSize == 0) {
            return null;
        }
        try {
            String content = new String(wrapper.getContentAsByteArray(), response.getCharacterEncoding());
            request.setAttribute(RESPONSE_CONTENT_ATTRIBUTE, content);
            return content;
        } catch (IOException e) {
            logger.error("ContentCachingResponseWrapper 内容转换失败: contentSize={}, encoding={}",
                    contentSize, response.getCharacterEncoding(), e);
            return null;
        }
    }
}
