package io.zhijun.spring.core.propertysource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class DefaultResourceComparatorTests {

    private final DefaultResourceComparator comparator = new DefaultResourceComparator();

    @Test
    void compareOrdersByResourceDescription() {
        Resource left = new ByteArrayResource(new byte[0]) {
            @Override
            public String getDescription() {
                return "a-resource";
            }
        };
        Resource right = new ByteArrayResource(new byte[0]) {
            @Override
            public String getDescription() {
                return "b-resource";
            }
        };

        assertThat(comparator.compare(left, right)).isNegative();
        assertThat(comparator.compare(right, left)).isPositive();
        assertThat(comparator.compare(left, left)).isZero();
    }

    @Test
    void compareTreatsNullResourcesAsEmptyDescription() {
        Resource resource = new ByteArrayResource(new byte[0]) {
            @Override
            public String getDescription() {
                return "resource";
            }
        };

        assertThat(comparator.compare(null, resource)).isNegative();
        assertThat(comparator.compare(resource, null)).isPositive();
        assertThat(comparator.compare(null, null)).isZero();
    }
}
