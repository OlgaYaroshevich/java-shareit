package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailConflictException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

@Repository
public class UserInMemoryStorage implements UserStorage {
    private final HashMap<Long, User> storageMap = new HashMap<>();
    private long id = 1;

    @Override
    public User create(User user) {
        if (isEmailUsed(user)) {
            throw new EmailConflictException("Email уже занят");
        }
        user.setId(id++);
        storageMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(long userId) {
        if (!storageMap.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }
        return storageMap.get(userId);
    }

    @Override
    public List<User> getAll() {
        return  new ArrayList<>(storageMap.values());
    }

    @Override
    public User update(User user, long userId) {
        if (isEmailUsed(user)) {
            throw new EmailConflictException("Email уже занят");
        }
        User stored = storageMap.get(userId);
        stored.setName(user.getName());
        stored.setEmail(user.getEmail());
        return stored;
    }

    @Override
    public void delete(long userId) {
        storageMap.remove(userId);
    }

    private boolean isEmailUsed(User user) {
        return storageMap.values().stream()
                .filter(storedUser -> !storedUser.getId().equals(user.getId()))
                .anyMatch(storedUser -> storedUser.getEmail().equalsIgnoreCase(user.getEmail()));
    }
}
