package com.alpha.exceptions;

public class CircuitBreakerTripException extends RuntimeException {
    public CircuitBreakerTripException() {
        super("Circuit breaker threshold reached");
    }
}
