package com.example.recycling.service;

import com.example.recycling.model.User;
import com.example.recycling.repository.RecyclingBatchRepository;
import com.example.recycling.repository.UserRepository;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final RecyclingBatchRepository batchRepository;

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public User create(User user) {
        validateUser(user);
        user.setName(user.getName().trim());
        return repository.save(user);
    }

    @Transactional
    public Optional<User> update(Long id, User newUser) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        validateUser(newUser);
        user.setName(newUser.getName().trim());
        return Optional.of(repository.save(user));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        if (batchRepository.existsByUser_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя удалить пользователя, у которого есть партии сдачи");
        }
        repository.deleteById(id);
        return true;
    }

    private void validateUser(User user) {
        if (user == null || user.getName() == null || user.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя пользователя не должно быть пустым");
        }
    }
}
