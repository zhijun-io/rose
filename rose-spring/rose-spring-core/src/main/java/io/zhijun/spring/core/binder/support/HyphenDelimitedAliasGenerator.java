package io.zhijun.spring.core.binder.support;

/**
 * Uses {@code -} as the alias delimiter.
 */
public final class HyphenDelimitedAliasGenerator extends DelimitedAliasGenerator {

    public HyphenDelimitedAliasGenerator() {
        super("-");
    }
}
