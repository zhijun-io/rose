/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.zhijun.spring.config.env;

import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.zhijun.spring.core.PropertyConstants.ROSE_SPRING_PROPERTY_NAME_PREFIX;
import static io.zhijun.spring.core.PropertyConstants.ENABLED_PROPERTY_NAME;

/**
 * The template class for component that is enabled or disabled based on {@link Environment}.
 *
 * @see Environment
 * @since 1.0.0
 */
public abstract class EnvironmentEnabled {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Logger getLogger() {
        return logger;
    }

    /**
     * Checks if this component is enabled based on the given {@link Environment}.
     *
     * @param environment the Spring {@link Environment} to check against, must not be {@code null}
     * @return {@code true} if the component is enabled, {@code false} otherwise
     * @see #getEnabledPropertyName()
     * @see #getDefaultEnabled()
     */
    public boolean isEnabled(Environment environment) {
        String enabledPropertyName = getEnabledPropertyName();
        boolean enabled = environment.getProperty(enabledPropertyName, boolean.class, getDefaultEnabled());
        Logger log = getLogger();
        if (enabled) {
            if (log.isTraceEnabled()) {
                log.trace("The {} is enabled, if it needs to be disabled[default : '{}'], please set the property '{}' to 'false' .",
                        getClass().getSimpleName(), getDefaultEnabled(), getEnabledPropertyName());
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("The {} is disabled, if it needs to be enabled[default : '{}'], please set the property '{}' to 'true' .",
                        getClass().getSimpleName(), getDefaultEnabled(), getEnabledPropertyName());
            }
        }
        return enabled;
    }

    /**
     * Gets the property name used to determine if this component is enabled.
     *
     * @return the property name key for checking the enabled status
     */
    public String getEnabledPropertyName() {
        String className = this.getClass().getSimpleName();
        return ROSE_SPRING_PROPERTY_NAME_PREFIX + className + '.' + ENABLED_PROPERTY_NAME;
    }

    /**
     * Gets the default enabled status for this component.
     *
     * @return {@code true} if the component is enabled by default, {@code false} otherwise
     * @see #isEnabled(Environment)
     */
    public boolean getDefaultEnabled() {
        return true;
    }
}
