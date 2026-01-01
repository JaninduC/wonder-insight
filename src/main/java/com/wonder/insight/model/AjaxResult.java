package com.wonder.insight.model;


import com.wonder.insight.constans.HttpStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author Lion Li
 */
@Data
@NoArgsConstructor
public class AjaxResult<T> implements Serializable {

    /**
     * 成功
     */
    public static final int SUCCESS = 200;
    /**
     * 失败
     */
    public static final int FAIL = 500;
    //    @Serial
    private static final long serialVersionUID = 1L;
    private int code;

    private String msg;

    private T data;

    public static <T> AjaxResult<T> ok() {
        return restResult(null, SUCCESS, "system.success");
    }

    public static <T> AjaxResult<T> ok(T data) {
        return restResult(data, SUCCESS, "system.success");
    }

    public static <T> AjaxResult<T> ok(String msg) {
        return restResult(null, SUCCESS, msg);
    }

    public static <T> AjaxResult<T> ok(String msg, T data) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> AjaxResult<T> fail() {
        return restResult(null, FAIL, "system.fail");
    }

    public static <T> AjaxResult<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> AjaxResult<T> fail(T data) {
        return restResult(data, FAIL, "system.fail");
    }

    public static <T> AjaxResult<T> fail(String msg, T data) {
        return restResult(data, FAIL, msg);
    }

    public static <T> AjaxResult<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> AjaxResult<T> warn(String msg) {
        return restResult(null, HttpStatus.WARN, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> AjaxResult<T> warn(String msg, T data) {
        return restResult(data, HttpStatus.WARN, msg);
    }

    private static <T> AjaxResult<T> restResult(T data, int code, String msg) {
        AjaxResult<T> r = new AjaxResult<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    public static <T> Boolean isError(AjaxResult<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(AjaxResult<T> ret) {
        return AjaxResult.SUCCESS == ret.getCode();
    }
}
