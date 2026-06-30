package io.zhijun.spring.context.lifecycle;

import org.springframework.context.SmartLifecycle;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

/**
 * {@link SmartLifecycle} 抽象基类，提供 doStart()/doStop() 模板方法。
 * <p>
 * （移植自 microsphere-spring {@code AbstractSmartLifecycle}）
 *
 * @see SmartLifecycle
 */
public abstract class AbstractSmartLifecycle implements SmartLifecycle {

    /** 最早阶段 */
    public static final int EARLIEST_PHASE = MIN_VALUE;

    /** 最晚阶段 */
    public static final int LATEST_PHASE = MAX_VALUE;

    /** 默认阶段，兼容 Spring Framework 5.1 之前的 {@link SmartLifecycle#DEFAULT_PHASE} */
    public static final int DEFAULT_PHASE = LATEST_PHASE;

    private int phase = DEFAULT_PHASE;

    private volatile boolean started = false;

    @Override
    public final void start() {
        doStart();
        started = true;
    }

    protected abstract void doStart();

    @Override
    public final void stop() {
        doStop();
        started = false;
    }

    protected abstract void doStop();

    @Override
    public final boolean isRunning() {
        return started;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public final int getPhase() {
        return phase;
    }

    public boolean isStarted() {
        return started;
    }

    public final void setPhase(int phase) {
        this.phase = phase;
    }

    protected void setStarted(boolean started) {
        this.started = started;
    }
}
