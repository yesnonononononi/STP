package com.summit.stp.user.application.service.impl;

import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.Username;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.domain.event.FileDeleteEvent;
import com.summit.stp.common.application.domain.exception.NoFoundUserInfoException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.application.domain.service.CaptchaService;
import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.common.constants.MemberConstants;
import com.summit.stp.common.util.EncryptUtil;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.user.application.command.CreateUserCommand;
import com.summit.stp.user.application.service.UserApplicationService;
import com.summit.stp.user.application.command.UserPasswordUpdateCommand;
import com.summit.stp.user.application.command.UserPhoneBindCommand;
import com.summit.stp.user.application.command.UserProfileUpdateCommand;
import com.summit.stp.user.application.service.UserCacheProvider;

import com.summit.stp.user.domain.model.Email;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.model.UserFollow;

import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.UserFollowRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.summit.stp.common.application.domain.event.UserChangedEvent;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAPPServiceImpl implements UserApplicationService {
    private final UserRepository<User> userRepository;
    private final UserMemberRepository userMemberRepository;
    private final CaptchaService captchaService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserCacheProvider userCacheProvider;
    private final QueueSender queueSender;
    private final UserFollowRepositoryImpl userFollowRepositoryImpl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileUpdateCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("修改用户基本资料: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername).orElseThrow(NoFoundUserInfoException::new);

        String newEmail = command.getEmail();
        String oldAvatar = user.getAvatar();
        String oldBgImage = user.getBgImage();
        user.updateProfile(command.getNick(), command.getAvatar(), newEmail, command.getIntroduce(), command.getVerifyCode(), command.getGender(), command.getAge());

        String newBgImage = command.getBgImage();

        if (newBgImage != null) {
            if (StringUtils.hasText(newBgImage)) {
                user.updateBgImage(newBgImage);
                if (!oldBgImage.equals(newBgImage)) {
                    applicationEventPublisher.publishEvent(new FileDeleteEvent(this, List.of(oldBgImage)));
                }
            } else {
                user.clearBgImage();
            }
        }

        userRepository.updateById(user);

        String newAvatar = command.getAvatar();
        if (oldAvatar != null && !oldAvatar.isEmpty() && !oldAvatar.equals(newAvatar)) {
            applicationEventPublisher.publishEvent(new FileDeleteEvent(this, List.of(oldAvatar)));
        }

        log.info("用户基本资料修改成功: {}", currentUsername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(UserPhoneBindCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("用户修改手机号绑定: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername).orElseThrow(NoFoundUserInfoException::new);


        boolean isPhoneValid = captchaService.validate(command.getPhoneNumber(), command.getVerifyCode());
        if (!isPhoneValid) {
            throw new ParameterException("手机验证码错误或已失效");
        }

        user.changePhoneNumber(PhoneNumber.of(command.getPhoneNumber()));
        userRepository.updateById(user);
        log.info("用户手机号绑定修改成功: {}", currentUsername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordUpdateCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("用户修改登录密码: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername).orElseThrow(NoFoundUserInfoException::new);


        user.changePassword(command.getOldPassword(), Password.fromRaw(command.getNewPassword()));
        userRepository.updateById(user);
        log.info("用户密码修改成功: {}", currentUsername);
    }


    @Override
    public UserProfileVO findUserById(Long id) {
        User user = userRepository.findUserById(id).orElse(null);
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
        return UserSimpleVO.builder()
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
    }


    @Override
    public Map<Long, UserSimpleVO> findSimpleUserByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        int size = userIds.size();
        userIds = new ArrayList<>(new HashSet<>(userIds));
        Map<Long, UserSimpleVO> resFromCache = new HashMap<>();


        try {
            // 尝试从缓存获取用户信息
            resFromCache = userCacheProvider.batchGetUserSimpleVO((List<Long>) userIds);
            if (!resFromCache.isEmpty()) {
                log.info("【批量获取用户信息-缓存命中】:{}/{}条", resFromCache.size(), size);
                if (resFromCache.size() == size) {
                    return resFromCache;
                }
                Map<Long, UserSimpleVO> finalResFromCache = resFromCache;
                userIds = userIds.stream().filter(id -> !finalResFromCache.containsKey(id)).toList();
            }
        } catch (Exception e) {
            log.error("【批量缓存用户】获取用户信息缓存失败,降级数据库查询", e);
        }

        // 查询剩余未缓存的用户从数据库
        log.info("【批量获取用户信息-缓存未全部命中】查询数据库");
        Map<Long, UserSimpleVO> res = findUsersFromDB(userIds);
        log.info("【批量获取用户信息-db】:{}/{}条", res.size(), size);

        try {
            userCacheProvider.batchSetUserSimpleVO(res);
            log.info("【批量缓存用户】设置用户信息缓存成功");
        } catch (Exception e) {
            log.error("【批量缓存用户】设置用户信息缓存失败,跳过缓存", e);
        }

        res.putAll(resFromCache);
        return res;
    }

    @Override
    public List<UserProfileVO> findProfileByIds(List<Long> ids) {
        Map<Long, User> userByIds = userRepository.findUserByIds(ids);
        Map<Long, UserMember> memberMap = userMemberRepository.queryUserMemberByUserIds(ids);
        List<UserProfileVO> res = new ArrayList<>();
        ids.forEach(id -> {
            User user = userByIds.get(id);
            UserMember userMember = memberMap.get(id);
            if (user != null) {
                res.add(convertToVO(user, userMember, false));
            }
        });
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(CreateUserCommand build) {
        String phoneNumber = build.getPhoneNumber();
        Username nick = Username.of("U_" + phoneNumber);
        User user = User.builder()
                .username(nick)
                .password(Password.fromRaw(build.getPassword()))
                .phoneNumber(PhoneNumber.of(phoneNumber))
                .createTime(Instant.now())
                .build();
        try {
            Long userId = userRepository.saveUser(user);
            Timestamp createTime = Timestamp.from(user.getCreateTime());

            initUserEvent(phoneNumber, nick.getValue(), userId, createTime);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户已存在");
        }

    }


    @Override
    public UserProfileVO findUserByPhone(String phone) {
        User user = userRepository.findUserByPhone(phone).orElseThrow(NoFoundUserInfoException::new);
        return convertToVO(user, null, false);
    }

    @Override
    public UserProfileVO findUserByUname(String uname) {
        User userByName = userRepository.findUserByName(uname).orElseThrow(NoFoundUserInfoException::new);
        return convertToVO(userByName, null, false);
    }

    private UserProfileVO convertToVO(User user, UserMember userMember) {
        boolean followed = false;
        try {
            UserSession currentUser = UserHolder.getUser();
            if (!currentUser.isLogin()) {
                return convertToVO(user, userMember, followed);
            }
            if (!currentUser.getId().equals(user.getId())) {
                UserFollow follow = userFollowRepositoryImpl.findByFollowerAndFollowee(currentUser.getId(), user.getId()).orElse(null);
                if (follow != null) {
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
        return UserProfileVO.builder()
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
                .createTime(String.valueOf(user.getCreateTime()))
                .status(String.valueOf(user.getStatusCode()))
                .bgImage(user.getBgImage())
                .vipConfigIcon(userMember != null && userMember.getLevel() != null ? userMember.getLevel().getIconUrl() : null)
                .vipExpireDate(vipExpireDate)
                .gender(user.getGender())
                .age(user.getAge())
                .followed(followed)
                .build();
    }


    /**
     * 批量从数据库获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息Map
     */
    private Map<Long, UserSimpleVO> findUsersFromDB(Collection<Long> userIds) {

        Map<Long, User> map = userRepository.findUserByIds(userIds);
        Map<Long, UserMember> memberMap = userMemberRepository.queryUserMemberByUserIds(userIds);

        return map.values().stream()
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
    }

    private void initUserEvent(String phoneNumber, String nick, Long userId, Timestamp createTime) {
        if (userId == null) throw new RuntimeException("未传入用户ID,初始化用户失败");
        UserChangedEvent event = UserChangedEvent.builder()
                .eventType(UserChangedEvent.EventType.CREATE)
                .userId(userId)
                .phone(phoneNumber)
                .nick(nick)
                .gender(1)
                .createTime(createTime)
                .build();

        try {
            queueSender.sendRegisterEvent(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_CHANGE, event);
            log.info("【用户注册】标识：MQ 动作：发布用户创建事件成功, userId={}", userId);
        } catch (Exception e) {
            log.error("【用户注册】标识：MQ 动作：发布用户创建事件失败, userId={}", userId, e);
        }
    }
}

