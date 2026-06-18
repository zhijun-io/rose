# Rose Spring PropertySource — 设计

> **Artifact：** `rose-spring-core`（**不**单独拆 Maven 模块）  
> **逻辑子域：** `property-source`（包 `io.zhijun.spring.core.propertysource.*`）  
> **前置：** [env-refresh 设计](./rose-spring-env-refresh-design.md) 同 artifact 内就绪  
> **定位：** 增强型 `@PropertySource`（注解加载、YAML/JSON、顺序、通配符、文件热更）；变更通过 `ListenableMutablePropertySources` 进入 env 事件链。

---

## 1. 背景

Spring 标准 `@PropertySource` 不支持：`first`/`before`/`after`、通配符、`autoRefreshed`、YAML/JSON 元注解组合。

Rose 在 `rose-spring-core` 内提供增强能力，与 Listenable Environment 同 artifact、不同包：

1. **逻辑分包** `io.zhijun.spring.core.propertysource.*`，与 `io.zhijun.spring.core.env.*` 并列。
2. **协作方式**：`replace()` / `addFirst()` 等走 `ListenableMutablePropertySources` → `PropertySourcesChangedEvent` + `getChangedKeys()`。
3. **`autoRefreshed`**：监听注解 `value[]` 中的**全部**可文件化路径，而非仅第一项。

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| 注解加载 | `@ResourcePropertySource`、`@ResourcePropertySources`、`@YamlPropertySource`、`@JsonPropertySource` |
| 资源 | properties / YAML / JSON；`classpath*` 通配符；`${}` 占位符 |
| 顺序 | `first` / `before` / `after` / 默认 `addLast` |
| 热更 | `autoRefreshed=true` → 文件 watch → `replace` → env 响应链 |
| Java 8 | 与 Rose 基线一致 |

### 2.2 非目标

- Listenable Environment / `Refreshable`（属 env-refresh）
- Boot `@ConfigurationProperties` rebind
- Cloud Config 客户端
- 远程 URL 热更（非 `Resource.isFile()`）

---

## 3. 架构与包结构

```
rose-spring-core/src/main/java/io/zhijun/spring/core/
├── env/                    # 可监听环境 + 变更响应（见 env-refresh 设计）
└── propertysource/
    ├── annotation/         # 对外注解 API
    ├── support/            # Loader、Factory、加载逻辑
    └── watch/              # autoRefreshed 文件触发（trigger 链）
```

**依赖方向：** property-source **不** import env 类型；通过 `MutablePropertySources` 操作间接协作。  
**前置条件：** `ListenableConfigurableEnvironmentInitializer` 已将 `PropertySources` 包装为 `ListenableMutablePropertySources`，否则加载仍可用，但无变更事件。

**外部依赖：** `spring-context`、`snakeyaml`（YAML）、`jackson-databind`（JSON）。

---

## 4. 注解 API

### 4.1 `@ResourcePropertySource`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(ResourcePropertySources.class)
@Import(ResourcePropertySourceLoader.class)
public @interface ResourcePropertySource {

