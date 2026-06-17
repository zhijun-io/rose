package io.zhijun.data.excel.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cn.idev.excel.FastExcel;
import cn.idev.excel.annotation.ExcelProperty;
import io.zhijun.data.excel.ReadFailMessageAware;
import io.zhijun.data.excel.listener.ReadValidateListener;

class RoseExcelHelperTests {

	@TempDir
	File tempDir;

	@Test
	void shouldWriteRowsToExcelFile() throws Exception {
		PocRow row = new PocRow();
		row.setName("张三");
		row.setPhone("13800138000");

		File outFile = RoseExcelHelper.writeExcelToFile("sample.xlsx", "用户", PocRow.class, Arrays.asList(row),
				target -> Files.newOutputStream(target.toPath()));

		assertThat(outFile).isNotNull().exists();
		List<PocRow> loaded = FastExcel.read(outFile).head(PocRow.class).sheet().doReadSync();
		assertThat(loaded).hasSize(1);
		assertThat(loaded.get(0).getPhone()).isEqualTo("13800138000");
	}

	@Test
	void shouldValidateImportRowsAndSaveFailFile() throws Exception {
		ByteArrayOutputStream workbook = new ByteArrayOutputStream();
		PocRow valid = new PocRow();
		valid.setName("张三");
		valid.setPhone("13800138000");
		PocRow invalid = new PocRow();
		invalid.setName("");
		invalid.setPhone("13800138000");
		FastExcel.write(workbook, PocRow.class).sheet("用户").doWrite(Arrays.asList(valid, invalid));

		ReadValidateListener<PocRow> listener = new ReadValidateListener<PocRow>((success, fail) -> {
		});
		File failFile = RoseExcelHelper.importAndSaveFail(new ByteArrayInputStream(workbook.toByteArray()), "users.xlsx",
				"用户", PocRow.class, listener);

		assertThat(listener.getSuccessData()).hasSize(1);
		assertThat(listener.getFailData()).hasSize(1);
		assertThat(listener.getFailData().get(0).getMessage()).contains("姓名");
		assertThat(failFile).isNotNull().exists();
	}

	public static class PocRow implements ReadFailMessageAware {
		@ExcelProperty("姓名")
		@NotBlank(message = "姓名不能为空")
		private String name;

		@ExcelProperty("手机号")
		private String phone;

		private String message;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public void setMessage(String message) {
			this.message = message;
		}
	}
}
