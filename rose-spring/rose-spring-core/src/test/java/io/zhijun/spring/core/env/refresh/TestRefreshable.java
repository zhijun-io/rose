package io.zhijun.spring.core.env.refresh;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test {@link Refreshable} registered via test {@code META-INF/spring.factories}.
 */
public final class TestRefreshable implements Refreshable {

    static final AtomicInteger REFRESH_COUNT = new AtomicInteger();

    static volatile Set<String> LAST_KEYS = Collections.emptySet();

    static void reset() {
        REFRESH_COUNT.set(0);
        LAST_KEYS = Collections.emptySet();
    }

    @Override
    public boolean supports(Set<String> changedKeys) {
        return changedKeys.contains("integration.key");
    }

    @Override
    public void refresh(Set<String> changedKeys) {
        REFRESH_COUNT.incrementAndGet();
        LAST_KEYS = changedKeys;
    }
}
