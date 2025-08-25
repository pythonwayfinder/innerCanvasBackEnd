package com.example.wayfinderai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TestController.java
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/admin/data")
    public String getAdminData() {
        return "관리자용 데이터입니다.";
    }

    @GetMapping("/member/data")
    public String getUserData() {
        return "일반 멤버용 데이터입니다.";
    }
}
