package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for {@link InjectionPointDependencyResolver} implementations.
 *
 * <p>Implements the Template Method pattern via {@code instanceof} dispatch:
 * the single {@link #resolve(AnnotatedElement, ConfigurableListableBeanFactory, Set)}
 * method delegates to one of four hook methods based on the runtime type of the
 * injection point element:
 * <ul>
 *   <li>{@link #resolveField(Field, ConfigurableListableBeanFactory, Set)}</li>
 *   <li>{@link #resolveMethod(Method, ConfigurableListableBeanFactory, Set)}</li>
 *   <li>{@link #resolveConstructor(Constructor, ConfigurableListableBeanFactory, Set)}</li>
 *   <li>{@link #resolveParameter(Parameter, ConfigurableListableBeanFactory, Set)}</li>
 * </ul>
 *
 * <h3>Design Rationale</h3>
 * <p>Before this refactoring, subclasses overrode individual {@code resolve(Field, ...)},
 * {@code resolve(Parameter, ...)}, etc. overloads declared on the interface. This required
 * the interface to carry four overloads, making the composite pattern
 * ({@link InjectionPointDependencyResolvers}) harder to implement correctly and creating
 * ambiguity when a single resolver needed custom dispatch logic.</p>
 *
 * <p>The new design uses a single {@code resolve(AnnotatedElement, ...)} method on the
 * interface, and this abstract class provides the {@code instanceof} dispatch to hook
 * methods. Subclasses no longer deal with type dispatch — they simply override the
 * relevant {@code resolveField}/ {@code resolveMethod}/ {@code resolveConstructor}/
 * {@code resolveParameter} method.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * public class FieldOnlyResolver extends AbstractInjectionPointDependencyResolver {
 *     protected void resolveField(Field field, ...) {
 *         String[] beanNames = getBeanFactory().getBeanNamesForType(field.getType());
 *         dependentBeanNames.addAll(Arrays.asList(beanNames));
 *     }
 *     // resolveMethod, resolveConstructor, resolveParameter are no-op by default
 * }
 * }</pre>
 *
 * <p>Subclasses that extend {@link AnnotatedInjectionPointDependencyResolver} (for
 * annotation-based resolution) already inherit this dispatch and only need to override
 * the hook methods as needed.</p>
 *
 * @see InjectionPointDependencyResolver
 * @see AnnotatedInjectionPointDependencyResolver
 * @since 1.0.0
 */
public abstract class AbstractInjectionPointDependencyResolver implements InjectionPointDependencyResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurableListableBeanFactory beanFactory;

    private final Set<Class<?>> frameworkProvidedTypes;

    public AbstractInjectionPointDependencyResolver(ConfigurableListableBeanFactory beanFactory, Set<Class<?>> frameworkProvidedTypes) {
        this.beanFactory = beanFactory;
        this.frameworkProvidedTypes = frameworkProvidedTypes;
    }

    public AbstractInjectionPointDependencyResolver(ConfigurableListableBeanFactory beanFactory) {
        this(beanFactory, Collections.emptySet());
    }

    protected AbstractInjectionPointDependencyResolver() {
        this.beanFactory = null;
        this.frameworkProvidedTypes = Collections.emptySet();
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Set<Class<?>> getFrameworkProvidedTypes() {
        return frameworkProvidedTypes;
    }

    /**
     * Dispatches to the appropriate hook method based on the runtime type of {@code element}.
     *
     * <p>Implementation of the Template Method pattern:
     * <ul>
     *   <li>{@link Field} → {@link #resolveField(Field, ConfigurableListableBeanFactory, Set)}</li>
     *   <li>{@link Method} → {@link #resolveMethod(Method, ConfigurableListableBeanFactory, Set)}</li>
     *   <li>{@link Constructor} → {@link #resolveConstructor(Constructor, ConfigurableListableBeanFactory, Set)}</li>
     *   <li>{@link Parameter} → {@link #resolveParameter(Parameter, ConfigurableListableBeanFactory, Set)}</li>
     * </ul>
     * Unrecognized element types are silently ignored.
     *
     * @param element            the injection point element
     * @param beanFactory        the bean factory to use for bean lookup
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(AnnotatedElement element, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        if (element instanceof Field) {
            resolveField((Field) element, beanFactory, dependentBeanNames);
        } else if (element instanceof Method) {
            resolveMethod((Method) element, beanFactory, dependentBeanNames);
        } else if (element instanceof Constructor) {
            resolveConstructor((Constructor<?>) element, beanFactory, dependentBeanNames);
        } else if (element instanceof Parameter) {
            resolveParameter((Parameter) element, beanFactory, dependentBeanNames);
        }
    }

    /**
     * Hook method for resolving dependency bean names from a {@link Field} injection point.
     *
     * <p>Called by {@link #resolve(AnnotatedElement, ConfigurableListableBeanFactory, Set)}
     * when the element is an instance of {@link Field}. Default implementation is a no-op;
     * subclasses override this method to provide resolution logic for field injection points.</p>
     *
     * @param field              the field injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to use for bean lookup
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    protected void resolveField(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    /**
     * Hook method for resolving dependency bean names from a {@link Method} injection point.
     *
     * <p>Called by {@link #resolve(AnnotatedElement, ConfigurableListableBeanFactory, Set)}
     * when the element is an instance of {@link Method}. Default implementation is a no-op;
     * subclasses override this method to provide resolution logic for method injection points.</p>
     *
     * @param method             the method injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to use for bean lookup
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    protected void resolveMethod(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    /**
     * Hook method for resolving dependency bean names from a {@link Constructor} injection point.
     *
     * <p>Called by {@link #resolve(AnnotatedElement, ConfigurableListableBeanFactory, Set)}
     * when the element is an instance of {@link Constructor}. Default implementation is a no-op;
     * subclasses override this method to provide resolution logic for constructor injection points.</p>
     *
     * @param constructor        the constructor injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to use for bean lookup
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    protected void resolveConstructor(Constructor<?> constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    /**
     * Hook method for resolving dependency bean names from a {@link Parameter} injection point.
     *
     * <p>Called by {@link #resolve(AnnotatedElement, ConfigurableListableBeanFactory, Set)}
     * when the element is an instance of {@link Parameter}. Default implementation is a no-op;
     * subclasses override this method to provide resolution logic for method/constructor parameter
     * injection points.</p>
     *
     * @param parameter          the parameter injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to use for bean lookup
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    protected void resolveParameter(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    @Nullable
    protected Class<?> resolveDependentType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            List<Type> arguments = resolveActualTypeArguments(type);
            Type argumentType = arguments.get(arguments.size() - 1);
            return resolveDependentType(argumentType);
        }
        return null;
    }

    private static List<Type> resolveActualTypeArguments(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            List<Type> result = new ArrayList<>(actualTypeArguments.length);
            Collections.addAll(result, actualTypeArguments);
            return result;
        }
        return Collections.emptyList();
    }

    protected boolean isFrameworkProvidedType(Class<?> type) {
        if (frameworkProvidedTypes.isEmpty()) {
            return false;
        }
        for (Class<?> t : frameworkProvidedTypes) {
            if (t.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

}

