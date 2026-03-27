package com.capg.ApplicationService.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "job-portal-exchange";
    public static final String USER_DELETED_QUEUE = "application.user.deleted.queue";
    public static final String JOB_DELETED_QUEUE = "application.job.deleted.queue";

    @Bean
    public Queue jobDeletedQueue() {
        return new Queue(JOB_DELETED_QUEUE);
    }

    @Bean
    public Binding jobDeletedBinding(Queue jobDeletedQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(jobDeletedQueue)
                .to(exchange)
                .with("job.deleted");
    }

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
                .with("user.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}