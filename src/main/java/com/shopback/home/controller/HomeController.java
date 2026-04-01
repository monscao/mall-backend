package com.shopback.home.controller;

import com.shopback.home.dto.HomeResponse;
import com.shopback.home.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public HomeResponse getHome() {
        return homeService.getHome();
    }
}
