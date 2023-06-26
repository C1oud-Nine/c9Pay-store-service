package com.c9pay.storeservice.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreDetailList {
    private List<StoreDetails> storeDetails;
}
