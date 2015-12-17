package com.alpha.configuration;

import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.processor.*;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.BeanInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@SuppressWarnings("unused")
@Configuration
public class AppConfig {

    @Bean
    public SpringCamelContext camelContext(ApplicationContext applicationContext) throws Exception {
        return new SpringCamelContext(applicationContext);
    }

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {
            @BeanInject
            private Reader reader;

            @BeanInject
            private CountProcessor countProcessor;

            private MessageMapping messageMapping = () -> new Mapping(
                    "CREATE_USER",
                    new FieldMapping("username", "USERNAME", Function.identity()),
                    new FieldMapping("password", "PASSWORD", Function.identity())
            );

//            final private CountProcessor countProcessor = new CountProcessor(countService);
            final private CircuitBreaker circuitBreaker = new CircuitBreaker(5);
            final private Transformer transformer = new Transformer(messageMapping);

            @Override
            public void configure() throws Exception {

                from("active-mq:queue:in")
                        .process(reader)
                        .process(transformer)
                        .process(exchange -> System.out.println("process ..."))
                        .to("active-mq:queue:buffer");

                from("active-mq:queue:buffer?transacted=true")
                        .process(countProcessor)
                        .process(circuitBreaker)
                        .process(exchange -> System.out.println("Done"))
                        .to("active-mq:queue:out");
            }
        };
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

