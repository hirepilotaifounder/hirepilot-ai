package com.hirepilot.hirepilotai.service;

import com.hirepilot.hirepilotai.dto.UserRegistrationRequest;
import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import com.hirepilot.hirepilotai.dto.UserRegistrationRequest;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRegistrationRequest request) {
        Optional<User> existingEmail = userRepository.findByEmail(request.getEmail());
        if (existingEmail.isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setMobile(request.getMobile());
        return userRepository.save(user);
    }
}