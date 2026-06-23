# Rose Spring Web Handler — 实现规格

> **主题目录（规划）：** `rose-spring-web/`  
> **Artifact（规划）：** `rose-spring-web-core` + optional `rose-spring-web-spring-boot-starter`  
> **逻辑子域：** **endpoint-registry**（启动期端点元数据） + **handler-interceptor**（Controller 方法拦截 SPI）  
> **基线：** Java 8、Spring Boot 2.7；**Phase 1 仅 Servlet MVC**，WebFlux optional Phase 2  

### 实现状态

| 项 | 状态 |
|----|------|
| 模块 / 代码 | ❌ 仅规格 |
| Phase 1 MVC | 规划 |

> **关联：** [bootstrap-diagnostics §7](./rose-spring-boot-bootstrap-diagnostics-design.md#7-启动顺序与-initializer-协作)、`rose-multitenancy-web`、`rose-opentelemetry`

## 如何使用本文档编码

| 步骤 | 章节 | 说明 |
|------|------|------|
| 1 | **§5** | endpoint-registry 子域 |
| 2 | **§6** | handler-interceptor 子域 |
| 3 | **§7** | Boot 自动配置与 Actuator |
| 4 | **§8–§9** | 测试与检查清单 |

**Phase 1 验收（模块创建后）：**

```bash
mvn -pl rose-spring-web/rose-spring-web-core test
```

---

## 1. 背景

借鉴 microsphere-spring 两类能力：

| 能力 | 价值 |
|------|------|
| **Web endpoint registry** | 启动时聚合 URL 映射 + Rose 元数据（租户、观测 route），供诊断与文档 |
| **Handler method interception** | 在 `HandlerMethod` 调用链上统一 SPI，补 Filter/AOP 未覆盖的参数绑定前后钩子 |

Rose **已有** Servlet Filter 级多租户与 OTel；**缺少** Controller 方法级可插拔链路与 enriched 端点目录。

---

## 2. 目标与非目标

### 2.1 目标

| 目标 | 说明 |
|------|------|
| MVC 端点注册表 | 不可变快照，`ContextRefreshedEvent` 后可用 |
| Rose metadata | 租户要求、OTel route 模板等扩展槽 |
| HandlerMethod SPI | `spring.factories` 插件，方法 invoke / 参数 resolve 前后 |
| 与 multitenancy 协作 | 可选内置 `TenantHandlerMethodInterceptor` |
| Java 8 | 与 Rose 一致 |

### 2.2 非目标

- 替代 Spring Boot Actuator `/actuator/mappings`（**增强**而非替换）
- 动态路由 / API Gateway 下发
- Phase 1 支持 WebFlux（optional 模块 Phase 2）
- 替代 `@Aspect` 处理纯业务横切

---

## 3. 模块结构（规划）

```
rose-spring-web/
├── rose-spring-web-core/              # 无 spring-boot-starter-web compile
│   └── io/zhijun/spring/web/
│       ├── registry/
│       └── handler/
└── rose-spring-web-spring-boot-starter/   # optional Actuator endpoint
```

**依赖：**

```
rose-spring-web-core
  ├── rose-spring-core          (SpringFactoriesLoaderUtils)
  ├── spring-web                (provided / optional for pure API)
  └── spring-webmvc             (optional — Phase 1 collector 需要)

rose-spring-web-spring-boot-starter
  ├── rose-spring-web-core
  ├── rose-spring-boot-starter
  └── spring-boot-starter-web
```

**WebFlux（Phase 2）：** `rose-spring-webflux-core`，optional 依赖 `spring-webflux`。

---

## 4. 实施阶段

| Phase | 内容 |
|-------|------|
| **1a** | registry：MVC collector + 内存 Registry |
| **1b** | handler：MVC `HandlerMethodInterceptorChain` + SPI |
| **1c** | starter：Actuator `RoseWebEndpointsEndpoint`（optional） |
| **2** | WebFlux collector + reactive handler SPI |
| **3** | 与 multitenancy / OTel 内置 interceptor 合并 PR |

---

## 5. 子域 A：endpoint-registry

### 5.1 模型

```java
package io.zhijun.spring.web.registry;

public final class RoseWebEndpointDescriptor {

    public enum Source { SERVLET_MVC, SERVLET, WEBFLUX }

    private final String pattern;           // e.g. /api/users/{id}
    private final HttpMethod httpMethod;    // 可为 null（类级映射）
    private final String handlerMethod;     // com.example.UserController#getUser
    private final Source source;
    private final Map<String, String> metadata;

    // getters, immutable
}
```

**metadata 键（Rose 约定）：**

| Key | 说明 | 写入方 |
|-----|------|--------|
| `rose.tenant.required` | `true` / `false` | multitenancy path matcher |
| `rose.observation.route` | OTel `http.route` 模板 | collector 或 `@RoseEndpoint` |
| `rose.module` | 逻辑模块名 | 可选注解 |

### 5.2 `@RoseEndpoint`（可选）

```java
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface RoseEndpoint {
    String module() default "";
    boolean tenantRequired() default false;
}
```

Collector 读取注解写入 metadata。

### 5.3 `WebEndpointRegistry`

```java
public interface WebEndpointRegistry {
    List<RoseWebEndpointDescriptor> getEndpoints();
    Optional<RoseWebEndpointDescriptor> find(String pattern, HttpMethod method);
}
```

实现类 `DefaultWebEndpointRegistry`：**构建后不可变**（`Collections.unmodifiableList`）。

### 5.4 MVC Collector

```java
public final class SpringMvcWebEndpointCollector implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        if (!(ctx instanceof WebApplicationContext)) {
            return;
        }
        RequestMappingHandlerMapping mapping = ctx.getBean(RequestMappingHandlerMapping.class);
        // 遍历 RequestMappingInfo + HandlerMethod → 构建 descriptors
    }
}
```

**规则：**

- 每个 `RequestMappingInfo` × HTTP method 一条 descriptor（无 method 则 method=null）
- `handlerMethod` = `handlerMethod.getBeanType().getName() + "#" + method.getName()`
- pattern 取 `patternsCondition` 最佳匹配（Spring 5.3 API）

**Parent / Child Context：**

- 仅处理 `event.getApplicationContext().getParent() == null` 的 **root** WebApplicationContext。
- 子 context 的 `ContextRefreshedEvent` **忽略**（避免 actuator / 管理端口重复注册）。
- 若需合并子 context 端点，Phase 2 再议。

### 5.5 注册 Bean

```java
@Configuration
@ConditionalOnClass(RequestMappingHandlerMapping.class)
public class WebEndpointRegistryConfiguration {

    @Bean
    public SpringMvcWebEndpointCollector springMvcWebEndpointCollector() { ... }

    @Bean
    public WebEndpointRegistry webEndpointRegistry() {
        return new DefaultWebEndpointRegistry();
    }
}
```

Collector 构建完成后调用 `registry.replaceAll(descriptors)`（package-private）。

### 5.6 与 Actuator mappings 的关系

| | Boot `/actuator/mappings` | Rose Registry |
|--|---------------------------|---------------|
| 目的 | 通用诊断 | Rose metadata + 下游集成 |
| 依赖 | Actuator | `rose-spring-web-core` |
| 关系 | **并存** | starter 可额外暴露 `rose-web-endpoints` |

---

## 6. 子域 B：handler-interceptor

### 6.1 SPI

```java
package io.zhijun.spring.web.handler;

public interface HandlerMethodInterceptor {

    /** 返回 false 则中断 invoke（抛出 AbortHandlerMethodException 或返回 403，实现方定） */
    default boolean preHandle(HandlerMethod handlerMethod, Object[] args) {
        return true;
    }

    default void postHandle(HandlerMethod handlerMethod, Object returnValue) {}

    default void afterCompletion(HandlerMethod handlerMethod, Exception ex) {}
}

public interface HandlerMethodArgumentInterceptor {

    default void beforeResolve(MethodParameter parameter, NativeWebRequest request) {}

    default void afterResolve(MethodParameter parameter, Object resolvedValue) {}
}
```

**加载：**

```properties
io.zhijun.spring.web.handler.HandlerMethodInterceptor=
io.zhijun.spring.web.handler.HandlerMethodArgumentInterceptor=
```

使用 `SpringFactoriesLoaderUtils.loadFactories`；加载后按 `AnnotationAwareOrderComparator` 排序。

**Ordered：** 实现 `org.springframework.core.Ordered` 或 `@Order`；数值越小越先执行。

### 6.2 调用链（Phase 1 方案）

Filter / `HandlerInterceptor` **无法** 在参数解析前后插入 `HandlerMethod` 级钩子。Phase 1 采用 **`BeanPostProcessor` 装饰 `RequestMappingHandlerAdapter`**（不替换 `@Primary` bean）：

```
RoseHandlerMethodAdapterPostProcessor
    postProcessAfterInitialization(bean, name):
        if bean instanceof RequestMappingHandlerAdapter:
            替换 getCustomInvocableHandlerMethod 返回 RoseInvocableHandlerMethod
```

- **唯一 BPP**：`rose-spring-web-core` 内仅一个 PostProcessor；第三方不得再包装同一 adapter（文档约定）。
- **顺序**：SPI 拦截器按 `AnnotationAwareOrderComparator` 排序（实现 `Ordered` 或 `@Order`）。

```
RoseInvocableHandlerMethod.invokeForRequest(...)
    HandlerMethodInterceptorChain: preHandle → super.invoke → postHandle
    on exception → afterCompletion
```

**`preHandle` 返回 false：** 抛出 `HandlerMethodAbortedException`；`RoseInvocableHandlerMethod` 捕获后由 `@ControllerAdvice` 或默认 **403 Forbidden**（`rose.web.handler.abort-status`，默认 403）。

**Argument 链：** 自定义 `HandlerMethodArgumentResolverComposite` 装饰器，在每个 delegate resolve 前后调用 `HandlerMethodArgumentInterceptor`。

### 6.3 `HandlerMethodInterceptorChain`

```java
public final class HandlerMethodInterceptorChain {

    private final List<HandlerMethodInterceptor> interceptors;

    public boolean applyPreHandle(HandlerMethod method, Object[] args) {
        for (HandlerMethodInterceptor i : interceptors) {
            if (!i.preHandle(method, args)) {
                return false;
            }
        }
        return true;
    }
    // postHandle / afterCompletion 同理
}
```

### 6.4 内置实现（Phase 3，optional）

| 类 | 模块 | 作用 |
|----|------|------|
| `TenantHandlerMethodInterceptor` | rose-multitenancy-web | 方法级 `@TenantRequired` |
| `ObservationHandlerMethodInterceptor` | rose-opentelemetry | 补 method attribute（若 Filter 不足） |

**不在 web-core 硬依赖 multitenancy** — 通过 SPI 注册。

### 6.5 与现有组件边界

| 机制 | 层次 | 用途 |
|------|------|------|
| `Filter` / `HandlerInterceptor` | HTTP | 租户解析、Trace 入口 |
| `HandlerMethodArgumentResolver` | 参数 | `@TenantId` 等 |
| **本 SPI** | Controller 方法 | 框架插件、方法级策略 |
| `@Aspect` | 业务 | 业务横切 |

---

## 7. Boot 自动配置

### 7.1 `rose-spring-web-spring-boot-starter`

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.zhijun.spring.web.autoconfigure.RoseSpringWebAutoConfiguration
```

**条件：**

- `@ConditionalOnClass(RequestMappingHandlerMapping.class)`
- `@ConditionalOnWebApplication(SERVLET)`

### 7.2 Actuator Endpoint（optional）

```java
@Endpoint(id = "rose-web-endpoints")
public class RoseWebEndpointsEndpoint {
    @ReadOperation
    public List<RoseWebEndpointDescriptor> endpoints() { ... }
}
```

配置：`management.endpoint.rose-web-endpoints.enabled`（默认 false，opt-in）。

---

## 8. 测试矩阵

| # | 测试类 | 场景 |
|---|--------|------|
| 1 | `SpringMvcWebEndpointCollectorTest` | `@GetMapping` 收集 pattern |
| 2 | 同上 | `@RoseEndpoint` metadata |
| 3 | `DefaultWebEndpointRegistryTest` | 不可变快照 |
| 4 | `HandlerMethodInterceptorChainTest` | preHandle false 中断 |
| 5 | `HandlerMethodArgumentInterceptorTest` | before/after resolve |
| 6 | `@WebMvcTest` 集成 | SPI 从 spring.factories 加载 |

---

## 9. 实现检查清单

- [ ] `rose-spring-web/` 主题目录 + BOM 条目
- [ ] Phase 1 仅 MVC；无 WebFlux 硬依赖
- [ ] Registry 不 duplicate Actuator mappings JSON 结构
- [ ] SPI 与 multitenancy **解耦**（SPI 注册 optional 实现）
- [ ] 模块分层与依赖方向符合根 `README.md` Reference 约定

---

## 10. 开工清单

1. 分支：`feature/rose-spring-web`
2. 脚手架 `rose-spring-web-core` + 测试
3. Phase 1a registry → 1b interceptor → 1c starter
4. 文档：本规格 + 主题 README
