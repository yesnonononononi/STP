package com.summit.stp.elasticsearch.service;

import com.summit.stp.elasticsearch.document.PostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class QueryServiceImpl implements QueryService {
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<PostDocument> findByKeyWords(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q ->
                        q.multiMatch(
                                m -> m.query(keyword).fields("title", "content")
                        )

                ).build();
        SearchHits<PostDocument> res = elasticsearchOperations.search(query, PostDocument.class);
        return res.get().map(SearchHit::getContent).toList();
    }
}
