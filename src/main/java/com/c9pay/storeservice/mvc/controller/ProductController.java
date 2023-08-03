package com.c9pay.storeservice.mvc.controller;

import com.c9pay.storeservice.certificate.Decoder;
import com.c9pay.storeservice.certificate.PublicKeyProvider;
import com.c9pay.storeservice.certificate.PublicKeyProviderFactory;
import com.c9pay.storeservice.data.dto.charge.ChargeAmount;
import com.c9pay.storeservice.data.dto.qr.QRContent;
import com.c9pay.storeservice.data.dto.qr.QRInfo;
import com.c9pay.storeservice.data.dto.certificate.ServiceDetails;
import com.c9pay.storeservice.data.dto.product.ProductDetailList;
import com.c9pay.storeservice.data.dto.product.ProductDetails;
import com.c9pay.storeservice.data.dto.product.ProductForm;
import com.c9pay.storeservice.data.dto.sale.PaymentInfo;
import com.c9pay.storeservice.data.dto.sale.ProductSaleInfo;
import com.c9pay.storeservice.data.dto.sale.PurchaseInfo;
import com.c9pay.storeservice.data.entity.Product;
import com.c9pay.storeservice.data.entity.Store;
import com.c9pay.storeservice.mvc.repository.ProductRepository;
import com.c9pay.storeservice.mvc.service.ProductService;
import com.c9pay.storeservice.mvc.service.StoreService;
import com.c9pay.storeservice.proxy.CreditServiceProxy;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final ProductRepository productRepository;
    private final StoreService storeService;
    private final PublicKeyProviderFactory publicKeyProviderFactory;
    private final Decoder decoder;
    private final CreditServiceProxy creditServiceProxy;

    @GetMapping
    public ResponseEntity<ProductDetailList> getProducts(
            HttpSession session,
            @PathVariable("store-id") Long storeId
    ) {
        UUID userId = (UUID) session.getAttribute("userId");
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
            HttpSession session,
            @PathVariable("store-id") Long storeId,
            @RequestBody ProductForm productForm
    ) {
        UUID userId = (UUID) session.getAttribute("userId");
        Optional<Store> storeOptional = storeService.findStore(storeId);

        // 가게 검증
        if (storeOptional.isEmpty() || !storeOptional.get().getUserId().equals(userId))
            return ResponseEntity.badRequest().build();

        // 상품 생성
        productService.saveProduct(productForm.getName(), productForm.getPrice(), storeOptional.get());

        List<ProductDetails> productDetailsList = productService.getProductDetailsByStoreId(storeId);

        return ResponseEntity.ok(new ProductDetailList(productDetailsList));
    }

    @PostMapping("/{product-id}")
    public ResponseEntity<ProductDetails> updateProduct(
            HttpSession session,
            @PathVariable("store-id") Long storeId,
            @PathVariable("product-id") Long productId,
            @RequestBody ProductForm productForm
    ) {
        UUID userId = (UUID) session.getAttribute("userId");
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

    @DeleteMapping("/{product-id}")
    public ResponseEntity<ProductDetailList> deleteProduct(
            HttpSession session,
            @PathVariable("store-id") Long storeId,
            @PathVariable("product-id") Long productId
    ) {
        UUID userId = (UUID) session.getAttribute("userId");
        Optional<Store> storeOptional = storeService.findStore(storeId);

        // 가게 검증
        if (storeOptional.isEmpty() || !storeOptional.get().getUserId().equals(userId))
            return ResponseEntity.badRequest().build();

        // 상품 삭제
        productService.deleteProduct(productId);

        List<ProductDetails> productDetailsList = productService.getProductDetailsByStoreId(storeId);

        return ResponseEntity.ok(new ProductDetailList(productDetailsList));
    }

    @PostMapping("/sale")
    public ResponseEntity<PaymentInfo> sellProducts(
            @PathVariable("store-id") long storeId,
            @RequestBody PurchaseInfo purchaseInfo
    ) {
        // 인증서 복호화를 통한 공개키 획득
        QRInfo qrInfo = purchaseInfo.getQrInfo();
        Optional<ServiceDetails> serviceDetailsOptional =
                decoder.decrypt(
                        publicKeyProviderFactory.authServicePublicKeyProvider(),
                        qrInfo.getCertificate().getCertificate(),
                        qrInfo.getCertificate().getSign(),
                        ServiceDetails.class);
        log.info("{}", serviceDetailsOptional);

        // Optional<QRContent> qrContent = serviceDetailsOptional.map(ServiceDetails::getPublicKey)
        //        .map(publicKeyProviderFactory::generalPublicKeyProvider)
        //        .flatMap(pp -> decoder.decrypt(pp, qrInfo.getContent(), QRContent.class));

        // todo 구매정보를 바탕으로 결제정보 생성
        List<ProductSaleInfo> productSaleInfoList = purchaseInfo.getPurchaseList().stream()
                .map((p) -> {
                    Product product = productRepository.findById(p.getProductId()).get();
                    return new ProductSaleInfo(product.getId(), product.getName(), product.getPrice(), p.getAmount());
                })
                .toList();
        int totalAmount = productSaleInfoList.stream().mapToInt((p)->p.getAmount() * p.getPrice()).sum();

        // todo 사용자 식별번호와 가게 주인 식별번호를 코인 서비스 송금으로 넘김
        // UUID userId = storeService.findStore(storeId).get().getUserId();
        // creditServiceProxy.transfer(userId.toString(), qrContent.get().getSerialNumber(), new ChargeAmount(totalAmount));

        return ResponseEntity.ok(new PaymentInfo(productSaleInfoList, totalAmount));
    }
}
