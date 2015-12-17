package com.alpha.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CountProcessor implements Processor {
    private final CountService service;

    @Autowired
    public CountProcessor(CountService service) {
        this.service = service;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader("PENDING_COUNT", service.getCount());
    }
}
