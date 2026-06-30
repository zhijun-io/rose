package io.zhijun.spring.core.propertysource;

import java.util.Set;

/**
 * Configuration refresh extension point. Register via {@code META-INF/spring.factories}.
 */
public interface Refreshable {

    /**
     * Whether this refreshable cares about any key in the change set.
     *
     * @param changedKeys non-empty changed keys
     */
    boolean supports(Set<String> changedKeys);

    /**
     * Perform refresh. Called only when {@link #supports(Set)} returns {@code true}.
     */
    void refresh(Set<String> changedKeys);
}
