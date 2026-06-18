package io.zhijun.spring.config.adapter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.zhijun.core.annotation.Internal;

/**
 * Adapts external configuration properties to Rose properties.
 */
@Internal
public class PropertyAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PropertyAdapter.class);

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)(ms|s|m|h)$");

    private final Map<String, Object> roseProperties = new HashMap<String, Object>();

    public Map<String, Object> getRoseProperties() {
        return roseProperties;
    }

    public static Builder builder(ConfigurableEnvironment environment) {
        return new Builder(environment);
    }

    public static class Builder {

        private final ConfigurableEnvironment environment;

        private final PropertyAdapter adapter;

        private Builder(ConfigurableEnvironment environment) {
            Assert.notNull(environment, "environment cannot be null");
            this.environment = environment;
            this.adapter = new PropertyAdapter();
        }

        public <T> Builder mapProperty(String externalKey, String roseKey, Function<String, T> converter) {
            Assert.hasText(externalKey, "externalKey cannot be null or empty");
            Assert.hasText(roseKey, "roseKey cannot be null or empty");
            Assert.notNull(converter, "converter cannot be null");

            String value = environment.getProperty(externalKey);
            if (StringUtils.hasText(value)) {
                T convertedValue = converter.apply(value.trim());
                if (convertedValue != null) {
                    adapter.roseProperties.put(roseKey, convertedValue);
                }
            }
            return this;
        }

        public <T> Builder mapEnum(String externalKey, String roseKey,
                Function<String, Function<String, T>> converterFactory) {
            Assert.notNull(converterFactory, "converterFactory cannot be null");
            return mapProperty(externalKey, roseKey, converterFactory.apply(externalKey));
        }

        public Builder mapBoolean(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, new Function<String, Boolean>() {
                @Override
                public Boolean apply(String value) {
                    return Boolean.valueOf(value);
                }
            });
        }

        public Builder mapInteger(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                try {
                    return Integer.parseInt(value);
                }
                catch (NumberFormatException e) {
                    logUnsupportedValue(externalKey, value);
                    return null;
                }
            });
        }

        public Builder mapDouble(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                try {
                    return Double.parseDouble(value);
                }
                catch (NumberFormatException e) {
                    logUnsupportedValue(externalKey, value);
                    return null;
                }
            });
        }

        public Builder mapDuration(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                try {
                    Matcher matcher = DURATION_PATTERN.matcher(value);
                    if (matcher.matches()) {
                        long amount = Long.parseLong(matcher.group(1));
                        String unit = matcher.group(2);
                        if ("ms".equals(unit)) {
                            return Duration.ofMillis(amount);
                        }
                        if ("s".equals(unit)) {
                            return Duration.ofSeconds(amount);
                        }
                        if ("m".equals(unit)) {
                            return Duration.ofMinutes(amount);
                        }
                        if ("h".equals(unit)) {
                            return Duration.ofHours(amount);
                        }
                        return null;
                    }
                    return Duration.ofMillis(Long.parseLong(value));
                }
                catch (Exception e) {
                    logUnsupportedValue(externalKey, value);
                    return null;
                }
            });
        }

        public Builder mapList(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                String[] parts = value.split(",");
                List<String> propertyList = new ArrayList<String>();
                for (String part : parts) {
                    if (StringUtils.hasText(part)) {
                        propertyList.add(part.trim());
                    }
                }
                return CollectionUtils.isEmpty(propertyList) ? null : propertyList;
            });
        }

        public Builder mapMap(String externalKey, String roseKey) {
            return mapMap(externalKey, roseKey, null);
        }

        public Builder mapMap(String externalKey, String roseKey,
                Function<Map<String, String>, Map<String, String>> postProcessor) {
            return mapProperty(externalKey, roseKey, value -> {
                Map<String, String> propertyMap = new HashMap<String, String>();
                String[] keyValuePairs = StringUtils.tokenizeToStringArray(value, ",");
                for (String pair : keyValuePairs) {
                    String[] entry = pair.split("=", 2);
                    if (entry.length == 2 && StringUtils.hasText(entry[0]) && StringUtils.hasText(entry[1])) {
                        propertyMap.put(entry[0].trim(),
                                StringUtils.uriDecode(entry[1].trim(), StandardCharsets.UTF_8));
                    }
                    else {
                        logger.warn("Invalid key-value pair in {}: {}", externalKey, pair);
                    }
                }
                Map<String, String> result = propertyMap;
                if (postProcessor != null) {
                    result = postProcessor.apply(propertyMap);
                }
                return CollectionUtils.isEmpty(result) ? null : result;
            });
        }

        public Builder mapString(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> value);
        }

        public PropertyAdapter build() {
            return adapter;
        }
    }

    private static void logUnsupportedValue(String externalKey, String value) {
        logger.warn("Unsupported value for {}: {}", externalKey, value);
    }
}
