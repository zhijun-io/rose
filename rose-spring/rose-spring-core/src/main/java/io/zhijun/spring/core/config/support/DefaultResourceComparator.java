package io.zhijun.spring.core.config.support;

import java.util.Comparator;

import org.springframework.core.io.Resource;

/**
 * Default comparator for resources.
 */
public class DefaultResourceComparator implements Comparator<Resource> {

    @Override
    public int compare(Resource left, Resource right) {
        String leftValue = left == null ? "" : left.getDescription();
        String rightValue = right == null ? "" : right.getDescription();
        return leftValue.compareTo(rightValue);
    }
}
