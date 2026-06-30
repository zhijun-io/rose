package io.zhijun.spring.context.lifecycle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractSmartLifecycleTests {

    @Test
    void shouldStartAndStop() {
        TestLifecycle lifecycle = new TestLifecycle();
        assertThat(lifecycle.isRunning()).isFalse();
        assertThat(lifecycle.isStarted()).isFalse();

        lifecycle.start();
        assertThat(lifecycle.isRunning()).isTrue();
        assertThat(lifecycle.isStarted()).isTrue();
        assertThat(lifecycle.doStartCalled).isTrue();

        lifecycle.stop();
        assertThat(lifecycle.isRunning()).isFalse();
        assertThat(lifecycle.isStarted()).isFalse();
        assertThat(lifecycle.doStopCalled).isTrue();
    }

    @Test
    void shouldBeAutoStartupByDefault() {
        TestLifecycle lifecycle = new TestLifecycle();
        assertThat(lifecycle.isAutoStartup()).isTrue();
    }

    @Test
    void shouldUseLateStopPhaseByDefault() {
        TestLifecycle lifecycle = new TestLifecycle();
        assertThat(lifecycle.getPhase()).isEqualTo(AbstractSmartLifecycle.LATEST_PHASE);
        assertThat(lifecycle.getPhase()).isEqualTo(AbstractSmartLifecycle.DEFAULT_PHASE);
    }

    @Test
    void shouldSupportCustomPhase() {
        TestLifecycle lifecycle = new TestLifecycle();
        lifecycle.setPhase(5);
        assertThat(lifecycle.getPhase()).isEqualTo(5);
    }

    @Test
    void shouldSupportEarliestAndLatestConstants() {
        assertThat(AbstractSmartLifecycle.EARLIEST_PHASE).isEqualTo(Integer.MIN_VALUE);
        assertThat(AbstractSmartLifecycle.LATEST_PHASE).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void shouldStopWithCallback() {
        TestLifecycle lifecycle = new TestLifecycle();
        lifecycle.start();

        boolean[] callbackCalled = {false};
        lifecycle.stop(() -> callbackCalled[0] = true);

        assertThat(lifecycle.isRunning()).isFalse();
        assertThat(callbackCalled[0]).isTrue();
    }

    @Test
    void shouldSupportSettingStartedState() {
        TestLifecycle lifecycle = new TestLifecycle();
        lifecycle.setStarted(true);
        assertThat(lifecycle.isRunning()).isTrue();
        assertThat(lifecycle.isStarted()).isTrue();
    }

    static class TestLifecycle extends AbstractSmartLifecycle {
        boolean doStartCalled;
        boolean doStopCalled;

        @Override
        protected void doStart() {
            doStartCalled = true;
        }

        @Override
        protected void doStop() {
            doStopCalled = true;
        }
    }
}
