package com.capg.NotificationService.config;

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

    // Queues
    public static final String APP_CREATED_QUEUE = "notification.application.created.queue";
    public static final String APP_STATUS_QUEUE = "notification.application.status.queue";
    public static final String JOB_CREATED_QUEUE = "notification.job.created.queue";

    // Routing Keys
    public static final String APP_CREATED_KEY = "notification.application.created";
    public static final String APP_STATUS_KEY = "notification.application.status";
    public static final String JOB_CREATED_KEY = "notification.job.created";

    // Exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Queues
    @Bean
    public Queue appCreatedQueue() {
        return new Queue(APP_CREATED_QUEUE);
    }

    @Bean
    public Queue appStatusQueue() {
        return new Queue(APP_STATUS_QUEUE);
    }

    @Bean
    public Queue jobCreatedQueue() {
        return new Queue(JOB_CREATED_QUEUE);
    }

    // Bindings
    @Bean
    public Binding appCreatedBinding() {
        return BindingBuilder
                .bind(appCreatedQueue())
                .to(exchange())
                .with(APP_CREATED_KEY);
    }

    @Bean
    public Binding appStatusBinding() {
        return BindingBuilder
                .bind(appStatusQueue())
                .to(exchange())
                .with(APP_STATUS_KEY);
    }

    @Bean
    public Binding jobCreatedBinding() {
        return BindingBuilder
                .bind(jobCreatedQueue())
                .to(exchange())
                .with(JOB_CREATED_KEY);
    }

    // JSON converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}