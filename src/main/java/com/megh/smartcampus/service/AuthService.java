package com.megh.smartcampus.service;

import com.megh.smartcampus.dto.request.LoginRequest;
import com.megh.smartcampus.dto.request.RegisterRequest;
import com.megh.smartcampus.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refresh(String refreshToken);
}
