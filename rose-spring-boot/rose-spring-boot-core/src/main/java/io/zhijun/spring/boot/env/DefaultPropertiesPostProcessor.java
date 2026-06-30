package io.zhijun.spring.boot.env;

import org.springframework.core.Ordered;

import java.util.Map;
import java.util.Set;

/**
 * 默认属性后置处理器 SPI：在 SpringApplication defaultProperties 阶段加载和修改默认属性。
 *
 * <h3>用法</h3>
 * <pre>{@code
 * // 实现接口
 * public class MyProcessor implements DefaultPropertiesPostProcessor {
 *     public void initializeResources(Set<String> resources) {
 *         resources.add("classpath*:META-INF/my-default.properties");
 *     }
 *     public void postProcess(Map<String, Object> defaults) {
 *         defaults.put("my.key", "value");
 *     }
 * }
 *
 * // 在 META-INF/spring.factories 注册
 * // io.zhijun.spring.boot.env.DefaultPropertiesPostProcessor=com.example.MyProcessor
 * }</pre>
 *
 * @see DefaultPropertiesApplicationListener
 */
public interface DefaultPropertiesPostProcessor extends Ordered {

    /**
     * 初始化默认属性资源路径列表。
     *
     * @param defaultPropertiesResources 待加入的资源路径集合
     */
    void initializeResources(Set<String> defaultPropertiesResources);

    /**
     * 后处理默认属性映射，可在加载资源后修改。
     *
     * @param defaultProperties SpringApplication defaultProperties
     */
    default void postProcess(Map<String, Object> defaultProperties) {
    }

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
