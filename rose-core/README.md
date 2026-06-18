# Rose Core

纯 Java **框架无关** 基础层：注解与契约标记，供 Rose 各模块引用。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-core` | 单 jar；仅依赖 `slf4j-api` |

## 已实现

| 能力 | 说明 |
|------|------|
| `@Since` | API 引入版本标记 |
| `@Incubating` | 孵化中 API |
| `@Internal` | 内部 API，非稳定契约 |
| `@ThreadSafe` / `@NotThreadSafe` / `@Immutable` | 并发语义文档注解 |

## 未实现 / 规划中

- 通用 Java 工具类（字符串、集合、反射等）—— benchmark 建议 **保持 framework-light**，避免变成杂物间
- 独立 starter 或自动配置（本模块 intentionally 无 Spring 依赖）

## 对标 Arconia

无直接对应。Arconia 的通用工具多在 Quarkus 扩展或独立库中，Rose 刻意将 `rose-core` 缩到最小。

## 对标 Microsphere

| Microsphere | Rose | 状态 |
|-------------|------|------|
| [microsphere-java](https://github.com/microsphere-projects/microsphere-java) | `rose-core` | ⚠️ 仅注解层；无工具库广度 |
| [microsphere-java-enterprise](https://github.com/microsphere-projects/microsphere-java-enterprise) | — | ❌ 不采纳「大而全」base 层 |

## 建议下一步

1. 维持「仅迁入真正跨主题、无 Spring 依赖的契约」原则
2. 新工具先放主题模块，多次复用后再评估是否下沉 `rose-core`
