package com.alpha.configuration;

import com.alpha.processor.CircuitBreaker;
import com.alpha.processor.CountProcessor;
import com.alpha.processor.CountService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransportRouteTest extends CamelTestSupport {
    @EndpointInject(uri = "mock:result")
    protected MockEndpoint mockEndpoint;

    protected ProducerTemplate template;

    private String fromUri = "mq:queue:buffer";
    private String toUri = "mq:queue:out";

    @Mock
    private CountService countService;

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Before
    public void setup() throws Exception {
        ActiveMQConnectionFactory activeMQConnectionFactory
                = new ActiveMQConnectionFactory("tcp://localhost:61616");
/*
    ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    This doesn't work. mq:queue:buffer looses message when exception is thrown.
 */
        context.addComponent("mq", JmsComponent.jmsComponentTransacted(activeMQConnectionFactory));

        context.getRouteDefinition("route2").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:result");
            }
        });

        template = new DefaultProducerTemplate(context);

        context.start();
        template.start();
    }

    @After
    public void tearDown() throws Exception {
        template.stop();
        context.stop();
    }


    @Test
    public void shouldRetryWhenCircuitBreakerTrips() throws Exception {
        when(countService.getCount())
                .thenReturn(6, 1);

        mockEndpoint.expectedMessageCount(1);

        template.sendBody(fromUri, "test message");

        mockEndpoint.assertIsSatisfied(3000);
    }


    @Override
    protected RouteBuilder createRouteBuilder() {
        CountProcessor countProcessor = new CountProcessor(countService);
        CircuitBreaker circuitBreaker = new CircuitBreaker(5, 2);

        return new TransportRoute(fromUri, toUri, countProcessor, circuitBreaker);
    }

}