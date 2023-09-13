package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.fromDto(userDto);
        try {
            return UserMapper.toDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public UserDto update(UserDto userDto, long userId) {
        User stored = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Optional.ofNullable(userDto.getName()).ifPresent(stored::setName);
        Optional.ofNullable(userDto.getEmail()).ifPresent(stored::setEmail);
        if (isValid(UserMapper.toDto(stored))) {
            try {
                return UserMapper.toDto(userRepository.save(stored));
            } catch (DataIntegrityViolationException e) {
                throw new DataConflictException(e.getMessage());
            }
        } else {
            throw new InvalidDataException("Некорректные данные для обновления");
        }
    }

    @Override
    public void delete(long userId) {
        userRepository.deleteById(userId);
    }

    private boolean isValid(UserDto userDto) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        return violations.isEmpty();
    }
}
