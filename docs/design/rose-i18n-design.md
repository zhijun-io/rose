# Rose I18n 设计方案

> 主题：`rose-i18n`（独立主题聚合，与 `rose-opentelemetry`、`rose-multitenancy` 同级）  
> Phase 1 artifact：`rose-i18n-spring`（仅 Spring Framework，**无 Spring Boot 生产依赖**）  
> 定位：借鉴 [microsphere-i18n](https://github.com/microsphere-projects/microsphere-i18n) 的 Bundle 组合与
`MessageSource` 桥接，采用 Spring 友好资源约定与渐进式能力。

## 文档目录

| 章节      | 内容                                                               | Phase                                   |
|---------|------------------------------------------------------------------|-----------------------------------------|
| §1–§5   | 背景、模块、资源约定、核心模型、Spring 集成                                        | 概念                                      |
| §6–§10  | Boot 配置、热更、用法、阶段规划、扩展路线图                                         | 规划                                      |
| §11–§14 | 测试索引、文档交付、决策摘要（含 **§13.1 裁剪建议**）、开工清单                            | 辅助                                      |
| **§15** | Phase 1 可执行规格                                                    | 1                                       |
| **§16** | Phase 1.5 可执行规格                                                  | 1.5                                     |
| **§17** | Cloud 可执行规格                                                      | 3                                       |
| —       | [**PropertySource 规格**](./rose-spring-property-source-design.md) | `rose-spring-core` 内 property-source 子域 |
| —       | [**Env 刷新规格**](./rose-spring-env-refresh-design.md)              | `rose-spring-core` 内 env-refresh 子域     |

> 实现规格按 **§15 → §16 → §17** 顺序阅读；章节编号与 Phase 一致。

## 如何使用本文档编码

| 步骤       | 章节        | 说明                                                            |
|----------|-----------|---------------------------------------------------------------|
| 1        | **§15**   | **Phase 1 唯一实现规格**（POM、源文件树、类 API、算法、Bean 注册、测试矩阵）            |
| 2        | **§16**   | **Phase 1.5 规格**（`EnvironmentMessageBundle`、YAML、迁移回退、Env 热更） |
| 3        | §3        | 资源路径与 key 约定（与 §15.5 / §16.5 一致）                              |
| 4        | §11       | 测试类与断言（与 §15.10 / §16.10 一致）                                  |
| 5        | §14       | 开工顺序与需改动的仓库文件                                                 |
| —        | **§13.1** | 复杂度评估与可裁剪项（控制 Phase 范围）                                       |
| —        | §6–§10    | **Phase 2 Boot 规划**；Phase 1 / 1.5 编码时跳过                       |
| Cloud 实现 | **§17**   | `rose-i18n-spring-cloud` 规格（依赖 Phase 1 + **§16**）             |

**验收命令：**

```bash
# Phase 1
mvn -pl rose-i18n/rose-i18n-spring test

# Phase 1.5（同一模块，追加 §16 类与测试）
mvn -pl rose-i18n/rose-i18n-spring test
```

---

## 1. 背景与目标

### 1.1 为什么要做

- Rose 已有 `rose-spring-core`（可监听 Environment、增强 PropertySource、文件刷新），具备 i18n 热更新与 Env 覆盖的基础。
- microsphere-i18n 验证了 `ServiceMessageSource` + 组合优先级 + Spring `MessageSource` 桥接的可行性，但路径约定偏重、Cloud
  模块过重，与 Rose（Java 8、务实分层）不完全匹配。

### 1.2 设计目标

| 目标            | 说明                                                  |
|---------------|-----------------------------------------------------|
| **主题聚合**      | 国际化相关模块统一放在 `rose-i18n/` 下，不并入 `rose-spring`        |
| **Spring 对齐** | 默认 `messages` basename、`{0}` 占位符、标准 `MessageSource` |
| **Bundle 隔离** | 多模块 / 多 JAR 按 bundle 分域，支持优先级覆盖                     |
| **简单默认**      | 单应用 `@EnableI18n` 即可用                               |
| **路径可配**      | 默认 `i18n/`，库内嵌可选 `META-INF/i18n/`                   |
| **渐进增强**      | Phase 1 classpath → Phase 2 热更/Env → Phase 3 观测（可选） |
| **Java 8**    | 与 Rose 基线一致                                         |

### 1.3 非目标（首版不做）

- i18n Server / 集中式消息服务
- Spring Cloud Feign 专用模块
- 独立 Actuator Endpoint（可 Phase 3）
- 占位符 `{}` 作为默认（仅兼容模式）

---

## 2. 模块结构

### 2.1 主题聚合（Rose 总设计对齐）

```
rose/                                 # rose-parent
├── rose-spring/                      # Spring 平台扩展（Environment、PropertySource…）
├── rose-spring-boot/                 # Boot 自动配置（与 i18n 并列，非 i18n 归属）
├── rose-i18n/                        # ★ 国际化主题（新建）
│   ├── pom.xml                       # packaging=pom
│   ├── rose-i18n-spring/             # Phase 1：Framework 集成
│   ├── rose-i18n-spring-boot/        # Phase 2+：rose.i18n.* 自动配置
│   └── rose-i18n-spring-cloud/       # 将来：Refresh / 配置中心
└── pom.xml                           # <module>rose-i18n</module>
```

**原则：**

| 模块                       | 职责                                                                          | 依赖                                |
|--------------------------|-----------------------------------------------------------------------------|-----------------------------------|
| `rose-i18n-spring`       | `MessageBundle`、`@EnableI18n`、`RoseMessageSource`；Phase 1.5 增 Env/YAML（§16） | `spring-context`                  |
| `rose-i18n-spring-boot`  | `I18nAutoConfiguration`、`rose.i18n.*`                                       | Boot + `rose-i18n-spring`         |
| `rose-i18n-spring-cloud` | `EnvironmentChangeEvent` → 刷新 Env bundle（§17）                               | Cloud + Boot + `rose-i18n-spring` |
| `rose-spring-core`       | 可选协作：Env/文件变更事件（§16.8）                                                      | **非 i18n 父模块**                    |

i18n **不**作为 `rose-spring/pom.xml` 的 submodule。

### 2.2 Phase 1 依赖关系

```
rose-i18n-spring
  ├── spring-context
  └── slf4j-api
```

Phase 1.5 可选引入 `rose-spring-core`（§16.8 事件）。Phase 1 **不**依赖 `rose-core`、`spring-boot`（生产）。

测试：`spring-test`、`junit-jupiter`、`assertj`（**不用** `spring-boot-starter-test`，保持纯 Spring 集成测）。

### 2.3 包结构（Phase 1 实现范围）

```
rose-i18n/rose-i18n-spring/src/main/java/io/zhijun/i18n/
├── MessageBundle.java
├── KeyPrefixMode.java
├── PlaceholderStyle.java
├── LoadStrategy.java
├── ClasspathMessageBundle.java
├── CompositeMessageBundle.java
├── loader/
│   └── PropertiesMessageBundleLoader.java
├── support/
│   ├── PlaceholderResolver.java
│   ├── MessageFormatResolver.java
│   └── Slf4jStyleResolver.java
└── spring/
    ├── annotation/
    │   ├── EnableI18n.java
    │   ├── I18nBundle.java
    │   └── I18nBundleRegistrar.java
    ├── I18nEagerLoadInitializer.java     # EAGER 时注册
    └── RoseMessageSource.java

rose-i18n/rose-i18n-spring/src/test/java/io/zhijun/i18n/
├── （与 main 对称，见 §15.9）
```

Phase 1.5+ 在 **`rose-i18n-spring-boot`** 增加 `I18nAutoConfiguration`；Cloud 在 **`rose-i18n-spring-cloud`**（§9）。

---

## 3. 资源约定

### 3.1 默认约定（应用侧推荐）

```
src/main/resources/
└── i18n/
    ├── common/
    │   ├── messages.properties
    │   ├── messages_zh_CN.properties
    │   └── messages_en.properties
    └── app/
        ├── messages.properties
        └── messages_zh_CN.properties
```

**默认 basename：**

```
classpath*:i18n/{bundle}/messages
```

Locale 文件名遵循 Java `ResourceBundle` 规则：`messages` + `_` + `locale`（如 `zh_CN`）。

### 3.2 库内嵌约定（可选）

库 JAR 自带默认文案：

```
META-INF/i18n/{bundle}/messages_zh_CN.properties
```

**basename：**

```
classpath*:META-INF/i18n/{bundle}/messages
```

通过 `@I18nBundle` 或配置显式声明，不作为应用默认。

### 3.3 为什么默认不用 META-INF

| 说法                              | 是否成立        |
|---------------------------------|-------------|
| META-INF 设计不合理                  | ❌ 在库/框架场景合理 |
| Rose 默认不应锁死 META-INF            | ✅           |
| Rose 应支持可配置 basename，含 META-INF | ✅           |

**META-INF 适合：** 库 JAR 内嵌默认文案、不占应用 resources 命名空间、与 microsphere 迁移对齐。

**`i18n/` 适合：** 应用业务文案、IDE 可发现、Spring 生态习惯、热更新开发体验。

多 JAR 合并靠的是 `classpath*:` + 固定相对路径，**不依赖 META-INF 本身**。

### 3.4 与 microsphere 对照

| 维度      | microsphere-i18n                                           | Rose                                         |
|---------|------------------------------------------------------------|----------------------------------------------|
| 默认路径    | `META-INF/i18n/{source}/i18n_messages_{locale}.properties` | `i18n/{bundle}/messages_{locale}.properties` |
| 术语      | source                                                     | bundle                                       |
| 文件内 key | 必须 `{source}.` 前缀                                          | 推荐短 key，加载时加 `{bundle}.` 前缀                  |
| 占位符     | `{}`                                                       | `{0}`（MessageFormat，默认）                      |
| 路径      | 固定                                                         | **basename 可配**                              |

### 3.5 文件内容规范

```properties
# i18n/app/messages_zh_CN.properties
welcome=欢迎
order.notFound=订单 {0} 不存在
error.validation=参数 {0} 无效：{1}
```

- 文件内 **不写** `app.` 前缀（默认 `key-prefix-mode: bundle`）
- 内存中规范化为：`app.welcome`、`app.order.notFound`
- 兼容模式 `key-prefix-mode: qualified`：文件内 **每个** key 必须以 `{bundle}.` 开头，否则加载时 `IllegalStateException`

### 3.6 多 JAR 合并

同一 basename 使用 `classpath*:`，`ClassLoader.getResources()` 加载多份 properties，**后加载覆盖先加载**（与 microsphere
一致）。

> **注意：** `getResources()` 返回顺序**未在 JVM 规范中保证**。实现时应按 URL/JAR 路径做**稳定排序**后再 merge，并在测试中固定
> classpath 顺序；不要依赖「碰巧」的覆盖顺序。

典型场景：

```
lib-common.jar   → i18n/common/messages_zh_CN.properties
app.jar          → i18n/common/messages_zh_CN.properties  # 覆盖部分 key
app.jar          → i18n/app/messages_zh_CN.properties
```

### 3.7 资源格式与 Loader SPI

**Phase 1 只实现 `.properties`**，由 `PropertiesMessageBundleLoader` 加载；Phase 1.5 再抽 `MessageBundleLoader` SPI 并增加
YAML。

**YAML 示例**（扁平化后与 properties 共用 lookup 逻辑，复用 `rose-spring-core` 的 `PropertySourceMaps.flatten`）：

```yaml
# i18n/app/messages_zh_CN.yml
welcome: 欢迎
order:
  notFound: 订单 {0} 不存在
```

**文件命名：**

```
i18n/{bundle}/messages_zh_CN.properties
i18n/{bundle}/messages_zh_CN.yml
i18n/{bundle}/messages_zh_CN.yaml
```

**规则建议：**

| 规则                        | 说明                                  |
|---------------------------|-------------------------------------|
| 同一 bundle + locale 默认一种格式 | 避免 properties / yml 混用歧义            |
| 若混用                       | 显式配置 `format` 或优先级：后加载覆盖（与多 JAR 一致） |
| YAML 多文档 `---`            | 首版不支持，只读单个 Map                      |
| 编码                        | 默认 UTF-8，与 properties 一致            |

**注解扩展示例：**

```java
@I18nBundle(name = "app", format = Format.YAML)
```

或通过 basename 带后缀：`classpath*:i18n/app/messages` + loader 按 `messages_zh_CN.yml` 存在性探测。

---

## 4. 核心模型

### 4.1 MessageBundle

```java
public interface MessageBundle {
    String getName();
    int getPriority();                          // 越大越优先
    String getMessage(String code, Locale locale, Object... args);
    Optional<String> resolveRaw(String code, Locale locale);
    void refresh();                             // Phase 1：Classpath 清 cache；§16：Env 重建索引
}
```

### 4.2 ClasspathMessageBundle

| 属性                   | 说明                               |
|----------------------|----------------------------------|
| `name`               | bundle 名，如 `app`                 |
| `basename`           | 如 `classpath*:i18n/app/messages` |
| `encoding`           | 默认 UTF-8                         |
| `fallbackToLanguage` | `zh_CN` 未命中时尝试 `zh`              |
| `placeholderStyle`   | 默认 `MESSAGE_FORMAT`              |
| `keyPrefixMode`      | `BUNDLE` / `QUALIFIED`           |

**Locale 解析顺序（Phase 1）：**

1. 精确 locale（`zh_CN`）
2. 仅 language（`zh`）— `fallbackToLanguage=true` 时
3. 默认文件（`messages.properties`）

（`rose.i18n.default-locale` 为 Phase 2 配置项。）

### 4.3 CompositeMessageBundle

- 持有有序 `List<MessageBundle>`（按 `priority` 降序）
- `getMessage(code, locale, args)`：从高优先级到低优先级查找第一个命中
- 线程安全：Phase 1 只读缓存；`refresh()` 时整体替换不可变快照（copy-on-write）

### 4.4 Message Code 规则

| 调用方式                  | code 示例              | 行为                             |
|-----------------------|----------------------|--------------------------------|
| 全局（RoseMessageSource） | `app.order.notFound` | 直接查 composite                  |
| 全局短码 + defaultBundle  | `welcome`            | 先查 `defaultBundle`，再 composite |
| Bundle 作用域            | `order.notFound`     | 仅在指定 bundle 内查，自动加 `app.` 前缀   |

```java
// 推荐：全限定
messageSource.getMessage("app.order.notFound", new Object[]{id}, locale);

// 库内：注入具名 bundle（Bean 名默认 {name}MessageBundle）
@Autowired @Qualifier("orderMessageBundle") MessageBundle orderBundle;
orderBundle.getMessage("notFound", locale, id);
```

### 4.5 占位符

| 风格                   | 示例          | 用途                        |
|----------------------|-------------|---------------------------|
| `MESSAGE_FORMAT`（默认） | `Hello {0}` | Spring、Bean Validation 一致 |
| `SLF4J`              | `Hello {}`  | microsphere 迁移兼容          |

---

## 5. Spring 集成

### 5.1 `@EnableI18n`

```java
@SpringBootApplication
@EnableI18n(
    bundles = {
        @I18nBundle(name = "common", priority = 0),
        @I18nBundle(name = "app", priority = 100)
    },
    defaultBundle = "app"
)
public class Application { }
```

**最简用法：**

```java
@EnableI18n({"common", "app"})   // 等价 value = {"common", "app"}
```

等价于默认 basename `classpath*:i18n/{name}/messages`，priority 按声明顺序递增（0、100…）。

### 5.2 Registrar 注册的 Bean

| Bean                     | 类型                       | 说明                         |
|--------------------------|--------------------------|----------------------------|
| `{name}MessageBundle`    | `ClasspathMessageBundle` | 每个 bundle 一个               |
| `compositeMessageBundle` | `CompositeMessageBundle` | 聚合                         |
| `messageSource`          | `RoseMessageSource`      | `@Primary` `MessageSource` |

条件：`rose.i18n.enabled=true`（**Phase 2** 起通过配置关闭；Phase 1 由 `@EnableI18n` 启用，不依赖 Boot 配置绑定）。

### 5.3 RoseMessageSource

实现 `org.springframework.context.MessageSource`：

- 委托 `CompositeMessageBundle`
- `useCodeAsDefaultMessage` 可配（默认 false，未找到抛 `NoSuchMessageException` 或返回 code，与 Spring 行为对齐）
- 供 `LocalValidatorFactoryBean`、Thymeleaf、`MessageSourceAccessor` 使用

### 5.4 Bean Validation

不单独定义 `ValidationMessages` 路径。标准做法：

- 校验注解使用 `{app.constraint.xxx}` 形式（花括号为 Bean Validation 占位语法，非 SpEL）
- 或增加 bundle `validation`：`i18n/validation/messages*.properties`

无需额外 `BeanPostProcessor`（microsphere 那套可省略，降低复杂度）。

### 5.5 自动发现（`rose-i18n-spring-boot`，Phase 2+）

`META-INF/spring.factories` 注册在 **`rose-i18n-spring-boot`** 模块：

```properties
io.zhijun.i18n.spring.boot.autoconfigure.I18nAutoConfiguration=
```

规则：classpath 存在 `i18n/*/messages.properties` 且未声明 `@EnableI18n` 时，自动注册 bundle。**不在 `rose-i18n-spring`
中实现。**

### 5.6 接管 Spring 自带 MessageSource

Rose 的目标是让 **`RoseMessageSource` 成为应用主 `MessageSource`**，与 Boot / MVC / Validation 共用同一 Bean。

**方式 A — 替换（Phase 1 默认）**

由 `I18nBundleRegistrar` 注册名为 `messageSource` 的 `@Primary` `RoseMessageSource` Bean（见 §15.8），无需手写 `@Bean`。

- Bean 名 **`messageSource`**，与 Spring 惯例一致
- `@Primary` 保证注入优先于 Boot `MessageSourceAutoConfiguration` 注册的 Bean
- `LocalValidatorFactoryBean`、Thymeleaf `#messages`、`MessageSourceAccessor` 自动走 Rose

**方式 B — 组合回退（迁移期）**

```
CompositeMessageBundle（按 priority）
  ├── app / common（Rose bundle，高 priority）
  └── SpringMessageBundleAdapter（包装 Boot ResourceBundleMessageSource，低 priority）
```

Rose 未命中时回退 `spring.messages.basename`，便于从 `messages.properties` 渐进迁移。

**方式 C — 显式关闭 Boot 默认（最干净）**

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration
```

启用 `@EnableI18n` 后统一使用 `rose.i18n.*`，不再依赖 `spring.messages.*`。

### 5.7 与 `spring.messages.*` 的关系

| Spring Boot                                   | Rose 等价配置                                      |
|-----------------------------------------------|------------------------------------------------|
| `spring.messages.basename`                    | `rose.i18n.bundles[].basename` 或 `@EnableI18n` |
| `spring.messages.encoding`                    | `rose.i18n.bundles[].encoding`                 |
| `spring.messages.fallback-to-system-locale`   | `rose.i18n.default-locale` + bundle fallback   |
| `spring.messages.use-code-as-default-message` | `rose.i18n.use-code-as-default-message`        |

**文档约定：** 启用 Rose i18n 后以 `rose.i18n` 为准；遗留项目可用方式 B 短期并存。

---

## 6. 配置（Phase 2）

```yaml
rose:
  i18n:
    enabled: true
    default-locale: zh_CN
    default-bundle: app
    use-code-as-default-message: false
    placeholder-style: message-format    # message-format | slf4j
    key-prefix-mode: bundle              # bundle | qualified
    bundles:
      - name: common
        basename: classpath*:i18n/common/messages
        priority: 0
        encoding: UTF-8
      - name: app
        basename: classpath*:i18n/app/messages
        priority: 100
      - name: rose-i18n
        basename: classpath*:META-INF/i18n/rose-i18n/messages
        priority: -100                   # 库默认，应用可覆盖
```

**配置优先级：** `@I18nBundle` 属性 > `rose.i18n.bundles` > 约定默认 basename。

---

## 7. 与 rose-spring-core 联动

> **实现规格（`rose-spring-core` 内 env-refresh）：
** [rose-spring-env-refresh-design.md](./rose-spring-env-refresh-design.md)  
> 下文为概念说明；类名、SPI、算法以该文档 §5–§6 为准。

```
文件变更 / Env 变更 / Cloud refresh
        ↓
PropertySourcesChangedEvent / EnvironmentChangeEvent
        ↓
PropertySourcesRefreshEnvironmentListener（rose-spring-core）
        ↓
Refreshable.refresh(changedKeys)   ← EnvironmentMessageBundleRefreshable（rose-i18n）
        ↓
MessageBundle.refresh()
```

| 触发源              | 机制                                                                                                                                |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| 本地文件修改           | `@ResourcePropertySource(autoRefreshed)`（[property-source 规格](./rose-spring-property-source-design.md)）→ replace → env-refresh 事件 |
| Environment 属性变更 | `PropertySourcesChangedEvent.getChangedKeys()` → orchestrator                                                                     |
| Config / Cloud   | `EnvironmentChangeEvent` → orchestrator（§17 + 平台 §6.4）                                                                            |
| Env 覆盖 bundle    | `EnvironmentMessageBundle` 读 `rose.i18n.messages.*`（**§16**）                                                                      |

Phase 1 i18n 不实现 Env 覆盖；`ClasspathMessageBundle.refresh()` 在 Phase 1 仅清 cache（§15）。

---

## 8. 典型用法

### 8.1 应用

```java
@RestController
public class OrderController {
    private final MessageSource messages;

    public OrderController(MessageSource messages) {
        this.messages = messages;
    }

    @GetMapping("/orders/{id}")
    public String get(@PathVariable String id, Locale locale) {
        return messages.getMessage("app.order.notFound", new Object[]{id}, locale);
    }
}
```

### 8.2 库提供默认 + 应用覆盖

```
rose-order.jar     → i18n/order/messages_*.properties     (priority 50)
your-app.jar       → i18n/order/messages_*.properties     (priority 100, 覆盖)
your-app.jar       → i18n/app/messages_*.properties
```

### 8.3 microsphere 迁移

```java
@I18nBundle(
    name = "test",
    basename = "classpath*:META-INF/i18n/test/i18n_messages",
    keyPrefixMode = KeyPrefixMode.QUALIFIED,
    placeholderStyle = PlaceholderStyle.SLF4J
)
// 资源文件：META-INF/i18n/test/i18n_messages_zh_CN.properties
// key 示例：test.hello=您好
```

或一次性脚本迁移到 `i18n/{bundle}/messages_*` 并去掉 key 前缀。

---

## 9. 实施阶段

### 9.1 Phase 1 — MVP（首 PR）

| 项      | 内容                                                                                                |
|--------|---------------------------------------------------------------------------------------------------|
| 主题     | 新建 `rose-i18n/` 聚合 POM + `rose-i18n-spring`                                                       |
| 根 POM  | `<module>rose-i18n</module>`（**不**改 `rose-spring/pom.xml`）                                        |
| Core   | `MessageBundle`、`ClasspathMessageBundle`、`CompositeMessageBundle`、`PropertiesMessageBundleLoader` |
| Spring | `@EnableI18n`、`@I18nBundle`、`I18nBundleRegistrar`、`RoseMessageSource`、`LoadStrategy`              |
| 约定     | 默认 `classpath*:i18n/{bundle}/messages`                                                            |
| 测试     | §15.10 矩阵                                                                                         |

**预估类数：** ~12 个生产类 + 6~8 个测试类。

### 9.2 Phase 1.5 — Env / 格式 / 迁移（仍属 `rose-i18n-spring`）

| 项      | 内容                                                                                                                    |
|--------|-----------------------------------------------------------------------------------------------------------------------|
| 模块     | **`rose-i18n-spring`**（不新建 Boot 模块）                                                                                   |
| Core   | `MessageBundleLoader` SPI、`EnvironmentMessageBundle`、`SpringMessageBundleAdapter`                                     |
| Loader | `YamlMessageBundleLoader`（可选依赖 `rose-spring-core` 扁平化）                                                                |
| 热更     | `EnvironmentMessageBundleRefreshable`（`Refreshable` SPI，见 [`rose-spring-core` 规格](rose-spring-env-refresh-design.md)） |
| 规格     | **§16**（可执行）                                                                                                          |

**Cloud 前置：** `EnvironmentMessageBundle` 在 Phase 1.5 完成，**不**等到 Phase 2 Boot。

### 9.3 Phase 2 — Boot 与运行时

- 新建 **`rose-i18n-spring-boot`**：`I18nProperties`、`rose.i18n.*`、`I18nAutoConfiguration`
- `LoadStrategy` 可通过 `rose.i18n.load-strategy` 覆盖注解
- `@EnableI18n` / `rose.i18n.bundles` 声明式注册 Env bundle、YAML format、spring 回退
- Actuator `/actuator/i18n`（Phase 3 观测可与此合并）

### 9.4 Phase 3 — Cloud 与观测

- 新建 **`rose-i18n-spring-cloud`**：见 **§17**（`EnvironmentChangeEvent` → i18n reload）
- Actuator 端点（放在 `rose-i18n-spring-boot`）
- 可选：`rose-spring-boot-starter` 聚合 `rose-i18n-spring-boot` + `rose-i18n-spring-cloud`

---

## 10. 扩展能力路线图

核心思路：**`MessageBundle` 是可组合、可装饰、可 SPI 扩展的抽象**；Spring 层只负责注册与桥接 `MessageSource`，不把能力写死在
properties 加载里。

### 10.1 扩展点（SPI / 接口）

| 扩展点                                         | 用途                                  | 阶段   |
|---------------------------------------------|-------------------------------------|------|
| `MessageBundleLoader`                       | 新资源格式（YAML、JSON、XML）                | 1.5+ |
| `MessageBundle` 实现                          | 新数据来源                               | 2+   |
| `MessageBundleProvider`（`spring.factories`） | 第三方自动注册 bundle                      | 2+   |
| `PlaceholderResolver`                       | 自定义占位符（ICU、SLF4J、命名参数）              | 2+   |
| `MissingMessageHandler`                     | 未命中 key 时的日志 / 默认值 / 告警             | 2+   |
| `LocaleResolver` 联动                         | 与 Web `LocaleResolver`、租户 locale 统一 | 3+   |

### 10.2 MessageBundle 实现扩展

| 实现                           | 说明                                    | 场景                       |
|------------------------------|---------------------------------------|--------------------------|
| `ClasspathMessageBundle`     | classpath 文件                          | Phase 1 默认               |
| `EnvironmentMessageBundle`   | 从 `Environment` 读扁平 key（§16）          | 配置中心覆盖、A/B 文案            |
| `ReloadableMessageBundle`    | 装饰器，监听变更后 `refresh()`                 | 热更新                      |
| `CachingMessageBundle`       | 装饰器，缓存 resolve 结果                     | 高 QPS 读多                 |
| `SpringMessageBundleAdapter` | 包装 `ResourceBundleMessageSource`（§16） | 迁移回退                     |
| `RemoteMessageBundle`        | HTTP / gRPC 拉取（可选）                    | 集中管理，非 Phase 1 Server    |
| `TenantMessageBundle`        | 按租户选子 bundle                          | 与 `rose-multitenancy` 联动 |

**组合示例：**

```
CompositeMessageBundle
  ├── EnvironmentMessageBundle(app, priority=200)   # 配置中心优先
  ├── ClasspathMessageBundle(app, priority=100)
  ├── ClasspathMessageBundle(common, priority=0)
  └── SpringMessageBundleAdapter(spring.messages, priority=-50)
```

### 10.3 与 Rose 其他模块的协作

| 能力     | 机制                                                                                                                              |
|--------|---------------------------------------------------------------------------------------------------------------------------------|
| 文件热更   | `AutoRefreshWatcher` / `FileChangedEvent` → `refresh(locale)`                                                                   |
| Env 热更 | `PropertySourcesRefreshEnvironmentListener` → `Refreshable`（[`rose-spring-core` 规格 §5–§6](./rose-spring-env-refresh-design.md)） |
| 远程配置   | `@YamlPropertySource` / Cloud 刷新后触发 i18n reload                                                                                 |
| 键名约定   | Env 覆盖：`rose.i18n.messages.{bundle}.{locale}.{code}` 或整文件 blob                                                                  |

不必单独做 microsphere-i18n-cloud 模块；Rose 在 **`rose-i18n-spring-cloud`** 中实现，完整规格见 **§17**。

### 10.4 Web 与校验

| 能力                              | 说明                           | 阶段  |
|---------------------------------|------------------------------|-----|
| `AcceptHeaderLocaleResolver` 默认 | 可选注册，与 Spring MVC 一致         | 1.5 |
| Validation 专用 bundle            | `i18n/validation/messages_*` | 1   |
| `@I18nCode` / 异常基类              | 业务异常携带 message code + args   | 3+  |
| Problem Details (RFC 7807)      | 错误响应 body 走 `MessageSource`  | 3+  |

### 10.5 国际化进阶

| 能力                | 说明                         | 阶段 |
|-------------------|----------------------------|----|
| ICU MessageFormat | 复数、性别、选择性格式                | 3+ |
| 默认 locale 链       | `zh_CN → zh → default` 可配置 | 1  |
| 参数命名占位符           | `{name}` 而不仅是 `{0}`        | 3+ |
| RTL / 脚本元数据       | 与 UI 层协作，core 只存文案         | 远期 |

### 10.6 工程与运维

| 能力                        | 说明                                 | 阶段 |
|---------------------------|------------------------------------|----|
| Actuator `/actuator/i18n` | 列出 bundle、locale、采样 key            | 3  |
| 缺失 key 开发模式               | `WARN` 日志 + 可选 HTTP 头提示            | 2  |
| 导出 / 对比                   | CLI 或测试工具导出 key 清单供翻译              | 3+ |
| `spring-boot-test` 工具     | `@FixedLocale("zh_CN")`、断言 message | 2  |

### 10.7 明确不做（或不做成独立重型模块）

| 项               | 替代方案                                  |
|-----------------|---------------------------------------|
| i18n Server 微服务 | Env / 配置中心 + Actuator 观测              |
| Feign 专用客户端     | 应用自行 REST + `RemoteMessageBundle`（按需） |
| 翻译管理平台          | 仓库内 properties/yml + 外部 TMS 导出        |
| 锁死 META-INF 路径  | basename 可配，默认 `i18n/`                |

### 10.8 第三方集成示例

```java
// spring.factories
io.zhijun.i18n.MessageBundleProvider=com.example.CustomMessageBundleProvider
```

```java
public class CustomMessageBundleProvider implements MessageBundleProvider {
    @Override
    public MessageBundle createMessageBundle(I18nBundleDefinition definition) {
        return new DbMessageBundle(definition.getName());
    }
}
```

注册进 `CompositeMessageBundle`，与 classpath bundle 同一套 priority 规则。

---

## 11. 测试计划

Phase 1 测试必须与 **§15.10 测试矩阵** 一致；以下为类名索引。

| 测试类                                 | 覆盖点                                      |
|-------------------------------------|------------------------------------------|
| `PropertiesMessageBundleLoaderTest` | 单文件加载、URL 排序 merge                       |
| `ClasspathMessageBundleTest`        | locale 回退、key 前缀、懒加载                     |
| `CompositeMessageBundleTest`        | priority 覆盖、全未命中                         |
| `ClasspathMessageBundleMergeTest`   | 两 JAR 同路径，后 jar 覆盖                       |
| `PlaceholderStyleTest`              | `{0}` 与 `{}`                             |
| `QualifiedKeyModeTest`              | QUALIFIED 非法 key 抛错                      |
| `RoseMessageSourceTest`             | 三种 `getMessage` 方法                       |
| `I18nBundleRegistrarTest`           | 注册 3 类 Bean、bean 名                       |
| `EnableI18nIntegrationTest`         | `@EnableI18n` + `@Primary messageSource` |

Phase 1.5 再增：`YamlMessageBundleLoaderTest`、`EnvironmentMessageBundleTest`、`SpringMessageBundleAdapterTest`、
`EnvironmentMessageBundleRefreshableTest`（**§16.10**；Env 热更见 [
`rose-spring-core` 规格 §6](./rose-spring-env-refresh-design.md#6-phase-2-对接规格rose-i18n)）。

---

## 12. 文档交付

实施后更新：

- `wiki/rose-bom/Consumer-Guide` — 消费说明与 BOM 索引
- 根 `README.md` — 增加 `rose-i18n` 条目

---

## 13. 决策摘要

| 决策               | 选择                                                   | 理由                                                                   |
|------------------|------------------------------------------------------|----------------------------------------------------------------------|
| 主题位置             | 顶层 `rose-i18n/`                                      | Rose 按主题聚合，与 multitenancy/otel 一致                                    |
| Phase 1 artifact | `rose-i18n-spring`                                   | 纯 Spring Framework，无 Boot                                            |
| Boot / Cloud     | 独立子模块                                                | 不污染 Framework 层                                                      |
| 包名               | `io.zhijun.i18n.*`                                   | 主题命名空间；Spring 集成在 `.spring`                                          |
| 默认资源路径           | `i18n/{bundle}/messages_*`                           | Spring 习惯、可发现                                                        |
| 加载策略             | `LoadStrategy.LAZY` 默认，可 `@EnableI18n` / Boot 配置     | 启动快；EAGER 可 fail-fast                                                |
| META-INF         | 可选 basename                                          | 库内嵌、microsphere 迁移                                                   |
| 占位符默认            | MessageFormat `{0}`                                  | Validation 一致                                                        |
| Cloud 集成         | `rose-i18n-spring-cloud` 监听 `EnvironmentChangeEvent` | 见 §17；不做 Feign Server                                                |
| 协作               | `rose-spring-core`（env-refresh + `Refreshable` SPI）  | Env 热更（[env-refresh §5–§6](rose-spring-env-refresh-design.md)），非归属关系 |

### 13.1 复杂度与裁剪建议

> **目的：** 记录「方案是否过重」的评估结论，以及**可延后 / 可裁剪**项，避免文档路线图被一次性实现。  
> **原则：** 扩展性靠 `MessageBundle + Composite + priority` 主轴；其余能力按真实需求渐进交付。

#### 13.1.1 总体结论

| 维度             | 结论                                                                                      |
|----------------|-----------------------------------------------------------------------------------------|
| 是否过度设计         | **整体否**——对 Rose（多 JAR、库覆盖、Config 覆盖）合理；对「单文件 `messages.properties`」偏重                   |
| 文档 vs 代码       | 文档偏宽（§6–§10、§10 路线图）；**实现须严格分 Phase**，勿把远期能力并进首 PR                                      |
| 扩展性            | **核心层足够**（组合式 `MessageBundle`、Loader SPI、`refresh()`）；Env key 约定、Enumerable 限制是边界耦合，可接受 |
| 相对 microsphere | Cloud / Server 更轻；组合模型类似，非另起炉灶                                                          |

#### 13.1.2 建议保留（不必裁剪）

| 能力                                                    | 理由                              |
|-------------------------------------------------------|---------------------------------|
| `MessageBundle` + `CompositeMessageBundle` + priority | 多模块 / 多 JAR 文案覆盖的刚需             |
| `RoseMessageSource` + `@EnableI18n`                   | 与 Spring 生态对齐，上手成本低             |
| `rose-i18n-spring` / `boot` / `cloud` 三模块             | Framework 不绑 Boot；与 Rose 主题聚合一致 |
| 明确非目标（无 i18n Server、无 Feign）                          | 控制范围，优于 microsphere Cloud 模块    |

Phase 1 预估 ~12 生产类，与 §15 范围匹配，**视为合适 MVP**。

#### 13.1.3 可延后或分 PR 交付（降复杂度）

| 项                                                   | 风险                           | 建议                                                                                |
|-----------------------------------------------------|------------------------------|-----------------------------------------------------------------------------------|
| **Phase 1.5 四项打包**（Env + YAML + Adapter + Listener） | 单 PR 认知负担大；Env 索引算法本身不轻      | **拆 PR**：① `EnvironmentMessageBundle`（Cloud 硬前置）→ ② YAML → ③ Adapter → ④ Listener |
| `KeyPrefixMode.QUALIFIED`                           | 主要为 microsphere 迁移；Rose 绿场少用 | Phase 1 可仅实现 `BUNDLE`；QUALIFIED 标迁移专用或 Phase 2                                    |
| `PlaceholderStyle.SLF4J`                            | 第二套占位符 + 测试                  | 团队若统一 `{0}`，可延后；接口可预留                                                             |
| `LoadStrategy.EAGER` + `I18nEagerLoadInitializer`   | 多数场景 LAZY 够用                 | Phase 1 可只交付 LAZY；EAGER 随 Boot 配置（Phase 2）                                        |
| `@EnableI18n` Phase 1.5 多开关                         | 注解趋近「小 DSL」                  | Phase 2 用 `rose.i18n.*` 收敛；1.5 优先只加 `enableEnvironmentOverrides`                  |
| §10 远期项（Remote / Tenant / ICU / Problem Details…）   | 文档膨胀感                        | **仅路线图**，不进入 Phase 1 / 1.5 检查清单                                                   |
| `priority + 10_000`                                 | 魔法数                          | 可接受；或改为固定规则「同 bundle 的 Env 永远排在 Classpath 前」                                      |

#### 13.1.4 扩展性评估

**强：**

- 新数据来源 = 新 `MessageBundle` + 调 priority，无需改 `RoseMessageSource`
- 新资源格式 = 新 `MessageBundleLoader`，不动 locale 回退与 key 规范化
- Phase 2 `MessageBundleProvider` 支持第三方 JAR 自注册
- `refresh()` 统一入口，Cloud / PropertySource / 将来文件热更同契约
- `rose-spring-core`、`snakeyaml` 为 **optional**，不拖累纯 Spring 用户

**边界耦合（扩展时注意）：**

| 点                                   | 说明                                                     |
|-------------------------------------|--------------------------------------------------------|
| Env key 前缀 `rose.i18n.messages.*`   | 与 Config 中心写入约定绑定；变更需同步文档与 Cloud §17                   |
| 仅索引 `EnumerablePropertySource`      | 非 Enumerable 源会漏 key；需在 README 说明                      |
| `refresh()` 全量重建                    | 文案量大时可优化为按 changed key 增量（远期）                          |
| `SpringMessageBundleAdapter` 双 Bean | 迁移期可用；长期应引导关闭 Boot 默认 `MessageSourceAutoConfiguration` |

#### 13.1.5 推荐交付顺序（瘦身版）

```
Phase 1（首 PR，§15）
  Classpath + Composite + @EnableI18n + BUNDLE 模式 + LAZY + MessageFormat {0}

Phase 1.5a（§16 子集）
  EnvironmentMessageBundle only  → Cloud / Config 覆盖可联调

Phase 1.5b–d（按需小 PR）
  YamlMessageBundleLoader → SpringMessageBundleAdapter → EnvironmentMessageBundleRefreshable（平台 §6）

Phase 2（§18 待写）
  I18nProperties / I18nAutoConfiguration；注解开关迁入 rose.i18n.*

Phase 3
  §17 Cloud + 可选 Actuator
```

**裁剪后仍保留扩展性：** 主轴抽象不变；延后项不删接口，仅推迟实现与测试矩阵行。

#### 13.1.6 与替代方案对比

| 方案                                   | 适用                | 对 Rose 的不足                           |
|--------------------------------------|-------------------|--------------------------------------|
| 仅 Boot `ResourceBundleMessageSource` | 单应用、单 basename    | 无多 bundle 优先级、无 Env 覆盖、无平台事件联动       |
| microsphere-i18n 原样引入                | 已有 microsphere 约定 | Cloud/Server 过重；路径与 Rose 平台不一致       |
| 本方案 Phase 1 only                     | 大多数应用             | 已覆盖多 JAR + MessageSource 接管；高级能力按需叠加 |

---

## 14. 开工清单

1. 分支：`feature/rose-i18n`
2. 新建 `rose-i18n/pom.xml`（聚合）与 `rose-i18n/rose-i18n-spring/`
3. 根 `pom.xml` 增加 `<module>rose-i18n</module>`；**勿**改 `rose-spring/pom.xml`
4. 更新 `rose-parent` dependencyManagement、`rose-bom`、`rose-coverage`
5. 按 §15 实现并测试
6. （可选下一 PR）按 §16 实现 Phase 1.5
7. 文档：`docs/design/rose-i18n-design.md`、`wiki/rose-bom/Consumer-Guide`
8. `mvn -pl rose-i18n/rose-i18n-spring test`

---

## 15. Phase 1 实现规格（按本节直接编码）

> **权威章节**：Phase 1 的所有类名、算法、Bean 名、测试断言以本节为准；§4–§5 为概念说明。

### 15.1 仓库改动清单

| 文件                                   | 改动                                           |
|--------------------------------------|----------------------------------------------|
| `pom.xml`（根）                         | `<module>rose-i18n</module>`                 |
| `rose-i18n/pom.xml`                  | 新建主题聚合 POM，见 §15.2                           |
| `pom.xml` / `rose-bom/pom.xml`       | `dependencyManagement` 增加 `rose-i18n-spring` |
| `rose-coverage/pom.xml`              | 依赖 `rose-i18n-spring`                        |
| `rose-i18n/rose-i18n-spring/pom.xml` | 新建，见 §15.2                                   |
| `rose-i18n/rose-i18n-spring/src/...` | 见 §2.3                                       |

**勿修改：** `rose-spring/pom.xml`（i18n 不属于 spring 平台聚合）。

### 15.2 Maven POM

**`rose-i18n/pom.xml`（主题聚合）：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>rose-i18n</artifactId>
    <packaging>pom</packaging>
    <name>Rose I18n</name>
    <modules>
        <module>rose-i18n-spring</module>
    </modules>
</project>
```

**`rose-i18n/rose-i18n-spring/pom.xml`：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>rose-i18n-spring</artifactId>
    <name>Rose I18n Spring</name>
    <description>Spring Framework i18n MessageBundle integration for Rose</description>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

`rose-parent` / `rose-bom` 追加：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-i18n-spring</artifactId>
    <version>${project.version}</version>
</dependency>
```

Phase 1 **不**依赖 `rose-spring-core`、`rose-core`、`spring-boot`（生产）。

### 15.3 注解 API（逐字实现）

```java
package io.zhijun.i18n.spring.annotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(I18nBundleRegistrar.class)
public @interface EnableI18n {

    /** 简写 bundle 名；与 {@link #bundles()} 二选一，本属性优先 */
    String[] value() default {};

    /** 完整 bundle 定义 */
    I18nBundle[] bundles() default {};

    /** {@link io.zhijun.i18n.spring.RoseMessageSource} 解析无 '.' 短 code 时使用 */
    String defaultBundle() default "";

    /** 默认 LAZY；EAGER 在容器 refresh 后预加载，见 §15.5.1 */
    LoadStrategy loadStrategy() default LoadStrategy.LAZY;
}
```

```java
package io.zhijun.i18n.spring.annotation;

public @interface I18nBundle {
    String name();
    String basename() default "";
    int priority() default -1;
    String encoding() default "UTF-8";
    KeyPrefixMode keyPrefixMode() default KeyPrefixMode.BUNDLE;
    PlaceholderStyle placeholderStyle() default PlaceholderStyle.MESSAGE_FORMAT;
    boolean fallbackToLanguage() default true;
}
```

```java
package io.zhijun.i18n;

public enum KeyPrefixMode { BUNDLE, QUALIFIED }

public enum PlaceholderStyle { MESSAGE_FORMAT, SLF4J }

public enum LoadStrategy {
    /** 首次 getMessage 时加载（默认） */
    LAZY,
    /** Context refresh 完成后预加载 Locale.getDefault() 回退链 */
    EAGER
}
```

**`I18nBundleDefinition` 增加字段：** `loadStrategy`（来自 `@EnableI18n.loadStrategy()`）。

**解析算法（`I18nBundleRegistrar.resolveDefinitions`）：**

1. 读 `@EnableI18n` 的 `value`、`bundles`、`defaultBundle`、`loadStrategy`。
2. 若 `value.length > 0`：对每个 `name` 构造 definition（仅设 `name`，其余默认）。
3. 否则：对每个 `@I18nBundle` 构造 definition。
4. 若结果为空 → `IllegalStateException("At least one i18n bundle is required")`。
5. 对每个 definition：`basename` blank → `classpath*:i18n/{name}/messages`；`priority == -1` → 下标 `i * 100`。
6. 返回 definitions + `defaultBundle` + `loadStrategy`.

### 15.4 核心类签名

```java
package io.zhijun.i18n;

public interface MessageBundle {
    String getName();
    int getPriority();
    /** 未找到返回 null */
    String getMessage(String code, Locale locale, Object... args);
    /** 未找到返回 empty；不格式化 */
    Optional<String> resolveRaw(String code, Locale locale);
    /** Phase 1：cache.clear() */
    void refresh();
}
```

```java
public final class ClasspathMessageBundle implements MessageBundle {

    public ClasspathMessageBundle(String name, int priority, String basename,
            String encoding, KeyPrefixMode keyPrefixMode,
            PlaceholderStyle placeholderStyle, boolean fallbackToLanguage) {
        this(name, priority, basename, encoding, keyPrefixMode, placeholderStyle,
                fallbackToLanguage, Thread.currentThread().getContextClassLoader());
    }

    public ClasspathMessageBundle(String name, int priority, String basename,
            String encoding, KeyPrefixMode keyPrefixMode,
            PlaceholderStyle placeholderStyle, boolean fallbackToLanguage,
            ClassLoader classLoader) { ... }
}
```

```java
public final class CompositeMessageBundle implements MessageBundle {
    /** 构造时按 priority 降序排序；防御性拷贝 */
    public CompositeMessageBundle(List<MessageBundle> bundles) { ... }
}
```

```java
package io.zhijun.i18n.loader;

public final class PropertiesMessageBundleLoader {
    public Map<String, String> load(String classpathLocation, String encoding,
            ClassLoader classLoader) throws IOException { ... }
}
```

```java
package io.zhijun.i18n.support;

public interface PlaceholderResolver {
    String resolve(String pattern, Object... args);
}
```

```java
package io.zhijun.i18n.spring;

public final class RoseMessageSource implements MessageSource {
    public RoseMessageSource(CompositeMessageBundle composite, String defaultBundle) { ... }
}
```

### 15.5 资源路径与加载

**`buildResourcePath(basename, locale)`：**

```
path = stripClasspathPrefix(basename)   // 去掉 classpath*: 或 classpath:
if locale == null → locale = Locale.getDefault()
if locale == ROOT || !hasText(locale.getLanguage()):
    return path + ".properties"
return path + "_" + locale.toString() + ".properties"
```

**Locale 回退链（`buildLocaleChain(locale, fallbackToLanguage)`）：**

1. 请求 locale（null → `Locale.getDefault()`）
2. 若 `fallbackToLanguage && hasText(country)` → `new Locale(language)`
3. `Locale.ROOT` → `messages.properties`

**单层加载：** `PropertiesMessageBundleLoader.load(path)` → `normalizeKeys(map)`；空 map 则试下一层。

**层 merge 顺序：** ROOT → language → exact（**后者覆盖前者**）。

**`normalizeKeys`：**

- `BUNDLE`：`prefix = name + "."`；key 已有 prefix 则原样，否则 `prefix + key`
- `QUALIFIED`：每个 key 必须以 `prefix` 开头，否则 `IllegalStateException`

**缓存：** `ConcurrentHashMap<String, Map<String,String>>`；`refresh()` → `clear()`。

**`preload(Locale locale)`（package-private）：** 调用 `loadForLocale(locale)` 填充缓存；供 EAGER 使用。

### 15.5.1 `LoadStrategy` 行为

| 策略         | 行为                                                                                                                                                        |
|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `LAZY`（默认） | 首次 `getMessage` / `resolveRaw` 触发 `loadForLocale`                                                                                                         |
| `EAGER`    | Registrar 额外注册 `I18nEagerLoadInitializer`（`InitializingBean`），在 `afterPropertiesSet` 中对 **每个** `ClasspathMessageBundle` 调用 `preload(Locale.getDefault())` |

Phase 2 在 **`rose-i18n-spring-boot`** 增加 `rose.i18n.load-strategy=lazy|eager` 覆盖注解。Boot 用户无需改代码。

### 15.6 查找与 MessageSource

**`ClasspathMessageBundle.getMessage`：**

1. `resolved = code.startsWith(name + ".") ? code : name + "." + code`
2. `pattern = cacheLayer.get(resolved)`；null → return null
3. `PlaceholderResolver.resolve(pattern, args)`

**`CompositeMessageBundle`：** 子 bundle 依次查，第一个非 null 返回。

**`RoseMessageSource`：**

```
loc = locale != null ? locale : Locale.getDefault()

lookup(code, loc, args):
  if hasText(defaultBundle) && !code.contains("."):
    msg = composite.getMessage(defaultBundle + "." + code, loc, args)
    if msg != null return msg
  return composite.getMessage(code, loc, args)

getMessage(code, args, defaultMessage, locale):
  msg = lookup(...); return msg != null ? msg : defaultMessage

getMessage(code, args, locale):
  msg = lookup(...); if msg == null throw new NoSuchMessageException(code, loc); return msg

getMessage(code, locale):
  return getMessage(code, null, locale)
```

### 15.7 `PropertiesMessageBundleLoader`

1. `Enumeration<URL> urls = classLoader.getResources(classpathLocation)`
2. 无 URL → `Collections.emptyMap()`
3. URL 列表按 `toExternalForm()` 升序排序
4. 顺序 `Properties.load` 进同一 `Properties`
5. 转为 `LinkedHashMap<String,String>` 返回

### 15.8 `I18nBundleRegistrar` Bean 注册

**常量：**

```java
static final String COMPOSITE_BEAN_NAME = "compositeMessageBundle";
static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";
static String bundleBeanName(String name) { return name + "MessageBundle"; }
```

**步骤：**

1. `resolveDefinitions(metadata)`
2. 每个 definition → `BeanDefinitionBuilder.genericBeanDefinition(ClasspathMessageBundle.class)` + 7 个构造参数 →
   `registerBeanDefinition(bundleBeanName(name), bd)`
3. Composite：`ManagedList<RuntimeBeanReference>` 引用各 bundle → `CompositeMessageBundle` 构造 →
   `registerBeanDefinition("compositeMessageBundle", bd)`
4. MessageSource：`RoseMessageSource(composite, defaultBundle)` → `setPrimary(true)` →
   `registerBeanDefinition("messageSource", bd)`
5. 若 `loadStrategy == EAGER`：注册 `I18nEagerLoadInitializer`（`InitializingBean`），注入全部 `{name}MessageBundle`，在
   `afterPropertiesSet` 调用各 bundle 的 `preload(Locale.getDefault())`

### 15.9 测试资源

```
src/test/resources/i18n/app/messages.properties          → welcome=Default welcome
src/test/resources/i18n/app/messages_zh.properties       → welcome=中文
src/test/resources/i18n/app/messages_zh_CN.properties   → welcome=欢迎 / order.notFound=订单 {0} 不存在
src/test/resources/i18n/app/messages_en.properties       → welcome=Hello
src/test/resources/i18n/common/messages_zh_CN.properties → shared=公共
src/test/resources/i18n/qualified/messages_zh_CN.properties → qualified.ok=qualified.ok / bad=缺少前缀
```

### 15.10 测试矩阵

| #  | 测试                | 输入                              | 期望                          |
|----|-------------------|---------------------------------|-----------------------------|
| 1  | exactLocale       | app, welcome, zh_CN             | `"欢迎"`                      |
| 2  | fallbackLanguage  | 无 zh_CN key，有 zh                | 命中 zh 层                     |
| 3  | fallbackDefault   | locale=fr                       | `"Default welcome"`         |
| 4  | qualifiedCode     | code `welcome` 或 `app.welcome`  | `"欢迎"`                      |
| 5  | formatArgs        | order.notFound, 42              | `"订单 42 不存在"`               |
| 6  | compositePriority | common+app 同 key                | app 优先                      |
| 7  | compositeMiss     | 不存在 code                        | `null`                      |
| 8  | qualifiedInvalid  | qualified bundle, key `bad`     | `IllegalStateException`     |
| 9  | slf4jStyle        | `Hi {}`, Tom                    | `"Hi Tom"`                  |
| 10 | noSuchMessage     | 无 default                       | `NoSuchMessageException`    |
| 11 | withDefault       | 无 key + default                 | 返回 default                  |
| 12 | shortCode         | defaultBundle=app, `welcome`    | `"欢迎"`                      |
| 13 | integration       | `@EnableI18n({"app","common"})` | `@Primary MessageSource` 可用 |

**`EnableI18nIntegrationTest` 模板：**

```java
@Configuration
@EnableI18n(value = {"app", "common"}, defaultBundle = "app")
static class TestConfig {}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class EnableI18nIntegrationTest {
    @Autowired MessageSource messageSource;
    @Test void resolvesMessage() {
        assertThat(messageSource.getMessage("app.welcome", null, Locale.SIMPLIFIED_CHINESE))
            .isEqualTo("欢迎");
    }
}
```

**`ClasspathMessageBundleMergeTest`：** 两个目录 `src/test/resources-merge/a/`、`b/` 含同路径 properties；自定义
ClassLoader 或测试里用 `PropertiesMessageBundleLoader` 直接测 URL 排序覆盖。

### 15.11 实现检查清单

- [ ] `mvn -pl rose-i18n/rose-i18n-spring test` 全绿
- [ ] `@EnableI18n({"app"})` 与 `@EnableI18n(bundles=@I18nBundle(name="app"))` 等价
- [ ] Bean：`appMessageBundle`、`compositeMessageBundle`、`messageSource`（Primary）
- [ ] 多 URL merge 字典序稳定
- [ ] QUALIFIED 非法 key 加载失败
- [ ] `LoadStrategy.EAGER` 时 `I18nEagerLoadInitializer` 在 refresh 后缓存非空
- [ ] `refresh()` 清空缓存

### 15.12 Phase 1.5 / 2 / 3

- **Phase 1.5**（仍在 `rose-i18n-spring`）：见 **§16**（`EnvironmentMessageBundle`、YAML、迁移回退）。
- **Phase 2**（`rose-i18n-spring-boot`）：§6–§10 为方向性规划，**尚无 §15 级 Boot 规格**（后续可补 §18）。
- **Phase 3 Cloud**：见 **§17**（依赖 §16 的 `EnvironmentMessageBundle`）。
- Phase 1 合并前勿实现 Boot / Cloud 模块；Phase 1.5 可与 Phase 1 同 PR 或紧随其后。
- **范围控制：** 若需瘦身，按 **§13.1.5** 拆分 Phase 1.5 为 1.5a（Env only）+ 1.5b–d。

---

## 16. Phase 1.5 实现规格（按本节直接编码）

> **权威章节**：Phase 1.5 的所有类名、算法、Bean 名、测试断言以本节为准。  
> **模块归属：** 全部落在 **`rose-i18n-spring`**；**不**新建 `rose-i18n-spring-boot`。  
> **前置：** Phase 1（§15）已完成且测试全绿。

### 16.1 目标与范围

| 在范围内                                                     | 不在范围内（Phase 2 Boot）                        |
|----------------------------------------------------------|--------------------------------------------|
| `EnvironmentMessageBundle`（Cloud 硬前置）                    | `I18nProperties` / `rose.i18n.*` YAML 绑定   |
| `SpringMessageBundleAdapter`（`spring.messages` 回退）       | `I18nAutoConfiguration`、`spring.factories` |
| `MessageBundleLoader` SPI + `YamlMessageBundleLoader`    | classpath 自动发现 bundle                      |
| `EnvironmentMessageBundleRefreshable`（`Refreshable` SPI） | Actuator 端点                                |
| `@EnableI18n` / `@I18nBundle` 扩展字段                       | `EnvironmentChangeEvent`（见 §17）            |

### 16.2 Maven 依赖变更

在 **`rose-i18n-spring/pom.xml`** 追加（均为 **optional**，避免强绑 Rose 平台）：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-core</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <optional>true</optional>
</dependency>
```

**规则：**

- 生产代码中引用 `io.zhijun.spring.core.*` 的类单独放在 `io.zhijun.i18n.spring.env` 包，并通过 **`@ConditionalOnClass`**
  或编译期 `#if` 不可行 → 使用 **独立类 + 文档约定**：listener 类仅在 classpath 存在 `PropertySourcesChangedEvent` 时被
  `@Import`。
- `YamlMessageBundleLoader` 在 runtime 检测 `snakeyaml` / `PropertySourceMaps`；缺失时跳过 YAML，仅 `.properties`（与 Phase
  1 行为一致）。
- Phase 1.5 **仍不**依赖 `spring-boot`（生产）。

### 16.3 源文件树（增量）

```
rose-i18n/rose-i18n-spring/src/main/java/io/zhijun/i18n/
├── loader/
│   ├── MessageBundleLoader.java              # SPI
│   ├── MessageBundleLoaderChain.java         # 按 supports() 选择 loader
│   ├── PropertiesMessageBundleLoader.java    # 实现 SPI（Phase 1 逻辑迁入）
│   └── YamlMessageBundleLoader.java          # 可选 snakeyaml + PropertySourceMaps
├── env/
│   └── EnvironmentMessageBundle.java
├── support/
│   └── SpringMessageBundleAdapter.java
└── spring/
    ├── env/
    │   └── EnvironmentMessageBundleRefreshable.java   # Refreshable SPI，见`rose-spring-core` 规格 §6
    └── annotation/
        EnableI18n.java                             # 增字段，见 §16.4
        I18nBundle.java                             # 增字段，见 §16.4
```

**重构 Phase 1：** `ClasspathMessageBundle` 内部不再直接调用 `PropertiesMessageBundleLoader`，改为注入
`MessageBundleLoaderChain`（默认：Properties → Yaml）。

### 16.4 注解 API 扩展

**`@EnableI18n` 新增：**

```java
/** 为每个声明的 bundle 额外注册 EnvironmentMessageBundle（priority 见 §16.6） */
boolean enableEnvironmentOverrides() default false;

/** Env key 前缀；默认 rose.i18n.messages. */
String environmentKeyPrefix() default "rose.i18n.messages.";

/** 注册 SpringMessageBundleAdapter 作为最低优先级回退 */
boolean springMessagesFallback() default false;

/** springMessagesFallback=true 时包装已有 MessageSource Bean 名；默认 messageSource */
String springMessagesBeanName() default "messageSource";
```

> Env 热更：依赖 `rose-spring-core` 的 `Refreshable`
> SPI（见 [rose-spring-env-refresh-design.md](./rose-spring-env-refresh-design.md)），**不**在 `@EnableI18n` 增加开关。

**`@I18nBundle` 新增：**

```java
/** 资源格式；AUTO 按扩展名探测 .properties / .yml / .yaml */
MessageFormat format() default MessageFormat.AUTO;

/** 单 bundle 级 Env 覆盖；默认继承 @EnableI18n.enableEnvironmentOverrides() */
boolean environmentOverride() default false;
```

```java
package io.zhijun.i18n;

public enum MessageFormat {
    AUTO, PROPERTIES, YAML
}
```

**`I18nBundleDefinition` 新增字段：** `format`、`environmentOverride`（及 EnableI18n 级 `environmentKeyPrefix` 传入 Env
bundle）。

### 16.5 `MessageBundleLoader` SPI

```java
package io.zhijun.i18n.loader;

public interface MessageBundleLoader {
    /** 例如 path 以 .properties / .yml / .yaml 结尾，或 MessageFormat 约束 */
    boolean supports(String resourcePath, MessageFormat format);

    Map<String, String> load(String resourcePath, String encoding,
            ClassLoader classLoader) throws IOException;
}
```

**`MessageBundleLoaderChain`：**

1. 构造时注入有序 loader 列表：`PropertiesMessageBundleLoader`、`YamlMessageBundleLoader`（后者 optional）。
2. `load(path, format, encoding, cl)`：按列表第一个 `supports==true` 的 loader 加载；无匹配 → `Collections.emptyMap()`。
3. `YamlMessageBundleLoader`：`Yaml.load` → `PropertySourceMaps.flatten` → 值 `String.valueOf` 进 `LinkedHashMap`（与 §3.7
   一致）。

**`ClasspathMessageBundle` 资源路径扩展（相对 §15.5）：**

对每个 locale 层，按 `format` 生成候选路径并 **顺序尝试**（先命中非空 map 者为准）：

| format       | 候选后缀（在 basename 后追加 locale 段）                   |
|--------------|-------------------------------------------------|
| `PROPERTIES` | `_zh_CN.properties` → … → `.properties`         |
| `YAML`       | `_zh_CN.yml`、`_zh_CN.yaml` → … → `.yml`、`.yaml` |
| `AUTO`       | 对每个 locale 层：**先 properties 全套，再 yaml 全套**      |

同一 locale 层若 properties 与 yaml 均存在且均非空：**properties 优先**（AUTO 模式下），避免 silent 混用；测试覆盖 §16.10
#15。

### 16.6 `EnvironmentMessageBundle`

```java
package io.zhijun.i18n.env;

public final class EnvironmentMessageBundle implements MessageBundle {

    public EnvironmentMessageBundle(
            String name,
            int priority,
            Environment environment,
            String keyPrefix,              // 如 rose.i18n.messages.
            KeyPrefixMode keyPrefixMode,
            PlaceholderStyle placeholderStyle,
            boolean fallbackToLanguage) { ... }
}
```

**Env key 约定（与 §17.5 一致）：**

| 方式               | key 模式                                    | 示例                                                |
|------------------|-------------------------------------------|---------------------------------------------------|
| 扁平覆盖             | `{prefix}{bundle}.{localeTag}.{code...}`  | `rose.i18n.messages.app.zh_CN.order.notFound=新文案` |
| 整文件 blob         | `{prefix}{bundle}.{localeTag}.properties` | 值为多行 properties 文本                                |
| locale `default` | `{prefix}{bundle}.default.{code...}`      | 等价 `Locale.ROOT` 层                                |

**`localeTag`：** `Locale.toString()`（如 `zh_CN`、`en`）；空 language 用 `default`。

**索引构建（`rebuildIndex()`，在构造与 `refresh()` 调用）：**

1. `prefixBundle = keyPrefix + name + "."`
2. 遍历 `environment` 中所有 property name（实现：`ConfigurableEnvironment` + 各 `EnumerablePropertySource`；非 Enumerable
   的 PropertySource **跳过**，文档说明限制）。
3. 对每个 `key.startsWith(prefixBundle)`：
    - 若 key 以 `.properties` 结尾且 key 去掉 prefixBundle 后 **不含** 第二个 `.properties` 段 → 解析为 blob：  
      `localeTag = segment between bundle and ".properties"`；`Properties.load` blob 值 → merge 进该 locale 的 map（blob
      key 本身不入 map）。
    - 否则 → 扁平 key：  
      `rest = key.substring(prefixBundle.length())`  
      第一个 `.` 之前为 `localeTag`，之后为 **raw code**（可含 `.`）。
4. 对每个 entry 执行与 §15.5 相同的 `normalizeKeys`（`KeyPrefixMode` / bundle prefix）。
5. 存入 `ConcurrentHashMap<LocaleLayerKey, Map<String,String>>`；`LocaleLayerKey` 为解析后的 `Locale` + 层序。

**查找：** 与 `ClasspathMessageBundle` 相同 locale 回退链（§15.5）；`getMessage` 使用相同 `PlaceholderResolver`。

**`refresh()`：** `rebuildIndex()`（全量重建，无 partial cache）。

**Priority 规则（Registrar）：**

- 若 `environmentOverride==true`（或 Enable 级 `enableEnvironmentOverrides`）：  
  注册 Bean `{name}EnvironmentMessageBundle`，**priority = definition.priority + 10_000**（保证同 bundle 的 classpath
  被覆盖）。
- Composite 成员顺序：Env bundles + classpath bundles + 可选 adapter（均按 priority 降序）。

**Bean 名：**

```java
static String environmentBundleBeanName(String name) {
    return name + "EnvironmentMessageBundle";
}
```

### 16.7 `SpringMessageBundleAdapter`

```java
package io.zhijun.i18n.support;

public final class SpringMessageBundleAdapter implements MessageBundle {

    public SpringMessageBundleAdapter(
            String name,                    // 固定 "spring-messages"
            int priority,                   // 默认 -50
            MessageSource delegate) { ... }

    @Override
    public void refresh() {
        if (delegate instanceof ReloadableResourceBundleMessageSource) {
            ((ReloadableResourceBundleMessageSource) delegate).clearCache();
        }
        // 其他 MessageSource：no-op
    }
}
```

**行为：**

- `getName()` → `"spring-messages"`（固定，不参与 BUNDLE prefix 拼接；lookup 时 code 原样传给 delegate）。
- `getMessage(code, locale, args)` → `delegate.getMessage(code, args, null, locale)`；`NoSuchMessageException` → return
  null。
- `resolveRaw` → `getMessage(code, locale)`（无 args 格式化）。

**Registrar（`springMessagesFallback=true`）：**

1. 解析 `@EnableI18n.springMessagesBeanName` 对应 Bean；**须已存在** `MessageSource`（典型：Boot
   `ResourceBundleMessageSource`）。
2. 若不存在 → `IllegalStateException`（明确提示关闭 Rose `@Primary messageSource` 冲突或改 bean 名）。
3. 注册 `springMessagesMessageBundle` → `SpringMessageBundleAdapter(-50, delegate)`。
4. **注意：** Rose 仍注册 `@Primary RoseMessageSource` 为 `messageSource`；回退 adapter 包装的是 **另一** Bean（如
   `springMessagesMessageSource`）时，需在迁移文档中说明双 Bean 模式。Phase 1.5 默认：`springMessagesFallback` 包装名为
   `legacyMessageSource` 的 `@Bean`，**不**与 Rose `messageSource` 同名——见 §16.8 迁移模式。

**推荐迁移双 Bean 配置（文档示例，非自动）：**

```java
@Bean("legacyMessageSource")
ResourceBundleMessageSource legacyMessageSource() { ... }

@EnableI18n(value = "app", springMessagesFallback = true, springMessagesBeanName = "legacyMessageSource")
```

### 16.8 Env 热更（对接 rose-spring-core）

> **已迁移至 `rose-spring-core` 规格：
** [rose-spring-env-refresh-design.md §6](./rose-spring-env-refresh-design.md#6-phase-2-对接规格rose-i18n)  
> 本节不再使用 `I18nPropertySourcesChangedListener` + `@EnableI18n(refreshOnPropertySourcesChanged)`。

**实现要点（摘要）：**

1. `rose-spring-core` 提供 `Refreshable` SPI + `PropertySourcesRefreshEnvironmentListener`（见`rose-spring-core` 规格
   §5）。
2. `rose-i18n-spring` 注册 `EnvironmentMessageBundleRefreshable`（`spring.factories`）。
3. `supports(changedKeys)`：任一 key 以 `rose.i18n.messages.` 开头。
4. `refresh(changedKeys)`：通过 `RefreshableContextHolder` 取 `ApplicationContext`，对全部 `EnvironmentMessageBundle` 调用
   `refresh()`
   （见 [env-refresh §5.2](./rose-spring-env-refresh-design.md#52-refreshablecontextholder-与-initializer-钩子)）。
5. **不** refresh `ClasspathMessageBundle`（classpath 热更另行规划）。

**前置：** 平台 Phase 1（`getChangedKeys()` + `publishEvent` + orchestrator）已合并。

**删除项：** `@EnableI18n.refreshOnPropertySourcesChanged`、`I18nPropertySourcesChangedListener`。

### 16.9 `I18nBundleRegistrar` 变更摘要

在 §15.8 步骤之上：

1. 每个 definition → `ClasspathMessageBundle`（含 `MessageFormat` / loader chain）。
2. 若 `environmentOverride` → 注册 `{name}EnvironmentMessageBundle`（构造注入 `Environment`、`environmentKeyPrefix` 等）。
3. 若 `springMessagesFallback` → 注册 `springMessagesMessageBundle`（`RuntimeBeanReference` 指向 delegate MessageSource）。
4. Composite：`ManagedList` 引用 **所有** MessageBundle bean（classpath + env + adapter），**不按 definition 分组**；priority
   在各类内部已设定。
5. EAGER：`I18nEagerLoadInitializer` 仅对 `ClasspathMessageBundle` 调用 `preload`（Env bundle 在 refresh 时已索引，首次
   lookup 即可）。

### 16.10 测试资源（增量）

```
src/test/resources/i18n/yaml-app/messages_zh_CN.yml
src/test/resources/i18n/yaml-app/messages.properties   # 可选，测 AUTO 优先级
```

**Env 测试：** 使用 `MapPropertySource` + `StandardEnvironment` / `@ContextConfiguration` + `TestPropertySourceUtils`
注入扁平 key。

### 16.11 测试矩阵（Phase 1.5 追加）

| #  | 测试                     | 输入                                        | 期望                             |
|----|------------------------|-------------------------------------------|--------------------------------|
| 14 | envFlatOverride        | `rose.i18n.messages.app.zh_CN.welcome=覆盖` | 命中 Env，覆盖 classpath            |
| 15 | envVsClasspathPriority | 同 key Env + classpath                     | Env 优先                         |
| 16 | envBlob                | `.properties` blob key                    | 多 key 加载                       |
| 17 | envLocaleFallback      | 仅 `zh` 层 Env key                          | 回退链与 classpath 一致              |
| 18 | envRefresh             | 改 PropertySource 后 `refresh()`            | 新值生效                           |
| 19 | propertySourcesChanged | 发布 `PropertySourcesChangedEvent`          | Env bundle refresh             |
| 20 | yamlLoad               | `messages_zh_CN.yml` 嵌套 map               | flatten 后 `order.notFound` 可解析 |
| 21 | yamlAutoFallback       | AUTO，仅 yml 存在                             | 加载 yaml                        |
| 22 | springAdapterHit       | adapter + Rose miss                       | delegate 返回值                   |
| 23 | springAdapterMiss      | 双方 miss                                   | `null`                         |
| 24 | compositeOrder         | env + app + adapter                       | priority 顺序正确                  |

**`EnvironmentMessageBundleTest` 模板：**

```java
@Test
void flatOverrideWinsOverClasspath() {
    Map<String, Object> map = Map.of(
            "rose.i18n.messages.app.zh_CN.welcome", "来自 Env");
    ConfigurableEnvironment env = new StandardEnvironment();
    env.getPropertySources().addFirst(new MapPropertySource("test", map));

    EnvironmentMessageBundle bundle = new EnvironmentMessageBundle(
            "app", 10100, env, "rose.i18n.messages.",
            KeyPrefixMode.BUNDLE, PlaceholderStyle.MESSAGE_FORMAT, true);

    assertThat(bundle.getMessage("welcome", Locale.SIMPLIFIED_CHINESE))
            .isEqualTo("来自 Env");
}
```

### 16.12 实现检查清单

- [ ] Phase 1 测试仍全绿（无回归）
- [ ] `MessageBundleLoader` SPI；`ClasspathMessageBundle` 经 chain 加载
- [ ] `EnvironmentMessageBundle` 扁平 + blob 两种 key
- [ ] Env bundle priority = classpath + 10_000
- [ ] `@EnableI18n(enableEnvironmentOverrides=true)` 注册 `{name}EnvironmentMessageBundle`
- [ ] `springMessagesFallback` 可回退 legacy `MessageSource`
- [ ] `EnvironmentMessageBundleRefreshable` 注册于 `spring.factories`；变更 `rose.i18n.messages.*` 触发 Env bundle
  refresh
- [ ] optional 依赖：`rose-spring-core`、`snakeyaml` 缺失时不破坏 Phase 1 行为
- [ ] §17 模块联调前，本地 `EnvironmentMessageBundleTest` + listener 测试通过

### 16.13 与 §17 Cloud 的衔接

- §17 `I18nRefreshOrchestrator` 在 `EnvironmentChangeEvent` 时调用 **`EnvironmentMessageBundle.refresh()`**（§16 实现）。
- Env 读取由 **§16** 完成；Cloud（§17）仍依赖 **`rose-i18n-spring-boot`** 绑定 `rose.i18n.cloud.*`，**不**依赖 Boot 实现 Env
  解析本身。

---

## 17. `rose-i18n-spring-cloud` 实现规格（Phase 3）

> **前置条件：** Phase 1（§15）+ Phase 1.5（**§16**，含 `EnvironmentMessageBundle`）+ Phase 2 Boot（`rose-i18n-spring-boot`
> ，配置绑定）已完成。  
> **目标：** 配置中心 / `/actuator/refresh` 变更后，**自动刷新** Env 覆盖的 i18n 文案；**不**做 Feign、i18n Server、Bus
> 专用客户端。

### 17.1 模块定位

| 项        | 说明                                                               |
|----------|------------------------------------------------------------------|
| artifact | `rose-i18n-spring-cloud`                                         |
| 父 POM    | `rose-i18n/pom.xml` 增加 `<module>rose-i18n-spring-cloud</module>` |
| 依赖 Cloud | `spring-cloud-context`（**optional**，`@ConditionalOnClass`）       |
| 依赖 Rose  | `rose-i18n-spring`、`rose-i18n-spring-boot`                       |
| 不依赖      | `spring-cloud-bus`、OpenFeign、Config Server 客户端（由应用自带）            |

**与 microsphere 差异：** 仅监听 `EnvironmentChangeEvent` 并刷新已有 `MessageBundle`；不引入
`PropertySourcesServiceMessageSource` 专用 Cloud 栈。

### 17.2 仓库改动清单

| 文件                                         | 改动                                                                                                       |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `rose-i18n/pom.xml`                        | `<module>rose-i18n-spring-cloud</module>`                                                                |
| `rose-i18n/rose-i18n-spring-cloud/pom.xml` | 新建，见 §17.3                                                                                               |
| `rose-parent` / `rose-bom`                 | 管理 `rose-i18n-spring-cloud`；**新增** `spring-cloud-context` 版本（与 Boot 2.7 对齐，建议 Spring Cloud **2021.0.x**） |
| `rose-coverage/pom.xml`                    | 可选依赖 `rose-i18n-spring-cloud`                                                                            |

### 17.3 `pom.xml`

路径：`rose-i18n/rose-i18n-spring-cloud/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>rose-i18n-spring-cloud</artifactId>
    <name>Rose I18n Spring Cloud</name>
    <description>Spring Cloud refresh integration for Rose i18n</description>
    <dependencies>
        <dependency>
            <groupId>io.zhijun</groupId>
            <artifactId>rose-i18n-spring</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zhijun</groupId>
            <artifactId>rose-i18n-spring-boot</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-context</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <!-- test: junit, assertj, spring-boot-test, spring-cloud-context (non-optional) -->
    </dependencies>
</project>
```

`rose-parent` `dependencyManagement` 需补充（示例，版本与 Rose Boot 2.7 对齐）：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-context</artifactId>
    <version>3.1.9</version>
</dependency>
```

### 17.4 包结构

```
rose-i18n/rose-i18n-spring-cloud/src/main/java/io/zhijun/i18n/spring/cloud/
├── autoconfigure/
│   └── I18nCloudAutoConfiguration.java
├── config/
│   └── I18nCloudProperties.java
├── event/
│   ├── I18nEnvironmentChangeListener.java
│   └── I18nRefreshScopeRefreshedListener.java    # 可选，默认不注册
└── support/
    └── I18nRefreshOrchestrator.java
```

**自动配置注册**（`META-INF/spring.factories`）：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.zhijun.i18n.spring.cloud.autoconfigure.I18nCloudAutoConfiguration
```

或由 `rose-i18n-spring-boot` 的 `@Import` 条件引入（二选一，**推荐 spring.factories 在本模块**）。

### 17.5 配置

```yaml
rose:
  i18n:
    cloud:
      enabled: true                              # 默认 true（类路径有 Cloud 且满足条件时）
      refresh-on-environment-change: true        # 监听 EnvironmentChangeEvent
      refresh-on-refresh-scope: false            # 可选监听 RefreshScopeRefreshedEvent
      env-key-prefix: rose.i18n.messages.        # 变更 key 前缀匹配
      refresh-classpath-bundles: false           # 默认 false；true 则任意变更都 refresh ClasspathMessageBundle
```

**§16 已实现：`EnvironmentMessageBundle` 读取约定（Config 中心写入；Cloud §17 消费）：**

| 方式             | Environment key 示例                                        | 说明                                |
|----------------|-----------------------------------------------------------|-----------------------------------|
| 扁平覆盖           | `rose.i18n.messages.app.zh_CN.order.notFound=新文案`         | 单 key 覆盖                          |
| 整文件 blob       | `rose.i18n.messages.app.zh_CN.properties=welcome=欢迎\n...` | 多行 properties 文本                  |
| 兼容 microsphere | `app.i18n_messages_zh_CN.properties=...`                  | 仅当 Phase 2 显式启用 `legacy-key-mode` |

`EnvironmentMessageBundle` 在 `CompositeMessageBundle` 中 **priority 高于** `ClasspathMessageBundle`（§16.6 已定义）。

### 17.6 核心类行为

#### `I18nCloudProperties`

`@ConfigurationProperties(prefix = "rose.i18n.cloud")`，由 `I18nCloudAutoConfiguration` 启用。

#### `I18nRefreshOrchestrator`（Cloud 专用，Env 刷新走平台 orchestrator）

> **Env bundle 刷新：**
> 由 [rose-spring-env-refresh-design.md §6](./rose-spring-env-refresh-design.md#6-phase-2-对接规格rose-i18n) 的
`EnvironmentMessageBundleRefreshable` + `PropertySourcesRefreshEnvironmentListener` 完成。  
> 本类 **仅** 负责 Cloud 配置项控制的 **ClasspathMessageBundle** 可选刷新。

```java
public final class I18nRefreshOrchestrator {

    public I18nRefreshOrchestrator(ApplicationContext context, I18nCloudProperties properties,
            PropertySourcesRefreshEnvironmentListener platformOrchestrator) { ... }

    /** Cloud EnvironmentChangeEvent 入口 */
    public void onKeysChanged(Set<String> changedKeys) { ... }
}
```

**`onKeysChanged` 算法：**

1. 若 `changedKeys` 为空 → return。
2. **Env 覆盖：** 调用 `platformOrchestrator.onEnvironmentChangeKeys(changedKeys)`（内部走 `Refreshable`，含 i18n Env
   bundle）。
3. **Classpath（可选）：** 若 `refreshClasspathBundles == true` 或 key 匹配 `rose.i18n.classpath.reload` → 对匹配
   `ClasspathMessageBundle` 调用 `refresh()`。
4. 记录 debug 日志：变更 key、被刷新的 classpath bundle 名。

**不**在 Cloud 模块内 parse properties；Env 索引重建见 §16.6。

#### `I18nEnvironmentChangeListener`

```java
public class I18nEnvironmentChangeListener
        implements ApplicationListener<EnvironmentChangeEvent>, SmartApplicationListener {

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        if (!properties.isRefreshOnEnvironmentChange()) return;
        orchestrator.onKeysChanged(event.getKeys());
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return EnvironmentChangeEvent.class.isAssignableFrom(eventType);
    }
}
```

触发路径：Config Server 推送 → Spring Cloud Context refresh → `/actuator/refresh` → `EnvironmentChangeEvent`。

#### `I18nRefreshScopeRefreshedListener`（可选）

- `@ConditionalOnProperty(rose.i18n.cloud.refresh-on-refresh-scope=true)`
- 监听 `RefreshScopeRefreshedEvent`，调用 `orchestrator.onKeysChanged(Collections.emptySet())` 且内部 treat empty 为 *
  *refresh 全部 EnvironmentMessageBundle**（仅当显式开启）。

#### `I18nCloudAutoConfiguration`

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnvironmentChangeEvent.class)
@ConditionalOnProperty(prefix = "rose.i18n.cloud", name = "enabled", matchIfMissing = true)
@ConditionalOnBean(name = "compositeMessageBundle")
@EnableConfigurationProperties(I18nCloudProperties.class)
public class I18nCloudAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public I18nRefreshOrchestrator i18nRefreshOrchestrator(
            ApplicationContext context, I18nCloudProperties properties) {
        return new I18nRefreshOrchestrator(context, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.i18n.cloud", name = "refresh-on-environment-change",
            matchIfMissing = true)
    public I18nEnvironmentChangeListener i18nEnvironmentChangeListener(
            I18nRefreshOrchestrator orchestrator, I18nCloudProperties properties) {
        return new I18nEnvironmentChangeListener(orchestrator, properties);
    }
}
```

### 17.7 与 `rose-spring-core` 的关系

| 路径                   | 场景                                                                              |
|----------------------|---------------------------------------------------------------------------------|
| **Cloud 模块**         | Config Server / `EnvironmentChangeEvent`（§17）                                   |
| **rose-spring-core** | 非 Cloud 应用的 `PropertySourcesChangedEvent`（Phase 2 在 `rose-i18n-spring-boot` 监听） |

两者 **不重复**：Cloud 应用优先 Cloud 事件；纯 Boot 应用用 Listenable Environment 事件。

### 17.8 非目标（本模块不做）

| 项                          | 替代                                         |
|----------------------------|--------------------------------------------|
| i18n Server + Feign        | Config 中心 key + `EnvironmentMessageBundle` |
| Spring Cloud Bus 专用监听器     | Bus 刷新最终仍触发本地 `EnvironmentChangeEvent`     |
| 集中式 Actuator 导出全部文案        | 放 `rose-i18n-spring-boot` Actuator（§9）     |
| 修改 Config Server / Gateway | 应用侧依赖即可                                    |

### 17.9 测试计划

| 测试类                                 | 场景                                                                    |
|-------------------------------------|-----------------------------------------------------------------------|
| `I18nRefreshOrchestratorTest`       | 给定 changedKeys，仅匹配 prefix 的 Env bundle 被 refresh                      |
| `I18nEnvironmentChangeListenerTest` | 发布 `EnvironmentChangeEvent`，mock bundle 验证 `refresh()` 调用             |
| `I18nCloudAutoConfigurationTest`    | 无 Cloud 类时不注册；有 Cloud + composite bean 时注册 listener                   |
| `I18nCloudIntegrationTest`          | `@SpringBootTest` + 内存 PropertySource 变更 + `@EnableI18n` + Env 覆盖 key |

**集成测试 sketch：**

```java
@SpringBootTest(classes = { CloudI18nTestApp.class })
@EnableI18n("app")
class I18nCloudIntegrationTest {
    @Autowired MessageSource messageSource;
    @Autowired ConfigurableEnvironment environment;

    @Test
    void reloadsAfterEnvironmentChange() {
        // 1. 初始无 override
        // 2. environment.getPropertySources().addFirst(map with rose.i18n.messages.app.zh_CN.welcome)
        // 3. publishEvent(new EnvironmentChangeEvent(context, Set.of("rose.i18n.messages.app.zh_CN.welcome")))
        // 4. assert messageSource.getMessage(...) 为新值
    }
}
```

### 17.10 验收命令

```bash
mvn -pl rose-i18n/rose-i18n-spring-cloud test
```

### 17.11 实现检查清单

- [ ] 仅当 classpath 存在 `EnvironmentChangeEvent` 时装配
- [ ] `rose.i18n.cloud.enabled=false` 时不注册 listener
- [ ] 变更 unrelated key（如 `server.port`）不 refresh i18n bundle（默认）
- [ ] 变更 `rose.i18n.messages.*` 触发 `EnvironmentMessageBundle.refresh()`
- [ ] 不引入 Feign / Bus 硬依赖
- [ ] 与 §16 `EnvironmentMessageBundle` 联调通过

### 17.12 实施顺序

1. 完成 Phase 1（§15）+ Phase 1.5（§16）
2. 完成 Phase 2 Boot（`rose-i18n-spring-boot`，`rose.i18n.cloud.*` 配置）
3. 根 POM 引入 `spring-cloud-context` BOM 或版本管理
4. 实现 §17.4–§17.6
5. §17.9 测试通过后，在 `wiki/rose-bom/Consumer-Guide` 增加 Cloud 使用说明
