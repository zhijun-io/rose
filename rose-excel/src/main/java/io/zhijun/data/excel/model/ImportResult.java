package io.zhijun.data.excel.model;

/**
 * Generic import summary for success / failure counts.
 */
public class ImportResult {

	private Integer success;

	private Integer fail;

	private String failUrl;

	public Integer getSuccess() {
		return success;
	}

	public ImportResult setSuccess(Integer success) {
		this.success = success;
		return this;
	}

	public Integer getFail() {
		return fail;
	}

	public ImportResult setFail(Integer fail) {
		this.fail = fail;
		return this;
	}

	public String getFailUrl() {
		return failUrl;
	}

	public ImportResult setFailUrl(String failUrl) {
		this.failUrl = failUrl;
		return this;
	}
}
