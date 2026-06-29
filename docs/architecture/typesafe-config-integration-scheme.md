# TypeSafe Config 引入方案
## 一、背景与痛点
Spring Boot原生配置体系非常适合固定结构的静态配置，但在Rose生态的**复杂动态配置场景**下存在明显不足：
### 现有痛点
1. **DevService多实例配置困难**：用户需要配置多个不同类型的DevService容器（比如2个Redis、3个MySQL），Spring原生`@ConfigurationProperties`很难支持动态多实例的灵活配置
2. **多租户差异化配置繁琐**：不同租户的限流规则、数据库配置、功能开关等差异化配置，用Spring原生配置很难实现层级合并、动态加载
3. **动态配置更新不灵活**：DevService配置、多租户规则需要运行时动态更新，Spring原生配置更新成本高
4. **配置引用、合并能力弱**：复杂配置需要大量重复编写，不能像代码一样引用、继承、合并，配置冗余严重

TypeSafe Config（Lightbend Config）是Java生态最成熟的轻量配置库，完美解决以上痛点，同时和Spring配置体系完全兼容，可以作为Spring配置的**有益补充而非替代**。

---

## 二、设计目标
严格遵循Rose「克制、可选、零侵入」的核心原则：
1. **全可选引入**：做成独立的可选starter模块，用户按需引入，不需要完全无感，不影响现有代码
2. **Spring生态完全兼容**：不替换Spring原生配置体系，只做复杂场景的补充，和Spring `@ConfigurationProperties`、`@Value`等能力完全共存，无缝打通
3. **零侵入core**：rose-core完全不需要修改，不感知这个模块的存在
4. **极致轻量**：仅依赖TypeSafe Config本身，体积~300KB，零传递依赖，不会增加额外负担
5. **仅解决Rose特有场景**：只封装DevService、多租户相关的配置增强，不做通用配置能力，不重复造Spring已经有的轮子

---

## 三、核心设计原则
### 🔴 绝对不替代Spring配置体系
Spring原生配置依然是首选，TypeSafe Config仅作为**复杂场景的补充能力**，不强制用户使用，简单配置场景依然推荐用Spring原生方式。
### 🟡 完全兼容无冲突
- 可以和Spring配置双向打通，互相读取对方的配置项
- 配置格式支持HOCON、JSON、Properties，和Spring现有配置格式完全兼容
- 不需要修改任何现有Spring配置代码
### 🟢 场景聚焦
仅支持Rose特有场景的配置增强，不做通用配置能力扩展：
- ✅ DevService多实例、动态配置
- ✅ 多租户差异化配置、动态加载
- ✅ 多环境配置合并、继承
- ❌ 不做Spring已经有的配置绑定、@Value、配置刷新等能力

---

## 四、模块结构
```
└── rose-config-starter                # 可选扩展模块，按需引入，体积~350KB（包含TypeSafe Config本身300KB）
    ├── config                         # 核心配置能力
    │   ├── RoseConfig                 # 统一配置入口，封装TypeSafe Config API
    │   ├── SpringConfigMerger         # 和Spring Environment配置合并工具
    │   └── DynamicConfigLoader        # 动态配置加载/刷新能力
    ├── devservice                     # DevService配置增强
    │   └── DevServiceConfigParser     # 多实例DevService配置自动解析
    └── tenant                         # 多租户配置增强
        └── TenantConfigParser         # 多租户差异化配置自动解析
```

---

## 五、核心能力设计
### 1. DevService多实例配置（解决原生配置痛点）
#### 配置示例（HOCON格式，比YAML更简洁灵活）
```hocon
// application.conf
devservice {
    // 支持配置多个Redis实例，Spring原生很难实现
    redis {
        order-db {
            port = 6379
            password = 123456
            shared = true
        }
        user-db {
            port = 6380
            password = 654321
            shared = false
        }
    }
    // 支持配置多个MySQL实例
    mysql {
        product-db {
            port = 3306
            database = product
        }
        user-db {
            port = 3307
            database = user
        }
    }
}
```
#### 代码读取示例，非常简洁
```java
// 自动解析所有Redis实例配置，不需要手写配置绑定类
List<RedisDevServiceProperties> redisConfigs = 
    DevServiceConfigParser.parseList("devservice.redis", RedisDevServiceProperties.class);

// 自动启动所有配置的Redis容器
List<RedisContainer> containers = redisConfigs.stream()
    .map(config -> DevService.redis(config).start())
    .toList();
```
> 对比Spring原生实现：需要写配置类、List绑定、手动解析，至少减少80%的样板代码。

