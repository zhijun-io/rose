package io.zhijun.spring.boot.properties.bind.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import static org.assertj.core.api.Assertions.assertThat;

class BindHandlerUtilsTests {

    @Test
    void shouldReturnDefaultWhenNeitherFlag() {
        BindHandler handler = BindHandlerUtils.createBindHandler(true, false);
        assertThat(handler).isSameAs(BindHandler.DEFAULT);
    }

    @Test
    void shouldWrapWithIgnoreErrorsWhenInvalidFieldsIgnored() {
        BindHandler handler = BindHandlerUtils.createBindHandler(true, true);
        assertThat(handler).isInstanceOf(IgnoreErrorsBindHandler.class);
    }

    @Test
    void shouldWrapWithNoUnboundElementsWhenUnknownFieldsNotIgnored() {
        BindHandler handler = BindHandlerUtils.createBindHandler(false, false);
        assertThat(handler).isInstanceOf(NoUnboundElementsBindHandler.class);
    }

    @Test
    void shouldChainBothHandlersWhenBothFlagsSet() {
        BindHandler handler = BindHandlerUtils.createBindHandler(false, true);
        assertThat(handler).isInstanceOf(NoUnboundElementsBindHandler.class);
    }
}
