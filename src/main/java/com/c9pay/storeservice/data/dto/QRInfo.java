package com.c9pay.storeservice.data.dto;

import com.c9pay.storeservice.data.dto.certificate.CertificateResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRInfo {
    private CertificateResponse certificate;
    private String content;
}
