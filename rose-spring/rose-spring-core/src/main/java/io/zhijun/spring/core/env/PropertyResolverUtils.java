 package io.zhijun.spring.core.env;
 
 import org.jspecify.annotations.Nullable;
 import org.springframework.core.env.PropertyResolver;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * {@link PropertyResolver} 工具类，递归解析集合和数组中的占位符。
  */
 public abstract class PropertyResolverUtils {
 
     private PropertyResolverUtils() {
     }
 
     /**
      * 递归解析 Map 值中的占位符。
      */
     public static Map<String, Object> resolvePlaceholders(Map<String, Object> source,
                                                            @Nullable PropertyResolver propertyResolver) {
         if (source == null || source.isEmpty() || propertyResolver == null) {
             return source;
         }
         Map<String, Object> result = new LinkedHashMap<String, Object>(source);
         for (Map.Entry<String, Object> entry : result.entrySet()) {
             entry.setValue(resolvePlaceholders(entry.getValue(), propertyResolver));
         }
         return result;
     }
 
     /**
      * 递归解析值中的占位符。
      */
     @Nullable
     public static Object resolvePlaceholders(@Nullable Object source,
                                               @Nullable PropertyResolver propertyResolver) {
         if (source instanceof String) {
             return resolvePlaceholders((String) source, propertyResolver);
         }
         if (source instanceof String[]) {
             return resolvePlaceholders((String[]) source, propertyResolver);
         }
         return source;
     }
 
     /**
      * 解析字符串数组中的占位符。
      */
     @Nullable
     public static String[] resolvePlaceholders(@Nullable String[] values,
                                                 @Nullable PropertyResolver propertyResolver) {
         if (values == null || values.length == 0 || propertyResolver == null) {
             return values;
         }
         String[] result = new String[values.length];
         for (int i = 0; i < values.length; i++) {
             result[i] = resolvePlaceholders(values[i], propertyResolver);
         }
         return result;
     }
 
     /**
      * 解析单个字符串中的占位符。
      */
     public static String resolvePlaceholders(@Nullable String value,
                                               @Nullable PropertyResolver propertyResolver) {
         if (value == null || value.isEmpty() || propertyResolver == null) {
             return value;
         }
         return propertyResolver.resolvePlaceholders(value);
     }
 }
