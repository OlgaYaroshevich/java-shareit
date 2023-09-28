package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestGetResponseDto> getAllByRequestorId(int userId, int from, int size);

    List<ItemRequestGetResponseDto> getAll(int userId, int from, int size);

    ItemRequestGetResponseDto getById(int userId, int itemRequestId);

    ItemRequestCreateResponseDto create(ItemRequestCreateDto itemRequestCreateDto, int userId);
}
