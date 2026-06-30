package io.zhijun.spring.core.context;

import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

/**
 * 泛型注解驱动的 {@link ImportBeanDefinitionRegistrar} 模板基类。
 * <p>
 * 子类实现
 * {@link #registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry, BeanNameGenerator, ResolvablePlaceholderAnnotationAttributes)}
 * 即可在启用状态下注册 Bean 定义。
 *
 * @param <A> 驱动此注册器的注解类型
 * @see AnnotatedBeanCapableImportCandidate
 * @see ImportBeanDefinitionRegistrar
 */
public abstract class AnnotatedBeanCapableImportBeanDefinitionRegistrar<A extends Annotation>
        extends AnnotatedBeanCapableImportCandidate<A> implements ImportBeanDefinitionRegistrar {

    private static final BeanNameGenerator DEFAULT_GENERATOR = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE;

    @Override
    public final void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                              BeanNameGenerator importBeanNameGenerator) {
        if (isEnabled(metadata)) {
            ResolvablePlaceholderAnnotationAttributes<A> attrs = getAnnotationAttributes(metadata);
            registerBeanDefinitions(metadata, registry, importBeanNameGenerator, attrs);
        }
    }

    @Override
    public final void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitions(metadata, registry, DEFAULT_GENERATOR);
    }

    /**
     * 注册 Bean 定义。
     * <p>
     * 仅当 {@link #isEnabled(AnnotationMetadata)} 返回 {@code true} 时调用。
     *
     * @param metadata               被标注类的元数据
     * @param registry               Bean 定义注册中心
     * @param importBeanNameGenerator Bean 名称生成器
     * @param annotationAttributes   已解析占位符的注解属性
     */
    protected abstract void registerBeanDefinitions(AnnotationMetadata metadata,
                                                    BeanDefinitionRegistry registry,
                                                    BeanNameGenerator importBeanNameGenerator,
                                                    ResolvablePlaceholderAnnotationAttributes<A> annotationAttributes);
}
