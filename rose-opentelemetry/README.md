# Rose OpenTelemetry

OpenTelemetry SDK 集成、日志桥接、Micrometer OTLP 导出与语义约定。

## 迭代导航

### 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-opentelemetry-core` | SDK 自动配置（traces / metrics / logs / resource / exporter） |
| Logback | `rose-opentelemetry-logback-bridge` | 日志 → OTel |
| Micrometer | `rose-opentelemetry-micrometer-registry-otlp` | Micrometer → OTLP（**默认**指标路径） |
| Micrometer | `rose-opentelemetry-micrometer-metrics-bridge` | Micrometer → OTel SDK `MeterProvider`（可选） |
| Conventions | `rose-opentelemetry-semantic-conventions` | 共享语义属性 |
| Starter | `rose-opentelemetry-spring-boot-starter` | Actuator + OTLP runtime 聚合入口 |

### 已实现

- OTel SDK Spring Boot 自动配置
- Logback bridge
- 双 Micrometer 路径：OTLP registry（默认）vs SDK meter bridge（显式依赖）
- `config/default/opentelemetry.properties` 排除 Boot 3 `OtlpMetricsExportAutoConfiguration`（Boot 2.7 无害）
- Starter 含 Actuator + OTLP runtime

### 未实现 / 规划中

- Traces / Logs 详细配置指南与示例应用
- Boot 3 原生 OTLP 路径的正式支持（当前仅 forward-compatible exclude）
- 独立 `rose-logging` 主题（logging 超出 telemetry 时）
- SQL / HTTP 自动 instrumentation 套件（依赖 OTel instrumentation BOM 选型）

### 对标 Arconia

Arconia 通过 Quarkus OpenTelemetry 扩展提供可观测性。Rose 面向 **Spring Boot 2.7**，自行组装 OTel SDK + Micrometer，无 Quarkus 式单一扩展包。

### 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-logging](https://github.com/microsphere-projects/microsphere-logging) | logback-bridge 子模块 | ⚠️ 仅 OTel 相关 |
| microsphere-spring-boot 诊断/默认配置 | `config/default` + exclude | ✅ 协作 |

### 建议下一步

1. 补充 traces/logs 配置文档与最小可运行示例
2. 在 README 明确「何时选 OTLP registry vs SDK bridge」决策树（根 README 已有简表，可下沉）
3. Boot 3 分支时评估是否切换到 Boot 原生 OTLP export

---

## Module defaults

Rose OTLP metrics use `rose-opentelemetry-micrometer-registry-otlp` instead of Spring Boot's built-in OTLP metrics export.

Modules ship merged exclusions via `config/default/*.properties`:

| File | Excludes |
|------|----------|
| `rose-opentelemetry-core` → `opentelemetry.properties` | `OtlpMetricsExportAutoConfiguration` |
| `rose-opentelemetry-micrometer-registry-otlp` → `micrometer-registry-otlp.properties` | same (accumulates) |

Requires `rose-spring-boot-core` on the classpath for `config/default/*` / `META-INF/config/default/*` loading and `ConfigurableAutoConfigurationImportFilter`. The starter (`rose-opentelemetry-spring-boot-starter`) includes it transitively via `rose-spring-boot-starter`.

`OtlpMetricsExportAutoConfiguration` exists from Spring Boot 3 onward; the exclusion is forward-compatible and harmless on Boot 2.7.
