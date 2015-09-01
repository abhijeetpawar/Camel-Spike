package com.alpha.configuration;

import com.alpha.engine.Reader;
import com.alpha.engine.Transformer;
import com.alpha.mapping.Mapping;
import com.alpha.mapping.FieldMapping;
import com.alpha.mapping.MessageMapping;
import com.alpha.mapping.TransformerFunction;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.BeanInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;

@Configuration
public class AppConfig {
    private static final String CAMEL_URL_MAPPING = "/camel/*";
    private static final String CAMEL_SERVLET_NAME = "CamelServlet";

    @Bean
    public SpringCamelContext camelContext(ApplicationContext applicationContext) throws Exception {
        SpringCamelContext camelContext = new SpringCamelContext(applicationContext);
        camelContext.addRoutes(routeBuilder());
        return camelContext;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), CAMEL_URL_MAPPING);
        registration.setName(CAMEL_SERVLET_NAME);
        return registration;
    }

    @Bean
    public MessageMapping messageMapping() {
        return () -> new Mapping(
                "CREATE_USER",
                new FieldMapping("username", "USERNAME", TransformerFunction.asString()),
                new FieldMapping("password", "PASSWORD", TransformerFunction.asString())
        );
    }

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {

            @BeanInject
            Reader reader;

            @BeanInject
            MessageMapping messageMapping;

            @Override
            public void configure() throws Exception {
                from("servlet:///hello")
                        .process(reader)
                        .process(new Transformer(messageMapping))
                        .marshal().json(JsonLibrary.Jackson)
                        .to(ExchangePattern.InOnly, "active-mq:queue:in");

                from("active-mq:queue:in")
                        .process(exchange -> System.out.println("process ..."))
                        .to("active-mq:queue:out");

                from("active-mq:queue:out")
                        .process(exchange -> System.out.println("done ..."))
                        .to("file://src/main/resources/out");
            }
        };
    }

    @Bean
    CamelContextConfiguration contextConfiguration() {
        return camelContext -> {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    "admin", "admin", ActiveMQConnection.DEFAULT_BROKER_URL);

            camelContext.addComponent("active-mq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        };
    }
}

