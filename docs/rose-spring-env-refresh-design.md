# Rose Spring 环境与配置刷新 — 实现规格

> **Artifact：** `rose-spring-core`（**不**单独拆 Maven 模块）  
> **逻辑子域：** `env-refresh`（包 `io.zhijun.spring.core.env.*`，见 §5）  
> **配套规格：** [rose-spring-property-source-design.md](./rose-spring-property-source-design.md)（同 artifact，包 `io.zhijun.spring.core.propertysource.*`）  
> **定位：** Listenable Environment、PropertySource 变更事件、`getChangedKeys()`、`Refreshable` 编排。  
> **重实现顺序：** **env-refresh §5 → property-source §6–§11 → i18n §6**

### 实现状态（相对 `rose-spring-core` 主代码）

| 能力 | 文档章节 | 代码 |
|------|----------|------|
| Listenable Environment 包装 | 已有 | ✅ |
| `publishEvent` | §5.5 | ❌ 待实现 |
| `getChangedKeys()` / `PropertySourceDiffSupport` | §5.3–§5.4 | ❌ 待实现 |
| `Refreshable` / `PropertySourcesRefreshOrchestrator` | §5.7–§5.8 | ❌ 待实现 |
| `RefreshableContextHolder` | §5.2 | ❌ 待实现 |
| `spring.factories` 键名 | §5.0 | ⚠️ 见 §5.0 迁移说明 |

## 如何使用本文档编码

