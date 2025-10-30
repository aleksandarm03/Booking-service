package com.example.bookingsservice.messaging;

import com.example.bookingsservice.model.Booking;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BookingCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(BookingCreatedListener.class);

    @RabbitListener(queues = "booking.created.q")
    public void onBookingCreated(Booking booking) {
        if (booking == null) {
            log.warn("[RabbitMQ] Received null booking payload");
            return;
        }
        log.info("[RabbitMQ] Received booking.created -> id={}", booking.getId());
    }
}



