# Rose 框架 OpenRewrite 经验借鉴落地方案

## 一、方案背景
OpenRewrite作为业界领先的自动化代码重构生态系统，在大规模代码转换、API治理、生态建设等方面积累了大量成熟经验。本方案结合Rose作为企业级Spring Boot开发框架的定位，梳理可落地的借鉴点，分优先级实施，打造Rose的核心竞争力。

### 核心目标
1. **降低升级成本**：实现版本自动迁移，消除Breaking Change带来的升级阻力
2. **提升开发效率**：自动化重复工作，模板化生成代码，减少重复劳动
3. **统一生态标准**：规范扩展开发、项目配置、代码风格，提升一致性
4. **打造差异化竞争力**：形成其他Spring Boot框架不具备的自动化能力

---

## 二、高优先级落地方案（1-2个版本迭代，V1.x周期）
小投入大收益，快速提升用户体验。

### 2.1 Rose版本自动化迁移能力建设
#### 2.1.1 方案概述
开发`rose-rewrite-recipes`模块，为每个Rose版本的变更提供自动化迁移Recipe，用户升级时不需要手动修改代码，一键完成升级。

#### 2.1.2 核心功能
- **基础框架**：封装Rose专属的Recipe开发基类、工具类，降低Recipe开发成本
- **构建插件**：配套Maven/Gradle插件，用户可以一键执行迁移命令
- **版本配套**：每个Rose发布版本配套对应的迁移Recipe，覆盖：
  - API签名变更
  - 注解包名/属性变更
  - 配置文件key变更
  - 包结构重构变更
  - 废弃API替换

#### 2.1.3 实现示例
```java
// 示例：rose-annotation从rose-foundation迁移到rose-core的自动替换Recipe
public class AnnotationPackageMigrationRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Rose 1.x -> 2.x 注解包名迁移";
    }
    
    @Override
    public String getDescription() {
        return "将旧包名 io.zhijun.annotation.* 替换为新包名 io.zhijun.core.annotation.*";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.Import visitImport(J.Import impo, ExecutionContext ctx) {
                // 替换import语句
                if (impo.getPackageName().startsWith("io.zhijun.annotation.")) {
                    String newPackage = "io.zhijun.core.annotation." + 
                        impo.getPackageName().substring("io.zhijun.annotation.".length());
                    return impo.withPackageName(newPackage);
                }
                return super.visitImport(impo, ctx);
            }
        };
    }
}
```

#### 2.1.4 用户使用方式
```bash
# 一键升级到Rose 2.0，自动迁移所有变更
mvn rose:rewrite migrate -DtargetVersion=2.0.0
```

#### 2.1.5 收益评估
- 用户升级成本降低90%以上，大部分场景不需要手动修改代码
- 避免用户长期停留在旧版本，减少旧版本维护负担
- 成为Rose的核心差异化竞争力，目前国内同类型框架几乎没有提供这种能力

---

### 2.2 核心上下文不可变设计改造
#### 2.2.1 方案概述
将Rose的核心上下文对象从可变设计改为不可变设计，保证线程安全和可追溯性，避免并发修改问题。

#### 2.2.2 改造范围
- `TenantContext`：当前多租户上下文
- 未来新增的`RoseContext`：统一全局上下文
- 各个模块的核心配置类：如多租户配置、观测配置等

#### 2.2.3 实现方式
- 使用Lombok的`@Value` + `@Builder(toBuilder = true)`实现不可变对象
- 修改上下文设置API，替换属性时返回新对象而不是修改原对象
- 提供向后兼容的过渡API，减少用户升级成本

**改造后示例：**
```java
// 改造后的不可变TenantContext
@Value
@Builder(toBuilder = true)
public class TenantContext {
    String tenantId;
    String appId;
    Map<String, Object> attributes;
    
    public static TenantContext current() {
        // 从ThreadLocal获取
    }
    
    public static void setCurrent(TenantContext context) {
        // 设置ThreadLocal
    }
}

// 使用方式
TenantContext newContext = TenantContext.current().toBuilder()
    .tenantId("newTenantId")
    .build();
TenantContext.setCurrent(newContext);
```

#### 2.2.4 收益评估
- 彻底解决多线程环境下上下文并发修改的问题，稳定性大幅提升
- 所有变更可追踪，方便调试和问题排查
- 符合OpenTelemetry等可观测系统的上下文设计最佳实践

---

### 2.3 统一全局上下文机制
#### 2.3.1 方案概述
设计统一的`RoseContext`作为全局上下文载体，整合各个模块的上下文数据，提供跨场景的自动传递能力，避免各个模块重复实现上下文管理。

