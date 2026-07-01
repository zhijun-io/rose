package io.zhijun.spring.beans.factory.support;

import static org.springframework.util.StringUtils.capitalize;

/**
 * 通过分隔符连接前缀各部分以生成别名的 {@link ConfigurationBeanAliasGenerator} 实现。
 */
public abstract class JoinAliasGenerator implements ConfigurationBeanAliasGenerator {

    @Override
    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        String[] prefixArray = prefix.split("\\.");
        StringBuilder sb = new StringBuilder(prefixArray[0]);
        for (int i = 1; i < prefixArray.length; i++) {
            sb.append(capitalize(prefixArray[i]));
        }
        return sb.toString() + delimiter() + beanName;
    }

    protected abstract String delimiter();
}
