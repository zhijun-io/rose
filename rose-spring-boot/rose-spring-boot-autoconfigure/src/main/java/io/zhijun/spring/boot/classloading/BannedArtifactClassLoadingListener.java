package io.zhijun.spring.boot.classloading;

import io.zhijun.spring.boot.event.SpringApplicationRunListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.zhijun.spring.boot.constants.PropertyConstants.ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.getBoolean;

/**
 * 禁止加载指定冲突 Artifact 的监听器
 */
public class BannedArtifactClassLoadingListener extends SpringApplicationRunListenerAdapter implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(BannedArtifactClassLoadingListener.class);

    public static final String BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME =
            ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "banned-artifacts.enabled";

    private static final ConcurrentMap<SpringApplication, Boolean> processedMap = new ConcurrentHashMap<>();

    public BannedArtifactClassLoadingListener(SpringApplication springApplication, String... args) {
        super(springApplication, args);
        setOrder(HIGHEST_PRECEDENCE);
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        if (isProcessed()) {
            return;
        }
        if (bannedArtifactsEnabled()) {
            banArtifacts();
        }
        markProcessed();
    }

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
        logger.info("Banned artifacts checking enabled for application: {}", springApplication.getMainApplicationClass());
    }
}
