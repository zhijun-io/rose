# Rose Cache TTL — 设计方案

> **主题目录（规划）：** `rose-cache/`  
> **Artifact（规划）：** `rose-cache-core` + optional `rose-cache-redis` + `rose-cache-spring-boot-starter`  
> **定位：** 在 Spring Cache 之上支持 **按缓存操作（方法）配置 TTL**；可选 Redis per-key 过期。

### 实现状态

| 项 | 状态 |
|----|------|
| 模块 / 代码 | ❌ 仅规格 |

> **关联：** `rose-multitenancy-core`（`TenantKeyGenerator`）、Spring `@Cacheable`

## 如何使用本文档编码

| 步骤 | 章节 | 说明 |
|------|------|------|
| 1 | **§5** | 注解与元数据 |
| 2 | **§6** | CacheInterceptor 扩展 |
| 3 | **§7** | Caffeine / Redis 后端 |
| 4 | **§8–§9** | Boot 自动配置与测试 |

**Phase 1 验收（模块创建后）：**

```bash
mvn -pl rose-cache/rose-cache-core test
```

---

## 1. 背景

Spring Cache + `@Cacheable` **不**支持同一 `cacheNames` 下不同方法使用不同 TTL。常见变通：

- 多个 cache 名 + 多个 `CacheManager` 配置 — 配置膨胀
- Redis `CacheManager` 全局 TTL — 无法 per-entry

microsphere **@TTLCacheable** 在操作级声明 TTL。Rose 在 **有明确痛点** 时引入独立主题 `rose-cache`，与 multitenancy 缓存 key 协作。

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| 注解 TTL | `@TTLCacheable` 或 `@Cacheable` + `@TTL` |
| 本地缓存 | Caffeine `Expiry` 或包装 Cache |
| Redis | per-key `SET EX`（optional 模块） |
| 多租户 | 与 `TenantKeyGenerator` 文档化协作 |
| Java 8 | 与 Rose 一致 |

### 2.2 非目标

- 替代 Spring Data Redis Cache 全部能力
- 多级 cache、Near-cache、分布式一致性
- Phase 1 支持 `@CachePut` / `@CacheEvict` 的复杂 TTL 语义（可 Phase 2）
- 在 `rose-spring-core` 内实现（独立主题）

---

## 3. 模块结构（规划）

```
rose-cache/
├── rose-cache-core/
│   └── io/zhijun/cache/
│       ├── annotation/
│       ├── support/          # TTLCacheOperationSource, TTLCacheInterceptor
│       └── caffeine/
├── rose-cache-redis/         # optional
└── rose-cache-spring-boot-starter/
```

**依赖：**

```
rose-cache-core
  ├── spring-context-support   (Cache abstraction)
  └── caffeine                 (Phase 1 默认后端)

rose-cache-redis
  ├── rose-cache-core
  └── spring-data-redis

rose-cache-spring-boot-starter
  ├── rose-cache-core
  ├── rose-cache-redis         (optional dependency)
  └── spring-boot-starter-cache
```

---

## 4. 实施阶段

| Phase | 内容 |
|-------|------|
| **1** | 注解 + Caffeine TTL + `@EnableTTLCaching` |
| **2** | `rose-cache-redis` per-key EX |
| **3** | multitenancy 文档 + 示例 starter 组合 |

---

## 5. 注解 API

### 5.1 `@EnableTTLCaching`

```java
@Target(TYPE)
@Retention(RUNTIME)
@Import(TTLCachingRegistrar.class)
public @interface EnableTTLCaching {
}
```

导入 `TTLCacheInterceptor`（替换或包装默认 `CacheInterceptor` bean）。

### 5.2 `@TTLCacheable`

**方案 A（推荐）：** 组合 Spring `@Cacheable`

```java
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Cacheable
public @interface TTLCacheable {

    @AliasFor(annotation = Cacheable.class, attribute = "cacheNames")
    String[] cacheNames() default {};

    @AliasFor(annotation = Cacheable.class, attribute = "key")
    String key() default "";

    /** ISO-8601 duration: "300s", "5m", "1h" 或纯秒数字符串 */
    String ttl() default "";

    /** 与 ttl 二选一；>0 时优先 */
    long ttlSeconds() default -1;
}
```

**默认 TTL：** 未指定时使用 `rose.cache.ttl.default`（Boot）或 `TTLCachingProperties.defaultTtl`。

### 5.3 元数据 `TTLCacheOperation`

扩展 Spring `CacheOperation`：

```java
public class TTLCacheOperation extends CacheOperation {
    private final Duration ttl;
    // getter
}
```

由 `TTLCacheOperationSource` 解析 `@TTLCacheable`。

---

## 6. 拦截器

### 6.1 `TTLCacheOperationSource`

继承 `AnnotationCacheOperationSource`，解析 `@TTLCacheable` 并附加 `Duration ttl`。

### 6.2 `TTLCacheInterceptor`

继承 `CacheInterceptor`，在 **cache put** 路径附带 TTL（**不在** cache hit 后改 TTL）：

```
invoke(context):
    operation = getCacheOperation()
    ttl = (operation instanceof TTLCacheOperation) ? ((TTLCacheOperation) operation).getTtl() : null
    if ttl == null:
        return super.invoke(context)
    return invokeWithTtl(context, operation, ttl)

invokeWithTtl(...):
    // 委托 CacheAspectSupport 逻辑，但在 Cache.put(key, value) 时调用 TTLCache.put(key, value, ttl)
    // cache miss 加载新值后 put；@CachePut 同理（Phase 2）
```

