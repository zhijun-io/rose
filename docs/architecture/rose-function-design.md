# Rose Function 模块设计方案
## 一、设计目标
`rose-function` 是Rose生态的**可选函数式能力增强层**，绝对不侵入rose-core，也不做通用函数式编程框架，核心目标只有三个：
1. **解决Rose特有场景痛点**：封装多租户上下文自动传递、DevService容器生命周期函数式处理等Rose专属场景的样板代码
2. **保持极致轻量**：核心API模块零三方依赖，可选实现模块最小仅20KB，最大不超过700KB
3. **全可选、零侵入**：用户按需引入，完全不影响现有代码，不需要改变编程习惯，可渐进式使用
4. **API统一兼容**：底层支持jOOL、Vavr、自研极简三种实现，对外API完全一致，用户可无缝切换

---

## 二、核心设计原则
### 🔴 绝对红线：不侵入rose-core
rose-core不需要做任何修改，完全不感知rose-function的存在，保持核心的纯净性。
### 🟡 功能边界：只做Rose场景绑定，不做通用功能
- ✅ 只做多租户、DevService、数据层增强等Rose特有场景的函数式封装
- ❌ 不做通用函数式工具类、不可变集合、模式匹配等通用功能，优先复用成熟开源实现
### 🟢 能力分层：用户按需选择，零强制
- 提供不同重量的实现方案，用户根据自己的场景选择，不需要的能力完全不会引入
- 所有API和JDK标准函数式接口对齐，不需要学习新的概念

---

## 三、模块结构
```
├── rose-function                          # 核心API模块，【零三方依赖】，体积<50KB
│   ├── base                               # 基础函数式接口
│   │   ├── RoseOption<T>                  # 空值处理接口
│   │   ├── RoseTry<T>                     # 异常处理接口
│   │   └── RoseLazy<T>                    # 惰性求值接口
│   ├── tenant                             # 多租户专属函数式接口
│   │   ├── TenantFunction<T,R>            # 自动传递租户上下文的Function
│   │   ├── TenantRunnable                 # 自动传递租户上下文的Runnable
│   │   └── TenantSupplier<T>              # 自动传递租户上下文的Supplier
│   └── devservice                         # DevService专属函数式工具
│       ├── DevServiceTry<T>               # 容器生命周期异常处理
│       └── DevServiceLazy<T>              # 容器懒加载封装
│
├── rose-function-jool-starter             # 【轻量实现，90%用户首选】，基于jOOL封装，额外体积~200KB
├── rose-function-vavr-starter             # 【完整实现，重度函数式用户选】，基于Vavr封装，额外体积~700KB
└── rose-function-simple-starter           # 【极简实现，极端场景选】，自研零依赖实现，总代码量<500行，体积<20KB
```

---

## 四、核心API设计（仅展示和Rose场景绑定的核心能力）
### 1. 多租户上下文自动传递（解决异步场景租户丢失痛点）
```java
// 自动捕获当前线程的租户ID，异步执行时自动传递，不需要手动设置
TenantFunction<Long, User> getUserFunc = TenantFunction.wrap(
    (id, tenantId) -> userMapper.selectByIdAndTenantId(id, tenantId)
);

// 异步场景直接用，不需要手动传递租户上下文
CompletableFuture<User> future = CompletableFuture.supplyAsync(
    () -> getUserFunc.apply(1L) // 自动携带当前租户ID，不会丢失
);

// Runnable版本，用于异步任务
TenantRunnable task = TenantRunnable.wrap(() -> {
    // 内部自动获取租户上下文
    String tenantId = TenantContext.getRequiredTenantId();
});
threadPool.execute(task);
```
> 减少100%手动传递租户上下文的样板代码，彻底解决异步场景租户丢失问题。

---

### 2. DevService容器生命周期函数式封装（简化异常/懒加载处理）
```java
// 链式处理容器启动异常，不需要写try-catch
Try<RedisContainer> redisTry = DevService.redis()
    .withPort(6379)
    .withPassword("123456")
    .tryStart();

// 启动失败自动降级，不需要手动判空
String redisUrl = redisTry
    .map(container -> "redis://" + container.getHost() + ":" + container.getMappedPort())
    .recover(e -> "redis://localhost:6379") // 启动失败用本地地址
    .get();

// 懒加载容器，第一次调用才会启动，避免不必要的资源消耗
Lazy<RedisContainer> lazyRedis = DevService.redis().lazyStart();
RedisContainer container = lazyRedis.get(); // 仅第一次调用会启动容器
```
> 减少70%的DevService异常处理、懒加载样板代码，代码更简洁易读。

---

### 3. 数据层空值/异常统一处理（消灭空指针）
```java
// 扩展MyBatis-Plus，自动用RoseOption包装返回值，不需要手动判空
RoseOption<User> userOpt = userMapper.selectByIdOpt(1L);

// 链式处理，不需要写if (user == null)判断
String username = userOpt
    .map(User::getUsername)
    .filter(name -> name.length() > 2)
    .getOrElse("默认用户");

// 统一处理DAO层异常，不需要写try-catch
RoseTry<List<User>> usersTry = RoseTry.of(() -> userMapper.listByTenantId(tenantId))
    .mapFailure(SQLException.class, e -> new BusinessException("数据库查询失败", e));
```
> 消灭80%的空指针判断和DAO层异常处理样板代码。

---

## 五、实现方案
三个实现模块对外API完全一致，用户可以无缝切换，不需要修改业务代码：
| 实现模块 | 依赖 | 体积 | 能力范围 | 适用场景 |
|---------|------|------|----------|----------|
| rose-function-jool-starter | jOOL | ~200KB | 基础Option/Try/Lazy + 多租户/DevService封装 + 流增强、Tuple元组 | 90%的普通业务项目，需要轻量函数式能力 |
| rose-function-vavr-starter | Vavr | ~700KB | 完整函数式能力：不可变集合、模式匹配、高级函数式能力 + 所有jOOL版本的能力 | 重度函数式编程的项目，需要高级能力 |
| rose-function-simple-starter | 无 | ~20KB | 仅核心Option/Try/Lazy + 多租户/DevService封装，无额外功能 | 对体积极度敏感的嵌入式/客户端SDK场景 |

### 兼容设计
所有实现都实现rose-function模块定义的统一接口，对外暴露的API完全一样，用户只需要切换依赖，不需要修改业务代码。
```xml
<!-- 切换成轻量jOOL实现 -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-function-jool-starter</artifactId>
</dependency>

<!-- 切换成Vavr实现，业务代码不需要改 -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-function-vavr-starter</artifactId>
</dependency>
```

---

## 六、迁移成本
- **零侵入**：不需要修改任何现有代码，不影响原有功能
- **零学习成本**：API完全对齐JDK函数式接口和常用用法，不需要学习新概念
- **渐进式使用**：可以只在需要的场景使用，不需要全量改造
- **无依赖冲突**：所有实现都做了可选依赖处理，不会和用户项目中的jOOL/Vavr冲突

---

## 七、收益评估
| 投入 | 收益 |
|------|------|
| 极低：API模块<1000行代码，每个实现模块<500行代码，开发成本<1人周 | 极高：减少30%~50%的多租户、DevService、数据层样板代码，彻底解决异步租户丢失、空指针等常见bug，提升开发效率 |

---

## 八、边界说明
1. 不会替代JDK原生函数式接口，Function/Supplier等原生接口依然优先使用
2. 不会实现不可变集合、模式匹配等通用函数式能力，这些能力由底层jOOL/Vavr提供，rose-function只做封装
3. 不会强制用户使用，所有能力都是可选增强，用户可以继续用传统方式写代码
