package io.zhijun.spring.web.test;

import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Spring Web 测试工具，快速创建 {@link NativeWebRequest} 等。
 */
public abstract class SpringTestWebUtils {

    public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".PATH";

    /**
     * 创建默认的 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createWebRequest() {
        return createWebRequest(r -> {});
    }

    /**
     * 通过自定义配置创建 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createWebRequest(Consumer<MockHttpServletRequest> requestBuilder) {
        MockHttpServletRequest request = new MockHttpServletRequest(new MockServletContext());
        MockHttpServletResponse response = new MockHttpServletResponse();
        requestBuilder.accept(request);
        return new ServletWebRequest(request, response);
    }

    /**
     * 创建带有指定 URI 的 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createWebRequest(String requestURI) {
        return createWebRequest(request -> {
            request.setRequestURI(requestURI);
            request.setAttribute(PATH_ATTRIBUTE, requestURI);
        });
    }

    /**
     * 创建带有指定参数的 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createWebRequestWithParams(Object... params) {
        return createWebRequest(request -> {
            request.setParameters(toMap(params));
        });
    }

    /**
     * 创建带有指定请求头的 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createWebRequestWithHeaders(Object... headers) {
        return createWebRequestWithHeaders(toMap(headers));
    }

    /**
     * 创建带有指定请求头的 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createWebRequestWithHeaders(Map<String, String> headers) {
        return createWebRequest(request -> {
            headers.forEach(request::addHeader);
        });
    }

    /**
     * 创建 CORS 预检请求的 {@link NativeWebRequest}。
     */
    public static NativeWebRequest createPreFlightRequest() {
        return createWebRequest(request -> {
            request.setMethod("OPTIONS");
            request.addHeader(":METHOD:", request.getMethod());
            request.addHeader(HttpHeaders.ORIGIN, "*");
            request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
        });
    }

    /**
     * 清除请求范围内的所有属性。
     */
    public static void clearAttributes(NativeWebRequest request) {
        clearAttributes(request, org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 清除指定范围内的所有属性。
     */
    public static void clearAttributes(NativeWebRequest request, int scope) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            return;
        }
        switch (scope) {
            case org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST:
                clearEnumeration(servletRequest.getAttributeNames(), servletRequest::removeAttribute);
                break;
            case org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION:
                HttpSession session = servletRequest.getSession(false);
                if (session != null) {
                    clearEnumeration(session.getAttributeNames(), session::removeAttribute);
                }
                break;
        }
    }

    private static void clearEnumeration(Enumeration<String> names, Consumer<String> remover) {
        while (names.hasMoreElements()) {
            remover.accept(names.nextElement());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> toMap(Object... keyValues) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], String.valueOf(keyValues[i + 1]));
        }
        return result;
    }

    private SpringTestWebUtils() {
    }
}
