package io.zhijun.spring.test.web.servlet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Test {@link Servlet} implementation.
 */
public class TestServlet extends HttpServlet {

    public static final String DEFAULT_SERVLET_NAME = "testServlet";
    public static final Class<? extends Servlet> SERVLET_CLASS = TestServlet.class;
    public static final String SERVLET_CLASS_NAME = SERVLET_CLASS.getName();
    public static final String DEFAULT_SERVLET_URL_PATTERN = "/" + DEFAULT_SERVLET_NAME;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    protected void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("Hello World!");
    }
}
