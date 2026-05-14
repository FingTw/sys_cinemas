package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.dto.UpdateProfileRequest;
import com.example.cinema.iam.application.dto.UserProfileDTO;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.iam.application.ports.in.UserProfileUseCase;
import com.example.cinema.iam.domain.entities.User;
import com.example.cinema.iam.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class UserProfileService implements UserProfileUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String userId) {
        log.info("Truy van thong tin ca nhan User ID: [{}]", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ClientException("Khong tim thay nguoi dung."));
            return mapToDTO(user);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi truy van profile User [{}]: {}", userId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van thong tin ca nhan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(String userId, UpdateProfileRequest request) {
        log.info("Cap nhat thong tin ca nhan User ID: [{}]", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ClientException("Khong tim thay nguoi dung."));

            // Validate email
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new ClientException("Email khong duoc de trong.");
            }

            if (!request.getEmail().contains("@")) {
                throw new ClientException("Email khong hop le.");
            }

            user.setEmail(request.getEmail());
            userRepository.save(user);

            log.info("Da cap nhat email thanh cong cho User [{}]", userId);
            return mapToDTO(user);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi cap nhat profile User [{}]: {}", userId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi cap nhat thong tin ca nhan: " + e.getMessage(), e);
        }
    }

    private UserProfileDTO mapToDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .build();
    }
}
