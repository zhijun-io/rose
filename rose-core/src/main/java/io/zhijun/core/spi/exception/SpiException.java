package io.zhijun.core.spi.exception;
/**
 * Base SPI exception.
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
    public Class<?> getSpiInterface() {
        return spiInterface;
    }
}
