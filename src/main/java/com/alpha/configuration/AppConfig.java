package com.alpha.configuration;

import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.processor.*;
import com.alpha.utils.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@SuppressWarnings("unused")
@Configuration
public class AppConfig {

    @Bean
    public RouteBuilder routeBuilder() {
        MessageMapping messageMapping = () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("username", "USERNAME", Function.identity()),
                new FieldMapping("password", "PASSWORD", Function.identity())
        );

        Reader reader = new Reader(new JsonMapper(new ObjectMapper()));
        CountProcessor countProcessor = new CountProcessor(new CountService());
        CircuitBreaker circuitBreaker = new CircuitBreaker(5, 2);
        Transformer transformer = new Transformer(messageMapping);

        return new CustomRoute(reader, countProcessor, circuitBreaker, transformer);
    }

    @Bean
    CamelContextConfiguration contextConfiguration() {
        return camelContext -> {
            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(
                    "admin", "admin", ActiveMQConnection.DEFAULT_BROKER_URL);

            RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            redeliveryPolicy.setMaximumRedeliveries(RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES);
            activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);

            PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
            pooledConnectionFactory.setMaxConnections(4);

            camelContext.getShutdownStrategy().setTimeout(2);
            camelContext.addComponent("active-mq", JmsComponent.jmsComponentTransacted(pooledConnectionFactory));
        };
    }
}

/*
http://tmielke.blogspot.in/2012/03/camel-jms-with-transactions-lessons.html
 */

