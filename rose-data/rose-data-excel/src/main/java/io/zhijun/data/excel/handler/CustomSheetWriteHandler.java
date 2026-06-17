package io.zhijun.data.excel.handler;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;

import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;

/**
 * Dropdown list constraints for Excel export templates.
 */
public class CustomSheetWriteHandler implements SheetWriteHandler {

	private final Map<Integer, List<String>> explicitListConstraintMap;

	private Integer rowCount;

	public CustomSheetWriteHandler(Map<Integer, List<String>> explicitListConstraintMap, Integer rowCount) {
		this.explicitListConstraintMap = explicitListConstraintMap;
		this.rowCount = rowCount;
	}

	@Override
	public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
		if (explicitListConstraintMap == null || explicitListConstraintMap.isEmpty()) {
			return;
		}
		if (rowCount == null || rowCount < 1) {
			rowCount = 1;
		}
		DataValidationHelper helper = writeSheetHolder.getSheet().getDataValidationHelper();
		int hiddenSheetIndex = 0;
		for (Map.Entry<Integer, List<String>> entry : explicitListConstraintMap.entrySet()) {
			Integer columnIndex = entry.getKey();
			List<String> values = entry.getValue();
			Workbook workbook = writeWorkbookHolder.getWorkbook();
			String sheetName = "sheet" + columnIndex;
			Sheet hiddenSheet = workbook.createSheet(sheetName);
			hiddenSheetIndex++;
			workbook.setSheetHidden(hiddenSheetIndex, true);
			for (int i = 0; i < values.size(); i++) {
				Row row = hiddenSheet.createRow(i);
				Cell cell = row.createCell(0);
				cell.setCellValue(values.get(i));
			}
			Name name = workbook.createName();
			name.setNameName(sheetName);
			name.setRefersToFormula(sheetName + "!$A$1:$A$" + values.size());
			CellRangeAddressList range = new CellRangeAddressList(1, rowCount, columnIndex, columnIndex);
			DataValidationConstraint constraint = helper.createFormulaListConstraint(sheetName);
			DataValidation validation = helper.createValidation(constraint, range);
			validation.setSuppressDropDownArrow(true);
			writeSheetHolder.getSheet().addValidationData(validation);
		}
	}
}
