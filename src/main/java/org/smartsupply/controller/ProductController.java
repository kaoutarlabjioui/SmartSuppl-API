package org.smartsupply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartsupply.dto.request.ProductRequestDto;
import org.smartsupply.dto.request.ProductUpdateDto;
import org.smartsupply.dto.response.ProductResponseDto;
import org.smartsupply.model.enums.Role;
import org.smartsupply.service.ProductService;
import org.smartsupply.service.implementation.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ProductController {
    private final S3Service s3Service;

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @ModelAttribute ProductRequestDto productRequestDto) {
        ProductResponseDto response = productService.createProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping

    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")

    public ResponseEntity<List<ProductResponseDto>> getActiveProducts() {
        List<ProductResponseDto> products = productService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<List<ProductResponseDto>> getInactiveProducts() {
        List<ProductResponseDto> products = productService.getInactiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        ProductResponseDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDto> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @PutMapping(path="/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductUpdateDto productUpdateDto) {
        ProductResponseDto response = productService.updateProduct(id, productUpdateDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ProductResponseDto> toggleProductStatus(@PathVariable Long id) {
        ProductResponseDto response = productService.toggleProductStatus(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean hard) {
        productService.deleteProduct(id,hard);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/name")
    //@RequireAuth
    public ResponseEntity<List<ProductResponseDto>> searchByName(@RequestParam String name) {
        List<ProductResponseDto> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/sku")
    //@RequireAuth
    public ResponseEntity<List<ProductResponseDto>> searchBySku(@RequestParam String sku) {
        List<ProductResponseDto> products = productService.searchBySku(sku);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    //@RequireAuth
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@RequestParam String keyword) {
        List<ProductResponseDto> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }



    @PatchMapping("/{sku}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@Valid @PathVariable String sku) {
                    productService.deactivateProduct(sku);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/S3")
    public ResponseEntity<HashMap<String , Object>> uploadS3(@RequestParam("file") MultipartFile file){
        String url = s3Service.uploadFile(file);
        HashMap<String , Object> response = new HashMap<>();
        response.put("File Url",url);
        return ResponseEntity.ok(response);
    }


}