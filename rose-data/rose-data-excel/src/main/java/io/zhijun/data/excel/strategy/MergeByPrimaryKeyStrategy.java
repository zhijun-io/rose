package io.zhijun.data.excel.strategy;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;

/**
 * Merges adjacent rows when the primary-key column value matches.
 */
public class MergeByPrimaryKeyStrategy implements CellWriteHandler {

	private final int headRowNumber;

	private final int[] mergeColumnIndex;

	private int primaryKeyIndex;

	private boolean mergeNullValue;

	public MergeByPrimaryKeyStrategy(int headRowNumber, int[] mergeColumnIndex) {
		this(headRowNumber, mergeColumnIndex, 0, false);
	}

	public MergeByPrimaryKeyStrategy(int headRowNumber, int[] mergeColumnIndex, int primaryKeyIndex,
			boolean mergeNullValue) {
		this.headRowNumber = headRowNumber;
		this.mergeColumnIndex = mergeColumnIndex;
		this.primaryKeyIndex = primaryKeyIndex;
		this.mergeNullValue = mergeNullValue;
	}

	@Override
	public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
			List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
		int currentRowIndex = cell.getRowIndex();
		int currentColumnIndex = cell.getColumnIndex();
		if (currentRowIndex <= headRowNumber) {
			return;
		}
		for (int columnIndex : mergeColumnIndex) {
			if (currentColumnIndex == columnIndex) {
				mergeWithPrevRow(writeSheetHolder, cell, currentRowIndex, currentColumnIndex, primaryKeyIndex);
				return;
			}
		}
	}

	private void mergeWithPrevRow(WriteSheetHolder writeSheetHolder, Cell cell, int rowIndex, int columnIndex,
			int keyColumnIndex) {
		Object currentValue = readCellValue(cell.getSheet(), rowIndex, keyColumnIndex);
		Object previousValue = readCellValue(cell.getSheet(), rowIndex - 1, keyColumnIndex);
		if (!shouldMerge(currentValue, previousValue)) {
			return;
		}

		Sheet sheet = writeSheetHolder.getSheet();
		List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
		boolean merged = false;
		for (int index = 0; index < mergedRegions.size() && !merged; index++) {
			CellRangeAddress region = mergedRegions.get(index);
			if (!region.isInRange(rowIndex - 1, columnIndex)) {
				continue;
			}
			sheet.removeMergedRegion(index);
			region.setLastRow(rowIndex);
			sheet.addMergedRegion(region);
			merged = true;
		}
		if (!merged) {
			sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex, columnIndex, columnIndex));
		}
	}

	private boolean shouldMerge(Object currentValue, Object previousValue) {
		if (mergeNullValue) {
			return !java.util.Objects.equals(currentValue, previousValue);
		}
		return currentValue != null && previousValue != null && currentValue.equals(previousValue);
	}

	private Object readCellValue(Sheet sheet, int rowIndex, int columnIndex) {
		Row row = sheet.getRow(rowIndex);
		if (row == null) {
			return null;
		}
		Cell keyCell = row.getCell(columnIndex);
		if (keyCell == null) {
			return null;
		}
		if (CellType.STRING == keyCell.getCellType()) {
			return keyCell.getStringCellValue();
		}
		return keyCell.getNumericCellValue();
	}
}
