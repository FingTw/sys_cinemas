package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.ports.in.PasswordPolicyUseCase;
import com.example.cinema.iam.domain.entities.PasswordPolicy;
import com.example.cinema.iam.domain.repositories.PasswordPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordPolicyServiceImpl implements PasswordPolicyUseCase {

    private final PasswordPolicyRepository passwordPolicyRepository;

    @Override
    @Transactional(readOnly = true)
    public PasswordPolicy getPolicy() {
        return passwordPolicyRepository.findById("default-policy")
                .orElseGet(() -> new PasswordPolicy("default-policy", 8, true, true, true, true, LocalDateTime.now()));
    }

}
