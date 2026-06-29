package io.zhijun.core.annotation.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.util.Optional;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

class SpiImplProcessorTest {

    @Test
    void generatesServiceFileForSpiImplementation() throws IOException {
        JavaFileObject spiType = JavaFileObjects.forSourceLines(
                "com.example.spi.ExampleService",
                "package com.example.spi;",
                "import io.zhijun.core.spi.annotation.Spi;",
                "@Spi",
                "public interface ExampleService {",
                "    String id();",
                "}");
        JavaFileObject implementation = JavaFileObjects.forSourceLines(
                "com.example.spi.impl.DefaultExampleService",
                "package com.example.spi.impl;",
                "import com.example.spi.ExampleService;",
                "import io.zhijun.core.spi.annotation.SpiImpl;",
                "@SpiImpl",
                "public class DefaultExampleService implements ExampleService {",
                "    @Override",
                "    public String id() {",
                "        return \"default\";",
                "    }",
                "}");

        Compilation compilation = Compiler.javac()
                .withProcessors(new SpiImplProcessor())
                .compile(spiType, implementation);

        assertThat(compilation).succeeded();
        Optional<JavaFileObject> generatedFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "META-INF/services/com.example.spi.ExampleService");
        assertThat(generatedFile).isPresent();
        assertThat(read(generatedFile.get())).isEqualTo("com.example.spi.impl.DefaultExampleService\n");
    }

    @Test
    void generatesOneDescriptorForMultipleImplementations() throws IOException {
        JavaFileObject spiType = JavaFileObjects.forSourceLines(
                "com.example.spi.ExampleService",
                "package com.example.spi;",
                "import io.zhijun.core.spi.annotation.Spi;",
                "@Spi",
                "public interface ExampleService {}");
        JavaFileObject firstImplementation = JavaFileObjects.forSourceLines(
                "com.example.spi.impl.AlphaExampleService",
                "package com.example.spi.impl;",
                "import com.example.spi.ExampleService;",
                "import io.zhijun.core.spi.annotation.SpiImpl;",
                "@SpiImpl",
                "public class AlphaExampleService implements ExampleService {}");
        JavaFileObject secondImplementation = JavaFileObjects.forSourceLines(
                "com.example.spi.impl.BetaExampleService",
                "package com.example.spi.impl;",
                "import com.example.spi.ExampleService;",
                "import io.zhijun.core.spi.annotation.SpiImpl;",
                "@SpiImpl",
                "public class BetaExampleService implements ExampleService {}");

        Compilation compilation = Compiler.javac()
                .withProcessors(new SpiImplProcessor())
                .compile(spiType, firstImplementation, secondImplementation);

        assertThat(compilation).succeeded();
        Optional<JavaFileObject> generatedFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "META-INF/services/com.example.spi.ExampleService");
        assertThat(generatedFile).isPresent();
        assertThat(read(generatedFile.get()))
                .isEqualTo("com.example.spi.impl.AlphaExampleService\ncom.example.spi.impl.BetaExampleService\n");
    }

    @Test
    void failsWhenSpiImplDoesNotImplementSpiType() {
        JavaFileObject implementation = JavaFileObjects.forSourceLines(
                "com.example.spi.impl.InvalidExampleService",
                "package com.example.spi.impl;",
                "import io.zhijun.core.spi.annotation.SpiImpl;",
                "@SpiImpl",
                "public class InvalidExampleService {}");

        Compilation compilation = Compiler.javac()
                .withProcessors(new SpiImplProcessor())
                .compile(implementation);

        assertThat(compilation).failed();
        assertThat(compilation)
                .hadErrorContaining("must implement at least one interface or superclass annotated with @Spi");
    }

    @Test
    void failsWhenSpiImplDoesNotDeclareAccessibleNoArgsConstructor() {
        JavaFileObject spiType = JavaFileObjects.forSourceLines(
                "com.example.spi.ExampleService",
                "package com.example.spi;",
                "import io.zhijun.core.spi.annotation.Spi;",
                "@Spi",
                "public interface ExampleService {}");
        JavaFileObject implementation = JavaFileObjects.forSourceLines(
                "com.example.spi.impl.InvalidExampleService",
                "package com.example.spi.impl;",
                "import com.example.spi.ExampleService;",
                "import io.zhijun.core.spi.annotation.SpiImpl;",
                "@SpiImpl",
                "public class InvalidExampleService implements ExampleService {",
                "    public InvalidExampleService(String value) {}",
                "}");

        Compilation compilation = Compiler.javac()
                .withProcessors(new SpiImplProcessor())
                .compile(spiType, implementation);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must declare an accessible no-args constructor");
    }

    @Test
    void allowsImplicitDefaultConstructor() {
        JavaFileObject spiType = JavaFileObjects.forSourceLines(
                "com.example.spi.ExampleService",
                "package com.example.spi;",
                "import io.zhijun.core.spi.annotation.Spi;",
                "@Spi",
                "public interface ExampleService {}");
        JavaFileObject implementation = JavaFileObjects.forSourceLines(
                "com.example.spi.impl.DefaultExampleService",
                "package com.example.spi.impl;",
                "import com.example.spi.ExampleService;",
                "import io.zhijun.core.spi.annotation.SpiImpl;",
                "@SpiImpl",
                "public class DefaultExampleService implements ExampleService {}");

        Compilation compilation = Compiler.javac()
                .withProcessors(new SpiImplProcessor())
                .compile(spiType, implementation);

        assertThat(compilation).succeeded();
    }

    private String read(FileObject fileObject) throws IOException {
        return fileObject.getCharContent(false).toString();
    }
}
