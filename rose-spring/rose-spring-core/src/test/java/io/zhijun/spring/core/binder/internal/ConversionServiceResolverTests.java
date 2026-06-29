package io.zhijun.spring.core.binder.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.format.support.DefaultFormattingConversionService;

@ExtendWith(MockitoExtension.class)
class ConversionServiceResolverTests {

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Test
    void resolveReturnsBeanFactoryConversionServiceWhenPresent() {
        ConversionService conversionService = mock(ConversionService.class);
        when(beanFactory.getConversionService()).thenReturn(conversionService);

        assertThat(new ConversionServiceResolver(beanFactory).resolve()).isSameAs(conversionService);
    }

    @Test
    void resolveReturnsConversionServiceBeanWhenBeanFactoryHasNone() {
        ConversionService conversionService = mock(ConversionService.class);
        when(beanFactory.getConversionService()).thenReturn(null);
        when(beanFactory.containsBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME))
                .thenReturn(true);
        when(beanFactory.getBean(ConversionService.class)).thenReturn(conversionService);

        assertThat(new ConversionServiceResolver(beanFactory).resolve()).isSameAs(conversionService);
    }

    @Test
    void resolveReturnsEnvironmentConversionServiceWhenAvailable() {
        ConfigurableConversionService conversionService = new DefaultFormattingConversionService();
        ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
        when(beanFactory.getConversionService()).thenReturn(null);
        when(beanFactory.containsBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME))
                .thenReturn(false);
        when(beanFactory.containsBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME))
                .thenReturn(true);
        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(environment);
        when(environment.getConversionService()).thenReturn(conversionService);

        assertThat(new ConversionServiceResolver(beanFactory).resolve()).isSameAs(conversionService);
    }

    @Test
    void resolveFallsBackToDefaultFormattingConversionService() {
        ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
        when(beanFactory.getConversionService()).thenReturn(null);
        when(beanFactory.containsBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME))
                .thenReturn(false);
        when(beanFactory.containsBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME))
                .thenReturn(true);
        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(environment);
        when(environment.getConversionService()).thenReturn(null);

        ConversionService resolved = new ConversionServiceResolver(beanFactory).resolve();

        assertThat(resolved).isInstanceOf(DefaultFormattingConversionService.class);
        assertThat(resolved.convert("42", Integer.class)).isEqualTo(42);
    }

    @Test
    void resolveSkipsConversionServiceBeanWhenBeanFactoryConversionServiceExists() {
        ConversionService beanFactoryConversionService = mock(ConversionService.class);
        when(beanFactory.getConversionService()).thenReturn(beanFactoryConversionService);

        assertThat(new ConversionServiceResolver(beanFactory).resolve()).isSameAs(beanFactoryConversionService);
        verify(beanFactory, never()).containsBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME);
    }

    @Test
    void resolveUsesDefaultWhenNoEnvironmentBean() {
        when(beanFactory.getConversionService()).thenReturn(null);
        when(beanFactory.containsBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME))
                .thenReturn(false);
        when(beanFactory.containsBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME))
                .thenReturn(false);

        ConversionService resolved = new ConversionServiceResolver(beanFactory).resolve();

        assertThat(resolved).isInstanceOf(DefaultFormattingConversionService.class);
    }
}
