package io.zhijun.devservice.core.api.provider;


/**
 * Mutually exclusive dev service category.
 * <p>
 * Providers in the same category compete for the same Spring integration point
 * (for example {@link #JDBC} for {@code spring.datasource}).
 */
public enum DevServiceCategory {
    JDBC("jdbc"),

    REDIS("redis"),

    MONGODB("mongodb"),

    JMS("jms"),

    MQTT("mqtt"),

    KAFKA("kafka"),

    RABBITMQ("rabbitmq"),

    OLLAMA("ollama"),

    OPENTELEMETRY("opentelemetry");

    private final String id;

    DevServiceCategory(String id) {
        this.id = id;
    }

    /**
     * Stable category identifier used in conflict messages and grouping.
     */
    public String id() {
        return id;
    }
}
