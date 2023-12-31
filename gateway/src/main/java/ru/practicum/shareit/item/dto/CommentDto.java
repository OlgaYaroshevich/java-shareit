package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Integer id;
    @NotBlank(message = "Текст комментария не должен быть пустым")
    private String text;
    private String authorName;
    private LocalDateTime created;
}