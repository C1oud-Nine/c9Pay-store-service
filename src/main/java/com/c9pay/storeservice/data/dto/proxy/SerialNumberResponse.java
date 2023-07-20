package com.c9pay.storeservice.data.dto.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerialNumberResponse {
    private UUID serialNumber;
}
