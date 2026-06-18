package io.zhijun.spring.core.env.refresh;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshableContextHolderTest {

    @AfterEach
    void tearDown() {
        RefreshableContextHolder.clear();
    }

    @Test
    void bindThenGetReturnsContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        RefreshableContextHolder.bind(context);

        assertThat(RefreshableContextHolder.getApplicationContext()).isSameAs(context);
    }

    @Test
    void getWithoutBindThrows() {
        assertThatThrownBy(RefreshableContextHolder::getApplicationContext)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApplicationContext not bound");
    }
}
