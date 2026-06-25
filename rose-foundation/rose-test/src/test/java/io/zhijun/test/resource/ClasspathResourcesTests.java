package io.zhijun.test.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathResourcesTests {

    @Test
    void readsUtf8Resource() {
        assertThat(ClasspathResources.readUtf8("rose-test-fixture.txt")).isEqualTo("rose-test\n");
    }
}
