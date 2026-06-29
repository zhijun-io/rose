# Rose 项目 Vavr 扩展方案指南

## 核心结论
完全可以扩展，且 **非常有必要做针对性的轻量化扩展**。所有扩展必须紧扣Rose项目「提升企业级开发效率、简化样板代码」的核心定位，只做和现有核心能力深度结合的场景，不做通用函数式编程框架，避免为了技术而技术。

---

## 一、为什么适合在Rose中扩展Vavr能力？
### 1. 适配成本极低，完全非侵入
- Vavr本身是纯JDK实现，无其他依赖，核心包仅 ~700KB，不会显著增加Rose的体积
- 可以做成**可选的独立starter模块**（`rose-vavr-starter`），用户按需引入，完全不影响Rose核心的轻量化定位
- 现有代码完全兼容，不需要任何改造，用户可以渐进式使用
- 和Spring生态无缝集成，Vavr的类型可以直接被Spring序列化、参数解析，不需要额外适配

### 2. 能切实解决Rose用户的高频痛点
Vavr的特性刚好可以解决很多Spring Boot开发中的常见痛点，和Rose的核心能力结合会产生1+1>2的效果，而不是单纯的技术炫技。

---

## 二、高价值、值得优先扩展的场景（和Rose核心能力深度结合）
### 1. DevService 容器化场景：简化容器生命周期管理
#### 痛点
当前处理容器启动/关闭需要写很多try-catch、判空、懒加载的样板代码，容易出错。

#### 适配方案
```java
// 封装后DevService的使用体验
Try<RedisContainer> redisTry = DevService.redis()
    .withPort(6379)
    .withPassword("123456")
    .tryStart(); // 用Try封装启动可能的异常

// 链式处理结果
String redisUrl = redisTry
    .map(container -> "redis://" + container.getHost() + ":" + container.getMappedPort())
    .recover(e -> "redis://localhost:6379") // 启动失败降级用本地
    .get();

// 懒加载容器，第一次调用才会启动
Lazy<RedisContainer> lazyRedis = DevService.redis().lazyStart();
RedisContainer container = lazyRedis.get(); // 仅第一次调用会启动
```

#### 价值
简化DevService的异常处理、懒加载逻辑，减少样板代码，提升开发体验。

---

### 2. 多租户场景：解决异步上下文传递痛点
#### 痛点
异步/线程池场景下租户ID容易丢失，需要手动传递，非常繁琐。

#### 适配方案
基于Vavr的函数式接口封装自动传递租户上下文的能力：
```java
// 封装后的函数式接口，自动携带当前租户ID
TenantFunction<Long, User> getUserFunc = TenantFunction.wrap(
    (id, tenantId) -> userMapper.selectByIdAndTenantId(id, tenantId)
);

// 异步场景下自动传递租户上下文，不需要手动设置
CompletableFuture<User> future = CompletableFuture.supplyAsync(
    () -> getUserFunc.apply(1L) // 自动携带当前调用线程的租户ID
);

// Runnable 版本
TenantRunnable task = TenantRunnable.wrap(() -> {
    // 内部自动获取租户上下文
    System.out.println(TenantContext.getRequiredTenantId());
});
threadPool.execute(task);
```

#### 价值
彻底解决异步场景下租户上下文丢失的问题，不需要用户手动传递，减少bug。

---

### 3. 数据层场景：简化空指针、异常处理
#### 痛点
数据库查询经常返回null，需要写很多判空逻辑；DAO层异常处理繁琐。

#### 适配方案
封装MyBatis-Plus扩展，自动返回Option类型：
```java
// 扩展后的BaseMapper，自动用Option包装返回值
Option<User> userOpt = userMapper.selectByIdOpt(1L);

// 链式处理，不需要判空
String username = userOpt
    .map(User::getUsername)
    .filter(name -> name.length() > 2)
    .getOrElse("默认用户");

// 用Try封装DAO层异常，统一转换
Try<List<User>> usersTry = Try.of(() -> userMapper.listByTenantId(tenantId))
    .mapFailure(SQLException.class, e -> new BusinessException("数据库查询失败", e));
```

#### 价值
消灭80%的空指针判断，统一异常处理逻辑，代码更简洁。

---

