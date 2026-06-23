package io.zhijun.dev.api.provider;

/**
 * Marker for mutually exclusive dev service categories.
 */
public interface LocalServiceProvider {

    String name();

    String category();

    static LocalServiceProvider of(final String name, final String category) {
        return new LocalServiceProvider() {
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
