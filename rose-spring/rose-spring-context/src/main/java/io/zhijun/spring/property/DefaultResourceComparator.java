package io.zhijun.spring.propertysource;

import java.util.Comparator;

import org.springframework.core.io.Resource;

public class DefaultResourceComparator implements Comparator<Resource> {

    private static final Comparator<Resource> INSTANCE =
            Comparator.nullsFirst(
                    Comparator.comparing(Resource::getDescription, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));

    @Override
    public int compare(Resource left, Resource right) {
        return INSTANCE.compare(left, right);
    }
}
