package com.windfallsheng.monicat.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzsheng on 2018/5/11.
 * <p>
 * 自定义事件Key-Value参数
 */
public class Properties {

    private Map<String, String> mPropertyMaps;

    public Properties() {
    }

    public Properties addProperty(String key, String value) {
        if (mPropertyMaps == null) {
            mPropertyMaps = new HashMap<>();
        }
        mPropertyMaps.put(key, value);
        return this;
    }

    public Properties removeProperty(String key) {
        if (mPropertyMaps != null && mPropertyMaps.containsKey(key)) {
            mPropertyMaps.remove(key);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Properties{" +
                "mPropertyMaps=" + mPropertyMaps +
                '}';
    }
}
