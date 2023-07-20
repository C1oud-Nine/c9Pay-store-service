package com.c9pay.storeservice.data.dto.sale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {
    List<ProductSaleInfo> productSaleInfoList;
    int totalAmount;
}
