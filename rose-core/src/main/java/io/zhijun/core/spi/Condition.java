package io.zhijun.core.spi;
import io.zhijun.core.spi.annotation.Spi;
/**
 * SPI load condition extension point.
 * Implement this interface to control whether an SPI implementation should be loaded.
 */
@Spi
public interface Condition {
    /**
     * Check if the implementation should be loaded.
     * @param implementationType implementation class
     * @return true to load, false to skip
     */
    boolean matches(Class<?> implementationType);
}
