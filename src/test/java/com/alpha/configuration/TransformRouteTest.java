package com.alpha.configuration;

import com.alpha.mapping.EngineMessage;
import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.processor.Reader;
import com.alpha.processor.Transformer;
import com.alpha.utils.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

public class TransformRouteTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint mockEndpoint;

    private String fromUri = "seda:in";
    private String toUri = "seda:out";


    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Before
    public void setup() throws Exception {
        context.getRouteDefinition("route1").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:result");
            }
        });

        context.start();
    }

    @After
    public void tearDown() throws Exception {
        context.stop();
    }

    @Test
    public void shouldTestCustomRoute() throws Exception {
        EngineMessage expectedMessage = EngineMessage.from(ImmutableMap.of("USERNAME", "xyz", "PASSWORD", "abc"));
        mockEndpoint.expectedMessageCount(1);

        template.sendBody(fromUri, "{\"username\":\"xyz\",\"password\":\"abc\"}");

        mockEndpoint.assertIsSatisfied();
        Exchange exchange = mockEndpoint.getExchanges().get(0);
        EngineMessage actualMessage = exchange.getIn().getBody(EngineMessage.class);

        Assertions.assertThat(actualMessage.getMessage()).isEqualTo(expectedMessage.getMessage());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        MessageMapping messageMapping = () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("username", "USERNAME", Function.identity()),
                new FieldMapping("password", "PASSWORD", Function.identity())
        );

        Reader reader = new Reader(new JsonMapper(new ObjectMapper()));
        Transformer transformer = new Transformer(messageMapping);

        return new TransformRoute(fromUri, toUri, reader, transformer);
    }
}
