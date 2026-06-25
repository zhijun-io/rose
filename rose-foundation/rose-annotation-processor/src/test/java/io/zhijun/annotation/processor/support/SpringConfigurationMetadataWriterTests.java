package io.zhijun.annotation.processor.support;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.zhijun.annotation.RosePropertyHint;

import static org.assertj.core.api.Assertions.assertThat;

class SpringConfigurationMetadataWriterTests {

    @Test
    void writesSortedPropertyMetadataJson() {
        RosePropertyHint beta = hint("rose.demo.beta", "second");
        RosePropertyHint alpha = hint("rose.demo.alpha", "first");

        String json = SpringConfigurationMetadataWriter.toJson(Arrays.asList(beta, alpha));

        assertThat(json).contains("\"name\": \"rose.demo.alpha\"");
        assertThat(json.indexOf("rose.demo.alpha")).isLessThan(json.indexOf("rose.demo.beta"));
        assertThat(json).contains("\"description\": \"first\"");
    }

    private static RosePropertyHint hint(final String name, final String description) {
        return new RosePropertyHint() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RosePropertyHint.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String type() {
                return "java.lang.String";
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String defaultValue() {
                return "";
            }
        };
    }
}
