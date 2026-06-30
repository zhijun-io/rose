package io.zhijun.spring.context.event;

import io.zhijun.spring.beans.factory.BeanDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static java.util.Collections.emptySet;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * 并行预初始化 Singleton Bean 的 {@link BeanFactoryListener}。
 * 在 BeanFactory 配置冻结时并行初始化 Bean 以提升启动性能。
 */
public class ParallelPreInstantiationSingletonsBeanFactoryListener implements BeanFactoryListener, EnvironmentAware, BeanFactoryAware {

    private static final String PROPERTY_NAME_PREFIX = "rose.spring.pre-instantiation.singletons.";

    public static final String THREADS_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "threads";

    public static final String DEFAULT_THREAD_NAME_PREFIX = "Parallel-Pre-Instantiation-Singletons-Thread-";

    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "thread.name-prefix";

    private static final Logger logger = LoggerFactory.getLogger(ParallelPreInstantiationSingletonsBeanFactoryListener.class);

    private Environment environment;

    private DefaultListableBeanFactory beanFactory;

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {
        if (bf != beanFactory) {
            if (logger.isWarnEnabled()) {
                logger.warn("Current BeanFactory[{}] is not the expected instance", bf);
            }
            return;
        }

        StopWatch stopWatch = new StopWatch("ParallelPreInstantiationSingletons");

        ExecutorService executorService = newExecutorService();
        if (executorService != null) {
            try {
                BeanDependencyResolver resolver = new BeanDependencyResolver(this.beanFactory, executorService);
                Map<String, Set<String>> dependentBeanNamesMap = resolver.resolve(this.beanFactory);
                preInstantiateSingletonsInParallel(dependentBeanNamesMap, beanFactory, executorService, stopWatch);
            } finally {
                executorService.shutdown();
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info(stopWatch.toString());
        }
    }

    private ExecutorService newExecutorService() {
        int threads = environment.getProperty(THREADS_PROPERTY_NAME, int.class, getDefaultThreads());
        if (threads < 1) {
            return null;
        }
        String threadNamePrefix = environment.getProperty(THREAD_NAME_PREFIX_PROPERTY_NAME, DEFAULT_THREAD_NAME_PREFIX);
        return newFixedThreadPool(threads, new CustomizableThreadFactory(threadNamePrefix));
    }

    private void preInstantiateSingletonsInParallel(Map<String, Set<String>> dependentBeanNamesMap,
                                                    DefaultListableBeanFactory beanFactory,
                                                    ExecutorService executorService, StopWatch stopWatch) {
        stopWatch.start("preInstantiateSingletonsInParallel");

        List<Set<String>> beanNamesInDependencyPaths = resolveBeanNamesInDependencyPaths(dependentBeanNamesMap);

        for (Set<String> beanNamesInDependencyPath : beanNamesInDependencyPaths) {
            executorService.submit(() -> {
                for (String beanName : beanNamesInDependencyPath) {
                    Object bean = beanFactory.getBean(beanName);
                    if (logger.isTraceEnabled()) {
                        logger.trace("The bean[name : '{}'] was created : {}", beanName, bean);
                    }
                }
                return null;
            });
        }

        try {
            while (!executorService.awaitTermination(10, TimeUnit.MILLISECONDS)) {
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        stopWatch.stop();
    }

    private List<Set<String>> resolveBeanNamesInDependencyPaths(Map<String, Set<String>> dependentBeanNamesMap) {
        List<Set<String>> beanNamesList = buildBeanNamesList(dependentBeanNamesMap);
        List<Set<String>> dependencyPaths = new LinkedList<>();
        int size = beanNamesList.size();
        for (int i = 0; i < size; i++) {
            Set<String> beanNames = beanNamesList.get(i);
            if (!beanNames.isEmpty()) {
                for (int j = i + 1; j < size; j++) {
                    Set<String> otherNames = beanNamesList.get(j);
                    if (containsAny(beanNames, otherNames)) {
                        beanNames.addAll(otherNames);
                        beanNamesList.set(j, emptySet());
                    }
                }
            }
        }
        for (Set<String> beanNames : beanNamesList) {
            if (!beanNames.isEmpty()) {
                dependencyPaths.add(beanNames);
            }
        }
        return dependencyPaths;
    }

    private List<Set<String>> buildBeanNamesList(Map<String, Set<String>> dependentBeanNamesMap) {
        List<Set<String>> beanNamesList = new ArrayList<>(dependentBeanNamesMap.size());
        for (Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            Set<String> beanNames = entry.getValue();
            beanNames.add(entry.getKey());
            beanNamesList.add(beanNames);
        }
        return beanNamesList;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private int getDefaultThreads() {
        return Math.max(1, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asDefaultListableBeanFactory(beanFactory);
    }
}
