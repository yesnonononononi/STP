package com.summit.stp.member.application.service.impl;

import com.summit.stp.member.application.converter.MemberVOConverter;
import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.common.application.vo.MemberTypeVO;
import com.summit.stp.common.application.vo.MemberVO;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberAppServiceImpl implements MemberAppService {
    private final MemberRepository memberRepository;
    private static final MemberVOConverter VO_CONVERTER = MemberVOConverter.INSTANCE;

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
        List<MemberTypeVO> res = Arrays.stream(MemberType.values())
                .filter(type -> type.getStatus() == 1)
                .map(memberType -> MemberTypeVO.builder()
                        .id(memberType.getTypeId())
                        .name(memberType.getTypeName())
                        .description(memberType.getDescription())
                        .build()
                ).toList();
        return Result.success(res);
    }

    @Override
    public Result<MemberTypeVO> queryMemberTypeById(Long id) {
        MemberType memberType = MemberType.getById(id);
        if (memberType == null) {
            return Result.success(null);
        }
        MemberTypeVO vo = MemberTypeVO.builder()
                .id(memberType.getTypeId())
                .name(memberType.getTypeName())
                .description(memberType.getDescription())
                .build();
        return Result.success(vo);
    }

    @Override
    public Result<Map<Long, MemberVO>> queryMemberByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        Map<Long, MemberVO> map = memberRepository.findMemberByIds(ids);
        return Result.success(map);
    }
}
