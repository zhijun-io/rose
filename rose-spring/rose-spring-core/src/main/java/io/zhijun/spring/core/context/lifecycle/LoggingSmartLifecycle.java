 package io.zhijun.spring.core.context.lifecycle;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 带日志的 {@link AbstractSmartLifecycle} 实现。
  * <p>在 {@link #doStart()} / {@link #doStop()} 时输出生命周期日志。</p>
  */
 public class LoggingSmartLifecycle extends AbstractSmartLifecycle {
 
     private static final Logger log = LoggerFactory.getLogger(LoggingSmartLifecycle.class);
 
     @Override
     protected void doStart() {
         if (log.isInfoEnabled()) {
             log.info("生命周期开始 [{}]", getClass().getSimpleName());
         }
     }
 
     @Override
     protected void doStop() {
         if (log.isInfoEnabled()) {
             log.info("生命周期结束 [{}]", getClass().getSimpleName());
         }
     }
 }
