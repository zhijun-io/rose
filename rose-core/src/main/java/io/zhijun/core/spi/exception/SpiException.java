package io.zhijun.core.spi.exception;

/**
 * SPI exception.
 */
public class SpiException extends RuntimeException {

    private final Class<?> spiInterface;

    public SpiException(Class<?> spiInterface, String message) {
        super(String.format("[SPI: %s] %s", spiInterface.getName(), message));
        this.spiInterface = spiInterface;
    }

    public SpiException(Class<?> spiInterface, String message, Throwable cause) {
        super(String.format("[SPI: %s] %s", spiInterface.getName(), message), cause);
        this.spiInterface = spiInterface;
    }

    public SpiException(Class<?> spiInterface, String resourcePath, String message, Throwable cause) {
        super(String.format("[SPI: %s] Config: %s, %s", spiInterface.getName(), resourcePath, message), cause);
        this.spiInterface = spiInterface;
    }

    public SpiException(Class<?> spiInterface, Class<?> implementationType, String message, Throwable cause) {
        super(String.format("[SPI: %s] Implementation: %s, %s", spiInterface.getName(), implementationType.getName(), message), cause);
        this.spiInterface = spiInterface;
    }

    public Class<?> getSpiInterface() {
        return spiInterface;
    }
}