    String name() default "";
    boolean autoRefreshed() default false;
    boolean first() default false;
    String before() default "";
    String after() default "";
    String[] value() default {};
    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;
    boolean ignoreResourceNotFound() default false;
    String encoding() default "${file.encoding:UTF-8}";
    Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
}
```

### 4.2 `@ResourcePropertySources`

容器注解，`@Import(ResourcePropertySourcesLoader.class)`，承载可重复的 `ResourcePropertySource[] value()`。

### 4.3 `@YamlPropertySource` / `@JsonPropertySource`

- 元注解 `@ResourcePropertySource(factory = XxxFactory.class)` + `@AliasFor` 转发全部属性。
- `@YamlPropertySource` 的 `encoding` 默认 **`"UTF-8"`**（非 `${file.encoding}`）。
- Yaml/Json **不**重复 `@Import`（由元注解触发 `ResourcePropertySourceLoader`）。

### 4.4 属性语义

| 属性 | 规则 |
|------|------|
| `value` | 至少一处；支持 `classpath:`、`classpath*:`、`${}` |
| `name` | blank → `{importingClass.name}@{ResourcePropertySource.class.name}` |
| `first` | true → `addFirst`；**与 before/after 同注解则 `IllegalStateException`** |
| `before` / `after` | 互斥；非 blank 时分别 `addBefore` / `addAfter` |
| 默认 | `addLast` |
| `ignoreResourceNotFound` | 无资源时不抛错，跳过加载 |
| `autoRefreshed` | 仅 `Resource.isFile()==true` 的资源；见 §7 |

---

## 5. 加载与排序

### 5.1 ImportSelector 基类

`AnnotatedPropertySourceImportSelector`：`selectImports` 解析 merged annotation → `loadPropertySource` → 返回 `NO_IMPORTS`；实现 `EnvironmentAware`、`BeanClassLoaderAware`、`ResourceLoaderAware`。

### 5.2 加载流程

```
1. propertySourceName = resolvePropertySourceName(importingClass, annotation)
2. resources = resolveResources(annotation)
3. if resources.isEmpty → return
4. propertySource = createCompositePropertySource(name, annotation, resources)
5. addPropertySource(annotation, propertySources, propertySource)
6. if annotation.autoRefreshed → registerAutoRefresh(propertySourceName, annotation)
```

### 5.3 资源解析

- 遍历 `value[]`，`resolvePlaceholders` 后 `getResources`。
- 未找到且未设 `ignoreResourceNotFound` → `IllegalStateException`。
- 多资源时按 `resourceComparator` 排序（默认 `DefaultResourceComparator`：按 `description` 字典序）。

### 5.4 复合 PropertySource

- 单资源：直接 `factory.createPropertySource`。
- 多资源：`CompositePropertySource`；**后 add 的源优先级更高**；通配符展开顺序由 comparator 控制。

### 5.5 顺序校验

- `first` 与 `before`/`after` 同注解 → 抛错。
- `before` 与 `after` 同注解 → 抛错。
- 锚点 PropertySource 不存在 → 委托 Spring `addBefore`/`addAfter` 行为。

### 5.6 容器注解

`ResourcePropertySourcesLoader` 对 `value[]` **按数组顺序**逐个加载，共享同一 `importingClass` 与 delegate context。

---

## 6. PropertySourceFactory

| 组件 | 行为 |
|------|------|
| `PropertySourceMaps.flatten` | 递归 Map → `a.b.c` 扁平 key；非 Map 叶子 `String.valueOf` |
| `YamlPropertySourceFactory` | YAML → Map → flatten；非 Map 则空 `MapPropertySource`；首版不支持 multi-document `---` |
| `JsonPropertySourceFactory` | Jackson `readValue` → flatten → `MapPropertySource` |
| `DefaultResourceComparator` | `description` 字典序，稳定可测 |

---

## 7. 文件热更（`autoRefreshed`）— trigger 链

property-source 只负责 **触发** 变更；**响应**（事件、`Refreshable`）由 env 子域处理。

### 7.1 `AutoRefreshWatcher`

对每个 `value[]` 中可解析为本地文件的 `Resource` 注册 `FileWatchService` 监听；文件变更时回调 `PropertySourceReloadCallback.onReload`。

### 7.2 注册策略

- 遍历 **全部** `annotation.value[]`（非仅 `[0]`）。
- 每个 `@ResourcePropertySource` 独立 watcher 实例；`@Repeatable` 各自注册。
- `AutoRefreshWatcherLifecycle` 在 `ContextClosedEvent` 时关闭全部 watcher。

### 7.3 `reload`

```
resources = resolveResources(annotation)
if empty → replace(name, empty MapPropertySource) 或跳过
else → replace(name, refreshed composite)   // → Listenable REPLACED + getChangedKeys()
```

property-source **不**直接 `publishEvent`；统一由 `ListenableMutablePropertySources` 发出。

### 7.4 术语：trigger vs dispatch

| 链 | 包 | 含义 |
|----|-----|------|
| **trigger** | `propertysource.watch` | 文件变 → reload → `replace()` |
| **dispatch** | `env.refresh` | 事件 → `Refreshable.refresh(changedKeys)` |

---

## 8. 与 env-refresh / i18n 的衔接

| 链路 | 说明 |
|------|------|
| 本地配置文件热更 | `autoRefreshed` → `replace` → env 事件链 |
| i18n Env 覆盖 | **不**经本模块；`rose.i18n.messages.*` 直接由 env `Refreshable` 消费 |
| `@YamlPropertySource` + i18n | 配置与 i18n 文案分离 |

详见 [rose-i18n-design.md §7](./rose-i18n-design.md#7-与-rose-spring-core-联动) 与 [env-refresh 设计 §6](./rose-spring-env-refresh-design.md#6-与-i18n--cloud-对接)。
