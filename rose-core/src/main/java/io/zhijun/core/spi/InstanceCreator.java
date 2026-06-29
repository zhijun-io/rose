package io.zhijun.core.spi;
import io.zhijun.core.spi.annotation.Spi;
/**
 * SPI instance creator extension point.
 * Implement this interface to customize SPI instance creation logic, such as:
 * - Spring bean injection
 * - AOP enhancement
 * - Instance pooling
 * - Custom initialization
 */
@Spi
public interface InstanceCreator {
    /**
     * Create SPI instance.
     * @param implementationType implementation class
     * @return instance, return null to fallback to default reflection creation
     * @param <T> instance type
     */
    <T> T createInstance(Class<T> implementationType);
}
