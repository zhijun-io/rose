# Rose Observation

**AI Observation Conventions** 的启动期校验：确保 classpath 上仅有一个 `AiObservationConventionsProvider` 实现。

## 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-observation-core` | `AiObservationConventionsProvider` API（无 Boot） |
| Boot | `rose-observation-spring-boot` | 冲突检测自动配置 + FailureAnalyzer |

## 依赖

**Spring Boot 应用（推荐）：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-observation-spring-boot</artifactId>
</dependency>
```

**仅 API（实现 Provider、无 Boot）：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-observation-core</artifactId>
</dependency>
```

## 已实现

| 能力 | 说明 |
|------|------|
| `AiObservationConventionsProvider` | 标记接口，各 AI 观测模块注册 Bean |
| `ObservationAutoConfiguration` | 启动时检测重复 Provider |
| `MultipleAiObservationConventionsFailureAnalyzer` | 冲突时给出可操作的启动失败说明 |
| `ObservationProperties` | `rose.observation.*` 配置 |

## 未实现 / 规划中

- 通用 Micrometer `ObservationRegistry` 封装（非 AI 专用）
- 与 `rose-opentelemetry` 的 trace 属性自动传播文档
- Web / MVC 层 observation 切面

## 对标 Arconia

Arconia 通过 Quarkus Observability 集成 OpenTelemetry。Rose 本模块范围很窄，仅解决 **多 AI conventions 冲突**，不等同于 Arconia 全栈可观测性。

## 对标 Microsphere

无直接 `microsphere-observation` 模块。诊断模式可参考 microsphere-spring-boot 的 `FailureAnalyzer` 体系（Rose 已在 dev-services、spring-boot 等多处采用）。

## 建议下一步

1. 明确本模块边界：保持「AI conventions 守门员」，不把通用观测逻辑塞进来
2. 若 AI 观测模块增多，在根 README 列出已知 `AiObservationConventionsProvider` 实现清单