#### 2.3.2 核心功能
- **统一承载**：租户信息、请求信息、观测数据、自定义扩展数据都可以存入上下文
- **自动传递**：支持跨线程、异步调用、MQ消费、RPC调用等场景的自动传递，用户不需要手动处理
- **SPI扩展**：各个模块可以通过SPI往上下文里添加自己的数据，不需要修改核心Context类
- **快照机制**：支持上下文快照创建和恢复，方便在异步场景下传递

#### 2.3.3 接口设计
```java
public interface RoseContext {
    // 通用存取方法
    <T> Optional<T> get(String key);
    <T> void set(String key, T value);
    <T> T remove(String key);
    
    // 内置便捷方法
    Optional<TenantContext> getTenantContext();
    Optional<RequestContext> getRequestContext();
    Optional<ObservationContext> getObservationContext();
    
    // 上下文传递方法
    RoseContext snapshot(); // 创建不可变快照用于跨线程传递
    void attach(RoseContext snapshot); // 将快照附着到当前线程
    void clear(); // 清除当前线程的上下文
}
```

#### 2.3.4 收益评估
- 减少模块耦合，各个模块不需要互相依赖对方的上下文类
- 简化用户开发，不需要自己处理异步场景下的上下文传递问题
- 为后续全链路观测、调试、审计能力打下基础

---

## 三、中优先级落地方案（3-6个月迭代，V2.x周期）
明显提升框架体验，形成完整的开发闭环。

### 3.1 SPI自动发现机制优化
#### 3.1.1 方案概述
基于注解处理器实现编译时SPI注册，避免运行时类路径扫描，提升应用启动速度，简化扩展开发流程。

#### 3.1.2 核心功能
- 自定义`@RoseExtension`注解，标注所有扩展点实现类
- 编译时注解处理器自动生成`META-INF/services/`注册文件
- 运行时直接加载注册文件，不需要扫描类路径
- 支持扩展优先级、条件加载等能力

#### 3.1.3 使用示例
```java
// 用户自定义租户解析器，只需要加注解，不需要其他配置
@RoseExtension(priority = 100, condition = DevEnvironmentCondition.class)
public class CustomTenantResolver implements TenantResolver {
    @Override
    public String resolveTenantId(HttpServletRequest request) {
        // 自定义实现
    }
}
```
用户不需要额外配置`@Bean`或者`spring.factories`，框架自动发现加载。

#### 3.1.4 收益评估
- 扩展开发零配置，降低用户使用门槛
- 应用启动速度提升，减少类路径扫描开销
- 统一所有模块的扩展点机制，避免每个模块自己实现扩展加载

---

### 3.2 模板化代码生成能力增强
#### 3.2.1 方案概述
基于模板引擎封装代码生成能力，提供开箱即用的CRUD代码生成，支持用户自定义模板，提升业务开发效率。

#### 3.2.2 核心功能
- **内置模板**：Controller/Service/Mapper/DTO/Entity全套CRUD代码，符合Rose最佳实践
- **自定义模板**：用户可以根据团队规范修改模板，不需要修改Rose源码
- **注解驱动**：在实体类上加注解即可自动生成代码，支持增量生成
- **IDE插件集成**：支持在IDEA中右键生成，可视化配置生成选项

#### 3.2.3 使用示例
```java
@RoseCodeGen(
    basePackage = "com.example.demo",
    modules = {Module.CONTROLLER, Module.SERVICE, Module.MAPPER, Module.DTO},
    author = "zhijun",
    overwrite = false // 不覆盖已有文件，增量生成
)
@TableName("user")
public class User {
    @TableId
    private Long id;
    private String username;
    private String email;
    // 其他字段
}
```

#### 3.2.4 收益评估
- 简单CRUD开发效率提升100%，不需要写一行业务代码
- 统一团队代码风格，生成的代码都符合规范
- 降低新手学习成本，不需要了解Rose的各个模块细节就能快速开发

---

### 3.3 DevService Recipe化
#### 3.3.1 方案概述
把Rose DevService的能力抽象为可执行的Recipe，实现项目初始化、功能集成的自动化，降低框架使用门槛。

#### 3.3.2 核心Recipe列表
| Recipe名称 | 功能描述 |
|-----------|----------|
| `InitRoseProjectRecipe` | 初始化标准Rose项目结构、配置文件、示例代码、Gitignore等 |
| `AddMultitenancyRecipe` | 给现有项目添加多租户支持，自动配置数据源、拦截器、注解支持等 |
| `AddObservationRecipe` | 集成观测能力，自动配置Metrics、Tracing、Logging，对接主流观测平台 |
| `ConfigureDatabaseRecipe` | 自动配置数据库连接、连接池、MyBatis Plus、分页插件等 |
| `AddGrpcSupportRecipe` | 集成gRPC支持，自动配置服务端/客户端、拦截器、上下文传递等 |

#### 3.3.3 使用方式
```bash
# 给现有项目添加多租户支持
mvn rose:devservice run AddMultitenancyRecipe

# 初始化一个新的Rose项目
mvn rose:devservice run InitRoseProjectRecipe -DgroupId=com.example -DartifactId=demo
```

