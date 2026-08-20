package com.summit.stp.elasticsearch.service;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.common.application.api.dto.RangeDTO;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.elasticsearch.document.PostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.security.Timestamp;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class PostQuerySupportImpl implements PostQuerySupport {
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<PostDocument> findByKeyWords(String keyword, Integer page) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(
                        m -> m.query(keyword).fields("title", "content"))

                )
                .withPageable(Pageable.ofSize(10).withPage(Math.max(page - 1, 0)))
                .build();
        SearchHits<PostDocument> res = elasticsearchOperations.search(query, PostDocument.class);
        return res.get().map(SearchHit::getContent).toList();
    }

    @Override
    public ESPageVO<Long> listBy(AdminPostQueryCommand command) {
        Query query = buildQuery(command);
        SearchHits<PostDocument> res = elasticsearchOperations.search(query, PostDocument.class);
        return new ESPageVO<>(res.getTotalHits(), command.getPage(),
                res.get().map(SearchHit::getContent).map(PostDocument::getPostId).toList());
    }

    public Query buildQuery(AdminPostQueryCommand command) {
        Integer page = Objects.requireNonNullElse(command.getPage(), 1);
        Integer pageSize = Objects.requireNonNullElse(command.getPageSize(), 10);
        String keyword = command.getKeyword();
        RangeDTO<Timestamp> createTime = command.getCreateTime();
        Integer status = command.getStatus();
        RangeDTO<Long> comment = command.getComment();
        RangeDTO<Long> like = command.getLikeCount();
        RangeDTO<Timestamp> updateTime = command.getUpdateTime();
        Long postId = command.getPostId();
        Long creatorId = command.getCreatorId();
        BoolQuery.Builder builder = new BoolQuery.Builder();
        if (StrUtil.isNotBlank(keyword))
            builder.must(m -> m.multiMatch(mm -> mm.query(keyword).fields("title", "content")));
        if(postId != null)
            builder.filter(m -> m.term(t->t.field("postId").value(postId)));
        if (createTime != null)
            builder.filter(f -> f.range(r -> r.date(d -> {
                DateRangeQuery.Builder body = d.field("createTime");
                Timestamp max = createTime.getMax();
                if (max != null)
                    body.lte(String.valueOf(max));
                Timestamp min = createTime.getMin();
                if (min != null)
                    body.gte(String.valueOf(min));
                return body;
            })));
        if (status != null)
            builder.filter(f -> f.term(t -> t.field("status").value(status)));
        if (comment != null)
            builder.filter(f -> f.range(r -> r.number(n -> {
                NumberRangeQuery.Builder body = n.field("comment");
                Long min = comment.getMin();
                if (min != null)
                    body.gte(Double.valueOf(min));
                Long max = comment.getMax();
                if (max != null)
                    body.lte(Double.valueOf(max));
                return body;
            }
            )));
        if (like != null)
            builder.filter(f -> f.range(r -> r.number(n -> {
                NumberRangeQuery.Builder body = n.field("like");
                Long min = like.getMin();
                if (min != null)
                    body.gte(Double.valueOf(min));
                Long max = like.getMax();
                if (max != null)
                    body.lte(Double.valueOf(max));
                return body;
            })));
        if (updateTime != null)
            builder.filter(f -> f.range(r -> r.date(d -> {
                DateRangeQuery.Builder body = d.field("updateTime");
                Timestamp min = updateTime.getMin();
                if (min != null)
                    body.gte(String.valueOf(min));
                Timestamp max = updateTime.getMax();
                if (max != null)
                    body.lte(String.valueOf(max));
                return body;
            })));
        if (creatorId != null)
            builder.filter(f -> f.term(t -> t.field("creatorId").value(creatorId)));
        BoolQuery bq = builder.build();
        return NativeQuery.builder()
                .withPageable(Pageable.ofSize(pageSize).withPage(Math.max(page - 1, 0)))
                .withQuery(q -> q.bool(bq)).build();

    }
}
