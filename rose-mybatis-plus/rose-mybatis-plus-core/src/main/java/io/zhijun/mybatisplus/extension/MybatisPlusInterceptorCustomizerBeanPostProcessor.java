package io.zhijun.mybatisplus.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * {@link BeanPostProcessor} that applies all registered {@link MybatisPlusInterceptorCustomizer}s
 * to the {@link MybatisPlusInterceptor} bean after initialization.
 * <p>
 * Customizers are gathered from two sources:
 * <ul>
 *     <li>Constructor-supplied customizers (e.g. {@code spring.factories}-discovered in pure Spring)</li>
 *     <li>Spring beans of type {@link MybatisPlusInterceptorCustomizer} (resolved at runtime from
 *         the enclosing {@link ListableBeanFactory}, when available)</li>
 * </ul>
 * Both sets are merged and sorted by {@link AnnotationAwareOrderComparator} before application.
 *
 * @see MybatisPlusInterceptorCustomizer
 * @since 0.0.0.2
 */
public class MybatisPlusInterceptorCustomizerBeanPostProcessor
        implements BeanPostProcessor, BeanFactoryAware {

    private final List<MybatisPlusInterceptorCustomizer> factoryCustomizers;

    private ListableBeanFactory beanFactory;

    public MybatisPlusInterceptorCustomizerBeanPostProcessor(
            Collection<MybatisPlusInterceptorCustomizer> factoryCustomizers) {
        List<MybatisPlusInterceptorCustomizer> list =
                new ArrayList<MybatisPlusInterceptorCustomizer>(factoryCustomizers);
        AnnotationAwareOrderComparator.sort(list);
        this.factoryCustomizers = list;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory instanceof ListableBeanFactory
                ? (ListableBeanFactory) beanFactory : null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof MybatisPlusInterceptor)) {
            return bean;
        }
        MybatisPlusInterceptor interceptor = (MybatisPlusInterceptor) bean;
        for (MybatisPlusInterceptorCustomizer customizer : resolveCustomizers()) {
            customizer.customize(interceptor);
        }
        return bean;
    }

    private List<MybatisPlusInterceptorCustomizer> resolveCustomizers() {
        List<MybatisPlusInterceptorCustomizer> all =
                new ArrayList<MybatisPlusInterceptorCustomizer>(factoryCustomizers);
        if (beanFactory != null) {
            // Exclude the BPP itself if it happens to be registered as a customizer bean
            for (MybatisPlusInterceptorCustomizer beanCustomizer :
                    beanFactory.getBeansOfType(MybatisPlusInterceptorCustomizer.class).values()) {
                if (!factoryCustomizers.contains(beanCustomizer)) {
                    all.add(beanCustomizer);
                }
            }
        }
        AnnotationAwareOrderComparator.sort(all);
        return all;
    }
}
