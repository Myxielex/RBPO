package com.example.recycling.service;

import com.example.recycling.model.User;
import com.example.recycling.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.example.recycling.repository.RecyclingBatchRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final RecyclingBatchRepository batchRepository;

    public UserService(
            UserRepository repository,
            RecyclingBatchRepository batchRepository) {
        this.repository = repository;
        this.batchRepository = batchRepository;
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public Optional<User> getById(Long id) {
        return repository.findById(id);
    }

    public User create(User user) {
        return repository.save(user);
    }

    public Optional<User> update(Long id, User newUser) {
        return repository.findById(id).map(user -> {
            user.setName(newUser.getName());
            return repository.save(user);
        });
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        if (batchRepository.existsByUser_Id(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }
}