package io.zhijun.spring.test.jdbc.embedded;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link EnableEmbeddedDatabases} {@link ImportBeanDefinitionRegistrar}
 */
class EmbeddedDataBaseBeanDefinitionsRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Class<EnableEmbeddedDatabases> ANNOTATION_TYPE = EnableEmbeddedDatabases.class;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        EmbeddedDataBaseBeanDefinitionRegistrar registrar = new EmbeddedDataBaseBeanDefinitionRegistrar();
        for (AnnotationAttributes valueAttributes : attributes.getAnnotationArray("value")) {
            registrar.registerBeanDefinitions(valueAttributes, registry);
        }
    }
}
