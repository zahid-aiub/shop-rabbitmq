package com.example.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchanges.inventory}")
    private String inventoryExchange;

    @Value("${rabbitmq.exchanges.payment}")
    private String paymentExchange;

    @Value("${rabbitmq.queues.inventory-failed}")
    private String inventoryFailedQueue;

    @Value("${rabbitmq.queues.payment-completed}")
    private String paymentCompletedQueue;

    @Value("${rabbitmq.queues.payment-failed}")
    private String paymentFailedQueue;

    @Value("${rabbitmq.routing-keys.inventory-failed}")
    private String inventoryFailedRoutingKey;

    @Value("${rabbitmq.routing-keys.payment-completed}")
    private String paymentCompletedRoutingKey;

    @Value("${rabbitmq.routing-keys.payment-failed}")
    private String paymentFailedRoutingKey;

    // Exchanges
    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(inventoryExchange, true, false);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentExchange, true, false);
    }

    // Queues
    @Bean
    public Queue inventoryFailedQueue() {
        return new Queue(inventoryFailedQueue, true);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(paymentCompletedQueue, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(paymentFailedQueue, true);
    }

    // Bindings
    @Bean
    public Binding inventoryFailedBinding() {
        return BindingBuilder
                .bind(inventoryFailedQueue())
                .to(inventoryExchange())
                .with(inventoryFailedRoutingKey);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(paymentCompletedQueue())
                .to(paymentExchange())
                .with(paymentCompletedRoutingKey);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(paymentFailedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