**禁止**在 `super.invoke` 返回后对已存在 entry 调用 `setTtl`（避免误延长 hot key 寿命）。

### 6.3 与 `@Cacheable` 的关系

- **仅使用 `@TTLCacheable`**（其自身 `@Cacheable` 元注解）；**不要**在同一方法上再标标准 `@Cacheable`。
- 启动时 `TTLCacheOperationSource` 校验：若检测到重复 Cache 操作定义 → `IllegalStateException`。

### 6.4 Duration 解析（`rose-cache-core`）

Java 8 / 无 Boot 依赖时使用 `io.zhijun.cache.support.DurationParser`：

- `ttlSeconds > 0` → 优先于 `ttl` 字符串
- `ttl` 支持：`300s`、`5m`、`1h`、`1d` 或纯数字秒
- 非法格式 → `@EnableTTLCaching` 处理阶段 `IllegalStateException`

Boot starter 可 **可选** 委托 `DurationStyle`（`@ConditionalOnClass`），core 不依赖 Boot。

## 7. 后端实现

### 7.1 Caffeine（Phase 1）

```java
public final class TTLCaffeineCache implements Cache {

    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> delegate;

    @Override
    public void put(Object key, Object value) {
        put(key, value, defaultTtl);
    }

    public void put(Object key, Object value, Duration ttl) {
        delegate.policy().expireVariably().ifPresent(expiry ->
            expiry.put(key, value, ttl.toNanos(), TimeUnit.NANOSECONDS));
    }
}
```

`TTLCaffeineCacheManager` 创建 named caches；**每个 cache region 仍一个 Caffeine 实例**，variably expiry 支持 per-entry TTL。

### 7.2 Redis（Phase 2，`rose-cache-redis`）

```java
public final class TTLRedisCache implements Cache {

    @Override
    public void put(Object key, Object value) {
        put(key, value, defaultTtl);
    }

    public void put(Object key, Object value, Duration ttl) {
        byte[] k = serializeKey(key);
        byte[] v = serializeValue(value);
        connection.set(k, v, Expiration.from(ttl), RedisStringCommands.SetOption.UPSERT);
    }
}
```

**不**修改全局 `RedisCacheConfiguration.entryTtl()`；每次 put 使用 operation 级 TTL。

### 7.3 多租户

使用 Spring Cache `KeyGenerator`：

```properties
# 应用配置
spring.cache.redis.key-prefix=   # 若用 Redis
```

Rose 文档约定：

```
最终 key = TenantKeyGenerator.generate(...)  // 已有 multitenancy
TTL 仅影响过期时间，不改变 key 结构
```

**不**在 `rose-cache-core` compile 依赖 multitenancy；示例在 starter 文档。

---

## 8. Boot 自动配置

### 8.1 配置前缀 `rose.cache`

| 属性 | 默认 | 说明 |
|------|------|------|
| `rose.cache.ttl.default` | `10m` | 未指定 ttl 的操作 |
| `rose.cache.caffeine.spec` | — | 透传 Caffeine spec（max size 等） |
| `rose.cache.redis.enabled` | `false` | 启用 redis 模块 |

### 8.2 AutoConfiguration

**与 Boot `CacheAutoConfiguration`：** `@AutoConfigureAfter(CacheAutoConfiguration.class)` + `@Primary` 仅作用于 `CacheInterceptor` bean；**不**替换用户自定义 `CacheManager`（`@ConditionalOnMissingBean(CacheManager.class)` 仅在没有 manager 时提供 TTL Caffeine manager）。

```java
@AutoConfiguration(after = CacheAutoConfiguration.class)
@ConditionalOnClass(CacheInterceptor.class)
@EnableConfigurationProperties(TTLCachingProperties.class)
public class RoseTTLCacheAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public TTLCacheInterceptor ttlCacheInterceptor() { ... }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager ttlCaffeineCacheManager(TTLCachingProperties props) { ... }
}
```

---

## 9. 测试矩阵

| # | 测试类 | 场景 |
|---|--------|------|
| 1 | `TTLCacheOperationSourceTest` | 解析 ttl / ttlSeconds |
| 2 | `TTLCaffeineCacheTest` | put 后 TTL 过期 get miss |
| 3 | `TTLCacheInterceptorTest` | 不同方法不同 TTL 同 cache name |
| 4 | `TTLRedisCacheTest` | Phase 2，EX 秒级 |
| 5 | 集成 | 同一方法 `@Cacheable` + `@TTLCacheable` 冲突检测 |

---

## 10. 实现检查清单

- [ ] 独立 `rose-cache` 主题 + BOM
- [ ] core **不**依赖 Boot starter
- [ ] Phase 1 Caffeine 无 Redis 硬依赖
- [ ] 与 multitenancy 仅文档协作，无 compile 耦合
- [ ] 说明何时用「多 cache 名配置」即可、不必引入本模块

---

## 11. 开工清单

1. 确认消费场景（同 cache 名 heterogeneous TTL）
2. 分支：`feature/rose-cache`
3. Phase 1：`rose-cache-core` + starter
4. Phase 2：`rose-cache-redis` + IT（Testcontainers Redis）
