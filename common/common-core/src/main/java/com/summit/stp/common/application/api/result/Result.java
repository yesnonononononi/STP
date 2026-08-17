package com.summit.stp.common.application.api.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private int code;
    private T data;
    private String errMsg;
    private String signature;

    public static <T> Result<T> success(T data) {
        return new Result<>(1, data, null, null);
    }

    public static Result<Void> success() {
        return new Result<>(1, null, null, null);
    }

    public static <T>Result<T> successWithSignature(T data, String signature){
        return new Result<>(1, data, null, signature);
    }

    public static <T> Result<T> error(String errMsg) {
        return new Result<>(0, null, errMsg, null);
    }

    public static <T> Result<T> error(int code, String errMsg) {
        return new Result<>(code, null, errMsg, null);
    }


    @JsonIgnore
    public boolean isSuccess() {
        return code == 1;
    }
}
