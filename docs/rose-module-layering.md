# Rose 模块分层与命名约定

> **定位：** 固化 Rose 仓库的模块分层规则、artifactId ↔ 包名映射与聚合/拆分策略，供贡献者新增模块时遵循。

## 1. 三层分层

各能力域按 `core` / `spring` / `spring-boot` 三层组织，职责自下而上递进：

| 层 | artifactId 后缀 | 职责 | 上游依赖 |
|----|------------------|------|----------|
| 核心 | `-core` | 纯逻辑，不依赖 Spring | `rose-core` / 三方库 |
| Spring 集成 | `-spring` | 对 Spring Framework 的扩展（非 Boot） | 对应 `-core` |
| Boot 自动配置 | `-spring-boot` | `AutoConfiguration` / starter 装配 | 对应 `-spring` + `rose-spring-boot-core` |

`rose-devservice-core` 遵循「无 Spring」约定（仅 Testcontainers + SLF4J）；Bootstrap 检测使用 `io.zhijun.devservice.core.util` 工具类。

跨能力公共基础：

- `rose-annotation`（`io.zhijun.annotation`）—— 契约注解
- `rose-core`（`io.zhijun.core`）—— 无 Spring 的工具实现
- `rose-test`（`io.zhijun.test`）—— 跨主题测试工具（`test` scope）
- `rose-spring-core`（`io.zhijun.spring.core`）—— 对 Spring Framework 的扩展：`binder` / `env` / `propertysource`
- `rose-spring-boot-core`（`io.zhijun.boot`）—— 将上述能力桥接到 Boot：`Binder` 适配 / `EnvironmentPostProcessor` / 启动诊断

> `rose-spring-core` 与 `rose-spring-boot-core` 是分层而非重叠：前者提供机制，后者做 Boot 适配。

## 2. artifactId ↔ 包名映射

| artifactId 段 | Java 包名段 |
|---------------|-------------|
| `rose-annotation` | `io.zhijun.annotation` |
| `rose-core` | `io.zhijun.core` |
| `rose-test` | `io.zhijun.test` |
| `rose-spring-*` | `io.zhijun.spring.*` |
| `rose-spring-boot-*` | `io.zhijun.boot.*` |
| `rose-<cap>-core` | `io.zhijun.<cap>.core` |
| `rose-<cap>-spring` | `io.zhijun.<cap>.spring` |
| `rose-<cap>-spring-boot` | `io.zhijun.<cap>.boot` |

> `-spring-boot` 统一映射为 `.boot`（而非 `.spring.boot`），保持包名简短。

## 3. 聚合 vs 拆分

能力域内部有两种组织模式：

| 模式 | 适用场景 | 代表 |
|------|----------|------|
| 内聚分包 | 能力轻量、子能力无重依赖，单 starter 分包承载 | `rose-multitenancy`、`rose-mybatis-plus` |
| 独立拆模块 | 子能力带重依赖或需按需引入，拆为独立 `-spring-boot-*` | `rose-observation`（otel / logback / micrometer-*）、`rose-devservice`（按中间件） |

判断标准：

- 子能力引入**重依赖**（OTel SDK、Testcontainers、中间件 client）→ 拆独立模块，避免污染核心
- 子能力**轻量且强相关** → 内聚分包
- 需**一键全栈** → 额外建聚合模块（如 `rose-observation-spring-boot` 聚合各 slice，子模块亦可单选）

**Maven parent 链：** 域内叶子模块的 `<parent>` 必须指向域聚合 POM（如 `rose-multitenancy`）；`rose-foundation` 下叶子指向 `rose-foundation`；域聚合与 `rose-foundation` 直挂 `rose-parent`。

## 4. 特例与已知决策

- **observation 省略 `-spring` 层**：OTel 配置天然绑定 Spring Boot `AutoConfiguration`，无独立 Spring Framework 层需求，故 `core` 之后直接 `-spring-boot`。
- **`rose-devservice-test` 命名**：devservice 集成测试共享支持库，`-test` 后缀沿用 `spring-boot-starter-test` / `spring-boot-test-autoconfigure` 先例表示"测试支持"，不强制加 `-spring-boot` 后缀。
- **Actuator 端点包**：`rose-devservice-spring-boot-actuator` 的端点类位于 `io.zhijun.devservice.boot.actuator`（仍遵循 `-spring-boot` → `.boot` 映射）。
- **`rose-mybatis-plus` → `io.zhijun.mybatisplus`**：Java 包名不含连字符，与 mybatis-plus 官方包名一致。

## 5. 模块清单

构建与版本：

- `rose-build` —— 构建父（插件 / profile），不对外发布
- `rose-bom` —— 版本 BOM，消费者 `import`（未实现模块不进 BOM）
- `rose-parent` —— reactor 构建；对 **仅 reactor 内** 的占位模块在 `dependencyManagement` 中单独钉版本

公共基础：

- `rose-foundation`：`rose-annotation`、`rose-core`、`rose-annotation-processor`、`rose-test`
- `rose-spring`：`rose-spring-core`；`rose-spring-web`（占位，**不在 BOM**）
- `rose-spring-boot`：`rose-spring-boot-core`、`rose-spring-boot-actuator`；`rose-spring-boot-web`（占位，**不在 BOM**）

能力域：

- `rose-mybatis-plus`：`-core` / `-spring` / `-spring-boot`
- `rose-observation`：`-core`、`-spring-boot`（聚合）、`-spring-boot-otel` / `-logback` / `-micrometer-otlp` / `-micrometer-bridge` / `-conventions-otel`
- `rose-multitenancy`：`-core` / `-spring` / `-spring-boot`
- `rose-devservice`：`-core`、`-spring-boot`、`-test`、`-spring-boot-{actuator, postgresql, mysql, redis, mongodb, kafka, rabbitmq, artemis, activemq, ollama, mqtt, openlit, otel}`

## 6. Java 类命名

Maven **artifactId** / **包名**已体现 Rose（`rose-*`、`io.zhijun.*`）时，**类名不再加 `Rose` 前缀**，除非消歧必需。

| 建议 | 示例 |
|------|------|
| 注解 / Processor | `SinceProcessor`、`InternalApiProcessor` |
| 模块内 Testcontainers 包装 | `PostgresqlContainer`（位于 `...postgresql` 包） |
| 与三方类同名时加场景后缀 | `DevServiceKafkaContainer`（避免与 `KafkaContainer` 混淆） |
| 保留 `Rose` 前缀 | 历史对外 API（如 `RoseBinder`）、CHANGELOG 已发布的类型 |

配置键命名空间仍为 **`rose.*`**，与类名是否带 `Rose` 无关。
