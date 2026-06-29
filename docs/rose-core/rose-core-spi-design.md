# rose-core SPI 设计说明

## 1. 目标

`rose-core` 的 SPI 目标是提供一套轻量、可预测、可排序、可手动重载的扩展机制，供 core 与上层模块复用。

当前设计重点是：

- 基于 `@Spi` / `@SpiImpl` 声明扩展点和实现
- 兼容 `META-INF/services` 约定
- 支持实现排序、别名、单例/多例、生命周期回调
- 支持编译期生成 metadata 以减少运行期扫描开销
- 支持手动 `reload` / `destroy`
- 为 Spring 等上层模块保留实例创建扩展点

当前设计**不以插件热部署平台为目标**，也**不负责自动探测 classpath/JAR 变化**。

## 2. 组成

核心类型如下：

- `io.zhijun.core.spi.annotation.Spi`
- `io.zhijun.core.spi.annotation.SpiImpl`
- `io.zhijun.core.spi.SpiLoader`
- `io.zhijun.core.spi.SpiLifecycle`
- `io.zhijun.core.spi.InstanceCreator`
- `io.zhijun.core.spi.SpiMetadataReader`

其中：

- `SpiLoader` 负责加载、排序、实例化、缓存、生命周期与手动重载
- `SpiMetadataReader` 只负责读取编译期生成的 `META-INF/rose/spi-metadata.json`
- `InstanceCreator` 是实例化扩展点，默认仍可回退到反射创建

## 3. 加载模型

SPI 加载顺序如下：

1. 优先尝试读取 `META-INF/rose/spi-metadata.json`
2. metadata 不存在或读取失败时，回退到 `META-INF/services/<spi-interface>`
3. 运行时统一进入同一套定义装配流程

装配流程包括：

- 过滤禁用实现
- 检查条件注解
- 按 `priority` 排序
- 对同优先级冲突打印告警
- 按 alias 处理 override

## 4. 条件模型

当前 SPI 条件只保留**注解式条件**。

典型方式：

- core 层：`@OnClassPresent`
- spring 层：`@OnProfile`、`@OnProperty`

条件注解通过 `@ConditionAnnotation` 关联对应的 `Condition` 实现类。  
`SpiLoader` 在运行期读取实现类上的条件注解并判断是否加载。

当前**不再支持** `@SpiImpl.conditions()` 这种第二入口，避免出现两套条件模型并存。

## 5. 实例化模型

默认情况下，SPI 实现通过无参构造器反射创建。

如果 classpath 中存在 `InstanceCreator` 实现，则：

1. 通过 JDK `ServiceLoader` 发现所有 `InstanceCreator`
2. 按 `@SpiImpl.priority` 排序
3. 依次尝试创建实例
4. 任一 `InstanceCreator` 返回非空实例则使用之
5. 全部返回 `null` 时回退到默认反射创建

这套设计允许上层模块扩展实例创建逻辑，例如：

- Spring 环境下从容器获取 Bean
- 容器参与依赖注入

但 `rose-core` 本身不直接依赖 Spring。

## 6. 生命周期模型

如果 SPI 实现类实现了 `SpiLifecycle`：

- 实例创建成功后调用 `init()`
- 单例实例在 `destroy()` / `reload()` / `destroyAll()` 过程中调用 `destroy()`

语义约束：

- 单例只初始化一次，直到被显式销毁
- 多例每次创建都会重新初始化
- 多例实例不会被框架统一回收，由调用方自行管理

## 7. 缓存与类加载器语义

`SpiLoader` 只缓存“稳定 classloader + 无排除项”的 loader。

这意味着：

- `SpiLoader.load(MySpi.class)` 默认会命中缓存
- 显式传入 `ClassLoader` 时，若不满足缓存条件，则每次返回新的 loader
- `reloadAll()` / `destroyAll()` 只作用于**内部缓存中的 loader**

这也是当前最重要的边界之一：

- 对于显式传入且未缓存的 loader，调用方需要自己持有引用
- `reloadAll(ClassLoader)` 不保证能处理所有外部自建 loader

## 8. 重载语义

当前 SPI 只支持**手动重载**，不支持自动热加载。

支持的方法：

- `SpiLoader.reload()`
- `SpiLoader.reloadAll()`
- `SpiLoader.reloadAll(ClassLoader)`
- `SpiLoader.destroy()`
- `SpiLoader.destroyAll()`

其中 `reload()` 的当前语义是：

1. 先尝试构建新的 loader
2. 新 loader 构建成功后，再销毁当前 loader 已初始化的单例实例
3. 若新 loader 构建失败，当前 loader 中已有实例仍然可继续使用

这保证了“重载失败不打空旧实例”。

## 9. 明确不支持的能力

当前 SPI **不负责**以下能力：

- 自动监听 JAR / 目录变化
- 自动创建新 `ClassLoader` 并完成类隔离切换
- 插件热部署 / 卸载平台
- 运行中无感知代理切流
- 引用计数退休、灰度切换、回滚编排
- 统一监控/actuator 暴露
- 安全沙箱或包白名单隔离

这些能力如果未来需要，应放在更高层模块中实现，例如：

- `rose-spring`
- `rose-spring-boot-*`
- 独立插件系统模块

而不是继续堆入 `rose-core`。

## 10. 设计取舍

当前 SPI 的设计取舍是：

- 优先保持 core 轻量，而不是追求完整插件平台能力
- 优先保证行为可解释，而不是引入过多自动化机制
- 优先复用 JDK 能力，而不是自造额外抽象

因此现在的 SPI 更适合作为：

- 框架内部扩展点机制
- 上层模块的可控装配基础

而不是完整的“动态插件系统”。

## 11. 后续建议

当前实现已经进入可接受状态，后续不建议继续大拆。更合理的演进方向是：

- 保持 core 语义稳定
- 在 `rose-spring` 层补充 SPI 与 Spring Bean 的接入文档
- 如果未来确实存在真实插件场景，再单独设计插件加载体系
