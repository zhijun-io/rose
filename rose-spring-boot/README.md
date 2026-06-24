# Rose Spring Boot

Rose 的 Spring Boot 集成模块族，结构参考 [microsphere-spring-boot](https://github.com/microsphere-projects/microsphere-spring-boot)。

## 迭代导航

### 已实现（摘要）

| 子模块 | 能力 |
|--------|------|
| `rose-spring-boot-core` | `config/default/*` EPP、`RoseBinder` + `RoseBindListener`、`rose.autoconfigure.exclude` 累加、`ArtifactsCollisionDiagnosisListener`、`@ConditionalOnDevMode`、应用基线运行时（含 `spring-boot-starter`） |
| `rose-spring-boot-actuator` | `MonitoredThreadPoolTaskScheduler` + Micrometer |
| `rose-spring-boot-web` | Spring Web Boot 自动配置（端点注册表、Handler SPI，🚧 脚手架） |

### 未实现 / 规划中

- Boot 3 `@ServiceConnection` / Arconia Boot 3 连接注入路径
- 更多 `FailureAnalyzer`（Docker 不可用等，见 bootstrap-diagnostics 设计）
- Dev Services 连接器并行启动（远期）
- `microsphere-spring-boot-compatible`（Rose 锁定 Boot 2.7 / Java 8，暂不引入）

### 对标 Arconia

| Arconia | Rose |
|---------|------|
| Dev Services 默认配置 + 动态属性 | `config/default/*` + `addDynamicProperty`（见 `rose-devservice`） |
| `@ServiceConnection`（Boot 3） | ❌ Boot 2.7 仍用 `addDynamicProperty` |

### 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| spring-boot-core | `rose-spring-boot-core` | ✅ |
| spring-boot-actuator | `rose-spring-boot-actuator` | ✅ |
| BindListener / exclude 合并 / artifact 冲突 | 同上 core | ✅ |
| spring-boot-dependencies | `rose-bom` | ✅ 不重复 |
| spring-boot-compatible | — | ❌ 低优先级 |

### 建议下一步

1. 补充 Docker 不可用等 `FailureAnalyzer`（与 dev-services 联动）
2. Boot 3 分支规划时对齐 Arconia `@ServiceConnection` 语义
3. 保持横切能力进 core/actuator，业务 AutoConfiguration 不进本主题

---

## 模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| `rose-spring-boot` | `rose-spring-boot` (pom) | 聚合父 POM |
| `rose-spring-boot-core` | `rose-spring-boot-core` | 共享 Boot 工具 + 应用基线运行时 |
| `rose-spring-boot-actuator` | `rose-spring-boot-actuator` | 调度监控、诊断扩展（可选） |
| `rose-spring-boot-web` | `rose-spring-boot-web` | Web MVC Boot 自动配置（🚧 脚手架） |

## 消费方式

通过 `rose-bom` 对齐版本：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot-core</artifactId>
</dependency>
```

功能模块（`rose-*-spring-boot`）已传递依赖 `rose-spring-boot-core`，一般无需重复声明。

## 模块默认配置

各 Rose 模块可在 classpath 放置可覆盖的推荐默认值：

```
src/main/resources/config/default/<模块名>.properties
src/main/resources/config/default/<模块名>.yml    # 或 .yaml
```

启动时扫描 `classpath*:config/default/*` 与 `classpath*:META-INF/config/default/*`（`.properties`、`.yml`、`.yaml`），扁平化后合并进 Spring Boot `defaultProperties`（最低优先级，`application.yml` 可覆盖）。

| 配置 | 默认 | 说明 |
|------|------|------|
| `rose.default-config.enabled` | `true` | 总开关（见下方 **生效时机**） |
| `rose.default-config.locations` | — | 额外 location pattern，逗号分隔 |

### 生效时机

`DefaultConfigPropertiesEnvironmentPostProcessor` 在 `application.yml` 加载**之前**运行。因此：

- **可用**：`-Drose.default-config.enabled=false`、`ROSE_DEFAULT_CONFIG_ENABLED=false`、其他启动前环境变量/系统属性
- **通常无效**：仅在 `application.yml` 里写 `rose.default-config.enabled=false`

应用配置（`application.yml`、profile、环境变量等）仍可覆盖已加载的默认 **属性值**；总开关要在启动早期设置。

### 合并与冲突

- 多个 jar 的 `config/default/*` 会按 resource URL **字典序**合并，同 key **后者覆盖前者**
- 覆盖发生时打 **DEBUG** 日志：`Rose default config key '...' overridden in ...`
- **避免**多模块定义同一 key；文件名仅作识别，不决定优先级

### YAML 处理

加载器在代码层保证与 `.properties` 一致的行为：

- **类型**：YAML 叶子值规范化为 `String`（与 properties 相同），避免同 key 因 `18080` vs `"18080"` 产生绑定差异
- **列表**：扁平化为 Spring 索引键，如 `tags[0]`、`servers[0].host`
- **多文档**：`---` 分隔的多段 YAML 按顺序合并；非 map 根文档跳过并 **WARN**

`@PropertySource` 使用的 `YamlPropertySourceFactory` 仍保留 YAML 原生类型，仅 `config/default` 默认配置走规范化。

### 与 DevServices 的关系

对齐 [Arconia Dev Services](https://docs.arconia.io/arconia/latest/dev-services/)：

| 机制 | 说明 |
|------|------|
| `config/default/*`（EPP） | 静态推荐默认 → `defaultProperties` |
| `rose.autoconfigure.exclude` | 多模块合并排除 AutoConfiguration（见下文） |
| `setDefaultProperty` | 同上 |
| `addDynamicProperty` | 最高优先级，开发/测试时覆盖手动配置 |
| `BootstrapMode` | 见 `rose-devservice-core`；Boot 装配见 `rose-devservice-spring-boot` |
| Actuator 端点 | `rose-devservice-spring-boot-actuator`，`@ConditionalOnDevMode` |

Boot 2.7 暂无 `@ServiceConnection`，连接注入仍走 `addDynamicProperty`（Arconia Boot 3 路径后续再对齐）。

### 模块约定

- 每个模块一个文件，文件名建议与 artifact 对应（如 `dev-services.properties`）
- 只放跨应用、可安全覆盖的推荐默认项

`rose-spring-boot-core` 自带 `config/default/core.properties`（graceful shutdown 等）。

## 高级 Auto-Configuration 管理

借鉴 [microsphere-spring-boot](https://github.com/microsphere-projects/microsphere-spring-boot) 的 `ConfigurableAutoConfigurationImportFilter`。

### 问题

`spring.autoconfigure.exclude` 在多个模块各自声明时，后加载的 property source 会**覆盖**先前的值，无法安全地在各 Rose 模块的 `config/default/*.properties` 中分散声明排除项。

### 方案

| 属性 | 行为 |
|------|------|
| `rose.autoconfigure.exclude` | 各 property source **累加**；`config/default/*` 加载时同 key **逗号合并** |
| `spring.autoconfigure.exclude` | Spring Boot 原生机制，仍可用 |

`ConfigurableAutoConfigurationImportFilter`（`META-INF/spring.factories`）在自动配置导入阶段过滤 `rose.autoconfigure.exclude` 中的类。

### 示例

各模块在 `config/default/<module>.properties` 中声明：

```properties
rose.autoconfigure.exclude=\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

运行时追加：

```java
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(environment, className);
```

亦支持索引写法：`rose.autoconfigure.exclude[0]=...`、`rose.autoconfigure.exclude[1]=...`。

### 已登记模块

| 模块 | `config/default` 文件 | 排除项 |
|------|---------------------|--------|
| `rose-spring-boot-core` | `core.properties` | — |
| `rose-spring-boot-actuator` | `META-INF/config/default/actuator.properties` | —（`rose.actuator.task-scheduler.*` 推荐默认） |
| `rose-observability-spring-boot-otel` | `opentelemetry.properties` | `OtlpMetricsExportAutoConfiguration` |
| `rose-observability-spring-boot-micrometer-otlp` | `micrometer-registry-otlp.properties` | 同上（累加） |
| `rose-devservice-spring-boot-artemis` | `artemis.properties` | —（仅 `spring.artemis.mode` 推荐值） |

新模块如需排除 Boot 自动配置，在各自 `config/default/<module>.properties` 声明；勿在 `EnvironmentPostProcessor` 里拼接 `spring.autoconfigure.exclude`。

## 配置绑定监听（BindListener）

借鉴 Microsphere `BindListener`，在 `RoseBinder` 绑定期间接收回调：

```java
RoseBinder binder = RoseBinder.get(environment, listener);
binder.bindString("rose.example.value", "default");
```

底层通过 `ListenableBindHandlerAdapter` 包装 Spring Boot `BindHandler`，可与 `rose-spring-core` 环境刷新协作。

## Classpath Artifact 冲突检测

借鉴 Microsphere `ArtifactsCollisionDiagnosisListener`：

```properties
rose.diagnostics.artifacts-collision.enabled=true
```

启动时扫描 `META-INF/maven/**/pom.properties`，发现重复 `groupId:artifactId` 则抛出 `ArtifactsCollisionException`，并由 `ArtifactsCollisionFailureAnalyzer` 给出 `mvn dependency:tree` 建议。

默认 **关闭**（opt-in），避免对 fat-jar / 特殊 classloader 误报。

## 监控任务调度（Actuator Task Scheduler）

模块 `rose-spring-boot-actuator` 提供 Micrometer 增强的专用 `TaskScheduler` Bean（对齐 [microsphere-spring-boot-actuator](https://github.com/microsphere-projects/microsphere-spring-boot)）：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot-actuator</artifactId>
</dependency>
```

| 配置 | 默认 | 说明 |
|------|------|------|
| Bean 名 | `actuatorTaskScheduler` | 专用调度器，**不**替换应用默认 `TaskScheduler` |
| `rose.actuator.task-scheduler.pool-size` | `1` | 线程池大小（见 `META-INF/config/default/actuator.properties`） |
| `rose.actuator.task-scheduler.thread-name-prefix` | `rose-spring-boot-actuator-task-` | 线程名前缀 |

需要 classpath 上有 `MeterRegistry`（通常来自 Actuator + Micrometer）。无 `MeterRegistry` 时不注册该 Bean。

## 与 Microsphere 的对照

| Microsphere | Rose（当前/规划） | 借鉴优先级 |
|-------------|-------------------|------------|
| `microsphere-spring-boot-core` | `rose-spring-boot-core` | ✅ 已有 |
| `microsphere-spring-boot-actuator` | ✅ `rose-spring-boot-actuator` | 中（调度监控） |
| `microsphere-spring-boot-dependencies` | `rose-bom` | ✅ 已有，不重复 |
| `microsphere-spring-boot-compatible` | 暂不引入（Rose 锁定 Boot 2.7 / Java 8） | 低 |
| 默认 `config/default/*.properties` | ✅ `rose-spring-boot-core` | 中 |
| `BindListener` / 配置绑定监听 | ✅ `RoseBindListener` | 中 |
| `FailureAnalyzer` / classpath 冲突检测 | ✅ `ArtifactsCollisionDiagnosisListener` | 中 |
| `rose.autoconfigure.exclude` 合并排除 | ✅ `ConfigurableAutoConfigurationImportFilter` | 中 |

## 边界

- **不放**各主题业务 AutoConfiguration（见根 [README.md](../README.md#reference) 与下文 **边界**）
- **不放** Dev Services / BootstrapMode（见 `rose-devservice-core`）
- 新 Boot 横切能力优先进 `rose-spring-boot-core` 或 `rose-spring-boot-actuator`

## 相关文档

- [microsphere-benchmark-notes](../docs/microsphere-benchmark-notes.md)
- [bootstrap-diagnostics](../docs/rose-spring-boot-bootstrap-diagnostics-design.md)（FailureAnalyzer 规范，Bootstrap 章节已过时）
