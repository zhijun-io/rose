package io.zhijun.spring.webmvc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleViewResolverConfigurationListenerTests {

    @Test
    void configureWithBlankBeanNameDoesNothing() {
        SingleViewResolverConfigurationListener listener = new SingleViewResolverConfigurationListener();
        assertDoesNotThrow(() -> listener.configureExclusiveViewResolver(null));
    }

    @Test
    void propertyNameConstant() {
        assertEquals("rose.spring.webmvc.view-resolver.exclusive-bean-name",
                SingleViewResolverConfigurationListener.EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME);
    }
}
