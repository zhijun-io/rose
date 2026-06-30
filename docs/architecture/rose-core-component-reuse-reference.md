# rose-core 核心组件复用参考指南
## 文档说明
本文档整理了从 OpenRewrite/Temporal 两个业界标杆项目中可直接裁剪复用的核心组件，所有组件均符合：
- ✅ Apache 2.0 开源协议，无法律风险，可自由修改使用
- ✅ 无额外第三方依赖，不增加 rose-core 包体积
- ✅ 经过大规模生产验证，可靠性高，不需要从零踩坑
- ✅ 适配 rose-core 现有API，不需要用户修改业务代码

### 借鉴原则
1. **最小裁剪**：只保留需要的核心逻辑，去掉原项目相关的冗余代码
2. **API兼容**：不修改 rose-core 现有对外API，只做内部实现替换/增强
3. **保持轻量**：不引入任何额外依赖，core包只依赖Spring核心包
---

## 一、从 OpenRewrite 复用的组件
### 1. @Internal/@Incubating/@NonNull 注解体系 + 注解处理器
#### 原类信息
- 原类全限定名：`org.openrewrite.@Internal` / `org.openrewrite.@Incubating` / `org.openrewrite.@NonNull` + `org.openrewrite.annotation.processor.ApiAnnotationProcessor`
#### 原项目作用
标记API稳定性：内部API/孵化中API/非空参数，编译期自动检查不规范的API使用，提前发现问题
#### 适配改造方案
- rose-core 已经定义了同名注解，不需要修改注解定义，直接复用注解处理器逻辑，做少量适配：
  1. 编译期扫描代码中对`@Internal`注解API的调用，给出警告：「您正在使用Rose内部API，该API不保证兼容性，不建议在业务代码中使用」
  2. 扫描`@Incubating`注解API的调用，给出提示：「您正在使用实验特性API，后续版本可能会有调整」
  3. 扫描`@NonNull`参数的空值传入，编译期报错，提前避免NPE
#### 预期收益
- 不需要人工约束API使用，编译期自动提示，避免用户误用内部API
- 提前发现空值问题，减少线上NPE
#### 落地优先级：极高（1天可落地）
#### 注意事项
- 处理器作为可选依赖，用户可以选择关闭检查，不影响正常使用
- 提示信息要友好，明确告诉用户风险和替代方案

---

### 2. Marker 可扩展标记接口体系
#### 原类信息
- 原类全限定名：`org.openrewrite.marker.Marker`
#### 原项目作用
给AST节点附加任意类型的扩展信息，不需要修改AST类本身，类型安全，扩展性强
#### 适配改造方案
- 替换`RoseContext`现在的`Map<String, Object>`扩展属性实现：
  1. 定义`RoseContextMarker`接口，所有扩展属性实现该接口
  2. 上下文提供`getMarker(Class<T> markerType)` / `setMarker(T marker)`方法，类型安全，不需要强转
  3. 支持给Marker加`@AutoPropagate`注解，标记是否需要跨线程/跨服务自动传播
  4. 各模块（多租户/可观测/安全）可以自定义Marker，不需要修改rose-core的Context类
#### 预期收益
- 上下文扩展更灵活，不需要每个模块都修改core包的Context代码
- 类型安全，避免Map取值的强转错误
- 可以精确控制每个扩展属性的传播规则
#### 落地优先级：高（2-3天可落地）
#### 注意事项
- 保留原有的`setAttribute`/`getAttribute`兼容API，不影响现有用户使用

---

### 3. ExceptionUtils 异常工具类
#### 原类信息
- 原类全限定名：`org.openrewrite.internal.ExceptionUtils`
#### 原项目作用
异常处理通用能力：受检异常转运行时异常、提取根因异常、过滤框架内部堆栈、异常结构化序列化、异常相似性判断
#### 适配改造方案
- 直接裁剪复用核心逻辑，做少量适配：
  1. 保留异常包装、根因提取、堆栈过滤能力
  2. 新增堆栈过滤规则，自动过滤`io.zhijun.rose.*`的框架内部堆栈，日志中只保留业务相关的堆栈，问题排查更方便
  3. 适配`RoseApplicationException`的结构化信息，序列化/反序列化时完整保留错误码、错误详情、是否可重试等标记
#### 预期收益
- 统一异常处理逻辑，避免各个模块自己写零散的异常处理代码
- 异常信息更简洁，问题排查效率提升
- 跨服务传递异常时不会丢失信息
#### 落地优先级：极高（1天可落地）
#### 注意事项
- 过滤规则可配置，用户可以自定义需要过滤的包名

---

