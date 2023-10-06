package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getById(int bookingId, int userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (!(booking.getUser().getId() == userId || booking.getItem().getOwner().getId() == userId)) {
            throw new BookingNotBelongException("Не найдено подходящих бронирований для пользователя " + userId);
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByState(RequestBookingStatus requestBookingStatus, int userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Pageable pageable = PageRequest.of(from / size, size);
        switch (requestBookingStatus) {
            case ALL:
                return bookingRepository.findAllByUserIdOrderByStartDesc(userId, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByUserIdAndEndBeforeOrderByStartDesc(userId,
                                LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByUserIdAndStartAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(userId,
                                BookingStatus.WAITING, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(userId,
                                BookingStatus.REJECTED, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                throw new NoSuchStateForBookingSearchException("Неизвестный статус бронирования");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByStateForOwner(RequestBookingStatus requestBookingStatus, int userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Pageable pageable = PageRequest.of(from / size, size);
        switch (requestBookingStatus) {
            case ALL:
                return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId,
                                LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId,
                                BookingStatus.WAITING, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId,
                                BookingStatus.REJECTED, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                throw new NoSuchStateForBookingSearchException("Неизвестный статус бронирования");
        }
    }

    @Override
    public BookingResponseDto create(BookingRequestDto bookingRequestDto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        Booking booking = BookingMapper.fromDto(bookingRequestDto);
        booking.setUser(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approve(int bookingId, boolean approved, int userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("Подтверждение доступно только для владельца вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingNotWaitingForApprovalException("Вещь не ожидает подтверждения");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }
}