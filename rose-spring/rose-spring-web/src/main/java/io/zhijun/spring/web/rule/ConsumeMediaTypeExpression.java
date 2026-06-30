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
  * 解析和匹配单个媒体类型表达式到请求的 'Content-Type' 头。
  */
 public class ConsumeMediaTypeExpression extends GenericMediaTypeExpression {
 
     public ConsumeMediaTypeExpression(String expression) {
         super(expression);
     }
 
     public ConsumeMediaTypeExpression(MediaType mediaType, boolean negated) {
         super(mediaType, negated);
     }
 
     public final boolean match(MediaType contentType) {
         boolean match = getMediaType().includes(contentType) && matchParameters(contentType);
         return !isNegated() == match;
     }
 
     public static List<ConsumeMediaTypeExpression> parseExpressions(String @Nullable [] consumes, @Nullable String[] headers) {
         int consumesSize = consumes != null ? consumes.length : 0;
         int headersSize = headers != null ? headers.length : 0;
 
         Set<ConsumeMediaTypeExpression> result = new LinkedHashSet<>(consumesSize + headersSize);
 
         if (headers != null) {
             for (String header : headers) {
                 WebRequestHeaderExpression expression = new WebRequestHeaderExpression(header);
                 if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(expression.name) && expression.value != null) {
                     List<MediaType> mediaTypes = MediaType.parseMediaTypes(expression.value);
                     for (MediaType mediaType : mediaTypes) {
                         result.add(new ConsumeMediaTypeExpression(mediaType, expression.isNegated));
                     }
                 }
             }
         }
 
         if (consumes != null) {
             for (String consume : consumes) {
                 result.add(new ConsumeMediaTypeExpression(consume));
             }
         }
 
         return result.isEmpty() ? Collections.emptyList() : new ArrayList<>(result);
     }
 }
