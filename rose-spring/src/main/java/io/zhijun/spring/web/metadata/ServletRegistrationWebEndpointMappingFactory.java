package io.zhijun.spring.web.metadata;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import static java.util.Collections.unmodifiableSet;
 
 /**
  * 从 {@link ServletRegistration} 创建 {@link WebEndpointMapping} 的工厂。
  */
 public class ServletRegistrationWebEndpointMappingFactory extends RegistrationWebEndpointMappingFactory<ServletRegistration> {
 
     private static final Map<String, String> METHOD_NAMES_TO_HTTP_METHODS = buildMethodNamesMap();

     private static Map<String, String> buildMethodNamesMap() {
         Map<String, String> map = new LinkedHashMap<>();
         map.put("doGet", "GET");
         map.put("doPost", "POST");
         map.put("doPut", "PUT");
         map.put("doDelete", "DELETE");
         map.put("doHead", "HEAD");
         map.put("doOptions", "OPTIONS");
         map.put("doTrace", "TRACE");
         return Collections.unmodifiableMap(map);
     }
 
     static final Collection<String> ALL_HTTP_METHODS = unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
             "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "PATCH"
    )));

    public ServletRegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected Collection<String> getMethods(ServletRegistration registration) {
        String className = registration.getClassName();
        ClassLoader classLoader = servletContext.getClassLoader();
        try {
            Class<?> servletClass = classLoader.loadClass(className);
            return getMethods(servletClass);
        } catch (ClassNotFoundException e) {
            return ALL_HTTP_METHODS;
        }
    }

    protected Collection<String> getMethods(Class<?> servletClass) {
        if (HttpServlet.class.isAssignableFrom(servletClass)) {
            return getMethodsFromHttpServlet(servletClass);
        }
        return ALL_HTTP_METHODS;
    }

    protected Collection<String> getMethodsFromHttpServlet(Class<?> servletClass) {
        Set<String> methods = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : METHOD_NAMES_TO_HTTP_METHODS.entrySet()) {
            try {
                Method method = servletClass.getDeclaredMethod(entry.getKey(), HttpServletRequest.class, HttpServletResponse.class);
                if (method.getDeclaringClass() == servletClass) {
                    methods.add(entry.getValue());
                }
            } catch (NoSuchMethodException e) {
                // method not overridden
            }
        }
        if (methods.isEmpty()) {
            return ALL_HTTP_METHODS;
        }
        return methods;
    }

    @Override
    protected ServletRegistration getRegistration(String name, ServletContext servletContext) {
        return servletContext.getServletRegistration(name);
    }

    @Override
    protected Collection<String> getPatterns(ServletRegistration registration) {
        return registration.getMappings();
    }
}
