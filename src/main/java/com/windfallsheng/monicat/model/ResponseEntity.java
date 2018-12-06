package com.windfallsheng.monicat.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Author: lzsheng
 * @Description: 网络请求的返回值实体类,
 * 使用新版本Gson的jar包，修改注解 @SerializedName(value = "val", alternate = {"alt1", "alt2"})
 * @Version:
 */
public class ResponseEntity<T> implements Serializable {

    @SerializedName("rt")
    private int rt;
    @SerializedName(value = "msg", alternate = {"message", "result"})
    private String msg;
    private T data;


    public int getRt() {
        return rt;
    }

    public void setRt(int rt) {
        this.rt = rt;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseEntity{" +
                "rt=" + rt +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
