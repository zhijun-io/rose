package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanPropertyChangedEventTests {

    @Test
    void constructorSetsAllFields() {
        Object bean = new Object();
        BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(bean, "name", "old", "new");
        assertSame(bean, event.getBean());
        assertEquals("name", event.getPropertyName());
        assertEquals("old", event.getOldValue());
        assertEquals("new", event.getNewValue());
        assertSame(bean, event.getSource());
    }

    @Test
    void toStringContainsPropertyName() {
        BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(new Object(), "foo", 1, 2);
        String str = event.toString();
        assertTrue(str.contains("foo"));
        assertTrue(str.contains("BeanPropertyChangedEvent"));
    }

    @Test
    void supportsNullPropertyValues() {
        Object bean = new Object();
        BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(bean, null, null, null);
        assertNull(event.getPropertyName());
        assertNull(event.getOldValue());
        assertNull(event.getNewValue());
        assertSame(bean, event.getBean());
    }
}
