package com.shadowbase.security.service;

import com.shadowbase.security.dto.AuthResponse;
import com.shadowbase.security.dto.LoginRequest;
import com.shadowbase.security.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}