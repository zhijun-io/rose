package io.zhijun.annotation.processor;

import java.io.IOException;
import java.util.Optional;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.junit.jupiter.api.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class RosePropertyMetadataProcessorTests {

  @Test
  void generatesAdditionalSpringConfigurationMetadata() throws IOException {
    Compilation compilation = Compiler.javac()
        .withProcessors(new RosePropertyMetadataProcessor())
        .compile(JavaFileObjects.forSourceString(
            "io.zhijun.demo.DemoHints",
            ""
                + "package io.zhijun.demo;\n"
                + "import io.zhijun.annotation.RosePropertyHint;\n"
                + "@RosePropertyHint(\n"
                + "    name = \"rose.demo.enabled\",\n"
                + "    type = \"java.lang.Boolean\",\n"
                + "    description = \"Enable demo\",\n"
                + "    defaultValue = \"false\"\n"
                + ")\n"
                + "class DemoHints {}\n"));

    assertThat(compilation).succeeded();

    Optional<JavaFileObject> metadata = findGeneratedMetadata(compilation);
    org.assertj.core.api.Assertions.assertThat(metadata).isPresent();
  }

  private static Optional<JavaFileObject> findGeneratedMetadata(Compilation compilation) throws IOException {
    for (JavaFileObject file : compilation.generatedFiles()) {
      if (file.getName().contains("additional-spring-configuration-metadata.json")) {
        String content = file.getCharContent(true).toString();
        org.assertj.core.api.Assertions.assertThat(content)
            .contains("\"name\": \"rose.demo.enabled\"")
            .contains("\"defaultValue\": \"false\"");
        return Optional.of(file);
      }
    }
    return Optional.empty();
  }
}
