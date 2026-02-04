package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.common.Constants;
import org.example.newyear.entity.Spring2026User;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.util.OssUtil;
import org.example.newyear.vo.UploadResultVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 上传服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final UserService userService;
    private final OssUtil ossUtil;

    /**
     * 上传图片
     */
    public UploadResultVO uploadImage(String userId, MultipartFile file) {
        // 1. 检查用户状态
        Spring2026User user = userService.getOrCreateUser(userId);
        if (user.getCanUpload() == 0) {
            throw new BusinessException(BusinessCode.ERROR_USER_RESTRICTED, "账号已被限制，无法上传文件");
        }

        // 2. 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(BusinessCode.ERROR_FILE_TYPE_INVALID, "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        List<String> imageExtensions = Arrays.asList(Constants.IMAGE_EXTENSIONS);
        if (!imageExtensions.contains(extension)) {
            throw new BusinessException(BusinessCode.ERROR_FILE_TYPE_INVALID,
                    "仅支持图片格式: " + String.join(", ", imageExtensions));
        }

        // 3. 检查文件大小
        if (file.getSize() > Constants.MAX_IMAGE_SIZE) {
            throw new BusinessException(BusinessCode.ERROR_FILE_SIZE_EXCEEDED,
                    "图片大小不能超过" + (Constants.MAX_IMAGE_SIZE / 1024 / 1024) + "MB");
        }

        // 4. 上传到OSS
        try {
            String objectKey = generateObjectKey(userId, "image", extension);
            String url = ossUtil.uploadFile(file, objectKey);

            UploadResultVO vo = new UploadResultVO();
            vo.setUrl(url);
            vo.setName(originalFilename);
            vo.setSize(file.getSize());

            log.info("上传图片成功: userId={}, url={}", userId, url);
            return vo;

        } catch (Exception e) {
            log.error("上传图片失败: userId={}", userId, e);
            throw new BusinessException(BusinessCode.ERROR_FILE_UPLOAD_FAILED, "上传图片失败");
        }
    }

    /**
     * 上传音频
     */
    public UploadResultVO uploadAudio(String userId, MultipartFile file) {
        // 1. 检查用户状态
        Spring2026User user = userService.getOrCreateUser(userId);
        if (user.getCanUpload() == 0) {
            throw new BusinessException(BusinessCode.ERROR_USER_RESTRICTED, "账号已被限制，无法上传文件");
        }

        // 2. 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(BusinessCode.ERROR_FILE_TYPE_INVALID, "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        List<String> audioExtensions = Arrays.asList(Constants.AUDIO_EXTENSIONS);
        if (!audioExtensions.contains(extension)) {
            throw new BusinessException(BusinessCode.ERROR_FILE_TYPE_INVALID,
                    "仅支持音频格式: " + String.join(", ", audioExtensions));
        }

        // 3. 检查文件大小
        if (file.getSize() > Constants.MAX_AUDIO_SIZE) {
            throw new BusinessException(BusinessCode.ERROR_FILE_SIZE_EXCEEDED,
                    "音频大小不能超过" + (Constants.MAX_AUDIO_SIZE / 1024 / 1024) + "MB");
        }

        // 4. 上传到OSS
        try {
            String objectKey = generateObjectKey(userId, "audio", extension);
            String url = ossUtil.uploadFile(file, objectKey);

            UploadResultVO vo = new UploadResultVO();
            vo.setUrl(url);
            vo.setName(originalFilename);
            vo.setSize(file.getSize());

            log.info("上传音频成功: userId={}, url={}", userId, url);
            return vo;

        } catch (Exception e) {
            log.error("上传音频失败: userId={}", userId, e);
            throw new BusinessException(BusinessCode.ERROR_FILE_UPLOAD_FAILED, "上传音频失败");
        }
    }

    /**
     * 生成OSS对象Key
     */
    private String generateObjectKey(String userId, String type, String extension) {
        return String.format("spring2026/%s/%s/%s%s",
                userId,
                type,
                UUID.randomUUID().toString().replace("-", ""),
                extension);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex).toLowerCase();
        }
        return "";
    }
}