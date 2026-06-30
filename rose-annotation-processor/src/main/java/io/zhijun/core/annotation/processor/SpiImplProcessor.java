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
import java.util.Map.Entry;
import java.util.stream.Collectors;
/**
 * Enhanced SPI processor:
 * <ul>
 *     <li>Auto generates META-INF/services configuration files</li>
 *     <li>Compile time validation for SPI implementations</li>
 *     <li>Generates spi-metadata.json for faster runtime loading</li>
 *     <li>Alias duplicate check</li>
 * </ul>
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.zhijun.core.spi.annotation.SpiImpl")
public class SpiImplProcessor extends AbstractProcessor {
    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String METADATA_PATH = "META-INF/rose/spi-metadata.json";
    private Elements elements;
    private Types types;
    private Filer filer;
    private Messager messager;
    // SPI接口 -> 实现元数据列表
    private final Map<String, List<SpiImplMeta>> spiMetadata = new LinkedHashMap<>();
    // 别名检查：SPI接口+别名 -> 实现类名，检查重复
    private final Map<String, Map<String, String>> aliasCheckMap = new LinkedHashMap<>();
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
            TypeElement implementationType = (TypeElement) element;
            // 1. 基础合法性检查
            if (!isConcreteImplementation(implementationType)) {
                continue;
            }
            // 2. 查找实现的SPI接口
            List<TypeElement> spiTypes = findSpiTypes(implementationType);
            if (spiTypes.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@SpiImpl must implement at least one interface or superclass annotated with @Spi",
                        implementationType);
                continue;
            }
            // 3. 获取注解配置
            SpiImpl spiImpl = implementationType.getAnnotation(SpiImpl.class);
            // 4. 检查注解配置合法性
            if (!validateSpiImplConfig(implementationType, spiImpl)) {
                continue;
            }
            // 5. 注册实现
            String implementationClassName = elements.getBinaryName(implementationType).toString();
            String alias = getAlias(implementationType, spiImpl);
            for (TypeElement spiType : spiTypes) {
                String spiClassName = elements.getBinaryName(spiType).toString();
                // 检查别名重复
                if (!checkAliasDuplicate(spiClassName, alias, implementationClassName, implementationType)) {
                    continue;
                }
                // 注册元数据
                SpiImplMeta meta = new SpiImplMeta();
                meta.setClassName(implementationClassName);
                meta.setAlias(alias);
                meta.setPriority(spiImpl.priority());
                meta.setDescription(spiImpl.description());
                meta.setTags(Arrays.asList(spiImpl.tags()));
                spiMetadata.computeIfAbsent(spiClassName, k -> new ArrayList<>()).add(meta);
            }
        }
        // 所有处理完成后，生成配置文件和元数据
        if (roundEnv.processingOver() && !spiMetadata.isEmpty()) {
            try {
                writeServiceFiles();
                writeMetadataFile();
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate SPI files: " + e.getMessage());
            }
        }
        return false;
    }
    /**
     * 检查实现类是否是合法的SPI实现
     */
    private boolean isConcreteImplementation(TypeElement implementationType) {
        // 必须是类
        if (implementationType.getKind() != ElementKind.CLASS) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl can only be applied to classes", implementationType);
            return false;
        }
        // 不能是抽象类
        if (implementationType.getModifiers().contains(Modifier.ABSTRACT)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl does not support abstract classes", implementationType);
            return false;
        }
        // 内部类必须是静态的
        Element enclosingElement = implementationType.getEnclosingElement();
        if (enclosingElement != null
                && enclosingElement.getKind() != ElementKind.PACKAGE
                && !implementationType.getModifiers().contains(Modifier.STATIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl nested classes must be static", implementationType);
            return false;
        }
        // 必须有公共无参构造函数
        if (!hasNoArgsConstructor(implementationType)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@SpiImpl class must declare an accessible no-args constructor", implementationType);
            return false;
        }
        return true;
    }
    /**
     * 检查是否有公共无参构造函数
     */
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
        // 没有显式构造函数，默认有无参构造
        return !hasDeclaredConstructor;
    }
    /**
     * 查找实现类所有的@Spi接口
     */
    private List<TypeElement> findSpiTypes(TypeElement implementationType) {
        Set<TypeElement> spiTypes = new LinkedHashSet<>();
        Set<String> visited = new LinkedHashSet<>();
        Deque<TypeMirror> queue = new ArrayDeque<>();
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
            // 找到带@Spi注解的接口/类
            if (!currentType.equals(implementationType) && currentType.getAnnotation(Spi.class) != null) {
                spiTypes.add(currentType);
            }
            // 遍历父接口
            for (TypeMirror interfaceType : currentType.getInterfaces()) {
                queue.addLast(interfaceType);
            }
            // 遍历父类
            TypeMirror superclass = currentType.getSuperclass();
            if (superclass != null && superclass.getKind() != TypeKind.NONE) {
                queue.addLast(superclass);
            }
        }
        return new ArrayList<>(spiTypes);
    }
    /**
     * 验证@SpiImpl配置是否合法
     */
    private boolean validateSpiImplConfig(TypeElement implementationType, SpiImpl spiImpl) {
        // 优先级不能为负
        if (spiImpl.priority() < 0) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "SPI priority cannot be negative, current: " + spiImpl.priority(), implementationType);
            return false;
        }
        // 别名不能包含特殊字符
        String alias = spiImpl.value().trim();
        if (!alias.isEmpty() && !alias.matches("^[a-zA-Z0-9_-]+$")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "SPI alias only supports letters, numbers, underscores and hyphens, current: " + alias, implementationType);
            return false;
        }
        return true;
    }
    /**
     * 检查别名是否重复
     */
    private boolean checkAliasDuplicate(String spiClassName, String alias, String implementationClassName, TypeElement element) {
        if (alias.isEmpty()) {
            return true;
        }
        Map<String, String> aliasMap = aliasCheckMap.computeIfAbsent(spiClassName, k -> new LinkedHashMap<>());
        if (aliasMap.containsKey(alias)) {
            String existingImpl = aliasMap.get(alias);
            messager.printMessage(Diagnostic.Kind.WARNING,
                    String.format("SPI alias '%s' is already used by %s, current implementation %s may be overridden",
                            alias, existingImpl, implementationClassName), element);
        } else {
            aliasMap.put(alias, implementationClassName);
        }
        return true;
    }
    /**
     * 获取别名，配置了就用配置的，否则返回类名首字母小写
     */
    private String getAlias(TypeElement implementationType, SpiImpl spiImpl) {
        String alias = spiImpl.value().trim();
        if (!alias.isEmpty()) {
            return alias;
        }
        // 类名首字母小写
        String simpleName = implementationType.getSimpleName().toString();
        if (simpleName.isEmpty()) {
            return "";
        }
        char firstChar = simpleName.charAt(0);
        return Character.toLowerCase(firstChar) + simpleName.substring(1);
    }
    /**
     * 生成META-INF/services配置文件
     */
    private void writeServiceFiles() throws IOException {
        for (Entry<String, List<SpiImplMeta>> entry : spiMetadata.entrySet()) {
            String spiClassName = entry.getKey();
            Set<String> implementations = entry.getValue().stream()
                    .map(SpiImplMeta::getClassName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            FileObject resource = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    SERVICES_DIRECTORY + spiClassName);
            try (Writer writer = resource.openWriter()) {
                for (String impl : implementations) {
                    writer.write(impl);
                    writer.write('\n');
                }
            }
        }
    }
    /**
     * 生成SPI元数据文件
     */
    private void writeMetadataFile() throws IOException {
        if (spiMetadata.isEmpty()) {
            return;
        }
        FileObject resource = filer.createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                METADATA_PATH);
        try (Writer writer = resource.openWriter()) {
            // 手动拼接JSON，不需要依赖JSON库，保持处理器轻量化
            writer.write("{\n");
            int spiIndex = 0;
            for (Entry<String, List<SpiImplMeta>> entry : spiMetadata.entrySet()) {
                String spiClassName = entry.getKey();
                List<SpiImplMeta> impls = entry.getValue();
                // 按优先级排序
                impls.sort(Comparator.comparingInt(SpiImplMeta::getPriority));
                writer.write(String.format("  \"%s\": [\n", escapeJson(spiClassName)));
                int implIndex = 0;
                for (SpiImplMeta meta : impls) {
                    writer.write("    {\n");
                    writer.write(String.format("      \"className\": \"%s\",\n", escapeJson(meta.getClassName())));
                    if (!meta.getAlias().isEmpty()) {
                        writer.write(String.format("      \"alias\": \"%s\",\n", escapeJson(meta.getAlias())));
                    }
                    writer.write(String.format("      \"priority\": %d", meta.getPriority()));
                    if (!meta.getDescription().isEmpty()) {
                        writer.write(",\n");
                        writer.write(String.format("      \"description\": \"%s\"", escapeJson(meta.getDescription())));
                    }
                    if (!meta.getTags().isEmpty()) {
                        writer.write(",\n");
                        writer.write("      \"tags\": [");
                        for (int i = 0; i < meta.getTags().size(); i++) {
                            if (i > 0) writer.write(", ");
                            writer.write("\"" + escapeJson(meta.getTags().get(i)) + "\"");
                        }
                        writer.write("]");
                    }
                    writer.write("\n    }");
                    if (implIndex < impls.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                    implIndex++;
                }
                writer.write("  ]");
                if (spiIndex < spiMetadata.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
                spiIndex++;
            }
            writer.write("}");
        }
    }
    /**
     * JSON字符串转义
     */
    private String escapeJson(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '/':
                    builder.append("\\/");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c <= '') {
                        builder.append(String.format("\\u%04X", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }
    /**
     * SPI实现元数据
     */
    private static class SpiImplMeta {
        private String className;
        private String alias;
        private int priority;
        private String description = "";
        private List<String> tags = Collections.emptyList();
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }
}
