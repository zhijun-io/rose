# Rose Spring 环境与配置刷新 — 设计方案

> **Artifact：** `rose-spring-core`（**不**单独拆 Maven 模块）  
> **逻辑子域：** `env-refresh`（包 `io.zhijun.spring.core.env.*`）  
> **配套：** [property-source 设计](./rose-spring-property-source-design.md)（同 artifact，包
`io.zhijun.spring.core.propertysource.*`）  
> **定位：** Listenable Environment、PropertySource 变更事件、`getChangedKeys()`、`Refreshable` 响应编排。

---

## 1. 背景

在 `rose-spring-core` 内为 Spring `Environment` 增加**可观测**与**可响应**能力：任何来源（Boot、Cloud、property-source
文件热更、业务代码）对 `PropertySources` 的增删改，都能以统一事件模型通知下游。

| 能力                    | 主要类型                                                         |
|-----------------------|--------------------------------------------------------------|
| 包装 Environment        | `ListenableConfigurableEnvironment`                          |
| 拦截 PropertySource 增删改 | `ListenableMutablePropertySources`                           |
| 变更事件                  | `PropertySourceChangedEvent` / `PropertySourcesChangedEvent` |
| key diff              | `PropertySourceDiffSupport`                                  |
| 刷新响应                  | `Refreshable` / `PropertySourcesRefreshEnvironmentListener`  |

在 **不破坏** 现有 `EnvironmentListener` SPI 的前提下扩展。

---

## 2. 目标与非目标

### 2.1 目标

| 目标             | 说明                                                                           |
|----------------|------------------------------------------------------------------------------|
| **双通道通知**      | `EnvironmentListener` 同步回调 + 可选 `ApplicationContext.publishEvent`            |
| **key 级 diff** | `PropertySourcesChangedEvent.getChangedKeys()` 覆盖 ADDED / REPLACED / REMOVED |
| **统一响应编排**     | `PropertySourcesRefreshEnvironmentListener` + `Refreshable` SPI              |
| **i18n 可对接**   | Env bundle / Cloud 共用同一 dispatch 路径                                          |
| **Java 8**     | 与 Rose 基线一致                                                                  |

### 2.2 非目标

- 替换 Spring Cloud `EnvironmentChangeEvent`（Cloud 仍保留，由 listener 转发 keys）
- 监听每次 `getProperty()`（PropertyResolver 拦截保持现状）
- 非 `EnumerablePropertySource` 的精确 key diff
- Boot `@ConfigurationProperties` 自动
  rebind（远期，见 [configuration-bean-binding §7](./rose-spring-configuration-bean-binding-design.md)）

### 2.3 运行时边界

| 主题               | 约定                                                                              |
|------------------|---------------------------------------------------------------------------------|
| **并发**           | dispatch 在触发线程同步执行；`Refreshable.refresh` 不重入（实现方自行防重入）                          |
| **失败**           | 单个 `Refreshable` 异常 → warn 日志，不中断其他 Refreshable                                 |
| **RefreshScope** | 不集成 `@RefreshScope`；Cloud 走 `EnvironmentChangeEvent` → keys 转发                  |
| **Initializer**  | `ListenableConfigurableEnvironmentInitializer` 尽早包装 Environment 并 `bind` holder |
| **Root Context** | `RefreshableContextHolder` 仅绑定 `getParent() == null` 的 root context             |
| **启动抑制**         | `isActive() == false` 时不 dispatch / 不 `publishEvent`（避免启动 ADDED 风暴）             |
| **开关**           | `rose.spring.env.refresh.enabled` 约束 PropertySource 路径与 Cloud keys 路径           |

---

## 3. 架构

```
PropertySources add / replace / remove     ← Spring / Cloud / property-source reload / 业务代码
        │
        ▼
ListenableMutablePropertySources.publish(subEvent)
        │
        ├──► EnvironmentListener.onPropertySourcesChanged(bulkEvent)
        │         └── PropertySourcesRefreshEnvironmentListener
        │                   └── Refreshable.refresh(changedKeys)
        │
        └──► publishEvent(bulkEvent)   [rose.spring.env.publish-property-source-events，默认 true]

EnvironmentChangeEvent (Spring Cloud)  ──► onEnvironmentChangeKeys(keys)
```

**子域边界（同 artifact）：**

| 子域                  | 包                                        | 职责                            |
|---------------------|------------------------------------------|-------------------------------|
| **env-refresh**     | `io.zhijun.spring.core.env.*`            | 观测 + 事件 + 响应编排                |
| **property-source** | `io.zhijun.spring.core.propertysource.*` | 注解加载、文件 trigger → `replace()` |

---

## 4. 包结构

```
rose-spring-core/src/main/java/io/zhijun/spring/core/env/
├── ListenableConfigurableEnvironment.java
├── ListenableConfigurableEnvironmentInitializer.java
├── ListenableMutablePropertySources.java
├── PropertySourcesUtils.java
├── listener/          # EnvironmentListener、ProfileListener、PropertyResolverListener
├── event/             # *ChangedEvent、PropertySourceDiffSupport
└── refresh/           # Refreshable、PropertySourcesRefreshEnvironmentListener、
                       # RefreshableContextHolder、EnvRefreshProperties
```

**SPI 注册（概念）：**

- `ApplicationContextInitializer` → `ListenableConfigurableEnvironmentInitializer`
- `EnvironmentListener` → `LoggingEnvironmentListener`、`PropertySourcesRefreshEnvironmentListener`
- `Refreshable` → 各模块（如 i18n）通过 `spring.factories` 追加
- property-source 的 `AutoRefreshWatcherLifecycle` 注册为 `ApplicationListener`（shutdown 清理）

