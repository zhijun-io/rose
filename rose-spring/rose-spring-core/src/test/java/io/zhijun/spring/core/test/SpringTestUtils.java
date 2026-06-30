package io.zhijun.spring.core.test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 轻量级 Spring 测试工具，快速创建和销毁 {@link AnnotationConfigApplicationContext}。
 *
 * <p>适用于不需要 {@code @SpringBootTest} 的非 Boot 模块快速集成测试。</p>
 */
public abstract class SpringTestUtils {

    /**
     * 创建 Spring 上下文并执行断言，执行后自动关闭上下文。
     *
     * @param consumer      断言回调
     * @param configClasses 配置类
     */
    public static void testInSpringContainer(Consumer<ConfigurableApplicationContext> consumer, Class<?>... configClasses) {
        testInSpringContainer((context, environment) -> consumer.accept(context), configClasses);
    }

    /**
     * 创建 Spring 上下文并执行断言（带 Environment），执行后自动关闭上下文。
     *
     * @param consumer      断言回调
     * @param configClasses 配置类
     */
    public static void testInSpringContainer(BiConsumer<ConfigurableApplicationContext, ConfigurableEnvironment> consumer,
                                             Class<?>... configClasses) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        for (Class<?> configClass : configClasses) {
            context.register(configClass);
        }
        context.refresh();
        try {
            consumer.accept(context, context.getEnvironment());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            context.close();
        }
    }

    private SpringTestUtils() {
    }
}
