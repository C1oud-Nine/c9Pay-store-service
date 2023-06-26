package com.c9pay.storeservice.controller;

import com.c9pay.storeservice.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/{store-id}/product")
public class ProductApiController {
    @GetMapping
    public ResponseEntity<List<ProductDetails>> getProducts(
            @PathVariable("store-id") int storeId
    ) {
        // todo Product 조회 로직 작성
        List<ProductDetails> productDetailsList = List.of(
                new ProductDetails(1L, "Item1", 1000),
                new ProductDetails(2L, "Item2", 2000),
                new ProductDetails(3L, "Item3", 3000)
        );

        return ResponseEntity.ok(productDetailsList);
    }

    @PostMapping
    public ResponseEntity<List<ProductDetails>> addProducts(
            @PathVariable("store-id") int storeId,
            @RequestBody ProductForm productForm
    ) {
        // todo Product 등록 로직 작성
        // todo Product 조회 로직 작성
        List<ProductDetails> productDetailsList = List.of(
                new ProductDetails(1L, "Item1", 1000),
                new ProductDetails(2L, "Item2", 2000),
                new ProductDetails(3L, "Item3", 3000),
                new ProductDetails(4L, productForm.getName(), productForm.getPrice())
        );

        return ResponseEntity.ok(productDetailsList);
    }

    @PostMapping("/sale")
    public ResponseEntity<PaymentInfo> sellProducts(
            @PathVariable("store-id") int storeId,
            @RequestBody PurchaseInfo purchaseInfo
    ) {
        // todo QR 정보를 바탕으로 인증 서비스에서 사용자 식별번호 획득

        // todo 구매정보를 바탕으로 결제정보 생성
        List<ProductSaleInfo> productSaleInfoList = purchaseInfo.getPurchaseList().stream()
                .map((p) -> new ProductSaleInfo(p.getProductId(), "store" + p.getProductId(),
                        (int) p.getProductId() * 1000, (int) p.getProductId() * p.getAmount() * 1000))
                .toList();
        int totalAmount = productSaleInfoList.stream().mapToInt(ProductSaleInfo::getAmount).sum();

        // todo 사용자 식별번호와 가게 주인 식별번호를 코인 서비스 송금으로 넘김

        return ResponseEntity.ok(new PaymentInfo(productSaleInfoList, totalAmount));
    }
}
