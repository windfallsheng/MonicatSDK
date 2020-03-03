package com.windfallsheng.monicat.util;

import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.model.Param;
import com.windfallsheng.monicat.model.ParamMap;

import java.util.List;
import java.util.Map;

/**
 * CreateDate: 2018/4/25
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 拼接SQL语句的工具类. todo 逻辑判断的范围有限，需要完善
 * <p>
 * Version:
 */
public class SQLUtils {

    /**
     * 遍历ParamMap对象中的所有Map集合，拼接SQL语句块
     *
     * @param conditions
     * @return
     */
    public static String spliceSQL(ParamMap conditions) {
        StringBuffer sbSQL = new StringBuffer();
        String andSQL = SQLUtils.spliceQueryParamMap(conditions);
        sbSQL.append(andSQL);
        String inSQL = SQLUtils.spliceInParamMap(conditions);
        sbSQL.append(inSQL);
        String orderSQL = SQLUtils.spliceOrderParamMap(conditions);
        sbSQL.append(orderSQL);
        return sbSQL.toString();
    }

    /**
     * 遍历ParamMap对象中的所有Map集合，拼接SQL语句块
     *
     * @param conditions
     * @return
     */
    public static String spliceQueryParamMap(ParamMap conditions) {
        List<Param> andList = conditions.getAndList();                  // and 条件的语句
        List<Param> orList = conditions.getOrList();                    // or 条件的语句
        List<Param> andLikeList = conditions.getAndLikeList();          // like 条件的语句
        StringBuffer sbSQL = new StringBuffer();
        if (andList != null) {
            String andSQL = SQLUtils.spliceSQLWhere(andList, MonicatConstants.AND);
            sbSQL.append(andSQL);
        }
        if (orList != null) {
            String orSQL = SQLUtils.spliceSQLWhere(orList, MonicatConstants.OR);
            sbSQL.append(orSQL);
        }
        if (andLikeList != null) {
            String andLikeSQL = SQLUtils.spliceSQLWhere(andLikeList, MonicatConstants.AND_LIKE);
            sbSQL.append(andLikeSQL);
        }
        String sql = sbSQL.toString().trim();// 去掉字符串前后的空字符
        if (sql.toUpperCase().startsWith("OR")) { // 保证拼接出的完善的sql语句块是以AND开头的
            sql = " AND " + sql.substring("OR".length(), sql.length());
        }
        return sql;
    }

    /**
     * 遍历ParamMap对象中的所有Map集合，拼接SQL语句块
     *
     * @param conditions
     * @return
     */
    public static String spliceInParamMap(ParamMap conditions) {
        Map<String, List<Object>> andInMap = conditions.getAndInMap();  // in 条件的语句
        String andInSQL = "";
        if (andInMap != null) {
            andInSQL = SQLUtils.spliceSQLIn(andInMap, MonicatConstants.AND);
        }
        return andInSQL;
    }

