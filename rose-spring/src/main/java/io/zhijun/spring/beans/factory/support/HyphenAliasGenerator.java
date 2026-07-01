package io.zhijun.spring.beans.factory.support;

/**
 * 使用连字符（"-"）作为分隔符的 {@link JoinAliasGenerator}。
 */
public class HyphenAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "-";
    }
}
