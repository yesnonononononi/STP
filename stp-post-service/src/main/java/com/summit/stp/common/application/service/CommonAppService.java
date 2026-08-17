package com.summit.stp.common.application.service;

import com.summit.stp.common.api.dto.response.UploadVO;
import com.summit.stp.common.application.api.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommonAppService {
    UploadVO upload(MultipartFile file,String folder);
    /**
     * 分片上传
     * @param file 文件
     * @param folder 文件夹
     * @param curIndex 当前分片
     * @param trunkNum 分片总数
     * @return 结果
     */
    Result<UploadVO> uploadDealBigFile(MultipartFile file, String folder, Integer curIndex, Integer trunkNum);
}
