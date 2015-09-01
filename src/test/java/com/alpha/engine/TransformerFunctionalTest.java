package com.alpha.engine;

import com.alpha.mapping.Mapping;
import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.mapping.TransformerFunction;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
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

    MessageMapping messageMapping = () -> new Mapping("CREATE_USER", new FieldMapping("username", "USERNAME", TransformerFunction.asString()), new FieldMapping("password", "PASSWORD", TransformerFunction.asString()));

    @Test
    public void shouldTransformUsingGivenMapping() throws InterruptedException {
        Map<String, Object> inputPayload = new LinkedHashMap<String, Object>() {{
            put("username", "abc");
            put("password", "xyz");
        }};

        resultEndpoint1.expectedBodiesReceived("{\"USERNAME\":\"abc\",\"PASSWORD\":\"xyz\"}");

        template.sendBody(inputPayload);

        resultEndpoint1.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .process(new Transformer(messageMapping)).marshal().json(JsonLibrary.Jackson)
                        .to("mock:result1");
            }
        };
    }

}