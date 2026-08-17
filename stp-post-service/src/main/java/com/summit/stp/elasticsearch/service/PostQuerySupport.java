package com.summit.stp.elasticsearch.service;

import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.elasticsearch.document.PostDocument;

import java.util.List;

public interface PostQuerySupport {

    List<PostDocument> findByKeyWords(String keyword, Integer page);

    ESPageVO<Long> listBy(AdminPostQueryCommand command);
}
