package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.beans.BeanDefinitionUtils;
import io.zhijun.spring.beans.factory.filter.ResolvableDependencyTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.StopWatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ClassUtils.getMostSpecificMethod;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;
import static org.springframework.util.ReflectionUtils.doWithLocalMethods;

/**
 * 默认 {@link BeanDependencyResolver} 实现。
 * 解析 Bean 的构造函数、字段、方法参数以及 BeanDefinition 中的依赖关系。
 */
public class DefaultBeanDependencyResolver implements BeanDependencyResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBeanDependencyResolver.class);

    private static final ThreadLocal<Set<Member>> resolvedBeanMembersHolder =
            ThreadLocal.withInitial(LinkedHashSet::new);

    private final DefaultListableBeanFactory beanFactory;

    private final ClassLoader classLoader;

    private final ResolvableDependencyTypeFilter resolvableDependencyTypeFilter;

    private final InjectionPointDependencyResolvers resolvers;

    private final List<SmartInstantiationAwareBeanPostProcessor> smartInstantiationAwareBeanPostProcessors;

    private final ExecutorService executorService;

    public DefaultBeanDependencyResolver(BeanFactory bf, ExecutorService executorService) {
        this.beanFactory = asDefaultListableBeanFactory(bf);
        this.classLoader = this.beanFactory.getBeanClassLoader();
        this.resolvableDependencyTypeFilter = new ResolvableDependencyTypeFilter(beanFactory);
        this.resolvers = new InjectionPointDependencyResolvers(beanFactory);
        this.smartInstantiationAwareBeanPostProcessors = getSmartInstantiationAwareBeanPostProcessors(beanFactory);
        this.executorService = executorService;
    }

    @Override
    public Map<String, Set<String>> resolve(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (beanFactory != bf) {
            if (logger.isWarnEnabled()) {
                logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            }
            return emptyMap();
        }

        StopWatch stopWatch = new StopWatch("BeanDependencyResolver");

        Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap = getEligibleBeanDefinitionsMap(beanFactory, stopWatch);

        preProcessLoadBeanClasses(eligibleBeanDefinitionsMap, stopWatch);

        Map<String, Set<String>> dependentBeanNamesMap = resolveDependentBeanNamesMap(eligibleBeanDefinitionsMap, stopWatch);

        flattenDependentBeanNamesMap(dependentBeanNamesMap, stopWatch);

        clearResolvedBeanMembers();

        if (logger.isTraceEnabled()) {
            logger.trace(stopWatch.toString());
        }

        return dependentBeanNamesMap;
    }

    @Override
    public Set<String> resolve(String beanName, RootBeanDefinition mergedBeanDefinition, ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (beanFactory != bf) {
            if (logger.isWarnEnabled()) {
                logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            }
            return emptySet();
        }
        return resolveDependentBeanNames(beanName, mergedBeanDefinition, beanFactory);
    }

    private Map<String, Set<String>> resolveDependentBeanNamesMap(
            Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap, StopWatch stopWatch) {
        stopWatch.start("resolveDependentBeanNamesMap");

        int beansCount = eligibleBeanDefinitionsMap.size();
        final Map<String, Set<String>> dependentBeanNamesMap = new HashMap<>(beansCount);

        CompletionService<Entry<String, Set<String>>> completionService = getEntryCompletionService(eligibleBeanDefinitionsMap);

        for (int i = 0; i < beansCount; i++) {
            try {
                Future<Entry<String, Set<String>>> future = completionService.take();
                Entry<String, Set<String>> entry = future.get();
                dependentBeanNamesMap.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to resolve dependent bean names", e);
                }
            }
        }

        stopWatch.stop();
        return dependentBeanNamesMap;
    }

    private CompletionService<Entry<String, Set<String>>> getEntryCompletionService(
            Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap) {
        CompletionService<Entry<String, Set<String>>> completionService =
                new ExecutorCompletionService<>(this.executorService);

        for (Entry<String, RootBeanDefinition> entry : eligibleBeanDefinitionsMap.entrySet()) {
            completionService.submit(() -> {
                String beanName = entry.getKey();
                RootBeanDefinition beanDefinition = entry.getValue();
                Set<String> dependentBeanNames = resolve(beanName, beanDefinition, beanFactory);
                return new AbstractMap.SimpleImmutableEntry<>(beanName, dependentBeanNames);
            });
        }
        return completionService;
    }

    private void preProcessLoadBeanClasses(Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap, StopWatch stopWatch) {
        stopWatch.start("preProcessLoadBeanClasses");

        for (Entry<String, RootBeanDefinition> entry : eligibleBeanDefinitionsMap.entrySet()) {
            String beanName = entry.getKey();
            RootBeanDefinition beanDefinition = entry.getValue();
            preProcessLoadBeanClass(beanName, beanDefinition, eligibleBeanDefinitionsMap, classLoader);
        }
        awaitTasksCompleted();

        stopWatch.stop();
    }

    private void awaitTasksCompleted() {
        try {
            while (!executorService.awaitTermination(10, TimeUnit.MILLISECONDS)) {
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void preProcessLoadBeanClass(String beanName, RootBeanDefinition beanDefinition,
                                         Map<String, RootBeanDefinition> beanDefinitionsMap, ClassLoader classLoader) {
        if (beanDefinition.hasBeanClass()) {
            return;
        }
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName == null) {
            if (beanDefinition.getResolvedFactoryMethod() == null) {
                String factoryBeanName = beanDefinition.getFactoryBeanName();
                if (factoryBeanName != null) {
                    RootBeanDefinition factoryBeanDefinition = getMergedBeanDefinition(factoryBeanName, beanDefinitionsMap);
                    preProcessLoadBeanClass(factoryBeanName, factoryBeanDefinition, beanDefinitionsMap, classLoader);
                }
            }
        } else {
            executorService.execute(() -> {
                try {
                    Class<?> beanClass = forName(beanClassName, classLoader);
                    beanDefinition.setBeanClass(beanClass);
                    if (logger.isTraceEnabled()) {
                        logger.trace("The bean[name : '{}'] class[name : '{}'] was loaded", beanName, beanClassName);
                    }
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Failed to load bean class [{}] for bean [{}]", beanClassName, beanName, e);
                    }
                }
            });
        }
    }

    private RootBeanDefinition getMergedBeanDefinition(String beanName,
                                                       Map<String, RootBeanDefinition> beanDefinitionsMap) {
        RootBeanDefinition beanDefinition = beanDefinitionsMap.get(beanName);
        if (beanDefinition == null) {
            beanDefinition = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
        }
        return beanDefinition;
    }

    private void flattenDependentBeanNamesMap(Map<String, Set<String>> dependentBeanNamesMap, StopWatch stopWatch) {
        stopWatch.start("flattenDependentBeanNamesMap");

        for (Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            Set<String> dependentBeanNames = entry.getValue();
            if (dependentBeanNames.isEmpty()) {
                continue;
            }
            String beanName = entry.getKey();
            Set<String> flattenDependentBeanNames = new LinkedHashSet<>(dependentBeanNames.size() * 2);
            flatDependentBeanNames(beanName, dependentBeanNamesMap, flattenDependentBeanNames);
            entry.setValue(flattenDependentBeanNames);
        }

        Set<String> nonRootBeanNames = new LinkedHashSet<>();
        for (Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            String beanName = entry.getKey();
            Set<String> dependentBeanNames = entry.getValue();
            for (String dependentBeanName : dependentBeanNames) {
                Set<String> nestedDependentBeanNames = dependentBeanNamesMap.get(dependentBeanName);
                if (nestedDependentBeanNames != null && !nestedDependentBeanNames.isEmpty()
                        && !dependentBeanNames.containsAll(nestedDependentBeanNames)) {
                    nonRootBeanNames.add(beanName);
                    break;
                }
            }
        }

        for (String nonRootBeanName : nonRootBeanNames) {
            dependentBeanNamesMap.remove(nonRootBeanName);
        }

        stopWatch.stop();
    }

    private void flatDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap,
                                        Set<String> flattenDependentBeanNames) {
        Set<String> dependentBeanNames = retrieveDependentBeanNames(beanName, dependentBeanNamesMap);
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        dependentBeanNames.remove(beanName);
        for (String dependentBeanName : dependentBeanNames) {
            if (flattenDependentBeanNames.add(dependentBeanName)) {
                flatDependentBeanNames(dependentBeanName, dependentBeanNamesMap, flattenDependentBeanNames);
            }
        }
    }

    private Set<String> retrieveDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap) {
        Set<String> dependentBeanNames = dependentBeanNamesMap.get(beanName);
        return dependentBeanNames != null ? dependentBeanNames : emptySet();
    }

    private Set<String> resolveDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                  DefaultListableBeanFactory beanFactory) {
        Set<String> dependentBeanNames = new LinkedHashSet<>();
        resolveBeanDefinitionDependentBeanNames(beanDefinition, dependentBeanNames);
        resolveConstructionParametersDependentBeanNames(beanName, beanDefinition, beanFactory, dependentBeanNames);
        resolveInjectionPointsDependentBeanNames(beanName, beanDefinition, beanFactory, dependentBeanNames);
        dependentBeanNames.remove(beanName);
        removeReadyBeanNames(dependentBeanNames, beanFactory);
        return dependentBeanNames;
    }

    private void resolveInjectionPointsDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                          DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Class<?> beanClass = resolveBeanClass(beanDefinition, classLoader);
        if (beanClass == null) {
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            beanClass = resolvableType.resolve();
        }
        if (beanClass == null || beanClass.isInterface()) {
            return;
        }
        resolveFieldDependentBeanNames(beanName, beanClass, beanFactory, dependentBeanNames);
        resolveMethodParametersDependentBeanNames(beanName, beanClass, beanFactory, dependentBeanNames);
    }

    private void resolveMethodParametersDependentBeanNames(String beanName, Class<?> beanClass,
                                                           DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Class<?> targetClass = beanClass;
        while (targetClass != null && targetClass != Object.class) {
            doWithLocalMethods(targetClass, method -> {
                if (isStatic(method.getModifiers())) {
                    return;
                }
                int length = method.getParameterCount();
                if (length > 0) {
                    Method bridgedMethod = findBridgedMethod(method);
                    if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                        return;
                    }
                    if (method.equals(getMostSpecificMethod(method, beanClass))) {
                        if (!isBeanMemberResolved(method)) {
                            resolvers.resolve(method, beanFactory, dependentBeanNames);
                            addResolvedBeanMember(method);
                        }
                    }
                }
            });
            targetClass = targetClass.getSuperclass();
        }
    }

    private void resolveFieldDependentBeanNames(String beanName, Class<?> beanClass,
                                                DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Class<?> targetClass = beanClass;
        while (targetClass != null && targetClass != Object.class) {
            doWithLocalFields(targetClass, field -> {
                if (isStatic(field.getModifiers())) {
                    return;
                }
                if (!isBeanMemberResolved(field)) {
                    resolvers.resolve(field, beanFactory, dependentBeanNames);
                    addResolvedBeanMember(field);
                }
            });
            targetClass = targetClass.getSuperclass();
        }
    }

    private void removeReadyBeanNames(Set<String> dependentBeanNames, DefaultListableBeanFactory beanFactory) {
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        Iterator<String> iterator = dependentBeanNames.iterator();
        while (iterator.hasNext()) {
            String dependentBeanName = iterator.next();
            if (isBeanReady(dependentBeanName, beanFactory)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("The dependent bean name['{}'] is removed since it's ready!", dependentBeanName);
                }
                iterator.remove();
            }
        }
    }

    private void resolveBeanDefinitionDependentBeanNames(RootBeanDefinition beanDefinition, Set<String> dependentBeanNames) {
        String[] dependsOn = beanDefinition.getDependsOn();
        if (!isEmpty(dependsOn)) {
            dependentBeanNames.addAll(Arrays.asList(dependsOn));
        }

        MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
        PropertyValue[] propertyValues = mutablePropertyValues.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues) {
            Object value = propertyValue.getValue();
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
               dependentBeanNames.add(beanReference.getBeanName());
           }
        }

        String factoryBeanName = beanDefinition.getFactoryBeanName();
        if (factoryBeanName != null) {
            dependentBeanNames.add(factoryBeanName);
        }
    }

    private void resolveConstructionParametersDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                                  DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
        if (factoryMethod == null) {
            Class<?> beanClass = resolveBeanClass(beanDefinition, classLoader);
            if (beanClass == null) return;
            Constructor<?>[] constructors = resolveConstructors(beanName, beanClass);
            if (constructors.length != 1) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Bean[name : '{}' , class : {}] has {} constructors", beanName, beanClass, constructors.length);
                }
            } else {
                Constructor<?> constructor = constructors[0];
                if (!isBeanMemberResolved(constructor)) {
                    resolvers.resolve(constructor, beanFactory, dependentBeanNames);
                    addResolvedBeanMember(constructor);
                }
            }
        } else {
            if (!isBeanMemberResolved(factoryMethod)) {
                resolvers.resolve(factoryMethod, beanFactory, dependentBeanNames);
                addResolvedBeanMember(factoryMethod);
            }
        }
    }

    private Constructor<?>[] resolveConstructors(String beanName, Class<?> beanClass) {
        Constructor<?>[] constructors = null;
        if (!beanClass.isInterface()) {
            for (SmartInstantiationAwareBeanPostProcessor processor : smartInstantiationAwareBeanPostProcessors) {
                constructors = processor.determineCandidateConstructors(beanClass, beanName);
                if (constructors != null) {
                    break;
                }
            }
        }
        constructors = isEmpty(constructors) ? beanClass.getConstructors() : constructors;
        constructors = isEmpty(constructors) ? beanClass.getDeclaredConstructors() : constructors;
        return constructors;
    }

    private List<SmartInstantiationAwareBeanPostProcessor> getSmartInstantiationAwareBeanPostProcessors(
            ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) beanFactory;
            List<SmartInstantiationAwareBeanPostProcessor> processors = new LinkedList<>();
            for (BeanPostProcessor beanPostProcessor : dbf.getBeanPostProcessors()) {
                if (beanPostProcessor instanceof SmartInstantiationAwareBeanPostProcessor) {
                    SmartInstantiationAwareBeanPostProcessor siaBeanProcessor = (SmartInstantiationAwareBeanPostProcessor) beanPostProcessor;
                    processors.add(siaBeanProcessor);
                }
            }
            return processors;
        }
        return emptyList();
    }

    @Nullable
    private Class<?> resolveBeanClass(RootBeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        return BeanDefinitionUtils.resolveBeanType(beanDefinition, classLoader);
    }

    private Map<String, RootBeanDefinition> getEligibleBeanDefinitionsMap(DefaultListableBeanFactory beanFactory, StopWatch stopWatch) {
        stopWatch.start("getEligibleBeanDefinitionsMap");

        String[] beanNames = beanFactory.getBeanDefinitionNames();
        Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap = new HashMap<>(beanNames.length);
        for (String beanName : beanNames) {
            if (isBeanReady(beanName, beanFactory)) {
                continue;
            }
            if (beanFactory.isCurrentlyInCreation(beanName)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("The Bean[name : '{}'] is creating currently", beanName);
                }
                continue;
            }

            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            RootBeanDefinition eligibleBeanDefinition = getEligibleBeanDefinition(beanDefinition);
            if (eligibleBeanDefinition != null) {
                eligibleBeanDefinitionsMap.put(beanName, eligibleBeanDefinition);
            }
        }

        stopWatch.stop();
        return eligibleBeanDefinitionsMap;
    }

    private RootBeanDefinition getEligibleBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition != null && !beanDefinition.isAbstract() && beanDefinition.isSingleton()
                && !beanDefinition.isLazyInit() && beanDefinition instanceof RootBeanDefinition) {
            RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
            Supplier<?> instanceSupplier = rootBeanDefinition.getInstanceSupplier();
            return instanceSupplier == null ? rootBeanDefinition : null;
        }
        return null;
    }

    private boolean isBeanReady(String beanName, DefaultListableBeanFactory beanFactory) {
        boolean ready = beanFactory.containsSingleton(beanName);
        if (ready && logger.isTraceEnabled()) {
            logger.trace("The Bean[name : '{}'] is ready in the BeanFactory", beanName);
        }
        return ready;
    }

    private static Set<Member> getResolvedBeanMembers() {
        return resolvedBeanMembersHolder.get();
    }

    private static void addResolvedBeanMember(Member resolvedBeanMember) {
        getResolvedBeanMembers().add(resolvedBeanMember);
    }

    private static boolean isBeanMemberResolved(Member member) {
        return getResolvedBeanMembers().contains(member);
    }

    private static void clearResolvedBeanMembers() {
        resolvedBeanMembersHolder.remove();
    }
}
