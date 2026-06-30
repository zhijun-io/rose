package io.zhijun.spring.core.test.context.annotation;

import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;

import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * 在测试中创建 {@link AnnotatedTypeMetadata} 实例的工具。
 *
 * <p>适用于测试基于 {@code @Conditional} 的条件注解逻辑。</p>
 */
public class AnnotatedTypeMetadataTestFactory {

    private final ClassLoader classLoader;

    public AnnotatedTypeMetadataTestFactory() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public AnnotatedTypeMetadataTestFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 为当前测试方法创建 {@link StandardMethodMetadata}。
     */
    public AnnotatedTypeMetadata createMethodAnnotatedTypeMetadata() {
        Method method = findTestMethod();
        return new StandardMethodMetadata(method);
    }

    private Method findTestMethod() {
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        StackTraceElement stackTrace = stackTraces[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        Class<?> targetClass = resolveClassName(className, classLoader);
        return findMethod(targetClass, methodName);
    }
}
