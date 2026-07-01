# MicroSphere 功能审计 — 与 Rose 对比

> 基于 [microsphere-projects/microsphere-spring](https://github.com/microsphere-projects/microsphere-spring) 和 [microsphere-spring-boot](https://github.com/microsphere-projects/microsphere-spring-boot) 分析  
> 日期：2026-07-01（最后更新：2026-07-01）

---

## 一、microsphere-spring-context（核心 Spring 扩展）

### 已实现 ✅

| 功能 | Rose 位置 | 说明 |
|------|-----------|------|
| `AnnotatedInjectionBeanPostProcessor` | `beans/factory/` | 自定义注解注入 |
| `InjectionPointDependencyResolver` 体系 | `beans/factory/` | 注入点依赖解析 SPI（Construction/Autowired/Resource/BeanMethod） |
| `EnableConfigurationBeanBinding` | `beans/factory/annotation/` | 配置 Bean 绑定 |
| `ConfigurationBeanBindingPostProcessor` | `beans/factory/` | 配置 Bean 后处理 |
| `DelegatingFactoryBean` | `beans/factory/` | 委托工厂 Bean |
| `BeanRegistrar` | `beans/factory/` | Bean 定义注册工具 |
| `ListenableAutowireCandidateResolver` | `beans/factory/` | 可监听自动装配候选解析器 |
| `AutowireCandidateResolvingListener` | `beans/factory/` | 自动装配监听器 |
| `BeanDependencyResolver` / `DefaultBeanDependencyResolver` | `beans/factory/` | Bean 依赖解析器 |
| `Dependency` / `DependencyTreeWalker` | `beans/factory/` | 依赖树模型 |
| `BeanSource` | `beans/` | Bean 来源枚举（BeanFactory/SpringFactories/Java SPI） |
| `GenericBeanNameGenerator` | `beans/` | 通用 Bean 名称生成器 |
| `ConfigurationBeanAliasGenerator` 体系 | `beans/factory/` | 配置 Bean 别名生成器 |
| `EnableEventExtension` + `EventExtensionRegistrar` | `context/event/` | 事件扩展注册系统 |
| `InterceptingApplicationEventMulticaster` | `context/event/` | 拦截式事件广播器 |
| `InterceptingApplicationEventMulticasterProxy` | `context/event/` | 代理式事件广播器 |
| `ApplicationEventInterceptor` / `ApplicationListenerInterceptor` | `context/event/` | 事件/监听器拦截链 |
| `BeanFactoryListener` / `BeanListener` | `context/event/` | Bean 工厂/Bean 生命周期监听器 |
| `DependencyAnalysisBeanFactoryListener` | `context/event/` | 依赖分析 |
| `ParallelPreInstantiationSingletonsBeanFactoryListener` | `context/event/` | 并行预实例化 |
| `EventPublishingBeanBefore/AfterProcessor` | `context/event/` | Bean 生命周期事件发布 |
| `EventPublishingBeanInitializer` | `context/event/` | 事件发布初始化器 |
| `JavaBeansPropertyChangeListenerAdapter` | `context/event/` | JavaBeans 属性变更适配器 |
| `GenericApplicationListenerAdapter` | `context/event/` | 通用监听器适配器 |
| `OverrideAnnotationAttributes` | `context/annotation/` | 注解属性覆盖机制 |
| `@EnableAutoRegistrationBean` | `context/annotation/` | 自动注册 Bean |
| `AnnotatedBeanCapableImportBeanDefinitionRegistrar` | `context/annotation/` | 注解驱动导入注册器基类 |
| `@ImportOptional` | `context/annotation/` | 条件导入 |
| `ExposingClassPathBeanDefinitionScanner` | `context/annotation/` | 暴露类路径扫描器 |
| `AnnotatedBeanDefinitionRegistryUtils` | `context/annotation/` | Bean 定义注册工具类 |
| `ConfigurationPropertyOverrideAnnotationAttributesStrategy` | `context/annotation/` | 配置属性覆盖策略 |
| `DefaultConfigurationBeanAliasGenerator` | `beans/factory/` | 默认别名生成器 |
| `ListenableConfigurableEnvironment` | `core/env/` | 可监听环境，支持 `EnvironmentListener` / `PropertyResolverListener` / `ProfileListener` 注册和回调 |
| `PropertyResolverListener` | `core/env/` | 属性解析器监听器 |
| `EnvironmentListener` | `core/env/` | 环境监听器 |
| `ProfileListener` | `core/env/` | Profile 监听器 |
| `LoggingEnvironmentListener` | `core/env/` | 日志环境监听器 |
| `ListenableConfigurableEnvironmentInitializer` | `core/env/` | 环境初始化器 |
| `LoggingBeanFactoryListener` | `context/event/` | 日志 Bean 工厂监听器 |
| `LoggingBeanListener` | `context/event/` | 日志 Bean 监听器 |
| `CompositeAutowireCandidateResolvingListener` | `beans/factory/support/` | 复合自动装配监听器 |
| `LoggingAutowireCandidateResolvingListener` | `beans/factory/support/` | 日志自动装配监听器 |
| `ListenableAutowireCandidateResolverInitializer` | `beans/factory/support/` | 自动装配解析器初始化器 |
| `HyphenAliasGenerator` | `beans/factory/support/` | 连字符别名生成器 |
| `JoinAliasGenerator` | `beans/factory/support/` | 连接别名生成器 |
| `UnderScoreJoinAliasGenerator` | `beans/factory/support/` | 下划线连接别名生成器 |

### 未实现 ❌

（全部已实现）

---

## 二、microsphere-spring-web（核心 Web 扩展）

### 已实现 ✅

| 功能 | Rose 位置 | 说明 |
|------|-----------|------|
| `WebEndpointMapping` | `web/metadata/` | Web 端点映射模型 |
| `WebEndpointMappingRegistry` | `web/metadata/` | Web 端点映射注册表 |
| `WebEndpointMappingResolver` | `web/metadata/` | Web 端点映射解析器 SPI |
| `WebEndpointMappingRegistrar` | `web/metadata/` | Web 端点映射注册器 |
| `WebEndpointMappingFactory` | `web/metadata/` | Web 端点映射工厂 SPI |
| `WebEndpointMappingFilter` | `web/metadata/` | Web 端点映射过滤器 SPI（Rose 用 `Predicate` 替代原始 `Filter`） |
| `SimpleWebEndpointMappingRegistry` | `web/metadata/` | 简单端点映射注册表实现 |
| `CompositeWebEndpointMappingRegistry` | `web/metadata/` | 复合端点映射注册表实现 |
| `Jackson2WebEndpointMappingFactory` | `web/metadata/` | Jackson JSON 格式的端点映射工厂 |
| `AbstractWebEndpointMappingFactory` | `web/metadata/` | 端点映射工厂抽象基类 |
| `SmartWebEndpointMappingFactory` | `web/metadata/` | 自动发现所有工厂 SPI |
| `RegistrationWebEndpointMappingFactory` | `web/metadata/` | Servlet Registration 工厂基类 |
| `ServletRegistrationWebEndpointMappingFactory` | `web/metadata/` | Servlet 注册工厂实现 |
| `FilterRegistrationWebEndpointMappingFactory` | `web/metadata/` | Filter 注册工厂实现 |
| `WebEventPublisher` | `web/event/` | Web 事件发布器 |
| `HandlerMethodAdvice` | `web/` | 处理器方法通知 |
| `DelegatingHandlerMethodAdvice` | `web/` | 委托处理器方法通知 |
| `HandlerMethodArgumentsResolvedEvent` | `web/event/` | 参数解析完成事件 |
| `WebEndpointMappingsReadyEvent` | `web/event/` | 端点映射就绪事件 |
| `MethodHandlerInterceptor` | `web/` | 方法处理器拦截器 |
| `@EnableWebExtension` | `web/annotation/` | Web 扩展总注解（适用于 WebMVC / WebFlux 等场景） |
| `WebExtensionBeanDefinitionRegistrar` | `web/annotation/` | Web 扩展 Bean 定义注册器 |
| `HandlerMethodArgumentInterceptor` | `web/method/support/` | 参数拦截器 SPI |
| `HandlerMethodInterceptor` | `web/method/support/` | 方法拦截器 SPI |
| `RequestContextStrategy` | `web/util/` | 请求上下文存储策略枚举 |

### 未实现 ❌

（全部已实现）

---

## 三、microsphere-spring-webmvc（WebMVC 扩展）

### 已实现 ✅

| 功能 | Rose 位置 | 说明 |
|------|-----------|------|
| `@EnableWebMvcExtension` | `webmvc/annotation/` | WebMVC 扩展总注解 |
| `WebMvcExtensionBeanDefinitionRegistrar` | `webmvc/annotation/` | Bean 定义注册器 |
| `WebMvcExtensionConfiguration` | `webmvc/` | WebMVC 配置类 |
| `InterceptingHandlerMethodProcessor` | `webmvc/method/` | 拦截式处理器方法处理器 |
| `ReversedProxyHandlerMapping` | `webmvc/handler/` | 反向代理处理器映射 |
| `LazyCompositeHandlerInterceptor` | `webmvc/interceptor/` | 懒加载复合拦截器 |
| `StoringRequestBodyArgumentAdvice` | `webmvc/advice/` | 存储 `@RequestBody` 参数 |
| `StoringResponseBodyReturnValueAdvice` | `webmvc/advice/` | 存储 `@ResponseBody` 返回值 |
| `ConfigurableContentNegotiationManagerWebMvcConfigurer` | `webmvc/` | 可配置内容协商 |
| `ContentCachingFilter` | `webmvc/` | 内容缓存过滤器 |
| `ExclusiveViewResolverApplicationListener` | `webmvc/` | 独占视图解析器 |
| `LoggingMethodHandlerInterceptor` | `webmvc/interceptor/` | 日志方法拦截器 |
| `LoggingPageRenderContextHandlerInterceptor` | `webmvc/interceptor/` | 日志页面渲染拦截器 |
| `LoggingHandlerMethodArgumentResolverAdvice` | `webmvc/` | 日志参数解析器通知 |
| `HandlerMappingWebEndpointMappingResolver` | `webmvc/metadata/` | HandlerMapping 端点解析器 |
| `ServletWebEndpointMappingResolver` | `web/metadata/` | Servlet 端点映射解析器（Rose 放在 `web/metadata/`，而非 `webmvc/metadata/`） |

### 未实现 ❌

（全部已实现）

注：Rose 将 WebMVC 功能直接放在 `webmvc/` 下，合并了 microsphere 的 `microsphere-spring-web` + `microsphere-spring-webmvc` 两层，结构更扁平。

---

## 四、microsphere-spring-boot-core

### 已实现 ✅

| 功能 | Rose 位置 | 说明 |
|------|-----------|------|
| `ConfigurationPropertiesAutoConfiguration` | `autoconfigure/` | 配置属性自动装配 |
| `ConfigurableAutoConfigurationImportFilter` | `autoconfigure/` | 可配置自动装配导入过滤器 |
| `ConditionEvaluationReportInitializer` | `report/` | 条件评估报告初始化器 |
| `ConditionEvaluationReportListener` | `report/` | 条件评估报告监听器 |
| `ConditionEvaluationSpringBootExceptionReporter` | `report/` | 条件评估异常报告器 |
| `OriginTrackedConfigurationPropertyInitializer` | `env/config/` | 来源追踪配置属性初始化器 |
| `BannedArtifactClassLoadingListener` | `classloading/` | 禁用构件类加载监听器 |
| `FailureReportSpringApplicationRunListener` | `event/` | 失败报告运行监听器 |
| `ArtifactsCollisionDiagnosisListener` | `diagnostics/` | 构件冲突诊断监听器 |
| `ArtifactsCollisionFailureAnalyzer` | `diagnostics/` | 构件冲突失败分析器 |
| `DefaultPropertiesApplicationListener` | `env/` | 默认属性应用监听器 |
| `SpringApplicationDefaultPropertiesPostProcessor` | `env/` | 默认属性后处理器 |

### 未实现 ❌

（全部已实现）

注：rose-spring-boot-autoconfigure 已经完整移植了 microsphere-spring-boot-core 的所有 SPI 扩展点。

---

## 五、microsphere-spring-boot-actuator

### 已实现 ✅

| 功能 | Rose 位置 | 说明 |
|------|-----------|------|
| `ActuatorEndpointsAutoConfiguration` | `starter-actuator/autoconfigure/` | 端点自动装配 |
| `ActuatorAutoConfiguration` | `starter-actuator/autoconfigure/` | 执行器自动装配（任务调度器） |
| `ArtifactsEndpoint` | `starter-actuator/endpoint/` | 构件端点 |
| `ConfigurationMetadataEndpoint` | `starter-actuator/endpoint/` | 配置元数据端点 |
| `ConfigurationPropertiesEndpoint` | `starter-actuator/endpoint/` | 配置属性端点 |
| `WebEndpoints` | `starter-actuator/endpoint/` | Web 端点聚合 |
| `ConfigurationMetadataReader` | `autoconfigure/properties/metadata/` | 配置元数据读取器 |

### 未实现 ❌

| 功能 | MicroSphere 位置 | 说明 |
|------|-----------------|------|
| `MonitoredThreadPoolTaskScheduler` — Rose 已将其放到 `rose-spring/core/` 而非 actuator 模块 | `core/` | 注意位置差异：Rose 在 `core/`，MicroSphere 在 actuator 模块内 |

注：Rose 的 actuator 已完整移植所有端点和自动装配。

---

## 六、microsphere-spring-boot-webmvc / webflux

### 已实现 ✅

| 功能 | Rose 位置 | 说明 |
|------|-----------|------|
| `WebMvcAutoConfiguration` — 内容已在 `@EnableWebMvcExtension` + WebMvc 扩展类中实现 | `webmvc/` | 通过注解驱动而非自动装配 |
| `@ConditionalOnWebMvcAvailable` | `rose-spring-boot-autoconfigure/condition/` | WebMVC 可用条件注解，用于 `WebMvcAutoConfiguration` |

### 未实现 ❌

| 功能 | MicroSphere 位置 | 说明 |
|------|-----------------|------|
| `@ConditionalOnWebFluxAvailable` | `boot-webflux/condition/` | WebFlux 可用条件注解（用户明确不移植 WebFlux） |
| `WebFluxAutoConfiguration` | `boot-webflux/autoconfigure/` | WebFlux 自动装配（用户明确不移植 WebFlux） |

注：Rose 的 WebMVC 自动装配是通过 `@EnableWebMvcExtension` 注解驱动的，而非 `spring.factories` 自动装配。microsphere 有两种方式均可。

---

## 七、microsphere-spring-test（测试工具集）

移植目标模块：`rose-test`（包 `io.zhijun.spring.test.*`）

### 已实现 ✅

| 子模块 | Rose 位置 | 说明 |
|--------|----------|------|
| `util/ThrowableConsumer.java` | `util/` | 异常化 Consumer 接口 |
| `util/ThrowableBiConsumer.java` | `util/` | 异常化 BiConsumer 接口 |
| `util/SpringTestUtils.java` | `util/` | Spring 测试通用工具方法 |
| `util/ServletTestUtils.java` | `util/` | Servlet 注册工具（addTestServlet / addTestFilter） |
| `util/SpringTestWebUtils.java` | `util/` | Web 请求构建工具（MockHttpServletRequest 封装） |
| `web/WebTestUtils.java` | `web/` | 测试常量（路径、参数名、header 名等） |
| `web/servlet/*` | `web/servlet/` | TestServlet / TestFilter / TestServletContext 等（6 个） |
| `web/context/request/MockServletWebRequest.java` | `web/context/request/` | Mock ServletWebRequest 测试实现 |
| `web/controller/TestController.java` | `web/controller/` | 测试用 Controller（helloWorld / user / view 等端点） |
| `context/annotation/TestConditionContext.java` | `context/annotation/` | 测试用 ConditionContext |
| `context/annotation/AnnotatedTypeMetadataTestFactory.java` | `context/annotation/` | AnnotatedTypeMetadata 工厂 |
| `domain/User.java` | `domain/` | 测试用 User 实体 |
| `webmvc/PersonHandler.java` | `webmvc/` | WebMVC 函数式端点 Handler（骨架） |
| `webmvc/RouterFunctionTestConfig.java` | `webmvc/` | RouterFunction 测试配置 |
| `webmvc/SimpleUrlHandlerMappingTestConfig.java` | `webmvc/` | SimpleUrlHandlerMapping 测试配置 |
| `webmvc/AbstractWebMvcTest.java` | `webmvc/` | WebMVC 集成测试基类 |
| `jdbc/embedded/*` | `jdbc/embedded/` | 嵌入式数据库支持（H2 / SQLite，基于注解驱动） |

### 未实现 ❌

| 子模块 | 原因 |
|--------|------|
| `webflux/*`（4 个文件） | 用户明确跳过 WebFlux |
| `tomcat/embedded/*`（4 个文件） | 依赖 `io.microsphere.logging` / `io.microsphere.util` 等微内核工具链，未移植 |
| `zookeeper/embedded/*`（2 个文件） | 需要 ZooKeeper 依赖，非核心功能 |
| `junit/jupiter/SpringLoggingTest.java` | 依赖 `microsphere-logging` 模块的 `LoggingLevelsClass`，未移植 |

---

## 微调适配项

| 项目 | MicroSphere 原始 | Rose 适配 | 原因 |
|------|-----------------|-----------|------|
| `WebEndpointMapping` | 泛型类 `WebEndpointMapping<E>` | 非泛型 `final class` | 简化——不耦合 Jakarta/Servlet 端点类型 |
| `WebEndpointMappingFactory.create()` | 返回 `Optional<WebEndpointMapping<E>>` | 返回 `Optional<WebEndpointMapping>` | 因 `WebEndpointMapping` 非泛型 |
| `WebEndpointMappingFilter` | `Filter<WebEndpointMapping>` (Servlet Filter) | `Predicate<WebEndpointMapping>` | Rose 基于 Spring Boot 2.7 / Java 8，使用标准 Java 函数式接口 |
| `DelegatingHandlerMethodAdvice` | 包 `web/method/support/` | 包 `web/` | Rose 扁平化分包 |
| `Map.of()` / `List.of()` | Java 9+ | 替换为 Java 8 兼容写法 | Rose 目标 Java 8 |
| `Stream.toList()` | Java 16+ | 替换为 `collect(toList())` | Rose 目标 Java 8 |
| 包前缀 | `io.microsphere.*` | `io.zhijun.*` | 项目命名空间 |

## 模块对照总表

| MicroSphere 模块 | Rose 对应模块 | 覆盖度 |
|-----------------|--------------|--------|
| `microsphere-spring-context` | `rose-spring` (`beans/`, `context/`, `core/`, `config/`) | **约 100%** |
| `microsphere-spring-web` | `rose-spring` (`web/`) | **约 100%** |
| `microsphere-spring-webmvc` | `rose-spring` (`webmvc/`) | **约 100%** |
| `microsphere-spring-boot-core` | `rose-spring-boot-autoconfigure` | **约 100%** |
| `microsphere-spring-boot-actuator` | `rose-spring-boot-starter-actuator` | **约 100%** |
| `microsphere-spring-boot-webmvc` | `rose-spring-boot-autoconfigure` / `rose-spring` (`webmvc/`) | **约 100%** |
| `microsphere-spring-boot-webflux` | 无 | 0%（跳过） |
| `microsphere-spring-test` | `rose-test` | **约 95%** — Servlet 测试工具、WebMVC 测试基类、嵌入式 JDBC 支持已移植；嵌入式 Tomcat/ZK、SpringLoggingTest 因依赖链未移植 |

---

> **总结：** Rose 已移植 microsphere **约 100%** 的 Spring / Spring Boot 核心功能。Spring 上下文、Web、WebMVC、Boot Core、Actuator 模块全部移植完成。WebFlux 功能已明确跳过不移植。
