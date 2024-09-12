package com.drevotiuk.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for application-specific beans.
 * Configures messaging-related beans such as message converters and AMQP
 * templates.
 */
@Configuration
public class AppConfig {

  /**
   * Creates a {@link MessageConverter} bean that converts messages to and from
   * JSON format using Jackson. This is used to serialize and deserialize messages
   * sent over RabbitMQ.
   *
   * @return a {@link Jackson2JsonMessageConverter} for JSON message conversion
   */
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * Creates an {@link AmqpTemplate} bean for RabbitMQ communication.
   * It uses the provided {@link ConnectionFactory} and configures it with the
   * custom {@link MessageConverter} for JSON message conversion.
   *
   * @param connectionFactory the RabbitMQ {@link ConnectionFactory} used to
   *                          create the {@link RabbitTemplate}
   * @return a configured {@link RabbitTemplate} for sending and receiving
   *         messages
   */
  @Bean
  public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter());
    return rabbitTemplate;
  }
}
