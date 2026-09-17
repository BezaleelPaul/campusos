package com.campusos.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal flat-JSON helper (no new dependencies for the API). */
public final class Json {
    private Json() {}

    /** Parses a flat object like {"a":"x","n":12} into raw string values (quotes stripped). */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> out = new LinkedHashMap<>();
        String t = json == null ? "" : json.strip();
        if (!t.startsWith("{") || !t.endsWith("}")) {
            return out;
        }
        for (String part : splitTopLevel(t.substring(1, t.length() - 1))) {
            int colon = part.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = unquote(part.substring(0, colon).strip());
            String val = part.substring(colon + 1).strip();
            out.put(key, unquote(val));
        }
        return out;
    }

    private static List<String> splitTopLevel(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inStr = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inStr = !inStr;
            }
            if (c == ',' && !inStr) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (!cur.isEmpty()) {
            out.add(cur.toString());
        }
        return out;
    }

    private static String unquote(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return s;
    }

    public static String q(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
