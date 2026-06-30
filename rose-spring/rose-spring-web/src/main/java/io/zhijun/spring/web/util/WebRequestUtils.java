package io.zhijun.spring.web.util;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * WebRequest 工具类，简化版（Servlet-only），提供规则匹配所需的公共方法。
 */
public abstract class WebRequestUtils {

    public static final String METHOD_HEADER_NAME = ":METHOD:";

    /**
     * 获取当前请求的 HTTP 方法。
     */
    @Nullable
    public static String getMethod(NativeWebRequest request) {
        javax.servlet.http.HttpServletRequest servletRequest =
                request.getNativeRequest(javax.servlet.http.HttpServletRequest.class);
        return servletRequest != null ? servletRequest.getMethod() : null;
    }

    /**
     * 判断是否为有效的 CORS 预检请求（OPTIONS + Origin + Access-Control-Request-Method）。
     */
    public static boolean isPreFlightRequest(NativeWebRequest request) {
        String method = getMethod(request);
        return HttpMethod.OPTIONS.matches(method)
                && request.getHeader(HttpHeaders.ORIGIN) != null
                && request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null;
    }

    /**
     * 解析 Content-Type 头为 MediaType。
     */
    @Nullable
    public static MediaType parseContentType(NativeWebRequest request) {
        String contentTypeValue = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentTypeValue == null || contentTypeValue.isEmpty()) return null;
        try {
            return MediaType.parseMediaType(contentTypeValue);
        } catch (InvalidMediaTypeException e) {
            return null;
        }
    }

    /**
     * 判断请求是否包含请求体。
     */
    public static boolean hasBody(NativeWebRequest request) {
        String contentLength = request.getHeader(HttpHeaders.CONTENT_LENGTH);
        String transferEncoding = request.getHeader(HttpHeaders.TRANSFER_ENCODING);
        if (transferEncoding != null && !transferEncoding.isEmpty()) return true;
        if (contentLength != null && !contentLength.trim().equals("0")) return true;
        return false;
    }

    /**
     * 获取已解析的 lookupPath。
     */
    public static String getResolvedLookupPath(NativeWebRequest request) {
        javax.servlet.http.HttpServletRequest servletRequest =
                request.getNativeRequest(javax.servlet.http.HttpServletRequest.class);
        if (servletRequest != null) {
            // 优先使用 Spring 5.3+ 的解析路径
            Object path = request.getAttribute(
                    "org.springframework.web.util.UrlPathHelper.PATH",
                    NativeWebRequest.SCOPE_REQUEST);
            if (path instanceof String) return (String) path;
            return servletRequest.getRequestURI();
        }
        return "";
    }

    private WebRequestUtils() {
    }
}
