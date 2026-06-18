package io.zhijun.spring.beans.factory.annotation;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

abstract class AbstractEnableConfigurationBeanBindingTest {

    protected AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(getClass());
        context.setEnvironment(new AbstractEnvironment() {
            @Override
            protected void customizePropertySources(MutablePropertySources propertySources) {
                try {
                    propertySources.addFirst(new ResourcePropertySource("temp",
                            new DefaultResourceLoader().getResource("classpath:/enable-configuration-bean-binding.properties")));
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        });
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }
}
