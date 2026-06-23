package io.zhijun.multitenancy.web.filter;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link TenantContextIgnorePathMatcher}.
 */
class TenantContextIgnorePathMatcherTests {

    @Test
    void whenNullPathsThenThrow() {
        assertThatThrownBy(() -> new TenantContextIgnorePathMatcher(null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ignorePathPatterns cannot be null");
    }

    @Test
    void matchAgainstFullPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/prometheus");
        TenantContextIgnorePathMatcher matcher = new TenantContextIgnorePathMatcher(Collections.singleton("/actuator/prometheus"));
        assertThat(matcher.matches(request)).isTrue();
    }

    @Test
    void matchAgainstFullPathWithoutTrailingSlash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/prometheus");
        TenantContextIgnorePathMatcher matcher = new TenantContextIgnorePathMatcher(Collections.singleton("actuator/prometheus"));
        assertThat(matcher.matches(request)).isTrue();
    }

    @Test
    void matchAgainstTemplatePath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/prometheus");
        TenantContextIgnorePathMatcher matcher = new TenantContextIgnorePathMatcher(Collections.singleton("/actuator/**"));
        assertThat(matcher.matches(request)).isTrue();
    }

    @Test
    void matchDifferentPathsThenFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuators");
        TenantContextIgnorePathMatcher matcher = new TenantContextIgnorePathMatcher(Collections.singleton("/actuator/**"));
        assertThat(matcher.matches(request)).isFalse();
    }

    @Test
    void whenNullRequestThenThrow() {
        TenantContextIgnorePathMatcher matcher = new TenantContextIgnorePathMatcher(Collections.singleton("/actuator/**"));
        assertThatThrownBy(() -> matcher.matches(null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("httpServletRequest cannot be null");
    }

}
