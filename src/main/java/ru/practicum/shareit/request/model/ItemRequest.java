package ru.practicum.shareit.request.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {
    private long id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
