package com.c9pay.storeservice.certificate;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.Signature;

public interface KeyAlgorithm {
    public KeyPair createKeyPair();
    public Signature getSignature();
    public Cipher getCipher();
    public int getMaxEncodingSize();
}
