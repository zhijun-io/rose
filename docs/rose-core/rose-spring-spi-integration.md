# rose-spring SPI 接入说明

## 1. 目的

本文档说明 `rose-core` SPI 在 Spring 环境中的接入方式，以及 `rose-spring-core` 提供了哪些增强能力。

`rose-core` 只定义通用 SPI 机制，不依赖 Spring。  
`rose-spring-core` 在此基础上补充：

- Spring Bean 参与 SPI 实例创建
- 基于 Spring 环境信息的条件加载能力

## 2. 相关类型

Spring 侧 SPI 相关类型主要包括：

- `io.zhijun.spring.core.spi.SpringInstanceCreator`
- `io.zhijun.spring.core.context.SpringContextHolder`
- `io.zhijun.spring.core.spi.condition.annotation.OnProfile`
- `io.zhijun.spring.core.spi.condition.annotation.OnProperty`
- `io.zhijun.spring.core.spi.condition.OnProfileCondition`
- `io.zhijun.spring.core.spi.condition.OnPropertyCondition`

## 3. Spring 如何接管 SPI 实例创建

`rose-core` 的 `SpiLoader` 在实例化 SPI 实现时，会先通过 JDK `ServiceLoader` 查找 `InstanceCreator`。

`rose-spring-core` 提供的 [`SpringInstanceCreator`](/Users/zhijunio/github/rose/rose-spring/rose-spring-core/src/main/java/io/zhijun/spring/core/spi/SpringInstanceCreator.java) 会在 classpath 中自动参与这一过程。

当前创建顺序如下：

1. `SpiLoader` 发现 `InstanceCreator`
2. 若当前为 Spring 环境，则优先委托 `SpringInstanceCreator`
3. `SpringInstanceCreator` 优先尝试从 `ApplicationContext` 获取目标 Bean
4. 如果容器中没有该 Bean，则尝试通过 `AutowireCapableBeanFactory#createBean(...)` 创建并注入依赖
5. 如果 Spring 侧创建失败，再回退到 `rose-core` 默认反射创建

因此，在 Spring 环境中，一个 SPI 实现类可以：

- 直接作为普通 Spring Bean 存在
- 不显式注册为 Bean，但仍可由 Spring 注入其依赖后创建

## 4. Spring 环境识别

Spring 侧增强依赖 [`SpringContextHolder`](/Users/zhijunio/github/rose/rose-spring/rose-spring-core/src/main/java/io/zhijun/spring/core/context/SpringContextHolder.java) 提供当前 `ApplicationContext`。

当 `ApplicationContext` 不可用时：

- `SpringInstanceCreator` 返回 `null`
- `SpiLoader` 自动回退到默认反射实例化
- Spring 条件注解不会按“Spring 环境已准备好”处理

也就是说，Spring 集成是**增强能力**，不是 core SPI 的硬前置条件。

## 5. Spring 条件注解

### 5.1 `@OnProfile`

[`OnProfile`](/Users/zhijunio/github/rose/rose-spring/rose-spring-core/src/main/java/io/zhijun/spring/core/spi/condition/annotation/OnProfile.java) 用于根据激活的 Spring Profile 决定某个 SPI 实现是否加载。

支持能力：

- 指定一个或多个 profile
- `allMatch = true/false`
- `matches = true/false`

语义：

- `matches = true`：条件成立时加载
- `matches = false`：条件成立时反向不加载

### 5.2 `@OnProperty`

[`OnProperty`](/Users/zhijunio/github/rose/rose-spring/rose-spring-core/src/main/java/io/zhijun/spring/core/spi/condition/annotation/OnProperty.java) 用于根据配置项决定某个 SPI 实现是否加载。

支持能力：

- 指定配置项名
- 指定一个或多个期望值
- `matchIfMissing = true/false`
- `matches = true/false`

语义：

- 未指定 `havingValue` 时，只要配置项存在即可匹配
- 指定 `havingValue` 时，要求配置值命中任一期望值

## 6. 非 Spring 环境下的条件语义

这是一个需要明确说明的边界。

对于 Spring 条件注解：

- 当 `ApplicationContext` 不存在时，`OnProfileCondition` / `OnPropertyCondition` 无法读取 Spring `Environment`
- 当前实现会按 `!condition.matches()` 的方式返回结果

这意味着：

- 默认场景下，Spring 条件注解在非 Spring 环境中通常视为不匹配
- 如果调用方显式使用反向条件（`matches = false`），则非 Spring 环境可能得到相反结果

因此，Spring 条件注解应当被视为：

- **Spring 专用 SPI 条件**
- 不建议在“同一 SPI 同时面向 Spring 和非 Spring 场景”的实现上滥用

## 7. 推荐用法

### 7.1 需要 Spring 注入的 SPI 实现

推荐：

- 给实现类标注 `@SpiImpl`
- 允许其依赖通过 Spring 注入
- 让 `SpringInstanceCreator` 参与实例创建

### 7.2 只在某个 Profile 启用的实现

推荐：

- 在实现类上叠加 `@OnProfile`
- 不需要再额外写 `@SpiImpl.conditions()`

### 7.3 只在配置开启时启用的实现

推荐：

- 在实现类上叠加 `@OnProperty`
- 将“是否启用”收敛为显式配置

## 8. 不建议的用法

当前不建议：

- 把 Spring 条件注解用于纯非 Spring 场景
- 指望 SPI 在 Spring 与非 Spring 环境中完全具备相同条件语义
- 在 SPI 实现中引入过强的容器耦合后，再要求其脱离 Spring 独立运行

## 9. 设计边界

`rose-spring-core` 当前只提供“Spring 参与 SPI 装配”的基础能力，不负责：

- 自动把某个 SPI 包装成 Spring 代理 Bean
- 在 Spring 容器运行期自动切换到 reload 后的新 SPI 实例
- SPI 级别的动态刷新编排
- 基于 actuator / endpoint 的 SPI 管理入口

这些如果未来需要，应放在更高层模块，例如：

- `rose-spring-boot-autoconfigure`
- 独立管理/诊断模块

## 10. 一句话总结

`rose-core` 提供通用 SPI 内核，`rose-spring-core` 负责把这套 SPI 接到 Spring 容器和 Spring 环境条件上，但不会把 core SPI 变成 Spring 专属机制。