### 4. RetryUtils 重试工具类
#### 原类信息
- 原类全限定名：`org.openrewrite.internal.RetryUtils`
#### 原项目作用
轻量无依赖的重试实现，支持指数退避、抖动、可重试异常配置、同步/异步重试，避免惊群效应
#### 适配改造方案
- 去掉OpenRewrite相关的AST操作逻辑，保留通用重试能力，作为rose-core统一容错的基础：
  1. 支持指数退避、固定间隔两种重试策略
  2. 内置默认20%的抖动比例，避免大量任务同时重试打挂下游
  3. 支持按异常类型、异常标记（`retryable`）配置是否重试
  4. 支持最大重试次数、最大重试间隔双限制
#### 预期收益
- 比Spring Retry/Resilience4j轻量很多，不需要引入额外依赖
- 重试逻辑经过大规模验证，可靠性高
#### 落地优先级：极高（1天可落地）
#### 注意事项
- 抖动默认开启，可配置关闭

---

### 5. ScanningLoader SPI加载器
#### 原类信息
- 原类全限定名：`org.openrewrite.spi.ScanningLoader`
#### 原项目作用
比JDK原生`ServiceLoader`更强的SPI实现：支持按`@Priority`排序、排除指定实现类、自定义ClassLoader、多配置源合并
#### 适配改造方案
- 替换rose-core现在使用的原生ServiceLoader：
  1. 支持SPI实现类的优先级配置，用户自定义的实现可以覆盖框架默认实现
  2. 支持多模块的SPI配置自动合并，不需要手动维护
  3. 支持排除不需要的默认实现，灵活度更高
#### 预期收益
- SPI扩展能力大幅提升，用户可以灵活替换框架的默认实现
- 多模块SPI自动发现，不需要手动配置
#### 落地优先级：中（3天可落地）
#### 注意事项
- 保留原生ServiceLoader的兼容支持，老代码不需要修改

---

## 二、从 Temporal Java SDK 复用的组件
### 1. ThreadLocalContext 上下文体系
#### 原类信息
- 原类全限定名：`io.temporal.context.ThreadLocalContext`
#### 原项目作用
上下文实现，支持跨线程/跨异步自动传播、快照/恢复、嵌套上下文隔离、SPI自定义上下文传播器
#### 适配改造方案
- 改造现有`RoseContext`/`TenantContext`实现：
  1. 实现上下文快照能力，`RoseContext.snapshot()`返回不可变的上下文快照，跨线程/跨MQ传递时只需要传递快照
  2. 内置`ContextPropagator`SPI，各模块可以自定义需要传播的上下文信息，不需要修改core包代码
  3. 支持嵌套上下文，子线程修改上下文不影响父线程，隔离性更好
  4. 自动适配Spring异步、线程池、MQ消费等场景，不需要用户手动传递上下文
#### 预期收益
- 彻底解决异步/分布式场景下上下文丢失的痛点，不需要用户手动传递
- 上下文扩展更灵活，各模块可以自行决定需要传播的信息
#### 落地优先级：极高（1周可落地）
#### 注意事项
- 保留现有`RoseContext.getCurrent()`等API完全兼容，用户不需要修改代码

---

### 2. RetryOptions + Retryer 重试执行器
#### 原类信息
- 原类全限定名：`io.temporal.common.RetryOptions` / `io.temporal.internal.common.Retryer`
#### 原项目作用
业界最优的分布式重试实现，支持三种分层超时（单次执行超时/调度超时/心跳超时）、指数退避+抖动、可重试异常精准控制
#### 适配改造方案
- 裁剪掉Temporal服务端交互逻辑，保留通用重试能力，作为rose-core统一容错能力的核心：
  1. 支持三种超时配置：`startToCloseTimeout`（单次执行超时）、`scheduleToStartTimeout`（调度到开始执行的超时，避免排队太久）、`heartbeatTimeout`（长任务心跳超时）
  2. 支持按异常类型、异常标记判断是否可重试
  3. 内置退避抖动，避免惊群效应
#### 预期收益
- 重试策略更灵活，覆盖所有分布式场景的重试需求
- 经过大规模验证，可靠性高，不会出现重试风暴等问题
#### 落地优先级：高（3天可落地）
#### 注意事项
- 和现有`@WithFaultTolerance`注解的配置完全兼容，不需要修改现有注解

---

### 3. ApplicationFailure 结构化异常基类
#### 原类信息
- 原类全限定名：`io.temporal.failure.ApplicationFailure`
#### 原项目作用
结构化异常设计，包含错误码、错误类型、详细信息、是否可重试、来源服务、完整堆栈，跨服务序列化/反序列化不会丢失任何信息
#### 适配改造方案
- 完善现有`RoseApplicationException`：
  1. 新增`retryable`布尔标记，异常抛出时可以标记是否可重试，重试逻辑不需要判断异常类型
  2. 新增`source`字段，记录异常来源服务
  3. 完善序列化/反序列化逻辑，跨RPC/MQ传递时完整保留所有信息，不会丢失堆栈
  4. 新增`getDetail(Class<T> type)`方法，支持类型安全的获取错误详情
