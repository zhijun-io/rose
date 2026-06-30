package io.zhijun.spring.webmvc;

import io.zhijun.spring.web.MethodHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.ResolvableType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import java.lang.annotation.Annotation;

import static org.springframework.core.ResolvableType.forType;

/**
 * 注解驱动的 {@link org.springframework.web.servlet.HandlerInterceptor HandlerInterceptor} 抽象基类。
 * <p>通过泛型参数指定注解类型，自动解析并提取处理方法上的注解实例。</p>
 *
 * @param <A> 注解类型
 */
public abstract class AnnotatedMethodHandlerInterceptor<A extends Annotation> extends MethodHandlerInterceptor {

    private final Class<A> annotationType;

    public AnnotatedMethodHandlerInterceptor() {
        this.annotationType = resolveAnnotationType();
    }

    @SuppressWarnings("unchecked")
    private Class<A> resolveAnnotationType() {
        ResolvableType resolvableType = forType(getClass());
        return (Class<A>) resolvableType.as(AnnotatedMethodHandlerInterceptor.class).getGeneric(0).resolve();
    }

    @Override
    protected final boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod)
            throws Exception {
        A annotation = getMethodAnnotation(handlerMethod);
        if (annotation != null) {
            return preHandle(request, response, handlerMethod, annotation);
        }
        return true;
    }

    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                HandlerMethod handlerMethod, A annotation) throws Exception {
        return true;
    }

    @Override
    protected final void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                    ModelAndView modelAndView) throws Exception {
        A annotation = getMethodAnnotation(handlerMethod);
        if (annotation != null) {
            postHandle(request, response, handlerMethod, modelAndView, annotation);
        }
    }

    protected void postHandle(HttpServletRequest request, HttpServletResponse response,
                              HandlerMethod handlerMethod, ModelAndView modelAndView, A annotation) throws Exception {
    }

    @Override
    protected final void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                         Exception ex) throws Exception {
        A annotation = getMethodAnnotation(handlerMethod);
        if (annotation != null) {
            afterCompletion(request, response, handlerMethod, ex, annotation);
        }
    }

    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   HandlerMethod handlerMethod, Exception ex, A annotation) throws Exception {
    }

    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * 获取处理方法上的注解。
     */
    protected A getMethodAnnotation(HandlerMethod handlerMethod) {
        return handlerMethod.getMethodAnnotation(annotationType);
    }
}