#### 3.3.4 收益评估
- 新项目初始化时间从小时级降到分钟级，不需要手动复制粘贴配置
- 统一项目配置规范，避免不同项目配置五花八门
- 降低Rose使用门槛，新手也能快速搭建符合规范的生产级项目

---

## 四、长期生态建设方案（6-12个月，V3.x周期）
打造差异化竞争力，形成良性生态循环。

### 4.1 Rose代码规范与最佳实践检查
#### 4.1.1 方案概述
开发Rose专属的静态代码检查和自动修复Recipe，统一代码质量，固化最佳实践，让所有使用Rose的项目都能达到较高的代码质量标准。

#### 4.1.2 检查范围
- **Rose API正确使用检查**：多租户注解使用、配置正确性、废弃API使用等
- **企业代码规范检查**：命名风格、注释要求、异常处理规范、日志规范等
- **安全最佳实践检查**：SQL注入风险、敏感信息泄露、权限校验缺失等
- **性能最佳实践检查**：慢查询风险、N+1问题、不合理的循环调用等

#### 4.1.3 集成方式
- 集成到Maven/Gradle构建流程，构建时自动检查，不符合规范的直接报错
- 支持IDEA插件实时检查和一键自动修复
- 支持自定义规则，企业可以添加自己内部的规范检查

#### 4.1.4 收益评估
- 统一团队代码质量，减少低级错误
- 最佳实践固化到工具，不需要口口相传，新人也能写出符合规范的代码
- 提升企业整体的代码质量水平，降低后续维护成本

---

### 4.2 Rose扩展生态建设
#### 4.2.1 方案概述
参考OpenRewrite的Recipe生态建设思路，打造可共享、可组合的Rose扩展生态，让社区用户可以贡献和复用扩展能力。

#### 4.2.2 核心功能
- **统一扩展开发规范**：所有扩展都遵循同一套标准，有统一的元数据、文档、测试要求
- **扩展市场平台**：用户可以贡献、搜索、安装扩展，支持版本管理、兼容性检查
- **扩展质量体系**：评分、下载量、官方认证、兼容性验证等机制，保证扩展质量
- **组合式扩展**：支持多个扩展组合使用，自动处理依赖关系和配置冲突

#### 4.2.3 收益评估
- 形成社区生态，丰富Rose的能力，不需要核心团队开发所有功能
- 提升Rose的影响力和用户粘性，用户贡献的越多，越离不开Rose
- 形成良性循环，生态越丰富，用户越多，贡献的人也越多

---

## 五、实施路线图
| 阶段 | 时间周期 | 目标 | 核心产出 |
|------|----------|------|----------|
| 第一阶段 | 1-2个版本（1-2个月） | 高优先级功能落地 | rose-rewrite-recipes模块、不可变上下文改造、统一RoseContext |
| 第二阶段 | 3-6个月 | 中优先级功能落地 | SPI自动发现机制、模板化代码生成、DevService Recipe化 |
| 第三阶段 | 6-12个月 | 生态建设 | 代码规范检查体系、扩展市场平台、官方扩展库 |

---

## 六、风险与应对措施
| 风险 | 影响 | 应对措施 |
|------|------|----------|
| OpenRewrite依赖过重，增加Rose的依赖大小 | 用户引入Rose时多了不必要的依赖 | rose-rewrite-recipes作为可选模块，用户需要迁移的时候才引入，核心模块不依赖OpenRewrite |
| 不可变上下文改造带来的API变更 | 现有用户需要修改代码才能升级 | 提供向后兼容的过渡API，同时配套自动迁移Recipe，用户可以一键完成升级 |
| 生态建设初期贡献不足，扩展市场内容少 | 扩展市场吸引力不足 | 核心团队先贡献常用扩展，鼓励合作伙伴贡献，提供贡献激励（如官方认证、技术支持等） |
| Recipe迁移覆盖不全，部分场景需要手动修改 | 影响用户升级体验 | 每个版本发布前进行充分的测试，收集用户反馈，及时补充遗漏的迁移场景，提供手动迁移指南 |

---

## 七、投入产出比评估
### 人力投入
- 第一阶段：约2人月
- 第二阶段：约3人月
- 第三阶段：约3人月
- 总投入：约8人月

### 预期收益
1. **用户升级成本降低90%**：每个用户升级版本平均节省2-3天的手动修改时间
2. **开发效率提升30%**：CRUD等重复工作自动化，每个项目平均节省20%的开发时间
3. **项目配置时间减少80%**：新项目初始化从平均2天降到1小时以内
4. **差异化竞争力**：形成其他框架没有的自动化能力，用户量预计增长30%以上

### ROI评估
收益远大于投入，投入产出比超过10倍，是非常值得投入的优化方向。
