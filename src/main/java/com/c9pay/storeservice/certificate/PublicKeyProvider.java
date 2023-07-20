package com.c9pay.storeservice.certificate;

import javax.crypto.Cipher;
import java.security.PublicKey;
import java.security.Signature;

public interface PublicKeyProvider {
    public PublicKey getPublicKey() throws KeyAcquisitionFailureException;
    public Signature getSignature();
    public Cipher getCipher();
}
