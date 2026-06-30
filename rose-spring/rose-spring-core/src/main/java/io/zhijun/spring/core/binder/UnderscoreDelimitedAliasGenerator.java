package io.zhijun.spring.core.binder;

/**
 * Uses {@code _} as the alias delimiter.
 */
public final class UnderscoreDelimitedAliasGenerator extends DelimitedAliasGenerator {

    public UnderscoreDelimitedAliasGenerator() {
        super("_");
    }
}
