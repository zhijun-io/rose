package io.zhijun.spring.beans.factory.support;

public class UnderScoreJoinAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "_";
    }
}
