package io.zhijun.spring.beans.factory.support;

public class HyphenAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "-";
    }
}
