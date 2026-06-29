# sdk-java 代码实现借鉴清单

> 对比对象：[temporalio/sdk-java](https://github.com/temporalio/sdk-java) 代码实现
> 视角：**代码层面**（设计模式、API 设计、扩展点、错误处理、配置绑定），区别于 [inspiration-sdk-java.md](./inspiration-sdk-java.md) 的工程实践视角
> 日期：2026-06-29 · 基线：Rose `eb7fac0`

## 总览

sdk-java 是一个成熟的、面向扩展的 SDK，其代码实现有几条贯穿全局的设计哲学。Rose 作为同类「扩展平台」，可借鉴的实现模式如下，按价值排序：

| # | 借鉴点 | sdk-java 实现 | Rose 现状 | 价值 |
|---|--------|-------------|----------|------|
| 1 | 统一泛型 Customizer 基类 | `TemporalOptionsCustomizer<T>` + `@Order` | 各域各定义一套 Customizer | ★★★ |
| 2 | Options + Builder + 默认实例 | `XxxOptions.newBuilder().build()` + `getDefaultInstance()` | 无统一 Options 模式 | ★★ |
| 3 | 链式编解码（Chain of Responsibility） | `PayloadCodec` + `ChainCodec` | 加密是单一实现无链 | ★★ |
| 4 | 分层失败体系 | `TemporalFailure` 根 + 具体子类 + `ApplicationFailure` | 异常零散无根类 | ★★ |
| 5 | Template 拆分复杂自动装配 | autoconfigure 下 `template/` 包 | 自动装配类较胖 | ★★ |
| 6 | `@Experimental` 注解 `@Inherited` | 继承传播 + 跨类/方法/字段 | `@Incubating` 不继承 | ★ |
| 7 | 序列化上下文（Context Propagator） | `SerializationContext` 注入 codec | 加密无上下文 | ★ |
| 8 | `@FunctionalInterface` 标注单方法 SPI | customizer/codec 普遍标注 | 部分标注 | ★ |

---

## 1. 统一泛型 Customizer 基类 ★★★

**sdk-java 实现**

`sdk-java` 定义一个泛型函数式接口，覆盖所有可定制点：

```java
// temporal-spring-boot-autoconfigure/.../TemporalOptionsCustomizer.java
public interface TemporalOptionsCustomizer<T> {
    @Nonnull
    T customize(@Nonnull T optionsBuilder);
}
```

- 泛型 `T` 是被定制的 Options Builder 类型（`WorkflowClientOptions.Builder`、`WorkerOptions.Builder` 等）
- 多个 customizer bean 通过 `@Order` / `Ordered` 排序后依次应用
- 调用时机：Spring Boot 自动装配初始化 Builder 之后，配置值之后——customizer 优先级最高

**Rose 现状**

Rose 已有 Customizer 思想，但**每个域各定义一套**，无统一基类：

- `MybatisPlusInterceptorCustomizer.customize(MybatisPlusInterceptor)` — 定制拦截器实例
- `OpenTelemetryResourceBuilderCustomizer.customize(ResourceBuilder)` — 定制 builder
- `OpenTelemetryMeterProviderBuilderCustomizer` — 又一个
- `ConfigurationBeanCustomizer` — Spring 配置绑定定制

这些接口形状一致（单方法 `customize(T)`），但各自独立、无泛型抽象、无统一排序约定。

**借鉴建议**

抽一个统一泛型基类（Rose 已有 `@Incubating` 治理习惯，可加 `@FunctionalInterface`）：

```java
package io.zhijun.core.extension;

@FunctionalInterface
public interface OptionsCustomizer<T> {
    void customize(T target);
}
```

各域 customizer 继承它，并统一用 `@Order` 排序、统一用 BeanPostProcessor 或 `ObjectPostProcessor` 应用。价值：

- 消费者记住一个 SPI 而非每域一套
- 新增定制点零成本（实现 `OptionsCustomizer<XxxBuilder>` 即可）
- 统一文档与排序语义

注意：这是行为不变的纯抽象提取，可渐进——先定义基类，现有 customizer 逐步 `extends`。但涉及公共 API 形状，建议走 `sdd-spec` 定兼容策略。

---

## 2. Options + Builder + 默认实例 ★★

**sdk-java 实现**

所有配置对象遵循统一三件套：

```java
// temporal-sdk/.../RetryOptions.java
public final class RetryOptions {
    public static Builder newBuilder() { return new Builder(null); }
    public static Builder newBuilder(RetryOptions options) { ... }
    public static RetryOptions getDefaultInstance() { return DEFAULT_INSTANCE; }

    public static final class Builder {
        // 链式 setter，validate 后 build
        public RetryOptions build() { ... }
        public RetryOptions validateAndBuildWithDefaults() { ... }
    }
}
```

- 不可变值对象（`final` 字段，构造后只读）
- Builder 支持从已有实例复制（`newBuilder(options)`）做派生
- `getDefaultInstance()` 提供全默认单例，避免重复构造
- `validateAndBuildWithDefaults()` 填充默认值并校验不变量

**Rose 现状**

配置以 `@ConfigurationProperties` + setter 绑定为主（如 `OpenTelemetryTracingProperties`、`EncryptorProperties`），运行时配置对象可变。无统一的不可变 Options + Builder 模式用于「编程式构造」场景。

**借鉴建议**

Rose 是声明式（配置驱动）为主，未必需要全套 Options 模式。但对**会被代码直接构造的扩展点**（如未来的 `FieldEncryptor` 配置、DevService spec），可借鉴：不可变 + Builder + 默认实例。价值在于线程安全与可测试性。当前优先级中等——看是否有「配置 + 编程式」双入口的需求。

---

## 3. 链式编解码（Chain of Responsibility） ★★

**sdk-java 实现**

`PayloadCodec` 是单一编解码接口，`ChainCodec` 把多个 codec 串成链：

```java
// temporal-sdk/.../payload/codec/PayloadCodec.java
public interface PayloadCodec {
    List<Payload> encode(List<Payload> payloads);
    List<Payload> decode(List<Payload> payloads);
    default PayloadCodec withContext(SerializationContext context) { return this; }
}

// ChainCodec：encode 反序应用，decode 正序，保证可逆
public class ChainCodec implements PayloadCodec {
    public List<Payload> encode(List<Payload> payloads) {
        // last to first：外层 codec 包裹内层
    }
    public List<Payload> decode(List<Payload> payloads) {
        // first to last：反序解包
    }
}
```

- `ZlibPayloadCodec`（压缩）、加密 codec 等都是 `PayloadCodec` 实现，可任意组合
- `withContext` 让 codec 拿到序列化上下文（哪个 workflow/activity），做上下文相关加密

**Rose 现状**

`FieldEncryptor` 是单一接口、单一实现（`DefaultFieldEncryptor`），加密算法在 enum 里硬编码（BASE64/AES），无链式组合能力。若未来要「先压缩再加密」或「按字段选不同 codec」，当前结构需改。

**借鉴建议**

Rose 的字段加密若要演进，可借鉴 codec 链模式：定义 `FieldCodec` 接口（encode/decode），`ChainFieldCodec` 串联多个，每个 codec 独立可测。这与 sdk-java 的加密场景（`CodecDataConverter` 包 `PayloadCodec` 链）同构。但当前加密需求简单，属「演进储备」而非立即需要——建议等出现多 codec 需求再做。

---

## 4. 分层失败体系 ★★

**sdk-java 实现**

异常有明确根类与分类：

```
TemporalException (extends RuntimeException)
└── TemporalFailure (abstract, 跨边界传播)
    ├── ApplicationFailure (应用抛出，唯一可被用户抛的)
    ├── ActivityFailure / TimeoutFailure / CanceledFailure ...
    └── DefaultFailureConverter (序列化/反序列化)
```

- Javadoc 明确「Never extend this class...Don't throw any subtype except ApplicationFailure」——约束扩展面
- `ApplicationFailure` 带 `type`、`nonRetryable`、`category`、`nextRetryDelay`，支持重试策略
- 失败可序列化跨边界传播（workflow ↔ activity）

**Rose 现状**

异常零散，无统一根类：

- `TenantVerificationException` / `TenantNotFoundException`（multitenancy）
- `MultipleDevServiceException`（devservice）
- `ArtifactsCollisionException`（spring-boot-core）
- `AmbiguousConventionsBackendException` / `UnknownConventionsBackendException`（observation）

各自直接 extends `RuntimeException`，无共享基类，无统一错误码/分类约定。

**借鉴建议**

Rose 不像 sdk-java 有跨进程传播需求，但可借鉴：

- 定义 `RoseException` 基类（带 errorCode + 可选 context），各域异常继承
- 统一错误码命名空间（如 `ROSE_TENANT_NOT_FOUND`），便于日志聚合与监控告警
- 给「面向消费者的可预期异常」加 `@Incubating` 与文档，区分框架内部异常

价值：运维侧可按 errorCode 聚合，而非靠 message 正则。中等优先级，可在下次涉及异常的改动时渐进引入。

---

## 5. Template 拆分复杂自动装配 ★★

**sdk-java 实现**

`temporal-spring-boot-autoconfigure` 把复杂装配逻辑拆到 `template/` 包，每个 Template 负责一类对象的构造：

```
template/
├── ClientTemplate.java          // 构造 WorkflowClient + ScheduleClient
├── NamespaceTemplate.java      // 构造 namespace 级 worker
├── WorkersTemplate.java         // 构造 worker 集合
├── WorkflowClientOptionsTemplate.java
├── WorkerOptionsTemplate.java
└── ... (11 个 Template)
```

`@AutoConfiguration` 类只负责编排（注入 properties + customizer → 调 Template → 注册 bean），具体构造逻辑全在 Template 里。好处：AutoConfiguration 类薄、Template 可单测、复杂度分散。

**Rose 现状**

Rose 的 `@AutoConfiguration` 类直接含构造逻辑，如 `OpenTelemetryResourceAutoConfiguration` 内联 builder 组装。规模小时没问题，规模大时 AutoConfiguration 类会变胖、难单测。

**借鉴建议**

当某个 AutoConfiguration 类超过 ~150 行或内联多个 `@Bean` 的构造逻辑复杂时，拆出 Template：Template 持有构造所需依赖，暴露 `build()` 方法，AutoConfiguration 只编排。可先在 observation-otel（最大模块，77 main 文件）试点。纯结构重构，行为不变，可走 `sdd-build`。

---

## 6. `@Experimental` 注解 `@Inherited` ★

**sdk-java 实现**

```java
// @Experimental 带 @Inherited，父类标注则子类自动继承实验性
@Documented @Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
public @interface Experimental {}
```

用法广泛（118 处），且 `@Inherited` 让抽象基类标注后所有子类自动标记。

**Rose 现状**

`@Incubating` 无 `@Inherited`，50 处使用。若一个 `@Incubating` 抽象类有多个子类，每个子类需重复标注才被 `InternalApiProcessor` 识别。

**借鉴建议**

给 `@Incubating` 加 `@Inherited`（一行改动），让基类标注传播到子类。低成本、减少遗漏。需确认 `InternalApiProcessor` 是否已按继承链扫描——若没有，处理器也要适配。属小改进，可顺手做。

---

## 7. 序列化上下文（Context Propagator） ★

**sdk-java 实现**

`SerializationContext` 接口 + `WorkflowSerializationContext` / `ActivitySerializationContext`，让 codec 拿到「这次编解码属于哪个 workflow/activity」上下文，做上下文相关处理（如按 workflow 用不同密钥）。

**Rose 现状**

`FieldEncryptProcessor` 调 `encryptor.encrypt(algorithm, secret, rawValue)`，secret 来自 `EncryptionKeyResolver.resolve(secretRef)`——已有按 secretRef 选密钥的能力，但无「当前实体/字段」级别的上下文注入。

**借鉴建议**

Rose 的 `EncryptionKeyResolver` 已能按 `secretRef` 选密钥，覆盖了大部分需求。若未来要「按租户/实体用不同密钥」，可借鉴 `SerializationContext` 注入字段级上下文。当前需求不足，属演进储备。

---

## 8. `@FunctionalInterface` 标注单方法 SPI ★

**sdk-java 实现**

单方法 SPI 接口普遍标注 `@FunctionalInterface`（如 customizer、codec 接口），明确契约 + 支持 lambda。

**Rose 现状**

部分单方法接口标注了（如 `OpenTelemetryResourceBuilderCustomizer` 标 `@FunctionalInterface`），部分没标（如 `MybatisPlusInterceptorCustomizer` 是单方法但未标）。

**借鉴建议**

统一给所有单方法 SPI 标 `@FunctionalInterface`。极低成本，提升一致性与 lambda 友好性。可顺手做，或在 #1 统一基类时一并处理。

---

## 落地建议

**立即可做（低成本、行为不变）**

- #6 `@Incubating` 加 `@Inherited` + 校验处理器适配
- #8 单方法 SPI 统一标 `@FunctionalInterface`

**需要设计（涉及 API 形状）**

- #1 统一 `OptionsCustomizer<T>` 基类 → 走 `sdd-spec` 定兼容与迁移策略
- #4 `RoseException` 根类 + errorCode → 涉及多域，建议先 spec
- #5 Template 拆分 → 纯结构重构，可走 `sdd-build`（observation-otel 试点）

**演进储备（等需求出现）**

- #2 Options + Builder（看编程式构造需求）
- #3 链式 codec（看多 codec 需求）
- #7 序列化上下文（看按租户加密需求）

---

## 与工程实践清单的关系

本文档聚焦**代码实现**（设计模式、API 形状、扩展点设计），与 [inspiration-sdk-java.md](./inspiration-sdk-java.md) 互补：

- 工程实践清单：CI、构建、发版、依赖治理、文档
- 代码实现清单（本文）：Customizer SPI、Options 模式、失败体系、Template、注解语义

两者无重叠，可独立推进。代码实现层面的改动多为 API 形状变化，需更谨慎（涉及消费者），建议优先走 `sdd-spec`。
