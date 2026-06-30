package io.zhijun.devservice.boot.autoconfigure.template;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import io.zhijun.devservice.boot.autoconfigure.DevServiceConflictValidatorTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.devservice.boot.autoconfigure.MultipleDevServiceException;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;

class DevServiceConflictValidatorTemplateTests {

    private final DevServiceConflictValidatorTemplate template = new DevServiceConflictValidatorTemplate();

    @Test
    void doesNotThrowForDistinctCategories() {
        ObjectProvider<DevServiceProvider> providers = providerProvider(Arrays.asList(
                DevServiceProvider.of("lgtm", DevServiceCategory.OLLAMA),
                DevServiceProvider.of("postgresql", DevServiceCategory.JDBC)));

        assertThatCode(() -> template.conflictValidator(providers).afterSingletonsInstantiated())
                .doesNotThrowAnyException();
    }

    @Test
    void throwsForDuplicateCategory() {
        ObjectProvider<DevServiceProvider> providers = providerProvider(Arrays.asList(
                DevServiceProvider.of("lgtm", DevServiceCategory.OLLAMA),
                DevServiceProvider.of("openlit", DevServiceCategory.OLLAMA)));

        assertThatThrownBy(() -> template.conflictValidator(providers).afterSingletonsInstantiated())
                .isInstanceOf(MultipleDevServiceException.class);
    }

    private static ObjectProvider<DevServiceProvider> providerProvider(final List<DevServiceProvider> providers) {
        ObjectProvider<DevServiceProvider> provider = mock(ObjectProvider.class);
        when(provider.iterator()).thenReturn(providers.iterator());
        return provider;
    }
}
