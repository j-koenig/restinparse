package com.github.jmfayard;

import com.github.jmfayard.internal.Strings;
import com.github.jmfayard.model.ParsePtr;
import com.squareup.moshi.Moshi;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class ParseQuery {
    public final String className;
    public final Map<String, String> params;

    public ParseQuery(String className, Map<String, String> params) {
        this.className = className;
        this.params = params;
    }

    public static class Builder<T extends ParseClass> {
        final Map<String, Object> _where = new HashMap<String, Object>();
        final List<String> _includes = new ArrayList<String>();
        final int skip = 0; // not implemtented
        private final SimpleDateFormat dateFormat;
        final private String className;
        int limit = -1; // no limit
        private String order = "";

        public Builder(String className) {
            this.className = className;
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public static String join(String delimiter, List<String> list) {
            if (list == null || list.size() == 0) {
                return "";
            }
            String result = list.get(0);
            for (int i = 1; i < list.size(); i++) {
                result += delimiter;
                result += list.get(i);
            }
            return result;
        }

        public
        @NotNull
        Builder<T> equalToInt(@NotNull T key, int value) {
            _where.put(key.toString(), value);
            return this;
        }

        public
        @NotNull
        Builder<T> equalToBoolean(@NotNull T key, boolean value) {
            _where.put(key.toString(), value);
            return this;
        }

        public
        @NotNull
        Builder<T> equalToString(@NotNull T key, String value) {
            _where.put(key.toString(), value);
            return this;
        }

        public
        @NotNull
        Builder<T> equalToPtr(@NotNull T key, ParsePtr value) {
            _where.put(key.toString(), value);
            return this;
        }

        public
        @NotNull
        Builder<T> inRelationWith(ParsePtr relation, @NotNull String key) {
            Object value = objectOf("object", relation, "key", key);
            _where.put("$relatedTo", value);
            return this;
        }

        public
        @NotNull
        Builder<T> ascending(@NotNull T key) {
            order = key.toString();
            return this;
        }

        public
        @NotNull
        Builder<T> descending(@NotNull T key) {
            order = "-" + key.toString();
            return this;
        }

        public Builder<T> limit(int limit) {
            this.limit = Math.min(limit, 1000);
            return this;
        }

        public Builder<T> exists(T key) {
            Object encodedValue = objectOf("$exists", true, key);
            _where.put(key.toString(), encodedValue);
            return this;
        }

        public Builder<T> doesNotExist(T key) {
            Object encodedValue = objectOf("$exists", false, key);
            _where.put(key.toString(), encodedValue);
            return this;
        }

        public Builder<T> greaterThan(T key, int value) {
            Object encodedValue = objectOf("$gt", value, key);
            _where.put(key.toString(), encodedValue);
            return this;
        }

        public Builder<T> lessThan(T key, int value) {
            Object encodedValue = objectOf("$lt", value, key);
            _where.put(key.toString(), encodedValue);
            return this;
        }

        public Builder<T> newerThan(T key, Date date) {
            Object encodedValue = objectOf("$gt", datePtr(date), key);
            _where.put(key.toString(), encodedValue);
            return this;
        }

        public Builder<T> olderThan(T key, Date date) {
            Object encodedValue = objectOf("$lt", datePtr(date), key);
            _where.put(key.toString(), encodedValue);
            return this;
        }

        public Builder<T> include(@NotNull T key) {
            _includes.add(key.toString());
            return this;
        }

        public
        @NotNull
        ParseQuery build() {
            Map<String, String> params = new HashMap<String, String>();
            Moshi moshi = new Moshi.Builder().build();

            if (_where.size() > 0) {
                params.put("where", moshi.adapter(Map.class).toJson(_where));
            }

            if (limit > 0) {
                params.put("limit", Integer.toString(limit));
            }

            if (!Strings.isNullOrEmpty(order)) {
                params.put("order", order);
            }

            String include = join(",", _includes);
            if (!Strings.isNullOrEmpty(include)) {
                params.put("include", include);
            }

            return new ParseQuery(className, params);
        }

        private Object datePtr(@NotNull Date date) {
            return objectOf("__type", "Date", "iso", dateFormat.format(date));
        }

        private Map objectOf(@NotNull String key1, @NotNull Object value1, @NotNull String key2, @NotNull Object value2) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(key1, value1);
            map.put(key2, value2);
            return map;
        }

        private Map objectOf(@NotNull String key, @NotNull Object value, T existingKey) {
            Map<String, Object> map;
            String _k = existingKey.toString();
            if (_where.containsKey(_k) && _where.get(_k) instanceof Map) {
                map = ((Map<String, Object>) _where.get(_k));
            } else {
                map = new HashMap<String, Object>();
            }
            map.put(key, value);
            return map;
        }
    }
}