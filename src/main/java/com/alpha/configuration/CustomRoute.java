package com.alpha.configuration;

import com.alpha.processor.CircuitBreaker;
import com.alpha.processor.CountProcessor;
import com.alpha.processor.Reader;
import com.alpha.processor.Transformer;
import org.apache.camel.builder.RouteBuilder;

public class CustomRoute extends RouteBuilder {
    final private Reader reader;
    final private CountProcessor countProcessor;
    final private CircuitBreaker circuitBreaker;
    final private Transformer transformer;

    public CustomRoute(Reader reader, CountProcessor countProcessor, CircuitBreaker circuitBreaker, Transformer transformer) {
        this.reader = reader;
        this.countProcessor = countProcessor;
        this.circuitBreaker = circuitBreaker;
        this.transformer = transformer;
    }

    @Override
    public void configure() throws Exception {
        from("active-mq:queue:in")
                .routeId("route1")
                .process(reader)
                .process(transformer)
                .process(exchange -> System.out.println("process ..."))
                .to("active-mq:queue:buffer");

        from("active-mq:queue:buffer?transacted=true")
                .routeId("route2")
                .process(countProcessor)
                .process(circuitBreaker)
                .process(exchange -> System.out.println("Done"))
                .to("active-mq:queue:out");
    }
}
