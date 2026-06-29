package io.zhijun.core.spi.exception;
/**
 * SPI instance instantiation exception.
 */
public class SpiInstantiationException extends SpiException {
    private final Class<?> implementationType;
    public SpiInstantiationException(Class<?> spiInterface, Class<?> implementationType, String message, Throwable cause) {
        super(spiInterface, String.format("Implementation: %s, %s", implementationType.getName(), message), cause);
        this.implementationType = implementationType;
    }
    public Class<?> getImplementationType() {
        return implementationType;
    }
}
