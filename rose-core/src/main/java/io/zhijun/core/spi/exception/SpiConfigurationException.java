package io.zhijun.core.spi.exception;
/**
 * SPI configuration exception.
 */
public class SpiConfigurationException extends SpiException {
    private final String resourcePath;
    public SpiConfigurationException(Class<?> spiInterface, String resourcePath, String message, Throwable cause) {
        super(spiInterface, String.format("Config: %s, %s", resourcePath, message), cause);
        this.resourcePath = resourcePath;
    }
    public String getResourcePath() {
        return resourcePath;
    }
}
