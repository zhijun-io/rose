package io.zhijun.dev.autoconfigure.bootstrap.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Test mode bootstrap properties.
 */
@ConfigurationProperties(prefix = BootstrapTestProperties.CONFIG_PREFIX)
public class BootstrapTestProperties {

    public static final String CONFIG_PREFIX = "rose.test";

    private List<String> profiles = new ArrayList<String>(Arrays.asList("test"));

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }
}
