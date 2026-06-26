package io.zhijun.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;

import io.zhijun.annotation.Since;

/**
 * Validates {@link Since} metadata at compile time.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.zhijun.annotation.Since")
public class SinceProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Since.class)) {
            Since since = element.getAnnotation(Since.class);
            if (since == null) {
                continue;
            }
            if (since.value() == null || since.value().trim().isEmpty()) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@Since.value() must not be blank",
                        element);
            }
        }
        return false;
    }
}
