package com.alpha.mapping;

import com.google.common.base.Function;

public class TransformerFunction {
    public static Function asString() {
        return o -> o.toString();
    }
}