    /**
     * @param params In条件的集合
     * @param flag   用来标识是需要拼AND, OR等条件语句块
     * @return
     */
    private static String spliceSQLIn(Map<String, List<Object>> params, int flag) {
        StringBuffer sbSQL = new StringBuffer();
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                List<Object> values = params.get(key);
                if (flag == MonicatConstants.AND) {
                    sbSQL.append(" AND ");
                } else if (flag == MonicatConstants.OR) {
                    sbSQL.append(" OR ");
                }
                sbSQL.append(key).append(" IN( ");
                for (int i = 0; i < values.size(); i++) {
                    Object value = values.get(i);
                    if (value instanceof String) {
                        sbSQL.append("'").append(value).append("'");
                    } else {
                        sbSQL.append(value);
                    }
                    if (i < values.size() - 1) {
                        sbSQL.append(", ");
                    }
                }
                sbSQL.append(" )");
            }
        }
        return sbSQL.toString();
    }

    /**
     * 遍历ParamMap对象中的所有Map集合，拼接SQL语句块
     *
     * @param conditions
     * @return
     */
    public static String spliceOrderParamMap(ParamMap conditions) {
        List<String> orderDescList = conditions.getOrderDescList();     // orderby 条件的语句
        List<String> orderAscList = conditions.getOrderAscList();       // orderby 条件的语句
        String sbSQL = "";
        if (orderDescList != null) {
            sbSQL = SQLUtils.spliceSQLOrder(orderDescList, MonicatConstants.ORDER_BY_DESC);
        } else if (orderAscList != null) {
            sbSQL = SQLUtils.spliceSQLOrder(orderAscList, MonicatConstants.ORDER_BY_ASC);
        }
        return sbSQL;
    }

    /**
     * 遍历ParamMap对象中的所有Map集合，拼接SQL语句块
     *
     * @param conditions
     * @return
     */
    public static String spliceUpdateParamMap(ParamMap conditions) {
        List<Param> updateList = conditions.getUpdateList();             // update 条件的语句
        String sbSQL = "";
        if (updateList != null) {
            sbSQL = SQLUtils.spliceSQLUpdate(updateList);
        }
        return sbSQL;
    }


    /**
     * @param params Update条件的List集合
     * @return
     */
    private static String spliceSQLUpdate(List<Param> params) {
        StringBuffer sbSQL = new StringBuffer();
        if (params != null && params.size() > 0) {
            for (int i = 0; i < params.size(); i++) {
                String key = params.get(i).getKey();
                Object value = params.get(i).getObj();
                sbSQL.append(key + " = ");
                if (value instanceof String) {
                    sbSQL.append("'").append(value).append("'");
                } else {
                    sbSQL.append(value);
                }
                if (i < params.size() - 1) {
                    sbSQL.append(" ,");
                }
            }
        }
        return sbSQL.toString();
    }

    /**
     * @param params WHERE条件的List集合
     * @param flag   用来标识是需要拼AND、OR、LIKE等条件语句块
     * @return
     */

    private static String spliceSQLWhere(List<Param> params, int flag) {
        StringBuffer sbSQL = new StringBuffer();
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                String key = param.getKey();
                Object value = param.getObj();
                if (flag == MonicatConstants.AND) {
                    sbSQL.append(" AND ");
                } else if (flag == MonicatConstants.OR) {
                    sbSQL.append(" OR ");
                } else if (flag == MonicatConstants.AND_LIKE || flag == MonicatConstants.AND_IN) {
                    sbSQL.append(key + " AND ");
                }
                if (flag == MonicatConstants.AND || flag == MonicatConstants.OR) {
                    sbSQL.append(key + " = ");
                    if (value instanceof String) {
                        sbSQL.append("'").append(value).append("'");
                    } else {
                        sbSQL.append(value);
                    }
                } else if (flag == MonicatConstants.AND_LIKE) {
                    sbSQL.append(key + " LIKE ");
                    sbSQL.append("'%").append(value).append("%'");
                } else if (flag == MonicatConstants.AND_IN) {
                    sbSQL.append(key + " IN( ");
                    List<Object> valueObj = (List<Object>) value;
                    StringBuffer sbValue = new StringBuffer();
                    for (int i = 0; i < valueObj.size(); i++) {
                        Object val = valueObj.get(i);
                        if (value instanceof String) {
                            sbValue.append("'").append(value).append("'");
                        } else {
                            sbValue.append(value);
                        }
                        if (i < valueObj.size() - 1) {
                            sbValue.append(" ,");
                        }
                    }
                    String valueStr = sbValue.toString();
                    sbSQL.append(valueStr).append(" )");
                }
            }
            return sbSQL.toString();
        }
        return sbSQL.toString();
    }

    /**
     * @param params ORDERBY条件的List集合
     * @param flag   用来标识是需要拼DESC, ASC等条件语句块
     * @return
     */
    private static String spliceSQLOrder(List<String> params, int flag) {
        StringBuffer sbSQL = new StringBuffer();
        if (params != null && params.size() > 0) {
            sbSQL.append(" ORDER BY ");
            for (int i = 0; i < params.size(); i++) {
                String key = params.get(i);
                sbSQL.append(key);
                if (i < params.size() - 1) {
                    sbSQL.append(" ,");
                }
            }
        }
        if (flag == MonicatConstants.ORDER_BY_DESC) {
            sbSQL.append(" DESC ");
        } else if (flag == MonicatConstants.ORDER_BY_ASC) {
            sbSQL.append(" ASC ");
        }
        return sbSQL.toString();
    }
}
