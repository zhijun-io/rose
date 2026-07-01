package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 日志实现的 {@link BeanListener}。
 */
public class LoggingBeanListener implements BeanListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingBeanListener.class);

    @Override
    public boolean supports(String beanName) {
        return true;
    }

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeanDefinitionReady - bean name : {} , definition : {}", beanName, mergedBeanDefinition);
        }
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeforeBeanInstantiate - bean name : {} , definition : {}", beanName, mergedBeanDefinition);
        }
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeforeBeanInstantiate - bean name : {} , definition : {} , constructor : {} , args : {}", beanName, mergedBeanDefinition, constructor, Arrays.toString(args));
        }
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeforeBeanInstantiate - bean name : {} , definition : {} , factoryBean : {} , factoryMethod : {} , args : {}", beanName, mergedBeanDefinition, factoryBean, factoryMethod, Arrays.toString(args));
        }
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        if (logger.isInfoEnabled()) {
            logger.info("onAfterBeanInstantiated - bean name : {} , definition : {} , instance : {}", beanName, mergedBeanDefinition, bean);
        }
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeanPropertyValuesReady - bean name : {} , instance : {} , PropertyValues : {}", beanName, bean, pvs);
        }
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeforeBeanInitialize - bean name : {} , instance : {}", beanName, bean);
        }
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        if (logger.isInfoEnabled()) {
            logger.info("onAfterBeanInitialized - bean name : {} , instance : {}", beanName, bean);
        }
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeanReady - bean name : {} , instance : {}", beanName, bean);
        }
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeforeBeanDestroy - bean name : {} , instance : {}", beanName, bean);
        }
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        if (logger.isInfoEnabled()) {
            logger.info("onAfterBeanDestroy - bean name : {} , instance : {}", beanName, bean);
        }
    }
}
