# Rose 贡献约定

> **定位：** 全仓库唯一的实现约束文档。领域设计见 [design/](design/)；使用与构建见 [wiki/](../wiki/)。

## 1. 模块分层

各能力域按 `core` / `spring` / `spring-boot` 组织：

| 层 | artifactId 后缀 | 职责 | 上游依赖 |
|----|------------------|------|----------|
| 核心 | `-core` | 纯逻辑，不依赖 Spring Boot | `rose-core` / 三方库 |
| Spring 集成 | `-spring` | Spring Framework 扩展（非 Boot） | 对应 `-core` |
| Boot 自动配置 | `-spring-boot` | `AutoConfiguration` / starter | 对应 `-spring` + `rose-spring-boot-core` |

公共基础：`rose-annotation`、`rose-core`、`rose-test`、`rose-spring-core`、`rose-spring-boot-core`。

`rose-devservice-core` 无 Boot 自动配置（Testcontainers + SLF4J；必要时最小 `spring-core` 依赖须在 PR 说明）。

## 2. artifactId 与包名

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

## 7. 提交流程

- **Conventional Commits**：`feat:`、`fix:`、`docs:`、`chore:`、`refactor:` 等。
- **一 PR 一主题**；依赖升级由 Renovate 处理。
- 应用不继承 `rose-parent` / `rose-build`；消费方 `import rose-bom`（见 [wiki/rose-bom/Consumer-Guide.md](../wiki/rose-bom/Consumer-Guide.md)）。

## 8. Reactor 模块清单

| 域 | 模块 |
|----|------|
| 构建 | `rose-build`、`rose-parent`、`rose-bom` |
| Foundation | `rose-annotation`、`rose-core`、`rose-annotation-processor`、`rose-test` |
| Spring | `rose-spring-core` |
| Spring Boot | `rose-spring-boot-core`、`rose-spring-boot-actuator` |
| MyBatis-Plus | `-core` / `-spring` / `-spring-boot` |
| Observation | `-core`、`-spring-boot` 及 otel / logback / micrometer-* slice |
| Multitenancy | `-core` / `-spring` / `-spring-boot` |
| DevService | `-core`、`-spring-boot`、`-test`、`-spring-boot-{connector,actuator}` |

BOM 托管坐标完整列表见 [wiki/rose-bom/Consumer-Guide.md § BOM artifacts](../wiki/rose-bom/Consumer-Guide.md#bom-artifacts)（与 `rose-bom/pom.xml` 同步）。
