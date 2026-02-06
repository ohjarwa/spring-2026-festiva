package org.example.newyear.service.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.example.newyear.util.OssClientFactory;
import org.example.newyear.util.OssClientFactory.OssClientWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

/**
 * OSS 服务实现（支持多账号）
 *
 * @author Claude
 * @since 2026-02-05
 */
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssClientFactory ossClientFactory;

    @Override
    public OssUploadResult upload(MultipartFile file, String path) {
        // 使用默认账号
        return upload(file, path, "default");
    }

    @Override
    public OssUploadResult upload(MultipartFile file, String path, String accountType) {
        System.out.println("开始上传文件: path=" + path + ", accountType=" + accountType +
                ", filename=" + file.getOriginalFilename());

        try {
            // 1. 获取指定账号的客户端
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);

            // 2. 生成文件路径
            String fileKey = generateFileKey(file.getOriginalFilename(), path);

            // 3. 上传到 OSS
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();
            ossClient.putObject(bucket, fileKey, file.getInputStream());

            System.out.println("上传成功: accountType=" + accountType + ", bucket=" + bucket + ", fileKey=" + fileKey);

            // 4. 生成访问 URL
            String accessUrl = generateAccessUrl(fileKey, clientWrapper);

            // 5. 构建返回结果
            return OssUploadResult.builder()
                    .fileKey(fileKey)
                    .accessUrl(accessUrl)
                    .originalFilename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadTime(System.currentTimeMillis())
                    .build();

        } catch (OssException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("上传失败: " + e.getMessage());
            e.printStackTrace();
            throw OssException.uploadFailed(e.getMessage(), e);
        }
    }

    @Override
    public String getAccessUrl(String fileKey) {
        return getAccessUrl(fileKey, "default");
    }

    @Override
    public String getAccessUrl(String fileKey, String accountType) {
        if (fileKey == null || fileKey.isEmpty()) {
            throw new IllegalArgumentException("fileKey 不能为空");
        }

        OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);

        // 检查文件是否存在
        OSS ossClient = clientWrapper.getOssClient();
        String bucket = clientWrapper.getBucket();
        if (!ossClient.doesObjectExist(bucket, fileKey)) {
            throw OssException.fileNotFound(fileKey);
        }

        return generateAccessUrl(fileKey, clientWrapper);
    }

    /**
     * 生成文件路径
     */
    private String generateFileKey(String originalFilename, String path) {

        return String.format("spring2026/%s/%s",
                path, originalFilename);
    }

    /**
     * 生成访问URL
     */
    private String generateAccessUrl(String fileKey, OssClientWrapper clientWrapper) {
        if (clientWrapper.isPrivate()) {
            // 私有Bucket，返回签名URL
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();
            Integer expire = clientWrapper.getSignedUrlExpire();

            Date expiration = Date.from(Instant.now().plusSeconds(expire));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, fileKey);
            request.setExpiration(expiration);
            request.setMethod(com.aliyun.oss.HttpMethod.GET);

            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } else {
            // 公共Bucket，返回永久URL
            return "https://" + clientWrapper.getBucket() + "." +
                    clientWrapper.getEndpoint() + "/" + fileKey;
        }
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

    @Override
    public boolean exists(String fileKey) {
        return exists(fileKey, "default");
    }

    @Override
    public boolean exists(String fileKey, String accountType) {
        try {
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();
            return ossClient.doesObjectExist(bucket, fileKey);
        } catch (Exception e) {
            System.err.println("检查文件存在性失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFile(String fileKey) {
        return deleteFile(fileKey, "default");
    }

    @Override
    public boolean deleteFile(String fileKey, String accountType) {
        try {
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();

            if (!ossClient.doesObjectExist(bucket, fileKey)) {
                System.err.println("文件不存在: " + fileKey);
                return false;
            }

            ossClient.deleteObject(bucket, fileKey);
            System.out.println("文件删除成功: " + fileKey);
            return true;
        } catch (Exception e) {
            System.err.println("删除文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public InputStream getFileStream(String fileKey) {
        return getFileStream(fileKey, "default");
    }

    @Override
    public InputStream getFileStream(String fileKey, String accountType) {
        try {
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();
            return ossClient.getObject(bucket, fileKey).getObjectContent();
        } catch (Exception e) {
            System.err.println("获取文件流失败: " + e.getMessage());
            e.printStackTrace();
            throw new OssException("获取文件流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String fileKey) {
        return getObjectMetadata(fileKey, "default");
    }

    @Override
    public ObjectMetadata getObjectMetadata(String fileKey, String accountType) {
        try {
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();
            return ossClient.getObjectMetadata(bucket, fileKey);
        } catch (Exception e) {
            System.err.println("获取文件元信息失败: " + e.getMessage());
            e.printStackTrace();
            throw new OssException("获取文件元信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ObjectListing listObjects(String prefix, int maxKeys, String marker) {
        return listObjects(prefix, maxKeys, marker, "default");
    }

    @Override
    public ObjectListing listObjects(String prefix, int maxKeys, String marker, String accountType) {
        try {
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();

            ListObjectsRequest request = new ListObjectsRequest(bucket);
            request.setPrefix(prefix);
            request.setMaxKeys(maxKeys);
            if (marker != null && !marker.isEmpty()) {
                request.setMarker(marker);
            }

            return ossClient.listObjects(request);
        } catch (Exception e) {
            System.err.println("列举文件失败: " + e.getMessage());
            e.printStackTrace();
            throw new OssException("列举文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean copyFile(String sourceKey, String destinationKey) {
        return copyFile(sourceKey, destinationKey, "default");
    }

    @Override
    public boolean copyFile(String sourceKey, String destinationKey, String accountType) {
        try {
            OssClientWrapper clientWrapper = ossClientFactory.getClient(accountType);
            OSS ossClient = clientWrapper.getOssClient();
            String bucket = clientWrapper.getBucket();

            if (!ossClient.doesObjectExist(bucket, sourceKey)) {
                System.err.println("源文件不存在: " + sourceKey);
                return false;
            }

            ossClient.copyObject(bucket, sourceKey, bucket, destinationKey);
            System.out.println("文件复制成功: " + sourceKey + " -> " + destinationKey);
            return true;
        } catch (Exception e) {
            System.err.println("复制文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
