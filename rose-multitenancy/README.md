# Rose Multitenancy

租户上下文、解析、缓存与 Web 集成。

## 迭代导航

### 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-multitenancy-core` | `TenantContext`、resolver、`TenantDetailsService`、MDC/observation |
| Web | `rose-multitenancy-web` | Header/Cookie 解析、`TenantContextFilter`、`@TenantIdentifier` |
| Starters | `rose-multitenancy-core-spring-boot-starter`、`rose-multitenancy-web-spring-boot-starter` | 应用入口 |

### 已实现

| 能力 | 说明 |
|------|------|
| `TenantContext` + `Scope` / `Carrier` | 线程级租户上下文 |
| `TenantDetailsService` / `PropertiesTenantDetailsService` | 租户元数据 |
| `FixedTenantResolver` | 固定租户（测试/单租户） |
| `HeaderTenantResolver` / `CookieTenantResolver` | HTTP 租户解析 |
| `TenantContextFilter` | Servlet 过滤器链集成 |
| `@TenantIdentifier` | Controller 参数解析 |
| MDC + Observation 集成 | 日志与观测携带 tenant |
| MyBatis-Plus | `TenantIdSupplier` 集成（见 `rose-mybatis-plus`） |

### 未实现 / 规划中

- 方法级 `@TenantRequired` 拦截器（规划在 `rose-spring-web` Phase 3）
- `TenantMessageBundle` 与 `rose-i18n` 联动
- 分布式 / 多活路由（参考 microsphere-multiactive，仅概念）
- 详细配置属性表与用法示例

### 对标 Arconia

Arconia 多租户多在应用架构层自行实现。Rose 提供 **Spring Boot 可插拔** 的 tenant context + Web 解析，无 Arconia 官方模块一一对应。

### 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| microsphere-spring 上下文扩展 | `rose-multitenancy-core` | ⚠️ 专题化 |
| microsphere-multiactive | — | ❌ 仅远期参考 |

### 建议下一步

1. 补全 `rose.multitenancy.*` 配置文档与示例
2. 与 `rose-spring-web` 同步落地 `@TenantRequired`
3. 评估数据源级多租户（dynamic datasource）是否独立子模块
