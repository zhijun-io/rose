# Rose 贡献约定

> **定位：** 全仓库唯一的实现约束文档。领域设计见 [design/](design/)；使用与构建见 [wiki/](../wiki/)。

## 1. 模块分层

各能力域按 `core` / `spring` / `spring-boot` 组织：

| 层 | artifactId 后缀 | 职责 | 上游依赖 |
|----|------------------|------|----------|
| 核心 | `-core` | 纯逻辑，不依赖 Spring Boot | `rose-annotation` / 三方库 |
| Spring 集成 | `-spring` | Spring Framework 扩展（非 Boot） | 对应 `-core` |
| Boot 自动配置 | `-spring-boot` | `AutoConfiguration` / starter | 对应 `-spring` + `rose-spring-boot-core` |

公共基础：`rose-annotation`、`rose-test`、`rose-spring-core`、`rose-spring-boot-core`。

`rose-devservice-core` 无 Boot 自动配置（Testcontainers + SLF4J；必要时最小 `spring-core` 依赖须在 PR 说明）。

## 2. artifactId 与包名

| artifactId 段 | Java 包名段 |
|---------------|-------------|
| `rose-annotation` | `io.zhijun.annotation` |
| `rose-test` | `io.zhijun.test` |
| `rose-spring-*` | `io.zhijun.spring.*` |
| `rose-spring-boot-*` | `io.zhijun.boot.*` |
| `rose-<cap>-core` | `io.zhijun.<cap>.core` |
| `rose-<cap>-spring` | `io.zhijun.<cap>.spring` |
| `rose-<cap>-spring-boot` | `io.zhijun.<cap>.boot` |
| `rose-<cap>-spring-boot-{slice}` | `io.zhijun.<cap>.boot.autoconfigure.{slice}.*` |

`-spring-boot` 映射为 `.boot`（非 `.spring.boot`）。配置键命名空间为 **`rose.*`**。

### Maven 命名

| 模式 | 含义 | 示例 |
|------|------|------|
| `rose-xxx-spring-boot` | 域级共享 Boot 装配 | `rose-multitenancy-spring-boot` |
| `rose-xxx-spring-boot-{slice}` | 可选 Boot slice | `rose-devservice-spring-boot-postgresql` |
| `rose-xxx-core` / `rose-xxx-spring` | 无 Boot 自动配置 | `rose-devservice-core` |

Dev Services 连接器保持 `rose-devservice-spring-boot-{tech}`，不缩短为 `rose-devservice-{tech}`。

## 3. 聚合与拆分

| 模式 | 适用 | 代表 |
|------|------|------|
| 内聚分包 | 轻量、强相关 | `rose-multitenancy`、`rose-mybatis-plus` |
| 独立 slice | 重依赖、按需引入 | `rose-observation`、`rose-devservice` 连接器 |

子能力带重依赖（OTel、Testcontainers、中间件 client）→ 独立模块。域内叶子 `<parent>` 指向域聚合 POM。

### 特例

- **observation** 省略 `-spring` 层：`core` 后直接 `-spring-boot`。
- **`rose-devservice-test`**：`-test` 表示测试支持库，不强制 `-spring-boot` 后缀。
- **`rose-mybatis-plus`** → 包名 `io.zhijun.mybatisplus`（与官方一致，无连字符）。

## 4. Java 类命名

artifactId / 包名已体现 Rose 时，**类名不加 `Rose` 前缀**（除非消歧）：

| 建议 | 示例 |
|------|------|
| Processor / 注解 | `SinceProcessor` |
| 模块内容器包装 | `PostgresqlContainer` |
| 与三方同名 | `DevServiceKafkaContainer` |
| 保留 `Rose` | 已发布对外 API（如 `RoseBinder`） |

## 5. 实现约束

### 克制新增

- 不随意添加 `*Utils`、`*Helper`、`*Support`。
- 新增 public 类或 public 静态方法前：是否已有三方 API？是否仅一处调用？
- 逻辑属领域模型时放在该类型上，而非新建 `util` 包。

**决策顺序：** 三方 API → 内联（一两行）→ 领域内聚 → 仅当 **≥2 处稳定复用** 才新抽象。

### 优先三方 API

| 场景 | 选用 |
|------|------|
| 字符串非空 | Commons Lang `StringUtils.isNotBlank` |
| 参数校验 | `Validate.notNull` / `Validate.notBlank` |
| 数值区间 | `Range.between(min, max).contains(n)` |
| 类路径探测 | Spring `ClassUtils.isPresent`（`-spring` / `-spring-boot`） |
| Docker 镜像名 | Testcontainers `DockerImageName.parse(name).getUnversionedPart()` |
| 环境 / 属性 | Spring `Environment`、`RoseBinder` |

```java
// ❌ 自研薄封装
DevServiceUtils.hasText(s);
ContainerUtils.isValidPort(port);

// ✅
Validate.notBlank(s, "message");
BaseDevServiceProperties.isFixedPort(port);
ClassUtils.isPresent("com.example.Foo", classLoader);
```

