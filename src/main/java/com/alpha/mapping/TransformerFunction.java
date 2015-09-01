package com.alpha.mapping;

import com.google.common.base.Function;

public class TransformerFunction {
    public static Function<?, String> asString() {
        return o -> o.toString();
    }

    public static <T> Function<T, T> identity() {
        return o -> o;
    }
}
