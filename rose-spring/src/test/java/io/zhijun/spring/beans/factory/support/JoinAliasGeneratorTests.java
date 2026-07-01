package io.zhijun.spring.beans.factory.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JoinAliasGeneratorTests {

    @Test
    void hyphenAliasGeneratorMultiSegment() {
        HyphenAliasGenerator generator = new HyphenAliasGenerator();
        assertEquals("springDatasource-myBean",
                generator.generateAlias("spring.datasource", "myBean", Object.class));
    }

    @Test
    void hyphenAliasGeneratorSingleSegment() {
        HyphenAliasGenerator generator = new HyphenAliasGenerator();
        assertEquals("spring-myBean",
                generator.generateAlias("spring", "myBean", Object.class));
    }

    @Test
    void underScoreAliasGeneratorMultiSegment() {
        UnderScoreJoinAliasGenerator generator = new UnderScoreJoinAliasGenerator();
        assertEquals("springDatasource_myBean",
                generator.generateAlias("spring.datasource", "myBean", Object.class));
    }

    @Test
    void underScoreAliasGeneratorThreeSegments() {
        UnderScoreJoinAliasGenerator generator = new UnderScoreJoinAliasGenerator();
        assertEquals("aBC_x",
                generator.generateAlias("a.b.c", "x", Object.class));
    }
}
