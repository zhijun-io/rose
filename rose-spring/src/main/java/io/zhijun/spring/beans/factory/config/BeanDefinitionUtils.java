package io.zhijun.spring.beans.factory.config;

import io.zhijun.core.annotation.Nonnull;
import io.zhijun.core.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

import java.util.function.Consumer;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;

public abstract class BeanDefinitionUtils {
    private static final Logger log = LoggerFactory.getLogger(BeanDefinitionUtils.class);

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the given bean type.
     *
     * <p>This method is a convenience wrapper that calls
     * {@link #genericBeanDefinition(Class, int, Object[])} with default role
     * ({@link BeanDefinition#ROLE_APPLICATION}) and no constructor arguments.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType);
     * }</pre>
     *
     * @param beanType the type of bean
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType) {
        return genericBeanDefinition(beanType, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition}
     *
     * <p>This method creates a bean definition for the specified bean type with optional constructor arguments.
     * It internally uses {@link BeanDefinitionBuilder} to construct the bean definition and sets the provided
     * constructor arguments via constructor injection.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * Object[] constructorArgs = new Object[]{"arg1", 123};
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, constructorArgs);
     * }</pre>
     *
     * <p>The above example will create a bean definition for the class {@code MyService}, passing in two constructor arguments.
     *
     * @param beanType             the type of bean
     * @param constructorArguments the arguments of Bean Classes' constructor
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, Object... constructorArguments) {
        return genericBeanDefinition(beanType, ROLE_APPLICATION, constructorArguments);
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the specified bean type and role.
     *
     * <p>This method is a convenience wrapper that calls
     * {@link #genericBeanDefinition(Class, int, Object[])} with no constructor arguments.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * int role = BeanDefinition.ROLE_APPLICATION;
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, role);
     * }</pre>
     *
     * @param beanType the type of bean
     * @param role     the role of the bean definition (e.g., application or infrastructure)
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role) {
        return genericBeanDefinition(beanType, role, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the specified bean type, role, and constructor arguments.
     *
     * <p>This method uses {@link BeanDefinitionBuilder} to construct a bean definition for the given bean type,
     * sets its role, and injects any provided constructor arguments via constructor injection.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Bean Definition</h4>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * int role = BeanDefinition.ROLE_APPLICATION;
     * Object[] constructorArgs = new Object[]{"arg1", 123};
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, role, constructorArgs);
     * }</pre>
     *
     * <p>The above example creates a bean definition for the class {@code MyService}, assigning it the role of an application bean,
     * and passing in two constructor arguments.
     *
     * <h4>No Constructor Arguments</h4>
     * <pre>{@code
     * Class<?> beanType = MyRepository.class;
     * int role = BeanDefinition.ROLE_INFRASTRUCTURE;
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, role);
     * }</pre>
     *
     * <p>In this case, no constructor arguments are provided, so the default constructor will be used.
     *
     * @param beanType             the type of bean to define
     * @param role                 the role of the bean definition (e.g., application or infrastructure)
     * @param constructorArguments the arguments to pass to the bean's constructor, if any
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role, Object[] constructorArguments) {
        return genericBeanDefinition(beanType, builder -> {
            // set the role
            builder.setRole(role);
            // Add the arguments of constructor if present
            int length = ArrayUtils.getLength(constructorArguments);
            for (int i = 0; i < length; i++) {
                Object constructorArgument = constructorArguments[i];
                builder.addConstructorArgValue(constructorArgument);
            }
        });
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the given bean type and builder consumer.
     *
     * @param beanType        the type of bean
     * @param builderConsumer the consumer to customize the {@link BeanDefinitionBuilder}
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, Consumer<BeanDefinitionBuilder> builderConsumer) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanType);
        builderConsumer.accept(beanDefinitionBuilder);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        if (log.isTraceEnabled()) {
            log.trace("Build an instance of {}", beanDefinition);
        }
        return beanDefinition;
    }

    @Nullable
    public static Class<?> resolveBeanType(BeanDefinition beanDefinition) {
        return resolveBeanType(beanDefinition, null);
    }

    @Nullable
    public static Class<?> resolveBeanType(BeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        Class<?> beanClass = resolveBeanTypeFromRootBeanDefinition(beanDefinition);
        if (beanClass != null) {
            return beanClass;
        }
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName != null) {
            try {
                if (classLoader != null) {
                    return Class.forName(beanClassName, false, classLoader);
                }
                return Class.forName(beanClassName);
            } catch (ClassNotFoundException | LinkageError e) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    private static Class<?> resolveBeanTypeFromRootBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof RootBeanDefinition) {
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            if (resolvableType != null) {
                return resolvableType.resolve();
            }
        }
        return null;
    }

    public static void setBeanDefinitionClass(BeanDefinition beanDefinition, String beanClassName,
                                              @Nullable Consumer<AbstractBeanDefinition> abstractBeanDefinitionConsumer) {
        beanDefinition.setBeanClassName(beanClassName);
        if (beanDefinition instanceof AbstractBeanDefinition && abstractBeanDefinitionConsumer != null) {
            abstractBeanDefinitionConsumer.accept((AbstractBeanDefinition) beanDefinition);
        }
    }

    public static BeanDefinitionBuilder createBeanDefinitionBuilder(String beanClassName) {
        return BeanDefinitionBuilder.genericBeanDefinition(beanClassName);
    }

    public static BeanDefinitionBuilder createBeanDefinitionBuilder(Class<?> beanType) {
        return BeanDefinitionBuilder.genericBeanDefinition(beanType);
    }
}
