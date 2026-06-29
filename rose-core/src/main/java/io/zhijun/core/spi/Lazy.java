package io.zhijun.core.spi;
import java.util.function.Supplier;
/**
 * 线程安全的懒加载包装器
 * 首次调用get()时才初始化实例，且只会初始化一次
 * @param <T> 实例类型
 */
public final class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private volatile T instance;
    /**
     * 创建懒加载包装器
     * @param supplier 实例创建函数
     */
    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    /**
     * 获取实例，首次调用会触发初始化
     * @return 初始化完成的实例
     */
    @Override
    public T get() {
        T result = instance;
        if (result == null) {
            synchronized (this) {
                result = instance;
                if (result == null) {
                    instance = result = supplier.get();
                }
            }
        }
        return result;
    }
    /**
     * 是否已初始化
     * @return true表示已经初始化完成
     */
    public boolean isInitialized() {
        return instance != null;
    }
}
