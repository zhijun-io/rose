# sdk-java 借鉴清单

> 对比对象：[temporalio/sdk-java](https://github.com/temporalio/sdk-java)（Gradle 多模块 Java 库 + Spring Boot 集成，与
> Rose 同为「被应用依赖的扩展平台」）
> 日期：2026-06-29 · 基线：Rose `eb7fac0` @ Spring Boot 2.7.18 / Java 8+

## 背景

sdk-java 与 Rose 定位高度可比：都是对外发布的 Java 库、都提供 BOM、都做 Spring Boot 集成、都锁 SB 2.7.18 构建基线、都面向
Java 8+。sdk-java 更成熟（多版本发版、SB3/4 兼容验证、formatter+errorprone 门禁），其工程实践对 Rose 有直接参考价值。

下表按 **对 Rose 的价值 × 落地成本** 排序。

| #  | 借鉴点                                  | sdk-java 做法                                    | Rose 现状                             | 价值  | 成本 |
|----|--------------------------------------|------------------------------------------------|-------------------------------------|-----|----|
| 1  | 多 SB 版本兼容测试                          | 构建锁 SB2.7，CI 主动测 SB3/4                         | CI 只测 JDK 矩阵，SB 锁死 2.7              | ★★★ | 中  |
| 2  | AutoConfiguration.imports 双注册        | SB2.7 模块同时保留 `.factories` + `.imports`         | 26 模块全用 `.factories`，0 个 `.imports` | ★★★ | 低  |
| 3  | formatter + 静态分析门禁                   | spotless(google-java-format) + errorprone，构建强制 | 仅 `.editorconfig`，无 formatter/分析    | ★★★ | 中  |
| 4  | 编译器 `-Werror` + `-Xlint:deprecation` | 编译即失败于弃用/告警                                    | 仅 `-parameters`，无 lint 严格化          | ★★  | 低  |
| 5  | `internal` 包边界约定                     | 包级 `*.internal.*` + 文档双保险                      | `@Incubating` 注解 + 处理器，无包级约定        | ★★  | 低  |
| 6  | 依赖兼容下界文档化                            | 每个依赖版本带 `[1.0.0,)` 兼容下界注释                      | BOM 给对齐版本，未标注兼容下界                   | ★★  | 低  |
| 7  | git tag 驱动版本号                        | `git describe --tags` 自动推导版本                   | `${revision}` 手动属性 + flatten        | ★★  | 中  |
| 8  | SNAPSHOT 自动发布                        | push 到 main 自动发 snapshot                       | 仅 tag/manual 触发                     | ★   | 低  |
| 9  | starter 极薄化                          | starter 只聚合依赖，零逻辑                              | 基本符合，需核对保持                          | ★   | 低  |
| 10 | 公共测试工具                               | `temporal-testing` 提供JUnit Rule                | `rose-test` 已有，可增强                  | ★   | 中  |
| 11 | shaded 兜底模块                          | shaded Protobuf/gRPC/Guava                     | 无                                   | ☆   | 高  |

---

## 1. 多 Spring Boot 版本兼容测试 ★★★

**sdk-java 做法**

构建基线锁 SB 2.7.18（`build.gradle:34`），但 CI 用 `-P springBoot3Test` / `-P springBoot4Test` + `edgeDepsTest`
切换测试依赖版本，主动验证下游升级路径：

- `build.gradle:36-38` 定义 `springBoot3Version=3.5.12`、`springBoot4Version=4.0.2`
- `.github/workflows/ci.yml:42-52` 三个独立 step 跑 SB2 / SB3 / SB4 兼容测试
- `edgeDepsTest` 属性同时切换 slf4j、micrometer、logback 等到 SB3 配套版本（`build.gradle:32-41`）

**Rose 现状**

CI 矩阵只覆盖 JDK 8–25（`.github/workflows/ci.yml:14-17`），Spring Boot 锁死 2.7，renovate 显式禁升（`renovate.json` 多条
`allowedVersions:<3.0.0`）。**完全不验证 SB3 兼容边界**，未来迁移时断裂点（`javax`→`jakarta`、`spring.factories`→`.imports`
、配置绑定路径变化）会集中暴露。

**借鉴建议**

即便维持「不升级 SB2」的决策，也应加一个 CI job：用 SB3 依赖跑 `rose-spring-boot-*` 的测试。价值：

- 提前暴露 `javax`→`jakarta`、`spring.factories`→`AutoConfiguration.imports` 断裂点
- 让依赖 SB3 的下游知道能否与 Rose 混用
- 缓解 SB2.7 EOL 的认知空白（这是 Rose 当前最大盲区）

成本中：需定义测试范围（哪些模块、SB3 依赖集）、可能要加 `jakarta` 适配层。建议先做 `sdd-spec` 定边界。

---

## 2. AutoConfiguration.imports 双注册过渡 ★★★

**sdk-java 做法**

`temporal-spring-boot-autoconfigure/src/main/resources/META-INF/` 下同时存在：

- `spring.factories`（SB2.x 机制）
- `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（SB2.7+/3.x 机制）

**Rose 现状**

26 个模块全用 `spring.factories`，0 个 `.imports`（审计 P2 迁移债）。

**借鉴建议**

SB2.7 同时支持两种机制。双注册是零成本过渡：SB2.7 读 `.imports`，SB3 读 `.imports`，两份并存不冲突。逐步迁移时每个模块加一份
`.imports` 即可，比「等升级一次性全转」风险低。与 #1 配合，可在 SB3 兼容测试中直接验证 `.imports` 是否被正确加载。

成本极低：每个模块新建一个 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，列出全限定类名。

---

## 3. formatter + 静态分析门禁 ★★★

**sdk-java 做法**

- `gradle/linting.gradle`：`spotless` + `google-java-format`，`classes.dependsOn 'spotlessApply'`（构建即格式化）
- `gradle/errorprone.gradle`：`errorprone` 静态分析，按 JDK 版本选 2.31.0 / 2.10.0
- `CONTRIBUTING.md`：PR 前须 `./gradlew spotlessApply`
- `AGENTS.md` review 清单：`spotlessCheck` 必过 + 新功能/bug 须加测试

**Rose 现状**

仅 `.editorconfig`，无 formatter 强制、无静态分析。CI 跑 enforcer（依赖收敛/版本约束）但不校验代码格式，也无空指针/资源泄漏类静态检查。

**借鉴建议**

Maven 生态对等工具：

- `spotless-maven-plugin` + `google-java-format`（或 `palantir-java-format`）
- `errorprone-maven-plugin` 或 `spotbugs-maven-plugin`

接入 CI 的 `verify` 阶段可堵住风格漂移和一类常见 bug。Rose 已有 enforcer 基础，加这两个成本低、收益高。注意：Java 8 下
errorprone 需特殊配置（sdk-java 用 `errorproneJavac` 单独处理）。

**spotless 已评估并移除**：JDK 8/11/17/21/25 矩阵下 formatter 跨版本规则不一致（palantir 1.1.0 与 2.x 链式调用换行不同），无法用单一版本覆盖全矩阵，按 JDK 分版本会令 CI 互斥。已移除 spotless 门禁，暂以 `.editorconfig` + IDE 格式化为准；如需强门禁须固定单一 JDK 跑格式化。

---

## 4. 编译器 `-Werror` + `-Xlint:deprecation` ★★

**sdk-java 做法**

`gradle/java.gradle:22-30`：

```groovy
compileJava {
    options.compilerArgs << '-Xlint:none' << '-Xlint:deprecation' << '-Werror' << '-parameters'
    options.compilerArgs.addAll(['--release', '8'])
}
```

弃用 API 用了即编译失败，强制处理迁移。

**Rose 现状**

`rose-build/pom.xml:131-133` 仅 `-parameters`，无 lint 严格化。

**借鉴建议**

在 `maven-compiler-plugin` 的 `compilerArgs` 加 `-Xlint:deprecation -Werror`（可先只对 main，test
放宽）。成本极低，但能强制及时处理弃用告警。注意需先清理现有告警（Java 9+ profile 已有部分配置），建议先 `mvn compile`
看当前告警量再决定。

---

## 5. `internal` 包边界约定 ★★

**sdk-java 做法**

- 包级：`io/temporal/internal/*`（`temporal-sdk/src/main/java/io/temporal/internal/` 下大量实现类）
- 文档：`AGENTS.md`「Anything under an `internal` directory is not part of the public API and may change freely.」

包级 + 文档双保险，消费者一看包名即知不可依赖。

**Rose 现状**

用 `@Incubating` 注解 + `InternalApiProcessor` 注解处理器做 API 治理，但缺包级 `internal` 约定——实现细节与公共 API
混在同一包，靠注解而非结构区分。

**借鉴建议**

补充约定：实现细节放 `*.internal.*` 子包，与现有 `@Incubating` 注解互补。更进一步，`InternalApiProcessor` 可校验 `internal`
包不被模块外部引用（编译期拦截）。成本主要在约定文档化 + 现有代码归位，可渐进。

---

## 6. 依赖兼容下界文档化 ★★

**sdk-java 做法**

`build.gradle:30-48` 每个依赖版本带兼容下界注释：

```groovy
grpcVersion = '1.76.0' // [1.38.0,) Needed for ...
slf4jVersion = '1.7.36' // slf4j 2.x is not compatible with spring boot 2.x
protoVersion = '3.25.8' // 3.25.5 is required because of our protoc compiler
```

向下游表达兼容承诺与升级约束。

**Rose 现状**

`rose-bom` 给对齐版本，`renovate.json` 有内部约束（testcontainers 锁 1.21.4 因 SB2.7 Jackson 2.13 不兼容），但这些约束*
*未文档化给消费者**。

**借鉴建议**

在 `rose-conventions.md` 或 BOM 注释里标注关键依赖（OTel、MyBatis-Plus、Testcontainers）的兼容下界与升级约束。例如「Testcontainers
锁 1.21.4：2.x 与 SB2.7 Jackson 2.13 不兼容」。帮消费者判断能否与自有版本共存，避免重复踩坑。成本极低，纯文档。

---

## 7. git tag 驱动版本号 ★★

**sdk-java 做法**

`gradle/versioning.gradle` 用 `git describe --tags` 自动推导版本：

- tag 上 → 版本 = tag
- tag 后 N commit → 下一 minor 的 SNAPSHOT
- RC tag 后 commit → RC 版本 SNAPSHOT

无需手动改 `revision` 属性，发版与 git tag 强绑定。

**Rose 现状**

`${revision}` 手动属性（`rose-build/pom.xml:60` = `0.0.1-SNAPSHOT`）+ flatten-maven-plugin 处理 CI-friendly。发版时需手动传
`-Drevision=x.y.z`（`publish.yml`）。

**借鉴建议**

Rose 已有 flatten 机制，可进一步用 `git-commit-id-maven-plugin` 或简单 Maven 扩展从 tag 推导 `${revision}`
，消除手动改版本号步骤。成本中等（需调整发版流程），价值在于减少发版时的人为错误。当前 0.0.1-SNAPSHOT 阶段优先级低，接近正式发版时再做。

---

## 8. SNAPSHOT 自动发布 ★

**sdk-java 做法**

`.github/workflows/publish-snapshot.yml`：push 到 main/master 自动发 snapshot 到 Sonatype，`paths-ignore` 排除文档变更。

**Rose 现状**

`publish.yml` 仅 tag / `workflow_dispatch` 触发，无自动 snapshot 发布。

**借鉴建议**

当前 0.0.1-SNAPSHOT 阶段下游消费少，优先级低。接近正式发版、有稳定下游消费 snapshot 时再加。成本极低（一个 workflow 文件 +
secrets）。

---

## 9. starter 极薄化 ★

**sdk-java 做法**

`temporal-spring-boot-starter/build.gradle` 仅 `api project` 聚合依赖，零 Java 代码。

**Rose 现状**

Rose 的 starter 模块基本符合（逻辑在 `-spring-boot` autoconfigure，starter 做聚合）。

**借鉴建议**

核对现有 starter 保持极薄：若有逻辑渗透到 starter，移到 autoconfigure。成本极低，主要是一次审查。

---

## 10. 公共测试工具 ★

**sdk-java 做法**

`temporal-testing` 模块提供 `TestWorkflowRule`、`TestActivityEnvironment` 等 JUnit Rule，消费者用它测自己的
workflow/activity。

**Rose 现状**

已有 `rose-test` 模块，定位类似。

**借鉴建议**

可参考 sdk-java 的 Rule/Extension 设计，让下游测 Rose 集成更省事（如自动装配测试基类、Testcontainers
便捷启动器）。价值中等，看下游测试痛点决定优先级。

---

## 11. shaded 兜底模块 ☆

**sdk-java 做法**

`temporal-shaded`：打包 shaded Protobuf/gRPC/Guava，给依赖冲突用户兜底。但 README 明确「不推荐，除非依赖地狱」。

**Rose 现状**

无 shaded 模块。

**借鉴建议**

Rose 的 OTel/Testcontainers 也是重依赖，未来若冲突投诉多可考虑。但 sdk-java 自己都不推荐，当前不必投入。成本高、收益不确定，*
*暂不建议**。

---

## 落地建议

按价值与成本，推荐分批：

**第一批（低成本高回报，可直接做）**

- #2 AutoConfiguration.imports 双注册：每个 Boot 模块加一份 `.imports` 文件
- #4 编译器 `-Werror`：先 `mvn compile` 看告警量，清理后开启
- #6 依赖兼容下界文档化：纯文档，补 `rose-conventions.md`
- #9 starter 极薄化核对：一次审查

**第二批（中等成本，需设计）**

- #1 多 SB 版本兼容测试：建议走 `sdd-spec` 先定测试范围与 SB3 依赖集
- #3 formatter + 静态分析：spotless 已评估移除（formatter 跨 JDK 版本规则不一致），errorprone 视 Java 8 兼容性而定
- #5 `internal` 包边界约定：约定文档化 + 现有代码渐进归位

**第三批（接近正式发版时）**

- #7 git tag 驱动版本号、#8 SNAPSHOT 自动发布

**不建议**

- #11 shaded 模块
