package io.zhijun.core.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpiLoaderTest {

    private static final AtomicInteger DISABLED_INITIALIZATIONS = new AtomicInteger();
    private static final AtomicInteger LIFECYCLE_INIT_COUNT = new AtomicInteger();
    private static final AtomicInteger LIFECYCLE_DESTROY_COUNT = new AtomicInteger();

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        SpiLoader.clearCache();
        SingletonImplementation.reset();
        PrototypeImplementation.reset();
        LifecycleImplementation.reset();
        DISABLED_INITIALIZATIONS.set(0);
    }

    @Test
    void loadsAndSortsImplementationsAcrossDescriptors() throws IOException {
        List<Class<? extends SampleService>> implementationTypes = SpiLoader
                .load(SampleService.class, createCompositeClassLoader())
                .getImplementationTypes();

        assertThat(implementationTypes)
                .containsExactly(
                        ExplicitPriorityImplementation.class,
                        PriorityImplementation.class,
                        LifecycleImplementation.class,
                        DefaultImplementation.class,
                        PrototypeImplementation.class,
                        SingletonImplementation.class);
    }

    @Test
    void returnsSingletonInstancesByDefault() {
        List<SampleService> first = SpiLoader.load(SampleService.class).get();
        List<SampleService> second = SpiLoader.load(SampleService.class).get();

        assertThat(first.get(1)).isSameAs(second.get(1));
        assertThat(SingletonImplementation.instancesCreated()).isEqualTo(1);
    }

    @Test
    void doesNotCacheLoadersForExplicitClassLoaders() throws IOException {
        SpiLoader.load(SampleService.class, createCompositeClassLoader()).get();
        SpiLoader.load(SampleService.class, createCompositeClassLoader()).get();

        assertThat(SingletonImplementation.instancesCreated()).isEqualTo(2);
    }

    @Test
    void supportsPrototypeImplementations() throws IOException {
        SpiLoader<SampleService> loader = SpiLoader.load(SampleService.class, createCompositeClassLoader());

        SampleService first = loader.get().get(4);
        SampleService second = loader.get().get(4);

        assertThat(first).isNotSameAs(second);
        assertThat(PrototypeImplementation.instancesCreated()).isEqualTo(2);
    }

    @Test
    void supportsExcludingImplementations() throws IOException {
        List<Class<? extends SampleService>> implementationTypes = SpiLoader
                .load(
                        SampleService.class,
                        createCompositeClassLoader(),
                        Arrays.asList(PriorityImplementation.class.getName(), DefaultImplementation.class.getName()))
                .getImplementationTypes();

        assertThat(implementationTypes)
                .containsExactly(
                        ExplicitPriorityImplementation.class,
                        LifecycleImplementation.class,
                        PrototypeImplementation.class,
                        SingletonImplementation.class);
    }

    @Test
    void returnsFirstImplementationByPriority() throws IOException {
        SampleService first = SpiLoader.load(SampleService.class, createCompositeClassLoader())
                .getFirst()
                .orElseThrow(() -> new AssertionError());

        assertThat(first).isInstanceOf(ExplicitPriorityImplementation.class);
    }

    @Test
    void rejectsTypesWithoutSpiAnnotation() {
        assertThatThrownBy(() -> SpiLoader.load(PlainService.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@Spi");
    }

    @Test
    void doesNotInitializeDisabledImplementationsWhenLoadingDefinitions() {
        SpiLoader.load(SampleService.class).getImplementationTypes();

        assertThat(DISABLED_INITIALIZATIONS).hasValue(0);
    }

    @Test
    void callsLifecycleInitMethodAfterInstantiation() {
        // 第一次获取实例，应该触发 init
        List<SampleService> instances = SpiLoader.load(SampleService.class).get();
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(1);
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(0);

        // 第二次获取实例，单例不会重新初始化，init 次数不变
        List<SampleService> instances2 = SpiLoader.load(SampleService.class).get();
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(1);
        assertThat(instances.get(0)).isInstanceOf(LifecycleImplementation.class);
        assertThat(instances.get(0)).isSameAs(instances2.get(0));
    }

    @Test
    void callsLifecycleDestroyMethodWhenDestroyingLoader() {
        // 加载并初始化实例
        SpiLoader<SampleService> loader = SpiLoader.load(SampleService.class);
        List<SampleService> instances = loader.get();
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(1);
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(0);

        // 销毁 Loader，应该触发 destroy
        loader.destroy();
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(1);

        // 再次获取实例，会重新创建并触发 init
        List<SampleService> newInstances = loader.get();
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(2);
        assertThat(newInstances.get(0)).isNotSameAs(instances.get(0));
    }

    @Test
    void reloadCreatesNewInstancesAndDestroysOldOnes() {
        // 加载初始实例
        SpiLoader<SampleService> oldLoader = SpiLoader.load(SampleService.class);
        SampleService oldInstance = oldLoader.getFirst().orElseThrow(() -> new AssertionError());
        int oldInitCount = LIFECYCLE_INIT_COUNT.get();
        assertThat(oldInitCount).isGreaterThan(0);
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(0);

        // 执行重载
        SpiLoader<SampleService> newLoader = oldLoader.reload();
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(1); // 旧实例被销毁

        // 新 Loader 返回新的实例
        SampleService newInstance = newLoader.getFirst().orElseThrow(() -> new AssertionError());
        assertThat(newInstance).isNotSameAs(oldInstance);
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(oldInitCount + 1); // 新实例被初始化
    }

    @Test
    void reloadAllDestroysAllInstances() {
        assertThat(SpiLoader.getAllSpiInfo()).isEmpty();

        // 加载多个 SPI 实例
        SpiLoader<SampleService> loader1 = SpiLoader.load(SampleService.class);
        loader1.get();
        SpiLoader<AnotherSampleService> loader2 = SpiLoader.load(AnotherSampleService.class);
        loader2.get();

        int initCount1 = LIFECYCLE_INIT_COUNT.get();
        int initCount2 = AnotherLifecycleImplementation.initCount.get();
        assertThat(initCount1).isGreaterThan(0);
        assertThat(initCount2).isGreaterThan(0);
        assertThat(SpiLoader.getAllSpiInfo()).containsKeys(SampleService.class, AnotherSampleService.class);

        // 全局重载
        Set<Class<?>> reloadedTypes = SpiLoader.reloadAll();
        assertThat(reloadedTypes).contains(SampleService.class, AnotherSampleService.class);
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(1);
        assertThat(AnotherLifecycleImplementation.destroyCount.get()).isEqualTo(1);
        assertThat(SpiLoader.getAllSpiInfo()).isEmpty();

        // 旧 loader 失去缓存后再次访问，也会重新创建实例
        SampleService recreatedByOldLoader = loader1.getFirst().orElseThrow(() -> new AssertionError());
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(initCount1 + 1);

        // 重新加载会创建新实例
        SpiLoader<SampleService> newLoader1 = SpiLoader.load(SampleService.class);
        assertThat(newLoader1.getFirst().orElseThrow(() -> new AssertionError()))
                .isNotSameAs(recreatedByOldLoader);
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(initCount1 + 2);
    }

    @Test
    void reloadWithClassLoaderOnlyAffectsCachedLoaders() throws IOException {
        // 系统类加载器加载的实例
        SpiLoader<SampleService> systemLoader = SpiLoader.load(SampleService.class);
        SampleService systemInstance = systemLoader.getFirst().orElseThrow(() -> new AssertionError());
        int systemInitCount = LIFECYCLE_INIT_COUNT.get();

        // 自定义类加载器加载的实例
        ClassLoader customClassLoader = createCompositeClassLoader();
        SpiLoader<SampleService> customLoader = SpiLoader.load(SampleService.class, customClassLoader);
        customLoader.get();
        SampleService customInstance = customLoader.getFirst().orElseThrow(() -> new AssertionError());
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(systemInitCount + 1);

        // 显式 classLoader 创建的 loader 默认不缓存，因此这里不会命中任何缓存条目
        Set<Class<?>> reloadedTypes = SpiLoader.reloadAll(customClassLoader);
        assertThat(reloadedTypes).isEmpty();

        // 现有实例都不受影响
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(0);

        // 系统类加载器的实例仍然可用
        assertThat(systemLoader.getFirst().orElseThrow(() -> new AssertionError())).isSameAs(systemInstance);
        assertThat(customLoader.getFirst().orElseThrow(() -> new AssertionError())).isSameAs(customInstance);

        // 再次加载自定义类加载器的 SPI 仍会创建新的 loader 与实例，因为显式 classLoader 的 loader 不缓存
        SpiLoader<SampleService> newCustomLoader = SpiLoader.load(SampleService.class, customClassLoader);
        assertThat(newCustomLoader.getFirst().orElseThrow(() -> new AssertionError()))
                .isNotSameAs(customInstance);
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(systemInitCount + 1);
    }

    @Test
    void reloadDoesNotDestroyCurrentInstanceWhenRebuildFails() throws IOException {
        Path additionalResources = tempDir.resolve("mutable-resources");
        writeServiceDescriptor(
                additionalResources,
                Arrays.asList(
                        PriorityImplementation.class.getName(),
                        ExplicitPriorityImplementation.class.getName(),
                        PrototypeImplementation.class.getName()));
        ClassLoader mutableClassLoader =
                new URLClassLoader(new URL[] {additionalResources.toUri().toURL()}, SampleService.class.getClassLoader());

        SpiLoader<SampleService> customLoader = SpiLoader.load(SampleService.class, mutableClassLoader);
        SampleService oldInstance = customLoader.getFirst().orElseThrow(() -> new AssertionError());
        int initCount = LIFECYCLE_INIT_COUNT.get();

        writeServiceDescriptor(additionalResources, Arrays.asList("com.example.missing.MissingImplementation"));

        assertThatThrownBy(customLoader::reload).isInstanceOf(RuntimeException.class);
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(0);
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(initCount);
        assertThat(customLoader.getFirst().orElseThrow(() -> new AssertionError())).isSameAs(oldInstance);
    }

    @Test
    void reloadableHandleSwitchesToNewLoaderAtomically() {
        ReloadableSpiHandle<SampleService> handle = ReloadableSpiHandle.of(SampleService.class);

        SpiLoader<SampleService> initialLoader = handle.getLoader();
        SampleService initialInstance = handle.requireFirst();
        int initCount = LIFECYCLE_INIT_COUNT.get();

        SpiLoader<SampleService> reloadedLoader = handle.reload();
        SampleService reloadedInstance = handle.requireFirst();

        assertThat(handle.getLoader()).isSameAs(reloadedLoader);
        assertThat(reloadedLoader).isNotSameAs(initialLoader);
        assertThat(reloadedInstance).isNotSameAs(initialInstance);
        assertThat(LIFECYCLE_DESTROY_COUNT).hasValue(1);
        assertThat(LIFECYCLE_INIT_COUNT).hasValue(initCount + 1);
    }

    interface PlainService {}

    @Spi
    interface SampleService {

        String id();
    }

    @SpiImpl(priority = 200)
    public static class ExplicitPriorityImplementation implements SampleService {

        @Override
        public String id() {
            return "explicit";
        }
    }

    @SpiImpl(priority = 300)
    public static class PriorityImplementation implements SampleService {

        @Override
        public String id() {
            return "priority";
        }
    }

    public static class DefaultImplementation implements SampleService {

        @Override
        public String id() {
            return "default";
        }
    }

    @SpiImpl(singleton = false)
    public static class PrototypeImplementation implements SampleService {

        private static final AtomicInteger INSTANCES = new AtomicInteger();

        public PrototypeImplementation() {
            INSTANCES.incrementAndGet();
        }

        static int instancesCreated() {
            return INSTANCES.get();
        }

        static void reset() {
            INSTANCES.set(0);
        }

        @Override
        public String id() {
            return "prototype";
        }
    }

    @SpiImpl(enabled = false, priority = 50)
    public static class DisabledImplementation implements SampleService {

        static {
            DISABLED_INITIALIZATIONS.incrementAndGet();
        }

        @Override
        public String id() {
            return "disabled";
        }
    }

    public static class SingletonImplementation implements SampleService {

        private static final AtomicInteger INSTANCES = new AtomicInteger();

        public SingletonImplementation() {
            INSTANCES.incrementAndGet();
        }

        static int instancesCreated() {
            return INSTANCES.get();
        }

        static void reset() {
            INSTANCES.set(0);
        }

        @Override
        public String id() {
            return "singleton";
        }
    }

    @SpiImpl(priority = 600) // 低优先级，排在最后
    public static class LifecycleImplementation implements SampleService, SpiLifecycle {
        static void reset() {
            LIFECYCLE_INIT_COUNT.set(0);
            LIFECYCLE_DESTROY_COUNT.set(0);
        }

        @Override
        public void init() {
            LIFECYCLE_INIT_COUNT.incrementAndGet();
        }

        @Override
        public void destroy() {
            LIFECYCLE_DESTROY_COUNT.incrementAndGet();
        }

        @Override
        public String id() {
            return "lifecycle";
        }
    }

    @Spi
    interface AnotherSampleService {
        String name();
    }

    @SpiImpl
    public static class AnotherLifecycleImplementation implements AnotherSampleService, SpiLifecycle {
        static final AtomicInteger initCount = new AtomicInteger();
        static final AtomicInteger destroyCount = new AtomicInteger();

        static void reset() {
            initCount.set(0);
            destroyCount.set(0);
        }

        @Override
        public void init() {
            initCount.incrementAndGet();
        }

        @Override
        public void destroy() {
            destroyCount.incrementAndGet();
        }

        @Override
        public String name() {
            return "another-lifecycle";
        }
    }

    private ClassLoader createCompositeClassLoader() throws IOException {
        Path additionalResources = tempDir.resolve("extra-resources");
        writeServiceDescriptor(
                additionalResources,
                Arrays.asList(
                        PriorityImplementation.class.getName(),
                        ExplicitPriorityImplementation.class.getName(),
                        PrototypeImplementation.class.getName()));
        URL[] urls = new URL[] {additionalResources.toUri().toURL()};
        return new URLClassLoader(urls, SampleService.class.getClassLoader());
    }

    private void writeServiceDescriptor(Path additionalResources, List<String> implementations) throws IOException {
        Path servicesDirectory = additionalResources.resolve("META-INF").resolve("services");
        Files.createDirectories(servicesDirectory);
        Files.write(servicesDirectory.resolve(SampleService.class.getName()), implementations, StandardCharsets.UTF_8);
    }
}
