 package io.zhijun.spring.core.beans.factory.config;
 
 import org.springframework.beans.factory.config.NamedBeanHolder;
 import org.springframework.core.annotation.AnnotationAwareOrderComparator;
 
 import java.util.Comparator;
 
 /**
  * {@link NamedBeanHolder} 比较器，委托 {@link AnnotationAwareOrderComparator} 比较内部 bean。
  */
 public class NamedBeanHolderComparator<T> implements Comparator<NamedBeanHolder<T>> {
 
     public static final NamedBeanHolderComparator<?> INSTANCE = new NamedBeanHolderComparator<>();
 
     @Override
     public int compare(NamedBeanHolder<T> o1, NamedBeanHolder<T> o2) {
         return AnnotationAwareOrderComparator.INSTANCE.compare(o1.getBeanInstance(), o2.getBeanInstance());
     }
 }
