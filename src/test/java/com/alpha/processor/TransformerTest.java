package com.alpha.processor;

import com.alpha.mapping.EngineMessage;
import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.google.common.collect.ImmutableMap;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class TransformerTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    MessageMapping messageMapping = () -> new Mapping(
            "CREATE_USER",
            new FieldMapping("name.firstName", "FIRST_NAME", Function.identity()),
            new FieldMapping("name.lastName", "LAST_NAME", Function.identity()),
            new FieldMapping("age", "AGE", Function.identity())
    );

    @Test
    @Ignore
    public void shouldTransformUsingGivenMapping() throws InterruptedException {
        Map<String, Object> inputPayload = new LinkedHashMap<String, Object>() {{
            put("name", new LinkedHashMap<String, Object>() {{
                put("firstName", "J");
                put("lastName", "Barns");
            }});
            put("age", 20);
        }};

        EngineMessage expectedMessage = EngineMessage.from(ImmutableMap.of("FIRST_NAME", "J", "LAST_NAME", "Barns", "AGE", "20"));
        resultEndpoint.expectedBodiesReceived(expectedMessage);

        template.sendBody(inputPayload);

        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .process(new Transformer(messageMapping))
                        .to("mock:result");
            }
        };
    }

}