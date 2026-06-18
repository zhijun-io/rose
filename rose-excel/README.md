# Rose Excel

基于 [FastExcel](https://github.com/dromara/fastexcel) 的 Excel **导入导出**工具库，无 Spring 自动配置。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-excel` | 单 jar |

## 已实现

| 能力 | 说明 |
|------|------|
| `RoseExcelHelper` | 读写入口 |
| `ReadValidateListener` / `ReadMergeValidateListener` | 导入校验与失败消息收集 |
| `ImportResult` | 导入结果封装 |
| `MergeByPrimaryKeyStrategy` / `ExcelMergeHelper` | 按主键合并单元格 |
| `LocalDateTimeConverter` / `IsEnableConverter` | 类型转换 |
| `@ExcelExplicitConstraint` / `ExplicitConstraintHelper` | 下拉约束 |
| `CustomSheetWriteHandler` | 写出扩展 |

## 消费方式

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-excel</artifactId>
</dependency>
```

无 starter；在业务代码中直接使用 `RoseExcelHelper`、listener 与 converter。版本由 `rose-bom` 管理。

## 未实现 / 规划中

- Spring Boot starter / 自动配置
- `rose/default` 模块默认配置
- 与 multitenancy / 数据权限的 Excel 导出联动
- 大文件流式导入的性能与内存文档

## 对标 Arconia

无 Excel 相关能力。

## 对标 Microsphere

无直接对应模块。数据导入导出在 Rose 中保持 **独立薄库**，不纳入 microsphere-mybatis 等持久化主题。

## 建议下一步

1. 若应用侧重复配置多，可增加可选 `rose-excel-spring-boot-starter`（仅 `@Bean` 工厂，非必须）
2. 补充 1～2 个端到端导入示例（含校验失败回写）
