package com.example.cinema.application.ports.out;

public interface CryptoPort {
    String getPublicKeyPem();
    String decrypt(String encryptedBase64);
}
