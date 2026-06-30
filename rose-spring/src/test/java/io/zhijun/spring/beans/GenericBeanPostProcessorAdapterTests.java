package io.zhijun.spring.beans;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;

import static org.assertj.core.api.Assertions.assertThat;

class GenericBeanPostProcessorAdapterTests {

    @Test
    void shouldProcessMatchingBeanTypeBeforeInitialization() {
        TestProcessor processor = new TestProcessor();
        String bean = "hello";
        Object result = processor.postProcessBeforeInitialization(bean, "testBean");
        assertThat(result).isSameAs(bean);
        assertThat(processor.beforeProcessed).isTrue();
    }

    @Test
    void shouldProcessMatchingBeanTypeAfterInitialization() {
        TestProcessor processor = new TestProcessor();
        String bean = "world";
        Object result = processor.postProcessAfterInitialization(bean, "testBean");
        assertThat(result).isSameAs(bean);
        assertThat(processor.afterProcessed).isTrue();
    }

    @Test
    void shouldSkipNonMatchingBeanType() {
        TestProcessor processor = new TestProcessor();
        Integer bean = 42;
        Object resultBefore = processor.postProcessBeforeInitialization(bean, "intBean");
        Object resultAfter = processor.postProcessAfterInitialization(bean, "intBean");
        assertThat(resultBefore).isSameAs(bean);
        assertThat(resultAfter).isSameAs(bean);
        assertThat(processor.beforeProcessed).isFalse();
        assertThat(processor.afterProcessed).isFalse();
    }

    @Test
    void shouldResolveCorrectBeanType() {
        TestProcessor processor = new TestProcessor();
        assertThat(processor.getBeanType()).isEqualTo(String.class);
    }

    @Test
    void shouldAllowCustomPostProcessOverride() {
        CustomProcessor processor = new CustomProcessor();
        String bean = "test";
        Object result = processor.postProcessBeforeInitialization(bean, "testBean");
        assertThat(result).isEqualTo("MODIFIED:test");
    }

    static class TestProcessor extends GenericBeanPostProcessorAdapter<String> {
        boolean beforeProcessed;
        boolean afterProcessed;

        @Override
        protected void processBeforeInitialization(String bean, String beanName) throws BeansException {
            beforeProcessed = true;
        }

        @Override
        protected void processAfterInitialization(String bean, String beanName) throws BeansException {
            afterProcessed = true;
        }
    }

    static class CustomProcessor extends GenericBeanPostProcessorAdapter<String> {
        @Override
        protected String doPostProcessBeforeInitialization(String bean, String beanName) throws BeansException {
            return "MODIFIED:" + bean;
        }
    }
}
