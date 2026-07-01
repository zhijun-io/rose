package io.zhijun.spring.web.metadata;

 import org.springframework.beans.factory.SmartInitializingSingleton;
 import org.springframework.beans.factory.ListableBeanFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.stream.Collectors;
 
 /**
  * 复合 {@link WebEndpointMappingRegistry}，委托给多个注册表实现。
 */
public class CompositeWebEndpointMappingRegistry extends FilteringWebEndpointMappingRegistry implements SmartInitializingSingleton {

    private List<WebEndpointMappingRegistry> registries = new ArrayList<>();

    @Override
    protected boolean doRegister(WebEndpointMapping mapping) {
        boolean registered = false;
        for (WebEndpointMappingRegistry registry : registries) {
            registered |= registry.register(mapping);
        }
        return registered;
    }

    @Override
     public Collection<WebEndpointMapping> getAll() {
         return registries.stream()
                 .flatMap(registry -> registry.getAll().stream())
                 .collect(Collectors.toList());
     }
 
     @Override
     public void afterSingletonsInstantiated() {
         if (beanFactory == null) return;
         String[] names = ((ListableBeanFactory) beanFactory).getBeanNamesForType(WebEndpointMappingRegistry.class, false, false);
         for (String name : names) {
             WebEndpointMappingRegistry registry = beanFactory.getBean(name, WebEndpointMappingRegistry.class);
             if (registry != this) {
                registries.add(registry);
            }
        }
    }
}
