# Rose Spring Configuration Bean Binding — 设计方案

> **Artifact：** `rose-spring-core`（**不**单独拆 Maven 模块）  
> **逻辑子域：** `configuration-bean-binding`（包 `io.zhijun.spring.core.binder.*`）  
> **前置：** [env-refresh 设计](./rose-spring-env-refresh-design.md)（热更走 `Refreshable` 响应链）  
> **定位：** 声明式将 `Environment` 前缀属性绑定为 Spring Bean（`@EnableConfigurationBeanBinding`）。

---

## 1. 背景

Spring Boot `@ConfigurationProperties` 需类上注解 + `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan`
。Rose 提供 **Import 驱动** 的绑定方式，借鉴 microsphere，适用于：

- 纯 Spring Framework 项目（无 Boot）
- 同一前缀绑定 **多个** Bean（`multiple = true`）
- 与 `PropertySourcesUtils` / Listenable Environment 同栈

绑定在启动时由 Registrar 注册 Bean、PostProcessor 完成属性注入；配置变更时由 `ConfigurationBeanBindingRefreshable` 在同一
Bean 实例上 **rebind**，不重新解析 `@Configuration`。

---

## 2. 目标与非目标

### 2.1 目标

| 目标     | 说明                                                                                            |
|--------|-----------------------------------------------------------------------------------------------|
| 声明式绑定  | `@EnableConfigurationBeanBinding(prefix, type)`                                               |
| 多 Bean | `multiple = true` 按一级子前缀拆分                                                                    |
| 扩展     | `ConfigurationBeanBinder` / `ConfigurationBeanCustomizer` / `ConfigurationBeanAliasGenerator` |
| Env 热更 | prefix 相关 key 变更 → `Refreshable` rebind                                                       |
| Java 8 | 与 Rose 基线一致                                                                                   |

### 2.2 非目标

- 替代 Boot `@ConfigurationProperties` + `@Validated` 全特性（JSR-303 可后续加）
- 绑定非 `Map` 结构的动态 schema
- 自动 rebind Boot `@ConfigurationProperties` Bean
- 触发 `@Configuration` 类 re-parse 或 `@PostConstruct`

---

## 3. 架构与包结构

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
├── support/
│   ├── ConfigurationBeanBindingSupport.java   # 注册 / 热更共享逻辑
│   ├── ConfigurationBeanAliasGenerator.java
│   ├── DefaultConfigurationBeanAliasGenerator.java
│   ├── ConversionServiceResolver.java
│   └── *AliasGenerator.java
└── refresh/
    └── ConfigurationBeanBindingRefreshable.java
```

**协作类型：**

| 类型                                                          | 包                       | 职责                                     |
|-------------------------------------------------------------|-------------------------|----------------------------------------|
| `PropertySourcesUtils`                                      | `core.env`              | `getSubProperties` / `normalizePrefix` |
| `Refreshable` / `PropertySourcesRefreshEnvironmentListener` | `core.env.refresh`      | 变更 key 分发                              |
| `RoseBinder`                                                | `rose-spring-boot-core` | Boot 侧 `Binder` 便捷封装；**不**参与本注解绑定链     |

**热更链路：**

```
PropertySource 变更 → getChangedKeys()
    → PropertySourcesRefreshEnvironmentListener
        → ConfigurationBeanBindingRefreshable.refresh(keys)
            → PostProcessor.rebindConfigurationBean(...)
