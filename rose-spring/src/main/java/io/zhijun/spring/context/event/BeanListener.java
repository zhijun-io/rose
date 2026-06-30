package io.zhijun.spring.context.event;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EventListener;

/**
 * 监听 Bean 生命周期全部阶段的事件：实例化 → 初始化 → 销毁。
 * <p>
 * 三个阶段的回调方法统一放在此接口中，实现者通过覆写相关 default 方法按需关注特定阶段。
 * <p>
 * (借鉴 microsphere-spring {@code BeanListener})
 *
 * @see BeanListeners
 */
public interface BeanListener extends EventListener {

    // ---- Instantiation phase ----

    /**
     * 合并后的 BeanDefinition 准备就绪时回调。
     *
     * @param beanName             bean 名称
     * @param mergedBeanDefinition 合并后的 BeanDefinition
     */
    default void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    /**
     * 在 Bean 通过无参构造器实例化前回调。
     *
     * @param beanName             bean 名称
     * @param mergedBeanDefinition 合并后的 BeanDefinition
     */
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    /**
     * 在 Bean 通过指定构造器实例化前回调。
     *
     * @param beanName             bean 名称
     * @param mergedBeanDefinition 合并后的 BeanDefinition
     * @param constructor          要使用的构造器
     * @param args                 构造器参数
     */
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Constructor<?> constructor, Object[] args) {
    }

    /**
     * 在 Bean 通过工厂方法实例化前回调。
     *
     * @param beanName             bean 名称
     * @param mergedBeanDefinition 合并后的 BeanDefinition
     * @param factoryBean          工厂 Bean（静态方法时为 null）
     * @param factoryMethod        工厂方法
     * @param args                 工厂方法参数
     */
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Object factoryBean, Method factoryMethod, Object[] args) {
    }

    /**
     * Bean 实例化完成后回调。
     *
     * @param beanName             bean 名称
     * @param mergedBeanDefinition 合并后的 BeanDefinition
     * @param bean                 实例化后的 Bean（可能为 null）
     */
    default void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
    }

    // ---- Initialization phase ----

    /**
     * Bean 的属性值准备就绪时回调。
     *
     * @param beanName bean 名称
     * @param bean     Bean 实例
     * @param pvs      属性值
     */
    default void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
    }

    /**
     * 在 Bean 的 init 方法调用前回调。
     *
     * @param beanName bean 名称
     * @param bean     Bean 实例
     */
    default void onBeforeBeanInitialize(String beanName, Object bean) {
    }

    /**
     * Bean 的 init 方法完成后回调。
     *
     * @param beanName bean 名称
     * @param bean     Bean 实例
     */
    default void onAfterBeanInitialized(String beanName, Object bean) {
    }

    /**
     * Bean 完全就绪（初始化完成且所有后处理器已应用）后回调。
     *
     * @param beanName bean 名称
     * @param bean     Bean 实例
     */
    default void onBeanReady(String beanName, Object bean) {
    }

    // ---- Destruction phase ----

    /**
     * 在 Bean 销毁前回调。
     *
     * @param beanName bean 名称
     * @param bean     Bean 实例
     */
    default void onBeforeBeanDestroy(String beanName, Object bean) {
    }

    /**
     * 在 Bean 销毁后回调。
     *
     * @param beanName bean 名称
     * @param bean     Bean 实例
     */
    default void onAfterBeanDestroy(String beanName, Object bean) {
    }

    /**
     * 检查此监听器是否关注指定名称的 Bean。
     *
     * @param beanName the bean name
     * @return true if the bean should be processed
     */
    boolean supports(String beanName);
}
