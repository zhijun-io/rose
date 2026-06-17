package io.zhijun.data.excel.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.metadata.CellExtra;

/**
 * Fills merged-cell values into row DTOs during import.
 */
public class ExcelMergeHelper<T> {

	private static final Logger log = LoggerFactory.getLogger(ExcelMergeHelper.class);

	public List<T> explainMergeData(List<T> data, List<CellExtra> extraMergeInfoList, Integer headRowNumber) {
		extraMergeInfoList.forEach(cellExtra -> applyMergeExtra(data, headRowNumber, cellExtra));
		return data;
	}

	private void applyMergeExtra(List<T> data, Integer headRowNumber, CellExtra cellExtra) {
		int firstRowIndex = cellExtra.getFirstRowIndex() - headRowNumber;
		int lastRowIndex = cellExtra.getLastRowIndex() - headRowNumber;
		int firstColumnIndex = cellExtra.getFirstColumnIndex();
		int lastColumnIndex = cellExtra.getLastColumnIndex();
		Object initValue = getInitValueFromList(firstRowIndex, firstColumnIndex, data);
		for (int rowIndex = firstRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
			for (int columnIndex = firstColumnIndex; columnIndex <= lastColumnIndex; columnIndex++) {
				setInitValueToList(initValue, rowIndex, columnIndex, data);
			}
		}
	}

	public void setInitValueToList(Object value, Integer rowIndex, Integer columnIndex, List<T> data) {
		T row = data.get(rowIndex);
		List<Field> fields = collectFields(row.getClass());
		for (Field field : fields) {
			field.setAccessible(true);
			ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
			if (annotation == null || annotation.index() != columnIndex) {
				continue;
			}
			try {
				field.set(row, value);
			} catch (IllegalAccessException ex) {
				log.error("设置合并单元格的值异常：{}", ex.getMessage());
			}
			return;
		}
	}

	private Object getInitValueFromList(Integer rowIndex, Integer columnIndex, List<T> data) {
		T row = data.get(rowIndex);
		List<Field> fields = collectFields(row.getClass());
		for (Field field : fields) {
			field.setAccessible(true);
			ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
			if (annotation == null || annotation.index() != columnIndex) {
				continue;
			}
			try {
				return field.get(row);
			} catch (IllegalAccessException ex) {
				log.error("设置合并单元格的初始值异常：{}", ex.getMessage());
			}
		}
		return null;
	}

	private List<Field> collectFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		Class<?> current = type;
		while (current != null) {
			fields.addAll(Arrays.asList(current.getDeclaredFields()));
			current = current.getSuperclass();
		}
		return fields;
	}
}
