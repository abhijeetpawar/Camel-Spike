package com.alpha.processor;

import com.alpha.configuration.CustomRoute;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CircuitBreakerTest extends CamelTestSupport {
    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint mockEndpoint;

    private CountService countService;

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
    public void shouldRetryWhenCircuitBreakerTrips() throws Exception {
        when(countService.getCount())
                .thenReturn(6, 1);

        mockEndpoint.expectedMessageCount(1);

        template.sendBody("active-mq:queue:buffer", "test message");

        mockEndpoint.assertIsSatisfied(2000);
    }


    @Override
    protected RouteBuilder createRouteBuilder() {
        Reader reader = mock(Reader.class);
        Transformer transformer = mock(Transformer.class);

        CircuitBreaker circuitBreaker = new CircuitBreaker(5, 2);

        countService = mock(CountService.class);
        CountProcessor countProcessor = new CountProcessor(countService);

        return new CustomRoute(reader, countProcessor, circuitBreaker, transformer);
    }

}