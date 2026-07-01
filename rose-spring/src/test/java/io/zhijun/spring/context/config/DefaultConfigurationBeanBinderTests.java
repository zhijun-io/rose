package io.zhijun.spring.context.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultConfigurationBeanBinderTests {

    private final DefaultConfigurationBeanBinder binder = new DefaultConfigurationBeanBinder();

    static class TestBean {
        private String name;
        private int age;

        public void setName(String name) { this.name = name; }
        public String getName() { return name; }
        public void setAge(int age) { this.age = age; }
        public int getAge() { return age; }
    }

    @Test
    void bindSetsProperties() {
        TestBean bean = new TestBean();
        Map<String, Object> props = new HashMap<>();
        props.put("name", "test");
        props.put("age", 30);

        binder.bind(props, true, true, bean);

        assertThat(bean.getName()).isEqualTo("test");
        assertThat(bean.getAge()).isEqualTo(30);
    }

    @Test
    void bindHandlesEmptyProperties() {
        TestBean bean = new TestBean();
        binder.bind(new HashMap<>(), true, true, bean);
        assertThat(bean.getName()).isNull();
        assertThat(bean.getAge()).isEqualTo(0);
    }
}
