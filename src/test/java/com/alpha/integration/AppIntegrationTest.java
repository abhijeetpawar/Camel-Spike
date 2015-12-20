package com.alpha.integration;

import com.alpha.Application;
import com.alpha.mapping.EngineMessage;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultConsumerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
This test can also be started as a regular Spring integration test. with @RunWith(SpringJUnit4ClassRunner.class)
How does CamelSpringJUnit4ClassRunner work? Working with advise in integration tests.
Do we need camel integration tests?
*/

@RunWith(CamelSpringJUnit4ClassRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@SpringApplicationConfiguration(classes = Application.class)
//@ContextConfiguration(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@UseAdviceWith
//@MockEndpoints("active-mq:*")
public class AppIntegrationTest {

    @Autowired
    private CamelContext camelContext;

    private ProducerTemplate producerTemplate;
    private ConsumerTemplate consumerTemplate;

    @Before
    public void setUp() throws Exception {
        producerTemplate = new DefaultProducerTemplate(camelContext);
        consumerTemplate = new DefaultConsumerTemplate(camelContext);

        producerTemplate.start();
        consumerTemplate.start();
    }

    @After
    public void tearDown() throws Exception {
        producerTemplate.stop();
        consumerTemplate.stop();
    }

    @Test
    public void shouldTestApplication() throws Exception {
        int messageCount = 5;
        String message = "{\"username\":\"xyz\",\"password\":\"abc\"}";

        List<EngineMessage> actual = new ArrayList<>();

        for (int i = 0; i < messageCount; i++) {
            producerTemplate.sendBody("active-mq:queue:in", message);
        }

        for (int i = 0; i < messageCount; i++) {
            EngineMessage engineMessage = consumerTemplate.receiveBody("active-mq:queue:out", 1000, EngineMessage.class);
            if (engineMessage != null)
                actual.add(engineMessage);
        }

        assertThat(actual.size()).isEqualTo(messageCount);
    }
}
