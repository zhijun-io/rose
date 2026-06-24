# Rose DevService

Testcontainers-backed infrastructure for local development and testing ([Arconia-aligned](https://docs.arconia.io/arconia/latest/dev-services/)).

## 迭代导航

### 子模块

| 模块 | Artifact | 角色 |
|------|----------|------|
| Core | `rose-devservice-core` | API、`BootstrapMode`、Testcontainers 工具（无 Boot 自动配置） |
| Spring Boot | `rose-devservice-spring-boot` | 注册、`addDynamicProperty`、Bootstrap / 全局 AutoConfiguration |
| Test | `rose-devservice-test` | 集成测试共享支持（`test` scope） |
| Actuator | `rose-devservice-spring-boot-actuator` | `/actuator/devservices`（`@ConditionalOnDevMode`） |
| Connectors | `rose-devservice-spring-boot-{tech}` | 按技术可选 `runtime` 依赖 |

**连接器（reactor）**：`postgresql`、`mysql`、`redis`、`mongodb`、`kafka`、`rabbitmq`、`artemis`、`activemq`、`ollama`、`mqtt`、`openlit`、`otel`。

> 无 `rose-devservice-spring`：Dev Services 面向 Boot 生命周期，共享装配在 `rose-devservice-spring-boot`。

### 包名

| 模块 | 根包 |
|------|------|
| `rose-devservice-core` | `io.zhijun.devservice.api.*`、`io.zhijun.devservice.bootstrap.*`、`io.zhijun.devservice.core.{container,docker,util}` |
| `rose-devservice-spring-boot` | `io.zhijun.devservice.core.{autoconfigure,registration}`、`io.zhijun.devservice.autoconfigure.bootstrap.*` |
| `rose-devservice-spring-boot-actuator` | `io.zhijun.devservice.actuator.*` |
| `rose-devservice-spring-boot-{tech}` | `io.zhijun.devservice.{tech}.*` |

Artifact 与 Java 根包均为 `devservice`（`io.zhijun.devservice.*`）。`*.core.autoconfigure` 类位于 `spring-boot` 模块、包名仍用 `core`，与 `rose-multitenancy` 一致。

### 已实现

- Testcontainers 生命周期与 `BootstrapMode`（DEV / TEST / PROD）
- `DevServiceRegistrar.addDynamicProperty`（最高优先级动态属性）
- Docker 环境检测（OrbStack / 默认 socket）
- 各连接器 AutoConfiguration + 集成测试
- `MultipleDevServiceFailureAnalyzer`
- 连接器 `config/default/*.properties` 静态推荐默认

### 未实现 / 规划中

- `@ServiceConnection`（Boot 2.7 不可用；Arconia Boot 3 路径待对齐）
- 连接器**并行启动**（bootstrap-diagnostics §8.1 远期）
- Docker 不可用等专用 `FailureAnalyzer`
- 更多连接器（按业务需求扩展）

### 对标 Arconia

| Arconia Dev Services | Rose                         | 状态 |
|----------------------|------------------------------|------|
| 开发/测试自动起容器 | Testcontainers 连接器           | ✅ |
| 动态配置注入 | `addDynamicProperty`         | ✅ |
| `config/default` 式静态默认 | 连接器 `config/default/*` + EPP | ✅ |
| `@ServiceConnection` | —                            | ❌ Boot 3 |
| Quarkus Dev UI | Actuator `/devservices`      | ⚠️ 部分（Boot Actuator） |

### 对标 Microsphere

Microsphere **无** Dev Services 对标。Rose 本主题主要对齐 **Arconia**；启动诊断模式参考 microsphere-spring-boot `FailureAnalyzer`。

### 建议下一步

1. 补齐 Docker 不可用 `FailureAnalyzer` 与运维文档
2. 评估高频连接器的并行启动与启动耗时
3. Boot 3 路线明确后实现 `@ServiceConnection` 或等价抽象

---

## BootstrapMode (`rose-devservice-core`)

| Mode | Detection | Behavior |
|------|-----------|----------|
| `TEST` | JUnit / `@SpringBootTest` stack | Containers are not reused |
| `DEV` | DevTools / local `bootRun` | `shared=true` enables reuse |
| `PROD` | Default | Production (connectors must not be on classpath) |

Override: `-Drose.bootstrap.mode=dev|test|prod`

## Enabling

**Tests**: connector on `test` classpath; `rose.dev.*.enabled=true` by default.

**Local development**: `runtime` + `optional` (or Gradle `testAndDevelopmentOnly`); `rose.bootstrap.mode=dev`.

**Actuator**: add `rose-devservice-spring-boot-actuator` when Spring Boot Actuator is on the classpath.

## 迁移（artifact 重命名）

| 旧 | 新 |
|----|-----|
| `rose-devservice-actuator` | `rose-devservice-spring-boot-actuator` |
| `rose-devservice-postgresql` 等 | `rose-devservice-spring-boot-postgresql` 等 |
| Boot 装配（原在 core） | `rose-devservice-spring-boot`（共享层，每域一个） |

## Module defaults

Connector modules may ship static recommendations under `src/main/resources/config/default/<name>.properties`.
They are merged into Spring Boot `defaultProperties` by `rose-spring-boot-core`.
Runtime connection values use `DevServicesRegistrar.addDynamicProperty` (highest precedence during dev/test).

To disable conflicting Spring Boot auto-configuration from a connector, use `rose.autoconfigure.exclude` in the same file (see `rose-spring-boot` README).
