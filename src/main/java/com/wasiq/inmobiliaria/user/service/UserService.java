package com.wasiq.inmobiliaria.user.service;


import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
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

    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        //* Si el usuario solo quiere actualizar su telefono, se actualiza solo el telefono, etc... */
        if(user.getPassword() != null) existingUser.setPassword(user.getPassword());
        //*
        if(user.getPhoneNumber() != null) existingUser.setPhoneNumber(user.getPhoneNumber());


        return userRepository.save(existingUser);
    }

    public Page<User> findAllByOrderByIdDesc(int page, int size) {
        return userRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
    }

}
