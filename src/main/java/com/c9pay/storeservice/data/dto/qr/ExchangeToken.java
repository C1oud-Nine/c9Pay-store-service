package com.c9pay.storeservice.data.dto.qr;

import lombok.Data;

@Data
public class ExchangeToken {
    private String content;
    private Long expiredAt;
    private String sign;
}
