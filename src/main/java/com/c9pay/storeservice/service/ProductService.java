package com.c9pay.storeservice.service;

import com.c9pay.storeservice.dto.product.ProductDetails;
import com.c9pay.storeservice.entity.Product;
import com.c9pay.storeservice.entity.Store;
import com.c9pay.storeservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Product saveProduct(String name, int price, Store store) {
        return productRepository.save(new Product(name, price, store));
    }

    public Optional<ProductDetails> updateProduct(Long productId, String name, int price) {
        Optional<Product> productOptional = productRepository.findById(productId);

        return productOptional.map((p)->{
            p.updateProduct(name, price);
            return new ProductDetails(p.getId(), p.getName(), p.getPrice());
        });
    }

    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}
