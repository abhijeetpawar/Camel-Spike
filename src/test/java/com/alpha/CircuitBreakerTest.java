package com.alpha;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultConsumerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port:0")
public class CircuitBreakerTest {

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

    // Trip Limit = 5; Retry = 5 Seconds
    @Test
    public void shouldTestCircuitBreaker() throws Exception {
        String message = "Some Random Message";

        int[] arr =  {6};
        for (int anArr : arr) {
            producerTemplate.sendBodyAndHeader("active-mq:queue:in", message, "PENDING_COUNT", anArr);
        }

        Thread.sleep(10000);

        int count = 0;
        for (int anArr : arr) {
            Exchange exchange = consumerTemplate.receive("active-mq:queue:out", 500L);
            if (exchange != null)
                count++;
        }

        assertThat(count, is(1));
    }
}
