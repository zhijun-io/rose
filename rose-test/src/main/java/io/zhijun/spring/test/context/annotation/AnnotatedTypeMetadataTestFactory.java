package io.zhijun.spring.test.context.annotation;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;

import static java.lang.Thread.currentThread;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * Factory for creating {@link AnnotatedTypeMetadata} instances in tests.
 */
public class AnnotatedTypeMetadataTestFactory implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    public AnnotatedTypeMetadata createMethodAnnotatedTypeMetadata() {
        Method method = findTestMethod();
        return new StandardMethodMetadata(method);
    }

    private Method findTestMethod() {
        StackTraceElement[] stackTraces = currentThread().getStackTrace();
        StackTraceElement stackTrace = stackTraces[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        Class<?> targetClass;
        try {
            targetClass = forName(className, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load test class: " + className, e);
        }
        return findMethod(targetClass, methodName);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
