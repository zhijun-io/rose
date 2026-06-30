package io.zhijun.spring.beans.factory;

/**
 * A strategy interface for generating aliases for configuration beans.
 *
 * @see DefaultConfigurationBeanAliasGenerator
 * @see HyphenAliasGenerator
 * @see UnderScoreJoinAliasGenerator
 */
public interface ConfigurationBeanAliasGenerator {

    String generateAlias(String prefix, String beanName, Class<?> configClass);

}
