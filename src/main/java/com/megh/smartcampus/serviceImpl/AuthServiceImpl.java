package com.megh.smartcampus.serviceImpl;

import com.megh.smartcampus.dto.request.LoginRequest;
import com.megh.smartcampus.dto.request.RegisterRequest;
import com.megh.smartcampus.dto.response.AuthResponse;
import com.megh.smartcampus.dto.response.UserResponse;
import com.megh.smartcampus.entity.Role;
import com.megh.smartcampus.entity.User;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.UserRepository;
import com.megh.smartcampus.security.JwtUtil;
import com.megh.smartcampus.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository  userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil         jwt;
    private final AuthenticationManager authManager;

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail().toLowerCase()))
            throw AppException.badRequest("Email already registered: " + req.getEmail());
        if (req.getStudentId() != null && !req.getStudentId().isBlank()
                && userRepo.existsByStudentId(req.getStudentId()))
            throw AppException.badRequest("Student ID already registered");

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail().toLowerCase().trim())
            .password(encoder.encode(req.getPassword()))
            .role(Role.ROLE_STUDENT)
            .phone(req.getPhone())
            .studentId(req.getStudentId())
            .department(req.getDepartment())
            .yearOfStudy(req.getYearOfStudy())
            .isActive(true)
            .build();

        User saved = userRepo.save(user);
        return buildResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getEmail().toLowerCase().trim(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw AppException.unauthorized("Invalid email or password");
        }
        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
            .orElseThrow(() -> AppException.unauthorized("User not found"));
        if (!user.isActive())
            throw AppException.unauthorized("Account is deactivated");
        return buildResponse(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwt.isStructurallyValid(refreshToken))
            throw AppException.unauthorized("Refresh token invalid or expired");
        String email = jwt.extractUsername(refreshToken);
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> AppException.unauthorized("User not found"));
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        UserDetails ud = new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(),
            List.of(new SimpleGrantedAuthority(user.getRole().name())));
        String access  = jwt.generateToken(ud, user.getRole().name(), user.getId());
        String refresh = jwt.generateRefreshToken(ud);
        return AuthResponse.builder()
            .accessToken(access).refreshToken(refresh)
            .tokenType("Bearer").expiresIn(jwt.getExpiration())
            .user(toDto(user)).build();
    }

    private UserResponse toDto(User u) {
        return UserResponse.builder()
            .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
            .fullName(u.getFullName()).email(u.getEmail()).role(u.getRole())
            .phone(u.getPhone()).studentId(u.getStudentId()).department(u.getDepartment())
            .yearOfStudy(u.getYearOfStudy()).isActive(u.getIsActive())
            .createdAt(u.getCreatedAt()).build();
    }
}
