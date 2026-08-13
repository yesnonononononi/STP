package com.summit.stp.common.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.api.dto.response.UploadVO;
import com.summit.stp.common.application.domain.exception.FileUploadException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.service.CommonAppService;
import com.summit.stp.common.result.Result;
import com.summit.stp.common.util.FileUploadUtil;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class CommonAppServiceImpl implements CommonAppService {
    private final FileUploadUtil fileUploadUtil;
    private final MinioClient minioClient;
    static File bigFileTempFolder;

    static {
        bigFileTempFolder = new File("file");
    }

    /**
     * MinIO 服务端内部地址，仅供后端 SDK 访问。
     * Docker 环境下通常是 http://minio:9000，本地环境通常是 http://localhost:9000。
     */
    @Value("${stp.minio.endpoint}")
    private String endpoint;

    /**
     * 返回给浏览器/前端的 MinIO 公网地址。
     * 优先使用 stp.minio.public-endpoint，其次使用 MINIO_PUBLIC_ENDPOINT，
     * 最后回退到 SDK endpoint，保证本地环境无需额外配置。
     */
    @Value("${stp.minio.public-endpoint:${MINIO_PUBLIC_ENDPOINT:}}")
    private String publicEndpoint;

    @Value("${stp.minio.default_bucket}")
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
        String fileUrl = buildPublicFileUrl(bucket, object);

        return new UploadVO(fileUrl, file.getOriginalFilename());
    }

    @Override
    public Result<UploadVO> uploadDealBigFile(MultipartFile file, String folder, Integer curIndex, Integer chunk) {
        //1, 校验上传参数
        validateUploadParams(curIndex);
        //2, 准备临时目录
        File folderDir = prepareTempFolder(folder);

        if (curIndex.equals(chunk)) {
            //3, 合并分片并上传
            return mergeAndUpload(folderDir, chunk, folder, file);
        }

        //4, 保存当前分片
        return saveChunkFile(file, folderDir, curIndex);
    }

    /**
     * 上传参数私有校验
     */
    private void validateUploadParams(Integer curIndex) {
        if (curIndex < 0) {
            throw new ParameterException("当前索引不能小于0");
        }
    }

    /**
     * 准备临时分片文件夹目录
     */
    private File prepareTempFolder(String folder) {
        if (!bigFileTempFolder.exists()) {
            bigFileTempFolder.mkdirs();
        }
        File folderDir = new File(bigFileTempFolder, folder);
        if (!folderDir.exists()) {
            folderDir.mkdirs();
        }
        return folderDir;
    }

    /**
     * 保存单个分片（128KB 扩容缓冲区高效保存）
     */
    private Result<UploadVO> saveChunkFile(MultipartFile file, File folderDir, Integer curIndex) {
        check(file);
        File partFile = new File(folderDir, String.valueOf(curIndex));
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(partFile)) {
            byte[] buffer = new byte[128 * 1024]; // 扩容为 128KB 缓冲区，减少94%的System Call
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
     * 合并大文件并上传 MinIO 私有编排方法
     */
    private Result<UploadVO> mergeAndUpload(File folderDir, Integer chunk, String folder, MultipartFile file) {
        List<File> partFiles = getAndValidatePartFiles(folderDir, chunk);
        String originalFilename = (file != null) ? file.getOriginalFilename() : "";
        File resFile = mergePartFilesWithNio(partFiles, originalFilename);

        String fileUrl = uploadToMinioAndClean(resFile, folderDir, folder, file);
        log.info("【文件模块】动作：完成大文件分片合并上传, fileUrl={}", fileUrl);
        return Result.success(new UploadVO(fileUrl, resFile.getName()));
    }

    /**
     * 获取并校验有序列的分片列表
     */
    private List<File> getAndValidatePartFiles(File folderDir, Integer chunk) {
        File[] files = folderDir.listFiles();
        if (files == null || files.length == 0) {
            throw new FileUploadException("没有找到分片文件");
        }

        List<File> partFiles = Arrays.stream(files)
                .filter(f -> f.isFile() && f.getName().matches("\\d+"))
                .sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName())))
                .toList();

        if (partFiles.size() != chunk) {
            throw new FileUploadException(String.format("缺少分片文件，当前有 %d 个分片，期望 %d 个", partFiles.size(), chunk));
        }
        return partFiles;
    }

    /**
     * 使用 NIO FileChannel 块级复制高效合并分片文件
     */
    private File mergePartFilesWithNio(List<File> partFiles, String originalFilename) {
        String ext = "";
        if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + ext;
        File resFile = new File(bigFileTempFolder, fileName);

        long expectSize = getExpectSize(partFiles, resFile);

        if (!resFile.exists() || resFile.length() != expectSize) {
            if (resFile.exists()) {
                resFile.delete();
            }
            throw new FileUploadException("合并文件校验失败，大小不符或文件不存在");
        }
        return resFile;
    }

    private static long getExpectSize(List<File> partFiles, File resFile) {
        long expectSize = 0;
        try (FileOutputStream fos = new FileOutputStream(resFile);
             FileChannel outChannel = fos.getChannel()) {
            for (File partFile : partFiles) {
                expectSize += partFile.length();
                try (FileInputStream fis = new FileInputStream(partFile);
                     FileChannel inChannel = fis.getChannel()) {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                }
            }
        } catch (Exception e) {
            if (resFile.exists()) {
                resFile.delete();
            }
            throw new FileUploadException("合并分片文件流写入失败: " + e.getMessage());
        }
        return expectSize;
    }

    /**
     * 上传合并后的文件到 MinIO 并递归清理临时文件
     */
    private String uploadToMinioAndClean(File resFile, File folderDir, String folder, MultipartFile file) {
        String objectName = (folder == null ? "" : (folder + "/")) + resFile.getName();
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
            if (resFile.exists()) {
                resFile.delete();
            }
            cn.hutool.core.io.FileUtil.del(folderDir);
        }
        return buildPublicFileUrl(bucketName, objectName);
    }




    /**
     * 组装给外部客户端使用的文件地址。
     *
     * endpoint 和 publicEndpoint 必须分离：
     * Docker 容器之间通过 minio 主机名通信，但浏览器无法解析 Docker 内部主机名。
     */
    private String buildPublicFileUrl(String bucket, String object) {
        String baseUrl = StrUtil.isBlank(publicEndpoint) ? endpoint : publicEndpoint;
        baseUrl = baseUrl.replaceAll("/+$", "");
        return baseUrl + "/" + bucket + "/" + object;
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
