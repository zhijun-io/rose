package io.zhijun.devservice.boot.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.dockerjava.api.DockerClient;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;

import io.zhijun.annotation.Incubating;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.core.api.registration.ContainerInfo;

/**
 * Registers Testcontainers container bean definitions.
 */
@Incubating
public class DevServiceRegistry {

    private final BeanDefinitionRegistry beanDefinitionRegistry;

    public DevServiceRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        Assert.notNull(beanDefinitionRegistry, "beanDefinitionRegistry cannot be null");
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    public void registerDevServiceProvider(String name, DevServiceCategory category) {
        String beanName = "devServiceProvider." + name;
        if (!beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.setInstanceSupplier(() -> DevServiceProvider.of(name, category));
            beanDefinitionRegistry.registerBeanDefinition(beanName, definition);
        }
    }

    public void registerDevService(Consumer<ServiceSpec> service) {
        ServiceSpec serviceSpec = new ServiceSpec();
        service.accept(serviceSpec);
        registerBeanDefinition(serviceSpec);
    }

    /**
     * Convenience overload that avoids nested anonymous {@link Consumer}/{@link Supplier} classes
     * for the common case of registering a named service with a single container.
     */
    public void registerDevService(
            String name,
            String description,
            Class<? extends Container<?>> type,
            Supplier<? extends Container<?>> supplier) {
        registerDevService(service -> service.name(name)
                .description(description)
                .container(container -> container.type(type).supplier(supplier)));
    }

    private void registerBeanDefinition(ServiceSpec service) {
        Assert.hasText(service.getName(), "service name cannot be null or empty");
        Assert.notNull(service.getContainerSpec(), "service container cannot be null");
        Assert.notNull(service.getContainerSpec().getType(), "service container type cannot be null");
        Assert.notNull(service.getContainerSpec().getSupplier(), "service container supplier cannot be null");

        String containerBeanName = "devService.container." + service.getName();
        if (beanDefinitionRegistry.containsBeanDefinition(containerBeanName)) {
            throw new IllegalStateException("Dev service already registered: " + service.getName());
        }
        GenericBeanDefinition containerBeanDefinition = createContainerBeanDefinition(service);
        beanDefinitionRegistry.registerBeanDefinition(containerBeanName, containerBeanDefinition);

        String descriptionBeanName = "devServiceRegistration." + service.getName();
        if (beanDefinitionRegistry.containsBeanDefinition(descriptionBeanName)) {
            throw new IllegalStateException("Dev service registration already registered: " + service.getName());
        }
        RootBeanDefinition descriptionBeanDefinition = createDescriptionBeanDefinition(service, containerBeanName);
        beanDefinitionRegistry.registerBeanDefinition(descriptionBeanName, descriptionBeanDefinition);
    }

    private GenericBeanDefinition createContainerBeanDefinition(ServiceSpec service) {
        ContainerSpec containerSpec = service.getContainerSpec();

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(containerSpec.getType());

        if (service.getDescription() != null) {
            beanDefinition.setDescription(service.getDescription());
        }

        final Supplier<? extends Container<?>> supplier = containerSpec.getSupplier();
        beanDefinition.setInstanceSupplier(new Supplier<Object>() {
            @Override
            public Object get() {
                return supplier.get();
            }
        });

        if (ClassUtils.isPresent("org.springframework.boot.devtools.restart.RestartScope", null)) {
            beanDefinition.setScope("restart");
        } else {
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        }

        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        return beanDefinition;
    }

    private RootBeanDefinition createDescriptionBeanDefinition(ServiceSpec service, String containerBeanName) {
        RootBeanDefinition descriptionBeanDefinition = new RootBeanDefinition();
        descriptionBeanDefinition.setBeanClass(DevServiceRegistrationFactoryBean.class);
        descriptionBeanDefinition.setRole(BeanDefinition.ROLE_SUPPORT);
        descriptionBeanDefinition.setDependsOn(containerBeanName);
        descriptionBeanDefinition.getPropertyValues().add("name", service.getName());
        descriptionBeanDefinition.getPropertyValues().add("description", service.getDescription());
        descriptionBeanDefinition.getPropertyValues().add("containerBeanName", containerBeanName);
        return descriptionBeanDefinition;
    }

    static ContainerInfo extractContainerInfoById(String containerId) {
        try {
            DockerClient dockerClient = DockerClientFactory.lazyClient();
            com.github.dockerjava.api.model.Container dockerContainer = dockerClient
                    .listContainersCmd()
                    .withIdFilter(Collections.singleton(containerId))
                    .withShowAll(true)
                    .exec()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Container not found with ID: " + containerId));

            List<String> names = new ArrayList<String>();
            for (String name : dockerContainer.getNames()) {
                names.add(name.charAt(0) == '/' ? name.substring(1) : name);
            }

            List<ContainerInfo.ContainerPort> exposedPorts = new ArrayList<ContainerInfo.ContainerPort>();
            if (dockerContainer.getPorts() != null) {
                for (com.github.dockerjava.api.model.ContainerPort port : dockerContainer.getPorts()) {
                    exposedPorts.add(new ContainerInfo.ContainerPort(
                            port.getIp(), port.getPrivatePort(), port.getPublicPort(), port.getType()));
                }
            }

            Map<String, String> labels =
                    dockerContainer.getLabels() != null ? dockerContainer.getLabels() : new HashMap<String, String>();

            return new ContainerInfo(
                    containerId, dockerContainer.getImage(), names, exposedPorts, labels, dockerContainer.getStatus());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to extract container information for ID: " + containerId, ex);
        }
    }

    public static final class ServiceSpec {

        private String name;
        private String description;
        private ContainerSpec containerSpec;

        private ServiceSpec() {}

        public ServiceSpec name(String name) {
            this.name = name;
            return this;
        }

        public ServiceSpec description(String description) {
            this.description = description;
            return this;
        }

        public ServiceSpec container(Consumer<ContainerSpec> containerSpecConsumer) {
            ContainerSpec spec = new ContainerSpec();
            containerSpecConsumer.accept(spec);
            this.containerSpec = spec;
            return this;
        }

        String getName() {
            return name;
        }

        String getDescription() {
            return description;
        }

        ContainerSpec getContainerSpec() {
            return containerSpec;
        }
    }

    public static final class ContainerSpec {

        private Class<? extends Container<?>> type;
        private Supplier<? extends Container<?>> supplier;

        private ContainerSpec() {}

        public ContainerSpec type(Class<? extends Container<?>> type) {
            this.type = type;
            return this;
        }

        public ContainerSpec supplier(Supplier<? extends Container<?>> supplier) {
            this.supplier = supplier;
            return this;
        }

        Class<? extends Container<?>> getType() {
            return type;
        }

        Supplier<? extends Container<?>> getSupplier() {
            return supplier;
        }
    }
}
