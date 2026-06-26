package io.zhijun.annotation.processor;

import org.junit.jupiter.api.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import static com.google.testing.compile.CompilationSubject.assertThat;

class InternalApiProcessorTests {

  @Test
  void warnsWhenPublicApiExposesInternalType() {
    Compilation compilation = Compiler.javac()
        .withProcessors(new InternalApiProcessor())
        .compile(
            JavaFileObjects.forSourceString(
                "io.zhijun.demo.InternalService",
                ""
                    + "package io.zhijun.demo;\n"
                    + "import io.zhijun.annotation.Internal;\n"
                    + "@Internal\n"
                    + "public class InternalService {}\n"),
            JavaFileObjects.forSourceString(
                "io.zhijun.demo.PublicApi",
                ""
                    + "package io.zhijun.demo;\n"
                    + "public class PublicApi {\n"
                    + "  public InternalService service() { return null; }\n"
                    + "}\n"));

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningCount(1);
    assertThat(compilation).hadWarningContaining("InternalService");
  }
}
