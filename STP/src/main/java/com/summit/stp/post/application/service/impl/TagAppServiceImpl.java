package com.summit.stp.post.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.application.command.CreateTagCommand;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.service.TagAppService;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.summit.stp.shared.exception.ParameterException;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagAppServiceImpl implements TagAppService {
    private final TagRepository tagRepository;

    @Override
    public TagVO getTagById(Long id) {
        TagPO tagPO = tagRepository.findById(id);
        return tagPO == null ? null : convertToVO(tagPO);
    }

    @Override
    public TagVO getTagByName(String name) {
        TagPO tagPO = tagRepository.findByName(name);
        return tagPO == null ? null : convertToVO(tagPO);
    }

    @Override
    public void createTag(CreateTagCommand command) {
        TagPO tag = new TagPO();
        tag.setTagName(command.getTagName());
        tag.setSort(command.getSort() == null ? 0 : command.getSort());
        tag.setStatus(command.getStatus() == null ? 1 : command.getStatus());
        tag.setUseCount(0);
        tag.setCreateTime(new Timestamp(System.currentTimeMillis()));
        tagRepository.save(tag);
    }



    @Override
    public void updateTag(UpdateTagCommand command) {
        TagPO tag = tagRepository.findById(command.getId());
        if (tag == null) {
            throw new ParameterException("标签不存在");
        }
        if (command.getTagName() != null) {
            tag.setTagName(command.getTagName());
        }
        if (command.getSort() != null) {
            tag.setSort(command.getSort());
        }
        if (command.getStatus() != null) {
            tag.setStatus(command.getStatus());
        }
        tagRepository.save(tag);
    }

    @Override
    public void deleteTag(Long id) {
        tagRepository.delete(id);
    }

    @Override
    public Page<TagVO> getTagPage(long page, long pageSize) {
        Page<TagPO> poPage = tagRepository.queryByPage(page, pageSize);
        Page<TagVO> voPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        
        List<TagVO> voList = poPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        voPage.setRecords(voList);
        return voPage;
    }

    private TagVO convertToVO(TagPO po) {
        return TagVO.builder()
                .id(po.getId())
                .tagName(po.getTagName())
                .sort(po.getSort())
                .useCount(po.getUseCount())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .build();
    }
}
