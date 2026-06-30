package io.zhijun.spring.core.context.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

class AutoRegistrationBeanTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void registersSpiDiscoveredBeans() {
        context = new AnnotationConfigApplicationContext();
        context.register(AutoRegistrationConfig.class);
        context.refresh();

        TestAutoRegistrationBean bean = context.getBean(TestAutoRegistrationBean.class);
        assertThat(bean).isNotNull();
    }

    @Test
    void configClassHasNoOwnErrors() {
        context = new AnnotationConfigApplicationContext();
        context.register(AutoRegistrationConfig.class);
        context.refresh();

        assertThat(context.getBean(AutoRegistrationConfig.class)).isNotNull();
    }

    @Configuration
    @EnableAutoRegistration
    static class AutoRegistrationConfig {
    }
}
