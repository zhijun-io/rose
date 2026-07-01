package io.zhijun.spring.config.context.annotation;

import io.zhijun.core.watch.FileChangedEvent;
import io.zhijun.core.watch.FileChangedListener;
import io.zhijun.core.watch.WatchService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.PathMatcher;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.zhijun.spring.core.io.ResourceUtils.isFileBasedResource;

/**
 * Loader for {@link ResourcePropertySource}.
 */
public class ResourcePropertySourceLoader extends PropertySourceExtensionLoader<ResourcePropertySource,
        PropertySourceExtensionAttributes<ResourcePropertySource>> implements InitializingBean, DisposableBean {

    private ResourcePatternResolver resourcePatternResolver;

    private PathMatcher pathMatcher;

    private WatchService watchService;

    @Override
    public void afterPropertiesSet() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getResourceLoader());
        this.resourcePatternResolver = resolver;
        this.pathMatcher = resolver.getPathMatcher();
    }

    private void ensureInitialized() {
        if (this.resourcePatternResolver == null || this.pathMatcher == null) {
            afterPropertiesSet();
        }
    }

    @Override
    protected Resource[] resolveResources(PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes,
                                          String propertySourceName, String resourceValue) throws Throwable {
        ensureInitialized();
        return this.resourcePatternResolver.getResources(resourceValue);
    }

    @Override
    public boolean isResourcePattern(String resourceValue) {
        ensureInitialized();
        return pathMatcher.isPattern(resourceValue);
    }

    @Override
    protected void configureResourcePropertySourcesRefresher(PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes,
                                                             List<PropertySourceResource> propertySourceResources,
                                                             CompositePropertySource propertySource,
                                                             ResourcePropertySourcesRefresher refresher) throws Throwable {
        ensureInitialized();
        this.watchService = WatchService.defaults();
        ListenerAdapter listenerAdapter = new ListenerAdapter(refresher);
        for (PropertySourceResource propertySourceResource : propertySourceResources) {
            Resource resource = propertySourceResource.getResource();
            if (isFileBasedResource(resource)) {
                File file = resource.getFile();
                listenerAdapter.register(file, propertySourceResource.getResourceValue());
                watchService.watch(file, listenerAdapter);
            }
        }
        watchService.start();
    }

    @Override
    public void destroy() throws Exception {
        if (watchService != null) {
            watchService.close();
        }
    }

    class ListenerAdapter implements FileChangedListener {

        private final ResourcePropertySourcesRefresher refresher;
        private final Map<File, String> fileToResourceValues = new LinkedHashMap<File, String>();
        private final Set<String> resourceValues = new HashSet<String>();

        ListenerAdapter(ResourcePropertySourcesRefresher refresher) {
            this.refresher = refresher;
        }

        void register(File file, String resourceValue) {
            fileToResourceValues.put(file.getAbsoluteFile(), resourceValue);
            resourceValues.add(resourceValue);
        }

        @Override
        public void onFileChanged(FileChangedEvent event) {
            File file = event.getFile().getAbsoluteFile();
            try {
                switch (event.getKind()) {
                    case CREATED:
                        onFileCreated(file);
                        break;
                    case MODIFIED:
                        onFileModified(file);
                        break;
                    case DELETED:
                        onFileDeleted(file);
                        break;
                    default:
                        break;
                }
            } catch (Throwable ex) {
                logger.warn("Failed to refresh property sources after file change: {}", file, ex);
            }
        }

        private void onFileCreated(File resourceFile) throws Throwable {
            for (String resourceValue : resourceValues) {
                if (!isResourcePattern(resourceValue)) {
                    continue;
                }
                Resource[] resources = resourcePatternResolver.getResources(resourceValue);
                Resource found = findResource(resourceFile, resources);
                if (found != null) {
                    refresher.refresh(resourceValue, found);
                    break;
                }
            }
        }

        private void onFileModified(File resourceFile) throws Throwable {
            String resourceValue = fileToResourceValues.get(resourceFile);
            if (resourceValue != null) {
                refresher.refresh(resourceValue, resourcePatternResolver.getResource(resourceFile.toURI().toString()));
            }
        }

        private void onFileDeleted(File resourceFile) throws Throwable {
            String resourceValue = fileToResourceValues.get(resourceFile);
            if (resourceValue != null) {
                refresher.refresh(resourceValue, resourcePatternResolver.getResource(resourceFile.toURI().toString()));
            }
        }

        private Resource findResource(File resourceFile, Resource[] resources) throws IOException {
            for (Resource resource : resources) {
                if (resourceFile.equals(resource.getFile().getAbsoluteFile())) {
                    return resource;
                }
            }
            return null;
        }
    }
}
