package com.alpha.processor;

import com.alpha.utils.JsonMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

// Perhaps a DataFormat?
@Component
public class Reader implements Processor {
    private JsonMapper jsonMapper;

    @Autowired
    public Reader(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        Map<Object, Object> objectMap = jsonMapper.jsonToMap(body);
        exchange.getIn().setBody(objectMap);
    }
}
