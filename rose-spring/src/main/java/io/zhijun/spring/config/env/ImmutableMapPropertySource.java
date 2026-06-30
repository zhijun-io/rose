package io.zhijun.spring.config.env;

import org.springframework.core.env.MapPropertySource;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

import static java.util.Collections.unmodifiableMap;

/**
 * An immutable implementation of {@link MapPropertySource} that ensures the underlying map remains unmodifiable.
 * <p>
 * This class is useful in scenarios where the configuration properties should be protected from further modifications
 * after initialization. It wraps the provided source map into an unmodifiable map using
 * {@link java.util.Collections#unmodifiableMap(Map)}.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *     Map<String, Object> source = new HashMap<>();
 *     source.put("key1", "value1");
 *     source.put("key2", 42);
 *
 *     ImmutableMapPropertySource propertySource = new ImmutableMapPropertySource("mySource", source);
 *
 *     // The following operations will throw UnsupportedOperationException
 *     try {
 *         propertySource.getPropertySources().addLast(new CustomPropertySource());
 *     } catch (UnsupportedOperationException e) {
 *         // Expected exception
 *     }
 * }</pre>
 *
 * @see MapPropertySource
 * @see java.util.Collections#unmodifiableMap(Map)
 * @since 1.0.0
 */
public class ImmutableMapPropertySource extends MapPropertySource {

    /**
     * Create a new immutable {@code MapPropertySource} with the given name and {@code Map}.
     *
     * @param name   the associated name
     * @param source the Map source (without {@code null} values in order to get
     *               consistent {@link #getProperty} and {@link #containsProperty} behavior)
     */
    public ImmutableMapPropertySource(String name, Map source) {
        super(name, immutableMap(source));
    }

    private static Map immutableMap(Map source) {
        Map result;
        synchronized (ImmutableMapPropertySource.class) {
            result = newMap(source);
        }
        return unmodifiableMap(result);
    }

    private static Map newMap(Map source) {
        if (source instanceof SortedMap) {
            return new java.util.TreeMap<>(source);
        } else if (source instanceof LinkedHashMap) {
            return new java.util.LinkedHashMap<>(source);
        } else if (source instanceof IdentityHashMap) {
            return new IdentityHashMap(source);
        } else {
            return new java.util.HashMap<>(source);
        }
    }
}
