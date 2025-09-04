package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.DoodleDto;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Doodle;
import com.example.wayfinderai.service.DoodleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoodleController {
    private final DoodleService doodleService;

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/doodles")
    public Map<String, Object> uploadDoodle(
            @RequestParam("file") MultipartFile file,
            @RequestParam("diaryId") Long diaryId) throws IOException {

        // 1️⃣ 파일 서버에 저장
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadPath + filename);
        Files.createDirectories(path.getParent());
        file.transferTo(path.toFile());

        String imageUrl = "/api/img/download?filename=" + filename;

        // 2️⃣ DB 저장
        Doodle saved = doodleService.saveDoodle(diaryId, imageUrl, null);

        // 3️⃣ 응답
        return Map.of("doodleId", saved.getDoodleId(), "imageUrl", saved.getImageUrl());
    }

//    @GetMapping
//    public DoodleDto getDoodle(Integer doodleId) {
//        return doodleService.getDoodle(doodleId);
//    }

    @GetMapping("/img/download")
    public FileSystemResource fileDownload(@RequestParam("filename") String fileName, HttpServletResponse response) {
//        System.out.println("arrive?");
        File file = new File(uploadPath + "/" + fileName);
//        System.out.println(file.getName());
//        System.out.println(file.getAbsolutePath());
        response.setContentType("application/download; utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        return new FileSystemResource(file);
    }
}
