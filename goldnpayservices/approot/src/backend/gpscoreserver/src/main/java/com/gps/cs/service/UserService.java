package com.gps.cs.service;

import com.gps.cs.dto.UserDtos.*;
import com.gps.cs.entity.User;
import com.gps.cs.exception.ApiException;
import com.gps.cs.mapper.UserMapper;
import com.gps.cs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Client self-service: profile and password. The email cannot be changed here. */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public UserResponse profile(Long userId) {
        return UserMapper.toResponse(load(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest r) {
        User u = load(userId);
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone());
        u.setAddress(r.address());
        return UserMapper.toResponse(users.save(u));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest r) {
        User u = load(userId);
        if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) {
            throw ApiException.badRequest("CURRENT_PASSWORD_INCORRECT", "Current password is incorrect");
        }
        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);
        tokenService.revokeAll(userId); // sign out everywhere; the client logs in again
    }

    public User load(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
    }
}
