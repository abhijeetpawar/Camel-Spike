package com.alpha.mapping;

import javafx.util.Pair;

import java.util.function.Function;

public class FieldMapping {
    private final String from;
    private final String to;
    private final Function transform;

    public FieldMapping(String from, String to, Function transform) {
        this.from = from;
        this.to = to;
        this.transform = transform;
    }

    public Pair<String, Object> map(EngineMessage input) {
        Object value = input.get(from);
        return new Pair<>(to, transform.apply(value));
    }
}
