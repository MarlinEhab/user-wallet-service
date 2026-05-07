package com.marketplace.rabbitmq;

import com.rabbitmq.client.*;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Singleton
@Startup
public class RabbitMQConsumer {

    private static final String EXCHANGE_NAME = "booking_exchange";
    private static final String HOST = "localhost";

    private Connection connection;
    private Channel channel;

    @PostConstruct
    public void startListening() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);

            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println("RabbitMQ Consumer started, waiting for messages...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received notification: " + message);
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            System.err.println("RabbitMQ Consumer error: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopListening() {
        try {
            if (channel != null) channel.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            System.err.println("Error closing RabbitMQ connection: " + e.getMessage());
        }
    }
}
