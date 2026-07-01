package io.zhijun.spring.config.env.support;

import org.springframework.core.io.Resource;

import java.util.Comparator;

/**
 * Default {@link Resource} comparator based on filename.
 */
public class DefaultResourceComparator implements Comparator<Resource> {

    public static final DefaultResourceComparator INSTANCE = new DefaultResourceComparator();

    @Override
    public int compare(Resource left, Resource right) {
        String leftName = left != null ? left.getFilename() : null;
        String rightName = right != null ? right.getFilename() : null;
        if (leftName == null && rightName == null) {
            return 0;
        }
        if (leftName == null) {
            return -1;
        }
        if (rightName == null) {
            return 1;
        }
        return leftName.compareTo(rightName);
    }
}
