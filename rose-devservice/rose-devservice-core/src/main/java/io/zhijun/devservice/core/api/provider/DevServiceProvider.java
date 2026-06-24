package io.zhijun.devservice.core.api.provider;

/**
 * Marker for mutually exclusive dev service categories.
 */
public interface DevServiceProvider {

    String name();

    String category();

    static DevServiceProvider of(final String name, final String category) {
        return new DevServiceProvider() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String category() {
                return category;
            }
        };
    }
}
