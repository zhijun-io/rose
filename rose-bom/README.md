# Rose BOM

消费方 **Bill of Materials**：对齐全部已发布 `io.zhijun` artifact 的版本。应用通过 `dependencyManagement` import 使用，**不**继承 `rose-parent` / `rose-build`。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-bom` | 单 POM（`packaging=pom`）；父 POM 为 `rose-build`（非 `rose-parent`，避免 import 循环） |

## 消费方式

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.zhijun</groupId>
            <artifactId>rose-bom</artifactId>
            <version>${rose.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- 版本由 BOM 管理，无需写 version -->
    <dependency>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-spring-boot-core</artifactId>
    </dependency>
</dependencies>
```

应用仍继承 `spring-boot-starter-parent`（或企业 parent）；Rose 只负责 `io.zhijun` 坐标对齐。第三方版本（Boot、Testcontainers、OTel 等）由 `rose-parent` 的 `dependencyManagement` 在构建时锁定，**不**进入 `rose-bom`。

## Managed artifacts（分组）

与 `rose-bom/pom.xml` 同步；新发布 artifact **仅须加入 `rose-bom`**（`rose-parent` 通过 import 继承）。

### Base

| Artifact | 说明 |
|----------|------|
| `rose-core` | 纯 Java 注解/契约 |
| `rose-spring-core` | Spring 扩展（属性源、env 刷新、配置绑定） |
| `rose-spring-boot-core` | Boot 工具 + 应用基线运行时（`RoseBinder`、`config/default/*`、诊断、`spring-boot-starter`） |
| `rose-spring-boot-actuator` | 调度监控等 Actuator 扩展 |

### Data & persistence

| Artifact | 说明 |
|----------|------|
| `rose-mybatis-plus-core` | 审计、加密、数据权限、租户 |
| `rose-mybatis-plus-spring-boot` | MyBatis-Plus Boot 自动装配 |

### Observability

| Artifact | 说明 |
|----------|------|
| `rose-observation-core` | AI observation conventions 冲突检测 |
| `rose-opentelemetry-core` | OTel SDK 自动配置 |
| `rose-opentelemetry-logback-bridge` | 日志桥接 |
| `rose-opentelemetry-micrometer-registry-otlp` | Micrometer → OTLP（默认指标路径） |
| `rose-opentelemetry-micrometer-metrics-bridge` | Micrometer → OTel SDK |
| `rose-opentelemetry-semantic-conventions` | 语义约定 |
| `rose-opentelemetry-spring-boot` | OTel Boot 自动装配 |

### Multitenancy

| Artifact | 说明 |
|----------|------|
| `rose-multitenancy-core` | 租户上下文 API（无 Spring） |
| `rose-multitenancy-spring` | Spring + Servlet/WebMVC 集成（无 Boot） |
| `rose-multitenancy-spring-boot` | Boot 自动装配 |

### Local services

| Artifact | 说明 |
|----------|------|
| `rose-devservice-core` | BootstrapMode、容器注册 |
| `rose-devservice-actuator` | `/actuator/devservices` |
| `rose-devservice-{postgresql,mysql,redis,mongodb,kafka,rabbitmq,artemis,activemq,ollama,mqtt,openlit,otel-collector}` | 连接器（`runtime` + `optional`） |
| `rose-devservice-tests` | 集成测试共享基类（`test` scope） |

### 不在 BOM 中

| Artifact | 原因 |
|----------|------|
| `rose-build` / `rose-parent` | 构建用，应用不 import |

## 已实现

- 上述全部 `io.zhijun` 坐标的**唯一维护点**（`dependencyManagement` 内 `${project.version}`）
- `rose-parent` 通过 `import` 引入本 BOM（reactor 构建）
- 发布时 `flatten-maven-plugin`（`flattenMode=bom`）生成可独立 import 的 POM（解析版本、移除 `properties` / `repositories` 等构建继承项）
- 根 `README.md` 消费契约说明

## 未实现 / 规划中

- artifact 重命名时的迁移坐标表（release notes / CHANGELOG）
- 版本兼容矩阵（Boot 2.7 / Java 8 当前唯一支持线）

## 对标 Arconia

Arconia 通过 Quarkus BOM / platform 管理版本。`rose-bom` 对应 **Spring Boot 应用侧** 依赖对齐，技术栈不同（Boot 2.7 vs Quarkus）。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-bom](https://github.com/microsphere-projects/microsphere-bom) | `rose-bom` | ✅ |
| [microsphere-spring-boot-dependencies](https://github.com/microsphere-projects/microsphere-spring-boot) | — | ❌ 不重复（`spring-boot-dependencies` + `rose-bom`） |

## 建议下一步

1. 模块重组时在 CHANGELOG 附 old→new 坐标对照
2. Boot 3 分支规划时评估是否拆 `rose-bom` 支持线或独立 BOM artifact
