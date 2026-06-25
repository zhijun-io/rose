package io.zhijun.annotation.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;

import io.zhijun.annotation.RosePropertyHint;
import io.zhijun.annotation.RosePropertyHints;
import io.zhijun.annotation.processor.support.SpringConfigurationMetadataWriter;

/**
 * Emits {@code META-INF/additional-spring-configuration-metadata.json} from {@link RosePropertyHint}.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.zhijun.annotation.RosePropertyHint",
        "io.zhijun.annotation.RosePropertyHints"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RosePropertyMetadataProcessor extends AbstractProcessor {

    static final String METADATA_PATH = "META-INF/additional-spring-configuration-metadata.json";

    private final List<RosePropertyHint> hints = new ArrayList<RosePropertyHint>();
    private final Set<String> hintNames = new HashSet<String>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        collectHints(roundEnv.getElementsAnnotatedWith(RosePropertyHint.class));
        collectHints(roundEnv.getElementsAnnotatedWith(RosePropertyHints.class));

        if (!roundEnv.processingOver() || hints.isEmpty()) {
            return false;
        }

        writeMetadata();
        return false;
    }

    private void collectHints(Set<? extends Element> elements) {
        for (Element element : elements) {
            RosePropertyHint single = element.getAnnotation(RosePropertyHint.class);
            if (single != null) {
                addHint(single, element);
            }
            RosePropertyHints repeated = element.getAnnotation(RosePropertyHints.class);
            if (repeated != null) {
                for (RosePropertyHint hint : repeated.value()) {
                    addHint(hint, element);
                }
            }
        }
    }

    private void addHint(RosePropertyHint hint, Element element) {
        if (hint.name() == null || hint.name().trim().isEmpty()) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "@RosePropertyHint.name() must not be blank",
                    element);
            return;
        }
        if (!hint.name().startsWith("rose.")) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "@RosePropertyHint.name() must start with 'rose.'",
                    element);
            return;
        }
        if (!hintNames.add(hint.name())) {
            return;
        }
        hints.add(hint);
    }

    private void writeMetadata() {
        Filer filer = processingEnv.getFiler();
        try {
            FileObject resource = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    METADATA_PATH);
            Writer writer = resource.openWriter();
            try {
                writer.write(SpringConfigurationMetadataWriter.toJson(hints));
            } finally {
                writer.close();
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to write " + METADATA_PATH + ": " + ex.getMessage());
        }
    }
}
