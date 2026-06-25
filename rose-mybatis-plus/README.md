# Rose MyBatis Plus

MyBatis-Plus 扩展：审计字段、字段加密、数据权限、租户集成。

## 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-mybatis-plus-core` | 能力库、SPI、MyBatis 拦截器（无 Spring） |
| Spring | `rose-mybatis-plus-spring` | 纯 Spring 集成（`@EnableMyBatisPlusExtension`） |
| Starter | `rose-mybatis-plus-spring-boot` | Spring Boot 自动装配入口 |

## 依赖关系

```
rose-mybatis-plus-spring-boot
  ├── rose-mybatis-plus-spring
  │     └── rose-mybatis-plus-core
  └── rose-mybatis-plus-core
```

| 场景 | 依赖 |
|------|------|
| Spring Boot | `rose-mybatis-plus-spring-boot` |
| 纯 Spring | `rose-mybatis-plus-spring` + `@EnableMyBatisPlusExtension` |
| 自行装配 | `rose-mybatis-plus-core` |

## 包名

| 模块 | 根包 |
|------|------|
| `rose-mybatis-plus-core` | `io.zhijun.mybatisplus.core.*` |
| `rose-mybatis-plus-spring` | `io.zhijun.mybatisplus.spring.*` |
| `rose-mybatis-plus-spring-boot` | `io.zhijun.mybatisplus.boot.autoconfigure.*` |

## 已实现

| 能力 | 说明 |
|------|------|
| 审计 | `AuditMetaObjectHandler`、`AuditableEntity` 自动填充 |
| 字段加密 | `@FieldEncrypt`、`FieldEncryptInterceptor` |
| 数据权限 | `@DataPermission`、`RoseDataPermissionHandler` |
| 租户 | `TenantIdSupplier` + `TenantLineInnerInterceptor`，与 `rose-multitenancy` 自动集成 |
| SQL 观测 | `SqlObservationInterceptor`（Micrometer / OpenTelemetry，classpath 有时自动装配） |
| 扩展点 | `MybatisPlusInterceptorCustomizer`（core SPI，spring 注册 BPP） |

## 配置

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `rose.mybatis-plus.enabled` | `true` | 总开关 |
| `rose.mybatis-plus.tenant.enabled` | `true` | 租户行过滤 |
| `rose.mybatis-plus.tenant.column` | `tenant_id` | 租户列名 |
| `rose.mybatis-plus.tenant.ignore-tables` | 空 | 跳过多租户的表 |
| `rose.mybatis-plus.observation.enabled` | `false` | SQL metrics/tracing（需 `MeterRegistry` 或 `Tracer` Bean；生产建议显式开启） |
| `rose.mybatis-plus.encryptor.password` | — | 默认加密密钥 |

## 租户接入

引入 `rose-multitenancy-spring-boot` 后，自动注册 `TenantContextTenantIdSupplier`，并向 `MybatisPlusInterceptor` 挂载 `TenantLineInnerInterceptor`：

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-mybatis-plus-spring-boot</artifactId>
</dependency>
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot</artifactId>
</dependency>
```

```yaml
rose:
  mybatis-plus:
    tenant:
      column: tenant_id
      ignore-tables: sys_dict,sys_config
```

自定义租户来源时，提供 `TenantIdSupplier` Bean 即可覆盖默认实现。

## 纯 Spring 接入

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-mybatis-plus-spring</artifactId>
</dependency>
```

```java
@Configuration
@EnableMyBatisPlusExtension
class MyBatisConfig {
    @Bean
    MybatisPlusInterceptorCustomizer tenantLineCustomizer() {
        return interceptor -> { /* ... */ };
    }
}
```

## 未实现 / 规划中

- 加密算法可插拔与密钥轮换文档
- 数据权限规则链 / SPI 文档化

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-mybatis](https://github.com/microsphere-projects/microsphere-mybatis) | `rose-mybatis-plus` | ⚠️ 部分能力已有 |
| 拦截器链 / SQL 观测 | 数据权限 + 加密 + 租户 + SQL observation | ✅ |
| 专题子模块拆分 | core + spring + starter | ✅ |
