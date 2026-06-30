 package io.zhijun.spring.core.env;
 
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.PropertyValues;
 import org.springframework.core.env.ConfigurableEnvironment;
 
 import java.util.Map;
 
 /**
  * {@link PropertyValues} 工具类。
  * <p>将环境中指定前缀的子属性集转换为 {@link PropertyValues}。</p>
  */
 public abstract class PropertyValuesUtils {
 
     private PropertyValuesUtils() {
     }
 
     /**
      * 获取环境中指定前缀的属性子集，返回 {@link PropertyValues}。
      */
     public static PropertyValues getSubPropertyValues(ConfigurableEnvironment environment, String prefix) {
         Map<String, Object> subProperties = PropertySourcesUtils.getSubProperties(environment, prefix);
         return new MutablePropertyValues(subProperties);
     }
 }
