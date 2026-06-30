package io.zhijun.spring.context.event;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StopWatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 跟踪 Bean 生命周期各阶段耗时，使用 {@link StopWatch} 测量。
 * 对性能分析和调试很有用。
 * <p>
 * （移植自 microsphere-spring {@code BeanTimeStatistics}）
 *
 * @see StopWatch
 * @see BeanListener
 */
public class BeanTimeStatistics implements BeanListener, BeanNameAware {

    private final StopWatch stopWatch = new StopWatch("spring.context.beans");

    private String beanName;

    @Override
    public boolean supports(String beanName) {
        return !isIgnoredBean(beanName);
    }

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        if (isIgnoredBean(beanName)) {
            return;
        }
        stopWatch.start("ready." + beanName);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        stopWatch.start("instantiation." + beanName);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Constructor<?> constructor, Object[] args) {
        stopWatch.start("instantiation." + beanName);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Object factoryBean, Method factoryMethod, Object[] args) {
        stopWatch.start("instantiation." + beanName);
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        stopWatch.stop();
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        stopWatch.start("initialization." + beanName);
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        stopWatch.stop();
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        if (isIgnoredBean(beanName)) {
            return;
        }
        stopWatch.stop();
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        stopWatch.start("destroy." + beanName);
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        stopWatch.stop();
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BeanTimeStatistics.class.getSimpleName() + "[", "]")
                .add(stopWatch.toString())
                .toString();
    }

    private boolean isIgnoredBean(String beanName) {
        return Objects.equals(this.beanName, beanName);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
