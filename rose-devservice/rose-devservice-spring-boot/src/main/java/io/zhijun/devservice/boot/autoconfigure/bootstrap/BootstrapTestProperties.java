package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Test mode bootstrap properties.
 */
@ConfigurationProperties(prefix = BootstrapTestProperties.CONFIG_PREFIX)
public class BootstrapTestProperties {

    public static final String CONFIG_PREFIX = "rose.test";

    public static final String PROFILES_PROPERTY = CONFIG_PREFIX + ".profiles";

    private List<String> profiles = new ArrayList<String>(Collections.singletonList("test"));

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }
}
