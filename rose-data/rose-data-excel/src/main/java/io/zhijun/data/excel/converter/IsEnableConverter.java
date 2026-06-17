package io.zhijun.data.excel.converter;

import org.apache.commons.lang3.StringUtils;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

/**
 * Maps boolean fields to 启用/禁用 labels in Excel cells.
 */
public class IsEnableConverter implements Converter<Boolean> {

	@Override
	public Class<?> supportJavaTypeKey() {
		return Boolean.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Boolean convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		String value = cellData.getStringValue();
		if (StringUtils.isBlank(value)) {
			return Boolean.FALSE;
		}
		return !"禁用".equals(value);
	}

	@Override
	public WriteCellData<?> convertToExcelData(Boolean value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return new WriteCellData<>(Boolean.TRUE.equals(value) ? "启用" : "禁用");
	}
}
