package com.alpha.processor;

import com.alpha.mapping.*;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransformerFunctionalTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result1")
    protected MockEndpoint resultEndpoint1;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    MessageMapping messageMapping = () -> new Mapping(
            "CREATE_USER",
            new FieldMapping("name.firstName", "FIRST_NAME", TransformerFunction.asString()),
            new FieldMapping("name.lastName", "LAST_NAME", TransformerFunction.asString()),
            new FieldMapping("age", "AGE", TransformerFunction.asString())
    );

    @Test
    public void shouldTransformUsingGivenMapping() throws InterruptedException {
        Map<String, Object> inputPayload = new LinkedHashMap<String, Object>() {{
            put("name", new LinkedHashMap<String, Object>() {{
                put("firstName", "J");
                put("lastName", "Barns");
            }});
            put("age", 20);
        }};

        resultEndpoint1.expectedBodiesReceived("{\"FIRST_NAME\":\"J\",\"LAST_NAME\":\"Barns\",\"AGE\":\"20\"}");

        template.sendBody(inputPayload);

        resultEndpoint1.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .process(new Transformer(messageMapping))
                        .process(exchange -> {
                            EngineMessage engineMessage = exchange.getIn().getBody(EngineMessage.class);
                            exchange.getIn().setBody(engineMessage.getMessage());
                        })
                        .marshal().json(JsonLibrary.Jackson)
                        .to("mock:result1");
            }
        };
    }

}