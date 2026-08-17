package com.summit.stp.elasticsearch.repo;

import com.summit.stp.elasticsearch.document.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AdminUserQueryRepository extends ElasticsearchRepository<UserDocument, Long> {
}
