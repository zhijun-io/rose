package io.zhijun.spring.beans;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

import static io.zhijun.spring.beans.BeanDefinitionUtils.resolveBeanType;
import static java.beans.Introspector.decapitalize;

/**
 * 通用 {@link BeanNameGenerator}，根据 bean 类型的简单名称生成驼峰命名。
 *
 * <p>例如 {@code com.example.UserService} → {@code userService}</p>
 */
public class GenericBeanNameGenerator implements BeanNameGenerator {

    public static final BeanNameGenerator INSTANCE = new GenericBeanNameGenerator();

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        Class<?> beanType = resolveBeanType(definition);
        if (beanType != null) {
            return decapitalize(beanType.getSimpleName());
        }
        throw new IllegalArgumentException("Cannot resolve bean type from definition: " + definition);
    }
}
