package io.zhijun.mybatisplus.core.permission;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;

class RoseDataPermissionHandlerTests {

    @Test
    void shouldAppendShopFilterExpression() {
        DataPermissionPrincipalResolver principalResolver =
                () -> DataPermissionPrincipal.of(new HashSet<Long>(Arrays.asList(1L, 2L)));
        DataPermissionConditionResolver conditionResolver = (annotation, principal) -> {
            InExpression inExpression = new InExpression(
                    new Column("sys_shop.id"),
                    new ExpressionList(Arrays.<Expression>asList(new LongValue(1), new LongValue(2))));
            return inExpression;
        };
        RoseDataPermissionHandler handler = new RoseDataPermissionHandler(principalResolver, conditionResolver);

        Expression expression = handler.getSqlSegment(null, SampleShopMapper.class.getName() + ".selectList");

        assertThat(expression).isNotNull();
        assertThat(expression.toString()).contains("sys_shop.id");
        assertThat(expression.toString()).contains("IN");
    }

    @Test
    void shouldBypassWhenPrincipalBypasses() {
        DataPermissionPrincipalResolver principalResolver = () -> DataPermissionPrincipal.bypass();
        DataPermissionConditionResolver conditionResolver = (annotation, principal) -> {
            throw new IllegalStateException("should not resolve condition when bypassed");
        };
        RoseDataPermissionHandler handler = new RoseDataPermissionHandler(principalResolver, conditionResolver);

        Expression expression = handler.getSqlSegment(null, SampleShopMapper.class.getName() + ".selectList");

        assertThat(expression).isNull();
    }

    @Test
    void shouldSkipBaseMapperSelectById() {
        DataPermissionPrincipalResolver principalResolver =
                () -> DataPermissionPrincipal.of(new HashSet<Long>(Collections.singletonList(1L)));
        DataPermissionConditionResolver conditionResolver = (annotation, principal) -> {
            throw new IllegalStateException("should not resolve condition for BaseMapper selectById");
        };
        RoseDataPermissionHandler handler = new RoseDataPermissionHandler(principalResolver, conditionResolver);

        // SampleShopMapper does not declare its own selectById -> inherited from BaseMapper -> skipped
        Expression expression = handler.getSqlSegment(null, SampleShopMapper.class.getName() + ".selectById");

        assertThat(expression).isNull();
    }

    @Test
    void shouldApplyPermissionToCustomMethodNamedSelectById() {
        DataPermissionPrincipalResolver principalResolver =
                () -> DataPermissionPrincipal.of(new HashSet<Long>(Arrays.asList(1L, 2L)));
        DataPermissionConditionResolver conditionResolver = (annotation, principal) -> new InExpression(
                new Column("sys_shop.id"),
                new ExpressionList(Arrays.<Expression>asList(new LongValue(1), new LongValue(2))));
        RoseDataPermissionHandler handler = new RoseDataPermissionHandler(principalResolver, conditionResolver);

        // CustomMapperWithSelectById declares its own selectById -> NOT skipped -> permission applied
        Expression expression = handler.getSqlSegment(null, CustomMapperWithSelectById.class.getName() + ".selectById");

        assertThat(expression).isNotNull();
        assertThat(expression.toString()).contains("sys_shop.id");
    }

    @DataPermission(alias = "sys_shop", column = "id")
    interface SampleShopMapper {}

    @DataPermission(alias = "sys_shop", column = "id")
    interface CustomMapperWithSelectById {
        void selectById();
    }
}
