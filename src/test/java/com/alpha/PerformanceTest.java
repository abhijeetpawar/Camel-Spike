package com.alpha;

import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.processor.Reader;
import com.alpha.processor.Transformer;
import com.alpha.utils.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.function.Function;

public class PerformanceTest extends CamelTestSupport {

//    public static final String BUFFER = "active-mq:queue:buffer?cacheLevelName=CACHE_CONSUMER&transferExchange=true";
    public static final String BUFFER = "direct:buffer";
//    public static final String BUFFER_OUT = "active-mq:queue:out";
    public static final String BUFFER_OUT = "direct:out";

    public static final String DIRECT_IN = "direct:in";
    public static final String MOCK_RESULT = "mock:result";

    @EndpointInject(uri = MOCK_RESULT)
    protected MockEndpoint mockEndpoint;

    private final Reader reader = new Reader(new JsonMapper(new ObjectMapper()));

    public MessageMapping testMapping() {
        return () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("name.firstName", "FIRST_NAME", Function.identity()),
                new FieldMapping("name.lastName", "LAST_NAME", Function.identity()),
                new FieldMapping("age", "AGE", Function.identity())
        );
    }

    @Test
    public void basicLatencyTest() throws Exception {
        String message = "{\"name\":{\"firstName\":\"J\",\"lastName\":\"Barns\"},\"age\":20}";

        double messageCount = 1000;
        mockEndpoint.expectedMessageCount((int) messageCount);

        Thread sender = new Thread(() -> {
            for (int i = 0; i < messageCount; i++) {
                template.sendBody(DIRECT_IN, message);
            }
        });
        sender.start();

        double startTime = System.currentTimeMillis();
//        Thread.sleep(5000);
        mockEndpoint.assertIsSatisfied(10000L);
        double endTime = System.currentTimeMillis();

        double duration = (endTime - startTime) / 1000;

        System.out.println("*****************************************");
        System.out.println(" Total Time : " + duration + " s");
        System.out.println(" Mean : " + messageCount / duration + " per second");
        System.out.println("*****************************************");

    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(DIRECT_IN)
                        .process(reader)
                        .process(new Transformer(testMapping()))
                        .to(BUFFER);

                from(BUFFER)
                        .process(exchange -> exchange.getIn().setHeader("TIME", System.currentTimeMillis()))
                        .to(BUFFER_OUT);

                from(BUFFER_OUT)
                        .to(MOCK_RESULT);
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(
                "admin", "admin", "tcp://localhost:61616");
        activeMQConnectionFactory.setUseAsyncSend(true);

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(2);
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(2);

        camelContext.addComponent("active-mq", JmsComponent.jmsComponentTransacted(pooledConnectionFactory));
        return camelContext;
    }
}

