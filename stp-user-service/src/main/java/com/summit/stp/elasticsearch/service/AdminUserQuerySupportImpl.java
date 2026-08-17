package com.summit.stp.elasticsearch.service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import com.summit.stp.common.application.api.dto.RangeDTO;
import com.summit.stp.elasticsearch.document.UserDocument;
import com.summit.stp.common.application.api.result.ESPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserQuerySupportImpl implements AdminUserQuerySupport {
    private final ElasticsearchOperations elasticsearchOperations;


    @Override
    public ESPageVO<Long> listBy (AdminUserQueryCommand command) {
        Query query = buildQuery(command);
        SearchHits<UserDocument> res = elasticsearchOperations.search(query, UserDocument.class);
        List<Long> list = res.stream()
                .map(SearchHit::getContent)
                .map(UserDocument::getUserId)
                .toList();
        return ESPageVO.<Long>builder()
                .total(res.getTotalHits())
                .page(Objects.requireNonNullElse(command.getPage(),1))
                .data(list)
                .build();
    }

    private Query buildQuery(AdminUserQueryCommand command) {
        Integer size = command.getSize();
        if(size == null || size == 0){
            size = 10;
        }
        return NativeQuery.builder()
                .withQuery(q ->
                        q.bool(b ->
                                resolveCommand(command)
                        )
                )
                .withSort(Sort.by(
                        Sort.Order.desc("createTime")
                ))
                .withPageable(Pageable.ofSize(size).withPage(Math.max(0, Objects.requireNonNullElse(command.getPage(), 0) - 1)))
                .build();
    }


    /**
     * 解析命令
     *
     * @param command 查询命令
     * @return 查询条件
     */
    private BoolQuery.Builder resolveCommand(AdminUserQueryCommand command) {
        BoolQuery.Builder b = new BoolQuery.Builder();
        String ip = command.getIp();
        String keyword = command.getKeyword();
        String phone = command.getPhone();
        String gender = command.getGender();
        Boolean enabledVIP = command.getEnabledVIP();
        Integer statusCode = command.getStatusCode();
        RangeDTO<Integer> vipLevel = command.getVIPLevel();
        RangeDTO<Timestamp> createTime = command.getCreateTime();
        RangeDTO<Integer> age = command.getAge();
        RangeDTO<Number> follow = command.getFollow();
        RangeDTO<Number> fans = command.getFans();
        RangeDTO<Number> topic = command.getTopic();

        if (StrUtil.isNotBlank(keyword)) b.must(m -> m.wildcard(w -> w.field("nick.keyword").value("*"+keyword +"*")));
        if (phone != null && StrUtil.isNotBlank(phone)) b.filter(f -> f.term(t -> t.field("phone").value(phone)));
        if (ip != null) b.filter(f -> f.term(t -> t.field("ip").value(ip)));
        if (gender != null) b.filter(f -> f.term(t -> t.field("gender").value(gender)));
        if (enabledVIP != null) b.filter(f -> f.term(t -> t.field("isVip").value(enabledVIP)));
        if (statusCode != null) b.filter(f -> f.term(t -> t.field("statusCode").value(statusCode)));
        if (age != null) {
            b.filter(f -> f.range(t -> t.number(tr -> {
                NumberRangeQuery.Builder body = tr.field("age");
                Integer min = age.getMin();
                Integer max = age.getMax();
                if (min != null) body.gte(Double.valueOf(min));
                if (max != null) body.lte(Double.valueOf(max));
                return body;
            })));
        }
        if (fans != null) {
            b.filter(f -> f.range(t -> t.number(tr -> {
                Number min = fans.getMin();
                Number max = fans.getMax();
                NumberRangeQuery.Builder body = tr.field("fans");
                if (min != null) body.gte(min.doubleValue());
                if (max != null) body.lte(max.doubleValue());
                return body;
            })));
        }
        if (createTime != null) {
            b.filter(f -> f.range(r -> r.date(nq -> {
                DateRangeQuery.Builder body = nq.field("createTime");
                if (createTime.getMin() != null) body.gte(createTime.getMin().toString());
                if (createTime.getMax() != null) body.lte(createTime.getMax().toString());
                return body;
            })));
        }
        if (follow != null) {
            b.filter(f -> f.range(r -> r.number(n -> {
                NumberRangeQuery.Builder body = n.field("follow");
                if (follow.getMin() != null) body.gte(follow.getMin().doubleValue());
                if (follow.getMax() != null) body.lte(follow.getMax().doubleValue());
                return body;
            })));
        }
        if (topic != null) {
            b.filter(f -> f.range(t -> t.number(nq -> {
                NumberRangeQuery.Builder body = nq.field("topic");
                if (topic.getMin() != null) body.gte(topic.getMin().doubleValue());
                if (topic.getMax() != null) body.lte(topic.getMax().doubleValue());
                return body;
            })));
        }
        if (vipLevel != null ) {
            b.filter(f -> f.range(t -> t.number(nq -> {
                NumberRangeQuery.Builder body = nq.field("vipLevel");
                if (vipLevel.getMin() != null) body.gte(Double.valueOf(vipLevel.getMin()));
                if (vipLevel.getMax() != null) body.lte(Double.valueOf(vipLevel.getMax()));
                return body;
            })));
        }
        return b;
    }
}
