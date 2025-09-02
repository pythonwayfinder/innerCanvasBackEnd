package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.DoodleDto;
import com.example.wayfinderai.entity.Doodle;
import com.example.wayfinderai.service.DoodleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/doodles")
@RequiredArgsConstructor
public class DoodleController {
    private final DoodleService doodleService;

    @Value("${upload.path}")
    private String uploadPath;


    @PostMapping
    public Map<String, Object> uploadDoodle(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId) throws IOException {

        // 1️⃣ 파일 서버에 저장
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadPath + filename);
        Files.createDirectories(path.getParent());
        file.transferTo(path.toFile());

        String imageUrl = uploadPath + filename;

        // 2️⃣ DB 저장
        Doodle saved = doodleService.saveDoodle(userId, imageUrl, null);

        // 3️⃣ 응답
        return Map.of("doodleId", saved.getDoodleId(), "imageUrl", saved.getImageUrl());
    }

    @GetMapping
    public DoodleDto getDoodle(Integer doodleId) {
        return doodleService.getDoodle(doodleId);
    }
}
