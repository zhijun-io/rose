package io.zhijun.spring.boot.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 一次性 {@link ApplicationPreparedEvent} 监听器基类——每个 ApplicationContext 只处理一次。
 * <p>
 * 通过 {@link #isIgnored} 跳过不需要处理的 context，通过
 * {@link #onApplicationEvent(SpringApplication, String[], ConfigurableApplicationContext)}
 * 实现自定义初始化逻辑。
 * <p>
 * （借鉴 microsphere-spring-boot {@code OnceApplicationPreparedEventListener}）
 *
 * @see ApplicationPreparedEvent
 * @see Ordered
 */
public abstract class OnceApplicationPreparedEventListener
        implements ApplicationListener<ApplicationPreparedEvent>, Ordered {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Map<Class<? extends ApplicationListener>, Set<String>>
            listenerProcessedContextIds = new ConcurrentHashMap<>();

    private final Set<String> processedContextIds;

    private int order = LOWEST_PRECEDENCE;

    public OnceApplicationPreparedEventListener() {
        this.processedContextIds = listenerProcessedContextIds
                .computeIfAbsent(getClass(), k -> new ConcurrentSkipListSet<>());
    }

    @Override
    public final void onApplicationEvent(ApplicationPreparedEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        String contextId = context.getId();
        if (processedContextIds.contains(contextId)) {
            logger.trace("ApplicationContext [id: {}] 已处理，跳过", contextId);
            return;
        }

        SpringApplication springApplication = event.getSpringApplication();
        String[] args = event.getArgs();

        if (isIgnored(springApplication, args, context)) {
            processedContextIds.add(contextId);
            logger.trace("ApplicationContext [id: {}] 被忽略", contextId);
            return;
        }

        processedContextIds.add(contextId);
        onApplicationEvent(springApplication, args, context);
    }

    /**
     * 是否忽略此 ApplicationContext。
     */
    protected abstract boolean isIgnored(SpringApplication springApplication,
                                         String[] args,
                                         ConfigurableApplicationContext context);

    /**
     * 处理一次性的初始化逻辑。
     */
    protected abstract void onApplicationEvent(SpringApplication springApplication,
                                               String[] args,
                                               ConfigurableApplicationContext context);

    /**
     * 清除指定监听器类的已处理上下文 ID 缓存。测试中需要重新处理同一个 context 时调用。
     *
     * @param listenerClass 监听器类，必须是 {@link OnceApplicationPreparedEventListener} 的子类
     */
    public static void clearProcessedContextIds(Class<? extends ApplicationListener> listenerClass) {
        listenerProcessedContextIds.remove(listenerClass);
    }

    public final void setOrder(int order) {
        this.order = order;
    }

    @Override
    public final int getOrder() {
        return order;
    }
}
