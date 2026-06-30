package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * 解析和匹配单个媒体类型表达式到请求的 'Accept' 头。
  */
 public class ProduceMediaTypeExpression extends GenericMediaTypeExpression {
 
     public ProduceMediaTypeExpression(String expression) {
         super(expression);
     }
 
     public ProduceMediaTypeExpression(MediaType mediaType, boolean negated) {
         super(mediaType, negated);
     }
 
     public final boolean match(List<MediaType> acceptedMediaTypes) {
         boolean match = matchMediaType(acceptedMediaTypes);
         return !isNegated() == match;
     }
 
     boolean matchMediaType(List<MediaType> acceptedMediaTypes) {
         for (MediaType acceptedMediaType : acceptedMediaTypes) {
             if (getMediaType().isCompatibleWith(acceptedMediaType) && matchParameters(acceptedMediaType)) {
                 return true;
             }
         }
         return false;
     }
 
     public static List<ProduceMediaTypeExpression> parseExpressions(String @Nullable [] produces, @Nullable String[] headers) {
         int producesSize = produces != null ? produces.length : 0;
         int headersSize = headers != null ? headers.length : 0;
 
         Set<ProduceMediaTypeExpression> result = new LinkedHashSet<>(producesSize + headersSize);
 
         if (headers != null) {
             for (String header : headers) {
                 WebRequestHeaderExpression expression = new WebRequestHeaderExpression(header);
                 if (HttpHeaders.ACCEPT.equalsIgnoreCase(expression.name) && expression.value != null) {
                     List<MediaType> mediaTypes = MediaType.parseMediaTypes(expression.value);
                     for (MediaType mediaType : mediaTypes) {
                         result.add(new ProduceMediaTypeExpression(mediaType, expression.isNegated));
                     }
                 }
             }
         }
 
         if (produces != null) {
             for (String produce : produces) {
                 result.add(new ProduceMediaTypeExpression(produce));
             }
         }
 
         return result.isEmpty() ? Collections.emptyList() : new ArrayList<>(result);
     }
 }
