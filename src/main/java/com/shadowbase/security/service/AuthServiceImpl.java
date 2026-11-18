package com.shadowbase.security.service;

import com.shadowbase.security.dto.AuthResponse;
import com.shadowbase.security.dto.LoginRequest;
import com.shadowbase.security.dto.RegisterRequest;
import com.shadowbase.security.entity.User;
import com.shadowbase.security.repository.UserRepository;
import com.shadowbase.security.jwt.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User();

        user.setUsername(request.username());
        user.setEmail(request.email());

        // Store hashed password, never plain text.
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        // Never allow users to register themselves as ADMIN.
        user.setRole("EMPLOYEE");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return AuthResponse.success(
                "Registration successful",
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalStateException("Account is disabled");
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return AuthResponse.success(
                "Login successful",
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}