`spring.factories` 键名须与接口全限定名一致（如 `io.zhijun.spring.core.env.listener.EnvironmentListener`）。

---

## 5. 组件设计

### 5.1 `RefreshableContextHolder`

`Refreshable` 由 `spring.factories` 加载，**非 Spring Bean**，无法注入 `ApplicationContext`。  
Initializer 在包装 Environment 后对 **root context** 调用 `bind`；`ContextClosedEvent` 时 `clear`。

Cloud / 静态 SPI 实现通过 holder 获取上下文。

### 5.2 `PropertySourceDiffSupport`

| 方法                          | 语义                                             |
|-----------------------------|------------------------------------------------|
| `getPropertyNames`          | 枚举 `EnumerablePropertySource` 的全部 key；否则 empty |
| `diffReplaced`              | key 集合差集 + `Objects.equals` 值比较                |
| `keysAdded` / `keysRemoved` | ADDED / REMOVED 分支的全量 key                      |

仅处理 `EnumerablePropertySource`；返回 `LinkedHashSet` 保序。

### 5.3 `PropertySourcesChangedEvent.getChangedKeys()`

在保留 `getChangedProperties()` 的前提下新增 key 级 diff：

- **ADDED**：新源全部 key
- **REMOVED**：旧源全部 key
- **REPLACED**：`diffReplaced(old, new)`

懒加载、不可序列化缓存；**选择性刷新应使用 `getChangedKeys()`**。

### 5.4 `ListenableMutablePropertySources.publish`

1. 构造 bulk `PropertySourcesChangedEvent`
2. 依次回调所有 `EnvironmentListener`（单条 + bulk）
3. 若 `publish-property-source-events=true` 且 context `isActive()` → `publishEvent`

先 SPI、后 Spring Event，保证 `@EventListener` 看到与 Refreshable 一致的状态。

### 5.5 `EnvRefreshProperties`

| 配置键                                              | 默认     | 作用                        |
|--------------------------------------------------|--------|---------------------------|
| `rose.spring.env.publish-property-source-events` | `true` | 是否 `publishEvent`         |
| `rose.spring.env.refresh.enabled`                | `true` | 是否 dispatch `Refreshable` |

### 5.6 `Refreshable` SPI

```java
public interface Refreshable {
    boolean supports(Set<String> changedKeys);
    void refresh(Set<String> changedKeys);
}
```

通过 `META-INF/spring.factories` 注册；由 `PropertySourcesRefreshEnvironmentListener` lazy-load 并缓存。

### 5.7 `PropertySourcesRefreshEnvironmentListener`

实现 `EnvironmentListener`，在 `onPropertySourcesChanged` 中：

1. 检查 `refresh.enabled` 与 `isActive()`
2. 取 `event.getChangedKeys()`，空则返回
3. 对每个 `Refreshable`：`supports` → `refresh`；单点失败仅 warn

另提供 `onEnvironmentChangeKeys(Set<String>)` 供 Cloud adapter 转发，受同一开关与 active 守卫约束。

**已否决：** 在 `ListenableConfigurableEnvironment` 构造器内硬编码 new listener；无 context 时 eager 加载 Refreshable。

### 5.8 与 property-source 的边界

property-source 的 `reload()` 只调用 `propertySources.replace()`；  
env-refresh **仅**在 `ListenableMutablePropertySources` 内发事件与 dispatch。  
详见 [property-source 设计 §7](./rose-spring-property-source-design.md#7-文件热更autorefrshed--trigger-链)。

---

## 6. 与 i18n / Cloud 对接

### 6.1 思路

用 **`Refreshable` 替代** 直接监听 `PropertySourcesChangedEvent` 的 i18n listener：

- `EnvironmentMessageBundleRefreshable.supports`：任一 key 以 `rose.i18n.messages.` 开头
- `refresh`：从 holder 取 context，对所有 `EnvironmentMessageBundle` bean 调用 `refresh()`

`@EnableI18n` 不再暴露 `refreshOnPropertySourcesChanged` 开关；Refreshable 由 i18n 模块 `spring.factories` 注册。

### 6.2 Cloud

`I18nCloudRefreshAdapter`（或等价物）监听 `EnvironmentChangeEvent`，将 `event.getKeys()` 转发给
`PropertySourcesRefreshEnvironmentListener.onEnvironmentChangeKeys`。  
前缀过滤仍在 `EnvironmentMessageBundleRefreshable.supports` 内完成。

### 6.3 Configuration Bean 热更

远期由 binding 模块提供独立 `Refreshable`
，见 [configuration-bean-binding §7](./rose-spring-configuration-bean-binding-design.md#7-env-热更契约)。

---

## 7. 决策摘要

| 决策                    | 选择                                  | 理由                                 |
|-----------------------|-------------------------------------|------------------------------------|
| 事件双通道                 | SPI + publishEvent                  | 兼容现有 listener 与 `@EventListener`   |
| key diff              | `getChangedKeys()` 新方法              | 不破坏 `getChangedProperties()`       |
| 刷新扩展                  | `Refreshable` spring.factories      | 纯 Spring 模块可加载，i18n optional 注册    |
| 响应注册                  | `EnvironmentListener`               | 复用 Listenable 链，无需新基础设施            |
| Refreshable 加载        | lazy-load + 缓存                      | 首次 dispatch 时已有 ApplicationContext |
| Context 注入            | `RefreshableContextHolder`          | factories 实例非 Bean                 |
| Cloud                 | 转发 keys 到同一 listener                | 单一 dispatch 路径                     |
| trigger / dispatch 分离 | propertysource.watch vs env.refresh | 因果清晰，依赖单向                          |
