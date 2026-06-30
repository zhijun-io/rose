package io.zhijun.devservice.boot.autoconfigure.bootstrap.template;

import io.zhijun.devservice.boot.autoconfigure.bootstrap.BootstrapProfilesTemplate;
import io.zhijun.devservice.core.bootstrap.BootstrapMode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class BootstrapProfilesTemplateTests {

    private final BootstrapProfilesTemplate template = new BootstrapProfilesTemplate();

    @Test
    void shouldResolveDevProfilesAndIgnoreDuplicates() {
        assertThat(template.resolve(
                BootstrapMode.DEV,
                Collections.singletonList("dev"),
                Arrays.asList("dev", "dev2", " "),
                null))
                .containsExactly("dev2");
    }

    @Test
    void shouldResolveTestProfilesAndIgnoreDuplicates() {
        assertThat(template.resolve(
                BootstrapMode.TEST,
                Arrays.asList("test1"),
                null,
                Arrays.asList("test1", "test2")))
                .containsExactly("test2");
    }

    @Test
    void shouldReturnEmptyListForProdMode() {
        assertThat(template.resolve(
                BootstrapMode.PROD,
                Collections.singletonList("prod"),
                Arrays.asList("dev"),
                Arrays.asList("test")))
                .isEmpty();
    }
}
