package io.zhijun.spring.core.io;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

/**
 * Resource utilities.
 */
public abstract class ResourceUtils {

    public static boolean isFileUrlResource(Resource resource) {
        return resource instanceof FileUrlResource;
    }

    public static boolean isFileBasedResource(Resource resource) {
        return resource instanceof FileSystemResource || isFileUrlResource(resource);
    }

    private ResourceUtils() {
    }
}
