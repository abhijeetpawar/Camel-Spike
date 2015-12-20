package com.alpha.configuration;

import com.alpha.processor.Reader;
import com.alpha.processor.Transformer;
import org.apache.camel.builder.RouteBuilder;

public class TransformRoute extends RouteBuilder {
    final private String fromUri;
    final private String toUri;
    final private Reader reader;
    final private Transformer transformer;

    public TransformRoute(String fromUri, String toUri, Reader reader, Transformer transformer) {
        this.fromUri = fromUri;
        this.toUri = toUri;
        this.reader = reader;
        this.transformer = transformer;
    }

    @Override
    public void configure() throws Exception {
        from(fromUri)
                .routeId("route1")
                .process(reader)
                .process(transformer)
                .process(exchange -> System.out.println("ROUTE 1"))
                .to(toUri);
    }
}
