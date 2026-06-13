package io.zhijun.core.config.adapter;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PropertyAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class PropertyAdapterTests {

    @Mock
    private ConfigurableEnvironment environment;

    // ARGUMENT CHECKS

    @Test
    void whenNullEnvironmentThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("environment cannot be null");
    }

    @Test
    void whenNullExternalKeyThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(environment)
            .mapString(null, "rose.string"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("externalKey cannot be null or empty");
    }

    @Test
    void whenEmptyExternalKeyThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(environment)
            .mapString("", "rose.string"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("externalKey cannot be null or empty");
    }

    @Test
    void whenNullRoseKeyThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(environment)
            .mapString("external.string", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("roseKey cannot be null or empty");
    }

    @Test
    void whenEmptyRoseKeyThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(environment)
            .mapString("external.string", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("roseKey cannot be null or empty");
    }

    @Test
    void whenNullConverterThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(environment)
            .mapProperty("external.custom", "rose.custom", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("converter cannot be null");
    }

    @Test
    void whenNullConverterFactoryThenThrow() {
        assertThatThrownBy(() -> PropertyAdapter.builder(environment)
            .mapEnum("external.enum", "rose.enum", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("converterFactory cannot be null");
    }

    // STRING

    @Test
    void shouldMapStringProperty() {
        when(environment.getProperty("external.string")).thenReturn("test-value");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapString("external.string", "rose.string")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.string", "test-value");
    }

    @Test
    void shouldHandleNullInputForString() {
        when(environment.getProperty("external.string")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapString("external.string", "rose.string")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.string");
    }

    @Test
    void shouldHandleEmptyInputForString() {
        when(environment.getProperty("external.string")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapString("external.string", "rose.string")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.string");
    }

    @Test
    void shouldHandleBlankInputForString() {
        when(environment.getProperty("external.string")).thenReturn("   ");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapString("external.string", "rose.string")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.string");
    }

    // BOOLEAN

    @Test
    void shouldMapBooleanProperty() {
        when(environment.getProperty("external.boolean")).thenReturn("true");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapBoolean("external.boolean", "rose.boolean")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.boolean", true);
    }

    @Test
    void shouldHandleNullInputForBoolean() {
        when(environment.getProperty("external.boolean")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapBoolean("external.boolean", "rose.boolean")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.boolean");
    }

    @Test
    void shouldHandleEmptyInputForBoolean() {
        when(environment.getProperty("external.boolean")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapBoolean("external.boolean", "rose.boolean")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.boolean");
    }

    @Test
    void shouldMapFalseBooleanProperty() {
        when(environment.getProperty("external.boolean")).thenReturn("false");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapBoolean("external.boolean", "rose.boolean")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.boolean", false);
    }

    @Test
    void shouldMapInvalidBooleanPropertyAsFalse() {
        when(environment.getProperty("external.boolean")).thenReturn("yes");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapBoolean("external.boolean", "rose.boolean")
            .build();

        // Boolean.valueOf returns false for any non-"true" string
        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.boolean", false);
    }

    // DOUBLE

    @Test
    void shouldMapDoubleProperty() {
        when(environment.getProperty("external.double")).thenReturn("42.5");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDouble("external.double", "rose.double")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.double", 42.5);
    }

    @Test
    void shouldHandleInvalidDoubleProperty() {
        when(environment.getProperty("external.double")).thenReturn("invalid");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDouble("external.double", "rose.double")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.double");
    }

    @Test
    void shouldHandleNullInputForDouble() {
        when(environment.getProperty("external.double")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDouble("external.double", "rose.double")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.double");
    }

    @Test
    void shouldHandleEmptyInputForDouble() {
        when(environment.getProperty("external.double")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDouble("external.double", "rose.double")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.double");
    }

    // DURATION

    @Test
    void shouldMapDurationPropertyWithMilliseconds() {
        when(environment.getProperty("external.duration")).thenReturn("60000");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.duration", Duration.ofMillis(60000));
    }

    @Test
    void shouldMapDurationPropertyWithMillisecondsUnit() {
        when(environment.getProperty("external.duration")).thenReturn("60000ms");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.duration", Duration.ofMillis(60000));
    }

    @Test
    void shouldMapDurationPropertyWithSecondsUnit() {
        when(environment.getProperty("external.duration")).thenReturn("60s");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.duration", Duration.ofSeconds(60));
    }

    @Test
    void shouldMapDurationPropertyWithMinutesUnit() {
        when(environment.getProperty("external.duration")).thenReturn("60m");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.duration", Duration.ofMinutes(60));
    }

    @Test
    void shouldMapDurationPropertyWithHoursUnit() {
        when(environment.getProperty("external.duration")).thenReturn("24h");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.duration", Duration.ofHours(24));
    }

    @Test
    void shouldHandleInvalidDurationUnitProperty() {
        when(environment.getProperty("external.duration")).thenReturn("60x");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.duration");
    }

    @Test
    void shouldHandleInvalidDurationProperty() {
        when(environment.getProperty("external.duration")).thenReturn("invalid");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.duration");
    }

    @Test
    void shouldHandleNullInputForDuration() {
        when(environment.getProperty("external.duration")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.duration");
    }

    @Test
    void shouldHandleEmptyInputForDuration() {
        when(environment.getProperty("external.duration")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapDuration("external.duration", "rose.duration")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.duration");
    }

    // INTEGER

    @Test
    void shouldMapIntegerProperty() {
        when(environment.getProperty("external.integer")).thenReturn("42");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapInteger("external.integer", "rose.integer")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.integer", 42);
    }

    @Test
    void shouldHandleInvalidIntegerProperty() {
        when(environment.getProperty("external.integer")).thenReturn("invalid");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapInteger("external.integer", "rose.integer")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.integer");
    }

    @Test
    void shouldHandleNullInputForInteger() {
        when(environment.getProperty("external.integer")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapInteger("external.integer", "rose.integer")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.integer");
    }

    @Test
    void shouldHandleEmptyInputForInteger() {
        when(environment.getProperty("external.integer")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapInteger("external.integer", "rose.integer")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.integer");
    }

    // LIST

    @Test
    void shouldMapListProperty() {
        when(environment.getProperty("external.list")).thenReturn("value1,value2,value3");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapList("external.list", "rose.list")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.list", Arrays.asList("value1", "value2", "value3"));
    }

    @Test
    void shouldHandleNullInputForList() {
        when(environment.getProperty("external.list")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapList("external.list", "rose.list")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.list");
    }

    @Test
    void shouldHandleEmptyInputForList() {
        when(environment.getProperty("external.list")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapList("external.list", "rose.list")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.list");
    }

    @Test
    void shouldHandleListWithOnlyDelimiters() {
        when(environment.getProperty("external.list")).thenReturn(",");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapList("external.list", "rose.list")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.list");
    }

    // MAP

    @Test
    void shouldMapMapProperty() {
        when(environment.getProperty("external.map")).thenReturn("key1=value1,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.map", mapOf("key1", "value1", "key2", "value2"));
    }

    @Test
    void shouldHandleInvalidMapProperty() {
        when(environment.getProperty("external.map")).thenReturn("invalid,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.map", mapOf("key2", "value2"));
    }

    @Test
    void shouldHandleEmptyMapProperty() {
        when(environment.getProperty("external.map")).thenReturn("");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.map");
    }

    @Test
    void shouldHandleNullInputForMap() {
        when(environment.getProperty("external.map")).thenReturn(null);

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.map");
    }

    @Test
    void shouldProcessMapWithPostProcessor() {
        when(environment.getProperty("external.map")).thenReturn("key1=value1,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map", map -> {
                map.put("key3", "value3");
                return map;
            })
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.map", mapOf("key1", "value1", "key2", "value2", "key3", "value3"));
    }

    @Test
    void shouldHandleEmptyKeyValuePairsInMap() {
        when(environment.getProperty("external.map")).thenReturn("=value1,key2=");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.map");
    }

    @Test
    void shouldHandleMultipleInvalidEntriesInMap() {
        when(environment.getProperty("external.map")).thenReturn("invalid1,key1=value1,invalid2,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.map", mapOf("key1", "value1", "key2", "value2"));
    }

    @Test
    void shouldHandleAllInvalidEntriesInMap() {
        when(environment.getProperty("external.map")).thenReturn("invalid1,invalid2,invalid3");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.map");
    }

    @Test
    void shouldHandleMapValuesContainingEquals() {
        when(environment.getProperty("external.map")).thenReturn("key1=dGVzdA==,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.map", mapOf("key1", "dGVzdA==", "key2", "value2"));
    }

    @Test
    void shouldStripWhitespaceInMapKeyValues() {
        when(environment.getProperty("external.map")).thenReturn(" key1 = value1 , key2 = value2 ");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.map", mapOf("key1", "value1", "key2", "value2"));
    }

    @Test
    void shouldHandleNullMapFromPostProcessor() {
        when(environment.getProperty("external.map")).thenReturn("key1=value1,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map", map -> null)
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.map");
    }

    @Test
    void shouldHandleEmptyMapFromPostProcessor() {
        when(environment.getProperty("external.map")).thenReturn("key1=value1,key2=value2");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapMap("external.map", "rose.map", map -> new HashMap<String, String>())
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.map");
    }

    // ENUM
    
    @Test
    void shouldMapEnum() {
        when(environment.getProperty("external.enum")).thenReturn("TEST_VALUE");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapEnum("external.enum", "rose.enum", key -> value -> {
                try {
                    return TestEnum.valueOf(value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.enum", TestEnum.TEST_VALUE);
    }

    @Test
    void shouldHandleInvalidEnum() {
        when(environment.getProperty("external.enum")).thenReturn("INVALID_VALUE");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapEnum("external.enum", "rose.enum", key -> value -> {
                try {
                    return TestEnum.valueOf(value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .build();

        assertThat(adapter.getRoseProperties())
            .doesNotContainKey("rose.enum");
    }

    // CUSTOM

    @Test
    void shouldMapCustomProperty() {
        when(environment.getProperty("external.custom")).thenReturn("42");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapProperty("external.custom", "rose.custom", value -> {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.custom", 42);
    }

    // MULTIPLE

    @Test
    void shouldMapMultipleProperties() {
        when(environment.getProperty("external.string")).thenReturn("test");
        when(environment.getProperty("external.boolean")).thenReturn("true");
        when(environment.getProperty("external.double")).thenReturn("42.5");

        PropertyAdapter adapter = PropertyAdapter.builder(environment)
            .mapString("external.string", "rose.string")
            .mapBoolean("external.boolean", "rose.boolean")
            .mapDouble("external.double", "rose.double")
            .build();

        assertThat(adapter.getRoseProperties())
            .containsEntry("rose.string", "test")
            .containsEntry("rose.boolean", true)
            .containsEntry("rose.double", 42.5);
    }


    @SafeVarargs
    private static final <V> Map<String, V> mapOf(Object... entries) {
        Map<String, V> map = new HashMap<String, V>();
        for (int i = 0; i < entries.length; i += 2) {
            @SuppressWarnings("unchecked")
            V value = (V) entries[i + 1];
            map.put((String) entries[i], value);
        }
        return map;
    }

    private enum TestEnum {
        TEST_VALUE
    }

}
