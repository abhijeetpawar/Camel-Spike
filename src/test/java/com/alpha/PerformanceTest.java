package com.alpha;

import com.alpha.engine.Reader;
import com.alpha.engine.Transformer;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.mapping.TransformerFunction;
import com.alpha.utils.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import javax.jms.ConnectionFactory;

public class PerformanceTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    private Reader reader = new Reader(new JsonMapper(new ObjectMapper()));

    private int messageCount = 1000;

    public MessageMapping testMapping() {
        return () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("username", "USERNAME", TransformerFunction.asString()),
                new FieldMapping("password", "PASSWORD", TransformerFunction.asString())
        );
    }

    @Test
    public void basicLatencyTest() throws Exception {
        String message = "{\"username\":\"abc\",\"password\":\"xyz\"}";

        resultEndpoint.expectedMessageCount(messageCount);

        for (int i = 0; i < messageCount; i++) {
            template.sendBodyAndHeader("active-mq:queue:in", message, "TIME", System.currentTimeMillis());
        }

        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                /* Chain */
                from("active-mq:queue:in")
                        .process(reader)
                        .process(new Transformer(testMapping()))
                        .process(new Processor() {
                            int lowerBound = messageCount / 4;
                            int upperBound = (3 * messageCount) / 4;

                            int messageCnt = 0;
                            long totalLatency = 0L;

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                Long messageCreationTime = (Long) exchange.getIn().getHeader("TIME");

                                if (messageCnt > lowerBound && messageCnt < upperBound) {
                                    totalLatency += System.currentTimeMillis() - messageCreationTime;
                                }
                                messageCnt++;

                                if (messageCnt == messageCount) {
                                    log.info("****************************************************");
                                    log.info("Average Latency = " + ((double) totalLatency / (messageCnt / 2) + "ms"));
                                    log.info("****************************************************");
                                }
                            }
                        }).to("mock:result");
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        camelContext.addComponent("active-mq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        return camelContext;
    }
}

