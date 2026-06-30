 package io.zhijun.spring.core.convert;
 
 import org.springframework.core.convert.ConversionService;
 import org.springframework.core.convert.support.DefaultConversionService;
 
 /**
  * {@link ConversionService} 工具类。
  */
 public abstract class ConversionServiceUtils {
 
     private ConversionServiceUtils() {
     }
 
     /**
      * 获取共享的 {@link ConversionService} 实例。
      */
     public static ConversionService getSharedInstance() {
         return DefaultConversionService.getSharedInstance();
     }
 }
