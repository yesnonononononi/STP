package com.summit.stp.shared.util;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.shared.exception.FileDeleteException;
import com.summit.stp.shared.exception.FileUploadException;
import io.minio.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadUtil {
    @Value("${minio.default_bucket}")
    private String defaultBucket;
    private final MinioClient minioClient;
    @Value("${minio.default_bucket}")
    private String bucketName;

    /**
     * 上传文件
     * @param multipartFile 文件
     * @return 结果
     */
    public ObjectWriteResponse upload( @Nullable String folder, MultipartFile multipartFile){
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
     * 删除文件
     * @param fileName 文件名
     * @param bucketName 存储桶
     */
    public void deleteFile(String fileName, String bucketName){
        try {
            if (StrUtil.isBlank(bucketName)) {
                bucketName = defaultBucket;
            }
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        }catch (Exception e){
            log.error("【删除文件】异常: ", e);
            throw new FileDeleteException("删除文件失败");
        }
    }


}
