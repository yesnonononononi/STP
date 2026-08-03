package com.summit.stp.entertainment.domain.model;

import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.entertainment.infrastructure.constants.EntertainmentConstants;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class Emoji {
    private final Long id;
    private final Long packageId;
    private String tiny;
    private Type type;
    private String name;
    private  String url;
    private Timestamp createTime;
    private Timestamp updateTime;

    public enum Type{
        IMAGE(1),
        GIF(2);

        private final int value;
        Type(int value){
            this.value = value;
        }
        public Integer getVal(){
            return this.value;
        }
        public static Type fromCode(Integer code){
            for(Type type : Type.values()){
                if(type.value == code){
                    return type;
                }
            }
            return null;
        }
    }
    public void updateName(String newName){
        if(newName == null || newName.length() > EntertainmentConstants.Business.MAX_NAME_LENGTH){
            throw new BusinessException("表情名长度不能大于" + EntertainmentConstants.Business.MAX_NAME_LENGTH);
        }
        this.name = newName;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public void updateUrl(String newUrl){
        this.url = newUrl;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }


    public void updateTiny(String newTiny){
        this.tiny = newTiny;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }
}
