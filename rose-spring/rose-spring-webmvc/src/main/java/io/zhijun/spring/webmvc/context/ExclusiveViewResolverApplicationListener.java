package io.zhijun.spring.webmvc.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * Exclusive {@link ViewResolver} {@link ApplicationListener} on {@link ContextRefreshedEvent}
 *
 * @see ViewResolver
 * @see ApplicationListener
 * @see ContextRefreshedEvent
 * @since 1.0.0
 */
public class ExclusiveViewResolverApplicationListener implements ApplicationListener<ContextRefreshedEvent>, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ExclusiveViewResolverApplicationListener.class);

    static final String EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME = "rose.spring.webmvc.view-resolver.exclusive-bean-name";

    private static final String VIEW_RESOLVER_COMPOSITE_BEAN_NAME = "mvcViewResolver";

    private String exclusiveViewResolverBeanName;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        configureExclusiveViewResolver(applicationContext);
    }

    @Override
    public void setEnvironment(Environment environment) {
        String exclusiveViewResolverBeanName = environment.getProperty(EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME);
        this.exclusiveViewResolverBeanName = exclusiveViewResolverBeanName;
    }

    public void setExclusiveViewResolverBeanName(String exclusiveViewResolverBeanName) {
        this.exclusiveViewResolverBeanName = exclusiveViewResolverBeanName;
    }

    void configureExclusiveViewResolver(ApplicationContext context) {
        String beanName = this.exclusiveViewResolverBeanName;
        if (!hasText(beanName)) {
            if (logger.isTraceEnabled()) {
                logger.trace("The 'exclusiveViewResolverBeanName' is blank, the configuration will be ignored!");
            }
            return;
        }

        ViewResolver exclusiveViewResolver = getBeanIfAvailable(context, beanName);
        if (exclusiveViewResolver == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No ViewResolver was found by the bean name : '{}'", beanName);
            }
            return;
        }

        configureContentNegotiatingViewResolver(exclusiveViewResolver, context);
    }

    private void configureContentNegotiatingViewResolver(ViewResolver exclusiveViewResolver, ApplicationContext context) {

        ContentNegotiatingViewResolver contentNegotiatingViewResolver = getOptionalBean(context, ContentNegotiatingViewResolver.class);

        if (contentNegotiatingViewResolver == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No ContentNegotiatingViewResolver was found in the application context : {}", context);
            }
            configureViewResolverComposite(exclusiveViewResolver, context);
            return;
        }

        List<ViewResolver> viewResolvers = contentNegotiatingViewResolver.getViewResolvers();

        contentNegotiatingViewResolver.setViewResolvers(Collections.singletonList(exclusiveViewResolver));

        if (logger.isTraceEnabled()) {
            logger.trace("The view resolvers of ContentNegotiatingViewResolver has been reset , before : {} , after : {}",
                    viewResolvers, exclusiveViewResolver);
        }
    }

    private void configureViewResolverComposite(ViewResolver exclusiveViewResolver, ApplicationContext context) {
        ViewResolverComposite viewResolverComposite = getBeanIfAvailable(context, VIEW_RESOLVER_COMPOSITE_BEAN_NAME, ViewResolverComposite.class);

        if (viewResolverComposite == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No ViewResolverComposite was found in the application context : {}", context);
            }
            return;
        }

        List<ViewResolver> viewResolvers = viewResolverComposite.getViewResolvers();

        viewResolverComposite.setViewResolvers(Collections.singletonList(exclusiveViewResolver));

        if (logger.isTraceEnabled()) {
            logger.trace("The view resolvers of ViewResolverComposite has been reset , before : {} , after : {}",
                    viewResolvers, exclusiveViewResolver);
        }
    }

    private static ViewResolver getBeanIfAvailable(ApplicationContext context, String beanName) {
        if (context.containsBean(beanName)) {
            return context.getBean(beanName, ViewResolver.class);
        }
        return null;
    }

    private static <T> T getBeanIfAvailable(ApplicationContext context, String beanName, Class<T> type) {
        if (context.containsBean(beanName)) {
            return context.getBean(beanName, type);
        }
        return null;
    }

    private static <T> T getOptionalBean(ApplicationContext context, Class<T> type) {
        Map<String, T> beans = context.getBeansOfType(type);
        return beans.isEmpty() ? null : beans.values().iterator().next();
    }
}
