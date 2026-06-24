# Rose Spring Boot Bootstrap 与启动诊断 — 实现规格

> **Artifact：** `rose-spring-boot`（Bootstrap）；各主题模块注册 **FailureAnalyzer**  
> **定位：** 应用模式检测（dev/test/prod）、Profile 激活、启动失败可行动诊断；**不**替代 Spring Boot 自身 diagnostics。  
> **关联：** [env-refresh §5.2 Initializer 顺序](./rose-spring-env-refresh-design.md#52-refreshablecontextholder-与-initializer-钩子)

## 如何使用本文档编码

| 步骤 | 章节 | 说明 |
|------|------|------|
| 1 | **§4–§5** | Bootstrap 模式与 EnvironmentPostProcessor |
| 2 | **§6** | FailureAnalyzer 注册规范 |
| 3 | **§7** | 启动顺序与 Initializer 协作 |
| 4 | **§8** | 并行初始化范围（P3 澄清） |

**验收：**

```bash
mvn -pl rose-spring-boot/rose-spring-boot-core,rose-devservice/rose-devservice-core test
```


### 实现状态

| 能力 | 代码 |
|------|------|
| Bootstrap 模式 / EPP | ✅ |
| FailureAnalyzer（dev-services、observation） | ✅ |
| Dev Services 并行启动 | ❌ 远期 §8.1 |
| 统一 Analyzer 注册表文档 | ✅ 本文 §6 |

---

## 1. 背景

Rose 应用在 **Boot 2.7 / Java 8** 上运行。`rose-spring-boot` 负责：

- 检测运行模式并激活 profile（dev / test）
- 为 dev-services 等主题提供 `@ConditionalOnDevMode` 等条件

**缺口：**

- FailureAnalyzer **分散**在各主题，无统一注册表与文案规范
- Listenable Environment Initializer 与 Bootstrap 的 **顺序**未文档化
- 「并行初始化」含义未定义，易与 Spring Boot 3 背景初始化混淆

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| Bootstrap 模式 | `DEV` / `TEST` / `PROD`，可 `rose.bootstrap.mode` 覆盖 |
| Profile 激活 | dev/test 默认 profile，可配置关闭 |
| FailureAnalyzer | Rose 自有异常 → 可行动 `FailureAnalysis` |
| 启动顺序文档 | EPP → Context refresh → Initializer 链 |
| Java 8 | 与 Rose 一致 |

### 2.2 非目标

- 替换 Spring Boot `FailureAnalyzers` 基础设施
- Spring Boot 3 式 **全局 parallel application context refresh**
- 统一 Actuator `/health` 聚合（属各主题）
- 在 `rose-spring-boot` 放置普通业务 AutoConfiguration（见 [rose-spring-boot/README.md](../rose-spring-boot/README.md) **边界**）

---

## 3. 模块边界

| 模块 | 职责 |
|------|------|
| `rose-spring-boot` | Bootstrap EPP、AutoConfiguration、条件注解、`RoseBinder` |
| `rose-devservice-core` | Dev Services 注册、容器生命周期、Dev Services FailureAnalyzer |
| `rose-observation-spring-boot` | Ambiguous conventions backend FailureAnalyzer |
| `rose-spring-core` | `ListenableConfigurableEnvironmentInitializer`（Framework 层） |

---

## 4. Bootstrap 模式

### 4.1 `BootstrapMode`

| 模式 | 检测顺序 |
|------|----------|
| 显式 | 系统属性 / Env：`rose.bootstrap.mode` = `DEV` \| `TEST` \| `PROD` |
| 推断 TEST | 调用栈含 JUnit / Spring Test / `@SpringBootTest` bootstrapper |
| 推断 DEV | `BootstrapModeDetector.isDevelopmentContext()`（IDE / spring-boot-devtools 等） |
| 默认 | `PROD` |

**缓存：** `BootstrapModeDetector` 进程内缓存检测结果；测试用 `BootstrapMode.clear()` / `clearCache()`。

### 4.2 配置前缀

| 前缀 | 用途 |
|------|------|
| `rose.bootstrap` | 总开关 |
| `rose.bootstrap.profiles.enabled` | 是否按模式追加 profile（默认 `true`） |
| `rose.bootstrap.mode` | 强制模式 |
| `rose.dev.profiles` | DEV 模式追加的 profile 列表（默认 `dev`） |
| `rose.test.profiles` | TEST 模式追加的 profile 列表（默认 `test`） |

---

## 5. `BootstrapEnvironmentPostProcessor`

### 5.1 行为

```
postProcessEnvironment(environment, application):
    if !rose.bootstrap.profiles.enabled: return
    mode = BootstrapMode.detect()
    switch mode:
        DEV  → merge rose.dev.profiles into additionalProfiles
        TEST → merge rose.test.profiles into additionalProfiles
        PROD → no-op (debug log only)
    ConfigDataEnvironmentPostProcessor.applyTo(..., additionalProfiles)
```

### 5.2 顺序

```java
@Override
public int getOrder() {
    return ConfigDataEnvironmentPostProcessor.ORDER + 5;
}
```

**含义：** 在 Config Data 处理 **之后**追加 profile，避免覆盖用户 `spring.profiles.active` 已显式设置的优先级逻辑（仅 **追加** 未激活的 profile）。

### 5.3 spring.factories（`rose-spring-boot`）

```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
io.zhijun.boot.autoconfigure.bootstrap.BootstrapEnvironmentPostProcessor

org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.zhijun.boot.autoconfigure.bootstrap.BootstrapAutoConfiguration
```

### 5.4 条件注解

| 注解 | 条件 |
|------|------|
| `@ConditionalOnDevMode` | `BootstrapMode.DEV == detect()`（`OnDevModeCondition`） |
| TEST 模式 | 无独立 `@ConditionalOnTestMode`；由 profile / `BootstrapMode.TEST` 驱动 dev-services 等行为 |

Dev Services connector **仅在** dev/test 模式启动容器（见 `ContainerConfigurer`）。

---

## 6. FailureAnalyzer 规范

### 6.1 注册方式

各主题在 **自身** `META-INF/spring.factories` 注册：

```properties
org.springframework.boot.diagnostics.FailureAnalyzer=\
fully.qualified.AnalyzerClass
```

**禁止**在 `rose-spring-boot` 集中注册其他主题 Analyzer（避免 boot 依赖全部主题）。

### 6.2 现有 Analyzer 清单

| Analyzer | 模块 | 异常类型 | Action 要点 |
|----------|------|----------|-------------|
| `MultipleDevServiceFailureAnalyzer` | rose-devservice-spring-boot | `MultipleDevServiceException` | 同 category 只启用一个 connector |
| `AmbiguousConventionsBackendFailureAnalyzer` | observation-spring-boot | `AmbiguousConventionsBackendException` | 设置 `rose.observation.conventions.backend` 或移除多余 backend |

### 6.3 新增 Analyzer 模板

```java
public final class ExampleFailureAnalyzer extends AbstractFailureAnalyzer<ExampleException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, ExampleException cause) {
        return new FailureAnalysis(
                "Human-readable problem (what went wrong).",
                "Human-readable action (how to fix).",
                cause);
    }
}
```

**文案规范：**

- **description**：一句说明失败原因，含关键标识（bean 名、property key）
- **action**：可执行步骤，优先 `rose.*` 配置键
- **cause**：保留原始异常链

### 6.4 规划中的 Analyzer（未实现）

| 场景 | 建议模块 | 优先级 |
|------|----------|--------|
| Listenable Initializer 未运行 | rose-spring-core | 低 |
| env-refresh orchestrator 循环 refresh | rose-spring-core | 低 |
| Dev services Docker 不可用 | rose-devservice-spring-boot | 中 |

---

## 7. 启动顺序与 Initializer 协作

```
1. SpringApplication.run
2. EnvironmentPostProcessor 链
      └── BootstrapEnvironmentPostProcessor（追加 dev/test profile）
3. ApplicationContext refresh
4. ApplicationContextInitializer 链
      └── ListenableConfigurableEnvironmentInitializer（包装 Environment + RefreshableContextHolder.bind）
5. BeanDefinition 加载 / @Import（含 @ResourcePropertySource、@EnableConfigurationBeanBinding）
6. Dev Services AutoConfiguration（@ConditionalOnDevMode）→ 容器启动
7. ApplicationStartedEvent / Ready
```

**约束：**

- Bootstrap **只**改 Environment（profile）；不依赖 Listenable。
- `@ResourcePropertySource` / Configuration Bean 绑定发生在 Initializer **之后** 的 refresh 阶段，依赖已包装的 `ListenableMutablePropertySources`（测试须显式注册 Initializer，见 property-source §11）。

---

## 8. 并行初始化（P3 范围澄清）

Rose **当前不做** 也 **不在本规格规划** 以下能力：

| 误解 | 说明 |
|------|------|
| Boot 3 `spring.main.lazy-initialization` 平台封装 | 应用自行配置；Rose 不包装 |
| 多 ApplicationContext 并行 refresh | 不支持 |
| Bean 默认异步初始化 | 不支持 |

**本规格中的「并行」仅指以下可选项（远期）：**

### 8.1 Dev Services 连接器并行启动（Phase 可选）

**问题：** 多个 Testcontainers 容器串行 `start()` 拉长 dev 启动时间。

**设计（远期）：**

```
DevServicesBootstrapCoordinator
    after Environment ready:
        connectors = discover enabled DevServicesConfigurer
        CompletableFuture.allOf(connectors.stream().map(c -> async c.start()).toArray())
        register dynamic properties when all complete
```

**约束：**

- 仅 **dev/test** profile
- 失败 fast：任一 connector 失败 → 聚合 `FailureAnalysis` 列出全部失败项
- **默认仍串行**；`rose.dev.parallel-startup=true`  opt-in

### 8.2 Initializer 链「并行」

**不做。** `ApplicationContextInitializer` 保持 Spring 单线程顺序；仅文档化 **相对顺序**（§7）。

---

## 9. 测试矩阵

| # | 测试类 | 场景 |
|---|--------|------|
| 1 | `BootstrapModeDetectorTests` | 栈检测 / 属性覆盖 |
| 2 | `BootstrapEnvironmentPostProcessorTests` | DEV/TEST profile 追加 |
| 3 | `BootstrapAutoConfigurationTests` | 属性绑定 |
| 4 | `MultipleDevServiceFailureAnalyzerTests` | 文案与 action |
| 5 | `AmbiguousConventionsBackendFailureAnalyzerTests` | 同上 |

---

## 10. 实现检查清单

- [ ] §4–§5 与现有 `rose-spring-boot` 测试一致
- [ ] §6.2 Analyzer 清单与 spring.factories 同步
- [ ] 新 Analyzer 遵循 §6.3 模板
- [ ] env-refresh Initializer 顺序与 §7 一致
- [ ] §8 并行范围不在未 opt-in 时改变行为

---

## 11. 开工清单

1. **文档期（本 PR）：** 规格落盘；可选补充 `rose-spring-boot/README.md` 链接
2. **Phase 1：** 整理 §6.4 高优先级 Analyzer（如 Docker 不可用）
3. **Phase 2（可选）：** Dev Services 并行启动 §8.1
