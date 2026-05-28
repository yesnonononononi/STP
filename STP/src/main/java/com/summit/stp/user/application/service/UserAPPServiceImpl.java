package com.summit.stp.user.application.service;

import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.service.CaptchaService;
import com.summit.stp.shared.util.EncryptUtil;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.command.UserPasswordUpdateCommand;
import com.summit.stp.user.application.command.UserPhoneBindCommand;
import com.summit.stp.user.application.command.UserProfileUpdateCommand;
import com.summit.stp.user.application.vo.UserProfileVO;
import com.summit.stp.user.application.vo.UserSimpleVO;
import com.summit.stp.user.domain.model.Email;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.shared.constant.MemberConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.summit.stp.shared.exception.ParameterException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAPPServiceImpl implements UserApplicationService {
    private final UserRepository userRepository;
    private final UserMemberRepository userMemberRepository;
    private final CaptchaService captchaService;

    @Override
    @Transactional
    public void updateProfile(UserProfileUpdateCommand command) {
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("修改用户基本资料: {}", currentUsername);

        User user = userRepository.findUserByName(currentUsername);
        if (user == null) {
            throw new RuntimeException(String.format("用户 %s 不存在", currentUsername));
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

        user.updateProfile(command.getNick(), command.getAvatar(), newEmail, command.getIntroduce(), command.getVerifyCode(), command.getGender(), command.getAge());
        userRepository.save(user);
        log.info("用户基本资料修改成功: {}", currentUsername);
    }

    @Override
    @Transactional
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
    @Transactional
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
        UserProfileVO user = findUserById(id);
        if (user == null) {
            return null;
        }
        return UserSimpleVO.builder()
                .id(user.getId())
                .nick(user.getNick())
                .avatar(user.getAvatar())
                .vipType(user.getVipType())
                .ip(user.getIp())
                .memberLevel(user.getMemberLevel())
                .vipConfigIcon(user.getVipConfigIcon())
                .build();
    }

    private UserProfileVO convertToVO(User user, UserMember userMember) {
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
                .vipConfigIcon(userMember != null && userMember.getLevel() != null ? userMember.getLevel().getIconUrl() : null)
                .vipExpireDate(vipExpireDate)
                .gender(user.getGender())
                .age(user.getAge())
                .build();
    }
}
