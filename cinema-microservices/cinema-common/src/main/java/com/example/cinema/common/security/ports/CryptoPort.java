package com.example.cinema.common.security.ports;

public interface CryptoPort {
    String getPublicKeyPem();
    String decrypt(String encryptedBase64);
}
