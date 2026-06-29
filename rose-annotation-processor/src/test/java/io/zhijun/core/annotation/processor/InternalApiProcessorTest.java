package io.zhijun.core.annotation.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static org.assertj.core.api.Assertions.assertThat;

class InternalApiProcessorTest {

    @Test
    void warnsWhenPublicApiExposesInternalAnnotatedType() {
        JavaFileObject internalType =
                JavaFileObjects.forSourceString(
                        "io.zhijun.sample.InternalType",
                        "package io.zhijun.sample;\n"
                                + "\n"
                                + "import io.zhijun.core.annotation.Internal;\n"
                                + "\n"
                                + "@Internal\n"
                                + "public class InternalType {}\n");
        JavaFileObject publicApi =
                JavaFileObjects.forSourceString(
                        "io.zhijun.sample.PublicApi",
                        "package io.zhijun.sample;\n"
                                + "\n"
                                + "public class PublicApi {\n"
                                + "    public InternalType internalType() {\n"
                                + "        return new InternalType();\n"
                                + "    }\n"
                                + "}\n");

        Compilation compilation =
                Compiler.javac()
                        .withProcessors(new InternalApiProcessor())
                        .compile(internalType, publicApi);

        assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);
        assertThat(compilation.warnings())
                .anySatisfy(
                        warning ->
                                assertThat(warning.getMessage(null))
                                        .contains("Public API must not expose internal type"));
    }
}
