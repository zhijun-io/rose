package io.zhijun.spring.core.binder.support;

/**
 * Alias generator using underscore as delimiter.
 */
public class UnderScoreJoinAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "_";
    }
}
