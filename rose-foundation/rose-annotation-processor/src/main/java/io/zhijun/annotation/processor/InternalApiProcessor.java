package io.zhijun.annotation.processor;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;

import org.apiguardian.api.API;

import io.zhijun.annotation.Internal;

/**
 * Warns when {@link Internal} types or {@code *.internal.*} package types leak into public project API surface.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
public class InternalApiProcessor extends AbstractProcessor {

    private Elements elements;
    private Types types;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> internalTypes = new HashSet<TypeElement>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Internal.class)) {
            if (element instanceof TypeElement) {
                internalTypes.add((TypeElement) element);
            }
        }

        for (Element root : roundEnv.getRootElements()) {
            if (root.getKind() != ElementKind.CLASS && root.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            TypeElement type = (TypeElement) root;
            if (!isProjectType(type) || isInternal(type) || !isPublic(type)) {
                continue;
            }
            checkTypeSurface(type, internalTypes);
        }
        return false;
    }

    private void checkTypeSurface(TypeElement type, Set<TypeElement> internalTypes) {
        checkTypeReference(type, type.asType(), internalTypes);
        for (TypeMirror iface : type.getInterfaces()) {
            checkTypeReference(type, iface, internalTypes);
        }
        TypeMirror superclass = type.getSuperclass();
        if (superclass.getKind() != TypeKind.NONE) {
            checkTypeReference(type, superclass, internalTypes);
        }

        for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
            if (!isPublicApi(method)) {
                continue;
            }
            checkTypeReference(method, method.getReturnType(), internalTypes);
            for (VariableElement parameter : method.getParameters()) {
                checkTypeReference(method, parameter.asType(), internalTypes);
            }
        }

        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (!isPublicApi(field)) {
                continue;
            }
            checkTypeReference(field, field.asType(), internalTypes);
        }
    }

    private void checkTypeReference(Element context, TypeMirror mirror, Set<TypeElement> internalTypes) {
        if (mirror == null || mirror.getKind() == TypeKind.NONE) {
            return;
        }
        TypeElement referenced = asTypeElement(mirror);
        if (referenced == null) {
            return;
        }
        if (containsInternalType(referenced, internalTypes)) {
            processingEnv
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.WARNING,
                            "Public API must not expose internal type: " + referenced.getQualifiedName(),
                            context);
        }
    }

    private boolean containsInternalType(TypeElement type, Set<TypeElement> internalTypes) {
        if (internalTypes.contains(type) || isInternal(type) || isInInternalPackage(type)) {
            return true;
        }
        for (TypeMirror iface : type.getInterfaces()) {
            TypeElement ifaceElement = asTypeElement(iface);
            if (ifaceElement != null && containsInternalType(ifaceElement, internalTypes)) {
                return true;
            }
        }
        TypeMirror superclass = type.getSuperclass();
        if (superclass != null && superclass.getKind() != TypeKind.NONE) {
            TypeElement superElement = asTypeElement(superclass);
            return superElement != null && containsInternalType(superElement, internalTypes);
        }
        return false;
    }

    private TypeElement asTypeElement(TypeMirror mirror) {
        if (mirror.getKind() != TypeKind.DECLARED) {
            return null;
        }
        Element element = types.asElement(mirror);
        if (element instanceof TypeElement) {
            return (TypeElement) element;
        }
        return null;
    }

    private boolean isProjectType(TypeElement type) {
        String packageName = elements.getPackageOf(type).getQualifiedName().toString();
        return packageName.startsWith("io.zhijun");
    }

    private boolean isInInternalPackage(TypeElement type) {
        return isInternalPackageName(
                elements.getPackageOf(type).getQualifiedName().toString());
    }

    private static boolean isInternalPackageName(String packageName) {
        for (String segment : packageName.split("\\.")) {
            if ("internal".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInternal(Element element) {
        if (element.getAnnotation(Internal.class) != null) {
            return true;
        }
        if (isInternalApi(element)) {
            return true;
        }
        if (element instanceof TypeElement && isInInternalPackage((TypeElement) element)) {
            return true;
        }
        return element.getEnclosingElement() != null
                && (element.getEnclosingElement().getAnnotation(Internal.class) != null
                        || isInternalApi(element.getEnclosingElement()));
    }

    private boolean isInternalApi(Element element) {
        API api = element.getAnnotation(API.class);
        return api != null && api.status() == API.Status.INTERNAL;
    }

    private boolean isPublic(Element element) {
        return element.getModifiers().contains(Modifier.PUBLIC);
    }

    private boolean isPublicApi(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        return modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED);
    }
}
