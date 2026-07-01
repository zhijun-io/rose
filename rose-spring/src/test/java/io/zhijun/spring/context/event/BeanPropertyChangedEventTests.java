package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanPropertyChangedEventTests {

    @Test
    void constructorSetsAllFields() {
        Object bean = new Object();
        BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(bean, "name", "old", "new");
        assertThat(event.getBean()).isSameAs(bean);
        assertThat(event.getPropertyName()).isEqualTo("name");
        assertThat(event.getOldValue()).isEqualTo("old");
        assertThat(event.getNewValue()).isEqualTo("new");
        assertThat(event.getSource()).isSameAs(bean);
    }

    @Test
    void toStringContainsPropertyName() {
        BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(new Object(), "foo", 1, 2);
        String str = event.toString();
        assertThat(str).contains("foo");
        assertThat(str).contains("BeanPropertyChangedEvent");
    }

    @Test
    void supportsNullPropertyValues() {
        Object bean = new Object();
        BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(bean, null, null, null);
        assertThat(event.getPropertyName()).isNull();
        assertThat(event.getOldValue()).isNull();
        assertThat(event.getNewValue()).isNull();
        assertThat(event.getBean()).isSameAs(bean);
    }
}
