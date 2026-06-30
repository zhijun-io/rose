package io.zhijun.spring.context;

import io.zhijun.spring.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

/**
 * 泛型注解驱动的 {@link BeanCapableImportCandidate} 基类。
 * <p>
 * 子类通过泛型参数 {@code A} 指定关联的注解类型，自动解析并获取注解属性。
 *
 * @param <A> 驱动此导入候选的注解类型
 * @see BeanCapableImportCandidate
 * @see AnnotatedBeanCapableImportSelector
 * @see AnnotatedBeanCapableImportBeanDefinitionRegistrar
 */
public abstract class AnnotatedBeanCapableImportCandidate<A extends Annotation> extends BeanCapableImportCandidate {

    private static final String ROSE_SPRING_PROPERTY_PREFIX = "rose.spring.";

    protected final Class<A> annotationType;

    @SuppressWarnings("unchecked")
    public AnnotatedBeanCapableImportCandidate() {
        this.annotationType = resolveAnnotationType();
    }

    // ---- 泛型解析 ----

    @SuppressWarnings("unchecked")
    protected Class<A> resolveAnnotationType() {
        ResolvableType type = ResolvableType.forType(getClass());
        ResolvableType superType = type.as(AnnotatedBeanCapableImportCandidate.class);
        return (Class<A>) superType.resolveGeneric(0);
    }

    // ---- 启用/禁用 ----

    /**
     * 检查当前导入候选是否启用。
     * <p>
     * 通过环境属性控制，优先级：
     * <ol>
     *     <li>{@code rose.spring.<导入类名>@<注解类名>.enabled}</li>
     *     <li>{@code rose.spring.<注解类名>.enabled}</li>
     *     <li>默认 {@code true}</li>
     * </ol>
     */
    protected boolean isEnabled(AnnotationMetadata metadata) {
        return isEnabled(getEnvironment(), metadata.getClassName(), getAnnotationType());
    }

    /**
     * 获取已解析占位符的注解属性。
     */
    protected ResolvablePlaceholderAnnotationAttributes<A> getAnnotationAttributes(AnnotationMetadata metadata) {
        return super.getAnnotationAttributes(metadata, getAnnotationType());
    }

    /**
     * 获取注解类型。
     */
    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    // ---- 静态工具 ----

    static boolean isEnabled(Environment environment, String importingClassName,
                             Class<? extends Annotation> annotationType) {
        String propertyName = getEnabledPropertyName(importingClassName, annotationType);
        String propertyValue = environment.getProperty(propertyName);
        if (propertyValue == null) {
            propertyName = getGlobalEnabledPropertyName(annotationType);
        }
        return environment.getProperty(propertyName, boolean.class, true);
    }

    static String getEnabledPropertyName(String importingClassName, Class<? extends Annotation> annotationType) {
        return ROSE_SPRING_PROPERTY_PREFIX + importingClassName + "@" + annotationType.getName() + ".enabled";
    }

    static String getGlobalEnabledPropertyName(Class<? extends Annotation> annotationType) {
        return ROSE_SPRING_PROPERTY_PREFIX + annotationType.getName() + ".enabled";
    }
}
