package com.summit.stp.member.application.converter;

import com.summit.stp.common.application.api.vo.MemberVO;
import com.summit.stp.member.domain.model.Member;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemberVOConverter {
    
    public static final MemberVOConverter INSTANCE = new MemberVOConverter();
    
    private MemberVOConverter() {}
    
    public MemberVO toVO(Member member) {
        if (member == null) {
            return null;
        }
        
        MemberVO.MemberVOBuilder builder = MemberVO.builder()
                .id(member.getId())
                .name(member.getName())
                .price(member.getPrice())
                .discount(member.getDiscount())
                .description(member.getDescription())
                .duration(member.getDuration())
                .quantity(member.getStock())
                .typeId(member.getTypeId())
                .priority(member.getPriority());
                
        if (member.getType() != null) {
            builder.typeName(member.getType().getTypeName());
        }
        
        return builder.build();
    }
    
    public List<MemberVO> toVOList(List<Member> members) {
        if (members == null) {
            return Collections.emptyList();
        }
        return members.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