### Spring Boot 自动配置双注册

每个 `-spring-boot` 模块在保留 `META-INF/spring.factories` 的 `EnableAutoConfiguration` 条目的同时，**同步维护**：

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

- 两类文件列出**相同**的全限定 `AutoConfiguration` 类名（每行一个）。
- Spring Boot 2.7+ 优先读 `.imports`；Spring Boot 3 仅读 `.imports`；双注册为零成本 SB3 过渡。
- 新增 `AutoConfiguration` 时两处一起改；非自动配置条目（`EnvironmentPostProcessor`、`FailureAnalyzer` 等）仍只写在 `spring.factories`。

### `internal` 包边界

实现细节、非稳定 API 放在 `*.internal.*` 子包；公共 SPI 与 `@ConfigurationProperties` 留在对外包。与 `@Incubating` / `InternalApiProcessor` 互补——包名一眼可辨，处理器可校验 `internal` 不被模块外引用。

### 依赖兼容下界（BOM 对齐版本）

Rose 构建锁 **Spring Boot 2.7.18** / **Java 8+**。BOM 对齐版本附带以下升级约束（与 `renovate.json` 一致）：

| 依赖 | Rose 锁定 | 兼容下界 / 约束 |
|------|-----------|-----------------|
| Spring Boot | 2.7.18 | `<3.0.0`（Reactor 不验证 SB3） |
| Testcontainers | 1.21.4 | `1.x`；**2.x 与 SB2.7 Jackson 2.13 不兼容**（[testcontainers#11236](https://github.com/testcontainers/testcontainers-java/issues/11236)） |
| OpenTelemetry SDK | `${opentelemetry.version}` | 随 BOM；instrumentation BOM 须与 SDK 配套 |
| MyBatis-Plus | `${mybatis-plus.version}` | 随 `mybatis-plus-bom` |
| Micrometer / Logback / SLF4J | SB2.7 BOM 传递 | Renovate 禁止 major 跃迁（见 `renovate.json` `allowedVersions`） |

下游若自行覆盖版本，须自行验证与 SB2.7 的传递依赖是否冲突。

### Starter / 聚合模块

`-spring-boot` 模块承载 `AutoConfiguration`；**不得**在仅做依赖聚合的模块（如 `rose-spring-boot-web` 占位）写业务逻辑。当前 `rose-spring-boot-web` 仅 `package-info`，符合极薄 starter 原则。

## 6. 测试

| 插件 | 阶段 | 类名 | 命令 |
|------|------|------|------|
| Surefire | `test` | `*Test`、`*Tests`（排除 `*IT`） | `mvn test` |
| Failsafe | `verify` | `*IT` | `mvn verify` |

- 每新增 **public 类** 或 **public 静态方法**，须在**同一模块**补单元测试。
- 删除工具类时迁移或删除对应测试；委托三方库但契约变化时更新断言。
- 集成行为（Testcontainers、完整 Boot 上下文）用 `*IT`，不替代单元测试。
- 新 dev-service 连接器须含 `*IT`。

构建 profile 与 CI 详见 [wiki/rose-build/Profiles-Management.md](../wiki/rose-build/Profiles-Management.md)、[wiki/rose-build/CI-CD-Integration.md](../wiki/rose-build/CI-CD-Integration.md)。

## 7. 编译与静态检查

- `maven-compiler-plugin`：`-parameters`、`-Xlint:deprecation`、`-Werror`（弃用 API 编译即失败）。
- 格式化 / Error Prone 门禁见 [inspiration-sdk-java.md](inspiration-sdk-java.md) 第二批规划。

## 8. 提交流程

- **Conventional Commits**：`feat:`、`fix:`、`docs:`、`chore:`、`refactor:` 等。
- **一 PR 一主题**；依赖升级由 Renovate 处理。
- 应用不继承 `rose-parent` / `rose-build`；消费方 `import rose-bom`（见 [wiki/rose-bom/Consumer-Guide.md](../wiki/rose-bom/Consumer-Guide.md)）。

## 9. Reactor 模块清单

| 域 | 模块 |
|----|------|
| 构建 | `rose-build`、`rose-parent`、`rose-bom` |
| Foundation | `rose-annotation`、`rose-annotation-processor`、`rose-test` |
| Spring | `rose-spring-core` |
| Spring Boot | `rose-spring-boot-core`、`rose-spring-boot-actuator` |
| MyBatis-Plus | `-core` / `-spring` / `-spring-boot` |
| Observation | `-spring-boot` 及 otel / logback / micrometer-* / conventions-otel slice |
| Multitenancy | `-core` / `-spring` / `-spring-boot` |
| DevService | `-core`、`-spring-boot`、`-test`、`-spring-boot-{connector,actuator}` |

BOM 托管坐标完整列表见 [wiki/rose-bom/Consumer-Guide.md § BOM artifacts](../wiki/rose-bom/Consumer-Guide.md#bom-artifacts)（与 `rose-bom/pom.xml` 同步）。
