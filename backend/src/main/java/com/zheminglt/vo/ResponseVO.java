package com.zheminglt.vo;

import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;

public class ResponseVO<T> {
    private Integer code;
    private String message;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(ErrorCodeConstant.CODE_SUCCESS);
        response.setMessage(MessageConstant.SUCCESS);
        response.setData(data);
        return response;
    }

    public static <T> ResponseVO<T> error(int code, String message) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