---

### 2. 多租户差异化配置（自动合并继承）
#### 配置示例
```hocon
tenant {
    // 默认配置，所有租户继承
    default {
        rate-limit = 1000
        database {
            max-pool-size = 20
        }
    }
    // 租户1的个性化配置，自动继承默认配置
    1001 {
        rate-limit = 2000 // 个性化配置，覆盖默认值
        // database配置继承默认的max-pool-size=20，不需要重复写
    }
    // 租户2的个性化配置
    1002 {
        rate-limit = 500
        database {
            max-pool-size = 10 // 个性化覆盖
        }
    }
}
```
#### 代码读取示例
```java
// 自动合并默认配置和租户个性化配置，不需要手动处理
int rateLimit = TenantConfigParser.getInt(tenantId, "rate-limit");
int maxPoolSize = TenantConfigParser.getInt(tenantId, "database.max-pool-size");
```
> 自动处理配置继承、合并，不需要手动写默认值判断逻辑，减少70%的多租户配置样板代码。

---

### 3. 和Spring配置双向打通，完全兼容
#### 方式1：把Spring配置合并到RoseConfig
```java
// Spring的配置项可以直接在RoseConfig中读取
RoseConfig config = RoseConfig.loadWithSpringEnvironment(environment);
String appName = config.getString("spring.application.name"); // 可以读取Spring配置
```
#### 方式2：把RoseConfig的配置项暴露到Spring Environment
```java
// RoseConfig的配置项可以直接用@Value读取，或者注入到@ConfigurationProperties
@Value("${devservice.redis.order-db.port}")
private int redisPort;
```
> 完全兼容Spring现有配置体系，用户可以混用，不需要修改原有代码。

---

### 4. 动态配置加载/刷新
```java
// 运行时动态加载新的配置
RoseConfig dynamicConfig = RoseConfig.load("file:/path/to/new-config.conf");

// 刷新DevService配置
DevServiceConfigParser.refresh(dynamicConfig);

// 刷新多租户配置
TenantConfigParser.refresh(dynamicConfig);
```
> 支持运行时动态更新配置，不需要重启应用，满足DevService、多租户的动态配置需求。

---

## 六、兼容方案
1. **完全无冲突**：TypeSafe Config的配置文件默认加载`application.conf`，和Spring的`application.yml`/`application.properties`完全独立，互不影响
2. **优先级可控**：默认配置优先级 Spring Environment > TypeSafe Config，用户可以自定义优先级
3. **无强制迁移**：现有Spring配置完全不需要修改，可以继续用`@ConfigurationProperties`、`@Value`等原生方式，和TypeSafe Config混用
4. **可选依赖**：模块依赖TypeSafe Config为`<optional>true</optional>`，不会强制传递给用户，用户也可以自己引入指定版本的TypeSafe Config

---

## 七、使用门槛极低
### 引入方式
```xml
<!-- 仅在需要复杂配置的场景引入，不需要不用加 -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-config-starter</artifactId>
</dependency>
```
### 不需要复杂配置
引入依赖后自动生效，不需要额外配置，自动加载`application.conf`文件，自动和Spring配置打通。

---

## 八、收益评估
| 投入 | 收益 |
|------|------|
| 极低：仅一个独立模块，代码量<1000行，开发成本<0.5人周 | 极高：解决DevService多实例、多租户差异化配置的核心痛点，减少80%的复杂配置样板代码，配置更简洁易维护 |

---

## 九、边界说明（非常重要，严格遵循克制原则）
### ✅ 推荐使用场景
1. 需要配置多个DevService实例
2. 有多租户差异化配置、动态配置需求
3. 配置需要继承、合并、引用，减少冗余
### ❌ 不推荐使用场景（依然首选Spring原生配置）
1. 简单固定结构的配置，Spring `@ConfigurationProperties`已经可以很好满足
2. 不需要多实例、多租户差异化配置的简单项目
3. 对HOCON格式有抵触，不想额外学习配置格式

### 🔴 绝对不会做的事情
1. 不会替换Spring的配置体系，永远是补充能力
2. 不会强制用户使用，不需要可以完全不用，零感知
3. 不会修改Spring的配置逻辑，不会影响现有配置的加载
4. 不会做通用配置中心、配置刷新等Spring Cloud已经有的能力，避免重复造轮子
