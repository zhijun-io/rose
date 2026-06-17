package io.zhijun.data.excel.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.enums.CellExtraTypeEnum;
import cn.idev.excel.metadata.CellExtra;
import io.zhijun.data.excel.ReadFailMessageAware;
import io.zhijun.data.excel.support.ExcelMergeHelper;

/**
 * Import listener that expands merged cells before Bean Validation.
 */
public class ReadMergeValidateListener<T extends ReadFailMessageAware> extends ReadValidateListener<T> {

	private final List<T> datas = new ArrayList<>();

	private final List<CellExtra> extraList = new ArrayList<>();

	private final Integer headRowNumber;

	private final int[] mergeColumnIndex;

	public ReadMergeValidateListener(Integer headRowNumber, int[] mergeColumnIndex,
			BiConsumer<List<T>, List<T>> consumer) {
		super(consumer);
		this.headRowNumber = headRowNumber;
		this.mergeColumnIndex = mergeColumnIndex;
	}

	@Override
	public void invoke(T data, AnalysisContext context) {
		context.readWorkbookHolder().setIgnoreEmptyRow(Boolean.FALSE);
		datas.add(data);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		for (T data : getData()) {
			validateData(data);
		}
		super.doAfterAllAnalysed(context);
	}

	@Override
	public void extra(CellExtra extra, AnalysisContext context) {
		if (CellExtraTypeEnum.MERGE != extra.getType()) {
			return;
		}
		if (extra.getRowIndex() >= headRowNumber) {
			extraList.add(extra);
		}
	}

	private List<T> getData() {
		if (extraList.isEmpty()) {
			return datas;
		}
		checkMergeIndex();
		return new ExcelMergeHelper<T>().explainMergeData(datas, extraList, headRowNumber);
	}

	private void checkMergeIndex() {
		if (extraList.size() % mergeColumnIndex.length != 0) {
			throw new RuntimeException("单元格合并错误，请检查后重新上传");
		}
	}
}
