package io.zhijun.spring.core.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

class SpringContextHolderTests {

    @AfterEach
    void tearDown() {
        SpringContextHolder.clear();
    }

    @Test
    void notBoundByDefault() {
        assertThat(SpringContextHolder.peekRefreshableContext()).isNull();
    }

    @Test
    void getRefreshableThrowsWhenNotBound() {
        assertThatThrownBy(SpringContextHolder::getRefreshableContext)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bindAndGetRefreshableContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        SpringContextHolder.bind(context);
        assertThat(SpringContextHolder.getRefreshableContext()).isSameAs(context);
    }

    @Test
    void ignoresChildContextOnBind() {
        StaticApplicationContext parent = new StaticApplicationContext();
        parent.refresh();
        StaticApplicationContext child = new StaticApplicationContext();
        child.setParent(parent);

        SpringContextHolder.bind(parent);
        assertThat(SpringContextHolder.getRefreshableContext()).isSameAs(parent);

        SpringContextHolder.bind(child);
        assertThat(SpringContextHolder.getRefreshableContext()).isSameAs(parent);
    }

    @Test
    void clearResetsContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        SpringContextHolder.bind(context);
        SpringContextHolder.clear();
        assertThat(SpringContextHolder.peekRefreshableContext()).isNull();
    }

    @Test
    void isNotSpringEnvironmentByDefault() {
        assertThat(SpringContextHolder.isSpringEnvironment()).isFalse();
    }

    @Test
    void getBeanReturnsNullWhenNoContext() {
        assertThat(SpringContextHolder.getBean(String.class)).isNull();
        assertThat(SpringContextHolder.getBean("foo", String.class)).isNull();
    }
}
