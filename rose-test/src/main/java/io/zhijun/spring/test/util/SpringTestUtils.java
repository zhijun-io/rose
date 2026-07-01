package io.zhijun.spring.test.util;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Test Utilities — executes code within an AnnotationConfigApplicationContext.
 */
public abstract class SpringTestUtils {

    public static void testInSpringContainer(ThrowableConsumer<ConfigurableApplicationContext> consumer, Class<?>... configClasses) {
        testInSpringContainer((context, environment) -> {
            consumer.accept(context);
        }, configClasses);
    }

    public static void testInSpringContainer(ThrowableBiConsumer<ConfigurableApplicationContext, ConfigurableEnvironment> consumer, Class<?>... configClasses) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        for (Class<?> configClass : configClasses) {
            context.register(configClass);
        }
        context.refresh();
        try {
            consumer.accept(context, context.getEnvironment());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        context.close();
    }

    private SpringTestUtils() {
    }
}
