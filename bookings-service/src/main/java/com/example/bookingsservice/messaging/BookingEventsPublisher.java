package com.example.bookingsservice.messaging;

import com.example.bookingsservice.model.Booking;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventsPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;

    public BookingEventsPublisher(RabbitTemplate rabbitTemplate, TopicExchange topicExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    public void publishBookingCreated(Booking booking) {
        rabbitTemplate.convertAndSend(topicExchange.getName(), "booking.created", booking);
    }
}

