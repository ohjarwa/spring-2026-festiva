package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.service.CreationRecordService;
import org.example.newyear.service.VideoService;
import org.example.newyear.service.oss.OssService;
import org.example.newyear.vo.VideoCreateVO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

/**
 * 视频控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final CreationRecordService creationRecordService;
    private final OssService ossService;

    /**
     * 创建视频任务
     */
    @PostMapping("/create")
    public Result<VideoCreateVO> createVideo(
            @RequestParam("userId") String userId,
            @Validated @RequestBody VideoCreateDTO dto) {
        log.info("创建视频任务: userId={}, templateId={}", userId, dto.getTemplateId());
        VideoCreateVO result = videoService.createVideo(userId, dto);
        return Result.success(result);
    }

    /**
     * 获取作品详情
     */
    @GetMapping("/detail")
    public Result<Spring2026CreationRecord> getDetail(
            @RequestParam("userId") String userId,
            @RequestParam("recordId") String recordId) {
        log.info("获取作品详情: userId={}, recordId={}", userId, recordId);
        Spring2026CreationRecord record = creationRecordService.getRecordDetail(recordId, userId);
        return Result.success(record);
    }

    /**
     * 下载视频
     *
     * @param recordId 视频记录ID
     * @param userId   用户ID（用于权限验证）
     * @return 视频文件流
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadVideo(
            @RequestParam("recordId") String recordId,
            @RequestParam("userId") String userId) {

        try {
            log.info("下载视频: userId={}, recordId={}", userId, recordId);

            // 1. 获取视频记录
            Spring2026CreationRecord record = creationRecordService.getRecordDetail(recordId, userId);

            if (record == null) {
                log.warn("视频记录不存在: recordId={}", recordId);
                return ResponseEntity.notFound().build();
            }

            // 2. 检查视频是否生成完成
            if (record.getStatus() != 2) {
                log.warn("视频未生成完成: recordId={}, status={}", recordId, record.getStatus());
                return ResponseEntity.badRequest().build();
            }

            // 3. 获取视频URL
            String resultUrl = record.getResultUrl();
            if (resultUrl == null || resultUrl.isEmpty()) {
                log.warn("视频URL为空: recordId={}", recordId);
                return ResponseEntity.notFound().build();
            }

            // 4. 从URL中提取fileKey
            String fileKey = extractFileKeyFromUrl(resultUrl);
            if (fileKey == null || fileKey.isEmpty()) {
                log.warn("无法从URL提取fileKey: resultUrl={}", resultUrl);
                return ResponseEntity.badRequest().build();
            }

            // 5. 判断使用哪个OSS账户（default或cv）
            String accountType = determineAccountType(resultUrl);

            // 6. 检查文件是否存在
            if (!ossService.exists(fileKey, accountType)) {
                log.warn("视频文件不存在: fileKey={}, accountType={}", fileKey, accountType);
                return ResponseEntity.notFound().build();
            }

            // 7. 获取文件流
            InputStream inputStream = ossService.getFileStream(fileKey, accountType);

            // 8. 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // 生成下载文件名：recordId_final.mp4
            String filename = recordId + "_final.mp4";
            headers.setContentDispositionFormData("attachment", filename);

            log.info("开始下载视频: recordId={}, fileKey={}, filename={}", recordId, fileKey, filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("下载视频失败: recordId={}, userId={}", recordId, userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 从OSS URL中提取fileKey
     *
     * @param url OSS URL
     * @return fileKey
     */
    private String extractFileKeyFromUrl(String url) {
        try {
            // 去掉协议前缀
            String cleanUrl = url.replaceFirst("^https?://", "");

            // 去掉域名部分
            // 格式：bucket.endpoint/filekey?params
            int firstSlashIndex = cleanUrl.indexOf('/');
            if (firstSlashIndex > 0) {
                String pathPart = cleanUrl.substring(firstSlashIndex + 1);

                // 去掉查询参数
                int queryIndex = pathPart.indexOf('?');
                if (queryIndex > 0) {
                    pathPart = pathPart.substring(0, queryIndex);
                }

                return pathPart;
            }
        } catch (Exception e) {
            log.error("解析URL失败: url={}", url, e);
        }
        return null;
    }

    /**
     * 判断使用哪个OSS账户
     *
     * @param url OSS URL
     * @return accountType (default 或 cv)
     */
    private String determineAccountType(String url) {
        if (url.contains("cv-springfestval-2026")) {
            return "cv";
        } else if (url.contains("ths-newyear-2026")) {
            return "default";
        }
        // 默认返回default
        return "default";
    }
}
