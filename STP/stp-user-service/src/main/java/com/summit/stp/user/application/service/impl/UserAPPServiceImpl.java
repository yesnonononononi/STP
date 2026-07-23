package com.summit.stp.user.application.service.impl;

import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.constants.MemberConstants;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.service.CaptchaService;
import com.summit.stp.shared.exception.NoFoundUserInfoException;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.util.EncryptUtil;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.command.UserPasswordUpdateCommand;
import com.summit.stp.user.application.command.UserPhoneBindCommand;
import com.summit.stp.user.application.command.UserProfileUpdateCommand;
import com.summit.stp.user.application.service.UserCacheProvider;
import com.summit.stp.user.application.vo.UserProfileVO;
import com.summit.stp.shared.application.vo.UserSimpleVO;
import com.summit.stp.user.domain.model.Email;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserFollowRepository;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserFollowPO;
import com.summit.stp.userAuth.domain.model.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import com.summit.stp.shared.event.FileDeleteEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAPPServiceImpl implements UserApplicationService {
    private final UserRepository userRepository;
    private final UserMemberRepository userMemberRepository;
    private final CaptchaService captchaService;
    private final UserFollowRepository userFollowRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserCacheProvider userCacheProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileUpdateCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("修改用户基本资料: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername);
        if (user == null) {
            throw new NoFoundUserInfoException(String.format("用户 %s 不存在", currentUsername));
        }

        String newEmail = command.getEmail();
        if (StringUtils.hasText(newEmail)) {
            String oldEmail = (user.getEmail() != null) ? user.getEmail().getValue() : null;
            if (!newEmail.equals(oldEmail)) {
                if (!StringUtils.hasText(command.getVerifyCode())) {
                    throw new ParameterException("修改邮箱时必须填写验证码");
                }
                boolean isEmailValid = captchaService.validateEmail(newEmail, command.getVerifyCode());
                if (!isEmailValid) {
                    throw new ParameterException("邮箱验证码错误或已失效");
                }
            }
        }

        String oldAvatar = user.getAvatar();
        String oldBgImage = user.getBgImage();
        user.updateProfile(command.getNick(), command.getAvatar(), newEmail, command.getIntroduce(), command.getVerifyCode(), command.getGender(), command.getAge());

        if (command.getBgImage() != null) {
            if (StringUtils.hasText(command.getBgImage())) {
                user.updateBgImage(command.getBgImage());
            } else {
                user.clearBgImage();
            }
        }

        userRepository.save(user);

        String newAvatar = command.getAvatar();
        if (oldAvatar != null && !oldAvatar.isEmpty() && !oldAvatar.equals(newAvatar)) {
            applicationEventPublisher.publishEvent(new FileDeleteEvent(this, List.of(oldAvatar)));
        }

        if (command.getBgImage() != null) {
            String newBgImage = command.getBgImage();
            if (oldBgImage != null && !oldBgImage.isEmpty() && !oldBgImage.equals(newBgImage)) {
                applicationEventPublisher.publishEvent(new FileDeleteEvent(this, java.util.List.of(oldBgImage)));
            }
        }

        log.info("用户基本资料修改成功: {}", currentUsername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(UserPhoneBindCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("用户修改手机号绑定: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean isPhoneValid = captchaService.validate(command.getPhoneNumber(), command.getVerifyCode());
        if (!isPhoneValid) {
            throw new ParameterException("手机验证码错误或已失效");
        }

        user.changePhoneNumber(PhoneNumber.of(command.getPhoneNumber()));
        userRepository.save(user);
        log.info("用户手机号绑定修改成功: {}", currentUsername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordUpdateCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("用户修改登录密码: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.changePassword(command.getOldPassword(), Password.fromRaw(command.getNewPassword()));
        userRepository.save(user);
        log.info("用户密码修改成功: {}", currentUsername);
    }


    @Override
    public UserProfileVO findUserById(Long id) {
        User user = userRepository.findUserById(id);
        if (user == null) {
            return null;
        }
        UserMember userMember = userMemberRepository.queryUserMemberByUserId(id);
        return convertToVO(user, userMember);
    }

    @Override
    public UserSimpleVO findSimpleUserById(Long id) {
        UserProfileVO profile = findUserById(id);
        if (profile == null) {
            return null;
        }
        UserSimpleVO vo = UserSimpleVO.builder()
                .id(profile.getId())
                .nick(profile.getNick())
                .avatar(profile.getAvatar())
                .vipType(profile.getVipType())
                .ip(profile.getIp())
                .memberLevel(profile.getMemberLevel())
                .memberLevelName(profile.getMemberLevel()) // 填充会员等级名称，避免前端解析为空
                .vipConfigIcon(profile.getVipConfigIcon())
                .followed(profile.isFollowed())
                .introduction(profile.getIntroduction())
                .fans(profile.getFans() != null ? Long.parseLong(profile.getFans()) : 0L)
                .liked(profile.getLiked() != null ? Long.parseLong(profile.getLiked()) : 0L)
                .topic(profile.getTopic() != null ? Long.parseLong(profile.getTopic()) : 0L)
                .gender(profile.getGender() != null ? String.valueOf(profile.getGender()) : null)
                .build();
        if (vo != null && "The follower of summit".equals(vo.getNick())) {
            vo.setVipConfigIcon("http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3N0cC1zdW1taXQtZmlsZXMvYXZhdGFyL2RlY29yYXRpb24vMjAyNTAxMDIxNzM1ODA1Njc5MTE0NDE3NS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BUThUT1JDRFk3RkxOVUFVQjVBWiUyRjIwMjYwNzAxJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MDcwMVQxMzA1MzRaJlgtQW16LUV4cGlyZXM9NDMyMDAmWC1BbXotU2VjdXJpdHktVG9rZW49ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhZMk5sYzNOTFpYa2lPaUpCVVRoVVQxSkRSRmszUmt4T1ZVRlZRalZCV2lJc0ltVjRjQ0k2TVRjNE1qazFOREl6T0N3aWNHRnlaVzUwSWpvaVlXUnRhVzRpZlEuNlVZOElwX1l1aGM5MjhndDY1bGtKRGRoX2ZfdUI0ck5BM3BrUEwzbVhONUhmbTU0SHZRenoxbDBIb0xFMWRWYXE1V29WZnpjeVc4Mjh5UG1jamU1UUEmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnZlcnNpb25JZD1udWxsJlgtQW16LVNpZ25hdHVyZT1mOWI0MTU4MmVmMzc4NjcyYjQxZDcyODM4YWQ1ZTY4NzNiNGYyNWQ5ZjgyNjEzOWQ3N2I4NTMyN2FhOTE3ZTIw");
        }
        return vo;
    }

    @Override
    public Map<Long, UserSimpleVO> findSimpleUserByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        userIds = userIds.stream().distinct().toList();
        try{
            Map<Long, UserSimpleVO> resFromCache = userCacheProvider.batchGetUserSimpleVO((List<Long>) userIds);
            if(!resFromCache.isEmpty()){
                log.info("【批量获取用户信息-缓存命中】:{}/{}条",resFromCache.size(),userIds.size());
                if(resFromCache.size() == userIds.size()) {
                    return resFromCache;
                }
                userIds  = userIds.stream().filter(id -> !resFromCache.containsKey(id)).toList();
            }
        }catch (Exception e){
            log.error("【批量缓存用户】获取用户信息缓存失败,降级数据库查询", e);
        }
        log.info("【批量获取用户信息-缓存未全部命中】查询数据库");

        Map<Long, User> map = userRepository.findUserByIds(userIds);
        Map<Long, UserMember> memberMap = userMemberRepository.queryUserMemberByUserIds(userIds);

        Map<Long, UserSimpleVO> res = map.values().stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    UserMember userMember = memberMap.get(user.getId());
                    String memberLevel = "";
                    String vipConfigIcon = null;
                    if (userMember != null) {
                        MemberLevelConfig level = userMember.getLevel();
                        if (level != null) {
                            memberLevel = level.getLevelName();
                            vipConfigIcon = level.getIconUrl();
                        }
                    }
                    String nick = user.getNick();
                    return UserSimpleVO.builder()
                            .id(user.getId())
                            .nick(nick == null ? user.getUsername().getValue() : nick)
                            .avatar(user.getAvatar())
                            .ip(user.getIp())
                            .fans(user.getFans())
                            .vipType(user.getVipType())
                            .liked(user.getLiked())
                            .topic(user.getTopic())
                            .memberLevel(memberLevel)
                            .memberLevelName(memberLevel)
                            .vipConfigIcon(vipConfigIcon)
                            .build();
                })
                .collect(Collectors.toMap(UserSimpleVO::getId, vo -> vo, (v1, v2) -> v1));
        try {
            userCacheProvider.batchSetUserSimpleVO(res);
        }catch (Exception e){
            log.error("【批量缓存用户】设置用户信息缓存失败,跳过缓存", e);
        }

        return res;
    }

    private UserProfileVO convertToVO(User user, UserMember userMember) {
        boolean followed = false;
        try {
            UserSession currentUser = UserHolder.getUser();
            if(!currentUser.isLogin()){
                return convertToVO(user, userMember, followed);
            }
            if (!currentUser.getId().equals(user.getId())) {
                UserFollowPO follow = userFollowRepository.findByFollowerAndFollowee(currentUser.getId(), user.getId());
                if (follow != null && follow.getStatus() == 1) {
                    followed = true;
                }
            }
        } catch (NoFoundUserInfoException e) {
            // 未登录或非 Web 请求线程，默认设为未关注
        }
        return convertToVO(user, userMember, followed);
    }

    private UserProfileVO convertToVO(User user, UserMember userMember, boolean followed) {
        if (user == null) {
            return null;
        }

        String memberLevel = "";
        String vipType = MemberConstants.DEFAULT_VIP_TYPE;
        String vipExpireDate = null;

        if (userMember != null) {
            if (userMember.getLevel() != null && userMember.getLevel().getLevel() != null) {
                memberLevel = userMember.getLevel().getLevelName();
            }
            if (userMember.isMemberActive()) {
                if (userMember.getMemberType() != null) {
                    vipType = userMember.getMemberType().getTypeName();
                } else {
                    vipType = MemberConstants.FALLBACK_VIP_TYPE;
                }
                if (userMember.getExpireTime() != null) {
                    vipExpireDate = userMember.getExpireTime().toString();
                }
            }
        }

        String nick = user.getNick();
        PhoneNumber phoneNumber = user.getPhoneNumber();
        Email email = user.getEmail();
        UserProfileVO vo = UserProfileVO.builder()
                .id(user.getId())
                .nick(nick == null ? user.getUsername().getValue() : nick)
                .avatar(user.getAvatar())
                .introduction(user.getIntroduction())
                .memberLevel(memberLevel)
                .phone(phoneNumber != null ? EncryptUtil.encodeStrForStar(phoneNumber.getValue(), "phone") : null)
                .email(email != null ? EncryptUtil.encodeStrForStar(email.getValue(), "email") : null)
                .ip(user.getIp())
                .liked(user.getLiked() != null ? String.valueOf(user.getLiked()) : "0")
                .topic(user.getTopic() != null ? String.valueOf(user.getTopic()) : "0")
                .fans(user.getFans() != null ? String.valueOf(user.getFans()) : "0")
                .vipType(vipType)
                .bgImage(user.getBgImage())
                .vipConfigIcon(userMember != null && userMember.getLevel() != null ? userMember.getLevel().getIconUrl() : null)
                .vipExpireDate(vipExpireDate)
                .gender(user.getGender())
                .age(user.getAge())
                .followed(followed)
                .build();
        if (vo != null && "The follower of summit".equals(vo.getNick())) {
            vo.setVipConfigIcon("http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3N0cC1zdW1taXQtZmlsZXMvYXZhdGFyL2RlY29yYXRpb24vMjAyNTAxMDIxNzM1ODA1Njc5MTE0NDE3NS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BUThUT1JDRFk3RkxOVUFVQjVBWiUyRjIwMjYwNzAxJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MDcwMVQxMzA1MzRaJlgtQW16LUV4cGlyZXM9NDMyMDAmWC1BbXotU2VjdXJpdHktVG9rZW49ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhZMk5sYzNOTFpYa2lPaUpCVVRoVVQxSkRSRmszUmt4T1ZVRlZRalZCV2lJc0ltVjRjQ0k2TVRjNE1qazFOREl6T0N3aWNHRnlaVzUwSWpvaVlXUnRhVzRpZlEuNlVZOElwX1l1aGM5MjhndDY1bGtKRGRoX2ZfdUI0ck5BM3BrUEwzbVhONUhmbTU0SHZRenoxbDBIb0xFMWRWYXE1V29WZnpjeVc4Mjh5UG1jamU1UUEmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnZlcnNpb25JZD1udWxsJlgtQW16LVNpZ25hdHVyZT1mOWI0MTU4MmVmMzc4NjcyYjQxZDcyODM4YWQ1ZTY4NzNiNGYyNWQ5ZjgyNjEzOWQ3N2I4NTMyN2FhOTE3ZTIw");
        }
        return vo;
    }
}
