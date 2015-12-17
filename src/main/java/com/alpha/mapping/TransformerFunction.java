package com.alpha.mapping;

import java.util.function.Function;

public class TransformerFunction {
    public static Function<?, String> asString() {
        return Object::toString;
    }
}
