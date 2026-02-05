package org.example.newyear.service.oss;

import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * OSS 对象存储服务
 *
 * 提供文件上传功能，隐藏底层存储细节
 * 支持多 OSS 账号切换
 *
 * @author Claude
 * @since 2026-02-05
 */
public interface OssService {

    /**
     * 上传文件（使用默认账号）
     *
     * @param file 文件
     * @param path
     * @return 上传结果
     */
    OssUploadResult upload(MultipartFile file, String path);

    /**
     * 上传文件（指定 OSS 账号）
     *
     * @param file 文件
     * @param path 存储路径
     * @param accountType OSS 账号类型（如：default、secondary）
     * @return 上传结果
     */
    OssUploadResult upload(MultipartFile file, String path, String accountType);

    /**
     * 获取文件访问URL（使用默认账号）
     *
     * @param fileKey 文件标识
     * @return 访问URL
     */
    String getAccessUrl(String fileKey);

    /**
     * 获取文件访问URL（指定 OSS 账号）
     *
     * @param fileKey 文件标识
     * @param accountType OSS 账号类型
     * @return 访问URL
     */
    String getAccessUrl(String fileKey, String accountType);

    /**
     * 检查文件是否存在（使用默认账号）
     *
     * @param fileKey 文件标识
     * @return 是否存在
     */
    boolean exists(String fileKey);

    /**
     * 检查文件是否存在（指定 OSS 账号）
     *
     * @param fileKey 文件标识
     * @param accountType OSS 账号类型
     * @return 是否存在
     */
    boolean exists(String fileKey, String accountType);

    /**
     * 删除文件（使用默认账号）
     *
     * @param fileKey 文件标识
     * @return 是否删除成功
     */
    boolean deleteFile(String fileKey);

    /**
     * 删除文件（指定 OSS 账号）
     *
     * @param fileKey 文件标识
     * @param accountType OSS 账号类型
     * @return 是否删除成功
     */
    boolean deleteFile(String fileKey, String accountType);

    /**
     * 获取文件流（使用默认账号）
     *
     * @param fileKey 文件标识
     * @return 文件输入流
     */
    InputStream getFileStream(String fileKey);

    /**
     * 获取文件流（指定 OSS 账号）
     *
     * @param fileKey 文件标识
     * @param accountType OSS 账号类型
     * @return 文件输入流
     */
    InputStream getFileStream(String fileKey, String accountType);

    /**
     * 获取文件元信息（使用默认账号）
     *
     * @param fileKey 文件标识
     * @return 文件元信息
     */
    ObjectMetadata getObjectMetadata(String fileKey);

    /**
     * 获取文件元信息（指定 OSS 账号）
     *
     * @param fileKey 文件标识
     * @param accountType OSS 账号类型
     * @return 文件元信息
     */
    ObjectMetadata getObjectMetadata(String fileKey, String accountType);

    /**
     * 列举文件（使用默认账号）
     *
     * @param prefix 前缀
     * @param maxKeys 最大数量
     * @param marker 标记
     * @return 文件列表
     */
    ObjectListing listObjects(String prefix, int maxKeys, String marker);

    /**
     * 列举文件（指定 OSS 账号）
     *
     * @param prefix 前缀
     * @param maxKeys 最大数量
     * @param marker 标记
     * @param accountType OSS 账号类型
     * @return 文件列表
     */
    ObjectListing listObjects(String prefix, int maxKeys, String marker, String accountType);

    /**
     * 复制文件（使用默认账号）
     *
     * @param sourceKey 源文件标识
     * @param destinationKey 目标文件标识
     * @return 是否复制成功
     */
    boolean copyFile(String sourceKey, String destinationKey);

    /**
     * 复制文件（指定 OSS 账号）
     *
     * @param sourceKey 源文件标识
     * @param destinationKey 目标文件标识
     * @param accountType OSS 账号类型
     * @return 是否复制成功
     */
    boolean copyFile(String sourceKey, String destinationKey, String accountType);
}
