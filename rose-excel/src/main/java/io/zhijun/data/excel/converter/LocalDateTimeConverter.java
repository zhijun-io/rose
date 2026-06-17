package io.zhijun.data.excel.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

/**
 * Reads and writes {@link LocalDateTime} as {@code yyyy-MM-dd HH:mm:ss}.
 */
public class LocalDateTimeConverter implements Converter<LocalDateTime> {

	private static final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	@Override
	public Class<LocalDateTime> supportJavaTypeKey() {
		return LocalDateTime.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public LocalDateTime convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return LocalDateTime.parse(cellData.getStringValue(),
				DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN));
	}

	@Override
	public WriteCellData<?> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return new WriteCellData<>(value.format(DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN)));
	}
}
