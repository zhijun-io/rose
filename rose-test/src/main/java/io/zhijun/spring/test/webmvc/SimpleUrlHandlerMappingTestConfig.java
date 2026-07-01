package io.zhijun.spring.test.webmvc;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link SimpleUrlHandlerMapping} 测试配置
 */
public class SimpleUrlHandlerMappingTestConfig {

    public static final String BASE_PATH = "/simple";

    public static final String FIRST_PATH = BASE_PATH + "/1";

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put(BASE_PATH, this);
        urlMap.put(FIRST_PATH, this);
        mapping.setUrlMap(urlMap);
        return mapping;
    }
}
