package io.zhijun.spring.core.binder.support;

/**
 * Alias generator using underscore as delimiter.
 */
public final class UnderScoreJoinAliasGenerator extends DelimitedAliasGenerator {

    public UnderScoreJoinAliasGenerator() {
        super("_");
    }
}
