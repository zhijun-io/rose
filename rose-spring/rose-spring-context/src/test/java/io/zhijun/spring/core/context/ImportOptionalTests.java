package io.zhijun.spring.core.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

class ImportOptionalTests {

    static final String EXISTING_CLASS = "io.zhijun.spring.core.context.ImportOptionalTests$ExistingImportConfig";

    @Test
    void existingClassIsImported() {
        ImportOptionalSelector selector = new ImportOptionalSelector();
        selector.setBeanClassLoader(getClass().getClassLoader());

        AnnotationMetadata metadata = metadataFor(ExistingImportConfig.class);
        String[] imports = selector.selectImports(metadata);

        assertThat(imports).contains(EXISTING_CLASS);
    }

    @Test
    void missingClassIsSkipped() {
        ImportOptionalSelector selector = new ImportOptionalSelector();
        selector.setBeanClassLoader(getClass().getClassLoader());

        AnnotationMetadata metadata = metadataFor(MissingImportConfig.class);
        String[] imports = selector.selectImports(metadata);

        assertThat(imports).isEmpty();
    }

    @Test
    void noAnnotationReturnsEmpty() {
        ImportOptionalSelector selector = new ImportOptionalSelector();
        selector.setBeanClassLoader(getClass().getClassLoader());

        AnnotationMetadata metadata = metadataFor(NoAnnotationConfig.class);
        String[] imports = selector.selectImports(metadata);

        assertThat(imports).isEmpty();
    }

    private static AnnotationMetadata metadataFor(Class<?> configClass) {
        try {
            SimpleMetadataReaderFactory factory = new SimpleMetadataReaderFactory();
            return factory.getMetadataReader(configClass.getName()).getAnnotationMetadata();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ImportOptional(EXISTING_CLASS)
    static class ExistingImportConfig {
    }

    @ImportOptional("com.does.not.Exist")
    static class MissingImportConfig {
    }

    static class NoAnnotationConfig {
    }
}
