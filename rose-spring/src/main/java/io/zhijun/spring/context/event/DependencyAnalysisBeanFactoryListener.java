package io.zhijun.spring.context.event;

import io.zhijun.spring.beans.factory.ResolvableDependencyTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * 依赖分析 {@link BeanFactoryListener}。
 * 在 BeanFactory 配置冻结时分析 Bean 的依赖关系。
 */
public class DependencyAnalysisBeanFactoryListener implements BeanFactoryListener {

    private static final Logger logger = LoggerFactory.getLogger(DependencyAnalysisBeanFactoryListener.class);

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = asDefaultListableBeanFactory(bf);

        ResolvableDependencyTypeFilter resolvableDependencyTypeFilter = new ResolvableDependencyTypeFilter(beanFactory);

        List<BeanDefinitionHolder> beanDefinitionHolders = getNonLazyInitSingletonMergedBeanDefinitionHolders(bf);
        int beansCount = beanDefinitionHolders.size();
        Map<String, Set<String>> dependentBeanNamesMap = new LinkedHashMap<>(beansCount);
        for (int i = 0; i < beansCount; i++) {
            BeanDefinitionHolder beanDefinitionHolder = beanDefinitionHolders.get(i);
            Set<String> dependentBeanNames = resolveDependentBeanNames(beanDefinitionHolder,
                    resolvableDependencyTypeFilter, beanDefinitionHolders, beanFactory);
            dependentBeanNamesMap.put(beanDefinitionHolder.getBeanName(), dependentBeanNames);
        }
        flattenDependentBeanNamesMap(dependentBeanNamesMap);
    }

    private void flattenDependentBeanNamesMap(Map<String, Set<String>> dependentBeanNamesMap) {
        Map<String, Set<String>> dependenciesMap = new LinkedHashMap<>(dependentBeanNamesMap.size());
        for (Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            Set<String> dependentBeanNames = entry.getValue();
            if (dependentBeanNames.isEmpty()) {
                continue;
            }
            String beanName = entry.getKey();
            Set<String> flattenDependentBeanNames = new LinkedHashSet<>(dependentBeanNames.size() * 2);
            flatDependentBeanNames(beanName, dependentBeanNamesMap, dependenciesMap, flattenDependentBeanNames);
            entry.setValue(flattenDependentBeanNames);
        }

        for (Entry<String, Set<String>> entry : dependenciesMap.entrySet()) {
            String dependentBeanName = entry.getKey();
            dependentBeanNamesMap.remove(dependentBeanName);
        }
    }

    private void flatDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap,
                                        Map<String, Set<String>> dependenciesMap,
                                        Set<String> flattenDependentBeanNames) {
        Set<String> dependentBeanNames = retrieveDependentBeanNames(beanName, dependentBeanNamesMap);
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        for (String dependentBeanName : dependentBeanNames) {
            Set<String> dependencies = dependenciesMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>());
            dependencies.add(beanName);
            flattenDependentBeanNames.add(dependentBeanName);
            flatDependentBeanNames(dependentBeanName, dependentBeanNamesMap, dependenciesMap, flattenDependentBeanNames);
        }
    }

    private Set<String> retrieveDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap) {
        Set<String> dependentBeanNames = dependentBeanNamesMap.get(beanName);
        return dependentBeanNames != null ? dependentBeanNames : emptySet();
    }

    private Set<String> resolveDependentBeanNames(BeanDefinitionHolder beanDefinitionHolder,
                                                  ResolvableDependencyTypeFilter resolvableDependencyTypeFilter,
                                                  List<BeanDefinitionHolder> beanDefinitionHolders,
                                                  DefaultListableBeanFactory beanFactory) {
        String beanName = beanDefinitionHolder.getBeanName();
        RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionHolder.getBeanDefinition();

        Set<String> dependentBeanNames = new LinkedHashSet<>();
        dependentBeanNames.addAll(resolveBeanDefinitionDependentBeanNames(beanDefinition));
        dependentBeanNames.addAll(resolveParameterDependentBeanNames(beanName, beanDefinition,
                resolvableDependencyTypeFilter, beanDefinitionHolders, beanFactory));

        dependentBeanNames.remove(beanName);
        removeInitializedBeanNames(dependentBeanNames, beanFactory);

        return dependentBeanNames;
    }

    private void removeInitializedBeanNames(Set<String> dependentBeanNames, DefaultListableBeanFactory beanFactory) {
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        Iterator<String> iterator = dependentBeanNames.iterator();
        while (iterator.hasNext()) {
            if (beanFactory.containsSingleton(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private List<String> resolveBeanDefinitionDependentBeanNames(RootBeanDefinition beanDefinition) {
        String[] dependsOn = beanDefinition.getDependsOn();
        List<String> dependentBeanNames = new ArrayList<>();
        if (!isEmpty(dependsOn)) {
            dependentBeanNames.addAll(asList(dependsOn));
        }

        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
            Object value = propertyValue.getValue();
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
               dependentBeanNames.add(beanReference.getBeanName());
           }
        }
        return dependentBeanNames;
    }

    private List<String> resolveParameterDependentBeanNames(String beanName,
                                                            RootBeanDefinition beanDefinition,
                                                            ResolvableDependencyTypeFilter resolvableDependencyTypeFilter,
                                                            List<BeanDefinitionHolder> beanDefinitionHolders,
                                                            DefaultListableBeanFactory beanFactory) {
        Parameter[] parameters = getParameters(beanName, beanDefinition, beanFactory);
        if (parameters.length < 1) {
            return emptyList();
        }

        List<String> dependentBeanNames = new ArrayList<>(parameters.length);
        for (Parameter parameter : parameters) {
            Class<?> dependentType = resolveDependentType(parameter);
            if (resolvableDependencyTypeFilter.accept(dependentType)) {
                continue;
            }
            String suggestedBeanName = resolveSuggestedDependentBeanName(parameter, beanFactory);
            if (suggestedBeanName != null) {
                dependentBeanNames.add(suggestedBeanName);
            } else {
                String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
                dependentBeanNames.addAll(asList(beanNames));
            }
        }
        return dependentBeanNames;
    }

    private String resolveSuggestedDependentBeanName(Parameter parameter, DefaultListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(
                MethodParameter.forParameter(parameter), true, false);
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    private Class<?> resolveDependentType(Parameter parameter) {
        Type parameterType = parameter.getParameterizedType();
        Class<?> parameterRawType = parameter.getType();
        Class<?> dependentType = parameterRawType;
        if (parameterType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) parameterType;
            Type[] arguments = pt.getActualTypeArguments();
            if (arguments.length > 0) {
                Type lastArg = arguments[arguments.length - 1];
                if (lastArg instanceof Class) {
                    dependentType = (Class<?>) lastArg;
                }
            }
        }
        return dependentType;
    }

    private Parameter[] getParameters(String beanName, RootBeanDefinition beanDefinition,
                                      DefaultListableBeanFactory beanFactory) {
        Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
        if (factoryMethod != null) {
            return factoryMethod.getParameters();
        }

        Class<?> beanClass = getBeanClass(beanDefinition, beanFactory.getBeanClassLoader());
        if (beanClass == null) return new Parameter[0];

        Constructor<?>[] constructors = resolveConstructors(beanName, beanClass, beanFactory);
        if (constructors.length != 1) {
            if (logger.isWarnEnabled()) {
                logger.warn("Bean[name : '{}', class : {}] has {} constructors", beanName, beanClass, constructors.length);
            }
            return new Parameter[0];
        }
        return constructors[0].getParameters();
    }

    private List<BeanDefinitionHolder> getNonLazyInitSingletonMergedBeanDefinitionHolders(
            ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        List<BeanDefinitionHolder> beanDefinitionHolders = new ArrayList<>(beanNames.length);
        for (String beanName : beanNames) {
            if (beanFactory.containsSingleton(beanName)) {
                continue;
            }
            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            if (isEligibleBeanDefinition(beanDefinition)) {
                String[] aliases = beanFactory.getAliases(beanName);
                beanDefinitionHolders.add(new BeanDefinitionHolder(beanDefinition, beanName, aliases));
            }
        }
        return beanDefinitionHolders;
    }

    private Constructor<?>[] resolveConstructors(String beanName, Class<?> beanClass,
                                                 ConfigurableListableBeanFactory beanFactory) {
        Constructor<?>[] constructors = null;
        if (!beanClass.isInterface()) {
            List<SmartInstantiationAwareBeanPostProcessor> processors = getSmartInstantiationAwareBeanPostProcessors(beanFactory);
            for (SmartInstantiationAwareBeanPostProcessor processor : processors) {
                constructors = processor.determineCandidateConstructors(beanClass, beanName);
                if (constructors != null) break;
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

    private Class<?> getBeanClass(RootBeanDefinition beanDefinition, ClassLoader classLoader) {
        return beanDefinition.hasBeanClass() ? beanDefinition.getBeanClass() :
                resolveClassName(beanDefinition.getBeanClassName(), classLoader);
    }

    private boolean isEligibleBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition != null && beanDefinition.isSingleton() && !beanDefinition.isLazyInit()
                && beanDefinition instanceof RootBeanDefinition) {
            RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
            Supplier<?> instanceSupplier = rootBeanDefinition.getInstanceSupplier();
            return instanceSupplier == null || instanceSupplier.get() == null;
        }
        return false;
    }
}
