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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpiLoaderTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        SpiLoader.clearCache();
        SingletonImplementation.reset();
        PrototypeImplementation.reset();
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
    void supportsPrototypeImplementations() throws IOException {
        SpiLoader<SampleService> loader = SpiLoader.load(SampleService.class, createCompositeClassLoader());

        SampleService first = loader.get().get(3);
        SampleService second = loader.get().get(3);

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
                        PrototypeImplementation.class,
                        SingletonImplementation.class);
    }

    @Test
    void returnsFirstImplementationByPriority() throws IOException {
        SampleService first = SpiLoader.load(SampleService.class, createCompositeClassLoader())
                .getFirst()
                .orElseThrow(AssertionError::new);

        assertThat(first).isInstanceOf(ExplicitPriorityImplementation.class);
    }

    @Test
    void rejectsTypesWithoutSpiAnnotation() {
        assertThatThrownBy(() -> SpiLoader.load(PlainService.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@Spi");
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

    private ClassLoader createCompositeClassLoader() throws IOException {
        Path additionalResources = tempDir.resolve("extra-resources");
        Path servicesDirectory = additionalResources.resolve("META-INF").resolve("services");
        Files.createDirectories(servicesDirectory);
        Files.write(
                servicesDirectory.resolve(SampleService.class.getName()),
                Arrays.asList(
                        PriorityImplementation.class.getName(),
                        ExplicitPriorityImplementation.class.getName(),
                        PrototypeImplementation.class.getName()),
                StandardCharsets.UTF_8);
        URL[] urls = new URL[] {additionalResources.toUri().toURL()};
        return new URLClassLoader(urls, SampleService.class.getClassLoader());
    }
}
