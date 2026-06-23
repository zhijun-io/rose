# Rose Build

Rose 仓库的 **Maven 构建父 POM**（`rose-build`），为所有模块提供统一的插件版本、测试约定、覆盖率与发布配置。

## 三层职责（build / parent / bom）

| 层级 | Artifact | 路径 | 职责 | 应用是否继承 |
|------|----------|------|------|--------------|
| Build | `rose-build` | `rose-build/` | 插件版本、测试/覆盖率/发布 profile、`${revision}` 源 | ❌ |
| Parent | `rose-parent` | 根 `pom.xml` | Reactor 模块列表、`dependencyManagement` | ❌ |
| BOM | `rose-bom` | `rose-bom/` | 消费方版本对齐 | ✅ import |

应用侧契约：**`spring-boot-starter-parent` + import `rose-bom`**，不继承 `rose-parent` 或 `rose-build`。详见根 [README.md](../README.md#getting-started)。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-build` | 单 POM，无子模块；被 `rose-parent` 继承 |

相关但不在本目录：

| Artifact | 路径 | 说明 |
|----------|------|------|
| `rose-parent` | 根 `pom.xml` | Reactor 与依赖版本父 POM |

## 已实现

### 版本与扁平化

- **`${revision}`** 定义于本文件 `properties`（当前发布版本唯一修改点）
- **`flatten-maven-plugin`**：`resolveCiFriendliesOnly`，install/deploy 时解析 CI 友好版本

### 默认绑定的插件（所有子模块继承）

| 插件 | 作用 |
|------|------|
| `maven-compiler-plugin` | Java 8、`-parameters` |
| `maven-source-plugin` | `package` 附加 sources |
| `maven-surefire-plugin` | 单元测试（`test` 阶段） |
| `maven-failsafe-plugin` | 集成测试（`integration-test` / `verify` 阶段） |
| `maven-enforcer-plugin` | `validate` 阶段环境校验 |
| `flatten-maven-plugin` | 扁平化 POM |

### 测试约定（Maven 标准生命周期）

| 插件 | 阶段 | 命名 | 典型命令 |
|------|------|------|----------|
| **Surefire** | `test` | `*Test`、`*Tests`（排除 `*IT`） | `mvn test` |
| **Failsafe** | `integration-test` + `verify` | `*IT` | `mvn verify` |

两者均在父 POM **默认绑定**；`failIfNoTests=false`，无对应测试的模块不会失败。

| 场景 | 命令 |
|------|------|
| 仅单元测试（快，无需 Docker） | `mvn test` |
| 单元 + 集成（本地默认，无 JaCoCo） | `mvn verify` |
| 跳过集成测试 | `mvn verify -DskipITs` |
| 本地查看覆盖率（可选，较慢） | 见下方 **本地查看覆盖率** |

**JaCoCo（`-Pcoverage` profile）**：JaCoCo **未**绑定默认 build；须加 `-Pcoverage` 才会启用 agent 并生成 `target/site/jacoco/` 报告。日常 `mvn verify` 不采集覆盖率。CI 在 JDK 21 使用 `mvn verify -Pcoverage`。POM-only starter 可设 `<jacoco.skip>true</jacoco.skip>`。

#### 本地查看覆盖率

```bash
# 全仓库（含 *IT，需 Docker，最慢）
mvn verify -Pcoverage

# 仅单元测试（更快，多数场景够用）
mvn test -Pcoverage

# 单模块（最快）
mvn -pl rose-opentelemetry/rose-opentelemetry-core -Pcoverage test
```

报告路径：`<module>/target/site/jacoco/index.html`（浏览器打开即可；macOS 示例：`open rose-core/target/site/jacoco/index.html`）。机器可读：`jacoco.xml` 同目录。

### Enforcer（`validate` 阶段，所有模块继承）

| 规则 | 约束 |
|------|------|
| `requireMavenVersion` | Maven ≥ 3.6 |
| `requireJavaVersion` | 构建 JDK **≥ 8**（字节码仍为 Java 8）；CI matrix 覆盖 8 / 11 / 17 / 21 / 25 |
| `banDuplicatePomDependencyVersions` | POM 内重复版本声明 |
| `bannedDependencies` | 占位（可扩展禁止依赖） |

### 发布（`-Prelease` profile）

- `maven-javadoc-plugin`、`maven-gpg-plugin`（`--pinentry-mode loopback`）
- `central-publishing-maven-plugin`（Sonatype Central Portal，`autoPublish=true`）
- 详见根 [README.md](../README.md#reference) 与 [wiki/CI-CD-Integration.md](../wiki/CI-CD-Integration.md)

### CI（GitHub Actions）

借鉴 [microsphere-build](https://github.com/microsphere-projects/microsphere-build) 的仓库内 workflow 风格（非 reusable workflow）：

| Workflow | 命令 / 行为 |
|----------|-------------|
| `maven-build.yml` | JDK **8 / 11 / 17 / 21 / 25** matrix；`mvn verify`（无 JaCoCo）；JDK 21 额外 `-Pcoverage`；`SONAR_TOKEN` / `CODECOV_TOKEN` 存在时上传覆盖率 |
| `maven-publish.yml` | `deploy` → Central；`release`：Tag → Cloudflare AI CHANGELOG → GitHub Release → bump SNAPSHOT → push |

`<revision>` 从 `rose-build/pom.xml` 读取；CI 通过 `-Drevision=…` 传入。

### 其他 profile

| Profile | 激活 | 作用 |
|---------|------|------|
| `coverage` | 手动 `-Pcoverage`（CI JDK 21 自动加） | JaCoCo agent + 各模块 `jacoco.xml` |
| `docs` | 手动 `-Pdocs` | AsciiDoc / DocBook 生成 |
| `java8+` / `java9+` / `java11+` | JDK 自动 | Javadoc 等 |
| `java9-15` | JDK 9–15 | `jvm.argLine`：`--illegal-access=permit` |
| `java16-20` | JDK 16–20 | `jvm.argLine`：Mockito 所需的 `--add-opens` |
| `java21+` | JDK 21+ | 同上，并追加 `-Dnet.bytebuddy.experimental=true`、`-XX:+EnableDynamicAgentLoading` |

### 元数据

组织、SCM、CI、许可证、开发者信息；`git-commit-id-plugin` 生成 `git.properties`（`pluginManagement`，子模块按需引用）。

## 未实现 / 规划中

- `maven-checkstyle-plugin` 未接入
- Failsafe 的 JaCoCo `prepare-agent-integration`（IT 覆盖率）待完善
- 多 JDK / 多 Boot 版本 matrix（Rose 锁定 Boot 2.7 / Java 8）

## 对标 Arconia

Arconia 为 Quarkus 生态扩展，构建以 Quarkus BOM / Gradle 为主。**无直接 Maven build parent 对标**。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-build](https://github.com/microsphere-projects/microsphere-build) | `rose-build` | ✅ 共享 build parent |
| BOM 文档化 | `rose-bom` | ✅ 见 `rose-bom/README.md` |

## 常用命令

```bash
# 改版本（唯一位置）
# rose-build/pom.xml → <revision>…</revision>

mvn validate
mvn compile test-compile          # 编译，不跑测试
mvn test                          # 单元测试（*Test / *Tests）
mvn verify                        # 单元 + 集成（*IT，需 Docker；无 JaCoCo）
mvn verify -DskipITs              # 仅单元，走完 verify 生命周期
mvn verify -Pcoverage             # 可选：本地生成 JaCoCo 报告
mvn -B clean deploy -Prelease     # 发布 Central（需 GPG + token）
```

## 建议下一步

1. 若需 IT 纳入 JaCoCo，增加 `prepare-agent-integration`
2. 若启用 checkstyle，补版本属性、规则文件与 execution
