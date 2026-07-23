package com.summit.stp.entertainment.application.service;

import com.summit.stp.entertainment.application.vo.EmojiPackageVO;
import com.summit.stp.entertainment.application.vo.EmojiVO;

import java.util.List;

public interface EmojiAppService {
    List<EmojiVO> queryList(Long packageId);
    List<EmojiPackageVO> queryPackages(Integer status);
}
