package com.example.inventory.config;

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

    @Value("${rabbitmq.exchanges.order}")
    private String orderExchange;

    @Value("${rabbitmq.exchanges.inventory}")
    private String inventoryExchange;

    @Value("${rabbitmq.queues.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.routing-keys.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.queues.product-import}")
    private String productImportQueue;

    @Value("${rabbitmq.routing-keys.product-import}")
    private String productImportRoutingKey;

    @Value("${rabbitmq.routing-keys.inventory-failed}")
    private String inventoryFailedRoutingKey;

    // Exchanges
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange, true, false);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(inventoryExchange, true, false);
    }

    // Queues
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue, true);
    }

    @Bean
    public Queue inventoryFailedQueue() {
        return new Queue("inventory-failed-queue", true);
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
    public Queue productImportQueue() {
        return new Queue(productImportQueue, true);
    }

    @Bean
    public Binding productImportBinding() {
        return BindingBuilder
                .bind(productImportQueue())
                .to(inventoryExchange())
                .with(productImportRoutingKey);
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
