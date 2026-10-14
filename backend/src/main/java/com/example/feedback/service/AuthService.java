package com.example.feedback.service;

import com.example.feedback.dto.AuthResponse;
import com.example.feedback.dto.LoginRequest;
import com.example.feedback.dto.RegisterRequest;
import com.example.feedback.dto.UserResponse;
import com.example.feedback.entity.Role;
import com.example.feedback.entity.User;
import com.example.feedback.exception.BadRequestException;
import com.example.feedback.exception.DuplicateResourceException;
import com.example.feedback.exception.ResourceNotFoundException;
import com.example.feedback.repository.UserRepository;
import com.example.feedback.security.AppUserDetails;
import com.example.feedback.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.password()),
                // Self-registration always creates a regular USER. Admins are provisioned separately.
                Role.USER
        );
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User no longer exists"));
        String token = jwtService.generateToken(principal);
        return AuthResponse.of(token, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User no longer exists"));
        return UserResponse.from(user);
    }
}
