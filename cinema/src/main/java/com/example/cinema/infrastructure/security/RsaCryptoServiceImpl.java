package com.example.cinema.infrastructure.security;

import com.example.cinema.application.ports.out.CryptoPort;

import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RsaCryptoServiceImpl implements CryptoPort {

    private static final Logger log = LoggerFactory.getLogger(RsaCryptoServiceImpl.class);

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RsaCryptoServiceImpl() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
            log.info("Khoi tao thanh cong cap khoa RSA-2048 cho phien lam viec nay.");
        } catch (Exception e) {
            log.error("Loi khi khoi tao khoa RSA: {}", e.getMessage(), e);
            throw new ServerException("Loi nghiem trong: Khong the khoi tao cap khoa RSA: " + e.getMessage(), e);
        }
    }

    public String getPublicKeyPem() {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        // Chuyen sang dinh dang chuan PEM
        return "-----BEGIN PUBLIC KEY-----\n" +
                base64.replaceAll("(.{64})", "$1\n") +
                "\n-----END PUBLIC KEY-----";
    }

    public String decrypt(String encryptedBase64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encryptedBase64);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(bytes));
        } catch (Exception e) {
            log.warn("Loi giai ma Payload: {}", e.getMessage(), e);
            throw new ClientException("Du lieu khong hop le hoac da bi can thiep! " + e.getMessage(), e);
        }
    }
}
