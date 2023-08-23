package ru.practicum.shareit.exception;

public class OwnershipViolationException extends RuntimeException {
    public OwnershipViolationException(String message) {
        super(message);
    }
}
