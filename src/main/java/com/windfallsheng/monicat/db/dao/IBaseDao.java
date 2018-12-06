package com.windfallsheng.monicat.db.dao;

import com.windfallsheng.monicat.model.ParamMap;

import java.io.Serializable;
import java.util.List;


/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 数据连接层：操作数据库表的BaseDao类
 * <p>
 * Version:
 */
public interface IBaseDao<T> {

    /**
     * 增加
     *
     * @param entity
     * @return 返回插入的数据的主键ID
     */
    public int save(T entity);

    /**
     * 查找所有,可根据条件进行查询
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    public List<T> queryAllByMap(ParamMap conditions);

    /**
     * 根据id查找
     *
     * @param id
     */
    public T queryById(Serializable id);

    /**
     * 查找Count值
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    public int queryCountByMap(ParamMap conditions);

    /**
     * 查询某条数据的上一条或者下一条数据，可根据条件进行查询
     *
     * @param id
     * @param conditions 查询的条件，没有条件查询的需求时可传null
     * @param lastOrNext 标识符，判断是获取上一条还是下一条数据的依据
     * @return
     */
    public T queryAdjacentData(Serializable id, ParamMap conditions, int lastOrNext);

    /**
     * 修改,可根据条件进行
     *
     * @param conditions 查询的条件，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    public void updateByMap(ParamMap conditions);


    /**
     * 修改
     *
     * @param entity
     */
    public void update(T entity);

    /**
     * 删除所有,可根据条件进行删除
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    public void deleteByMap(ParamMap conditions);

    /**
     * 根据id删除
     *
     * @param id
     */
    public void deleteById(Serializable id);
}
