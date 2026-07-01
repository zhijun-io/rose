package io.zhijun.spring.beans.factory.annotation;

import io.zhijun.spring.beans.factory.InjectionPointDependencyResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;


/**
 * Abstract {@link InjectionPointDependencyResolver} for annotated element
 *
 * @since 1.0.0
 */
public abstract class AnnotatedInjectionPointDependencyResolver<A extends Annotation> extends AbstractInjectionPointDependencyResolver {

    private final Class<A> annotationType;

    public AnnotatedInjectionPointDependencyResolver() {
        this.annotationType = (Class<A>) org.springframework.core.ResolvableType.forClass(AnnotatedInjectionPointDependencyResolver.class, getClass()).resolveGeneric(0);
    }

    public AnnotatedInjectionPointDependencyResolver(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param field {@link Field}
     * @return the injection annotation if found
     */
    protected A getAnnotation(Field field) {
        return getAnnotation((AnnotatedElement) field);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param parameter {@link Parameter}
     * @return the injection annotation if found
     */
    protected A getAnnotation(Parameter parameter) {
        return getAnnotation((AnnotatedElement) parameter);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param annotated {@link Field} or {@link Parameter}
     * @return the injection annotation if found
     */
    protected A getAnnotation(AnnotatedElement annotated) {
        return annotated.getAnnotation(getAnnotationType());
    }

}
