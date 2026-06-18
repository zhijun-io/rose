# Rose Spring Boot

Rose 的 Spring Boot 集成模块族，结构参考 [microsphere-spring-boot](https://github.com/microsphere-projects/microsphere-spring-boot)。

## 模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| `rose-spring-boot` | `rose-spring-boot` (pom) | 聚合父 POM |
| `rose-spring-boot-core` | `rose-spring-boot-core` | 共享 Boot 工具（如 `RoseBinder`） |
| `rose-spring-boot-starter` | `rose-spring-boot-starter` | 应用基线 starter（聚合 core + spring-core + boot-starter） |

## 消费方式

通过 `rose-bom` 对齐版本：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot-starter</artifactId>
</dependency>
```

仅需工具类、不需要 starter 栈时：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot-core</artifactId>
</dependency>
```

## 模块默认配置

各 Rose 模块可在 classpath 放置可覆盖的推荐默认值：

```
src/main/resources/rose/default/<模块名>.properties
src/main/resources/rose/default/<模块名>.yml    # 或 .yaml
```

启动时扫描 `classpath*:rose/default/*.properties`、`*.yml`、`*.yaml`，扁平化后合并进 Spring Boot `defaultProperties`（最低优先级，`application.yml` 可覆盖）。

| 配置 | 默认 | 说明 |
|------|------|------|
| `rose.default-config.enabled` | `true` | 总开关（见下方 **生效时机**） |
| `rose.default-config.locations` | — | 额外 location pattern，逗号分隔 |

### 生效时机

`DefaultConfigPropertiesEnvironmentPostProcessor` 在 `application.yml` 加载**之前**运行。因此：

- **可用**：`-Drose.default-config.enabled=false`、`ROSE_DEFAULT_CONFIG_ENABLED=false`、其他启动前环境变量/系统属性
- **通常无效**：仅在 `application.yml` 里写 `rose.default-config.enabled=false`

应用配置（`application.yml`、profile、环境变量等）仍可覆盖已加载的默认 **属性值**；总开关要在启动早期设置。

### 合并与冲突

- 多个 jar 的 `rose/default/*` 会按 resource URL **字典序**合并，同 key **后者覆盖前者**
- 覆盖发生时打 **DEBUG** 日志：`Rose default config key '...' overridden in ...`
- **避免**多模块定义同一 key；文件名仅作识别，不决定优先级

### YAML 处理

加载器在代码层保证与 `.properties` 一致的行为：

- **类型**：YAML 叶子值规范化为 `String`（与 properties 相同），避免同 key 因 `18080` vs `"18080"` 产生绑定差异
- **列表**：扁平化为 Spring 索引键，如 `tags[0]`、`servers[0].host`
- **多文档**：`---` 分隔的多段 YAML 按顺序合并；非 map 根文档跳过并 **WARN**

`@PropertySource` 使用的 `YamlPropertySourceFactory` 仍保留 YAML 原生类型，仅 `rose/default` 默认配置走规范化。

### 与 DevServices 的关系

对齐 [Arconia Dev Services](https://docs.arconia.io/arconia/latest/dev-services/)：

| 机制 | 说明 |
|------|------|
| `rose/default/*`（EPP） | 静态推荐默认 → `defaultProperties` |
| `setDefaultProperty` | 同上 |
| `addDynamicProperty` | 最高优先级，开发/测试时覆盖手动配置 |
| `BootstrapMode` | 见 `rose-dev-services-core` |
| Actuator 端点 | `rose-dev-services-actuator`，`@ConditionalOnDevMode` |

Boot 2.7 暂无 `@ServiceConnection`，连接注入仍走 `addDynamicProperty`（Arconia Boot 3 路径后续再对齐）。

### 模块约定

- 每个模块一个文件，文件名建议与 artifact 对应（如 `dev-services.properties`）
- 只放跨应用、可安全覆盖的推荐默认项

`rose-spring-boot-core` 自带 `rose/default/core.properties`（graceful shutdown 等）。

## 与 Microsphere 的对照

| Microsphere | Rose（当前/规划） | 借鉴优先级 |
|-------------|-------------------|------------|
| `microsphere-spring-boot-core` | `rose-spring-boot-core` | ✅ 已有 |
| `microsphere-spring-boot-actuator` | 未来 `rose-spring-boot-actuator` | 中（诊断端点、调度监控） |
| `microsphere-spring-boot-dependencies` | `rose-bom` | ✅ 已有，不重复 |
| `microsphere-spring-boot-compatible` | 暂不引入（Rose 锁定 Boot 2.7 / Java 8） | 低 |
| 默认 `rose/default/*.properties` | ✅ `rose-spring-boot-core` | 中 |
| `BindListener` / 配置绑定监听 | 可与 `rose-spring-core` env 刷新协作评估 | 中 |
| `FailureAnalyzer` / classpath 冲突检测 | 各主题模块分散注册（见 bootstrap-diagnostics 设计） | 中 |
| `microsphere.autoconfigure.exclude` 合并排除 | 可选，解决多模块各自 exclude 被覆盖问题 | 低 |

## 边界

- **不放**各主题业务 AutoConfiguration（见 `CONTRIBUTING.md`）
- **不放** Dev Services / BootstrapMode（见 `rose-dev-services-core`）
- 新 Boot 横切能力优先进 `rose-spring-boot-core` 或独立子模块（如未来的 `rose-spring-boot-actuator`）

## 相关文档

- [module-layering](../docs/module-layering.md)
- [microsphere-benchmark-notes](../docs/microsphere-benchmark-notes.md)
- [bootstrap-diagnostics](../docs/rose-spring-boot-bootstrap-diagnostics-design.md)（FailureAnalyzer 规范，Bootstrap 章节已过时）
