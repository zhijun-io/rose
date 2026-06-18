# Rose Multitenancy

租户上下文、解析、缓存与 Web 集成。

## 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-multitenancy-core` | `TenantContext`、resolver、`TenantDetailsService`、MDC/observation（无 Boot） |
| Web | `rose-multitenancy-web` | Header/Cookie 解析、`TenantContextFilter`、`@TenantIdentifier`（无 Boot） |
| Starter | `rose-multitenancy-spring-boot-starter` | Spring Boot 自动装配（唯一 Boot 入口） |

## 依赖

**非 Web 应用：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot-starter</artifactId>
</dependency>
```

**Web 应用（额外引入 web 模块）：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-web</artifactId>
</dependency>
```

## 已实现

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

## 迁移说明

| 旧 Artifact | 新 Artifact |
|-------------|-------------|
| `rose-multitenancy-core-spring-boot-starter` | `rose-multitenancy-spring-boot-starter` |
| `rose-multitenancy-web-spring-boot-starter` | `rose-multitenancy-spring-boot-starter` + `rose-multitenancy-web` |
