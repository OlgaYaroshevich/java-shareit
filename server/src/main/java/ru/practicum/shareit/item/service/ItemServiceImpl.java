package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(ItemDto itemDto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = ItemMapper.fromDto(itemDto);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setItemRequest(itemRequest);
        }
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, int userId, int itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        Comment comment = CommentMapper.fromDto(commentDto);
        if (bookingRepository.findAllApprovedByItemIdAndUserId(itemId, userId, LocalDateTime.now()).isEmpty()) {
            throw new CommentWithoutBookingException("Комментарии можно оставлять только к тем вещам, на которые было бронирование");
        }
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(int userId, int itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        ItemDto itemDto = ItemMapper.toDto(item);
        itemDto = (item.getOwner().getId() == userId) ? addBookingInfo(itemDto) : itemDto;
        itemDto = addComments(itemDto);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllByOwnerId(int userId, int from, int size) {
        return itemRepository.findAllByOwnerId(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemMapper::toDto)
                .map(this::addBookingInfo)
                .map(this::addComments)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllBySearchText(String searchText, int from, int size) {
        if (searchText.isBlank()) {
            return Collections.EMPTY_LIST;
        }
        return itemRepository.findBySearchText(searchText, PageRequest.of(from, size)).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(ItemDto itemDto, int itemId, int userId) {
        Item stored = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!stored.getOwner().getId().equals(userId)) {
            throw new ItemNotBelongToUserException("Редактирование вещи доступно только владельцу");
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(stored::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(stored::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(stored::setAvailable);
        try {
            return ItemMapper.toDto(itemRepository.save(stored));
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(e.getMessage());
        }
    }

    @Override
    public void delete(int itemId) {
        itemRepository.deleteById(itemId);
    }

    private ItemDto addBookingInfo(ItemDto itemDto) {
        List<Booking> bookings = bookingRepository.findAllByItemId(itemDto.getId());
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);
        itemDto.setNextBooking(nextBooking != null ? ItemDto.ItemBooking.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getUser().getId())
                .build() : null);
        itemDto.setLastBooking(lastBooking != null ? ItemDto.ItemBooking.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getUser().getId())
                .build() : null);
        return itemDto;
    }

    private ItemDto addComments(ItemDto itemDto) {
        itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId()).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList()));
        return itemDto;
    }
}
