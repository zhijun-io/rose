package io.zhijun.spring.core.context;

import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import org.springframework.beans.factory.config.BeanDefinition;
import java.beans.Introspector;

/**
 * SPI 自动注册 Bean 的标记接口。
 * <p>
 * 实现类通过 {@code META-INF/services/} 声明，由
 * {@link EnableAutoRegistration @EnableAutoRegistration} 自动注册为 Spring Bean。
 * <p>
 * <b>职责边界</b>：此接口只负责 Bean 注册的元数据（名称、启用状态、Bean 定义定制），
 * 不管理 SPI 实现的发现和排序——那是 {@code SpiServiceLoader} 的事。
 */
public interface AutoRegistrationBean extends Ordered {

    default String getBeanName() {
        return Introspector.decapitalize(getClass().getSimpleName());
    }

    default String getScope() {
        return BeanDefinition.SCOPE_SINGLETON;
    }

    default boolean isAutoRegistered(ConfigurableEnvironment environment) {
        return true;
    }

    @Override
    default int getOrder() { return 0; }
}
