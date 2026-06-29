package io.zhijun.core.spi;
/**
 * SPI生命周期回调接口
 * SPI实现类可以实现此接口，在初始化和销毁时执行自定义逻辑
 */
public interface SpiLifecycle {
    /**
     * 初始化回调，实例创建完成、依赖注入完成后执行
     * 单例模式只会执行一次，多例模式每次实例化都会执行
     */
    default void init() {
    }
    /**
     * 销毁回调，SPI重新加载/应用关闭时执行
     * 单例模式只会执行一次，多例模式实例不会被自动销毁，需要用户自行管理
     */
    default void destroy() {
    }
}
