# Rose Multitenancy

租户上下文、解析、缓存与 Web 集成。

## 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-multitenancy-core` | `TenantContext`、resolver、`TenantDetailsService`、Micrometer observation（无 Spring） |
| Spring | `rose-multitenancy-spring` | 事件、Cache Key、异步传播、MDC、Servlet/WebMVC（`@TenantId`、`TenantContextFilter` 等，无 Boot） |
| Boot | `rose-multitenancy-spring-boot` | Spring Boot 自动装配（唯一 Boot 入口） |

## 依赖

**Spring Boot 应用（推荐）：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot</artifactId>
</dependency>
```

Starter 传递依赖 `rose-multitenancy-spring`；Servlet 环境下自动启用 HTTP 租户解析。

## 包名

| 模块 | 根包 |
|------|------|
| `rose-multitenancy-core` | `io.zhijun.multitenancy.{context,detail,exception,observation}.*` |
| `rose-multitenancy-spring` | `io.zhijun.multitenancy.spring.*`（WebMVC 在 `spring.web`） |
| `rose-multitenancy-spring-boot` | `io.zhijun.multitenancy.boot.autoconfigure.*`、`io.zhijun.multitenancy.boot.autoconfigure.web.*` |

**非 Boot、纯 Spring 应用：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring</artifactId>
</dependency>
```

**仅要租户上下文 API（无 Spring）：**

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-core</artifactId>
</dependency>
```

## 配置

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `rose.multitenancy.resolution.fixed.enabled` | `false` | 固定租户（测试/单租户） |
| `rose.multitenancy.resolution.fixed.tenant-identifier` | `default` | 固定租户 ID |
| `rose.multitenancy.resolution.http.enabled` | `true` | HTTP 租户解析 |
| `rose.multitenancy.resolution.http.resolution-mode` | `header` | `header` 或 `cookie` |
| `rose.multitenancy.resolution.http.header.header-name` | `X-TenantId` | 请求头名称 |
| `rose.multitenancy.resolution.http.cookie.cookie-name` | `TENANT-ID` | Cookie 名称 |
| `rose.multitenancy.resolution.http.filter.enabled` | `true` | 启用 `TenantContextFilter` |
| `rose.multitenancy.resolution.http.filter.ignore-paths` | `/actuator/**` 等 | 跳过租户解析的路径 |
| `rose.multitenancy.logging.mdc.enabled` | `true` | 将租户写入 MDC |
| `rose.multitenancy.logging.mdc.key-name` | `tenantId` | MDC 键名 |
| `rose.multitenancy.async.propagation-enabled` | `true` | 向 `ThreadPoolTaskExecutor` 传播租户 |

## 接入示例

```yaml
rose:
  multitenancy:
    resolution:
      http:
        resolution-mode: header
        header:
          header-name: X-TenantId
```

```java
@RestController
class ItemController {
    @GetMapping("/items")
    List<Item> list(@TenantId String tenantId) {
        return itemService.findByTenant(tenantId);
    }
}
```

异步任务默认通过 `TenantContextTaskDecorator` 传播当前租户；也可手动使用：

```java
TenantContext.where(tenantId).run(() -> asyncService.process());
```

## 已实现

| 能力 | 说明 |
|------|------|
| `TenantContext` + `Scope` / `Carrier` | 线程级租户上下文 |
| `TenantDetailsService` / `PropertiesTenantDetailsService` | 租户元数据 |
| `FixedTenantResolver` | 固定租户（测试/单租户） |
| `HeaderTenantResolver` / `CookieTenantResolver` | HTTP 租户解析 |
| `TenantContextFilter` | Servlet 过滤器链集成 |
| `@TenantId` | Controller 参数解析 |
| MDC + Observation 集成 | 日志与观测携带 tenant |
| 异步传播 | `TenantContextTaskDecorator` + `ThreadPoolTaskExecutor` BPP |
| MyBatis-Plus | `TenantIdSupplier` 行级过滤（见 `rose-mybatis-plus`） |

## 迁移说明

| 旧 Artifact | 新 Artifact |
|-------------|-------------|
| `rose-multitenancy-core-spring-boot-starter` | `rose-multitenancy-spring-boot` |
| `rose-multitenancy-web-spring-boot-starter` | `rose-multitenancy-spring-boot` |
| `rose-multitenancy-web` | `rose-multitenancy-spring` |
