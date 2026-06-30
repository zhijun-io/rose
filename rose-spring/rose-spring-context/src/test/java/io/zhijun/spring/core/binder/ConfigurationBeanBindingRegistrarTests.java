package io.zhijun.spring.core.binder;

import static io.zhijun.spring.core.binder.EnableConfigurationBeanBinding.DEFAULT_IGNORE_INVALID_FIELDS;
import static io.zhijun.spring.core.binder.EnableConfigurationBeanBinding.DEFAULT_IGNORE_UNKNOWN_FIELDS;
import static io.zhijun.spring.core.binder.EnableConfigurationBeanBinding.DEFAULT_MULTIPLE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.spring.core.binder.ConfigurationBeanBindingRegistrar;

class ConfigurationBeanBindingRegistrarTests {

    private AnnotationConfigApplicationContext context;

    private DefaultListableBeanFactory beanFactory;

    private MockEnvironment environment;

    private ConfigurationBeanBindingRegistrar registrar;

    private AnnotationAttributes attributes;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        environment = new MockEnvironment();
        registrar = new ConfigurationBeanBindingRegistrar();
        registrar.setBeanFactory(beanFactory);
        registrar.setEnvironment(environment);
        attributes = new AnnotationAttributes();
        attributes.put("prefix", "user");
        attributes.put("type", User.class);
        attributes.put("multiple", DEFAULT_MULTIPLE);
        attributes.put("ignoreUnknownFields", DEFAULT_IGNORE_UNKNOWN_FIELDS);
        attributes.put("ignoreInvalidFields", DEFAULT_IGNORE_INVALID_FIELDS);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void shouldRegisterSingleBeanWithGeneratedName() {
        assertSingle(null, "Mercy", 18);
    }

    @Test
    void shouldRegisterSingleBeanWithExplicitId() {
        assertSingle("u0", "Ma", 28);
    }

    @Test
    void shouldRegisterMultipleBeans() {
        attributes.put("prefix", "users");
        attributes.put("multiple", true);
        environment.setProperty("users.u0", "-");
        environment.setProperty("users.u1.name", "Mercy");
        environment.setProperty("users.u1.age", "18");
        environment.setProperty("users.u2.name", "Ma");
        environment.setProperty("users.u2.age", "28");

        registrar.registerConfigurationBeanDefinitions(attributes, beanFactory);
        context.refresh();

        assertUserBean("u1", "Mercy", 18);
        assertUserBean("u2", "Ma", 28);
    }

    private void assertSingle(String beanName, String name, int age) {
        if (beanName != null) {
            environment.setProperty("user.id", beanName);
        }
        environment.setProperty("user.name", name);
        environment.setProperty("user.age", String.valueOf(age));

        registrar.registerConfigurationBeanDefinitions(attributes, beanFactory);
        context.refresh();

        assertUserBean(beanName, name, age);
    }

    private void assertUserBean(String id, String name, int age) {
        Map<String, User> beans = context.getBeansOfType(User.class);
        String beanName = id == null ? User.class.getName() + "#0" : id;
        User user = beans.get(beanName);
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getAge()).isEqualTo(age);
    }
}
