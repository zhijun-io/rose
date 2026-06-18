package io.zhijun.mybatisplus.permission;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;

class RoseDataPermissionHandlerTests {

    @Test
    void shouldAppendShopFilterExpression() {
        DataPermissionPrincipalResolver principalResolver = () -> DataPermissionPrincipal.of(new HashSet<Long>(Arrays.asList(1L, 2L)));
        DataPermissionConditionResolver conditionResolver = (annotation, principal) -> {
            InExpression inExpression = new InExpression(new Column("sys_shop.id"),
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

    @DataPermission(alias = "sys_shop", column = "id")
    interface SampleShopMapper {
    }
}
