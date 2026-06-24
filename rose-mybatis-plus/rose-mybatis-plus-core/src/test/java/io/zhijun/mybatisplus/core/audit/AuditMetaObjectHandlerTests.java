package io.zhijun.mybatisplus.core.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

class AuditMetaObjectHandlerTests {

    @Test
    void shouldFillAuditFieldsOnInsert() {
        AuditableEntity entity = new AuditableEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        AuditMetaObjectHandler handler = new AuditMetaObjectHandler(() -> "tester", () -> LocalDateTime.of(2026, 6, 17, 10, 0));

        handler.insertFill(metaObject);

        assertThat(entity.getCreator()).isEqualTo("tester");
        assertThat(entity.getUpdater()).isEqualTo("tester");
        assertThat(entity.getCreateTime()).isEqualTo(LocalDateTime.of(2026, 6, 17, 10, 0));
        assertThat(entity.getUpdateTime()).isEqualTo(LocalDateTime.of(2026, 6, 17, 10, 0));
    }

    @Test
    void shouldNotOverwriteManualCreatorOnInsert() {
        AuditableEntity entity = new AuditableEntity();
        entity.setCreator("manual");
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        AuditMetaObjectHandler handler = new AuditMetaObjectHandler(() -> "tester");

        handler.insertFill(metaObject);

        assertThat(entity.getCreator()).isEqualTo("manual");
        assertThat(entity.getUpdater()).isEqualTo("tester");
    }

    @Test
    void shouldOverwriteUpdaterOnUpdate() {
        AuditableEntity entity = new AuditableEntity();
        entity.setUpdater("old");
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        AuditMetaObjectHandler handler = new AuditMetaObjectHandler(() -> "new-user", () -> LocalDateTime.of(2026, 6, 17, 11, 0));

        handler.updateFill(metaObject);

        assertThat(entity.getUpdater()).isEqualTo("new-user");
        assertThat(entity.getUpdateTime()).isEqualTo(LocalDateTime.of(2026, 6, 17, 11, 0));
    }
}
