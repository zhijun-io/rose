package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import io.zhijun.devservice.core.bootstrap.BootstrapMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves active bootstrap profiles for the current mode.
 */
public final class BootstrapProfilesTemplate {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapProfilesTemplate.class);

    public List<String> resolve(
            BootstrapMode mode,
            List<String> currentProfiles,
            List<String> developmentProfiles,
            List<String> testProfiles) {
        List<String> additionalProfiles = new ArrayList<String>();
        switch (mode) {
            case DEV:
                addProfiles(currentProfiles, additionalProfiles, developmentProfiles);
                break;
            case TEST:
                addProfiles(currentProfiles, additionalProfiles, testProfiles);
                break;
            default:
                break;
        }
        return additionalProfiles;
    }

    private static void addProfiles(
            List<String> currentProfiles, List<String> additionalProfiles, List<String> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }
        for (String profile : profiles) {
            if (StringUtils.hasText(profile) && !currentProfiles.contains(profile)) {
                logger.debug("Adding active profile '{}' for bootstrap mode", profile);
                additionalProfiles.add(profile);
            }
        }
    }
}
