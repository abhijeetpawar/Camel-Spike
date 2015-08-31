package com.alpha.engine;

import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMap;
import com.alpha.mapping.MessageMapping;
import javafx.util.Pair;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.LinkedHashMap;
import java.util.Map;

public class Transformer implements Processor {
    private MessageMapping messageMapping;

    public Transformer(MessageMapping messageMapping) {
        this.messageMapping = messageMapping;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> output = new LinkedHashMap<>();
        Map<String, Object> input = exchange.getIn().getBody(Map.class);

        Mapping mapping = messageMapping.get();

        for (MessageMap messageMap : mapping.getMessageMaps()) {
            Pair<String, Object> pair = messageMap.map(input);
            output.put(pair.getKey(), pair.getValue());
        }
        exchange.getOut().setHeader("RECORD_TYPE", mapping.getSourceId());
        exchange.getOut().setBody(output);
    }
}
