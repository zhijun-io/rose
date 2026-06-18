# Rose Spring

Spring Framework 层扩展（**非 Boot**）：环境刷新、属性源、配置 Bean 绑定等，供 `rose-spring-boot` 与各能力模块复用。

## 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| `rose-spring-core` | `rose-spring-core` | 全部 Spring 扩展实现 |

## 已实现

### 环境刷新（`env.refresh`）

- `ListenableConfigurableEnvironment`：PropertySource 变更事件
- `Refreshable` SPI + `PropertySourcesRefreshEnvironmentListener`
- 与 `rose-spring-boot-core` 的 `RoseBindListener` 可协作

### 属性源（`propertysource`）

- `@ResourcePropertySource` / `@YamlPropertySource` / `@ResourcePropertySources`
- `YamlPropertySourceFactory`
- 文件热更：`AutoRefreshWatcher`、`FileWatchService`

### 配置 Bean 绑定（`binder`）

- `@EnableConfigurationBeanBinding`：plain `spring-context` 下的多 Bean 属性绑定（Rose 版 `@ConfigurationProperties` 替代）
- `ConfigurationBeanBindingRefreshable`：环境热更时 rebind

### 其他

- `PropertyAdapter`：外部配置 → Rose 属性适配
- `LoggingEnvironmentListener`

## 未实现 / 规划中

| 主题 | 状态 | 文档 |
|------|------|------|
| `rose-spring-web` | ❌ 无代码 | [rose-spring-web-handler-design.md](../docs/rose-spring-web-handler-design.md) |
| `rose-i18n` | ❌ 无代码 | [rose-i18n-design.md](../docs/rose-i18n-design.md) |
| Boot `@ConfigurationProperties` 自动 rebind | 远期 | [configuration-bean-binding-design](../docs/rose-spring-configuration-bean-binding-design.md) §7 |
| JDBC / Test 子模块 | ❌ | microsphere-spring 有 `jdbc`、`test` 等专题 |

## 对标 Arconia

Arconia 侧重 Quarkus CDI / 配置扩展。Rose Spring 层解决的是 **Spring `Environment` / PropertySource 增强**，与 Arconia 无直接模块对应；Dev Services 动态属性语义在 `rose-dev-services` 与 Arconia 对齐。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-spring](https://github.com/microsphere-projects/microsphere-spring) 配置/属性源 | `rose-spring-core` propertysource + binder | ✅ 部分已有 |
| microsphere-spring env 刷新 | `env.refresh` | ✅ 已有 |
| microsphere-spring-web / webmvc | — | ❌ 见 `rose-spring-web` 设计 |
| microsphere-i18n | — | ❌ 见 `rose-i18n` 设计 |

## 建议下一步

1. **高**：按设计落地 `rose-spring-web`（Handler 增强、`@TenantRequired` 等与 multitenancy 联动）
2. **中**：评估 `rose-i18n` 是否独立主题
3. **低**：主题级 `*-tests` 共享测试工具（先于全库 `rose-test`）
