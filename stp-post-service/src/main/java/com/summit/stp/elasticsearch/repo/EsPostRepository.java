package com.summit.stp.elasticsearch.repo;

import com.summit.stp.elasticsearch.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
public interface EsPostRepository extends ElasticsearchRepository<PostDocument, Long> {


}


