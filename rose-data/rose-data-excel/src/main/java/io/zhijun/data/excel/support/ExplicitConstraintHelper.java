package io.zhijun.data.excel.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.zhijun.data.excel.annotation.ExcelExplicitConstraint;

/**
 * Builds dropdown constraint maps from {@link ExcelExplicitConstraint} metadata.
 */
public final class ExplicitConstraintHelper {

	private ExplicitConstraintHelper() {
	}

	public static Map<Integer, List<String>> buildExplicitListConstraintMap(Class<?> headClass) {
		Map<Integer, List<String>> explicitListConstraintMap = new HashMap<>();
		Field[] declaredFields = headClass.getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			ExcelExplicitConstraint excelExplicitConstraint = field.getAnnotation(ExcelExplicitConstraint.class);
			if (excelExplicitConstraint == null) {
				continue;
			}
			Class<?> enumClass = excelExplicitConstraint.source();
			explicitListConstraintMap.put(i, new ArrayList<String>());
			int columnIndex = i;
			for (Object enumObj : enumClass.getEnumConstants()) {
				for (Field enumField : enumClass.getDeclaredFields()) {
					if (!"name".equals(enumField.getName())) {
						continue;
					}
					enumField.setAccessible(true);
					try {
						explicitListConstraintMap.get(columnIndex).add(enumField.get(enumObj).toString());
					} catch (IllegalAccessException ex) {
						throw new IllegalStateException("Failed to read enum name: " + enumClass.getName(), ex);
					}
				}
			}
		}
		return explicitListConstraintMap;
	}
}
