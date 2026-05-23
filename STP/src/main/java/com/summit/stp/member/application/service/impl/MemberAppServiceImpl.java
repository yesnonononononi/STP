package com.summit.stp.member.application.service.impl;

import com.summit.stp.order.application.converter.MemberVOConverter;
import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.member.application.vo.MemberTypeVO;
import com.summit.stp.member.application.vo.MemberVO;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.domain.repository.MemberTypeRepository;
import com.summit.stp.order.infrastructure.persistence.mapper.MemberTypeMapper;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAppServiceImpl implements MemberAppService {
    private final MemberRepository memberRepository;
    private final MemberTypeMapper memberTypeMapper;
    private static final MemberVOConverter VO_CONVERTER = MemberVOConverter.INSTANCE;
    private final MemberTypeRepository memberTypeRepository;

    @Override
    public Result<MemberVO> queryMemberById(Long id) {
        Member memberById = memberRepository.findMemberById(id);
        MemberVO memberVO = VO_CONVERTER.toVO(memberById);
        return Result.success(memberVO);
    }

    @Override
    public Result<List<MemberVO>> queryMemberByType(Long typeId, int status) {
        List<Member> list = memberRepository.findMemberByType(typeId, status);
        List<MemberVO> voList = VO_CONVERTER.toVOList(list);
        return Result.success(voList);
    }



    @Override
    public Result<List<MemberTypeVO>> list() {
        List<MemberType> list = memberTypeRepository.list(1);
        List<MemberTypeVO> res = list.stream()
                .map(memberType -> MemberTypeVO.builder()
                        .id(memberType.getTypeId())
                        .name(memberType.getTypeName())
                        .description(memberType.getDescription())
                        .build()
                ).toList();
        return Result.success(res);
    }
}
