# Rose SQLite

Spring Data JDBC 的 **SQLite 方言**与类型转换，通过 `spring.factories` 注册 `DialectResolver`。

## 子模块

| Artifact | 说明 |
|----------|------|
| `rose-sqlite` | 单 jar |

## 已实现

| 能力 | 说明 |
|------|------|
| `SqliteDialect` / `JdbcSqliteDialect` | JDBC 方言实现 |
| `SqliteJdbcDialectProvider` | `META-INF/spring.factories` 自动发现 |
| `NumberToBooleanConverter` | SQLite 0/1 ↔ Boolean |
| `TimestampAtUtcToOffsetDateTimeConverter` | 时间类型映射 |

## 消费方式

```xml
<dependencyManagement>
    <!-- import rose-bom -->
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-sqlite</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
    </dependency>
</dependencies>
```

```yaml
spring:
  datasource:
    url: jdbc:sqlite:./data/app.db
  sql:
    init:
      mode: always   # 按需
```

`SqliteJdbcDialectProvider` 通过 `spring.factories` 自动注册，无需 `@Configuration`。

## 未实现 / 规划中

- Spring Boot starter（当前需手动依赖 + `spring-boot-starter-data-jdbc`）
- JPA / Hibernate SQLite 支持
- Dev Services SQLite 连接器（本地嵌入式库场景）
- Flyway / Liquibase SQLite 专用迁移说明

## 对标 Arconia

Arconia Dev Services 提供多种容器化数据库；SQLite 为嵌入式场景，Arconia 通常不单独提供 Dev Service。Rose 本模块聚焦 **方言**，非 Dev Services。

## 对标 Microsphere

无 `microsphere-sqlite` 对标。Rose 将 SQLite 作为轻量本地/测试持久化补充。

## 建议下一步

1. 若与 `rose-dev-services` 组合需求出现，再评估嵌入式 vs 容器化策略
