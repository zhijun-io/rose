package io.zhijun.spring.boot.event;

import io.zhijun.core.classloading.Artifact;
import io.zhijun.spring.boot.diagnostics.ArtifactsCollisionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.zhijun.spring.boot.constants.PropertyConstants.*;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.getBoolean;

/**
 * Spring Boot 启动监听器，用于在应用启动时检测并禁止加载指定构件 (artifact)。
 *
 * <p>在设计上，本监听器通过系统属性控制，而非 Spring Environment，因为
 * {@link #starting(ConfigurableBootstrapContext)} 阶段 Environment 尚未就绪。
 * 使用方式：通过 JDK 系统属性启用并配置需要禁止的构件列表。
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * # 启用并配置禁止加载的构件
 * -Drose.spring.boot.banned-artifacts.enabled=true
 * -Drose.spring.boot.banned-artifacts=com.example:bad-lib,com.example:another-lib
 *
 * # 模式匹配 (使用 * 匹配任意部分)
 * -Drose.spring.boot.banned-artifacts=com.example:*,*:bad-lib
 * }</pre>
 *
 * <p>当检测到被禁构件存在于 classpath 时，抛出 {@link ArtifactsCollisionException}，
 * 引导用户通过 Maven dependency exclusion 解决。
 *
 * @see io.zhijun.core.classloading.ArtifactDetector
 * @see ArtifactsCollisionException
 */
public class BannedArtifactClassLoadingListener extends SpringApplicationRunListenerAdapter implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(BannedArtifactClassLoadingListener.class);

    /**
     * 已处理标记缓存，确保单个 JVM 进程内每个 SpringApplication 只处理一次
     */
    private static final ConcurrentMap<SpringApplication, Boolean> processedMap = new ConcurrentHashMap<>();

    public BannedArtifactClassLoadingListener(SpringApplication springApplication, String... args) {
        super(springApplication, args);
        setOrder(HIGHEST_PRECEDENCE);
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        if (isProcessed()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Banned artifact checking already performed for this application");
            }
            return;
        }
        if (bannedArtifactsEnabled()) {
            banArtifacts();
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Banned artifact checking disabled (system property '{}' is missing or false)",
                    BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
            }
        }
        markProcessed();
    }

    /**
     * 检查当前 SpringApplication 是否已处理过
     */
    boolean isProcessed() {
        return TRUE.equals(processedMap.get(getSpringApplication()));
    }

    private boolean bannedArtifactsEnabled() {
        return getBoolean(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
    }

    private void markProcessed() {
        processedMap.put(getSpringApplication(), TRUE);
    }

    private void banArtifacts() {
        SpringApplication springApplication = getSpringApplication();
        ClassLoader classLoader = springApplication.getClassLoader();

        String bannedArtifactsProperty = System.getProperty(BANNED_ARTIFACTS_PROPERTY_NAME, "");
        if (!StringUtils.hasText(bannedArtifactsProperty)) {
            logger.warn("Banned artifact checking enabled but no artifacts configured via system property '{}'. " +
                "Use comma-separated format: groupId:artifactId", BANNED_ARTIFACTS_PROPERTY_NAME);
            return;
        }

        List<ArtifactPattern> patterns = parsePatterns(bannedArtifactsProperty);
        if (patterns.isEmpty()) {
            return;
        }

        io.zhijun.core.classloading.ArtifactDetector detector = new io.zhijun.core.classloading.ArtifactDetector(classLoader);
        List<Artifact> detectedArtifacts = detector.detect();

        Set<String> matchedArtifacts = new LinkedHashSet<>();
        for (Artifact artifact : detectedArtifacts) {
            if (matchesAny(artifact, patterns)) {
                matchedArtifacts.add(artifact.getGroupId() + ":" + artifact.getArtifactId()
                    + ":" + artifact.getVersion() + " (" + artifact.getLocation() + ")");
            }
        }

        if (matchedArtifacts.isEmpty()) {
            logger.info("No banned artifacts found in classpath");
            return;
        }

        String message = buildErrorMessage(matchedArtifacts);
        logger.error(message);
        throw new ArtifactsCollisionException(message, matchedArtifacts);
    }

    /**
     * 解析逗号分隔的构件规则列表
     */
    private static List<ArtifactPattern> parsePatterns(String propertyValue) {
        String[] parts = StringUtils.tokenizeToStringArray(propertyValue, ",");
        List<ArtifactPattern> patterns = new ArrayList<>(parts.length);
        for (String part : parts) {
            String trimmed = part.trim();
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex < 0) {
                logger.warn("Invalid banned artifact pattern '{}', expected format: groupId:artifactId", trimmed);
                continue;
            }
            String groupIdPattern = trimmed.substring(0, colonIndex).trim();
            String artifactIdPattern = trimmed.substring(colonIndex + 1).trim();
            patterns.add(new ArtifactPattern(
                groupIdPattern.isEmpty() || "*".equals(groupIdPattern) ? null : groupIdPattern,
                artifactIdPattern.isEmpty() || "*".equals(artifactIdPattern) ? null : artifactIdPattern));
        }
        return patterns;
    }

    /**
     * 检查构件是否匹配任意一条规则
     */
    private static boolean matchesAny(Artifact artifact, List<ArtifactPattern> patterns) {
        for (ArtifactPattern pattern : patterns) {
            if (pattern.matches(artifact)) {
                return true;
            }
        }
        return false;
    }

    private static String buildErrorMessage(Set<String> matchedArtifacts) {
        StringBuilder message = new StringBuilder("Banned artifacts detected in classpath:");
        message.append(System.lineSeparator());
        for (String artifact : matchedArtifacts) {
            message.append("  - ").append(artifact).append(System.lineSeparator());
        }
        message.append(System.lineSeparator());
        message.append("Action: Exclude these dependencies in your pom.xml, for example:");
        message.append(System.lineSeparator());
        message.append("  <exclusion>");
        message.append(System.lineSeparator());
        message.append("    <groupId>...</groupId>");
        message.append(System.lineSeparator());
        message.append("    <artifactId>...</artifactId>");
        message.append(System.lineSeparator());
        message.append("  </exclusion>");
        message.append(System.lineSeparator());
        message.append("Or disable this check with -D").append(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME).append("=false");
        return message.toString();
    }

    /**
     * 构件匹配规则，支持通配符（null 字段表示匹配任意值）
     */
    private static class ArtifactPattern {

        private final String groupIdPattern;

        private final String artifactIdPattern;

        ArtifactPattern(String groupIdPattern, String artifactIdPattern) {
            this.groupIdPattern = groupIdPattern;
            this.artifactIdPattern = artifactIdPattern;
        }

        boolean matches(Artifact artifact) {
            return (groupIdPattern == null || groupIdPattern.equals(artifact.getGroupId()))
                && (artifactIdPattern == null || artifactIdPattern.equals(artifact.getArtifactId()));
        }
    }
}
