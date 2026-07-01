package io.zhijun.spring.web.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 基于 Jackson2 解析 JSON 格式创建 {@link WebEndpointMapping} 的工厂。
 */
public class Jackson2WebEndpointMappingFactory extends AbstractWebEndpointMappingFactory<String> {

    private static final String OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

    private static final boolean objectMapperPresent;

    static {
        boolean present = false;
        try {
            Class.forName(OBJECT_MAPPER_CLASS_NAME, false, Jackson2WebEndpointMappingFactory.class.getClassLoader());
            present = true;
        } catch (ClassNotFoundException e) {
            // Jackson not on classpath
        }
        objectMapperPresent = present;
    }

    @Override
    public boolean supports(String endpoint) {
        return objectMapperPresent;
    }

    @Override
    protected WebEndpointMapping doCreate(String endpoint) throws Throwable {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(endpoint, WebEndpointMapping.class);
    }
}
