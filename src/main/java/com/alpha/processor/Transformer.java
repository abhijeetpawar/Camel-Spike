package com.alpha.processor;

import com.alpha.mapping.EngineMessage;
import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import javafx.util.Pair;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class Transformer implements Processor {
    private MessageMapping messageMapping;

    public Transformer(MessageMapping messageMapping) {
        this.messageMapping = messageMapping;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EngineMessage output = new EngineMessage();
        EngineMessage input = EngineMessage.from(exchange.getIn().getBody(Map.class));

        Mapping mapping = messageMapping.get();

        for (FieldMapping fieldMapping : mapping.getFieldMappings()) {
            Pair<String, Object> pair = fieldMapping.map(input);
            output.put(pair.getKey(), pair.getValue());
        }
        exchange.getIn().setBody(output);
    }
}
