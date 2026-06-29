package io.zhijun.spring.core.spi;
import io.zhijun.core.spi.InstanceCreator;
import io.zhijun.core.spi.annotation.SpiImpl;
import io.zhijun.spring.core.context.SpringContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
/**
 * Spring SPI实例创建器，支持自动注入Spring依赖
 * <p>优先级高于默认反射创建器，Spring环境下自动生效：
 * <ul>
 *     <li>优先从Spring容器中获取Bean（支持@Autowired/@Value等注入）</li>
 *     <li>容器中不存在则自动注册并注入依赖</li>
 *     <li>非Spring环境下自动降级到反射创建</li>
 * </ul>
 */
@SpiImpl(priority = 100, singleton = true)
public class SpringInstanceCreator implements InstanceCreator {
    @Override
    public <T> T createInstance(Class<T> implementationClass) {
        ApplicationContext context = SpringContextHolder.getApplicationContext();
        if (context == null) {
            // 非Spring环境，返回null降级到反射创建
            return null;
        }
        try {
            // 优先从容器中获取
            return context.getBean(implementationClass);
        } catch (BeansException e) {
            // 容器中不存在，尝试创建并注入依赖
            try {
                AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
                // 创建实例并注入依赖，执行初始化方法（@PostConstruct等）
                return (T) beanFactory.createBean(implementationClass, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
            } catch (BeansException ex) {
                // 创建失败，返回null降级到反射创建
                return null;
            }
        }
    }
}
