# Rose Build

Rose 仓库的 **Maven 构建父 POM**（`rose-build`），为所有模块提供统一的插件版本、测试约定、覆盖率与发布配置。

## 三层职责（build / parent / bom）

| 层级 | Artifact | 路径 | 职责 | 应用是否继承 |
|------|----------|------|------|--------------|
| Build | `rose-build` | `rose-build/` | 插件版本、测试/覆盖率/发布 profile、`${revision}` 源 | ❌ |
| Parent | `rose-parent` | 根 `pom.xml` | Reactor 模块列表、`dependencyManagement`、`-Pcoverage` | ❌ |
| BOM | `rose-bom` | `rose-bom/` | 消费方版本对齐 | ✅ import |

应用侧契约：**`spring-boot-starter-parent` + import `rose-bom`**，不继承 `rose-parent` 或 `rose-build`。详见 [CONTRIBUTING.md](../CONTRIBUTING.md)。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-build` | 单 POM，无子模块；被 `rose-parent` 继承 |

相关但不在本目录：

| Artifact | 路径 | 说明 |
|----------|------|------|
| `rose-parent` | 根 `pom.xml` | Reactor 与依赖版本父 POM |
| `rose-coverage` | `rose-coverage/` | `-Pcoverage` 时加入 reactor；JaCoCo 聚合 + 行覆盖率门禁 |

## 已实现

### 版本与扁平化

- **`${revision}`** 定义于本文件 `properties`（当前发布版本唯一修改点）
- **`flatten-maven-plugin`**：`resolveCiFriendliesOnly`，install/deploy 时解析 CI 友好版本

### 默认绑定的插件（所有子模块继承）

| 插件 | 作用 |
|------|------|
| `maven-compiler-plugin` | Java 8、`-parameters` |
| `maven-source-plugin` | `package` 附加 sources |
| `flatten-maven-plugin` | 扁平化 POM |

### 测试约定（`pluginManagement`）

| 插件 | 包含 | 排除 |
|------|------|------|
| **Surefire**（默认 `test` 阶段） | `**/*Test.java`、`**/*Tests.java` | `**/*IT.java` |
| **Failsafe**（`-Ptest` profile） | `**/*IT.java` | `**/*Test.java`、`**/*Tests.java` |

> 集成测试（`*IT.java`）需显式 `-Ptest` 或 `mvn verify -Ptest`；仅 `mvn test` **不会**执行 Failsafe。

JaCoCo：`prepare-agent` 注入 `@{jacoco.argLine}` 到 Surefire；POM-only starter 可设 `<jacoco.skip>true</jacoco.skip>`。

### 覆盖率（`-Pcoverage`，在 `rose-parent` 激活）

```bash
mvn verify -Pcoverage    # CI 同款；聚合报告 + 行覆盖率门禁
```

| 项 | 值 |
|----|-----|
| 聚合模块 | `rose-coverage`（不在默认 reactor） |
| 门禁 | `rose-coverage` 在 verify 阶段读取 `jacoco.minimum.line.coverage`（**未在 build 父 POM 定义**，由 CI / 命令行 `-D` 传入） |
| 报告路径 | `rose-coverage/target/site/jacoco-aggregate/index.html` |
| 发布 | `maven.deploy.skip=true`，不发布到 Central |

### Enforcer（`pluginManagement`，`-Prelease` 时绑定）

| 规则 | 约束 |
|------|------|
| `requireMavenVersion` | Maven ≥ 3.6 |
| `requireJavaVersion` | **仅 JDK 8**（`[1.8,1.9)`） |
| `banDuplicatePomDependencyVersions` | POM 内重复版本声明 |
| `bannedDependencies` | 占位（可扩展禁止依赖） |

日常 `mvn test` 不强制 Enforcer；`-Prelease` deploy 时启用。

### 发布（`-Prelease` profile）

- `maven-javadoc-plugin`、`maven-gpg-plugin`（`--pinentry-mode loopback`）
- `central-publishing-maven-plugin`（Sonatype Central Portal，`autoPublish=true`）
- 详见 [CONTRIBUTING.md § Releasing](../CONTRIBUTING.md#releasing-to-maven-central-sonatype)

### 其他 profile

| Profile | 激活 | 作用 |
|---------|------|------|
| `test` | 手动 `-Ptest` | 绑定 Failsafe + Surefire |
| `docs` | 手动 `-Pdocs` | AsciiDoc / DocBook 生成 |
| `java8+` / `java9+` / `java11+` / `java16+` | JDK 自动 | Javadoc、`jvm.argLine`（高版本 JDK 编译 Java 8 字节码时用） |

### 元数据

组织、SCM、CI、许可证、开发者信息；`git-commit-id-plugin` 生成 `git.properties`（`pluginManagement`，子模块按需引用）。

## 未实现 / 规划中

- **`docs/releasing.md`**：CONTRIBUTING 仍引用，文件尚未创建
- Enforcer **默认构建**未绑定（仅 release profile）——是否改为 `validate` 阶段全局执行待决
- **`-Ptest` 未默认激活**——与根 README「`mvn test` 需 Docker」表述不完全一致，CI 是否跑 IT 依赖 reusable workflow 配置
- `maven-checkstyle-plugin` 仅在 `pluginManagement` 声明，**未接入**构建
- `pluginManagement` 内 **重复声明** `maven-enforcer-plugin`（Maven 警告，待合并）
- 多 JDK / 多 Boot 版本 matrix（Rose 锁定 Boot 2.7 / Java 8）

## 对标 Arconia

Arconia 为 Quarkus 生态扩展，构建以 Quarkus BOM / Gradle 为主。**无直接 Maven build parent 对标**。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-build](https://github.com/microsphere-projects/microsphere-build) | `rose-build` | ✅ 共享 build parent |
| 独立 coverage 模块 | `rose-coverage`（profile 激活） | ✅ |
| BOM 文档化 | `rose-bom` | ⚠️ 见 `rose-bom/README.md` |

## 常用命令

```bash
# 改版本（唯一位置）
# rose-build/pom.xml → <revision>…</revision>

mvn validate
mvn compile test-compile          # 编译，不跑测试
mvn test                          # 单元测试（*Test / *Tests）
mvn verify -Ptest                 # 含集成测试（*IT，需 Docker）
mvn verify -Pcoverage             # CI：覆盖率聚合（门禁阈值见 CI -D 传参）
mvn -B clean deploy -Prelease     # 发布 Central（需 GPG + token）
```

## 建议下一步

1. 补齐 `docs/releasing.md` 或把 CONTRIBUTING 中的链接改为锚点
2. 决定 IT 策略：默认激活 Failsafe，或在文档/CI 中统一写 `verify -Ptest,coverage`
3. 合并 `pom.xml` 中重复的 `maven-enforcer-plugin` 声明
4. 若启用 checkstyle，在 `rose-build` 增加 execution 与规则文件路径
