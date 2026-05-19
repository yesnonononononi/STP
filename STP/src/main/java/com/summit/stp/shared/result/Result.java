package com.summit.stp.shared.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private int code;
    private T data;
    private String errMsg;
    private String signature;

    public Result(int code, T data, String errMsg, String signature) {
        this.code = code;
        this.data = data;
        this.errMsg = errMsg;
        this.signature = signature;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(1, data, null, null);
    }

    public static Result<Void> success() {
        return new Result<>(1, null, null, null);
    }

    public static <T>Result<T> successWithSignature(T data, String signature){
        return new Result<>(1, data, null, signature);
    }

    public static Result<Void> error(String errMsg) {
        return new Result<>(0, null, errMsg, null);
    }


    @JsonIgnore
    public boolean isSuccess() {
        return code == 1;
    }
}
