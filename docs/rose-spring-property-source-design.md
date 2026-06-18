# Rose Spring PropertySource — 实现规格

> **Artifact：** `rose-spring-core`（**不**单独拆 Maven 模块）  
> **逻辑子域：** `property-source`（包 `io.zhijun.spring.core.propertysource.*`，自 `core.config.*` 整理）  
> **前置：** env-refresh 同 artifact 内先就绪（见 [rose-spring-env-refresh-design.md §5](./rose-spring-env-refresh-design.md#5-phase-1-实现规格env-refreshrose-spring-core-内)）  
> **定位：** 增强型 `@PropertySource`（注解加载、YAML/JSON、顺序、通配符、文件热更）；变更通过 `ListenableMutablePropertySources` 发出事件。

## 如何使用本文档编码

| 步骤 | 章节 | 说明 |
|------|------|------|
| 0 | [env-refresh §5](./rose-spring-env-refresh-design.md#5-phase-1-实现规格env-refreshrose-spring-core-内) | **先**完成 Listenable Environment + 事件（同 artifact） |
| 1 | **§6** | 包结构（`rose-spring-core` 内） |
| 2 | **§7** | 注解 API（逐字） |
| 3 | **§8–§10** | Loader 算法、Factory、文件热更 |
| 4 | **§11** | 测试矩阵 |

**验收：**

```bash
mvn -pl rose-spring/rose-spring-core test
```


### 实现状态（相对 `rose-spring-core` 主代码）

| 能力 | 文档章节 | 代码 |
|------|----------|------|
| 注解 / Loader / Factory | §7–§9 | ✅（`config.*` 与 `propertysource.*` 并存） |
| `autoRefreshed` 监听全部 `value[]` | §10.2 | ❌ 待修复 |
| `getChangedKeys()` 联调 | §11 #10 | ❌ 依赖 env-refresh |
| 包迁移 R1–R4 | §4 | ⚠️ 进行中 |

---

## 1. 背景

Spring 标准 `@PropertySource` 不支持：`first`/`before`/`after`、通配符、`autoRefreshed`、YAML/JSON 元注解组合。

Rose 现有实现位于 `rose-spring-core`，与 Listenable Environment 同 artifact。重实现目标：

1. **逻辑分包** `io.zhijun.spring.core.propertysource.*`，与 `io.zhijun.spring.core.env.*` 并列，**不**拆 Maven 模块。
2. **同 artifact 协作**：`replace()` / `addFirst()` 走 `ListenableMutablePropertySources` → 自动 `PropertySourcesChangedEvent` + `getChangedKeys()`。
3. **修复已知问题**：`autoRefreshed` 监听 **全部** `value[]` 路径（现实现只 watch `value[0]`）。

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| 注解加载 | `@ResourcePropertySource`、`@ResourcePropertySources`、`@YamlPropertySource`、`@JsonPropertySource` |
| 资源 | properties / YAML / JSON；`classpath*` 通配符；`${}` 占位符 |
| 顺序 | `first` / `before` / `after` / 默认 `addLast` |
| 热更 | `autoRefreshed=true` → 文件 watch → `replace` → env-refresh 事件链 |
| Java 8 | 与 Rose 一致 |

### 2.2 非目标

- Listenable Environment / `Refreshable`（属 env-refresh）
- Boot `@ConfigurationProperties` rebind
- Cloud Config 客户端
- 远程 URL 热更（非 `Resource.isFile()`）

---

## 3. 逻辑子域结构（均在 `rose-spring-core`）

```
rose-spring/
└── rose-spring-core/
    └── src/main/java/io/zhijun/spring/core/
        ├── env/                    # env-refresh 规格
        └── propertysource/         # 本规格
            ├── annotation/
            ├── support/
            └── refresh/
```

**同 artifact 内依赖：** property-source 直接调用 `io.zhijun.spring.core.env.ListenableMutablePropertySources`，无跨 module Maven 依赖。

**外部依赖（`rose-spring-core/pom.xml`，已有或补齐）：**

```
rose-spring-core
  ├── spring-context
  ├── snakeyaml          # YAML 工厂
  └── jackson-databind   # JSON 工厂
```

---

## 4. 包名整理（可选渐进迁移）

| 阶段 | 动作 | 时间点 |
|------|------|--------|
| **R1** | 在 `rose-spring-core` 内新增/重构 property-source 类 | env-refresh Phase 1 完成后 |
| **R2** | 旧 `io.zhijun.spring.core.config.*` 类标记 `@Deprecated`，委托至 `propertysource.*` | property-source Phase 1–2 同 PR 或紧随其后 |
| **R3** | 测试与 README 包路径对齐 | R2 同一 release |
| **R4** | 删除 `config.annotation` / `config.support` 重复类 | **下一 minor**（至少一个 release 的 deprecated 窗口） |

**包名映射：**

| 旧包 | 新包 |
|------|------|
| `io.zhijun.spring.core.config.annotation.*` | `io.zhijun.spring.core.propertysource.annotation.*` |
| `io.zhijun.spring.core.config.support.*` | `io.zhijun.spring.core.propertysource.support.*` |
| `io.zhijun.spring.core.env.refresh.ResourcePropertySource*` | `io.zhijun.spring.core.propertysource.refresh.*` |

**Maven：** 不新增 artifact；`rose-bom` / `rose-coverage` 仍只管理 `rose-spring-core`。

---

## 5. 实施阶段

| Phase | 内容 |
|-------|------|
| 0 | env-refresh §5 全绿（同 artifact） |
| 1 | 注解 + Loader + Factory（无 autoRefreshed） |
| 2 | `autoRefreshed` + Watcher + Lifecycle |
| 3 | 包名整理 + 文档 + 下游 import 迁移 |

---

## 6. 包结构（`rose-spring-core` 内）

### 6.1 不改动 Maven 模块列表

`rose-spring/pom.xml` **仅**保留 `rose-spring-core`（及既有子模块），**不**增加 `rose-spring-env-refresh` / `rose-spring-property-source`。

### 6.2 源文件树

```
rose-spring-core/src/main/java/io/zhijun/spring/core/propertysource/
├── annotation/
│   ├── ResourcePropertySource.java
│   ├── ResourcePropertySources.java
│   ├── YamlPropertySource.java
│   └── JsonPropertySource.java
├── support/
│   ├── AnnotatedPropertySourceImportSelector.java   # package-private abstract
│   ├── ResourcePropertySourceLoader.java
│   ├── ResourcePropertySourcesLoader.java
│   ├── PropertySourceMaps.java
│   ├── DefaultResourceComparator.java
│   ├── YamlPropertySourceFactory.java
│   └── JsonPropertySourceFactory.java
└── refresh/
    ├── ResourcePropertySourcesRefresher.java
    ├── ResourcePropertySourceRefreshWatcher.java
    └── ResourcePropertySourceRefreshLifecycle.java
```

**预估：** ~12 生产类 + 8 测试类（路径：`rose-spring-core/src/test/java/...`）。

---

## 7. 注解 API（逐字实现）

### 7.1 `@ResourcePropertySource`

```java
package io.zhijun.spring.core.propertysource.annotation;

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

### 7.2 `@ResourcePropertySources`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(ResourcePropertySourcesLoader.class)
public @interface ResourcePropertySources {
    ResourcePropertySource[] value();
}
```

### 7.3 `@YamlPropertySource` / `@JsonPropertySource`

- 仅元注解 `@ResourcePropertySource(factory = XxxFactory.class)` + `@AliasFor` 转发全部属性。
- `@YamlPropertySource` 的 `encoding` 默认 **`"UTF-8"`**（非 `${file.encoding}`）。
- **禁止** 在 Yaml/Json 注解上重复 `@Import`（由元注解触发 `ResourcePropertySourceLoader`）。

### 7.4 属性语义

| 属性 | 规则 |
|------|------|
| `value` | 至少一处；支持 `classpath:`、`classpath*:`、`${}` |
| `name` | blank → `{importingClass.name}@{ResourcePropertySource.class.name}` |
| `first` | true → `addFirst`；**与 before/after 同注解则 `IllegalStateException`（§8.5）** |
| `after` | 非 blank → `addAfter(after, ps)` |
| `before` | 非 blank → `addBefore(before, ps)` |
| 默认 | `addLast` |
| `ignoreResourceNotFound` | 无资源时不抛错，跳过加载 |
| `autoRefreshed` | 仅 `Resource.isFile()==true` 的资源；见 §10 |

---

## 8. Loader 算法

### 8.1 `AnnotatedPropertySourceImportSelector<A>`

与现实现一致：

1. `selectImports` → 解析 merged annotation → `loadPropertySource` → 返回 `NO_IMPORTS`。
2. 实现 `EnvironmentAware`、`BeanClassLoaderAware`、`ResourceLoaderAware`。
3. `resolveImportingClass(metadata)` → `Class.forName(metadata.getClassName())`。

### 8.2 `ResourcePropertySourceLoader.loadPropertySource`

```
1. propertySourceName = resolvePropertySourceName(importingClass, annotation)
2. resources = resolveResources(annotation)     // §8.3
3. if resources.isEmpty → return
4. propertySource = createCompositePropertySource(name, annotation, resources)  // §8.4
5. addPropertySource(annotation, environment.getPropertySources(), propertySource)  // §8.5
6. if annotation.autoRefreshed → registerAutoRefresh(propertySourceName, annotation)  // §10
```

**Environment 要求：** `getEnvironment().getPropertySources()` **必须** 为 `ListenableMutablePropertySources`（由 env-refresh Initializer 包装）。否则：

- Phase 1 测试使用 `ListenableConfigurableEnvironmentInitializer` 或测试 `@ContextConfiguration` + initializer。
- 若未包装：文档说明「无变更事件」；**不**在 loader 内 fallback 发事件。

### 8.3 `resolveResources`

```
for each location in annotation.value():
    resolved = environment.resolvePlaceholders(location)
    found = resourceResolver.getResources(resolved)
    if found.length == 0 && !ignoreResourceNotFound:
        throw IllegalStateException("Resource location [...] not found")
    append all found to list
if list.size >= 2:
    sort by resourceComparator (instantiate via BeanUtils)
return list
```

### 8.4 `createCompositePropertySource`

```
factory = instantiate(annotation.factory())
if resources.size == 1:
    return factory.createPropertySource(name + "@" + description, EncodedResource)
else:
    composite = new CompositePropertySource(propertySourceName)
    for each resource in resources (sorted order):
        composite.addPropertySource(factory.create(...))
    return composite
```

**Merge 规则：** 多资源同 composite 内，**后 add 的 PropertySource 优先级更高**（Spring Composite 惯例）；通配符顺序由 `resourceComparator` 控制。

### 8.5 `addPropertySource`

**前置校验（`validateOrderAttributes`，在 load 入口调用）：**

```java
if (annotation.first() && (hasText(before) || hasText(after))) {
    throw new IllegalStateException(
            "@ResourcePropertySource on " + importingClass.getName()
            + ": 'first' cannot be used with 'before' or 'after'");
}
if (hasText(before) && hasText(after)) {
    throw new IllegalStateException(
            "@ResourcePropertySource on " + importingClass.getName()
            + ": 'before' and 'after' are mutually exclusive");
}
```

```java
if (annotation.first()) {
    propertySources.addFirst(propertySource);
} else if (StringUtils.hasText(annotation.after())) {
    propertySources.addAfter(annotation.after(), propertySource);
} else if (StringUtils.hasText(annotation.before())) {
    propertySources.addBefore(annotation.before(), propertySource);
} else {
    propertySources.addLast(propertySource);
}
```

**`before` / `after` 目标不存在：** 委托 Spring `MutablePropertySources.addBefore/addAfter` 行为（抛 `IllegalArgumentException`）。

### 8.6 `ResourcePropertySourcesLoader`

对 `@ResourcePropertySources.value()` **按数组顺序** 逐个调用 `ResourcePropertySourceLoader.loadPropertySource`（共享同一 `importingClass` 与 delegate context）。

---

## 9. PropertySourceFactory

### 9.1 `PropertySourceMaps.flatten`

与现实现相同：递归 Map → `a.b.c` 扁平 key；**非 Map 叶子** `String.valueOf`。

### 9.2 `YamlPropertySourceFactory`

```
load YAML → if not Map → empty MapPropertySource
else flatten → MapPropertySource(name, flattened)
```

- 不支持 multi-document `---`（首版）。
- 编码：`EncodedResource.getReader()`（UTF-8 或 annotation.encoding）。

### 9.3 `JsonPropertySourceFactory`

```
ObjectMapper.readValue → Map → flatten → MapPropertySource
```

### 9.4 `DefaultResourceComparator`

`left.getDescription().compareTo(right.getDescription())` — 稳定、可测。

---

## 10. 文件热更（`autoRefreshed`）

### 10.1 `ResourcePropertySourceRefreshWatcher`

```
watch(String resourcePattern, ResourcePropertySourcesRefresher refresher):
    for each Resource r in resolver.getResources(resourcePattern):
        if r.isFile():
            fileWatchService.watch(r.getFile(), listener → refresher.refresh(pattern, r))
    fileWatchService.start()
```

### 10.2 `registerAutoRefresh`（相对旧实现的修复）

```
for each location in annotation.value():
    resolved = resolvePlaceholders(location)
    watcher.watch(resolved, refresher → reload(propertySourceName, annotation))
ResourcePropertySourceRefreshLifecycle.register(watcher)
```

**每个 `@ResourcePropertySource` 一个 Watcher 实例**（可接受）；同一配置类多个 `@Repeatable` 各自独立 watcher。

### 10.3 `reload`

```
resources = resolveResources(annotation)
if resources.isEmpty:
    if propertySources.contains(name):
        propertySources.replace(name, empty MapPropertySource)
    return
refreshed = createCompositePropertySource(...)
if propertySources.contains(name):
    propertySources.replace(name, refreshed)    // → Listenable → REPLACED event + getChangedKeys()
else:
    addPropertySource(...)
```

**不在 property-source 子域内 `publishEvent`**；由 env-refresh 的 `ListenableMutablePropertySources` 统一处理。

### 10.4 `ResourcePropertySourceRefreshLifecycle`

- 静态 `CopyOnWriteArrayList<AutoCloseable>` 注册 watcher。
- `ContextClosedEvent` → close 全部 watcher → clear。

**注册：** 合并进 `rose-spring-core` 的 `META-INF/spring.factories`（**非**独立 Maven 模块）：

```properties
org.springframework.context.ApplicationListener=\
io.zhijun.spring.core.propertysource.refresh.ResourcePropertySourceRefreshLifecycle
```

---

## 11. 测试矩阵

| # | 测试类 | 场景 | 期望 |
|---|--------|------|------|
| 1 | `ResourcePropertySourceLoaderTest` | 单 properties | `environment.getProperty("a")` 命中 |
| 2 | 同上 | `classpath*:` 多文件 + comparator | 排序稳定、后者覆盖 |
| 3 | 同上 | `first=true` | 优先于 systemProperties 等（测 precedence） |
| 4 | 同上 | `ignoreResourceNotFound` | 不抛错 |
| 5 | 同上 | `@ResourcePropertySources` 两个 | 均加载 |
| 5b | 同上 | `first` + `before` 同注解 | `IllegalStateException` |
| 6 | `PropertySourceFactoryTest` | YAML 嵌套 | flatten key |
| 7 | 同上 | JSON | flatten key |
| 8 | `YamlPropertySourceMetaTest` | `@YamlPropertySource` | 等价 factory |
| 9 | `ResourcePropertySourceRefreshWatcherTest` | 改文件 + autoRefreshed | property 更新 |
| 10 | `ResourcePropertySourceRefreshEventTest` | autoRefreshed + Listenable | `PropertySourcesChangedEvent` 且 `getChangedKeys()` 含变更 key |
| 11 | `ResourcePropertySourceRefreshLifecycleTest` | Context close | watcher closed |

**测试 Environment  setup（必须）：**

```java
public static class ListenableInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
    }
}

@ContextConfiguration(initializers = ListenableInitializer.class)
```

### 11.1 测试资源

```
src/test/resources/config/app.properties          → app.key=1
src/test/resources/config/override.properties     → app.key=2
src/test/resources/config/app.yml                 → nested: { key: yaml }
src/test/resources/config/app.json                → { "flat": "json" }
```

---

## 12. 实现检查清单

- [ ] env-refresh Phase 1 已在 `rose-spring-core` 完成
- [ ] `mvn -pl rose-spring/rose-spring-core test` 全绿
- [ ] `autoRefreshed` 监听 **全部** `value[]`
- [ ] reload 走 `replace` 且集成测试收到 `getChangedKeys()`
- [ ] Yaml/Json 元注解无重复 `@Import`
- [ ] `rose-bom` 仍仅管理 `rose-spring-core`（无新 artifact）
- [ ] `rose-spring/README.md` 指向本规格

---

## 13. 与 env-refresh / i18n 的衔接

| 链路 | 说明 |
|------|------|
| 本地配置文件热更 | 本模块 `autoRefreshed` → `replace` → env-refresh 事件 |
| i18n Env 覆盖 | **不**经本模块；Config/Env 直接写 `rose.i18n.messages.*` → env-refresh `Refreshable` |
| `@YamlPropertySource` + i18n | 配置与 i18n 文案分离；i18n 默认 `classpath*:i18n/.../messages` |

详见 [rose-i18n-design.md §7](./rose-i18n-design.md#7-与-rose-spring-core-联动) 与 [env-refresh §6](./rose-spring-env-refresh-design.md#6-phase-2-对接规格rose-i18n)。

---

## 14. 开工清单

1. 分支：`feature/rose-spring-core-refresh`（或与 env-refresh 共用）
2. 在 `rose-spring-core` 先完成 env-refresh §5
3. 同 artifact 内按 §6–§10 实现 property-source
4. `mvn -pl rose-spring/rose-spring-core test`
4. §11 测试全绿
5. 更新 `rose-spring-core` 聚合（§4 R2）
6. 更新 `rose-spring/README.md`、`rose-bom`、`rose-coverage`
