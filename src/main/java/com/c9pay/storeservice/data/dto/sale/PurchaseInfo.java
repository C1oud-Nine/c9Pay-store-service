package com.c9pay.storeservice.data.dto.sale;

import com.c9pay.storeservice.data.dto.QRInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseInfo {
    List<ProductPurchaseInfo>  purchaseList;
    QRInfo qrInfo;
}
