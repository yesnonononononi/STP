package com.summit.stp.post.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.service.TagAppService;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.SearchSuggest.SuggestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagAppServiceImpl implements TagAppService {
    private final TagRepository tagRepository;
    private final PostTagRelRepository postTagRelRepository;
    @Override
    public TagVO getTagById(Long id) {
        Tag tag = tagRepository.findById(id);
        return tag == null ? null : convertToVO(tag);
    }

    @Override
    public TagVO getTagByName(String name) {
        Tag tag = tagRepository.findByName(name);
        return tag == null ? null : convertToVO(tag);
    }

    @Override
    public void createTag(String content) {
        Tag tag = Tag.builder()
                .tagName(content)
                .sort(0)
                .status(0)
                .useCount(0)
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        tagRepository.save(tag);
    }

    @Override
    public void updateTag(UpdateTagCommand command) {
        Tag tag = tagRepository.findById(command.getId());
        if (tag == null) {
            throw new ParameterException("标签不存在");
        }
        tag.updateInfo(command.getTagName(), command.getSort(), command.getStatus());
        tagRepository.save(tag);
    }

    @Override
    public void deleteTag(Long id) {
        tagRepository.delete(id);
    }

    @Override
    public Page<TagVO> getTagPage(long page, long pageSize) {
        Page<Tag> domainPage = tagRepository.queryByPage(page, pageSize);
        Page<TagVO> voPage = new Page<>(domainPage.getCurrent(), domainPage.getSize(), domainPage.getTotal());
        
        List<TagVO> voList = domainPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Result<SuggestVO> searchTag(String keyword, Integer limit) {
        return tagRepository.searchTag(keyword, limit);
    }

    @Override
    public List<TagVO> getRecentTag(Integer limit) {
        if(limit == null || limit <= 0 || limit > 100){
            limit = 10;
        }
        List<Tag> list = tagRepository.getRecentTag(limit);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private TagVO convertToVO(Tag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .tagName(tag.getTagName())
                .sort(tag.getSort())
                .useCount(tag.getUseCount())
                .status(tag.getStatus())
                .createTime(tag.getCreateTime())
                .build();
    }
}
