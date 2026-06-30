package io.zhijun.core.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import io.zhijun.core.spi.annotation.Priority;

/**
 * SPI 工具类，基于 {@link java.util.ServiceLoader} 实现。
  *
  * <p><b>职责边界</b>：SPI 层只做两件事——<b>发现</b>（从 {@code META-INF/services/} 加载）
  * 和 <b>排序</b>（按 {@link Priority @Priority} 排序实现类）。
  * 其余所有能力（缓存、生命周期管理、条件过滤、依赖注入）由上层框架
  *（Spring / Spring Boot）或应用层负责。
  *
  * <p>服务描述文件在编译期由 {@code SpiImplProcessor} 自动生成。
  */
public final class SpiServiceLoader {

    private SpiServiceLoader() {}

    private static <S> List<S> loadAndSort(Class<S> type, ClassLoader cl) {
        List<S> instances = new ArrayList<S>();
        ServiceLoader.load(type, cl).forEach(instances::add);
        instances.sort(Comparator.comparingInt(impl -> resolvePriority(impl.getClass())));
        return Collections.unmodifiableList(instances);
    }

    /** 加载 {@code type} 的所有实现，按优先级排序。 */
    public static <S> List<S> loadAll(Class<S> type) {
        ClassLoader cl = type.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return loadAndSort(type, cl);
    }

    /** 使用指定 {@link ClassLoader} 加载所有实现。 */
    public static <S> List<S> loadAll(Class<S> type, ClassLoader cl) {
        return loadAndSort(type, cl);
    }

    /** 加载 {@code type} 优先级最高的实现。 */
    public static <S> Optional<S> loadFirst(Class<S> type) {
        return loadAll(type).stream().findFirst();
    }

    /**
     * 惰性流式加载 {@code type} 的所有实现。结果<b>不排序</b>。
     * <p>当你需要 {@link Stream} 操作但又不想物化到 {@link List} 时使用。
     */
    public static <S> Stream<S> stream(Class<S> type) {
        Iterator<S> it = ServiceLoader.load(type).iterator();
        Spliterator<S> spliterator = Spliterators.spliteratorUnknownSize(it, 0);
        return StreamSupport.stream(spliterator, false);
    }

    private static int resolvePriority(Class<?> implClass) {
        Priority p = implClass.getAnnotation(Priority.class);
        return p != null ? p.value() : Integer.MAX_VALUE;
    }
}
