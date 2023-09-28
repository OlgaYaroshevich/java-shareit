package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@PathVariable int bookingId,
                                      @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping()
    public List<BookingResponseDto> getAllByState(@RequestParam(defaultValue = "ALL")
                                                  @Valid RequestBookingStatus state,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "20") @Positive int size,
                                                  @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.getAllByState(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByStateForOwner(@RequestParam(defaultValue = "ALL")
                                                          @Valid RequestBookingStatus state,
                                                          @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                          @RequestParam(defaultValue = "20") @Positive int size,
                                                          @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.getAllByStateForOwner(state, userId, from, size);
    }

    @PostMapping
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") int userId,
                                     @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.create(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@PathVariable int bookingId,
                                      @RequestParam boolean approved,
                                      @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.approve(bookingId, approved, userId);
    }
}
