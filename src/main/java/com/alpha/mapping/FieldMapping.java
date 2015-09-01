package com.alpha.mapping;

import com.google.common.base.Function;
import javafx.util.Pair;

import java.util.Map;

public class FieldMapping {
    private final String from;
    private final String to;
    private final Function transform;

    public FieldMapping(String from, String to, Function transform) {
        this.from = from;
        this.to = to;
        this.transform = transform;
    }

    public Pair<String, Object> map(Map<String, Object> input) {
        Object value = input.get(from);
        return new Pair<>(to, transform.apply(value));
    }
}
