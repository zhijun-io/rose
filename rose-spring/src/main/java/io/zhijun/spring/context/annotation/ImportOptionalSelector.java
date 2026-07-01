package io.zhijun.spring.context.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link ImportOptional} 的实现。加载不存在的类时静默跳过。
 */
class ImportOptionalSelector implements ImportSelector, BeanClassLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(ImportOptionalSelector.class);

    private static final String[] NONE = new String[0];

    private ClassLoader classLoader;

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ImportOptional.class.getName());
        if (attrs == null) {
            return NONE;
        }
        String[] classNames = (String[]) attrs.get("value");
        if (classNames == null || classNames.length == 0) {
            return NONE;
        }
        List<String> imports = new ArrayList<String>(classNames.length);
        for (String name : classNames) {
            if (isPresent(name)) {
                imports.add(name);
            }
        }
        return imports.toArray(new String[0]);
    }

    private boolean isPresent(String className) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            logger.debug("Optional import [{}] not found on classpath, skipped", className);
            return false;
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
