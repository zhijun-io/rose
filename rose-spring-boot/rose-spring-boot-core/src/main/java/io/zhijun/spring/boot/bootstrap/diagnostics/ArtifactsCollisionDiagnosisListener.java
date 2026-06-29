package io.zhijun.spring.boot.bootstrap.diagnostics;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import io.zhijun.core.annotation.Internal;
import io.zhijun.spring.boot.constants.PropertyConstants;
import io.zhijun.spring.boot.bootstrap.diagnostics.internal.ClasspathMavenArtifactScanner;

/**
 * Internal Boot listener that detects duplicate Maven coordinates on the classpath during application startup.
 */
@Internal
public final class ArtifactsCollisionDiagnosisListener
        implements ApplicationListener<ApplicationContextInitializedEvent> {

    public static final String ENABLED_PROPERTY = PropertyConstants.ARTIFACTS_COLLISION_ENABLED_PROPERTY_NAME;

    private static final Logger logger = LoggerFactory.getLogger(ArtifactsCollisionDiagnosisListener.class);

    private final ClasspathMavenArtifactScanner scanner = new ClasspathMavenArtifactScanner();

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        if (!isEnabled(event)) {
            return;
        }
        ClassLoader classLoader = event.getSpringApplication().getClassLoader();
        try {
            Set<String> collisions = scanner.findCollidingCoordinates(classLoader);
            if (collisions.isEmpty()) {
                return;
            }
            logger.error("Classpath artifact collision detected: {}", collisions);
            throw new ArtifactsCollisionException(
                    "Classpath artifact collision detected for coordinates: "
                            + StringUtils.collectionToCommaDelimitedString(collisions),
                    collisions);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to scan classpath for artifact collisions", ex);
        }
    }

    private static boolean isEnabled(ApplicationContextInitializedEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        ConfigurableEnvironment environment = context.getEnvironment();
        return environment
                .getProperty(ENABLED_PROPERTY, Boolean.class, Boolean.FALSE)
                .booleanValue();
    }
}
