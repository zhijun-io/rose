package io.zhijun.annotation.processor.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.zhijun.annotation.RosePropertyHint;

/**
 * Writes {@code additional-spring-configuration-metadata.json} fragments.
 */
public final class SpringConfigurationMetadataWriter {

    private SpringConfigurationMetadataWriter() {
    }

    public static String toJson(List<RosePropertyHint> hints) {
        List<RosePropertyHint> sorted = new ArrayList<RosePropertyHint>(hints);
        Collections.sort(sorted, (left, right) -> left.name().compareTo(right.name()));

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"properties\": [\n");
        for (int index = 0; index < sorted.size(); index++) {
            appendProperty(json, sorted.get(index));
            if (index < sorted.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]\n}\n");
        return json.toString();
    }

    private static void appendProperty(StringBuilder json, RosePropertyHint hint) {
        json.append("    {\n");
        List<String[]> fields = new ArrayList<String[]>();
        fields.add(new String[] {"name", hint.name()});
        fields.add(new String[] {"type", hint.type()});
        if (!hint.description().isEmpty()) {
            fields.add(new String[] {"description", hint.description()});
        }
        if (!hint.defaultValue().isEmpty()) {
            fields.add(new String[] {"defaultValue", hint.defaultValue()});
        }
        for (int index = 0; index < fields.size(); index++) {
            String[] field = fields.get(index);
            json.append("      \"").append(field[0]).append("\": \"")
                    .append(escapeJson(field[1])).append('"');
            if (index < fields.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("    }");
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
