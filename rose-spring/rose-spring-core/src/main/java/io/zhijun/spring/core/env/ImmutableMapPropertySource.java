 package io.zhijun.spring.core.env;
 
 import org.springframework.core.env.MapPropertySource;
 
 import java.util.Collections;
 import java.util.Map;
 
 /**
  * 不可变的 {@link MapPropertySource}。
  * <p>构造函数自动将源 Map 包装为不可变 Map。</p>
  */
 public class ImmutableMapPropertySource extends MapPropertySource {
 
     public ImmutableMapPropertySource(String name, Map<String, Object> source) {
         super(name, Collections.unmodifiableMap(source));
     }
 }
