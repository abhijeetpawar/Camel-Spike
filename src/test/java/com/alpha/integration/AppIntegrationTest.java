package com.alpha.integration;

import com.alpha.Application;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/*
How does CamelSpringJUnit4ClassRunner work? Is its only benefit Advice & Mock Endpoints?
Can we use Notify Builders with plain Spring Integration tests.
Do we need "Camel" integration tests?
*/

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AppIntegrationTest {

    @Autowired
    private CamelContext camelContext;

    private ProducerTemplate producerTemplate;

    @Before
    public void setUp() throws Exception {
        producerTemplate = new DefaultProducerTemplate(camelContext);
        producerTemplate.start();
    }

    @After
    public void tearDown() throws Exception {
        producerTemplate.stop();
    }

    @Test
    public void shouldTestApplication() throws Exception {
        int messageCount = 5;
        String message = "{\"username\":\"xyz\",\"password\":\"abc\"}";

        /*
        Notify Builder provides an alternative to AdviceWith.
        Used to testing existing routes without modifying them
        */
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .from("active-mq:queue:out").whenCompleted(messageCount).create();

        for (int i = 0; i < messageCount; i++) {
            producerTemplate.sendBody("active-mq:queue:in", message);
        }

        notify.matches(5, TimeUnit.SECONDS);
    }
}
