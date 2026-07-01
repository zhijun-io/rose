package io.zhijun.spring.beans.factory.support;

/**
 * 使用下划线（"_"）作为分隔符的 {@link JoinAliasGenerator}。
 */
public class UnderScoreJoinAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "_";
    }
}
