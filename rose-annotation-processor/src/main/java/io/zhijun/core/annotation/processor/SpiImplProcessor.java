package io.zhijun.core.annotation.processor;

import com.google.auto.service.AutoService;
import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * SPI 注解处理器：
 * <ul>
 *   <li>编译期校验 SPI 实现类合法（非抽象、静态内部类、无参构造）</li>
 *   <li>自动生成 {@code META-INF/services/} 服务描述文件</li>
 * </ul>
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.zhijun.core.spi.annotation.SpiImpl")
public class SpiImplProcessor extends AbstractProcessor {

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private Elements elements;
    private Types types;
    private Filer filer;
    private Messager messager;

    // SPI 接口 -> 实现类全限定名集合
    private final Map<String, Set<String>> serviceEntries = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SpiImpl.class)) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeElement implType = (TypeElement) element;

            if (!isConcreteImplementation(implType)) {
                continue;
            }

            List<TypeElement> spiTypes = findSpiTypes(implType);
            if (spiTypes.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@SpiImpl must implement at least one interface or superclass annotated with @Spi",
                        implType);
                continue;
            }

            String implClassName = elements.getBinaryName(implType).toString();
            for (TypeElement spiType : spiTypes) {
                String spiClassName = elements.getBinaryName(spiType).toString();
                serviceEntries.computeIfAbsent(spiClassName, k -> new LinkedHashSet<>()).add(implClassName);
            }
        }

        if (roundEnv.processingOver() && !serviceEntries.isEmpty()) {
            try {
                writeServiceFiles();
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate SPI service files: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean isConcreteImplementation(TypeElement implType) {
        if (implType.getKind() != ElementKind.CLASS) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl can only be applied to classes", implType);
            return false;
        }
        if (implType.getModifiers().contains(Modifier.ABSTRACT)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl does not support abstract classes", implType);
            return false;
        }

        Element enclosing = implType.getEnclosingElement();
        if (enclosing != null
                && enclosing.getKind() != ElementKind.PACKAGE
                && !implType.getModifiers().contains(Modifier.STATIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl nested classes must be static", implType);
            return false;
        }

        if (!hasNoArgsConstructor(implType)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl class must declare an accessible no-args constructor", implType);
            return false;
        }
        return true;
    }

    private boolean hasNoArgsConstructor(TypeElement implType) {
        boolean hasDeclaredConstructor = false;
        for (Element e : implType.getEnclosedElements()) {
            if (e.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            hasDeclaredConstructor = true;
            ExecutableElement constructor = (ExecutableElement) e;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (constructor.getParameters().isEmpty() && !modifiers.contains(Modifier.PRIVATE)) {
                return true;
            }
        }
        return !hasDeclaredConstructor;
    }

    private List<TypeElement> findSpiTypes(TypeElement implType) {
        Set<TypeElement> spiTypes = new LinkedHashSet<>();
        Set<String> visited = new LinkedHashSet<>();
        Deque<TypeMirror> queue = new ArrayDeque<>();
        queue.add(implType.asType());

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
            if (!currentType.equals(implType) && currentType.getAnnotation(Spi.class) != null) {
                spiTypes.add(currentType);
            }
            for (TypeMirror iface : currentType.getInterfaces()) {
                queue.addLast(iface);
            }
            TypeMirror superclass = currentType.getSuperclass();
            if (superclass != null && superclass.getKind() != TypeKind.NONE) {
                queue.addLast(superclass);
            }
        }
        return new ArrayList<>(spiTypes);
    }

    private void writeServiceFiles() throws IOException {
        for (Map.Entry<String, Set<String>> entry : serviceEntries.entrySet()) {
            FileObject resource = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    SERVICES_DIRECTORY + entry.getKey());
            try (Writer writer = resource.openWriter()) {
                for (String impl : entry.getValue()) {
                    writer.write(impl);
                    writer.write('\n');
                }
            }
        }
    }
}
