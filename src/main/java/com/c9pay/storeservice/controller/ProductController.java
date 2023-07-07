package com.c9pay.storeservice.controller;

import com.c9pay.storeservice.dto.product.ProductDetailList;
import com.c9pay.storeservice.dto.product.ProductDetails;
import com.c9pay.storeservice.dto.product.ProductForm;
import com.c9pay.storeservice.dto.sale.PaymentInfo;
import com.c9pay.storeservice.dto.sale.ProductSaleInfo;
import com.c9pay.storeservice.dto.sale.PurchaseInfo;
import com.c9pay.storeservice.entity.Store;
import com.c9pay.storeservice.service.ProductService;
import com.c9pay.storeservice.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/{store-id}/product")
public class ProductController {
    private final ProductService productService;
    private final StoreService storeService;
    @GetMapping
    public ResponseEntity<ProductDetailList> getProducts(
            @RequestAttribute UUID userId,
            @PathVariable("store-id") Long storeId
    ) {
        Optional<Store> storeOptional = storeService.findStore(storeId);

        // 가게 검증
        if (storeOptional.isEmpty() || !storeOptional.get().getUserId().equals(userId))
            return ResponseEntity.badRequest().build();

        List<ProductDetails> productDetailsList =
                productService.getProductDetailsByStoreId(storeId);

        return ResponseEntity.ok(new ProductDetailList(productDetailsList));
    }

    @PostMapping
    public ResponseEntity<ProductDetailList> addProduct(
            @RequestAttribute UUID userId,
            @PathVariable("store-id") Long storeId,
            @RequestBody ProductForm productForm
    ) {
        Optional<Store> storeOptional = storeService.findStore(storeId);

        // 가게 검증
        if (storeOptional.isEmpty() || !storeOptional.get().getUserId().equals(userId))
            return ResponseEntity.badRequest().build();

        productService.saveProduct(productForm.getName(), productForm.getPrice(), storeOptional.get());

        List<ProductDetails> productDetailsList = productService.getProductDetailsByStoreId(storeId);

        return ResponseEntity.ok(new ProductDetailList(productDetailsList));
    }

    @PostMapping("/{product-id}")
    public ResponseEntity<ProductDetails> updateProduct(
            @RequestAttribute UUID userId,
            @PathVariable("store-id") Long storeId,
            @PathVariable("product-id") Long productId,
            @RequestBody ProductForm productForm
    ) {
        Optional<Store> storeOptional = storeService.findStore(storeId);

        // 가게 검증
        if (storeOptional.isEmpty() || !storeOptional.get().getUserId().equals(userId))
            return ResponseEntity.badRequest().build();

        // 상품 업데이트
        Optional<ProductDetails> productDetailsOptional =
                productService.updateProduct(productId, productForm.getName(), productForm.getPrice());

        return productDetailsOptional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
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
