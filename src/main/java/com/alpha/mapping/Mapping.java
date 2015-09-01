package com.alpha.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mapping {
    private final String sourceId;
    private final List<FieldMapping> fieldMappings;

    public Mapping(String sourceId, FieldMapping... fieldMappings) {
        this.sourceId = sourceId;
        this.fieldMappings = new ArrayList<>();
        Collections.addAll(this.fieldMappings, fieldMappings);
    }

    public String getSourceId() {
        return sourceId;
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }
}
