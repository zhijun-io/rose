# Rose MyBatis Plus

MyBatis-Plus 扩展：审计字段、字段加密、数据权限、租户集成。

## 迭代导航

### 子模块

| 模块 | Artifact | 说明 |
|------|----------|------|
| Core | `rose-mybatis-plus-core` | 能力库与自动配置 |
| Starter | `rose-mybatis-plus-spring-boot-starter` | 应用入口 |

### 已实现

| 能力 | 说明 |
|------|------|
| 审计 | `AuditMetaObjectHandler`、`AuditableEntity` 自动填充 |
| 字段加密 | `@FieldEncrypt`、`FieldEncryptInterceptor` |
| 数据权限 | `@DataPermission`、`RoseDataPermissionHandler` |
| 租户 | `TenantIdSupplier` + `rose-multitenancy` 自动配置集成 |

### 未实现 / 规划中

- 配置属性表与完整使用示例
- SQL 执行 metrics / tracing（microsphere-mybatis 方向）
- 加密算法可插拔与密钥轮换文档
- 数据权限规则链 / SPI 文档化（extension pipeline）
- 独立 design doc

### 对标 Arconia

无 MyBatis 专题。Rose 面向国内常见的 MyBatis-Plus 栈。

### 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-mybatis](https://github.com/microsphere-projects/microsphere-mybatis) | `rose-mybatis-plus` | ⚠️ 部分能力已有 |
| 拦截器链 / SQL 观测 | 数据权限 + 加密拦截器 | ⚠️ 无 metrics/tracing |
| 专题子模块拆分 | core + starter | ✅ |

### 建议下一步

1. **高**：补 README 配置示例与 `@DataPermission` 规则写法
2. **中**：评估 SQL observation 是否与 `rose-opentelemetry` 协作
3. **低**：参考 microsphere-mybatis 整理 interceptor 扩展点文档
