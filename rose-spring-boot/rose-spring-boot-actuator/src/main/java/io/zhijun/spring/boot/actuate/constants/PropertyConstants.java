package io.zhijun.spring.boot.actuate.constants;

import static io.zhijun.spring.boot.constants.PropertyConstants.ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX;

/**
 * Property name constants for Rose Spring Boot Actuator.
 */
public interface PropertyConstants {

    /**
     * Property prefix {@code rose.actuator.}.
     */
    String ROSE_ACTUATOR_PROPERTY_NAME_PREFIX = "rose.actuator.";

    /**
     * Property prefix {@code rose.actuator.task-scheduler.}.
     */
    String TASK_SCHEDULER_PROPERTY_NAME_PREFIX = ROSE_ACTUATOR_PROPERTY_NAME_PREFIX + "task-scheduler.";

    /**
     * Canonical Rose Spring Boot Actuator prefix {@code rose.spring.boot.actuator.}.
     */
    String ROSE_SPRING_BOOT_ACTUATOR_PROPERTY_NAME_PREFIX = ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "actuator.";
}
