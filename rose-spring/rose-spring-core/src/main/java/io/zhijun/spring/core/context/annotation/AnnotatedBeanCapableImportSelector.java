package io.zhijun.spring.core.context.annotation;

import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 泛型注解驱动的 {@link ImportSelector} 模板基类。
 * <p>
 * 子类实现 {@link #selectImports(AnnotationMetadata, ResolvablePlaceholderAnnotationAttributes, Set)}
 * 即可按需选择导入的类。
 *
 * @param <A> 驱动此导入选择的注解类型
 * @see AnnotatedBeanCapableImportCandidate
 * @see ImportSelector
 */
public abstract class AnnotatedBeanCapableImportSelector<A extends Annotation>
        extends AnnotatedBeanCapableImportCandidate<A> implements ImportSelector {

    @Override
    public final String[] selectImports(AnnotationMetadata metadata) {
        Set<String> imports = new LinkedHashSet<String>();
        selectImports(metadata, getAnnotationAttributes(metadata), imports);
        return imports.toArray(new String[0]);
    }

    /**
     * 选择需要导入的类。
     *
     * @param metadata             被标注类的元数据
     * @param annotationAttributes 已解析占位符的注解属性
     * @param imports              导入类名集合，子类向此集合添加
     */
    protected abstract void selectImports(AnnotationMetadata metadata,
                                          ResolvablePlaceholderAnnotationAttributes<A> annotationAttributes,
                                          Set<String> imports);
}
