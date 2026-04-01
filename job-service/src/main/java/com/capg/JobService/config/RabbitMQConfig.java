package com.capg.JobService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "job-portal-exchange";
    public static final String USER_DELETED_QUEUE = "job.user.deleted.queue";
    public static final String USER_DELETED_ROUTING_KEY = "job.user.deleted";
    public static final String JOB_CREATED_ROUTING_KEY = "notification.job.created";
    public static final String JOB_DELETED_ROUTING_KEY = "application.job.deleted";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userDeletedQueue() {
        return new Queue(USER_DELETED_QUEUE);
    }

    @Bean
    public Binding userDeletedBinding(Queue userDeletedQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(userDeletedQueue)
                .to(exchange)
                .with(USER_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
