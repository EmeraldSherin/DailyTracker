package com.dailytracker.tracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dailytracker.tracker.model.User;
import com.dailytracker.tracker.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public String registerUser(String name, String email, String rawPassword) {
        if (emailExists(email)) {
            return "Email already registered";
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(name, email, hashedPassword);
        user.setRole("USER");

        userRepository.save(user);

        return "success";
    }
}