package io.zhijun.spring.beans.factory.support;

/**
 * Alias generator using hyphen as delimiter.
 */
public class HyphenAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "-";
    }
}