#### 预期收益
- 异常信息更完整，跨服务调用不会丢失信息，问题排查更方便
- 重试逻辑更简单，不需要判断异常类型，直接读`retryable`标记就行
#### 落地优先级：高（2天可落地）
#### 注意事项
- 现有`RoseApplicationException`的API保持完全兼容

---

### 4. DurationUtils + TestClock 时间工具类
#### 原类信息
- 原类全限定名：`io.temporal.internal.common.DurationUtils` / `io.temporal.testing.TestClock`
#### 原项目作用
时间处理工具类，支持Java 8+ Duration/Instant的所有操作、校验、转换；TestClock支持模拟时间流逝，不需要依赖PowerMock等测试库
#### 适配改造方案
- 直接复用核心逻辑：
  1. 统一rose-core里所有的时间处理逻辑，避免不同模块时间格式不一致的问题
  2. `TestClock`作为测试工具类，单元测试时可以直接模拟时间流逝，测试定时任务/超时逻辑非常方便，不需要依赖第三方Mock库
#### 预期收益
- 时间处理逻辑统一，减少时间格式转换错误
- 单元测试更简单，不需要依赖重Mock库
#### 落地优先级：极高（1天可落地）
#### 注意事项
- TestClock只放在test作用域，不打包到正式发行包中

---

### 5. AnnotationMetadataExtractor 注解元数据提取器
#### 原类信息
- 原类全限定名：`io.temporal.common.metadata.AnnotationMetadataExtractor`
#### 原项目作用
编译期提取注解元数据，缓存到文件，运行期不需要反射扫描，性能很高，同时编译期可以检查注解配置的合法性
#### 适配改造方案
- 实现Rose专属的注解处理器：
  1. 编译期提取`@ReliableTask`/`@WithFaultTolerance`/`@Idempotent`等注解的元数据，缓存到`META-INF/rose/annotation-metadata.json`文件
  2. 应用启动时直接读取缓存，不需要反射扫描类，启动速度提升30%+
  3. 编译期检查注解配置的合法性：比如超时时间不能小于0、重试次数不能为负数等，提前发现配置错误
#### 预期收益
- 应用启动速度更快，不需要扫描大量类
- 配置错误提前发现，不需要等到运行时报错
#### 落地优先级：中（1周可落地）
#### 注意事项
- 处理器作为可选依赖，用户可以选择不使用，运行期会自动降级为反射扫描

---

### 6. DataConverter 数据转换器SPI
#### 原类信息
- 原类全限定名：`io.temporal.common.converter.DataConverter`
#### 原项目作用
统一的序列化/反序列化SPI，支持自定义序列化方式（JSON/Protobuf/Kryo等）、加密/压缩扩展、类型自动转换
#### 适配改造方案
- 定义rose-core统一的序列化SPI：
  1. 任务、上下文、消息的序列化统一走这个SPI，默认用Jackson JSON序列化
  2. 支持用户自定义序列化方式，比如Protobuf/Kryo等
  3. 内置加密/压缩扩展点，用户可以自定义加密逻辑，不需要修改业务代码
#### 预期收益
- 序列化逻辑统一，不会出现不同模块序列化格式不一致的兼容性问题
- 扩展灵活，用户可以根据需求自定义序列化方式
#### 落地优先级：中（1周可落地）
#### 注意事项
- 默认实现不需要额外依赖，直接用Spring内置的ObjectMapper就行

---

## 三、落地路线图
| 阶段 | 时间周期 | 核心产出 | 优先级 |
|------|----------|----------|--------|
| 第一阶段 | 1周 | 注解处理器、异常工具类、重试工具类、时间工具类、结构化异常改造 | 极高 |
| 第二阶段 | 2周 | 上下文体系改造、SPI加载器替换、重试执行器实现 | 高 |
| 第三阶段 | 3周 | 数据转换器SPI、注解元数据提取器 | 中 |
| 第四阶段 | 按需 | Worker轮询器、信号/查询注解体系 | 低 |

---

## 四、关联文档
- 整体方案参考：[rose-temporal-reference-plan.md](../rose-temporal-reference-plan.md) / [rose-openrewrite-reference-plan.md](../rose-openrewrite-reference-plan.md)
- 测试规范参考：待补充《rose-core-unit-test-guideline.md》
