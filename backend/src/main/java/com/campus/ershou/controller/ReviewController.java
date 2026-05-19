package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.common.UserContext;
import com.campus.ershou.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired private ReviewService reviewService;

    @PostMapping("/product")
    public Result<?> product(@RequestParam Long orderId, @RequestParam Long productId,
                             @RequestParam Integer rating, @RequestParam(required = false) String content) {
        reviewService.reviewProduct(orderId, productId, rating, content, UserContext.getUserId());
        return Result.ok();
    }

    @PostMapping("/merchant")
    public Result<?> merchant(@RequestParam Long orderId, @RequestParam Long merchantId,
                              @RequestParam Integer rating, @RequestParam(required = false) String content) {
        reviewService.reviewMerchant(orderId, merchantId, rating, content, UserContext.getUserId());
        return Result.ok();
    }

    @PostMapping("/buyer")
    public Result<?> buyer(@RequestParam Long orderId, @RequestParam Long buyerId,
                           @RequestParam Integer rating, @RequestParam(required = false) String content) {
        reviewService.reviewBuyer(orderId, buyerId, rating, content, UserContext.getUserId());
        return Result.ok();
    }
}
