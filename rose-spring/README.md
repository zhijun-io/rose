# Rose Spring

Rose Spring 是 Rose 的 Spring 扩展模块，提供可监听的环境（Listenable Environment）与增强型 PropertySource 注解。

## 实现规格（`rose-spring-core`）

| 规格文档 | 逻辑子域 | 包 | 状态 |
|----------|----------|-----|------|
| [env-refresh](../docs/rose-spring-env-refresh-design.md) | 可刷新环境 | `core.env.*` | §5 待实现 |
| [property-source](../docs/rose-spring-property-source-design.md) | PropertySource 增强 | `core.propertysource.*` | Phase 2（R2 包迁移 ✅） |
| [configuration-bean-binding](../docs/rose-spring-configuration-bean-binding-design.md) | `@EnableConfigurationBeanBinding` | `core.binder.*` | ✅ 已实现 |

**实施顺序：** env-refresh §5 → property-source §6–§11 → i18n §6 对接。

## 相关规格（其他模块 / 规划中）

| 规格文档 | 说明 |
|----------|------|
| [bootstrap-diagnostics](../docs/rose-spring-boot-bootstrap-diagnostics-design.md) | Bootstrap 模式、FailureAnalyzer、启动顺序 |
| [web-handler](../docs/rose-spring-web-handler-design.md) | Endpoint registry + HandlerMethod SPI（规划） |
| [cache TTL](../docs/rose-cache-design.md) | `@TTLCacheable`（规划） |
| [i18n](../docs/rose-i18n-design.md) | 国际化（规划） |

## 模块

| Artifact | 说明 |
|----------|------|
| `rose-spring-core` | Listenable Environment、PropertySource 注解、Configuration Bean Binding（单一 artifact） |

## 依赖

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-core</artifactId>
</dependency>
```

通过 `rose-bom` 对齐版本。若已引入 `rose-spring-boot-starter` 或 `rose-spring-boot`，则无需单独声明。

**要求：** Java 8+，Spring Framework 5.x（与项目所用 Spring Boot 版本一致）。

---

## 特性

### 1. Listenable Environment

在标准 `ConfigurableEnvironment` 之上增加监听能力，可观测 profile 切换、`PropertySource` 增删改，以及属性读取。

**自动接入：** `META-INF/spring.factories` 注册 `ListenableConfigurableEnvironmentInitializer`，应用启动时自动包装环境。

**扩展监听器：** 在 `META-INF/spring.factories` 中注册实现类：

```properties
io.zhijun.spring.core.env.EnvironmentListener=your.pkg.YourEnvironmentListener
io.zhijun.spring.core.env.ProfileListener=your.pkg.YourProfileListener
io.zhijun.spring.core.env.PropertyResolverListener=your.pkg.YourPropertyResolverListener
```

默认已注册 `LoggingEnvironmentListener`（`slf4j` 输出访问与变更日志）。

**配置刷新（实现规格）：** 见 [Env 刷新实现规格](../docs/rose-spring-env-refresh-design.md)。`PropertySourcesChangedEvent#getChangedKeys()` 提供 key 级 diff；`Refreshable` SPI 由 `PropertySourcesRefreshEnvironmentListener` 编排。

**PropertySource 注解（实现规格）：** 见 [PropertySource 实现规格](../docs/rose-spring-property-source-design.md)。

**Configuration Bean Binding（实现规格）：** 见 [configuration-bean-binding 规格](../docs/rose-spring-configuration-bean-binding-design.md)。

**关键类型：**

| 类型 | 包路径 | 职责 |
|------|--------|------|
| `ListenableConfigurableEnvironment` | `core.env` | 代理包装，拦截环境读取与变更 |
| `ListenableMutablePropertySources` | `core.env` | 监听 `PropertySource` 增删改 |
| `PropertySourceChangedEvent` / `PropertySourcesChangedEvent` | `core.env.event` | 单次 / 批量变更事件 |
| `Refreshable` / `PropertySourcesRefreshEnvironmentListener` | `core.env.refresh` | 配置刷新 SPI（见 Env 刷新规格） |
| `YamlPropertySourceFactory` / `JsonPropertySourceFactory` | `propertysource.support` | YAML / JSON 属性源工厂 |
| `DefaultResourceComparator` | `propertysource.support` | 通配符资源排序 |
| `SpringFactoriesLoaderUtils` | `core.io.support` | 统一 SPI 加载入口 |

---

### 2. Enhanced `@PropertySource`

