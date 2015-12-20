package com.alpha.configuration;

import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.processor.*;
import com.alpha.utils.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@SuppressWarnings("unused")
@Configuration
public class AppConfig {

    @Value("${route1.from}")
    String from1;

    @Value("${route1.to}")
    String to1;

    @Value("${route2.from}")
    String from2;

    @Value("${route2.to}")
    String to2;

    @Bean
    public RouteBuilder transformRoute() {
        MessageMapping messageMapping = () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("username", "USERNAME", Function.identity()),
                new FieldMapping("password", "PASSWORD", Function.identity())
        );

        Reader reader = new Reader(new JsonMapper(new ObjectMapper()));
        Transformer transformer = new Transformer(messageMapping);

        return new TransformRoute(from1, to1, reader, transformer);
    }

    @Bean
    public RouteBuilder transportRoute() {
        CountProcessor countProcessor = new CountProcessor(new CountService());
        CircuitBreaker circuitBreaker = new CircuitBreaker(5, 2);

        return new TransportRoute(from2, to2, countProcessor, circuitBreaker);
    }


    @Bean
    CamelContextConfiguration contextConfiguration() {
        return camelContext -> {
            ActiveMQConnectionFactory activeMQConnectionFactory =
                    new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
//                  new ActiveMQConnectionFactory("admin", "admin", ActiveMQConnection.DEFAULT_BROKER_URL);

            RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            redeliveryPolicy.setMaximumRedeliveries(RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES);
            activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);

            PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
            pooledConnectionFactory.setMaxConnections(2);
            pooledConnectionFactory.setMaximumActiveSessionPerConnection(2);

            camelContext.addComponent("active-mq", JmsComponent.jmsComponentTransacted(pooledConnectionFactory));
            camelContext.getShutdownStrategy().setTimeout(2);
        };
    }
}

/*
http://tmielke.blogspot.in/2012/03/camel-jms-with-transactions-lessons.html
 */

