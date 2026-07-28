package com.summit.stp.common.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.api.dto.response.UploadVO;
import com.summit.stp.common.application.service.CommonAppService;
import com.summit.stp.common.application.domain.exception.FileUploadException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.result.Result;
import com.summit.stp.common.util.FileUploadUtil;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.summit.stp.post.infrastructure.constants.PostConstants;

@Service
public class CommonAppServiceImpl implements CommonAppService {
    private final FileUploadUtil fileUploadUtil;
    private final MinioClient minioClient;
    static File bigFileTempFolder;

    static {
        bigFileTempFolder = new File("file");
    }

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.default_bucket}")
    private String bucketName;

    public CommonAppServiceImpl(FileUploadUtil fileUploadUtil, MinioClient minioClient) {
        this.fileUploadUtil = fileUploadUtil;
        this.minioClient = minioClient;
    }

    @Override
    public UploadVO upload(MultipartFile file, @Nullable String folder) {
        if (file == null || file.isEmpty()) {
            throw new ParameterException("文件不能为空");
        }
        check(file);
        ObjectWriteResponse response = fileUploadUtil.upload(folder, file);

        String bucket = response.bucket();
        String object = response.object();
        String fileUrl = endpoint;
        if (!fileUrl.endsWith("/")) {
            fileUrl += "/";
        }
        fileUrl += bucket + "/" + object;

        return new UploadVO(fileUrl, file.getOriginalFilename());
    }

    @Override
    public Result<UploadVO> uploadDealBigFile(MultipartFile file, String folder, Integer curIndex, Integer chunk) {
        if (curIndex < 0) {
            throw new ParameterException("当前索引不能小于0");
        }

        // bigFileTempFolder 目录确保存在
        if (!bigFileTempFolder.exists()) {
            bigFileTempFolder.mkdirs();
        }

        File folderDir = new File(bigFileTempFolder, folder);
        if (!folderDir.exists()) {
            folderDir.mkdirs();
        }

        // 如果是最后一步：合并大文件
        if (curIndex.equals(chunk)) {
            return merge(folderDir, chunk, folder, file);
        }

        // 非合并步骤：校验并写入当前分片
        check(file);
        File partFile = new File(folderDir, String.valueOf(curIndex));
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(partFile)) {
            byte[] buffer = new byte[8192];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, readBytes);
            }
        } catch (Exception e) {
            throw new FileUploadException("保存分片文件失败: " + e.getMessage());
        }

        return Result.success(new UploadVO(String.format("当前分片 %d 上传成功!", curIndex), file.getOriginalFilename()));
    }


    /**
     * 合并大文件
     * @param folderDir 临时文件夹 如 /file/folder/index.part
     * @param chunk 文件总片
     * @param folder 临时文件夹名
     * @param file 分片文件实体(最后一片)
     * @return 合并结果
     */
    private Result<UploadVO> merge(File folderDir,Integer chunk,String folder,MultipartFile file){
        File[] files1 = folderDir.listFiles();
        if (files1 == null || files1.length == 0) {
            return Result.error("没有找到分片文件");
        }

        // 筛选并按分片名序号从小到大排序
        List<File> partFiles = Arrays.stream(files1)
                .filter(f -> f.isFile() && f.getName().matches("\\d+"))
                .sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName())))
                .toList();

        if (partFiles.size() != chunk) {
            return Result.error(String.format("缺少分片文件，当前有 %d 个分片，期望 %d 个", partFiles.size(), chunk));
        }

        // 获取原文件名扩展名并生成唯一合并文件名，将合并文件建在分片文件夹外部以防删除时冲突
        String ext = "";
        String originalFilename = (file != null) ? file.getOriginalFilename() : "";
        if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;
        File resFile = new File(bigFileTempFolder, fileName);

        long expectSize = 0;
        try (FileOutputStream fileOutputStream = new FileOutputStream(resFile)) {
            byte[] buffer = new byte[8192];
            for (File partFile : partFiles) {
                expectSize += partFile.length();
                try (FileInputStream fileInputStream = new FileInputStream(partFile)) {
                    int readBytes;
                    while ((readBytes = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, readBytes);
                    }
                }
            }
        } catch (Exception e) {
            if (resFile.exists()) {
                resFile.delete();
            }
            throw new FileUploadException("合并分片文件流写入失败: " + e.getMessage());
        }

        // 检查合并后的文件是否正常
        if (!resFile.exists() || resFile.length() != expectSize) {
            if (resFile.exists()) {
                resFile.delete();
            }
            throw new FileUploadException("合并文件校验失败，大小不符或文件不存在");
        }

        // 上传到 MinIO
        String objectName = (folder == null ? "" : (folder + "/")) + fileName;
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(resFile.getAbsolutePath())
                            .contentType(file != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("上传合并后的文件到 MinIO 失败: " + e.getMessage());
        } finally {
            // 删除合并后的本地临时文件
            if (resFile.exists()) {
                resFile.delete();
            }
            // 递归清理整个分片临时文件夹
            cn.hutool.core.io.FileUtil.del(folderDir);
        }

        // 获取最终的静态 URL
        String fileUrl = endpoint;
        if (!fileUrl.endsWith("/")) {
            fileUrl += "/";
        }
        fileUrl += bucketName + "/" + objectName;

        return Result.success(new UploadVO(fileUrl, fileName));

    }



    private static void check(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        long size = file.getSize();

        boolean isImage = contentType != null && contentType.startsWith("image/");
        boolean isVideo = contentType != null && contentType.startsWith("video/");

        // 若 Content-Type 是通用流或未识别，根据文件名后缀兜底判定
        if (!isImage && !isVideo && cn.hutool.core.util.StrUtil.isNotBlank(filename)) {
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                lowerName.endsWith(".png") || lowerName.endsWith(".gif") || 
                lowerName.endsWith(".webp") || lowerName.endsWith(".bmp")) {
                isImage = true;
            } else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || 
                       lowerName.endsWith(".avi") || lowerName.endsWith(".mkv") || 
                       lowerName.endsWith(".flv") || lowerName.endsWith(".webm") || 
                       lowerName.endsWith(".ogg")) {
                isVideo = true;
            }
        }

        if (!isImage && !isVideo) {
            throw new ParameterException("只支持上传图片和视频文件！");
        }

        double sizeMB = size / 1024.0 / 1024.0;
        if (isImage && sizeMB > PostConstants.Business.MAX_IMAGE_SIZE_MB) {
            throw new ParameterException("图片文件大小不能超过 " + PostConstants.Business.MAX_IMAGE_SIZE_MB + "MB!");
        }
        if (isVideo && sizeMB > PostConstants.Business.MAX_VIDEO_SIZE_MB) {
            throw new ParameterException("视频文件大小不能超过 " + PostConstants.Business.MAX_VIDEO_SIZE_MB + "MB!");
        }

    }
}
