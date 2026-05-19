package com.campus.ershou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ershou.common.Result;
import com.campus.ershou.dto.ProductDTO;
import com.campus.ershou.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired private ProductService productService;

    @GetMapping("/search")
    public Result<Page<Map<String, Object>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "default") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.ok(productService.search(keyword, categoryId, sortBy, page, size));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @PostMapping
    public Result<?> publish(@Valid @RequestBody ProductDTO dto) {
        return Result.ok(productService.publish(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto.setId(id);
        productService.update(dto);
        return Result.ok();
    }

    @PutMapping("/{id}/off-shelf")
    public Result<?> offShelf(@PathVariable Long id) {
        productService.offShelf(id);
        return Result.ok();
    }

    @GetMapping("/merchant/my")
    public Result<List<Map<String, Object>>> myProducts(@RequestParam(required = false) String status) {
        return Result.ok(productService.merchantProducts(status));
    }
}
