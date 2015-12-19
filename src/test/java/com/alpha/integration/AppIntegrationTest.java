package com.alpha.integration;

import com.alpha.Application;
import com.alpha.processor.CountService;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultConsumerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;


@RunWith(CamelSpringJUnit4ClassRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@MockEndpoints("log:*")
public class AppIntegrationTest {

    @Autowired
    private CamelContext camelContext;

    @EndpointInject(uri = "mock:advised")
    protected MockEndpoint mockEndpoint;

    private ProducerTemplate producerTemplate;

    @Before
    public void setUp() throws Exception {
        producerTemplate = new DefaultProducerTemplate(camelContext);

        camelContext.getRouteDefinition("route2").adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:advised");
            }
        });

        producerTemplate.start();
    }

    @After
    public void tearDown() throws Exception {
        producerTemplate.stop();
    }

    @Test
    public void shouldTestRoute() throws Exception {
        int messageCount = 5;
        String message = "{\"username\":\"xyz\",\"password\":\"abc\"}";

        mockEndpoint.expectedMessageCount(messageCount);

        for (int i = 0; i < messageCount; i++) {
            producerTemplate.sendBody("active-mq:queue:in", message);
        }

        mockEndpoint.assertIsSatisfied();
    }
}