对标 Spring 标准 `@PropertySource`，并借鉴 [microsphere-spring](https://github.com/microsphere-projects/microsphere-spring) 的 `@ResourcePropertySource` 设计，补齐以下能力：

| 能力 | 标准 `@PropertySource` | Rose |
|------|------------------------|------|
| 元注解组合（如 `@YamlPropertySource`） | 不支持 | 支持 |
| `PropertySource` 顺序（`first` / `before` / `after`） | 不支持 | 支持 |
| 资源通配符（`classpath*:/config/*.yaml`） | 不支持 | 支持 |
| 可选资源（`ignoreResourceNotFound`） | 支持 | 支持 |
| 文件变更自动刷新（`autoRefreshed`） | 不支持 | 支持 |
| 自定义 `PropertySourceFactory` | 支持 | 支持 |
| `@Inherited` | 不支持 | 支持 |

在 `@Configuration` 类上使用：

```java
@Configuration
@YamlPropertySource("classpath:application.yml")
public class AppConfig {
}
```

#### `@ResourcePropertySource`

通用增强属性源，默认使用 `DefaultPropertySourceFactory` 加载 `.properties`。

```java
@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/config/*.properties",
        first = true,
        ignoreResourceNotFound = false,
        encoding = "UTF-8")
public class AppConfig {
}
```

可重复标注，或使用容器注解：

```java
@ResourcePropertySources({
        @ResourcePropertySource("classpath:defaults.properties"),
        @ResourcePropertySource("classpath:overrides.properties")
})
```

#### `@YamlPropertySource`

YAML 属性源，内部元注解 `@ResourcePropertySource(factory = YamlPropertySourceFactory.class)`。

```java
@Configuration
@YamlPropertySource("classpath:application.yml")
public class AppConfig {
}
```

嵌套结构会扁平化为 `app.name`、`app.nested.enabled` 等形式。

#### `@JsonPropertySource`

JSON 属性源，内部元注解 `@ResourcePropertySource(factory = JsonPropertySourceFactory.class)`。

```java
@Configuration
@JsonPropertySource("classpath:config.json")
public class AppConfig {
}
```

#### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `String[]` | `{}` | 资源位置，支持 `${...}` 占位符与通配符 |
| `name` | `String` | `""` | `PropertySource` 名称；空则使用 `类名@注解名` |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory` | 属性源工厂 |
| `encoding` | `String` | `${file.encoding:UTF-8}` | 文件编码 |
| `first` | `boolean` | `false` | 置于最前（与 `before`/`after` 互斥） |
| `before` | `String` | `""` | 插入到指定 `PropertySource` 之前 |
| `after` | `String` | `""` | 插入到指定 `PropertySource` 之后 |
| `ignoreResourceNotFound` | `boolean` | `false` | 资源不存在时是否忽略 |
| `autoRefreshed` | `boolean` | `false` | 监听文件变更并热更新（仅文件类资源） |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator` | 通配符匹配多资源时的排序 |

#### 加载机制

```
@Configuration 类
    │
    ├─ @ResourcePropertySource ──► ResourcePropertySourceLoader (ImportSelector)
    ├─ @ResourcePropertySources ──► ResourcePropertySourcesLoader ──► 委托 Loader
    ├─ @YamlPropertySource ──────► 元注解 @ResourcePropertySource ──► 同上
    └─ @JsonPropertySource ──────► 元注解 @ResourcePropertySource ──► 同上
```

- 在 `selectImports` 阶段通过 `EnvironmentAware` 获取 `Environment` 并注册 `PropertySource`。
- `autoRefreshed = true` 时由 `AutoRefreshWatcher` 监听文件，`AutoRefreshWatcherLifecycle` 在 Context 关闭时释放资源。

---

### 3. `@EnableConfigurationBeanBinding`

将 `Environment` 中指定前缀的属性绑定为 Spring Bean。  
**实现规格：** [rose-spring-configuration-bean-binding-design.md](../docs/rose-spring-configuration-bean-binding-design.md)

```java
@Configuration
@EnableConfigurationBeanBinding(prefix = "usr", type = UserProperties.class)
public class AppConfig {
}
```

`application.properties`：

```properties
usr.id = main
usr.name = rose
usr.age = 1
```

#### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `prefix` | `String` | — | 属性前缀 |
| `type` | `Class<?>` | — | 绑定目标类型 |
| `multiple` | `boolean` | `false` | 是否按子前缀绑定多个 Bean |
| `ignoreUnknownFields` | `boolean` | `true` | 忽略未知字段 |
| `ignoreInvalidFields` | `boolean` | `true` | 忽略无效字段 |

#### 扩展点

| 类型 | 包路径 | 说明 |
|------|--------|------|
| `ConfigurationBeanBinder` | `context.config` | 自定义绑定逻辑 |
| `ConfigurationBeanCustomizer` | `context.config` | 绑定后回调（支持 `Ordered`） |
| `ConfigurationBeanAliasGenerator` | `core.binder.support` | Bean 别名生成（`spring.factories` SPI） |

可重复标注，或使用 `@EnableConfigurationBeanBindings` 容器注解。

## 包结构（对齐 microsphere）

```
io.zhijun.spring
├── core
│   ├── binder
│   │   ├── annotation         # @EnableConfigurationBeanBinding、Registrar、PostProcessor
│   │   ├── config             # ConfigurationBeanBinder、Customizer
│   │   └── support            # ConfigurationBeanAliasGenerator、ConversionServiceResolver
│   ├── config                 # PropertyAdapter
│   ├── env                    # Listenable Environment、PropertySourcesUtils
│   │   ├── listener/
│   │   ├── event/
│   │   ├── support/
│   │   └── refresh/           # 配置刷新 SPI（Refreshable、EnvironmentListener）
│   ├── propertysource/        # @ResourcePropertySource 主包
│   │   ├── annotation/
│   │   ├── support/
│   │   └── watch/             # 文件触发（autoRefreshed）
│   ├── convert/support
│   └── io
│       ├── support            # SpringFactoriesLoaderUtils
│       └── watch              # 文件监听底座
└── context/config             # ConfigurationBeanBinder 接口（legacy，与 core.binder 并存）
```

### 借鉴 Microsphere

- **Listenable Environment：** `EnvironmentListener` / `ProfileListener` / `PropertyResolverListener` 三层模型，`spring.factories` + 环境代理统一分发。
- **Enhanced PropertySource：** `AnnotatedPropertySourceImportSelector` + `ImportSelector` 加载链路；单注解与 `@ResourcePropertySources` 容器分离委托。
- **Configuration Bean Binding：** `ImportBeanDefinitionRegistrar` 注册 Bean 定义 + `ConfigurationBeanBindingPostProcessor` 完成属性绑定。

### 简化处理

- 未引入 microsphere 的 `BeanCapableImportCandidate` 全 Bean 生命周期注册。
- `auto-refresh` 使用静态注册表 + `ContextClosedEvent` 清理，而非 `DisposableBean` 自注册。
- `@YamlPropertySource` / `@JsonPropertySource` 不重复声明 `@Import`，仅通过元注解 `@ResourcePropertySource` 触发加载。
- `PropertySourcesUtils` 仅保留绑定所需的最小 API（`getSubProperties` / `normalizePrefix`）。
- `ConversionServiceResolver` 精简为 BeanFactory / Environment 查找，不注册额外单例。
- 全部按 **Java 8** 实现。

### 测试覆盖

| 测试类 | 覆盖 |
|--------|------|
| `ListenableMutablePropertySourcesTests` | PropertySource 变更事件 |
| `ListenableConfigurableEnvironmentTests` | 环境代理与监听 |
| `ListenableConfigurableEnvironmentInitializerTests` | 启动时自动包装 |
| `SpringFactoriesLoaderUtilsTests` | SPI 加载 |
| `FileWatchServiceTests` | 文件监听底座 |
| `PropertySourceFactoryTests` | YAML / JSON 工厂（`propertysource.support`） |
| `ResourcePropertySourceLoaderTests` | 加载、排序、通配符、元注解、repeatable、ignoreResourceNotFound |
| `AutoRefreshWatcherTest` | 文件变更触发 reload（`propertysource.watch`） |
| `AutoRefreshWatcherLifecycleTest` | Context 关闭清理 watcher（`propertysource.watch`） |
| `EnableConfigurationBeanBindingTest` | 单 Bean 绑定 + Customizer |
| `EnableConfigurationBeanBindingMultipleTest` | `multiple = true` 多 Bean |
| `EnableConfigurationBeanBindingAliasTest` | 别名 SPI |
| `EnableConfigurationBeanBindingsTest` | 容器注解 |
| `ConfigurationBeanBindingRegistrarTest` | Registrar 单测 |
| `PropertySourcesUtilsTests` | 前缀属性提取 |
