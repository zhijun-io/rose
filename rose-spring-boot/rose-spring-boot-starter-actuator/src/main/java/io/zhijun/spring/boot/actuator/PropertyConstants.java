package io.zhijun.spring.boot.actuator;

import static io.zhijun.spring.boot.constants.PropertyConstants.ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX;

/**
 * Actuator 配置属性常量
 */
public interface PropertyConstants {

    String ROSE_SPRING_BOOT_ACTUATOR_PROPERTY_NAME_PREFIX = ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "actuator.";

    String TASK_SCHEDULER_PROPERTY_NAME_PREFIX = ROSE_SPRING_BOOT_ACTUATOR_PROPERTY_NAME_PREFIX + "task-scheduler.";
}
