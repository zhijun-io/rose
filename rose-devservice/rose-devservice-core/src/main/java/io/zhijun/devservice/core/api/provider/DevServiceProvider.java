package io.zhijun.devservice.core.api.provider;

/**
 * Marker for mutually exclusive dev service categories.
 */
public interface DevServiceProvider {

    String name();

    DevServiceCategory category();

    static DevServiceProvider of(final String name, final DevServiceCategory category) {
        return new DevServiceProvider() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public DevServiceCategory category() {
                return category;
            }
        };
    }
}
