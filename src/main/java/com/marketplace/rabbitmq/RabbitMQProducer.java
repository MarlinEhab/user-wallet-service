package com.marketplace.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQProducer {

    private static final String EXCHANGE_NAME = "booking_exchange";
    private static final String HOST = "localhost";

    public static void sendBookingConfirmation(Long userId, String message) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);

                String fullMessage = "{\"userId\":" + userId + ",\"message\":\"" + message + "\",\"type\":\"BOOKING_CONFIRMED\"}";
                channel.basicPublish(EXCHANGE_NAME, "", null, fullMessage.getBytes());
                System.out.println("Sent confirmation to user " + userId + ": " + message);
            }
        } catch (Exception e) {
            System.err.println("RabbitMQ error: " + e.getMessage());
        }
    }

    public static void sendBookingRejection(Long userId, String message) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);

                String fullMessage = "{\"userId\":" + userId + ",\"message\":\"" + message + "\",\"type\":\"BOOKING_REJECTED\"}";
                channel.basicPublish(EXCHANGE_NAME, "", null, fullMessage.getBytes());
                System.out.println("Sent rejection to user " + userId + ": " + message);
            }
        } catch (Exception e) {
            System.err.println("RabbitMQ error: " + e.getMessage());
        }
    }
}
