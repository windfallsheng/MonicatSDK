package com.windfallsheng.monicat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CreateDate: 2018/4/25
 * Author: lzsheng
 * Description: 每个List集合中Param对象的Key为要修改的数据库字段名，Value为要修改的值
 * 不同的条件语句放在不同的集合当中
 * Version:
 */
public class ParamMap {

    private List<Param> andList;            // and 条件的语句
    private List<Param> orList;             // or 条件的语句
    private List<Param> andLikeList;        // like 条件的语句
    private Map<String, List<Object>> andInMap;          // in 条件的语句
    private List<String> orderDescList;     // orderby 条件的语句
    private List<String> orderAscList;      // orderby 条件的语句
    private List<Param> updateList;        // update 条件的语句

    public ParamMap() {
    }

    public List<Param> getAndList() {
        return andList;
    }

    public List<Param> getOrList() {
        return orList;
    }

    public List<Param> getAndLikeList() {
        return andLikeList;
    }

    public Map<String, List<Object>> getAndInMap() {
        return andInMap;
    }

    public List<String> getOrderDescList() {
        return orderDescList;
    }

    public List<String> getOrderAscList() {
        return orderAscList;
    }

    public List<Param> getUpdateList() {
        return updateList;
    }

    /**
     * AND 条件的参数
     *
     * @param key   数据库字段名称
     * @param value 对应的值
     * @return
     */
    public ParamMap setAndMap(String key, Object value) {
        if (andList == null) {
            andList = new ArrayList<>();
        }
        andList.add(new Param(key, value));
        return this;
    }

    /**
     * OR 条件的参数
     *
     * @param key   数据库字段名称
     * @param value 对应的值
     * @return
     */
    public ParamMap setOrMap(String key, Object value) {
        if (orList == null) {
            orList = new ArrayList<>();
        }
        orList.add(new Param(key, value));
        return this;
    }

    /**
     * AndLike 条件的参数
     *
     * @param key   数据库字段名称
     * @param value 对应的值
     * @return
     */
    public ParamMap setAndLikeMap(String key, Object value) {
        if (andLikeList == null) {
            andLikeList = new ArrayList<>();
        }
        andLikeList.add(new Param(key, value));
        return this;
    }

    /**
     * In 条件的参数
     *
     * @param key   数据库字段名称
     * @param value 对应的值
     * @return
     */
    public ParamMap setAndInMap(String key, Object value) {
        if (andInMap == null) {
            andInMap = new HashMap<>();
        }
        List<Object> vals = andInMap.get(key);
        if (vals == null) {
            vals = new ArrayList<>();
        }
        if (value instanceof List) {
            vals.addAll((List) value);
        } else {
            vals.add(value);
        }
        andInMap.put(key, vals);
        return this;
    }

    /**
     * order 条件的参数
     *
     * @param key 数据库字段名称
     * @return
     */
    public ParamMap setOrderDescMap(String key) {
        if (orderDescList == null) {
            orderDescList = new ArrayList<>();
        }
        orderDescList.add(key);
        return this;
    }

    /**
     * order 条件的参数
     *
     * @param key 数据库字段名称
     * @return
     */
    public ParamMap setOrderAscMap(String key) {
        if (orderAscList == null) {
            orderAscList = new ArrayList<>();
        }
        orderAscList.add(key);
        return this;
    }

    /**
     * update 条件的参数
     *
     * @param key   数据库字段名称
     * @param value 对应的值
     * @return
     */
    public ParamMap setUpdateMap(String key, Object value) {
        if (updateList == null) {
            updateList = new ArrayList<>();
        }
        updateList.add(new Param(key, value));
        return this;
    }

    @Override
    public String toString() {
        return "ParamMap{" +
                "andList=" + andList +
                ", orList=" + orList +
                ", andLikeList=" + andLikeList +
                ", andInList=" + andInMap +
                ", orderDescList=" + orderDescList +
                ", orderAscList=" + orderAscList +
                ", updateList=" + updateList +
                '}';
    }
}
