package com.hirepilot.hirepilotai.service;

import com.hirepilot.hirepilotai.dto.request.UserRegistrationRequest;
import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.repository.UserRepository;
import com.hirepilot.hirepilotai.security.JwtService;
import org.springframework.stereotype.Service;
import com.hirepilot.hirepilotai.dto.request.LoginRequest;
import com.hirepilot.hirepilotai.dto.response.LoginResponse;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder,JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobile(request.getMobile());
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = existingUser.get();
        if (!passwordEncoder.matches(request.getPassword(),user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        String token = jwtService.generateToken(user);
        return new LoginResponse(token);
    }
}