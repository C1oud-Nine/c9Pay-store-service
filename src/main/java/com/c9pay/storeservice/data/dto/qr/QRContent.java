package com.c9pay.storeservice.data.dto.qr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRContent {
    private String serialNumber;
    private long expiredAt;
}
