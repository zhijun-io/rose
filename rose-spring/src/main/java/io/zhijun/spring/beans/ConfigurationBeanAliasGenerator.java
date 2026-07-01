package io.zhijun.spring.beans;

import io.zhijun.spring.beans.factory.DefaultConfigurationBeanAliasGenerator;
import io.zhijun.spring.beans.factory.support.HyphenAliasGenerator;
import io.zhijun.spring.beans.factory.support.UnderScoreJoinAliasGenerator;

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
