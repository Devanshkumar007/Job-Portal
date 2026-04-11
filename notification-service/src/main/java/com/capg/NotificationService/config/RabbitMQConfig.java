package com.capg.NotificationService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    public static final String APP_INTERVIEW_SCHEDULED_QUEUE = "notification.application.interview.scheduled.queue";
    public static final String APP_OFFER_SENT_QUEUE = "notification.application.offer.sent.queue";
    public static final String USER_PASSWORD_RESET_QUEUE = "notification.user.password.reset.queue";

    // Routing Keys
    public static final String APP_CREATED_KEY = "notification.application.created";
    public static final String APP_STATUS_KEY = "notification.application.status";
    public static final String JOB_CREATED_KEY = "notification.job.created";
    public static final String APP_INTERVIEW_SCHEDULED_KEY = "notification.application.interview.scheduled";
    public static final String APP_OFFER_SENT_KEY = "notification.application.offer.sent";
    public static final String USER_PASSWORD_RESET_KEY = "job.user.password.reset.requested";

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

    @Bean
    public Queue appInterviewScheduledQueue() {
        return new Queue(APP_INTERVIEW_SCHEDULED_QUEUE);
    }

    @Bean
    public Queue appOfferSentQueue() {
        return new Queue(APP_OFFER_SENT_QUEUE);
    }

    @Bean
    public Queue userPasswordResetQueue() {
        return new Queue(USER_PASSWORD_RESET_QUEUE);
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

    @Bean
    public Binding appInterviewScheduledBinding() {
        return BindingBuilder
                .bind(appInterviewScheduledQueue())
                .to(exchange())
                .with(APP_INTERVIEW_SCHEDULED_KEY);
    }

    @Bean
    public Binding appOfferSentBinding() {
        return BindingBuilder
                .bind(appOfferSentQueue())
                .to(exchange())
                .with(APP_OFFER_SENT_KEY);
    }

    @Bean
    public Binding userPasswordResetBinding() {
        return BindingBuilder
                .bind(userPasswordResetQueue())
                .to(exchange())
                .with(USER_PASSWORD_RESET_KEY);
    }

    // JSON converter
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
