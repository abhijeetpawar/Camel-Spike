package com.alpha.configuration;

import com.alpha.engine.Reader;
import com.alpha.engine.Transformer;
import com.alpha.mapping.*;
import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;

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
                new MessageMap("username", "USERNAME", TransformerFunction.asString()),
                new MessageMap("password", "PASSWORD", TransformerFunction.asString())
        );
    }

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {

            @BeanInject
            Reader reader;

            @Override
            public void configure() throws Exception {
                from("servlet:///hello")
                        .process(reader)
                        .process(new Transformer(messageMapping()))
                        .marshal().json(JsonLibrary.Jackson)
                .to(ExchangePattern.InOnly,"activemq:queue:in");

                from("activemq:queue:in")
                        .process(exchange -> System.out.println("setp 1"))
                        .process(exchange -> System.out.println("setp 2"))
                        .to("activemq:queue:out");

                from("activemq:queue:out")
                        .process(exchange -> System.out.println("done"))
                        .to("file://src/main/resources/out");
            }
        };
    }

    @Bean
    CamelContextConfiguration contextConfiguration() {
        return camelContext -> {
            camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        };
    }
}

