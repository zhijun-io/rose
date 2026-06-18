# Rose Spring Configuration Bean Binding — 实现规格

> **Artifact：** `rose-spring-core`（**不**单独拆 Maven 模块）  
> **逻辑子域：** `configuration-bean-binding`（包 `io.zhijun.spring.core.binder.*`）  
> **前置：** [env-refresh §5](./rose-spring-env-refresh-design.md#5-phase-1-实现规格env-refreshrose-spring-core-内)（Env 热更对接见 §7）  
> **定位：** 声明式将 `Environment` 前缀属性绑定为 Spring Bean（`@EnableConfigurationBeanBinding`）。

## 如何使用本文档编码

| 步骤 | 章节 | 说明 |
|------|------|------|
| 1 | **§5** | 注解与 Registrar 算法（现状 + 规范） |
| 2 | **§6** | PostProcessor 绑定与 Customizer |
| 3 | **§7** | Env 热更 `Refreshable` 对接（Phase 2） |
| 4 | **§8–§9** | 测试与检查清单 |

**验收：**

```bash
mvn -pl rose-spring/rose-spring-core test -Dtest='*ConfigurationBean*','*EnableConfiguration*'
```


### 实现状态

| 能力 | 代码 |
|------|------|
| `@EnableConfigurationBeanBinding` + Registrar + PostProcessor | ✅ |
| `ConfigurationBeanBindingRefreshable` | ❌ Phase 2 |

---

## 1. 背景

Spring Boot `@ConfigurationProperties` 需类上注解 + `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan`。Rose 提供 **Import 驱动** 的绑定方式，借鉴 microsphere，适用于：

- 纯 Spring Framework 项目（无 Boot）
- 同一前缀绑定 **多个** Bean（`multiple = true`）
- 与 `PropertySourcesUtils` / Listenable Environment 同栈

**现状：** 实现已在 `rose-spring-core`；本文档将 **行为规格化**，并定义与 env-refresh 的热更路径。

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| 声明式绑定 | `@EnableConfigurationBeanBinding(prefix, type)` |
| 多 Bean | `multiple = true` 按一级子前缀拆分 |
| 扩展 | `ConfigurationBeanBinder` / `ConfigurationBeanCustomizer` / `ConfigurationBeanAliasGenerator` |
| Java 8 | 与 Rose 基线一致 |

### 2.2 非目标

- 替代 Boot `@ConfigurationProperties` + `@Validated` 全特性（JSR-303 可后续加）
- 绑定非 `Map` 结构的动态 schema
- 在本 Phase 1 规格中实现 Boot `@ConfigurationProperties` rebind

---

## 3. 包结构（`rose-spring-core` 内）

```
io/zhijun/spring/core/binder/
├── annotation/
│   ├── EnableConfigurationBeanBinding.java
│   ├── EnableConfigurationBeanBindings.java
│   ├── ConfigurationBeanBindingRegistrar.java
│   └── ConfigurationBeanBindingPostProcessor.java
├── config/
│   ├── ConfigurationBeanBinder.java
│   ├── DefaultConfigurationBeanBinder.java
│   └── ConfigurationBeanCustomizer.java
└── support/
    ├── ConfigurationBeanAliasGenerator.java
    ├── DefaultConfigurationBeanAliasGenerator.java
    ├── ConversionServiceResolver.java
    └── *AliasGenerator.java
```

**协作类型：**

| 类型 | 包 | 职责 |
|------|-----|------|
| `PropertySourcesUtils` | `core.env` | `getSubProperties` / `normalizePrefix` |
| `RoseBinder` | `rose-spring-boot` | Boot 侧 `Binder` 便捷封装；**不**参与本注解绑定链 |

---

## 4. 注解 API

### 4.1 `@EnableConfigurationBeanBinding`

```java
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingRegistrar.class)
@Repeatable(EnableConfigurationBeanBindings.class)
public @interface EnableConfigurationBeanBinding {

    String prefix();

    Class<?> type();

    boolean multiple() default false;

    boolean ignoreUnknownFields() default true;

    boolean ignoreInvalidFields() default true;
}
```

### 4.2 `@EnableConfigurationBeanBindings`

容器注解，`value()` 为 `@EnableConfigurationBeanBinding[]`。

---

## 5. Registrar 算法（`ConfigurationBeanBindingRegistrar`）

### 5.1 入口

```
registerBeanDefinitions(metadata, registry):
    attributes = metadata.getAnnotationAttributes(EnableConfigurationBeanBinding.class)
    if attributes != null:
        registerConfigurationBeanDefinitions(attributes, registry)
```

### 5.2 属性解析

```
prefix = resolvePlaceholders(attributes.prefix)     // 必填
type   = attributes.type                            // 必填
multiple / ignoreUnknownFields / ignoreInvalidFields = attributes 或默认值
configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix)
```

### 5.3 Bean 名称解析

**`multiple = false`（默认）：**

```
if properties["id"] 为非空 String:
    beanName = id
else:
    beanName = generateBeanName(RootBeanDefinition(type), registry)
subProperties = configurationProperties   // 整棵子树
```

**`multiple = true`：**

```
for each key in configurationProperties.keySet():
    dot = key.indexOf('.')
    if dot > 0:
        beanNames.add(key.substring(0, dot))   // 一级段作为 bean 名
for each beanName in beanNames (LinkedHashSet 保序):
    subProperties = getSubProperties(normalizePrefix(beanName))  // 见 §5.4
    register beanName → type
```

**示例：**

```properties
usr.id = 1
usr.name = rose
app.id = 2
app.name = other
```

`prefix=usr` + `multiple=true` → Bean `usr`（非 `app`）。  
`prefix=""` 或根前缀需应用自行约定；推荐显式前缀如 `clients`。

### 5.4 `resolveSubProperties`（multiple）

```
propertySources = MutablePropertySources + MapPropertySource("_", configurationProperties)
return PropertySourcesUtils.getSubProperties(propertySources, environment, normalizePrefix(beanName))
```

### 5.5 BeanDefinition 元数据

每个 Configuration Bean 的 `AbstractBeanDefinition`：

| Attribute | 内容 |
|-----------|------|
| `source` | `EnableConfigurationBeanBinding.class`（识别标记） |
| `configurationProperties` | `Map<String,Object>` 待绑定 flat map |
| `ignoreUnknownFields` | boolean |
| `ignoreInvalidFields` | boolean |

### 5.6 别名

```
for each ConfigurationBeanAliasGenerator from spring.factories:
    registry.registerAlias(beanName, generator.generateAlias(prefix, beanName, type))
```

默认 SPI：`DefaultConfigurationBeanAliasGenerator`（hyphen / underscore 变体，见现有实现）。

### 5.7 PostProcessor 注册

若 registry 尚无 `configurationBeanBindingPostProcessor`：

```
register RootBeanDefinition(ConfigurationBeanBindingPostProcessor)
    role = ROLE_INFRASTRUCTURE
```

---

## 6. PostProcessor 绑定（`ConfigurationBeanBindingPostProcessor`）

### 6.1 识别 Configuration Bean

```
isConfigurationBean(bean, beanDefinition):
    beanDefinition.source == EnableConfigurationBeanBinding.class
    && userClass(bean).name == beanDefinition.beanClassName
```

### 6.2 绑定时机

`postProcessBeforeInitialization`：

```
configurationProperties = beanDefinition attribute
binder = getConfigurationBeanBinder()   // Bean 或 DefaultConfigurationBeanBinder
binder.bind(map, ignoreUnknown, ignoreInvalid, bean)
for each ConfigurationBeanCustomizer (Ordered):
    customizer.customize(beanName, bean)
```

### 6.3 `DefaultConfigurationBeanBinder`

基于 Spring `DataBinder`：

```
DataBinder(bean)
    .setIgnoreUnknownFields / setIgnoreInvalidFields
    .initDirectFieldAccess()
    .setConversionService(from ConversionServiceResolver)
    .bind(MutablePropertyValues(configurationProperties))
```

### 6.4 与 `RoseBinder` 的边界

| 能力 | `@EnableConfigurationBeanBinding` | `RoseBinder`（Boot） |
|------|-----------------------------------|----------------------|
| 场景 | 注册 **Spring Bean** | 一次性读取配置值 |
| 依赖 | `spring-context` | `spring-boot` |
| 热更 | §7 `Refreshable` | 无 |

---

## 7. Env 热更对接（Phase 2，依赖 env-refresh）

> env-refresh [§5.7–§5.8](./rose-spring-env-refresh-design.md#57-refreshable-spi) 就绪后实现。

### 7.1 Registrar 元数据（Phase 2 前置）

在 §5.5 注册 BeanDefinition 时 **追加 attribute**：

| Attribute | 值 |
|-----------|-----|
| `configurationBindingPrefix` | 注解 `prefix`（已 resolvePlaceholders） |
| `configurationBindingMultiple` | `multiple` 标志 |

`ConfigurationBeanBindingRefreshable.supports(changedKeys)`：任一 key 满足 `key.startsWith(prefix + ".")` 或 `key.equals(prefix)` 即 true。

### 7.2 `ConfigurationBeanBindingRefreshable`

```java
package io.zhijun.spring.core.binder.refresh;

public final class ConfigurationBeanBindingRefreshable implements Refreshable {

    @Override
    public boolean supports(Set<String> changedKeys) {
        for (String beanName : RefreshableContextHolder.getApplicationContext().getBeanDefinitionNames()) {
            BeanDefinition def = getDefinition(beanName);
            if (def == null || !isConfigurationBeanDefinition(def)) {
                continue;
            }
            String prefix = getPrefix(def);
            for (String key : changedKeys) {
                if (key.equals(prefix) || key.startsWith(prefix + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void refresh(Set<String> changedKeys) {
        ApplicationContext ctx = RefreshableContextHolder.getApplicationContext();
        ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory) ctx.getAutowireCapableBeanFactory();
        ConfigurationBeanBindingPostProcessor processor = bf.getBean(ConfigurationBeanBindingPostProcessor.class);
        for (String beanName : bf.getBeanDefinitionNames()) {
            BeanDefinition def = bf.getBeanDefinition(beanName);
            if (!isConfigurationBeanDefinition(def)) {
                continue;
            }
            String prefix = getPrefix(def);
            if (!prefixAffected(prefix, changedKeys)) {
                continue;
            }
            rebind(ctx, processor, beanName, def, prefix);
        }
    }
}
```

**`rebind` 算法：**

```
1. configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix)
2. if multiple:
       subProperties = resolveSubProperties(beanName, configurationProperties)  // 同 §5.4
   else:
       subProperties = configurationProperties
3. bean = ctx.getBean(beanName)
4. processor.getConfigurationBeanBinder().bind(subProperties, ignoreUnknown, ignoreInvalid, bean)
5. processor.getConfigurationBeanCustomizers() 按 Ordered 再执行 customize(beanName, bean)
```

**不**重新注册 BeanDefinition；**不**调用 `@PostConstruct`；Customizer **会**再次执行（与首次绑定一致）。

### 7.3 spring.factories

```properties
io.zhijun.spring.core.env.refresh.Refreshable=\
io.zhijun.spring.core.binder.refresh.ConfigurationBeanBindingRefreshable
```

### 7.4 非目标（本 Phase 2）



- 不自动 rebind Boot `@ConfigurationProperties` Bean
- 不触发 `@Configuration` 类 re-parse

---

## 8. 测试矩阵

| # | 测试类 | 场景 | 期望 |
|---|--------|------|------|
| 1 | `ConfigurationBeanBindingRegistrarTest` | 单 prefix + type | 1 Bean，属性绑定 |
| 2 | `EnableConfigurationBeanBindingTest` | Customizer | customize 被调用 |
| 3 | `EnableConfigurationBeanBindingMultipleTest` | multiple | 多 Bean |
| 4 | `EnableConfigurationBeanBindingAliasTest` | AliasGenerator SPI | alias 注册 |
| 5 | `EnableConfigurationBeanBindingsTest` | 容器注解 | 等价多个 @Import |
| 6 | `ConfigurationBeanBindingRefreshableTest` | Phase 2 | prefix 变更 → 字段更新 |

---

## 9. 实现检查清单

- [ ] §5 Registrar 行为与现有测试一致
- [ ] §6 PostProcessor 与 `ConversionServiceResolver` 文档一致
- [ ] env-refresh 合并后实现 §7（可选 Phase 2 PR）
- [ ] `rose-spring/README.md` 指向本规格

---

## 10. 开工清单

1. 分支：`feature/rose-spring-core-config-binding-spec`（或合入 env-refresh 分支）
2. Phase 2：env-refresh §5 全绿后追加 `ConfigurationBeanBindingRefreshable`
3. `mvn -pl rose-spring/rose-spring-core test`
