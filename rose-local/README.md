# Rose Local

Testcontainers-backed infrastructure for local development and testing ([Arconia-aligned](https://docs.arconia.io/arconia/latest/dev-services/)).

## 迭代导航

### 子模块

| 模块 | Artifact | 角色 |
|------|----------|------|
| Core | `rose-local-core` | API、`BootstrapMode`、容器注册、`addDynamicProperty` |
| Actuator | `rose-local-actuator` | `/actuator/devservices`（`@ConditionalOnDevMode`） |
| Connectors | `rose-local-{tech}` | 按技术可选 `runtime` 依赖 |
| Tests | `rose-local-tests` | 集成测试共享支持（test scope） |

**连接器（reactor）**：`postgresql`、`mysql`、`redis`、`mongodb`、`kafka`、`rabbitmq`、`artemis`、`activemq`、`ollama`、`mqtt`、`openlit`、`otel-collector`。

> 目录中存在 `rose-local-api` / `rose-local-bootstrap` 源码，逻辑已并入 core，**未列入** reactor。

### 已实现

- Testcontainers 生命周期与 `BootstrapMode`（DEV / TEST / PROD）
- `DevServicesRegistrar.addDynamicProperty`（最高优先级动态属性）
- Docker 环境检测（OrbStack / 默认 socket）
- 各连接器 AutoConfiguration + 集成测试
- `MultipleDevServicesFailureAnalyzer`
- 连接器 `config/default/*.properties` 静态推荐默认

### 未实现 / 规划中

- `@ServiceConnection`（Boot 2.7 不可用；Arconia Boot 3 路径待对齐）
- 连接器**并行启动**（bootstrap-diagnostics §8.1 远期）
- Docker 不可用等专用 `FailureAnalyzer`
- 更多连接器（按业务需求扩展）

### 对标 Arconia

| Arconia Dev Services | Rose | 状态 |
|----------------------|------|------|
| 开发/测试自动起容器 | Testcontainers 连接器 | ✅ |
| 动态配置注入 | `addDynamicProperty` | ✅ |
| `config/default` 式静态默认 | 连接器 `config/default/*` + EPP | ✅ |
| `@ServiceConnection` | — | ❌ Boot 3 |
| Quarkus Dev UI | Actuator `/devservices` | ⚠️ 部分（Boot Actuator） |

### 对标 Microsphere

Microsphere **无** Dev Services 对标。Rose 本主题主要对齐 **Arconia**；启动诊断模式参考 microsphere-spring-boot `FailureAnalyzer`。

### 建议下一步

1. 补齐 Docker 不可用 `FailureAnalyzer` 与运维文档
2. 评估高频连接器的并行启动与启动耗时
3. Boot 3 路线明确后实现 `@ServiceConnection` 或等价抽象

---

## BootstrapMode (`rose-local-core`)

| Mode | Detection | Behavior |
|------|-----------|----------|
| `TEST` | JUnit / `@SpringBootTest` stack | Containers are not reused |
| `DEV` | DevTools / local `bootRun` | `shared=true` enables reuse |
| `PROD` | Default | Production (connectors must not be on classpath) |

Override: `-Drose.bootstrap.mode=dev|test|prod`

## Enabling

**Tests**: connector on `test` classpath; `rose.dev.services.*.enabled=true` by default.

**Local development**: `runtime` + `optional` (or Gradle `testAndDevelopmentOnly`); `rose.bootstrap.mode=dev`.

**Actuator**: add `rose-local-actuator` when Spring Boot Actuator is on the classpath.

## Module defaults

Connector modules may ship static recommendations under `src/main/resources/config/default/<name>.properties`.
They are merged into Spring Boot `defaultProperties` by `rose-spring-boot-core`.
Runtime connection values use `DevServicesRegistrar.addDynamicProperty` (highest precedence during dev/test).

To disable conflicting Spring Boot auto-configuration from a connector, use `rose.autoconfigure.exclude` in the same file (see `rose-spring-boot` README).
