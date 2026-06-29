package io.zhijun.core.annotation.processor;

import static org.assertj.core.api.Assertions.assertThat;

import javax.tools.JavaFileObject;

import org.junit.jupiter.api.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

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

    @Test
    void warnsWhenPublicApiExposesApiGuardianInternalType() {
        JavaFileObject internalType =
                JavaFileObjects.forSourceString(
                        "io.zhijun.sample.ApiGuardianInternalType",
                        "package io.zhijun.sample;\n"
                                + "\n"
                                + "\n"
                                + "\n"
                                + "@API(status = API.Status.INTERNAL)\n"
                                + "public class ApiGuardianInternalType {}\n");
        JavaFileObject publicApi =
                JavaFileObjects.forSourceString(
                        "io.zhijun.sample.ApiGuardianPublicApi",
                        "package io.zhijun.sample;\n"
                                + "\n"
                                + "public class ApiGuardianPublicApi {\n"
                                + "    public ApiGuardianInternalType internalType() {\n"
                                + "        return new ApiGuardianInternalType();\n"
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
