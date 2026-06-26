# Rose Foundation

平台基础层聚合（`packaging=pom`），对标 microsphere-java 的注解 / core / test / processor 分包。

| Artifact | 包名 | 说明 |
|----------|------|------|
| `rose-annotation` | `io.zhijun.annotation` | 零依赖契约注解（`@Since`、`@Incubating`、`@Internal` 等） |
| `rose-core` | `io.zhijun.core` | 框架无关工具 |
| `rose-annotation-processor` | `io.zhijun.annotation.processor` | `SinceProcessor` / `InternalApiProcessor` |

命名：artifact / 包名已带 `rose` 时，类名不再加 `Rose` 前缀（除非消歧必需）。
| `rose-test` | `io.zhijun.test` | 跨主题测试工具（`test` scope） |

域内测试支持（如 `rose-devservice-test`）仍留在各能力域，不并入 `rose-test`。
