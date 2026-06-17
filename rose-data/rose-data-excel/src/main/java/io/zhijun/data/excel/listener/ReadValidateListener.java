package io.zhijun.data.excel.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import io.zhijun.data.excel.ReadFailMessageAware;

/**
 * Validates each imported row with Bean Validation and splits success / failure buckets.
 */
public class ReadValidateListener<T extends ReadFailMessageAware> extends AnalysisEventListener<T> {

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	private final List<T> failData = new ArrayList<T>();

	private final List<T> successData = new ArrayList<T>();

	private final BiConsumer<List<T>, List<T>> consumer;

	public ReadValidateListener(BiConsumer<List<T>, List<T>> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void invoke(T data, AnalysisContext context) {
		validateData(data);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		if (consumer != null) {
			consumer.accept(successData, failData);
		}
	}

	protected void validateData(T data) {
		Set<ConstraintViolation<T>> violations = VALIDATOR.validate(data, Default.class);
		if (violations != null && !violations.isEmpty()) {
			data.setMessage(violations.stream()
					.map(ConstraintViolation::getMessage)
					.collect(Collectors.joining("、")));
			failData.add(data);
			return;
		}
		String customMessage = customValidate(successData, data);
		if (customMessage != null) {
			data.setMessage(customMessage);
			failData.add(data);
			return;
		}
		successData.add(data);
	}

	protected String customValidate(List<T> currentSuccessData, T data) {
		return null;
	}

	public List<T> getFailData() {
		return failData;
	}

	public List<T> getSuccessData() {
		return successData;
	}
}
