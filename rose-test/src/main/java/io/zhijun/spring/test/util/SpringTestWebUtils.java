package io.zhijun.spring.test.util;

import io.zhijun.spring.test.web.servlet.TestServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.UrlPathHelper;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * Spring Test Web 工具类
 */
public abstract class SpringTestWebUtils {

    public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".PATH";

    public static NativeWebRequest createWebRequest() {
        return createWebRequest(r -> {
        });
    }

    public static NativeWebRequest createWebRequest(Consumer<MockHttpServletRequest> requestBuilder) {
        MockHttpServletRequest request = new MockHttpServletRequest(new TestServletContext());
        MockHttpServletResponse response = new MockHttpServletResponse();
        requestBuilder.accept(request);
        return new ServletWebRequest(request, response);
    }

    public static NativeWebRequest createWebRequest(String requestURI) {
        return createWebRequest(request -> {
            request.setRequestURI(requestURI);
            request.setAttribute(PATH_ATTRIBUTE, requestURI);
        });
    }

    public static NativeWebRequest createWebRequestWithParams(Object... params) {
        return createWebRequest(request -> {
            Map<String, String[]> paramsMap = toMap(params);
            request.setParameters(paramsMap);
        });
    }

    public static NativeWebRequest createWebRequestWithHeaders(Object... headers) {
        return createWebRequestWithHeaders(toMap(headers));
    }

    public static NativeWebRequest createWebRequestWithHeaders(Map<String, String> headers) {
        return createWebRequest(request -> {
            headers.forEach(request::addHeader);
        });
    }

    public static NativeWebRequest createPreFightRequest() {
        return createWebRequest(request -> {
            request.setMethod(OPTIONS.name());
            request.addHeader(":METHOD:", request.getMethod());
            request.addHeader(ORIGIN, "*");
            request.addHeader(ACCESS_CONTROL_REQUEST_METHOD, "*");
        });
    }

    public static void clearAttributes(NativeWebRequest request) {
        clearAttributes(request, SCOPE_REQUEST);
    }

    public static void clearAttributes(NativeWebRequest request, int scope) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        switch (scope) {
            case SCOPE_REQUEST:
                clearAttributes(servletRequest.getAttributeNames(), servletRequest::removeAttribute);
                break;
            case SCOPE_SESSION:
                HttpSession session = servletRequest.getSession(false);
                if (session != null) {
                    clearAttributes(session.getAttributeNames(), session::removeAttribute);
                }
                break;
        }
    }

    static void clearAttributes(Enumeration<String> attributeNames, Consumer<String> attributeToRemove) {
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            attributeToRemove.accept(attributeName);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> toMap(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, T> map = new HashMap<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put((String) keyValues[i], (T) keyValues[i + 1]);
        }
        return map;
    }

    private SpringTestWebUtils() {
    }
}
