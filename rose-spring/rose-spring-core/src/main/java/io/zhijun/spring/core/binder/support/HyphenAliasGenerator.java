package io.zhijun.spring.core.binder.support;

/**
 * Alias generator using hyphen as delimiter.
 */
public final class HyphenAliasGenerator extends DelimitedAliasGenerator {

    public HyphenAliasGenerator() {
        super("-");
    }
}
