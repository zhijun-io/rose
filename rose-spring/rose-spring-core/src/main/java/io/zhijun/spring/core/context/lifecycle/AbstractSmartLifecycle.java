 package io.zhijun.spring.core.context.lifecycle;
 
 import org.springframework.context.SmartLifecycle;
 
 /**
  * {@link SmartLifecycle} 的抽象基类，提供模板方法 {@link #doStart()}/{@link #doStop()}。
  */
 public abstract class AbstractSmartLifecycle implements SmartLifecycle {
 
     private int phase = SmartLifecycle.DEFAULT_PHASE;
 
     private volatile boolean running = false;
 
     @Override
     public final void start() {
         doStart();
         running = true;
     }
 
     protected abstract void doStart();
 
     @Override
     public final void stop() {
         doStop();
         running = false;
     }
 
     protected abstract void doStop();
 
     @Override
     public final boolean isRunning() {
         return running;
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
 
     public final void setPhase(int phase) {
         this.phase = phase;
     }
 }
