package io.zhijun.spring.config.property;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultResourceComparator} 单元测试。
 */
class DefaultResourceComparatorTests {

    private final DefaultResourceComparator comparator = new DefaultResourceComparator();

    @Test
    void shouldCompareByDescription() {
        Resource r1 = describe("z.yaml");
        Resource r2 = describe("a.yaml");
        assertThat(comparator.compare(r1, r2)).isPositive();
        assertThat(comparator.compare(r2, r1)).isNegative();
    }

    @Test
    void shouldBeEqualForSameDescription() {
        Resource r1 = describe("same.yaml");
        Resource r2 = describe("same.yaml");
        assertThat(comparator.compare(r1, r2)).isZero();
    }

    @Test
    void shouldHandleNullFirst() {
        assertThat(comparator.compare(null, new ByteArrayResource(new byte[0]))).isNegative();
        assertThat(comparator.compare(new ByteArrayResource(new byte[0]), null)).isPositive();
    }

    @Test
    void shouldHandleBothNull() {
        assertThat(comparator.compare(null, null)).isZero();
    }

    @Test
    void shouldBeCaseInsensitive() {
        Resource r1 = describe("ABC.yaml");
        Resource r2 = describe("abc.yaml");
        assertThat(comparator.compare(r1, r2)).isZero();
    }

    private static Resource describe(String description) {
        return new ByteArrayResource(new byte[0]) {
            @Override
            public String getDescription() {
                return description;
            }
        };
    }
}
