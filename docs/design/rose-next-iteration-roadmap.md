# Rose 下一步迭代方向

**状态：** 2026-06-27 维护者路线图（基于 [microsphere-projects](https://github.com/orgs/microsphere-projects/repositories)
公开仓库对标）  
**受众：** 维护者、PR 规划、Agent 任务分解  
**关联：** [microsphere-benchmark-notes.md](./microsphere-benchmark-notes.md)（能力对标细节）、[README.md](./README.md)
（设计索引）

> **原则：** 不复制 Microsphere 功能清单；只在 Rose **已有产品方向**或**已写设计规格**处对齐结构与优先级。  
> Rose 基线：**Spring Boot 2.7 / Java 8**；消费方通过 **`rose-bom`** 对齐版本。

---

## 1. 对标快照（Microsphere 组织，2026-06）

### 1.1 仓库分层（按最近活跃度）

| 层级         | 代表仓库                                                                | 最近更新      | 与 Rose 关系                                  |
|------------|---------------------------------------------------------------------|-----------|--------------------------------------------|
| 构建 / BOM   | `microsphere-build`, `microsphere-bom`                              | 2026-06   | 对标 `rose-build`, `rose-bom`                |
| Java 基础    | `microsphere-java` (★102)                                           | 2026-06   | 对标 `rose-core`                             |
| Spring 扩展  | `microsphere-spring` (★61)                                          | 2026-06   | **高度重叠** `rose-spring-core`                |
| Boot 平台    | `microsphere-spring-boot` (★21)                                     | 2026-06   | 对标 `rose-spring-boot`                      |
| Cloud / 兼容 | `microsphere-spring-cloud`                                          | 2026-06   | Rose **暂无**；文档策略可借鉴                        |
| 数据         | `microsphere-mybatis`, `microsphere-hibernate`, `microsphere-redis` | 2026-06   | 对标 `rose-mybatis-plus`；Redis 仅在 devservice |
| 可观测        | `microsphere-observability`, `microsphere-logging`                  | 2025–2026 | Rose **`rose-observation` 更强**（OTel 全栈）    |
| 国际化        | `microsphere-i18n`                                                  | 2026-06   | Rose **规划** `rose-i18n`（设计已完成）             |
| 测试         | `microsphere-test`                                                  | 2026-03   | 对标 `rose-test` + 各主题 `*-test`              |
| 运行时 / 生态   | gateway, resilience4j, sentinel, dubbo, nacos…                      | 各异        | Rose **非目标**（无 Cloud / 微服务栈定位）             |

### 1.2 Microsphere 近期重点能力（README 摘要）

**`microsphere-spring-context`（与 Rose 重叠最多）：**

- Listenable `Environment`、增强 `@PropertySource`（YAML/JSON/热更）
- `@EnableConfigurationBeanBinding`、配置热更
- `@EnableTTLCaching` / `@TTLCacheable`
- 并行 Bean 实例化

**`microsphere-spring-boot-core`（Rose 部分具备、部分缺口）：**

- classpath `config/default/*.properties` 默认配置
- 可合并的 `microsphere.autoconfigure.exclude`
- `@ConfigurationProperties` **BindListener**（运行时变更通知）
- 自定义 Actuator 端点、classpath 构件冲突检测
- Monitored `TaskScheduler` + Micrometer
- Boot 2.x–4.x **兼容 shim** 模块

---

## 2. Rose 当前能力矩阵

| 能力域                        | Rose 模块                                          | 实现状态            | Microsphere 对照                    | 差距摘要                                                           |
|----------------------------|--------------------------------------------------|-----------------|-----------------------------------|----------------------------------------------------------------|
| 纯 Java 基础                  | `rose-foundation`                                | ✅               | `microsphere-java`                | 保持克制，避免变「工具堆」                                                  |
| Listenable Environment     | `rose-spring-core`                               | ✅               | `microsphere-spring-context`      | 已对齐；需持续补单测                                                     |
| PropertySource 增强          | `rose-spring-core`                               | ✅               | 同上                                | YAML/JSON/热更已实现                                                |
| Configuration Bean Binding | `rose-spring-core`                               | ✅ 核心 / 🟡 热更集成测 | `@EnableConfigurationBeanBinding` | 单测已补；IT + Cloud 转发待加强                                          |
| Env 热更 / Refreshable       | `rose-spring-core`                               | ✅               | 同上                                | 与 i18n、binding 联动待验收                                           |
| Bootstrap / 模式检测           | `rose-spring-boot-core`                          | ✅               | Boot 默认配置 + 诊断                    | 缺统一 default properties                                         |
| FailureAnalyzer            | 各主题分散                                            | 🟡              | 构件冲突 / 依赖健康                       | 缺统一注册表与文档                                                      |
| Actuator 扩展                | `rose-spring-boot-actuator`, devservice-actuator | 🟡              | 4 个自定义端点                          | 可暴露 Rose 运行时状态                                                 |
| OpenTelemetry              | `rose-observation/*`                             | ✅ 领先            | `microsphere-observability`       | Rose 差异化优势                                                     |
| Dev Services               | `rose-devservice/*`                              | ✅ 领先            | 无直接对标                             | Testcontainers 连接器生态                                           |
| 多租户                        | `rose-multitenancy/*`                            | ✅               | 无直接对标                             | 与 cache/i18n 协作                                                |
| MyBatis-Plus               | `rose-mybatis-plus/*`                            | 🟡              | `microsphere-mybatis`             | 缺 SQL 拦截扩展面                                                    |
| i18n                       | —                                                | ❌ 仅设计           | `microsphere-i18n`                | [rose-i18n-design.md](./rose-i18n-design.md) Phase 1 就绪        |
| TTL Cache                  | —                                                | ❌ 仅设计           | `@TTLCacheable`                   | [rose-cache-design.md](./rose-cache-design.md)                 |
| Web Handler SPI            | `rose-spring-web` 占位                             | ❌ 仅设计           | webmvc/webflux                    | [rose-web-handler-design](./rose-spring-web-handler-design.md) |
| 测试支持                       | `rose-test`, `rose-devservice-test`              | 🟡              | `microsphere-test`                | 主题级 `*-test` 已起步                                               |
| Boot 3 / Jakarta           | —                                                | ❌               | `spring-boot-compatible`          | 需独立分支 / 兼容策略                                                   |

图例：✅ 已交付 · 🟡 部分 · ❌ 未开始

---

## 3. 战略判断

### 3.1 Rose 已领先或应保持差异化的领域

1. **`rose-observation`** — OTel SDK、Logback、Micrometer OTLP 一体化；不必向 Microsphere 可观测仓库收敛。
2. **`rose-devservice`** — Testcontainers 连接器 + Bootstrap 集成是 Rose 独有卖点；优先 **连接器覆盖与 DX**，而非泛化
   Redis/MySQL 应用层集成。
3. **`rose-multitenancy`** — 独立主题；与 i18n/cache 的 key 策略是 Rose 内部协同点。

### 3.2 应对齐 Microsphere「平台层」而非「功能堆叠」的领域

1. **`rose-spring-boot`** — 把 bootstrap、默认配置、自动配置排除、诊断收拢为**一个可叙述的平台层**（类似
   `microsphere-spring-boot-core` README 结构）。
2. **`rose-spring-core`** — 已有与 `microsphere-spring-context` 平行的核心；下一步是**测试与文档**，不是再开平行 API。
3. **`rose-mybatis-plus`** — 参照 `microsphere-mybatis`，把拦截/审计做成**显式扩展管道**，而非散落 InnerInterceptor。

### 3.3 明确非目标（本迭代周期不做）

- Spring Cloud / Gateway / Nacos / Dubbo 集成
- WebFlux、Guice、MicroProfile / Jakarta EE
- Resilience4j / Sentinel 独立主题
- 为对标而对标 Boot 3 全量迁移（仅作 **文档化分支策略** 预备）

---

## 4. 推荐迭代路线

### Phase 0 — 平台稳固（1–2 周，**当前优先**）

| #   | 工作项                                            | 产出                                                                                              | 依据                               |
|-----|------------------------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------|
| 0.1 | 补全 `rose-spring-core` 单测缺口                     | `ConversionServiceResolver`、`ConfigurationBeanBindingSupport` 等                                 | Convention §6；已部分完成              |
| 0.2 | `rose-devservice-test` 依赖 `rose-test`（compile） | 编译通过、无重复 junit/assertj                                                                          | 测试支持库模式                          |
| 0.3 | 消费者兼容矩阵                                        | [wiki/rose-build/Compatibility-Matrix.md](../wiki/rose-build/Compatibility-Matrix.md)           | 对标 `microsphere-spring-cloud` 文档 |
| 0.4 | FailureAnalyzer 注册表                            | 更新 [bootstrap-diagnostics-design §6](./rose-spring-boot-bootstrap-diagnostics-design.md) + wiki | 分散 → 可查询                         |
| 0.5 | CI 绿 + coverage 不回归                            | `./mvnw verify -DskipITs` / `-Pcoverage`                                                        | 合并门槛                             |

**验收：**

```bash
./mvnw -B -ntp verify -DskipITs
./mvnw -B -ntp -Pcoverage verify -DskipITs
```

---

### Phase 1 — Boot 平台层增强（2–4 周）

| #   | 工作项                                | 说明                                             | Microsphere 参考                      |
|-----|------------------------------------|------------------------------------------------|-------------------------------------|
| 1.1 | **Default properties 加载**          | classpath `config/default/*.properties`，可被应用覆盖 | `microsphere-spring-boot-core`      |
| 1.2 | **Auto-configuration 排除合并**        | Rose 命名空间下可累积 exclude（避免后覆盖前）                  | `microsphere.autoconfigure.exclude` |
| 1.3 | **Actuator：Rose 运行时视图**            | dev-services 容器状态、OTel 生效配置摘要                  | 自定义 `/actuator/rose/*`              |
| 1.4 | **ConfigurationBeanBinding 热更 IT** | prefix 变更 → rebind 端到端                         | env-refresh × binding 设计 §7         |
| 1.5 | **BindListener（可选）**               | 若多个主题需要「配置变更通知」，再引入；避免与 Refreshable 重复         | Boot BindListener                   |

**非目标：** 不在此阶段做 Boot 3 代码路径。

---

### Phase 2 — 数据与横切主题（4–8 周）

| #   | 工作项                               | 优先级 | 设计文档                                             |
|-----|-----------------------------------|-----|--------------------------------------------------|
| 2.1 | **`rose-mybatis-plus` 拦截扩展面**     | 高   | 新建或扩展 design；参考 `microsphere-mybatis`            |
| 2.2 | **`rose-i18n` Phase 1**           | 高   | [rose-i18n-design.md §15](./rose-i18n-design.md) |
| 2.3 | **`rose-i18n` Phase 1.5**         | 中   | Env bundle + 热更（依赖 env-refresh）                  |
| 2.4 | **`rose-cache` Phase 1**          | 中   | 在多租户缓存 key 有明确需求后启动                              |
| 2.5 | **`rose-spring-web` MVC Phase 1** | 低–中 | HandlerMethodInterceptor 最小 SPI                  |

**顺序建议：** 2.1 → 2.2 → 2.3 → 2.4 → 2.5（2.4 可并行 2.2，若团队容量允许）。

---

### Phase 3 — 规模化与长期（8 周+）

| #   | 工作项                                     | 说明                                                                                |
|-----|-----------------------------------------|-----------------------------------------------------------------------------------|
| 3.1 | Dev Services **并行启动**                   | [bootstrap-diagnostics §8](./rose-spring-boot-bootstrap-diagnostics-design.md) 远期 |
| 3.2 | **Boot 3 / Jakarta 分支策略**               | 文档 + `rose-build` profile；可参考 compatible shim 思路                                  |
| 3.3 | Classpath **构件冲突检测**                    | FailureAnalyzer + 启动期扫描                                                           |
| 3.4 | 主题级 README + wiki 同步                    | 每个 `rose-*` 聚合目录一页「能做什么」                                                          |
| 3.5 | 评估 **BindListener vs Refreshable** 统一模型 | 避免两套配置变更 API                                                                      |

---

## 5. 模块级下一步（速查）

| 模块                  | 下一步                                    | 不建议                       |
|---------------------|----------------------------------------|---------------------------|
| `rose-foundation`   | 保持稳定；`rose-test` 作为唯一测试 BOM 入口         | 向 `rose-core` 堆 Spring 依赖 |
| `rose-spring-core`  | 热更 + binding 集成测；文档与 microsphere 差异表   | 新建 `-context` 子模块（已内聚）    |
| `rose-spring-boot`  | default properties、exclude 合并、Actuator | 复制 Cloud 集成               |
| `rose-observation`  | 文档化 OTel 组合推荐；保持 slice 独立              | 合并 logging 大杂烩模块          |
| `rose-devservice`   | 连接器质量、Renovate 约束（Testcontainers 1.x）  | 泛化为应用 Redis 客户端           |
| `rose-multitenancy` | 与 i18n/cache 的 key 契约                  | 扩成 gateway 路由             |
| `rose-mybatis-plus` | 拦截链、审计、数据权限 pipeline                   | 重命名为 `-data-*`  umbrella  |
| `rose-i18n`         | 按 §15 开工                               | 首 PR 包含 Cloud / Boot 全量   |
| `rose-cache`        | 等 multitenancy 缓存场景明确                  | 无需求提前实现                   |

---

## 6. 成功指标

| 指标                       | Phase 0    | Phase 1         | Phase 2                  |
|--------------------------|------------|-----------------|--------------------------|
| `./mvnw verify`（含 IT 可选） | 绿          | 绿               | 绿                        |
| `rose-spring-core` 行覆盖率  | ≥ 现有基线 +5% | 维持              | 维持                       |
| 设计文档 ↔ 代码                | 状态表更新      | binding IT 标记 ✅ | i18n Phase 1 ✅           |
| 消费者文档                    | 兼容矩阵       | Boot 平台能力页      | 主题 Getting Started       |
| BOM 破坏性变更                | 0          | 0（additive）     | 评估 cache/i18n 新 artifact |

---

## 7. 文档与 Agent 协作

- **迭代规划提示词：** [../prompts/analyze-next-iteration.prompt.md](../prompts/analyze-next-iteration.prompt.md)
- **对标细节：** [microsphere-benchmark-notes.md](./microsphere-benchmark-notes.md)
- **实现约束：** [../rose-conventions.md](../rose-conventions.md)

更新本路线图时：同步修改 §2 状态矩阵与 Phase 表格中的 ✅/🟡/❌，并在 CHANGELOG 记录消费者可见变更。

---

## 8. 修订记录

| 日期         | 说明                                                |
|------------|---------------------------------------------------|
| 2026-06-27 | 初版：基于 microsphere-projects 37 个公开仓库与 Rose 设计稿/代码库 |
