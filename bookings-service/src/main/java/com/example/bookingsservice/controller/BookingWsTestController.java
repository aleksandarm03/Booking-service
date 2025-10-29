package com.example.bookingsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BookingWsTestController {

    @MessageMapping("ping")
    @SendTo("/topic/pong")
    public String ping(String payload) {
        return payload == null ? "pong" : payload;
    }
}

