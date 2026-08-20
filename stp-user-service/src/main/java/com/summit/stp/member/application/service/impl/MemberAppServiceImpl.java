package com.summit.stp.member.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.admin.domain.exception.NoMemberPackageException;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.member.application.command.MemberCreateCommand;
import com.summit.stp.member.application.vo.UserMemberVO;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.user.api.vo.MemberTypeVO;
import com.summit.stp.user.api.vo.MemberVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberAppServiceImpl implements MemberAppService {
    private final MemberRepository<Member> memberRepository;

    private final UserMemberRepository userMemberRepository;

    @Override
    public Result<MemberVO> queryMemberById(Long id) {
        Member memberById = memberRepository.findMemberById(id).orElseThrow(() -> new BusinessException("会员套餐不存在"));
        MemberVO memberVO = toVO(memberById);
        return Result.success(memberVO);
    }

    @Override
    public Result<List<MemberVO>> queryMemberByType(Long typeId, int status) {
        List<MemberVO> list = memberRepository.findMemberByType(typeId, status).stream().map(this::toVO).toList();
        return Result.success(list);
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
    public Result<Map<Long, MemberTypeVO>> queryMemberTypeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        Map<Long, MemberTypeVO> map = new HashMap<>();
        ids.stream().distinct()
                .map(MemberType::getById)
                .filter(java.util.Objects::nonNull)
                .forEach(type -> map.put(type.getTypeId(), MemberTypeVO.builder()
                        .id(type.getTypeId())
                        .name(type.getTypeName())
                        .description(type.getDescription())
                        .build()));
        return Result.success(map);
    }

    @Override
    public Result<Map<Long, MemberVO>> queryMemberByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        Map<Long, MemberVO> map = memberRepository.findMemberByIds(ids).values().stream().map(this::toVO).collect(Collectors.toMap(MemberVO::getId, memberVO -> memberVO));
        return Result.success(map);
    }

    @Override
    public Result<Void> save(MemberCreateCommand command) {
        validateCommand(command);
        BigDecimal price = command.getPrice();
        Integer duration = command.getDuration();
        command.setDailyRate(price.divide(new BigDecimal(duration), RoundingMode.DOWN));
        Long id = command.getId();
        Member member = Member.builder()
                .id(id)
                .stock(command.getStock())
                .typeId(command.getTypeId())
                .isSuper(command.getIsSuper())
                .stock(command.getStock())
                .name(command.getName())
                .price(price)
                .duration(duration)
                .priority(command.getPriority())
                .dailyRate(command.getDailyRate())
                .discount(command.getDiscount())
                .description(command.getDescription())
                .status(command.getStatus() != null ? command.getStatus() : 0)
                .createTime(Instant.now())
                .build();
        try {
             if(id == null){ memberRepository.save(member);} else memberRepository.updateById(member);
        } catch (DuplicateKeyException e) {
            return Result.error("会员套餐已存在");
        }
        return Result.success();
    }

    @Override
    public Result<Void> deleteById(Long id) {
        if (id == null) return Result.error("会员套餐ID不能为空");
        Member memberById = memberRepository.findMemberById(id).orElseThrow(NoMemberPackageException::new);
        memberRepository.delete(memberById);
        return Result.success();
    }

    @Override
    public Result<Void> updateUserLevel(Long uid) {
        UserMember userMember = userMemberRepository.queryUserMemberByUserId(uid);
        userMember.raiseLevel();
        userMemberRepository.update(userMember);
        return Result.success();
    }

    @Override
    public Result<List<MemberVO>> queryPackageList() {
        List<Member> list = memberRepository.list();
        return Result.success(list.stream().map(this::toVO).toList());
    }

    @Override
    public PageResult<List<UserMemberVO>> queryUserMemberList(Integer page, Integer pageSize) {
        Page<UserMember> p = userMemberRepository.queryPage(page, pageSize);
        List<UserMemberVO> list = p.getRecords().stream().map(this::toUserMemberVO).toList();
        return new PageResult<>(p.getCurrent(), p.getTotal(), list);
    }

    @Override
    public Result<Void> extendUserExpire(Long uid, Timestamp expireTime) {
        //1, find user
        UserMember userMember = userMemberRepository.queryUserMemberByUserId(uid);
        //2, extend
        userMember.extendExpire(expireTime);
        //3, update
        userMemberRepository.update(userMember);
        return Result.success();
    }

    private UserMemberVO toUserMemberVO(UserMember userMember) {
        return UserMemberVO.builder()
                .userId(userMember.getUserId())
                .level(userMember.getLevel())
                .dailyRate(userMember.getDailyRate())
                .memberType(userMember.getMemberType())
                .createTime(userMember.getCreateTime())
                .expireTime(userMember.getExpireTime())
                .levelUpgradeTime(userMember.getLevelUpgradeTime())
                .packageTypeId(userMember.getPackageTypeId())
                .build();
    }

    private MemberVO toVO(Member member) {
        return MemberVO.builder()
                .id(member.getId())
                .name(member.getName())
                .price(member.getPrice())
                .discount(member.getDiscount())
                .description(member.getDescription())
                .duration(member.getDuration())
                .typeId(member.getTypeId())
                .typeName(member.getType() != null ? member.getType().getTypeName() : null)
                .priority(member.getPriority())
                .status(member.getStatus())
                .createTime(member.getCreateTime() != null ? Timestamp.from(member.getCreateTime()) : null)
                .build();
    }


    private void validateCommand(MemberCreateCommand command) {
        Integer duration = command.getDuration();
        String name = command.getName();
        BigDecimal price = command.getPrice();
        Double discount = command.getDiscount();
        if (discount != null && discount > 1) throw new BusinessException("套餐折扣不能大于10");
        if (duration == null || duration <= 0) throw new BusinessException("会员套餐时长不能为空");
        if (name == null || name.isEmpty()) throw new BusinessException("会员套餐名称不能为空");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("会员套餐价格不能为空");
    }
}

