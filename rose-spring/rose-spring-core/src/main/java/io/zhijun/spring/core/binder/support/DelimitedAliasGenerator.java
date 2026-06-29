package io.zhijun.spring.core.binder.support;

import org.springframework.util.StringUtils;

/**
 * Joins prefix segments and bean name with a configurable delimiter.
 */
public class DelimitedAliasGenerator implements ConfigurationBeanAliasGenerator {

    private final String delimiter;

    public DelimitedAliasGenerator(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        String[] prefixArray = prefix.split("\\.");
        String first = prefixArray[0];
        StringBuilder builder = new StringBuilder(first);
        for (int i = 1; i < prefixArray.length; i++) {
            builder.append(StringUtils.capitalize(prefixArray[i]));
        }
        return builder.append(delimiter).append(beanName).toString();
    }

    public static final class Hyphen extends DelimitedAliasGenerator {

        public Hyphen() {
            super("-");
        }
    }

    public static final class Underscore extends DelimitedAliasGenerator {

        public Underscore() {
            super("_");
        }
    }
}