| 步骤 | 章节 | 说明 |
|------|------|------|
| 1 | **§5** | **env-refresh 规格**（在 `rose-spring-core` 内实现） |
| 2 | [property-source §6–§11](./rose-spring-property-source-design.md) | 注解与文件热更（同 artifact） |
| 3 | **§6** | i18n `Refreshable` 对接 |
| — | §3–§4 | 架构、阶段 |
| — | [configuration-bean-binding §7](./rose-spring-configuration-bean-binding-design.md#7-env-热更对接phase-2依赖-env-refresh) | Configuration Bean 热更（Phase 2） |

**验收（与 property-source 共用同一 artifact）：**

```bash
mvn -pl rose-spring/rose-spring-core test
```

---

## 1. 背景与问题

在 **`rose-spring-core` 同一 artifact 内** 重实现 env-refresh 逻辑子域；PropertySource 注解见 [property-source 规格](./rose-spring-property-source-design.md)（同 artifact，不同包）。

本逻辑子域职责：

| 能力 | 类（包 `io.zhijun.spring.core.env.*`） |
|------|-----|
| 包装 Environment | `ListenableConfigurableEnvironment` |
| 拦截 PropertySource 增删改 | `ListenableMutablePropertySources` |
| 变更事件 | `PropertySourceChangedEvent` / `PropertySourcesChangedEvent` |
| key diff | `PropertySourceDiffSupport` |
| 刷新编排 | `Refreshable` / `PropertySourcesRefreshOrchestrator` |

**现存缺口：**

1. `PropertySourcesChangedEvent` 继承 `ApplicationContextEvent`，但 **从未 `publishEvent`** — `@EventListener` 无法消费。
2. `getChangedProperties()` 对 **REPLACED** 源返回新源**全部** key，无法做精确前缀过滤（如 `rose.i18n.messages.*`）。
3. 各模块（i18n、Cloud）若各自写 `ApplicationListener`，**重复且与 Rose SPI 双轨**。
4. `rose-i18n` 文档 §16.8 假设 `ApplicationListener<PropertySourcesChangedEvent>`，与现状 **不兼容**。

本规格在 **不破坏** 现有 `EnvironmentListener` SPI 的前提下补齐上述能力。

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| **双通道通知** | 保留 `EnvironmentListener` 同步回调 + 可选 `ApplicationContext.publishEvent` |
| **key 级 diff** | `PropertySourcesChangedEvent.getChangedKeys()` 支持 ADDED / REPLACED / REMOVED |
| **统一刷新编排** | `PropertySourcesRefreshOrchestrator` + `Refreshable` SPI |
| **i18n 可对接** | Env bundle / Cloud 共用 orchestrator，见 §6 |
| **Java 8** | 与 Rose 基线一致 |

### 2.2 非目标（本规格不做）

- 替换 Spring Cloud `EnvironmentChangeEvent`（Cloud 仍保留，由 orchestrator 统一转发）
- 监听每次 `getProperty()`（PropertyResolver 拦截保持现状，不扩展）
- 非 `EnumerablePropertySource` 的 key diff（文档说明限制即可）
- Boot `@ConfigurationProperties` 自动 rebind（远期，见 [configuration-bean-binding §7](./rose-spring-configuration-bean-binding-design.md)）

### 2.3 运行时边界（Phase 1 约定）

| 主题 | 约定 |
|------|------|
| **并发** | `PropertySourcesRefreshOrchestrator.dispatch` 在触发线程同步执行；`Refreshable.refresh` **不重入**（实现方自行防重入） |
| **失败** | 单个 `Refreshable` 抛异常 → 记录 warn，**不中断**其他 Refreshable（见 §5.8） |
| **RefreshScope** | 本规格 **不** 集成 Spring Cloud `@RefreshScope`；Cloud 仍通过 `EnvironmentChangeEvent` → keys 转发 |
| **Initializer 顺序** | `ListenableConfigurableEnvironmentInitializer` 在 `ApplicationContextInitializer` 链中尽早执行；`RefreshableContextHolder.bind` 见 §5.2 |

## 3. 架构

```
PropertySources add / replace / remove          ← 来源：Spring / Cloud / property-source reload
        │
        ▼
ListenableMutablePropertySources.publish(subEvent)
        │
        ├──► EnvironmentListener.onPropertySourcesChanged(bulkEvent)
        │         └── PropertySourcesRefreshOrchestrator
        │                   └── Refreshable.refresh(changedKeys)
        │
        └──► publishEvent(bulkEvent)  [rose.spring.env.publish-property-source-events，默认 true]

EnvironmentChangeEvent (Spring Cloud)  ──► orchestrator.onEnvironmentChangeKeys(keys)
```

**逻辑子域边界（均在 `rose-spring-core`）：**

| 子域 | 包 / 文档 | 职责 |
|------|-----------|------|
| **env-refresh** | `io.zhijun.spring.core.env.*`、本规格 §5 | Listenable、事件、Refreshable |
| **property-source** | `io.zhijun.spring.core.propertysource.*`、[property-source 规格](./rose-spring-property-source-design.md) | 注解加载、文件热更 → 触发 env 事件链 |

**Maven：** 仅 `rose-spring-core` 一个 artifact；下游继续依赖 `io.zhijun:rose-spring-core`。

---

## 4. 实施阶段

| Phase | 位置 | 内容 | 依赖 |
|-------|------|------|------|
| **1** | `rose-spring-core` | env-refresh §5 | 无 |
| **1b** | `rose-spring-core` | [property-source §6–§11](./rose-spring-property-source-design.md) | Phase 1 |
| **2a** | `rose-i18n-spring` | §6 Refreshable 对接 | Phase 1 |
| **2b** | `rose-i18n-spring-cloud` | §6.4 Cloud adapter | 2a |

---

## 5. Phase 1 实现规格（env-refresh，`rose-spring-core` 内）

> **权威章节**：类名、算法、SPI、测试以本节为准。

### 5.0 包结构与 `spring.factories`

**路径（均在 `rose-spring-core`）：**

```
rose-spring/rose-spring-core/src/main/java/io/zhijun/spring/core/
├── env/
│   ├── ListenableConfigurableEnvironment.java
│   ├── ListenableConfigurableEnvironmentInitializer.java
│   ├── ListenableMutablePropertySources.java      # + publishEvent
│   ├── PropertySourcesUtils.java
│   ├── listener/ ...
│   ├── event/ ...
│   ├── support/
│   │   └── PropertySourceDiffSupport.java         # 新增
│   └── refresh/
│       ├── Refreshable.java
│       ├── RefreshableContextHolder.java
│       ├── PropertySourcesRefreshOrchestrator.java
│       └── RoseSpringEnvironmentRefreshProperties.java
└── io/support/
    └── SpringFactoriesLoaderUtils.java            # 已有
```

**`META-INF/spring.factories`（合并进 core 现有文件）：**

```properties
org.springframework.context.ApplicationContextInitializer=\
io.zhijun.spring.core.env.ListenableConfigurableEnvironmentInitializer

io.zhijun.spring.core.env.listener.EnvironmentListener=\
io.zhijun.spring.core.env.listener.LoggingEnvironmentListener,\
io.zhijun.spring.core.env.refresh.PropertySourcesRefreshOrchestrator
```

**`spring.factories` 迁移（Phase 1 PR 一并修正）：**

| 项 | 现状（错误） | 目标（本规格） |
|----|--------------|----------------|
| EnvironmentListener **键** | `io.zhijun.spring.core.env.EnvironmentListener` | **`io.zhijun.spring.core.env.listener.EnvironmentListener`**（接口全限定名） |
| Logging 实现 **值** | `io.zhijun.spring.core.env.LoggingEnvironmentListener` | **`io.zhijun.spring.core.env.listener.LoggingEnvironmentListener`** |
| orchestrator | 未注册 | 追加 `PropertySourcesRefreshOrchestrator` |

`SpringFactoriesLoader.loadFactoryNames(EnvironmentListener.class, …)` 以 **接口类名** 为键；键名错误会导致 listener 加载失败或行为不一致。

**Refreshable 键（Phase 2 模块追加）：**

```properties
io.zhijun.spring.core.env.refresh.Refreshable=
```

**POM：** 不新建子模块；变更仅在 `rose-spring-core/pom.xml`（无新增 artifact）。

**可选包整理：** 现有类已在 `io.zhijun.spring.core.env.*`；新增类同包即可，**无需**拆 artifact。

### 5.1 仓库改动清单

| 文件 | 改动 |
|------|------|
| `rose-spring-core/src/main/java/io/zhijun/spring/core/env/...` | 见 §5.0 |
| `rose-spring-core/src/test/java/...` | 见 §5.11 |
| `rose-spring-core/src/main/resources/META-INF/spring.factories` | 注册 orchestrator |
| `rose-spring/README.md` | 指向两份规格 |

**勿修改：** `rose-i18n/`（Phase 2）；property-source 见 [另一规格](./rose-spring-property-source-design.md)

### 5.2 `RefreshableContextHolder` 与 Initializer 钩子

`Refreshable` 由 `spring.factories` 加载，**非 Spring Bean**，无法注入 `ApplicationContext`。Phase 1 在 env-refresh 内提供静态 holder，供 orchestrator 注册的 Refreshable（含 i18n Phase 2）使用。

```java
package io.zhijun.spring.core.env.refresh;

public final class RefreshableContextHolder {

    private static volatile ApplicationContext applicationContext;

    private RefreshableContextHolder() {}

    public static void bind(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        ApplicationContext ctx = applicationContext;
        if (ctx == null) {
            throw new IllegalStateException(
                    "ApplicationContext not bound; ensure ListenableConfigurableEnvironmentInitializer ran");
        }
        return ctx;
    }

    /** 测试专用 */
    static void clear() {
        applicationContext = null;
    }
}
```

**修改 `ListenableConfigurableEnvironmentInitializer`：**

```java
@Override
public void initialize(ConfigurableApplicationContext applicationContext) {
    applicationContext.setEnvironment(new ListenableConfigurableEnvironment(
            applicationContext.getEnvironment(), applicationContext));
    RefreshableContextHolder.bind(applicationContext);
}
```

**Initializer 顺序：**

| Initializer | 顺序要求 |
|-------------|----------|
| `ListenableConfigurableEnvironmentInitializer` | 包装 Environment **早于** 任何读取 `PropertySources` 的 `@Configuration` / `@ResourcePropertySource` 加载 |
| Bootstrap `EnvironmentPostProcessor` | 在 Initializer **之前** 已写入 profile（Boot 生命周期）；Listenable 包装 **不** 改变 property 值 |
| 其他 Rose Initializer | 若依赖 Listenable / Refreshable，在 `spring.factories` 中 **排在** Listenable 之后（文档化，不强制 `@Order`） |

**测试：** `RefreshableContextHolderTest` — bind 后 get；clear 后 get 抛 `IllegalStateException`。

（i18n 对接示例见 [§6.2](#62-environmentmessagebundlerefreshable)，引用本 §5.2 `RefreshableContextHolder`。）

### 5.3 `PropertySourceDiffSupport`

```java
package io.zhijun.spring.core.env.support;

public final class PropertySourceDiffSupport {

    private PropertySourceDiffSupport() {}

    /** 枚举 PropertySource 的全部 property name；非 Enumerable 返回 empty。 */
    public static Set<String> getPropertyNames(PropertySource<?> source) { ... }

    /**
     * REPLACED：old 有 new 无 → removed；new 有且 (old 无或值不等) → changed。
     * 值比较：Objects.equals(oldSource.getProperty(k), newSource.getProperty(k))
     */
    public static Set<String> diffReplaced(PropertySource<?> oldSource, PropertySource<?> newSource) { ... }

    /** ADDED：new 源全部 key。 */
    public static Set<String> keysAdded(PropertySource<?> newSource) { ... }

    /** REMOVED：old 源全部 key。 */
    public static Set<String> keysRemoved(PropertySource<?> oldSource) { ... }
}
```

**规则：**

- 仅处理 `EnumerablePropertySource`；否则对应分支返回 `Collections.emptySet()`。
- 返回 `LinkedHashSet` 保持稳定顺序。

### 5.4 `PropertySourcesChangedEvent` 扩展

在现有类上 **新增**（不删现有 `getChangedProperties()` 以保持兼容）：

```java
/**
 * key 级变更集合：
 * - ADDED：新源全部 key
 * - REMOVED：旧源全部 key
 * - REPLACED：PropertySourceDiffSupport.diffReplaced(old, new)
 */
public Set<String> getChangedKeys() {
    if (changedKeys == null) {
        changedKeys = computeChangedKeys();
    }
    return changedKeys;
}

private Set<String> computeChangedKeys() {
    LinkedHashSet<String> keys = new LinkedHashSet<String>();
    for (PropertySourceChangedEvent sub : subEvents) {
        switch (sub.getKind()) {
            case ADDED:
                keys.addAll(PropertySourceDiffSupport.keysAdded(sub.getNewPropertySource()));
                break;
            case REMOVED:
                keys.addAll(PropertySourceDiffSupport.keysRemoved(sub.getOldPropertySource()));
                break;
            case REPLACED:
                keys.addAll(PropertySourceDiffSupport.diffReplaced(
                        sub.getOldPropertySource(), sub.getNewPropertySource()));
                break;
            default:
                break;
        }
    }
    return Collections.unmodifiableSet(keys);
}
```

**字段：** `private transient Set<String> changedKeys;`（懒加载 + 不可序列化依赖，与 Spring Event 惯例一致）

**文档注释：** `getChangedProperties()` 仍为新源/ADDED 全量 map；**选择性刷新请用 `getChangedKeys()`**。

### 5.5 `ListenableMutablePropertySources` 变更

修改 `publish(PropertySourceChangedEvent event)`：

```java
private void publish(PropertySourceChangedEvent event) {
    PropertySourcesChangedEvent bulkEvent = new PropertySourcesChangedEvent(
            applicationContext, Collections.singletonList(event));

    for (EnvironmentListener listener : listeners) {
        listener.onPropertySourceChanged(event);
        listener.onPropertySourcesChanged(bulkEvent);
    }

    if (RoseSpringEnvironmentRefreshProperties.isPublishPropertySourceEvents(applicationContext)
            && applicationContext instanceof ConfigurableApplicationContext) {
        ((ConfigurableApplicationContext) applicationContext).publishEvent(bulkEvent);
    }
}
```

**import：** `org.springframework.context.ConfigurableApplicationContext`

**顺序：** 先 SPI 回调，再 `publishEvent`（保证 orchestrator 在 SPI 阶段已刷新，且 `@EventListener` 看到一致状态）。

### 5.6 `RoseSpringEnvironmentRefreshProperties`

```java
package io.zhijun.spring.core.env.refresh;

public final class RoseSpringEnvironmentRefreshProperties {

    public static final String PUBLISH_EVENTS = "rose.spring.env.publish-property-source-events";
    public static final String ORCHESTRATOR_ENABLED = "rose.spring.env.refresh-orchestrator.enabled";

    /** 默认 true */
    public static boolean isPublishPropertySourceEvents(ApplicationContext context) {
        if (context == null) {
            return true;
        }
        Environment env = context.getEnvironment();
        return env.getProperty(PUBLISH_EVENTS, Boolean.class, Boolean.TRUE);
    }

    /** 默认 true */
    public static boolean isOrchestratorEnabled(Environment environment) {
        return environment.getProperty(ORCHESTRATOR_ENABLED, Boolean.class, Boolean.TRUE);
    }
}
```

Phase 2 Boot 可将上述 key 绑定到 `rose.spring.env.*`；Phase 1 仅 `Environment.getProperty`。

### 5.7 `Refreshable` SPI

```java
package io.zhijun.spring.core.env.refresh;

/**
 * 配置刷新扩展点。通过 META-INF/spring.factories 注册。
 */
public interface Refreshable {

    /**
     * 是否关心本次变更中的任一 key。
     * @param changedKeys 非 empty
     */
    boolean supports(Set<String> changedKeys);

    /**
     * 执行刷新。仅在 supports 返回 true 时由 orchestrator 调用。
     */
    void refresh(Set<String> changedKeys);
}
```

**spring.factories 键：**

```properties
io.zhijun.spring.core.env.refresh.Refreshable=
```

（Phase 1 无默认实现；i18n Phase 2 追加。）

### 5.8 `PropertySourcesRefreshOrchestrator`

**注册方式（Phase 1 唯一方案）：**

1. 在 `spring.factories` 注册为 **`EnvironmentListener`**（见 §5.0）。
2. `ListenableConfigurableEnvironment` 通过 `SpringFactoriesLoaderUtils.loadFactories(EnvironmentListener.class, …)` 加载（**不**在构造器内手动 new orchestrator）。
3. `Refreshable` 列表在 orchestrator 内 **lazy-load**（首次 `onPropertySourcesChanged` 时从 `Refreshable` factories 加载）。

```java
package io.zhijun.spring.core.env.refresh;

import io.zhijun.spring.core.env.listener.EnvironmentListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.LOWEST_PRECEDENCE)
public final class PropertySourcesRefreshOrchestrator implements EnvironmentListener {

    private volatile List<Refreshable> refreshables;

    public PropertySourcesRefreshOrchestrator() {}

    public PropertySourcesRefreshOrchestrator(List<Refreshable> refreshables) {
        this.refreshables = refreshables;
    }

    @Override
    public void onPropertySourcesChanged(PropertySourcesChangedEvent event) {
        ApplicationContext context = (ApplicationContext) event.getSource();
        if (!RoseSpringEnvironmentRefreshProperties.isOrchestratorEnabled(context.getEnvironment())) {
            return;
        }
        Set<String> changedKeys = event.getChangedKeys();
        if (changedKeys.isEmpty()) {
            return;
        }
        dispatch(context, changedKeys);
    }

    public void onEnvironmentChangeKeys(Set<String> keys) {
        dispatch(RefreshableContextHolder.getApplicationContext(), keys);
    }

    private void dispatch(ApplicationContext context, Set<String> changedKeys) {
        if (changedKeys == null || changedKeys.isEmpty()) {
            return;
        }
        for (Refreshable refreshable : resolveRefreshables(context)) {
            try {
                if (refreshable.supports(changedKeys)) {
                    refreshable.refresh(changedKeys);
                }
            } catch (RuntimeException ex) {
                org.slf4j.LoggerFactory.getLogger(getClass())
                        .warn("Refreshable {} failed", refreshable.getClass().getName(), ex);
            }
        }
    }

    private List<Refreshable> resolveRefreshables(ApplicationContext context) {
        if (refreshables != null) {
            return refreshables;
        }
        synchronized (this) {
            if (refreshables == null) {
                ClassLoader cl = context == null ? null : context.getClassLoader();
                refreshables = SpringFactoriesLoaderUtils.loadFactories(Refreshable.class, cl);
            }
        }
        return refreshables;
    }
}
```

**已否决方案（勿实现）：** 在 `ListenableConfigurableEnvironment` 构造器内组装 orchestrator；orchestrator 无参构造内 eager `loadFactories` 且无 `ApplicationContext`。

### 5.9 Cloud 转发

Phase 1 **不**依赖 `spring-cloud-context`。Cloud 模块调用 §5.8 的 `onEnvironmentChangeKeys`；细节见 §6.4。

### 5.10 与 property-source 的边界

文件 `autoRefreshed` reload 在 **`rose-spring-core` 内 property-source 子域** 调用 `propertySources.replace()`；env-refresh **仅** 在 `ListenableMutablePropertySources` 内发事件。  
规格见 [rose-spring-property-source-design.md §10](./rose-spring-property-source-design.md#10-文件热更autorefrshed)。

### 5.11 测试矩阵

| # | 测试类 | 场景 | 期望 |
|---|--------|------|------|
| 1 | `PropertySourceDiffSupportTest` | REPLACED 改 1 key | diff 仅含该 key |
| 2 | 同上 | REPLACED 无变化 | empty |
| 3 | 同上 | ADDED / REMOVED | 全 key |
| 4 | `PropertySourcesChangedEventDiffTest` | bulk 含 ADDED+REMOVED | `getChangedKeys()` 并集 |
| 5 | `ListenableMutablePropertySourcesPublishTest` | addLast + `@EventListener` | 收到 1 事件 |
| 6 | 同上 | `publish-property-source-events=false` | listener 不触发 |
| 7 | `PropertySourcesRefreshOrchestratorTest` | 注册 test Refreshable + prefix | supports 命中则 refresh 1 次 |
| 8 | 同上 | 无关 key | refresh 不调用 |
| 9 | `ListenableMutablePropertySourcesTests` | 原有 3 事件 | 仍通过（回归） |

**`PropertySourcesRefreshOrchestratorTest` 模板：**

```java
@Test
void refreshesWhenPrefixMatches() {
    AtomicInteger count = new AtomicInteger();
    Refreshable refreshable = new Refreshable() {
        @Override
        public boolean supports(Set<String> keys) {
            return keys.stream().anyMatch(k -> k.startsWith("app.messages."));
        }
        @Override
        public void refresh(Set<String> keys) {
            count.incrementAndGet();
        }
    };
    PropertySourcesRefreshOrchestrator orchestrator =
            new PropertySourcesRefreshOrchestrator(Collections.singletonList(refreshable));

    PropertySourcesChangedEvent event = ... // REPLACED app.messages.welcome
    orchestrator.onPropertySourcesChanged(event);

    assertThat(count).hasValue(1);
}
```

**`ListenableMutablePropertySourcesPublishTest`：**

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PublishTestConfig.class)
class ListenableMutablePropertySourcesPublishTest {
    @Autowired ApplicationContext context;
    AtomicReference<PropertySourcesChangedEvent> captured = new AtomicReference<>();

    @BeforeEach
    void listen() {
        ((ConfigurableApplicationContext) context).addApplicationListener(
                (ApplicationListener<PropertySourcesChangedEvent>) captured::set);
    }

    @Test
    void publishesOnReplace() {
        // 通过 Listenable Environment replace PropertySource
        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().getChangedKeys()).contains("demo.key");
    }
}
```

### 5.12 实现检查清单

- [ ] `mvn -pl rose-spring/rose-spring-core test` 全绿
- [ ] `getChangedKeys()` 对 REPLACED 为 diff，非全量
- [ ] `publishEvent` 默认开启，可 `rose.spring.env.publish-property-source-events=false` 关闭
- [ ] `spring.factories` 键名与 §5.0 迁移表一致
- [ ] `PropertySourcesRefreshOrchestrator` 注册为 `EnvironmentListener`
- [ ] 现有 `ListenableMutablePropertySourcesTests` 无回归
- [ ] `rose-spring/README.md` 更新 Refreshable 与 getChangedKeys 说明

---

## 6. Phase 2 对接规格（`rose-i18n`）

> **前置：** §5 已合并并发布（或同仓库 SNAPSHOT）。

### 6.1 删除 / 替换项（相对 `rose-i18n-design.md` §16.8）

| 原设计 | 新设计 |
|--------|--------|
| `I18nPropertySourcesChangedListener` implements `ApplicationListener<PropertySourcesChangedEvent>` | **`EnvironmentMessageBundleRefreshable` implements `Refreshable`** |
| `@EnableI18n(refreshOnPropertySourcesChanged=true)` 注册 listener | `@EnableI18n` + **spring.factories 注册 Refreshable**（或 Boot 自动注册 Bean） |

### 6.2 `EnvironmentMessageBundleRefreshable`

**位置：** `rose-i18n-spring/src/main/java/io/zhijun/i18n/spring/env/EnvironmentMessageBundleRefreshable.java`

`Refreshable` 由 `spring.factories` 加载，**非 Spring Bean** — **不要**使用 `ApplicationContextAware`；通过 [§5.2 `RefreshableContextHolder`](#52-refreshablecontextholder-与-initializer-钩子) 获取上下文。

```java
public final class EnvironmentMessageBundleRefreshable implements Refreshable {

    private static final String KEY_PREFIX = "rose.i18n.messages.";

    @Override
    public boolean supports(Set<String> changedKeys) {
        for (String key : changedKeys) {
            if (key.startsWith(KEY_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void refresh(Set<String> changedKeys) {
        ApplicationContext ctx = RefreshableContextHolder.getApplicationContext();
        for (EnvironmentMessageBundle bundle : ctx.getBeansOfType(EnvironmentMessageBundle.class).values()) {
            bundle.refresh();
        }
    }
}
```

**注册（`rose-i18n-spring` 的 `META-INF/spring.factories`）：**

```properties
io.zhijun.spring.core.env.refresh.Refreshable=\
io.zhijun.i18n.spring.env.EnvironmentMessageBundleRefreshable
```

### 6.3 `@EnableI18n` 调整

- **删除** `refreshOnPropertySourcesChanged` 注解属性（改由 spring.factories 自动注册 Refreshable）。
- §16.8 整节替换为本文 §6.2。

### 6.4 Cloud 模块（`rose-i18n-spring-cloud`）调整

**原 §17 `I18nRefreshOrchestrator`** 精简为 Cloud 专用 adapter：

```java
public final class I18nCloudRefreshAdapter implements ApplicationListener<EnvironmentChangeEvent> {

    private final PropertySourcesRefreshOrchestrator orchestrator;

    public I18nCloudRefreshAdapter(PropertySourcesRefreshOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        orchestrator.onEnvironmentChangeKeys(event.getKeys());
    }
}
```

**Bean 获取 orchestrator：** 从 `ApplicationContext.getBeansOfType(EnvironmentListener.class)` 过滤 `PropertySourcesRefreshOrchestrator`，或 spring.factories 暴露单例 accessor（Phase 2 实现时二选一，**推荐** context 内唯一 orchestrator EnvironmentListener）。

**Env key 前缀过滤：** 仍在 `EnvironmentMessageBundleRefreshable.supports()` 内完成；Cloud 传入全量 `event.getKeys()` 即可。

### 6.5 i18n 验收

```bash
mvn -pl rose-i18n/rose-i18n-spring,rose-i18n/rose-i18n-spring-cloud test
```

- [ ] 变更 `rose.i18n.messages.app.zh_CN.welcome` → Env bundle `refresh()` 调用
- [ ] `/actuator/refresh` 模拟 `EnvironmentChangeEvent` → 同上
- [ ] 变更 `server.port` → Env bundle **不** refresh

---

## 7. 与现有文档的映射

| 文档 | 章节 | 动作 |
|------|------|------|
| [rose-i18n-design.md](./rose-i18n-design.md) | §7 | 指向本文 §5–§6 |
| 同上 | §16.8 | 按 §6.2 重写 |
| 同上 | §17.6 `I18nRefreshOrchestrator` | 按 §6.4 精简 |
| [rose-spring/README.md](../rose-spring/README.md) | Listenable Environment | 增加 Refreshable / publishEvent |
| [configuration-bean-binding §7](./rose-spring-configuration-bean-binding-design.md) | Env 热更 | Configuration Bean `Refreshable` 对接 |
| [bootstrap-diagnostics §4](./rose-spring-boot-bootstrap-diagnostics-design.md) | Initializer 顺序 | 与 Listenable Initializer 协作 |

---

## 8. 决策摘要

| 决策 | 选择 | 理由 |
|------|------|------|
| 事件双通道 | SPI + publishEvent | 兼容现有 listener 与 `@EventListener` |
| key diff | `getChangedKeys()` 新方法 | 不破坏 `getChangedProperties()` |
| 刷新扩展 | `Refreshable` spring.factories | 纯 Spring 模块可加载，i18n optional 注册 |
| orchestrator 注册 | `EnvironmentListener` | 复用 Listenable 链，无需新基础设施 |
| Refreshable 加载 | orchestrator **lazy-load** | 首次 `onPropertySourcesChanged` 时有 `ApplicationContext` |
| Context 注入 Refreshable | `RefreshableContextHolder` | factories 实例非 Bean，需静态绑定 |
| Cloud | 转发 keys 到 orchestrator | 单一 refresh 路径 |

---

## 9. 开工清单

1. 分支：`feature/rose-spring-core-env-refresh`（或 `feature/rose-spring-core-refresh`）
2. 在 `rose-spring-core` 按 §5 实现 env-refresh
3. `mvn -pl rose-spring/rose-spring-core test`
4. 同一 artifact 内继续 property-source（[property-source 规格](./rose-spring-property-source-design.md)），可同 PR 或后续 PR
5. 更新 `rose-spring-core` 聚合（property-source §4）
6. （可选）`rose-i18n` 按 §6 对接
