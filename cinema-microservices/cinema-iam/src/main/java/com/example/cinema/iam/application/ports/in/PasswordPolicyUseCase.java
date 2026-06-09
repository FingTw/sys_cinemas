package com.example.cinema.iam.application.ports.in;

import com.example.cinema.iam.domain.entities.PasswordPolicy;

public interface PasswordPolicyUseCase {
    PasswordPolicy getPolicy();
}
