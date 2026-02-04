package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.service.UploadService;
import org.example.newyear.vo.UploadResultVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<UploadResultVO> uploadImage(
            @RequestParam("userId") String userId,
            @RequestParam("file") MultipartFile file) {
        log.info("上传图片: userId={}, filename={}, size={}", userId, file.getOriginalFilename(), file.getSize());
        UploadResultVO result = uploadService.uploadImage(userId, file);
        return Result.success(result);
    }

    /**
     * 上传音频
     */
    @PostMapping("/audio")
    public Result<UploadResultVO> uploadAudio(
            @RequestParam("userId") String userId,
            @RequestParam("file") MultipartFile file) {
        log.info("上传音频: userId={}, filename={}, size={}", userId, file.getOriginalFilename(), file.getSize());
        UploadResultVO result = uploadService.uploadAudio(userId, file);
        return Result.success(result);
    }
}