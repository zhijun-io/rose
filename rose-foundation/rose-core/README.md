# Rose Core

纯 Java **框架无关** 工具层：并发委托等实现；契约注解见 **`rose-annotation`**。

## 子模块（`rose-foundation`）

| Artifact | 说明 |
|----------|------|
| `rose-annotation` | 契约注解（`io.zhijun.annotation`） |
| `rose-core` | 工具实现（`io.zhijun.core`） |
| `rose-annotation-processor` | 编译期注解处理（可选） |
| `rose-test` | 跨主题 JUnit 测试工具（`test` scope） |

## 已实现

| 能力 | 说明 |
|------|------|
| `DelegatingScheduledExecutorService` | `ScheduledExecutorService` 委托包装 |

契约注解（`@Since`、`@Incubating`、`@Internal`、`@Nullable`、并发语义注解）在 **`rose-annotation`**。编译期处理见 `SinceProcessor` / `InternalApiProcessor`。`@ConfigurationProperties` 的 IDE 元数据由 Spring Boot `configuration-processor` 生成。

## 消费方式

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-core</artifactId>
</dependency>
```

`rose-annotation` 由 `rose-core` **传递**引入；仅需注解时可单独依赖 `rose-annotation`。

## 编译期处理（可选）

`rose-annotation-processor` 使用 [Auto Service](https://github.com/google/auto) 注册 `javax.annotation.processing.Processor`（无需手写 `META-INF/services`）。

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-annotation-processor</artifactId>
    <optional>true</optional>
</dependency>
```

## 测试支持

模块单测可依赖：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| `microsphere-java-annotations` | `rose-annotation` | ✅ |
| `microsphere-java-core` | `rose-core` | ⚠️ 保持精简 |
| `microsphere-java-test` | `rose-test` | ✅ 起步 |
| `microsphere-annotation-processor` | `rose-annotation-processor` | ✅ 起步 |
