package com.alpha.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mapping {
    private final String sourceId;
    private final List<MessageMap> messageMaps;

    public Mapping(String sourceId, MessageMap... messageMaps) {
        this.sourceId = sourceId;
        this.messageMaps = new ArrayList<>();
        Collections.addAll(this.messageMaps, messageMaps);
    }

    public String getSourceId() {
        return sourceId;
    }

    public List<MessageMap> getMessageMaps() {
        return messageMaps;
    }
}
