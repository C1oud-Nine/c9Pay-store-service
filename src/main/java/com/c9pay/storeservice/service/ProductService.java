package com.c9pay.storeservice.service;

import com.c9pay.storeservice.dto.product.ProductDetails;
import com.c9pay.storeservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductDetails> getProductDetailsByStoreId(Long storeId) {
        return productRepository.findAllByStoreId(storeId).stream()
                .map((p)->new ProductDetails(p.getId(), p.getName(), p.getPrice()))
                .toList();
    }
}
