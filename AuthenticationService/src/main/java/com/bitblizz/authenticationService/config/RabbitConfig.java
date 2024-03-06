package com.bitblizz.authenticationService.config;


import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class RabbitConfig {

    private final ConnectionFactory connectionFactory;

    public static final String AUTH_QUEUE = "auth.queue";
    public static final String EXCHANGE = "auth.exchange";

    public static final String ROUTING_KEY = "auth.*";


    @Bean
    public TopicExchange authExchange(){
        return new TopicExchange(EXCHANGE);
    }
    @Bean
    public Queue authQueue() {
        return new Queue(AUTH_QUEUE);
    }

    @Bean
    public Binding authBinding() {
        return BindingBuilder.bind(authQueue()).to(authExchange()).with(ROUTING_KEY);
    }


    @Bean
    public AmqpTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonConverter());
        return factory;
    }

    @Bean
    public MessageConverter jacksonConverter() {
        return new Jackson2JsonMessageConverter();
    }

}

