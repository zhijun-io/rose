package io.zhijun.data.excel.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;

import cn.idev.excel.FastExcel;
import io.zhijun.data.excel.ReadFailMessageAware;
import io.zhijun.data.excel.converter.LocalDateTimeConverter;
import io.zhijun.data.excel.listener.ReadValidateListener;

/**
 * FastExcel-based helpers mirroring legacy rose-excel file operations.
 */
public final class RoseExcelHelper {

	public static final LocalDateTimeConverter LOCAL_DATE_TIME_CONVERTER = new LocalDateTimeConverter();

	private RoseExcelHelper() {
	}

	public static File writeExcelToFile(String originalFilename, String sheetName, Class<?> headClass,
			Collection<?> data) {
		return writeExcelToFile(originalFilename, sheetName, headClass, data, null);
	}

	public static File writeExcelToFile(String originalFilename, String sheetName, Class<?> headClass,
			Collection<?> data, OutputStreamProvider outputStreamProvider) {
		if (data == null || data.isEmpty()) {
			return null;
		}
		String filename = String.format("导入失败_%s", originalFilename);
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File outFile = new File(tempDir, filename);
		try (OutputStream outputStream = openOutputStream(outFile, outputStreamProvider)) {
			FastExcel.write(outputStream, headClass)
					.registerConverter(LOCAL_DATE_TIME_CONVERTER)
					.sheet(sheetName)
					.doWrite(data);
			return outFile;
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to write excel file: " + outFile.getAbsolutePath(), ex);
		}
	}

	public static <T extends ReadFailMessageAware> File importAndSaveFail(InputStream inputStream, String originalFilename,
			String sheetName, Class<T> headClass, ReadValidateListener<T> listener) {
		FastExcel.read(inputStream, headClass, listener).sheet().doRead();
		return writeExcelToFile(originalFilename, sheetName, headClass, listener.getFailData());
	}

	private static OutputStream openOutputStream(File outFile, OutputStreamProvider provider) throws IOException {
		if (provider != null) {
			return provider.open(outFile);
		}
		return Files.newOutputStream(outFile.toPath());
	}

	@FunctionalInterface
	public interface OutputStreamProvider {
		OutputStream open(File targetFile) throws IOException;
	}
}