```

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

容器注解，`value()` 为 `@EnableConfigurationBeanBinding[]`；由 `ConfigurationBeanBindingsRegistrar` 按数组顺序委托
Registrar。

---

## 5. Registrar 契约

### 5.1 属性解析

| 属性                                            | 规则                                   |
|-----------------------------------------------|--------------------------------------|
| `prefix`                                      | 必填；`environment.resolvePlaceholders` |
| `type`                                        | 必填；绑定目标类                             |
| `multiple`                                    | 默认 `false`                           |
| `ignoreUnknownFields` / `ignoreInvalidFields` | 默认 `true`                            |

`configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix)`。

### 5.2 Bean 名称

**`multiple = false`：**

- `properties["id"]` 为非空 String → beanName = id
- 否则 → `generateBeanName(RootBeanDefinition(type), registry)`
- `subProperties` = 整棵 `configurationProperties`

**`multiple = true`：**

- 遍历 `configurationProperties` 的 key，取第一个 `.` 前的一级段作为 beanName（`LinkedHashSet` 保序）
- 每个 beanName 单独注册，类型均为 `type`
- `subProperties` = `resolveSubProperties(beanName, configurationProperties)`（§5.3）

示例：`prefix=usr`，properties 含 `usr.id`、`usr.name` → Bean 名 `usr`（不含其他顶层段）。

### 5.3 `resolveSubProperties`（multiple）

```
propertySources = MutablePropertySources + MapPropertySource("_", configurationProperties)
return PropertySourcesUtils.getSubProperties(propertySources, environment, normalizePrefix(beanName))
```

### 5.4 BeanDefinition 元数据

| Attribute                                     | 内容                                           |
|-----------------------------------------------|----------------------------------------------|
| `source`                                      | `EnableConfigurationBeanBinding.class`（识别标记） |
| `configurationProperties`                     | 待绑定的 flat `Map<String,Object>`               |
| `configurationBindingPrefix`                  | 已 resolve 的 `prefix`                         |
| `configurationBindingMultiple`                | `multiple` 标志                                |
| `ignoreUnknownFields` / `ignoreInvalidFields` | boolean                                      |

### 5.5 别名与基础设施

- 每个 bean 经 `ConfigurationBeanAliasGenerator` SPI（`spring.factories`）注册别名。
- 若尚无 `configurationBeanBindingPostProcessor`，注册 `ConfigurationBeanBindingPostProcessor`（`ROLE_INFRASTRUCTURE`）。

---

## 6. PostProcessor 契约

### 6.1 识别 Configuration Bean

```
beanDefinition.source == EnableConfigurationBeanBinding.class
&& userClass(bean).name == beanDefinition.beanClassName
```

### 6.2 首次绑定

`postProcessBeforeInitialization`：

1. 从 BeanDefinition attribute 读取 `configurationProperties` 与 ignore 标志
2. `ConfigurationBeanBinder.bind(...)`（默认 `DefaultConfigurationBeanBinder` + `ConversionServiceResolver`）
3. 按 `Ordered` 执行全部 `ConfigurationBeanCustomizer.customize(beanName, bean)`

### 6.3 `DefaultConfigurationBeanBinder`

基于 Spring `DataBinder`：`initDirectFieldAccess`、可选 `ConversionService`、`MutablePropertyValues` 绑定 flat map。

### 6.4 热更 rebind

`rebindConfigurationBean(beanName, environment)`：

1. `configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix)`
2. `multiple` 时 `subProperties = resolveSubProperties(beanName, ...)`，否则整棵子树
3. 对**已有** Bean 实例再次 `bind`
4. Customizer **再次**执行

**不**重新注册 BeanDefinition；**不**调用 `@PostConstruct`。

### 6.5 与 `RoseBinder` 的边界

| 能力 | `@EnableConfigurationBeanBinding`     | `RoseBinder`（Boot） |
|----|---------------------------------------|--------------------|
| 场景 | 注册 **Spring Bean**                    | 一次性读取配置值           |
| 依赖 | `spring-context`                      | `spring-boot`      |
| 热更 | `ConfigurationBeanBindingRefreshable` | 无                  |

---

## 7. Env 热更契约

### 7.1 `ConfigurationBeanBindingRefreshable`

实现 `Refreshable`，经 `spring.factories` 注册。

**`supports(changedKeys)`：** 遍历 Configuration Bean 的 BeanDefinition；若任一 bean 的 `configurationBindingPrefix` 满足
`key.equals(prefix)` 或 `key.startsWith(prefix + ".")`，返回 true。

**`refresh(changedKeys)`：** 对每个受影响的 bean 调用 `PostProcessor.rebindConfigurationBean`；依赖
`RefreshableContextHolder` 获取 `ApplicationContext`。

### 7.2 非目标

- 不自动 rebind Boot `@ConfigurationProperties` Bean
- 不触发 `@Configuration` 类 re-parse

---

## 8. 决策摘要

| 决策        | 选择                                   | 理由                                           |
|-----------|--------------------------------------|----------------------------------------------|
| 注册方式      | `ImportBeanDefinitionRegistrar`      | 无 Boot 依赖，与 microsphere 一致                   |
| 绑定时机      | `BeanPostProcessor` before init      | 在 `@PostConstruct` 前完成首次绑定                   |
| 热更        | 同实例 rebind + Customizer 重跑           | 避免 Bean 生命周期重复，行为可预期                         |
| prefix 匹配 | 全前缀或 `prefix.` 子树                    | 与 `getSubProperties` 语义一致                    |
| 共享逻辑      | `ConfigurationBeanBindingSupport`    | Registrar 与 Refreshable 同一套 subProperties 规则 |
| SPI 扩展    | Binder / Customizer / AliasGenerator | 绑定算法与命名可替换                                   |
