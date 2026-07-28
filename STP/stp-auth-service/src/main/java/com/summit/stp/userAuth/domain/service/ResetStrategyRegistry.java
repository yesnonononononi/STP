package com.summit.stp.userAuth.domain.service;

import com.summit.stp.userAuth.domain.exception.ResetPasswordException;
import com.summit.stp.userAuth.domain.model.ResetType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResetStrategyRegistry {
    private final List<ResetPasswordStrategy> strategies;
    private final Map<ResetType,ResetPasswordStrategy> resetStrategyMap = new HashMap<>();

    @PostConstruct
    public void init(){
        strategies.forEach(resetStrategy -> resetStrategyMap.put(resetStrategy.getStrategy(),resetStrategy));
    }


    /**
     * 获取对应的找回密码策略
     * @param resetType 类型
     */
    public ResetPasswordStrategy getResetStrategy(ResetType resetType){
        ResetPasswordStrategy resetPasswordStrategy = resetStrategyMap.get(resetType);
        if(resetPasswordStrategy == null){
            throw new ResetPasswordException("未找到对应的找回密码策略");
        }
        return resetPasswordStrategy;
    }
}
