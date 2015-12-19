package com.alpha.configuration;

import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.processor.*;
import com.alpha.utils.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

public class CustomRouteTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint mockEndpoint;

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Before
    public void setup() throws Exception {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        context.addComponent("active-mq", JmsComponent.jmsComponent(activeMQConnectionFactory));

        context.getRouteDefinition("route2").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:result");
//                mockEndpoints();
            }
        });

        template.start();
        context.start();
    }

    @After
    public void tearDown() throws Exception {
        context.stop();
    }


    @Test
    public void shouldTestCustomRoute() throws Exception {
        mockEndpoint.expectedMessageCount(1);

        template.sendBody("active-mq:queue:in", "{\"username\":\"xyz\",\"password\":\"abc\"}");

        mockEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {

        MessageMapping messageMapping = () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("username", "USERNAME", Function.identity()),
                new FieldMapping("password", "PASSWORD", Function.identity())
        );

        Reader reader = new Reader(new JsonMapper(new ObjectMapper()));
        CountProcessor countProcessor = new CountProcessor(new CountService());
        CircuitBreaker circuitBreaker = new CircuitBreaker(5);
        Transformer transformer = new Transformer(messageMapping);


        return new CustomRoute(reader, countProcessor, circuitBreaker, transformer);
    }
}

// http://opensourceconnections.com/blog/2014/04/24/correctly-using-camels-advicewith-in-unit-tests/
