package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

/**
 * Resolves dependent bean names from an annotated injection point.
 *
 * <p>This interface defines a single method that accepts an {@link AnnotatedElement}
 * instead of separate overloads for {@code Field}, {@code Method}, {@code Constructor},
 * and {@code Parameter}. This keeps the API surface small and leaves the type dispatch
 * to {@link AbstractInjectionPointDependencyResolver}, which implements the Template
 * Method pattern via {@code instanceof} dispatch.
 *
 * <h3>Design Rationale</h3>
 * <ul>
 *   <li><b>Single method vs overloads:</b> Four separate {@code resolve(Field, ...)},
 *   {@code resolve(Method, ...)}, etc. overloads complicate the composite pattern
 *   ({@link InjectionPointDependencyResolvers}) and are harder to implement correctly.
 *   A single {@code resolve(AnnotatedElement, ...)} method avoids overload ambiguity
 *   and allows the abstract base to centralize the dispatch logic.</li>
 *   <li><b>Template Method:</b> {@link AbstractInjectionPointDependencyResolver}
 *   implements the interface and dispatches to protected methods
 *   ({@code resolveField}, {@code resolveMethod}, {@code resolveConstructor},
 *   {@code resolveParameter}). Subclasses override only the hook methods they need
 *   rather than re-implementing dispatch.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Implement the interface directly (custom dispatch):
 * public class MyResolver implements InjectionPointDependencyResolver {
 *     public void resolve(AnnotatedElement element, ...) { ... }
 * }
 *
 * // Or extend the abstract base (Template Method):
 * public class MyResolver extends AbstractInjectionPointDependencyResolver {
 *     protected void resolveField(Field field, ...) { ... }
 * }
 * }</pre>
 *
 * @see AbstractInjectionPointDependencyResolver
 * @see InjectionPointDependencyResolvers
 * @since 1.0.0
 */
public interface InjectionPointDependencyResolver {

    void resolve(AnnotatedElement element, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);
}