### 4. 接口层场景：统一结果处理
#### 痛点
接口层需要写很多if判断处理错误情况，返回格式不统一。

#### 适配方案
用Either封装统一返回结果，自动适配Spring MVC序列化：
```java
// 接口层直接返回Either，自动序列化
@GetMapping("/user/{id}")
public Either<ErrorInfo, User> getUser(@PathVariable Long id) {
    return userService.getUser(id)
        .toEither(() -> ErrorInfo.of("USER_NOT_FOUND", "用户不存在"));
}

// 全局异常处理自动转换为Either
@ExceptionHandler(Exception.class)
public Either<ErrorInfo, Void> handleException(Exception e) {
    return Either.left(ErrorInfo.of("SYSTEM_ERROR", e.getMessage()));
}
```

#### 价值
统一接口返回格式，减少样板代码，错误处理更优雅。

---

### 5. 流式处理场景：简化多租户/权限过滤逻辑
#### 痛点
多租户数据过滤、权限过滤需要写很多循环判断，代码冗长。

#### 适配方案
增强集合处理能力，链式处理多租户数据：
```java
// 过滤租户数据 + 权限校验 + 转换DTO，链式完成
List<UserDTO> userDTOs = Stream.ofAll(users)
    .filter(user -> tenantId.equals(user.getTenantId())) // 租户过滤
    .filter(user -> permissionService.hasAccess(user, currentUser)) // 权限校验
    .map(UserDTO::from) // 转换DTO
    .toJavaList();
```

#### 价值
流式处理代码更简洁，可读性更高。

---

## 三、这些场景没必要扩展，避免过度设计
### 1. 不需要替换JDK原生API
- 不要强制用户改用Vavr的不可变集合，JDK集合已经满足90%的场景，Vavr集合作为可选能力提供即可
- 不需要重复实现JDK已经有的函数式接口，比如Function、Supplier这些直接用JDK原生的就行

### 2. 不需要做完整的函数式编程支持
- Rose是业务开发框架，不是函数式编程语言，不需要提供模式匹配、高级类型系统这些复杂特性
- 只封装高频使用的核心能力（Option、Try、Lazy、上下文传递），不需要把Vavr的所有API都暴露给用户

### 3. 不要改变用户的编程习惯
- 所有函数式能力都是**可选增强**，不影响用户用传统命令式风格写代码
- 不要强制用户学习函数式编程概念，封装的API要尽量符合普通Java开发者的习惯

---

## 四、最佳扩展方式：轻量化、非侵入、可选
### 1. 独立模块，可选引入
单独做`rose-vavr-starter`模块，不集成到rose核心中，用户需要用才引入，完全不影响核心的轻量化。

### 2. 上层封装，隐藏依赖
对外暴露Rose自己的API，Vavr作为内部实现，用户甚至不需要知道Vavr的存在：
```java
// 对外暴露RoseOption，内部用Vavr Option实现
public final class RoseOption<T> {
    private final Option<T> delegate;

    public static <T> RoseOption<T> of(T value) {
        return new RoseOption<>(Option.of(value));
    }

    public T getOrElse(T defaultValue) {
        return delegate.getOrElse(defaultValue);
    }
    // 其他简化API
}
```

### 3. 渐进式扩展，小步快跑
优先实现最高频的3个能力：
1. 多租户上下文自动传递的函数式接口
2. DevService的Try/Lazy封装
3. 数据层的Option返回封装

后续根据用户反馈再逐步扩展其他能力，不要一上来就做全套。

---

## 五、投入产出分析
| 投入 | 收益 | 风险 |
|------|------|------|
| 极低：Vavr本身成熟稳定，只需要做简单封装，开发成本<1人周 | 极高：可以减少30%~50%的样板代码，降低空指针、上下文丢失等常见bug，提升开发效率 | 极低：完全可选，不影响现有用户，不需要可以随时移除 |

---

## 最终建议
**非常值得扩展**，但要控制范围，只做和Rose核心能力（DevService、多租户、数据层增强）紧密结合的场景，做成可选的starter模块，保持Rose核心的轻量化。不需要做通用函数式框架，只解决用户的实际痛点即可。

这样既可以享受到Vavr带来的开发效率提升，又不会增加Rose的复杂度，符合Rose「提升开发效率、轻量化、开箱即用」的定位。
