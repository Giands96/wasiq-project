package com.wasiq.inmobiliaria.services;


import com.wasiq.inmobiliaria.models.Role;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> findByRole(Role role, int page, int size) {
        return userRepository.findByRole(
                role, PageRequest.of(page, size)
        );
    }

    public Page<User> findAllByOrderByIdDesc(int page, int size) {
        return userRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
    }

}
