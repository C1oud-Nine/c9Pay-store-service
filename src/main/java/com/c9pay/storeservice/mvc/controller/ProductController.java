package com.c9pay.storeservice.mvc.controller;

import com.c9pay.storeservice.config.Resilience4JConfig;
import com.c9pay.storeservice.data.dto.charge.ChargeAmount;
import com.c9pay.storeservice.data.dto.product.ProductDetailList;
import com.c9pay.storeservice.data.dto.product.ProductDetails;
import com.c9pay.storeservice.data.dto.product.ProductForm;
import com.c9pay.storeservice.data.dto.qr.ExchangeToken;
import com.c9pay.storeservice.data.dto.sale.PaymentInfo;
import com.c9pay.storeservice.data.dto.sale.ProductSaleInfo;
import com.c9pay.storeservice.data.dto.sale.PurchaseInfo;
import com.c9pay.storeservice.data.entity.Product;
import com.c9pay.storeservice.data.entity.Store;
import com.c9pay.storeservice.exception.NotExistException;
import com.c9pay.storeservice.interceptor.GatewayValidation;
import com.c9pay.storeservice.mvc.repository.ProductRepository;
import com.c9pay.storeservice.mvc.service.ProductService;
import com.c9pay.storeservice.mvc.service.StoreService;
import com.c9pay.storeservice.proxy.CreditServiceProxy;
import com.c9pay.storeservice.qr.QrDecoder;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.c9pay.storeservice.config.Resilience4JConfig.circuitBreakerThrowable;
import static com.c9pay.storeservice.constant.GatewayConstant.API;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/{store-id}/product")
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final StoreService storeService;
    private final CreditServiceProxy creditServiceProxy;
    private final QrDecoder qrDecoder;

    private final CircuitBreakerFactory circuitBreakerFactory;

    @GetMapping
    @RateLimiter(name = "Rate_limiter")
    @GatewayValidation(API)
    public ResponseEntity<ProductDetailList> getProducts(
            @PathVariable("store-id") Long storeId
    ) {
        List<ProductDetails> productDetailsList =
                productService.getProductDetailsByStoreId(storeId);

        return ResponseEntity.ok(new ProductDetailList(productDetailsList));
    }

    @PostMapping
    @RateLimiter(name = "Rate_limiter")
    @GatewayValidation(API)
    public ResponseEntity<ProductDetailList> addProduct(
            Principal principal,
            @PathVariable("store-id") Long storeId,
            @RequestBody ProductForm productForm
    ) {
        UUID userId = UUID.fromString(principal.getName());
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
    @RateLimiter(name = "Rate_limiter")
    @GatewayValidation(API)
    public ResponseEntity<ProductDetailList> updateProduct(
            Principal principal,
            @PathVariable("store-id") Long storeId,
            @PathVariable("product-id") Long productId,
            @RequestBody ProductForm productForm
    ) {
        UUID userId = UUID.fromString(principal.getName());
        Optional<Store> storeOptional = storeService.findStore(storeId);

        // 가게 검증
        if (storeOptional.isEmpty() || !storeOptional.get().getUserId().equals(userId))
            return ResponseEntity.badRequest().build();

        // 상품 업데이트
        Optional<ProductDetails> productDetailsOptional =
                productService.updateProduct(productId, productForm.getName(), productForm.getPrice());

        List<ProductDetails> productDetailsList = productService.getProductDetailsByStoreId(storeId);

        return ResponseEntity.ok(new ProductDetailList(productDetailsList));
    }

    @DeleteMapping("/{product-id}")
    @RateLimiter(name = "Rate_limiter")
    @GatewayValidation(API)
    public ResponseEntity<ProductDetailList> deleteProduct(
            Principal principal,
            @PathVariable("store-id") Long storeId,
            @PathVariable("product-id") Long productId
    ) {
        UUID userId = UUID.fromString(principal.getName());
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
    @RateLimiter(name = "Rate_limiter")
    @GatewayValidation(API)
    public ResponseEntity<PaymentInfo> sellProducts(
            @PathVariable("store-id") long storeId,
            @RequestBody PurchaseInfo purchaseInfo
    ) {
        ExchangeToken exchangeToken = purchaseInfo.getExchangeToken();
        Optional<UUID> userIdOptional = qrDecoder.getSerialNumber(exchangeToken);
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        try {
            // 구매정보를 바탕으로 결제정보 생성
            List<ProductSaleInfo> productSaleInfoList = purchaseInfo.getPurchaseList().stream()
                    .map((p) -> {
                        Product product = productRepository.findById(p.getProductId())
                                .orElseThrow(()->new NotExistException("상품을 찾을 수 없습니다."));
                        return new ProductSaleInfo(product.getId(), product.getName(), product.getPrice(), p.getAmount());
                    })
                    .toList();
            // 총 결제 금액 계산
            int totalAmount = productSaleInfoList.stream().mapToInt((p) -> p.getAmount() * p.getPrice()).sum();

            // 구매자와 가게 주인의 식별번호를 획득
            UUID userId = userIdOptional.orElseThrow(()->new NotExistException("사용자 ID를 복호화할 수 없습니다."));
            UUID ownerId = storeService.getOwnerId(storeId)
                    .orElseThrow(()->new NotExistException("저장된 가게 주인 정보가 없습니다."));

            // 크레딧 서비스에 송금 요청
            // todo 실패 시의 로직 필요

            ResponseEntity exchangeResponse = circuitbreaker.run(() -> creditServiceProxy.transfer(userId.toString(), ownerId.toString(), new ChargeAmount(totalAmount)),
                    throwable -> circuitBreakerThrowable());

            if (exchangeResponse.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok(new PaymentInfo(productSaleInfoList, totalAmount));
            else return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
