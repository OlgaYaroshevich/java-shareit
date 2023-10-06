package ru.practicum.shareit.exception;

public class BookingNotWaitingForApprovalException extends RuntimeException {
    public BookingNotWaitingForApprovalException(String message) {
        super(message);
    }
}
