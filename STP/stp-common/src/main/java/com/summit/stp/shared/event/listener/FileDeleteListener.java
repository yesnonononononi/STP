package com.summit.stp.shared.event.listener;

import com.summit.stp.shared.event.FileDeleteEvent;
import com.summit.stp.shared.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ConditionalOnProperty(name = "minio.endpoint")
@Component
@RequiredArgsConstructor
public class FileDeleteListener {
    private final FileUploadUtil fileUploadUtil;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleteEvent(FileDeleteEvent event) {
        List<String> fileUrls = event.getFileUrls();
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            for (String url : fileUrls) {
                try {
                    ParsedMinioUrl parsed = parseUrl(url);
                    if (parsed != null) {
                        fileUploadUtil.deleteFile(parsed.getObjectName(), parsed.getBucketName());
                        log.info("【MinIO 文件删除】成功物理删除文件, bucket: {}, object: {}", 
                                parsed.getBucketName(), parsed.getObjectName());
                    }
                } catch (Exception e) {
                    log.error("【MinIO 文件删除】删除文件失败, url: {}", url, e);
                }
            }
        });
    }

    private ParsedMinioUrl parseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path == null || path.isEmpty()) {
                return null;
            }
            int idx = path.indexOf('/');
            if (idx != -1) {
                String bucketName = path.substring(0, idx);
                String objectName = path.substring(idx + 1);
                return new ParsedMinioUrl(bucketName, objectName);
            } else {
                return new ParsedMinioUrl(null, path);
            }
        } catch (Exception e) {
            log.warn("【MinIO URL 解析】解析 URL 异常, url: {}", url, e);
        }
        return null;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    private static class ParsedMinioUrl {
        private final String bucketName;
        private final String objectName;
    }
}
