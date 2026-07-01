package io.zhijun.spring.test.web.context.request;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.ServletContext;

/**
 * Mock {@link ServletWebRequest} for testing.
 */
public class MockServletWebRequest extends ServletWebRequest {

    public MockServletWebRequest() {
        this(new MockServletContext());
    }

    public MockServletWebRequest(ServletContext servletContext) {
        super(new MockHttpServletRequest(servletContext), new MockHttpServletResponse());
    }

    public MockHttpServletRequest getMockHttpServletRequest() {
        return getNativeRequest(MockHttpServletRequest.class);
    }

    public MockHttpServletResponse getMockHttpServletResponse() {
        return getNativeResponse(MockHttpServletResponse.class);
    }
}
