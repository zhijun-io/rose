package io.zhijun.core.annotation.processor;

import com.google.auto.service.AutoService;
import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Generates {@code META-INF/services} entries for {@link SpiImpl} implementations.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.zhijun.core.spi.annotation.SpiImpl")
public class SpiImplProcessor extends AbstractProcessor {

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private final Map<String, Set<String>> serviceImplementations = new LinkedHashMap<String, Set<String>>();

    private Elements elements;

    private Types types;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SpiImpl.class)) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeElement implementationType = (TypeElement) element;
            if (!isConcreteImplementation(implementationType)) {
                continue;
            }
            registerImplementation(implementationType);
        }
        if (roundEnv.processingOver()) {
            writeServiceFiles();
        }
        return false;
    }

    private boolean isConcreteImplementation(TypeElement implementationType) {
        if (implementationType.getKind() != ElementKind.CLASS) {
            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "@SpiImpl can only be applied to classes",
                            implementationType);
            return false;
        }
        Set<Modifier> modifiers = implementationType.getModifiers();
        if (modifiers.contains(Modifier.ABSTRACT)) {
            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "@SpiImpl does not support abstract classes",
                            implementationType);
            return false;
        }
        Element enclosingElement = implementationType.getEnclosingElement();
        if (enclosingElement != null
                && enclosingElement.getKind() != ElementKind.PACKAGE
                && !modifiers.contains(Modifier.STATIC)) {
            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "@SpiImpl nested classes must be static",
                            implementationType);
            return false;
        }
        if (!hasNoArgsConstructor(implementationType)) {
            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "@SpiImpl class must declare an accessible no-args constructor",
                            implementationType);
            return false;
        }
        return true;
    }

    private boolean hasNoArgsConstructor(TypeElement implementationType) {
        boolean hasDeclaredConstructor = false;
        for (Element enclosedElement : implementationType.getEnclosedElements()) {
            if (enclosedElement.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            hasDeclaredConstructor = true;
            ExecutableElement constructor = (ExecutableElement) enclosedElement;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (constructor.getParameters().isEmpty() && !modifiers.contains(Modifier.PRIVATE)) {
                return true;
            }
        }
        return !hasDeclaredConstructor;
    }

    private void registerImplementation(TypeElement implementationType) {
        List<TypeElement> spiTypes = findSpiTypes(implementationType);
        if (spiTypes.isEmpty()) {
            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "@SpiImpl class must implement at least one interface or superclass annotated with @Spi",
                            implementationType);
            return;
        }
        String implementationClassName = elements.getBinaryName(implementationType).toString();
        for (TypeElement spiType : spiTypes) {
            String spiClassName = elements.getBinaryName(spiType).toString();
            Set<String> implementations = serviceImplementations.get(spiClassName);
            if (implementations == null) {
                implementations = new LinkedHashSet<String>();
                serviceImplementations.put(spiClassName, implementations);
            }
            implementations.add(implementationClassName);
        }
    }

    private List<TypeElement> findSpiTypes(TypeElement implementationType) {
        Set<TypeElement> spiTypes = new LinkedHashSet<TypeElement>();
        Set<String> visited = new LinkedHashSet<String>();
        Deque<TypeMirror> queue = new ArrayDeque<TypeMirror>();
        queue.add(implementationType.asType());
        while (!queue.isEmpty()) {
            TypeMirror current = queue.removeFirst();
            if (current.getKind() != TypeKind.DECLARED) {
                continue;
            }
            Element currentElement = types.asElement(current);
            if (!(currentElement instanceof TypeElement)) {
                continue;
            }
            TypeElement currentType = (TypeElement) currentElement;
            String key = elements.getBinaryName(currentType).toString();
            if (!visited.add(key)) {
                continue;
            }
            if (!currentType.equals(implementationType) && currentType.getAnnotation(Spi.class) != null) {
                spiTypes.add(currentType);
            }
            for (TypeMirror interfaceType : currentType.getInterfaces()) {
                queue.addLast(interfaceType);
            }
            TypeMirror superclass = currentType.getSuperclass();
            if (superclass != null && superclass.getKind() != TypeKind.NONE) {
                queue.addLast(superclass);
            }
        }
        return new ArrayList<TypeElement>(spiTypes);
    }

    private void writeServiceFiles() {
        for (Map.Entry<String, Set<String>> entry : serviceImplementations.entrySet()) {
            try {
                writeServiceFile(entry.getKey(), entry.getValue());
            }
            catch (IOException ex) {
                processingEnv.getMessager()
                        .printMessage(
                                Diagnostic.Kind.ERROR,
                                "Failed to generate SPI descriptor for " + entry.getKey() + ": " + ex.getMessage());
            }
        }
    }

    private void writeServiceFile(String spiClassName, Set<String> implementations) throws IOException {
        FileObject resource = filer.createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                SERVICES_DIRECTORY + spiClassName);
        List<String> sortedImplementations = new ArrayList<String>(implementations);
        Collections.sort(sortedImplementations);
        Writer writer = resource.openWriter();
        try {
            for (String implementation : sortedImplementations) {
                writer.write(implementation);
                writer.write('\n');
            }
        }
        finally {
            writer.close();
        }
    }
}
