package org.example.newyear.controller;

import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.example.newyear.common.Result;
import org.example.newyear.service.oss.OssException;
import org.example.newyear.service.oss.OssService;
import org.example.newyear.service.oss.OssUploadResult;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OSS测试控制器 - 用于测试OSS各项功能
 * 所有方法都使用 OssService 统一调用
 *
 * @author Claude
 * @since 2026-02-05
 */
@RestController
@RequestMapping("/oss/test")
@RequiredArgsConstructor
public class OssTestController {

    private final OssService ossService;

    /**
     * 测试页面
     */
    @GetMapping("/")
    public String index() {
        return "OSS测试接口可用！请使用 /oss/test/help 查看接口列表";
    }

    /**
     * 帮助信息
     */
    @GetMapping("/help")
    public Result<Map<String, String>> help() {
        Map<String, String> apis = new LinkedHashMap<>();
        apis.put("POST /oss/test/upload", "上传文件");
        apis.put("GET /oss/test/download?key={objectKey}", "下载文件");
        apis.put("GET /oss/test/url?key={objectKey}&expire={seconds}", "获取签名URL");
        apis.put("GET /oss/test/exists?key={objectKey}", "检查文件是否存在");
        apis.put("DELETE /oss/test/delete?key={objectKey}", "删除文件");
        apis.put("GET /oss/test/list?prefix={prefix}&maxKeys={num}", "列举文件");
        apis.put("GET /oss/test/info?key={objectKey}", "获取文件信息");
        apis.put("POST /oss/test/copy?sourceKey={source}&destinationKey={dest}", "复制文件");
        return Result.success(apis);
    }

    /**
     * 通用上传接口
     */
    @PostMapping("/upload")
    public Result<OssUploadResult> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "test") String folder) {

        try {
            // 直接调用 Service，所有细节都被封装了
            OssUploadResult result = ossService.upload(file, folder);
            return Result.success(result);

        } catch (OssException e) {
            // 业务异常，返回友好提示
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下载文件（流式传输）
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam("key") String objectKey) {
        try {
            System.out.println("下载文件: " + objectKey);

            // 使用 ossService 检查文件是否存在
            if (!ossService.exists(objectKey)) {
                return ResponseEntity.notFound().build();
            }

            // 使用 ossService 获取文件流
            InputStream inputStream = ossService.getFileStream(objectKey);

            // 使用 ossService 获取文件元信息
            ObjectMetadata metadata = ossService.getObjectMetadata(objectKey);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    metadata.getContentType() != null ? metadata.getContentType() : "application/octet-stream"));
            headers.setContentLength(metadata.getContentLength());

            // 从objectKey提取文件名
            String filename = objectKey.substring(objectKey.lastIndexOf('/') + 1);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            System.err.println("文件下载失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取签名URL
     */
    @GetMapping("/url")
    public Result<String> getSignedUrl(
            @RequestParam("key") String objectKey) {

        try {
            String url = ossService.getAccessUrl(objectKey);
            return Result.success(url);

        } catch (OssException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists")
    public Result<Map<String, Object>> checkExists(@RequestParam("key") String objectKey) {
        try {
            System.out.println("检查文件: " + objectKey);

            // 使用 ossService
            boolean exists = ossService.exists(objectKey);

            Map<String, Object> result = new HashMap<>();
            result.put("objectKey", objectKey);
            result.put("exists", exists);

            return Result.success(result);

        } catch (Exception e) {
            System.err.println("检查文件存在性失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestParam("key") String objectKey) {
        try {
            System.out.println("删除文件: " + objectKey);

            // 使用 ossService 检查文件是否存在
            if (!ossService.exists(objectKey)) {
                return Result.error("文件不存在");
            }

            // 使用 ossService 删除文件
            boolean deleted = ossService.deleteFile(objectKey);

            if (deleted) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }

        } catch (Exception e) {
            System.err.println("删除文件失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 列举文件
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listFiles(
            @RequestParam(value = "prefix", defaultValue = "") String prefix,
            @RequestParam(value = "maxKeys", defaultValue = "100") int maxKeys) {

        try {
            // 使用 ossService
            ObjectListing listing = ossService.listObjects(prefix, maxKeys, null);

            List<Map<String, Object>> files = listing.getObjectSummaries().stream()
                    .map(summary -> {
                        Map<String, Object> file = new HashMap<>();
                        file.put("key", summary.getKey());
                        file.put("size", summary.getSize());
                        file.put("lastModified", summary.getLastModified());
                        file.put("storageClass", summary.getStorageClass());
                        return file;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("files", files);
            result.put("count", files.size());
            result.put("isTruncated", listing.isTruncated());
            result.put("nextMarker", listing.getNextMarker());

            return Result.success(result);

        } catch (Exception e) {
            System.err.println("列举文件失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getFileInfo(@RequestParam("key") String objectKey) {
        try {
            System.out.println("获取文件信息: " + objectKey);

            // 使用 ossService 检查文件是否存在
            if (!ossService.exists(objectKey)) {
                return Result.error("文件不存在");
            }

            // 使用 ossService 获取文件元信息
            ObjectMetadata metadata = ossService.getObjectMetadata(objectKey);

            Map<String, Object> info = new HashMap<>();
            info.put("objectKey", objectKey);
            info.put("size", metadata.getContentLength());
            info.put("contentType", metadata.getContentType());
            info.put("lastModified", metadata.getLastModified());
            info.put("etag", metadata.getETag());

            return Result.success(info);

        } catch (Exception e) {
            System.err.println("获取文件信息失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 复制文件
     */
    @PostMapping("/copy")
    public Result<String> copyFile(
            @RequestParam("sourceKey") String sourceKey,
            @RequestParam("destinationKey") String destinationKey) {

        try {
            // 使用 ossService 检查源文件是否存在
            if (!ossService.exists(sourceKey)) {
                return Result.error("源文件不存在");
            }

            // 使用 ossService 复制文件
            boolean copied = ossService.copyFile(sourceKey, destinationKey);

            if (copied) {
                return Result.success("复制成功");
            } else {
                return Result.error("复制失败");
            }

        } catch (Exception e) {
            System.err.println("复制文件失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}
