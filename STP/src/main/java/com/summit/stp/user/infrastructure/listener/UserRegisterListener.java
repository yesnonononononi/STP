package com.summit.stp.user.infrastructure.listener;

import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.user.domain.exception.UserExistException;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.userAuth.domain.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class UserRegisterListener {
  private final UserRepository userRepository;


  /**
   * 用户注册事件监听
   * 
   * @param event 用户注册事件
   */
  @EventListener
  public void handleUserRegisterEvent(UserRegisterEvent event) {
    try {
      // 使用事件传来的唯一用户名
      Username username = Username.of(event.getUsername());
      // 使用哈希值还原密码对象（安全且满足构造要求）
      Password password = Password.fromHash(event.getPasswordHash());
      PhoneNumber phoneNumber = PhoneNumber.of(event.getPhoneNumber());

      User user = User.create(username, password, phoneNumber);
      userRepository.save(user);
    } catch (DuplicateKeyException e) {
      throw new UserExistException();
    }
  }
}
