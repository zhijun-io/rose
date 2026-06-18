package io.zhijun.mybatisplus.audit;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.util.ClassUtils;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

/**
 * Default audit field auto-fill handler.
 */
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private final CurrentAuditorProvider auditorProvider;

    private final Supplier<LocalDateTime> timeSupplier;

    public AuditMetaObjectHandler(CurrentAuditorProvider auditorProvider) {
        this(auditorProvider, LocalDateTime::now);
    }

    public AuditMetaObjectHandler(CurrentAuditorProvider auditorProvider, Supplier<LocalDateTime> timeSupplier) {
        this.auditorProvider = auditorProvider;
        this.timeSupplier = timeSupplier;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = timeSupplier.get();
        String auditor = auditorProvider.getCurrentAuditor();
        fillIfAbsent("createTime", now, metaObject, false);
        fillIfAbsent("updateTime", now, metaObject, false);
        fillIfAbsent("creator", auditor, metaObject, false);
        fillIfAbsent("updater", auditor, metaObject, false);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fillIfAbsent("updateTime", timeSupplier.get(), metaObject, true);
        fillIfAbsent("updater", auditorProvider.getCurrentAuditor(), metaObject, true);
    }

    private static void fillIfAbsent(String fieldName, Object fieldValue, MetaObject metaObject, boolean overwrite) {
        if (!metaObject.hasSetter(fieldName)) {
            return;
        }
        Object current = metaObject.getValue(fieldName);
        String currentText = current == null ? null : current.toString();
        if (!overwrite && StringUtils.isNotBlank(currentText)) {
            return;
        }
        if (fieldValue == null) {
            return;
        }
        Class<?> getterType = metaObject.getGetterType(fieldName);
        if (ClassUtils.isAssignableValue(getterType, fieldValue)) {
            metaObject.setValue(fieldName, fieldValue);
        }
    }
}
