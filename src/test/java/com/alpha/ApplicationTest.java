package com.alpha;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ApplicationTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result1")
    protected MockEndpoint resultEndpoint1;

    @EndpointInject(uri = "mock:result2")
    protected MockEndpoint resultEndpoint2;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Produce(uri = "direct:mid")
    protected ProducerTemplate template1;


    @Test
    public void testSendMatchingMessage() throws Exception {
        resultEndpoint1.expectedBodiesReceived("<matched/>");
        resultEndpoint2.expectedMessageCount(0);

        template.sendBodyAndHeader("<matched/>", "headerKey", "headerValue");

        resultEndpoint1.assertIsSatisfied();
        resultEndpoint2.assertIsSatisfied();
    }

    @Test
    public void testSendNotMatchingMessage() throws Exception {
        resultEndpoint1.expectedMessageCount(0);
        resultEndpoint2.expectedBodiesReceived("<notMatched/>");

        template.sendBodyAndHeader("<notMatched/>", "headerKey", "notMatchedHeaderValue");

        resultEndpoint1.assertIsSatisfied();
        resultEndpoint2.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").choice()
                        .when(header("headerKey").isEqualTo("headerValue")).to("direct:mid")
                        .when(header("headerKey").isNotEqualTo("headerValue")).to("mock:result2")
                        .otherwise().stop();

                from("direct:mid").to("mock:result1");
            }
        };
    }
}
