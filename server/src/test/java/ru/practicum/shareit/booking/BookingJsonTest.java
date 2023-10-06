package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingJsonTest {
    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseDtoJacksonTester;

    @Test
    void bookingResponseDtoTest() throws Exception {
        LocalDateTime timestamp = LocalDateTime.now().withNano(0);
        LocalDateTime startTimestamp = timestamp;
        LocalDateTime endTimestamp = timestamp.plusDays(1);

        BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .id(5)
                .start(startTimestamp)
                .end(endTimestamp)
                .status(BookingStatus.APPROVED)
                .booker(BookingResponseDto.BookingResponseUserDto.builder()
                        .id(10)
                        .build())
                .item(BookingResponseDto.BookingResponseItemDto.builder()
                        .id(15)
                        .name("Гараж")
                        .build())
                .build();

        JsonContent<BookingResponseDto> jsonContent = bookingResponseDtoJacksonTester.write(bookingResponseDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start").isEqualTo(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(startTimestamp));
        assertThat(jsonContent).extractingJsonPathStringValue("$.end").isEqualTo(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(endTimestamp));
        assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.APPROVED.toString());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(10);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(15);
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo("Гараж");
    }
}
