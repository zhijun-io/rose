package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Map;
import java.util.Set;

/**
 * Bean 依赖解析策略接口。
 * 用于分析 Spring Bean 之间的依赖关系。
 *
 * @see DefaultBeanDependencyResolver
 * @see RootBeanDefinition
 * @see ConfigurableListableBeanFactory
 */
public interface BeanDependencyResolver {

    Map<String, Set<String>> resolve(ConfigurableListableBeanFactory beanFactory);

    Set<String> resolve(String beanName, RootBeanDefinition mergedBeanDefinition, ConfigurableListableBeanFactory beanFactory);
}
