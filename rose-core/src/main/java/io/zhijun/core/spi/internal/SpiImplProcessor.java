package io.zhijun.core.spi.internal;

import com.google.auto.service.AutoService;
import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compile-time processor for {@link SpiImpl @SpiImpl}.
 *
 * <p>Scans all {@code @SpiImpl}-annotated concrete classes, discovers the
 * {@link Spi @Spi}-annotated interfaces they implement (by walking the full
 * type hierarchy), and generates {@code META-INF/services/} descriptor files
 * for each SPI interface.
 *
 * <p>This eliminates the need to manually maintain service descriptor files.
 * Generated files follow the standard {@link java.util.ServiceLoader} format.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.zhijun.core.spi.annotation.SpiImpl")
public final class SpiImplProcessor extends AbstractProcessor {

    private final Map<String, Set<String>> implementations = new LinkedHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateServiceFiles();
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(SpiImpl.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "@SpiImpl is only supported on classes, skipping " + element, element);
                continue;
            }
            TypeElement implElement = (TypeElement) element;
            if (implElement.getModifiers().contains(Modifier.ABSTRACT)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "@SpiImpl on abstract class " + implElement.getQualifiedName()
                                + " is ignored; only concrete classes are supported", element);
                continue;
            }
            String implName = implElement.getQualifiedName().toString();
            Set<TypeElement> spiInterfaces = new LinkedHashSet<>();
            collectSpiInterfaces(implElement, spiInterfaces);

            if (spiInterfaces.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "@SpiImpl class " + implName
                                + " does not implement any @Spi-annotated interface", element);
                continue;
            }

            for (TypeElement iface : spiInterfaces) {
                String ifaceName = iface.getQualifiedName().toString();
                implementations.computeIfAbsent(ifaceName, k -> new LinkedHashSet<>()).add(implName);
            }
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Walk the full type hierarchy (superclasses and interfaces) and collect
     * all interfaces annotated with {@link Spi @Spi}.
     */
    private static void collectSpiInterfaces(TypeElement typeElement, Set<TypeElement> result) {
        if (typeElement == null) {
            return;
        }
        for (TypeMirror ifaceMirror : typeElement.getInterfaces()) {
            TypeElement ifaceElement = asTypeElement(ifaceMirror);
            if (ifaceElement == null) {
                continue;
            }
            if (ifaceElement.getAnnotation(Spi.class) != null) {
                result.add(ifaceElement);
            }
            collectSpiInterfaces(ifaceElement, result);
        }
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            TypeElement superElement = asTypeElement(superclass);
            if (superElement != null && !isJavaLangObject(superElement)) {
                collectSpiInterfaces(superElement, result);
            }
        }
    }

    private static TypeElement asTypeElement(TypeMirror mirror) {
        if (mirror instanceof DeclaredType) {
            Element element = ((DeclaredType) mirror).asElement();
            if (element instanceof TypeElement) {
                return (TypeElement) element;
            }
        }
        return null;
    }

    private static boolean isJavaLangObject(TypeElement element) {
        return "java.lang.Object".equals(element.getQualifiedName().toString());
    }

    private void generateServiceFiles() {
        if (implementations.isEmpty()) {
            return;
        }

        Filer filer = processingEnv.getFiler();
        for (Map.Entry<String, Set<String>> entry : implementations.entrySet()) {
            String interfaceName = entry.getKey();
            Set<String> implNames = entry.getValue();
            String resourceName = "META-INF/services/" + interfaceName;
            try {
                FileObject file = filer.createResource(
                        StandardLocation.CLASS_OUTPUT, "", resourceName);
                try (Writer writer = file.openWriter()) {
                    for (String impl : implNames) {
                        writer.write(impl);
                        writer.write('\n');
                    }
                }
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate " + resourceName + ": " + e.getMessage());
            }
        }
    }
}
