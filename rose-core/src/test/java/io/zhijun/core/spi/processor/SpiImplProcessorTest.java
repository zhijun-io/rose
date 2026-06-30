package io.zhijun.core.spi.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

class SpiImplProcessorTest {

    @Test
    void generatesServiceFile() {
        Compilation compilation = javac()
                .withProcessors(new SpiImplProcessor())
                .compile(
                        JavaFileObjects.forSourceLines("test.spi.GreetingService",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.Spi;",
                                "@Spi",
                                "public interface GreetingService {}"
                        ),
                        JavaFileObjects.forSourceLines("test.spi.HelloGreeting",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.SpiImpl;",
                                "import io.zhijun.core.spi.annotation.Priority;",
                                "@SpiImpl",
                                "@Priority(100)",
                                "public class HelloGreeting implements GreetingService {}"
                        )
                );

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/services/test.spi.GreetingService")
                .contentsAsUtf8String()
                .isEqualTo("test.spi.HelloGreeting\n");
    }

    @Test
    void skipsAbstractClass() {
        Compilation compilation = javac()
                .withProcessors(new SpiImplProcessor())
                .compile(
                        JavaFileObjects.forSourceLines("test.spi.GreetingService",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.Spi;",
                                "@Spi",
                                "public interface GreetingService {}"
                        ),
                        JavaFileObjects.forSourceLines("test.spi.AbstractGreeting",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.SpiImpl;",
                                "@SpiImpl",
                                "public abstract class AbstractGreeting implements GreetingService {}"
                        )
                );

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining("abstract");
    }

    @Test
    void warnsWhenNoSpiInterface() {
        Compilation compilation = javac()
                .withProcessors(new SpiImplProcessor())
                .compile(
                        JavaFileObjects.forSourceLines("test.spi.NotSpi",
                                "package test.spi;",
                                "public interface NotSpi {}"
                        ),
                        JavaFileObjects.forSourceLines("test.spi.SomeImpl",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.SpiImpl;",
                                "@SpiImpl",
                                "public class SomeImpl implements NotSpi {}"
                        )
                );

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining("does not implement any @Spi");
    }

    @Test
    void supportsMultipleImplementations() {
        Compilation compilation = javac()
                .withProcessors(new SpiImplProcessor())
                .compile(
                        JavaFileObjects.forSourceLines("test.spi.GreetingService",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.Spi;",
                                "@Spi",
                                "public interface GreetingService {}"
                        ),
                        JavaFileObjects.forSourceLines("test.spi.HelloGreeting",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.SpiImpl;",
                                "@SpiImpl",
                                "public class HelloGreeting implements GreetingService {}"
                        ),
                        JavaFileObjects.forSourceLines("test.spi.ByeGreeting",
                                "package test.spi;",
                                "import io.zhijun.core.spi.annotation.SpiImpl;",
                                "@SpiImpl",
                                "public class ByeGreeting implements GreetingService {}"
                        )
                );

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/services/test.spi.GreetingService")
                .contentsAsUtf8String()
                .isEqualTo("test.spi.HelloGreeting\ntest.spi.ByeGreeting\n");
    }
}
