package io.zhijun.devservice.boot.autoconfigure.bootstrap.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Development mode bootstrap properties.
 */
@ConfigurationProperties(prefix = BootstrapDevProperties.CONFIG_PREFIX)
public class BootstrapDevProperties {

    public static final String CONFIG_PREFIX = "rose.dev";

    public static final String PROFILES_PROPERTY = CONFIG_PREFIX + ".profiles";

    private List<String> profiles = new ArrayList<String>(Arrays.asList("dev"));

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }
}
