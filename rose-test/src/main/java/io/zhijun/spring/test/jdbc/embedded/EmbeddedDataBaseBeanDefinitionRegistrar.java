package io.zhijun.spring.test.jdbc.embedded;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.StringReader;
import java.util.Properties;
import java.util.StringJoiner;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * 嵌入式数据库 {@link ImportBeanDefinitionRegistrar}
 */
class EmbeddedDataBaseBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Class<EnableEmbeddedDatabase> ANNOTATION_TYPE = EnableEmbeddedDatabase.class;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        registerBeanDefinitions(attributes, registry);
    }

    void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        assertBeanName(attributes, registry);
        EmbeddedDatabaseType type = attributes.getEnum("type");
        switch (type) {
            case SQLITE:
                processSQLite(attributes, registry);
                break;
            case H2:
                processH2(attributes, registry);
                break;
        }
    }

    private void assertBeanName(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String beanName = attributes.getString("dataSource");
        if (registry.containsBeanDefinition(beanName)) {
            throw new BeanCreationException("重复的 BeanDefinition 名称：" + beanName);
        }
    }

    private void processSQLite(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerSQLiteDataSourceBeanDefinition(attributes, registry);
    }

    private void processH2(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerH2DataSourceBeanDefinition(attributes, registry);
    }

    private void registerSQLiteDataSourceBeanDefinition(AnnotationAttributes attributes,
                                                        BeanDefinitionRegistry registry) {
        registerDataSourceBeanDefinition("jdbc:sqlite::memory:", attributes, registry);
    }

    private void registerH2DataSourceBeanDefinition(AnnotationAttributes attributes,
                                                    BeanDefinitionRegistry registry) {
        registerDataSourceBeanDefinition("jdbc:h2:mem:", attributes, registry);
    }

    private void registerDataSourceBeanDefinition(String jdbcURL,
                                                  AnnotationAttributes attributes,
                                                  BeanDefinitionRegistry registry) {
        String beanName = attributes.getString("dataSource");
        boolean primary = attributes.getBoolean("primary");
        Properties properties = resolveProperties(attributes, registry);

        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(DriverManagerDataSource.class);
        beanDefinitionBuilder.addConstructorArgValue(jdbcURL);
        beanDefinitionBuilder.addConstructorArgValue(properties);

        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private Properties resolveProperties(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) registry;
        String[] values = attributes.getStringArray("properties");
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        for (String value : values) {
            String resolvedValue = beanFactory.resolveEmbeddedValue(value);
            stringJoiner.add(resolvedValue);
        }
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(stringJoiner.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
