package com.summit.stp.order.application.converter;

import com.summit.stp.member.application.vo.MemberVO;
import com.summit.stp.member.domain.model.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MemberVOConverter {
    
    MemberVOConverter INSTANCE = Mappers.getMapper(MemberVOConverter.class);
    
    @Mapping(target = "typeId", source = "type.typeId")
    @Mapping(target = "typeName", source = "type.typeName")
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "isSuper", ignore = true)
    MemberVO toVO(Member member);
    
    List<MemberVO> toVOList(List<Member> members);
}
