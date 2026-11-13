package com.example.recycling.service;

import com.example.recycling.model.User;
import com.example.recycling.repository.RecyclingBatchRepository;
import com.example.recycling.repository.UserRepository;
import com.example.recycling.dto.RegisterUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public User register(RegisterUserRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите имя пользователя");
        }
        if (request.getUsername() == null || !request.getUsername().matches("[A-Za-z0-9._-]{3,50}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Логин должен содержать 3–50 символов: латинские буквы, цифры, точку, дефис или подчёркивание");
        }
        String password = request.getPassword();
        if (password == null || password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пароль должен содержать от 8 до 72 символов, включая буквы и цифры");
        }
        String username = request.getUsername().trim().toLowerCase();
        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Этот логин уже занят");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return repository.save(user);
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
