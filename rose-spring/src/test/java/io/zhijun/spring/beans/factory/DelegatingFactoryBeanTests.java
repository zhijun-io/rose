package io.zhijun.spring.beans.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DelegatingFactoryBeanTests {

    @Test
    void shouldReturnDelegateObject() throws Exception {
        Object delegate = new Object();
        DelegatingFactoryBean fb = new DelegatingFactoryBean(delegate);
        assertThat(fb.getObject()).isSameAs(delegate);
    }

    @Test
    void shouldResolveObjectType() {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test");
        assertThat(fb.getObjectType()).isEqualTo(String.class);
    }

    @Test
    void shouldBeSingletonByDefault() {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test");
        assertThat(fb.isSingleton()).isTrue();
    }

    @Test
    void shouldSupportNonSingleton() {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test", false);
        assertThat(fb.isSingleton()).isFalse();
    }

    @Test
    void shouldCallAfterPropertiesSetOnInitializingBeanDelegate() throws Exception {
        InitializingBean delegate = mock(InitializingBean.class);
        DelegatingFactoryBean fb = new DelegatingFactoryBean(delegate);
        fb.afterPropertiesSet();
        verify(delegate).afterPropertiesSet();
    }

    @Test
    void shouldNotFailWhenDelegateIsNotInitializingBean() throws Exception {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test");
        fb.afterPropertiesSet();
    }

    @Test
    void shouldPropagateApplicationContextToAwareDelegate() {
        ApplicationContextAware delegate = mock(ApplicationContextAware.class);
        DelegatingFactoryBean fb = new DelegatingFactoryBean(delegate);
        ApplicationContext ctx = mock(ApplicationContext.class);
        fb.setApplicationContext(ctx);
        verify(delegate).setApplicationContext(ctx);
    }

    @Test
    void shouldPropagateEnvironmentToEnvironmentAwareDelegate() {
        EnvironmentAware delegate = mock(EnvironmentAware.class);
        DelegatingFactoryBean fb = new DelegatingFactoryBean(delegate);
        ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
        org.springframework.core.env.ConfigurableEnvironment env = mock(org.springframework.core.env.ConfigurableEnvironment.class);
        when(ctx.getEnvironment()).thenReturn(env);
        fb.setApplicationContext(ctx);
        verify(delegate).setEnvironment(env);
    }

    @Test
    void shouldPropagateBeanNameToBeanNameAwareDelegate() {
        BeanNameAware delegate = mock(BeanNameAware.class);
        DelegatingFactoryBean fb = new DelegatingFactoryBean(delegate);
        fb.setBeanName("myBean");
        verify(delegate).setBeanName("myBean");
    }

    @Test
    void shouldCallDestroyOnDisposableBeanDelegate() throws Exception {
        DisposableBean delegate = mock(DisposableBean.class);
        DelegatingFactoryBean fb = new DelegatingFactoryBean(delegate);
        fb.destroy();
        verify(delegate).destroy();
    }

    @Test
    void shouldNotFailDestroyWhenDelegateIsNotDisposable() throws Exception {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test");
        fb.destroy();
    }

    @Test
    void shouldNotPropagateApplicationContextToNonAwareDelegate() {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test");
        ApplicationContext ctx = mock(ApplicationContext.class);
        fb.setApplicationContext(ctx);
    }

    @Test
    void shouldNotPropagateBeanNameToNonAwareDelegate() {
        DelegatingFactoryBean fb = new DelegatingFactoryBean("test");
        fb.setBeanName("myBean");
    }
}
