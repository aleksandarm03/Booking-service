package com.example.bookingsservice.service;

import com.example.bookingsservice.client.UserClient;
import com.example.bookingsservice.model.Booking;
import com.example.bookingsservice.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserClient userClient;

    public BookingService(BookingRepository bookingRepository, UserClient userClient) {
        this.bookingRepository = bookingRepository;
        this.userClient = userClient;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    @Transactional
    @CircuitBreaker(name = "userService", fallbackMethod = "createFallback")
    @Retry(name = "userService")
    public Booking create(Booking booking) {
        if (booking.getEndTime() != null && booking.getStartTime() != null &&
                !booking.getEndTime().isAfter(booking.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        // Validate user exists via Feign
        userClient.getUserById(booking.getUserId());
        return bookingRepository.save(booking);
    }

    @Transactional
    public Optional<Booking> update(Long id, Booking update) {
        return bookingRepository.findById(id).map(existing -> {
            existing.setUserId(update.getUserId());
            existing.setStartTime(update.getStartTime());
            existing.setEndTime(update.getEndTime());
            return bookingRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            return false;
        }
        bookingRepository.deleteById(id);
        return true;
    }

    public Map<String, Object> getDetails(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return null;
        }
        Booking booking = bookingOpt.get();
        UserClient.UserDto user = userClient.getUserById(booking.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("booking", booking);
        result.put("user", user);
        return result;
    }

    // Fallback signature must match original method with Throwable as last param
    protected Booking createFallback(Booking booking, Throwable t) {
        throw new IllegalStateException("User service unavailable. Cannot create booking right now.");
    }
}

