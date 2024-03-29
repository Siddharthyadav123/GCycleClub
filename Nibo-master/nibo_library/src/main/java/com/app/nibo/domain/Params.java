package com.app.nibo.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class backed by a Map, used to pass parameters to {@link com.app.nibo.domain.base.BaseUseCase} instances.
 */
public class Params {
    public static final Params EMPTY = Params.create();

    private final Map<String, Object> parameters = new HashMap<>();

    private Params() {
    }

    public static Params create() {
        return new Params();
    }

    public void putInt(@NonNull String key,
                       @NonNull int value) {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public int getInt(@Nullable String key,
                      @Nullable int defaultValue) {
        final Object object = parameters.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return (int) object;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public void putString(@NonNull String key,
                          @NonNull String value) {
        parameters.put(key, value);
    }


    public void putList(@NonNull String key,
                          @NonNull List<String> value) {
        parameters.put(key, value);
    }


    public String getString(@NonNull String key,
                            @Nullable String defaultValue) {
        final Object object = parameters.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return (String) object;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public void putLong(@NonNull String key,
                        @NonNull long value) {
        parameters.put(key, value);
    }

    public void putFloat(@NonNull String key,
                        @NonNull float value) {
        parameters.put(key, value);
    }

    public void putData(@NonNull String key,
                        Object value) {
        parameters.put(key, value);
    }

    public float getFloat(@NonNull String key,
                        @Nullable long defaultValue) {
        final Object object = parameters.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return (float) object;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public Object getData(@NonNull String key,
                            @Nullable Object defaultValue) {
        final Object object = parameters.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return  object;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public double getDouble(@NonNull String key,
                          @Nullable long defaultValue) {
        final Object object = parameters.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return (double) object;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public long getLong(@NonNull String key,
                        @Nullable long defaultValue) {
        final Object object = parameters.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return (long) object;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}