package com.summit.stp.elasticsearch.service;

import com.summit.stp.elasticsearch.document.PostDocument;

import java.util.List;

public interface QueryService {
    List<PostDocument> findByKeyWords(String keyword);
}
