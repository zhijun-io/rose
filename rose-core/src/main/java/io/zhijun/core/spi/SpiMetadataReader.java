package io.zhijun.core.spi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SpiMetadataReader {

    private SpiMetadataReader() {
    }

    static List<MetadataEntry> read(InputStream inputStream, String serviceName) throws IOException {
        String json;
        try {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            json = new String(buffer, StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }

        String spiKey = "\"" + serviceName + "\":";
        int spiIndex = json.indexOf(spiKey);
        if (spiIndex == -1) {
            return null;
        }

        int arrayStart = json.indexOf('[', spiIndex + spiKey.length());
        int arrayEnd = findMatchingBracket(json, arrayStart, '[', ']');
        if (arrayStart == -1 || arrayEnd == -1) {
            return null;
        }

        String arrayJson = json.substring(arrayStart + 1, arrayEnd);
        List<MetadataEntry> entries = new ArrayList<>();
        int objectStart = arrayJson.indexOf('{');
        while (objectStart != -1) {
            int objectEnd = findMatchingBracket(arrayJson, objectStart, '{', '}');
            if (objectEnd == -1) {
                break;
            }
            String objectJson = arrayJson.substring(objectStart + 1, objectEnd);
            entries.add(new MetadataEntry(
                    getJsonStringValue(objectJson, "className"),
                    getJsonStringValue(objectJson, "alias"),
                    getJsonIntValue(objectJson, "priority", Integer.MAX_VALUE),
                    getJsonBooleanValue(objectJson, "singleton", true),
                    getJsonBooleanValue(objectJson, "enabled", true),
                    getJsonBooleanValue(objectJson, "override", false),
                    getJsonStringArray(objectJson, "conditions")));
            objectStart = arrayJson.indexOf('{', objectEnd + 1);
        }
        return entries;
    }

    static final class MetadataEntry {
        private final String className;
        private final String alias;
        private final int priority;
        private final boolean singleton;
        private final boolean enabled;
        private final boolean override;
        private final List<String> conditions;

        MetadataEntry(
                String className,
                String alias,
                int priority,
                boolean singleton,
                boolean enabled,
                boolean override,
                List<String> conditions) {
            this.className = className;
            this.alias = alias;
            this.priority = priority;
            this.singleton = singleton;
            this.enabled = enabled;
            this.override = override;
            this.conditions = conditions;
        }

        String getClassName() {
            return className;
        }

        String getAlias() {
            return alias;
        }

        int getPriority() {
            return priority;
        }

        boolean isSingleton() {
            return singleton;
        }

        boolean isEnabled() {
            return enabled;
        }

        boolean isOverride() {
            return override;
        }

        List<String> getConditions() {
            return conditions;
        }
    }

    private static int findMatchingBracket(String json, int startIndex, char openBracket, char closeBracket) {
        int count = 1;
        for (int i = startIndex + 1; i < json.length(); i++) {
            char current = json.charAt(i);
            if (current == openBracket) {
                count++;
            } else if (current == closeBracket) {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String getJsonStringValue(String objectJson, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objectJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return null;
        }
        int valueStart = objectJson.indexOf('"', keyIndex + keyPattern.length()) + 1;
        int valueEnd = objectJson.indexOf('"', valueStart);
        if (valueStart < valueEnd) {
            return unescapeJson(objectJson.substring(valueStart, valueEnd));
        }
        return null;
    }

    private static int getJsonIntValue(String objectJson, String key, int defaultValue) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objectJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return defaultValue;
        }
        int valueStart = keyIndex + keyPattern.length();
        while (valueStart < objectJson.length() && Character.isWhitespace(objectJson.charAt(valueStart))) {
            valueStart++;
        }
        int valueEnd = valueStart;
        while (valueEnd < objectJson.length()
                && (Character.isDigit(objectJson.charAt(valueEnd)) || objectJson.charAt(valueEnd) == '-')) {
            valueEnd++;
        }
        if (valueStart < valueEnd) {
            try {
                return Integer.parseInt(objectJson.substring(valueStart, valueEnd).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static boolean getJsonBooleanValue(String objectJson, String key, boolean defaultValue) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objectJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return defaultValue;
        }
        int valueStart = keyIndex + keyPattern.length();
        while (valueStart < objectJson.length() && Character.isWhitespace(objectJson.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart + 4 <= objectJson.length() && "true".equals(objectJson.substring(valueStart, valueStart + 4))) {
            return true;
        }
        if (valueStart + 5 <= objectJson.length() && "false".equals(objectJson.substring(valueStart, valueStart + 5))) {
            return false;
        }
        return defaultValue;
    }

    private static List<String> getJsonStringArray(String objectJson, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objectJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return Collections.emptyList();
        }
        int arrayStart = objectJson.indexOf('[', keyIndex + keyPattern.length());
        int arrayEnd = findMatchingBracket(objectJson, arrayStart, '[', ']');
        if (arrayStart == -1 || arrayEnd == -1) {
            return Collections.emptyList();
        }
        String arrayJson = objectJson.substring(arrayStart + 1, arrayEnd);
        List<String> values = new ArrayList<>();
        int current = 0;
        while (current < arrayJson.length()) {
            int stringStart = arrayJson.indexOf('"', current);
            if (stringStart == -1) {
                break;
            }
            int stringEnd = arrayJson.indexOf('"', stringStart + 1);
            if (stringEnd == -1) {
                break;
            }
            values.add(unescapeJson(arrayJson.substring(stringStart + 1, stringEnd)));
            current = stringEnd + 1;
        }
        return values;
    }

    private static String unescapeJson(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (escape) {
                switch (current) {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '/':
                        builder.append('/');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'u':
                        if (i + 4 < value.length()) {
                            String hex = value.substring(i + 1, i + 5);
                            try {
                                builder.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException ignored) {
                                builder.append(current);
                            }
                        } else {
                            builder.append(current);
                        }
                        break;
                    default:
                        builder.append(current);
                        break;
                }
                escape = false;
            } else if (current == '\\') {
                escape = true;
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }
}
