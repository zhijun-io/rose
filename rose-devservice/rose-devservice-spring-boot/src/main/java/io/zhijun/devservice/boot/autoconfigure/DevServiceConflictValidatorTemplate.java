package io.zhijun.devservice.boot.autoconfigure;

import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.*;

/**
 * Builds startup validation for conflicting dev service providers.
 */
public final class DevServiceConflictValidatorTemplate {

    public SmartInitializingSingleton conflictValidator(ObjectProvider<DevServiceProvider> providers) {
        return () -> {
            Map<DevServiceCategory, List<DevServiceProvider>> grouped = new EnumMap<DevServiceCategory, List<DevServiceProvider>>(
                    DevServiceCategory.class);
            for (DevServiceProvider provider : providers) {
                List<DevServiceProvider> group = grouped.get(provider.category());
                if (group == null) {
                    group = new ArrayList<DevServiceProvider>();
                    grouped.put(provider.category(), group);
                }
                group.add(provider);
            }
            for (Map.Entry<DevServiceCategory, List<DevServiceProvider>> entry : grouped.entrySet()) {
                if (entry.getValue().size() > 1) {
                    List<String> names = new ArrayList<String>();
                    for (DevServiceProvider provider : entry.getValue()) {
                        names.add(provider.name());
                    }
                    Collections.sort(names);
                    throw new MultipleDevServiceException(entry.getKey(), names);
                }
            }
        };
    }
}
