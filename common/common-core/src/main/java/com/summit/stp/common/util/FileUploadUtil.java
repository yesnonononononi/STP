package com.summit.stp.common.util;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.application.domain.exception.FileDeleteException;
import com.summit.stp.common.application.domain.exception.FileUploadException;
import io.minio.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@ConditionalOnProperty(name = "stp.minio.endpoint")
@Component
@RequiredArgsConstructor
public class FileUploadUtil {

    /** 单文件大小上限：50MB（与系统视频上限对齐，防滥用） */
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    /** 允许的图片 MIME 类型白名单 */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    /** 允许的视频 MIME 类型白名单 */
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-matroska",
            "video/x-flv", "video/webm", "video/ogg");

    /** 允许的图片扩展名白名单 */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp");

    /** 允许的视频扩展名白名单 */
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "mkv", "flv", "webm", "ogg");

    /** 显式拒绝的可执行/脚本型扩展名（存储型 XSS 防护，#22） */
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "svg", "html", "htm", "xhtml", "xml", "js", "mjs", "mhtml", "shtml");

    @Value("${stp.minio.default_bucket}")
    private String defaultBucket;
    private final MinioClient minioClient;
    @Value("${stp.minio.default_bucket}")
    private String bucketName;

    /**
     * 上传文件（含媒体类型白名单 + 大小校验，显式拒绝 SVG/HTML/XML/JS，#22）
     * @param multipartFile 文件
     * @return 结果
     */
    public ObjectWriteResponse upload( @Nullable String folder, MultipartFile multipartFile){
        validateUpload(multipartFile);

        if (StrUtil.isBlank(bucketName)) {
            bucketName = defaultBucket;
        }

        try{
            String fileName = UUID.randomUUID().toString();
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object((folder == null ? "" : (folder +'/'))+ fileName)
                    .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                    .contentType(multipartFile.getContentType())
                    .build());
        } catch (Exception e){
            log.error("【文件上传】上传文件异常: ", e);
            throw new FileUploadException("上传文件失败");
        }

    }

    /**
     * 上传白名单校验：大小 + 媒体类型（图片/视频）+ 扩展名，显式拒绝 SVG/HTML/XML/JS 等可执行内容。
     * <p>说明：本工具为全站共享上传入口，C 端同时支持图片与视频上传，
     * 故白名单保留图片与视频两类（与 {@code CommonAppServiceImpl#check} 对齐），核心安全目标是拦截
     * SVG/HTML 等可脚本化内容造成的存储型 XSS。</p>
     */
    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("文件大小不能超过 50MB");
        }

        String ext = extractExtension(file.getOriginalFilename());
        // 显式拒绝可脚本化/可执行扩展名
        if (ext != null && BLOCKED_EXTENSIONS.contains(ext)) {
            throw new FileUploadException("禁止上传 SVG/HTML/XML/JS 等可执行内容");
        }

        String contentType = file.getContentType();
        String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        // 显式拒绝可脚本化内容类型
        if (normalizedType.contains("svg") || normalizedType.contains("html") || normalizedType.contains("xml")) {
            throw new FileUploadException("禁止上传 SVG/HTML/XML 等可执行内容");
        }

        boolean allowed = ALLOWED_IMAGE_TYPES.contains(normalizedType)
                || ALLOWED_VIDEO_TYPES.contains(normalizedType)
                || (ext != null && ALLOWED_IMAGE_EXTENSIONS.contains(ext))
                || (ext != null && ALLOWED_VIDEO_EXTENSIONS.contains(ext));
        if (!allowed) {
            throw new FileUploadException("仅支持图片(jpg/jpeg/png/gif/webp/bmp)或视频(mp4/mov/avi/mkv/flv/webm/ogg)上传");
        }
    }

    private static String extractExtension(String originalName) {
        if (originalName == null) return null;
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot >= originalName.length() - 1) return null;
        return originalName.substring(dot + 1).trim().toLowerCase(Locale.ROOT);
    }


    /**
     * 删除文件
     * @param fileName 文件名
     * @param bucketName 存储桶
     */
    public void deleteFile(String fileName, String bucketName){
        try {
            if (StrUtil.isBlank(bucketName)) {
                bucketName = defaultBucket;
            } else if (!bucketName.equals(defaultBucket)) {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!exists) {
                    fileName = bucketName + "/" + fileName;
                    bucketName = defaultBucket;
                }
            }
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        }catch (Exception e){
            log.error("【删除文件】异常: ", e);
            throw new FileDeleteException("删除文件失败");
        }
    }


}
