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
package io.zhijun.spring.constants;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;

import static java.lang.Boolean.parseBoolean;

/**
 * The Property constants for Rose Spring
 *
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Rose Spring : "rose.spring."
     */
    String ROSE_PROPERTY_NAME_PREFIX = "rose.";

    /**
     * The property name prefix of Rose Spring : "rose.spring."
     */
    String ROSE_SPRING_PROPERTY_NAME_PREFIX = ROSE_PROPERTY_NAME_PREFIX + "spring.";

    /**
     * The property name of enabled : "enabled"
     */
    String ENABLED_PROPERTY_NAME = "enabled";

    /**
     * The char of "@"
     */
    String AT_CHAR = "@";

    /** The char of "." */
    String DOT_CHAR = ".";

    /**
     * The property name prefix of the configuration property prefix : "rose.spring.prefix."
     */
    String PREFIX_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_NAME_PREFIX + "prefix.";

    /**
     * The property name prefix of beans : "rose.spring.beans."
     */
    String BEANS_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_NAME_PREFIX + "beans.";

    /**
     * The property name prefix of {@link ConfigurableApplicationContextInitializer} : "rose.spring.context-initializer."
     *
     * @see ConfigurableApplicationContextInitializer
     * @see ApplicationContextInitializer
     */
    String APPLICATION_CONTEXT_INITIALIZER_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_NAME_PREFIX + "context-initializer.";

    /**
     * The property name suffix of auto registered : "auto-registered"
     */
    String AUTO_REGISTERED_PROPERTY_NAME_SUFFIX = "auto-registered";

    /**
     * The default value of property of auto registered : "true"
     */
    String DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE = "true";

    /**
     * The default value of auto registered : true
     */
    boolean DEFAULT_AUTO_REGISTERED_VALUE = parseBoolean(DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE);
}