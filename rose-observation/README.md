# Rose Observation

统一可观测性域：conventions 契约与选型、OpenTelemetry SDK 集成、日志/指标桥接。

## 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-observation-core` | `TelemetryConventionsBackend` 契约（零依赖） |
| **Boot** | `rose-observation-spring-boot` | **默认栈**：conventions 选型 + OTel SDK + logback + OTLP metrics + conventions |
| OTel SDK | `rose-observation-spring-boot-otel` | 仅 SDK Boot 自动配置 |
| Logback | `rose-observation-spring-boot-logback` | 日志 → OTel |
| Micrometer OTLP | `rose-observation-spring-boot-micrometer-otlp` | Micrometer → OTLP（默认指标路径） |
| Micrometer Bridge | `rose-observation-spring-boot-micrometer-bridge` | Micrometer → OTel SDK（可选） |
| Conventions | `rose-observation-spring-boot-conventions-otel` | OTel conventions backend |

命名与 `rose-devservice-spring-boot-{tech}` 一致：slice 用 **具体能力**（`logback`、`micrometer-otlp`），不重复 `otel` 前缀；仅 SDK 与 conventions 保留 `otel` 以区分未来其他 backend。

## 消费方式

**推荐（默认栈）：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-observation-spring-boot</artifactId>
</dependency>
```

**仅 SDK：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-observation-spring-boot-otel</artifactId>
</dependency>
```

## Conventions 选型

```yaml
rose:
  observation:
    conventions:
      backend: opentelemetry
```

## 配置前缀

| 前缀 | 说明 |
|------|------|
| `rose.observation.*` | 总开关与 conventions 选型 |
| `rose.observation.conventions.otel.*` | OTel conventions slice |
| `rose.otel.*` | OTel SDK |

## 包名

| Artifact | 包根 |
|----------|------|
| `rose-observation-core` | `io.zhijun.observation` |
| `rose-observation-spring-boot` | `io.zhijun.observation.autoconfigure` |
| `rose-observation-spring-boot-otel` | `io.zhijun.observation.autoconfigure.otel` |
| `rose-observation-spring-boot-logback` | `io.zhijun.observation.autoconfigure.logback` |
| `rose-observation-spring-boot-micrometer-otlp` | `io.zhijun.observation.autoconfigure.micrometer.otlp` |
| `rose-observation-spring-boot-conventions-otel` | `io.zhijun.observation.autoconfigure.conventions.otel` |
