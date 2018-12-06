package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/23.
 */

public class Param {
    private String key;
    private Object obj;

    public Param(String key, Object obj) {
        this.key = key;
        this.obj = obj;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public String toString() {
        return "Param{" +
                "key='" + key + '\'' +
                ", obj=" + obj +
                '}';
    }
}
