# Rose BOM

消费方 **Bill of Materials**：对齐全部已发布 `io.zhijun` artifact 的版本，应用通过 `dependencyManagement` import 使用。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-bom` | 单 POM（`packaging=pom`），无子模块 |

## 已实现

- 管理 core / spring / spring-boot / excel / sqlite / mybatis-plus / observation / opentelemetry / multitenancy / dev-services 等全部发布坐标
- 与 `rose-parent` reactor 模块列表保持同步（新 artifact 须同时加入 parent 与 bom）
- 根 `README.md` 已说明消费契约：应用继承 `spring-boot-starter-parent` + import `rose-bom`，**不**继承 `rose-parent` 或 `rose-build`

## 未实现 / 规划中

- BOM 自身的用法示例与 artifact 分组说明（目前分散在根 README）
- artifact 重命名时的迁移坐标表（release notes / upgrade doc）
- 可选：按主题分组的 BOM import 片段（类似 Spring Cloud BOM 文档风格）

## 对标 Arconia

Arconia 通过 Quarkus BOM / platform 管理版本。Rose 的 `rose-bom` 对应 **Spring Boot 应用侧** 的依赖对齐，与 Arconia platform BOM 角色类似，但技术栈不同（Boot 2.7 vs Quarkus）。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-bom](https://github.com/microsphere-projects/microsphere-bom) | `rose-bom` | ✅ 已有 |
| [microsphere-spring-boot-dependencies](https://github.com/microsphere-projects/microsphere-spring-boot) | — | ❌ 不重复（Rose 用 `spring-boot-dependencies` + `rose-bom` 双层） |

## 建议下一步

1. 在本 README 增加「Managed artifacts 分组表」（Base / Data / Observability / Multitenancy / Dev Services）
2. 每次模块重组时在 CHANGELOG 附 old→new 坐标对照
