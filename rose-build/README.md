# Rose Build

Rose 仓库的 **Maven 构建父 POM**（`rose-build`），为所有模块提供统一的插件版本、覆盖率策略与发布配置。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-build` | 单 POM，无子模块；被 `rose-parent` 继承 |

相关但不在本目录：`rose-parent`（reactor 根）、`rose-coverage`（`-Pcoverage` profile 激活的 JaCoCo 聚合）。

## 已实现

- `${revision}` + `flatten-maven-plugin` 统一版本号
- JaCoCo / Surefire 测试与覆盖率基线
- Maven Enforcer 构建约束
- `release` profile：Javadoc、GPG、Central Publishing
- Java 8 基线、组织/SCM 元数据

## 未实现 / 规划中

- 面向贡献者的构建治理独立文档（与功能文档分离）
- `rose-coverage` 90% 行覆盖率门禁的例外说明文档化
- 多 JDK / 多 Boot 版本的 matrix 构建（Rose 当前锁定 Boot 2.7 / Java 8）

## 对标 Arconia

Arconia 为 Quarkus 生态的 Dev Services 与框架扩展，**无直接 Maven build parent 对标**。Rose 的构建层职责与 Arconia 的 Gradle/Maven 发布流程无一一对应关系。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-build](https://github.com/microsphere-projects/microsphere-build) | `rose-build` | ✅ 已有共享 build parent |
| 独立 coverage 模块 | `rose-coverage`（profile 激活） | ✅ 已有 |

## 建议下一步

1. 在根 `README.md` 或 `docs/releasing.md` 中补充「build / parent / bom 三者职责」速查表（benchmark notes §3.1 建议）
2. 若对外发布线增多，增加版本兼容矩阵文档（参考 microsphere-spring-cloud 的 matrix 写法）
