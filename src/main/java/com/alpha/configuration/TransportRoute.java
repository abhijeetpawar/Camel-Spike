package com.alpha.configuration;

import com.alpha.processor.CircuitBreaker;
import com.alpha.processor.CountProcessor;
import org.apache.camel.builder.RouteBuilder;

public class TransportRoute extends RouteBuilder {
    final private String fromUri;
    final private String toUri;
    final private CountProcessor countProcessor;
    final private CircuitBreaker circuitBreaker;

    public TransportRoute(String fromUri, String toUri, CountProcessor countProcessor, CircuitBreaker circuitBreaker) {
        this.fromUri = fromUri;
        this.toUri = toUri;
        this.countProcessor = countProcessor;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void configure() throws Exception {
        from(fromUri)
                .routeId("route2")
                .process(countProcessor)
                .process(circuitBreaker)
                .process(exchange -> System.out.println("ROUTE 2"))
                .to(toUri);
    }
}
