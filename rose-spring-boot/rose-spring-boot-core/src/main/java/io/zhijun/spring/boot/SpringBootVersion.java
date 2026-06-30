package io.zhijun.spring.boot;

/**
 * Spring Boot 版本枚举
 */
public enum SpringBootVersion {

    CURRENT("2.7.0");

    private final String version;

    SpringBootVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
