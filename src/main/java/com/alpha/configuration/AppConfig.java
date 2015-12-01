package com.alpha.configuration;

import com.alpha.processor.CircuitBreaker;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public SpringCamelContext camelContext(ApplicationContext applicationContext) throws Exception {
        SpringCamelContext camelContext = new SpringCamelContext(applicationContext);
        camelContext.addRoutes(routeBuilder());
        return camelContext;
    }

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {
            final CircuitBreaker circuitBreaker = new CircuitBreaker(5);

            @Override
            public void configure() throws Exception {

                from("active-mq:queue:in")
                        .process(exchange -> System.out.println("process ..."))
                        .to("active-mq:queue:buffer");

                from("active-mq:queue:buffer?transacted=true")
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
1. Use activeMq PooledConnectionFactory (manages conns, sessions & producers)
2. Set transacted flag=true(not necessary) && add jmsTransactionManager for transactions.
3. Add cacheLevelName=CACHE_CONSUMER as PooledConnFactory does not cache consumers
4. Check redelivery policy of ActiveMq for
- maximumRedeliveries

http://tmielke.blogspot.in/2012/03/camel-jms-with-transactions-lessons.html
 */

