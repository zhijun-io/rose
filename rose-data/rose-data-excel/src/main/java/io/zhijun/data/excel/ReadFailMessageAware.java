package io.zhijun.data.excel;

/**
 * Marks import rows that can carry validation failure messages.
 */
public interface ReadFailMessageAware {

	void setMessage(String message);
}
