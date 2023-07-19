package com.c9pay.storeservice.certificate;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

@Component
public class Rsa implements KeyAlgorithm {

    private final int KEY_SIZE = 2048;

    @Override
    public KeyPair createKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();

            keyPairGenerator.initialize(KEY_SIZE, random);

            return keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Signature getSignature() {
        try {

            return Signature.getInstance("SHA1withRSA");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cipher getCipher() {
        try {

            return Cipher.getInstance("RSA");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMaxEncodingSize() {
        return KEY_SIZE / 8 - 11;
    }
}
