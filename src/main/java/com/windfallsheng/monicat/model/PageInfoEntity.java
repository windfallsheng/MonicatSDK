package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class PageInfoEntity {

    private int pageInfoId;      // 本地数据库存储的ID
    private String className;    // 页面的ID，也即页面所在Activity的全路径
    private String pageName;     // 自定义的页面名称，即备注名
    private long occurrenceTime; // 发生T时间
    private int openOrClose;     // 打开或者关闭标识符

    public PageInfoEntity(String className, String pageName, long occurrenceTime, int openOrClose) {
        this.className = className;
        this.pageName = pageName;
        this.occurrenceTime = occurrenceTime;
        this.openOrClose = openOrClose;
    }

    @Override
    public String toString() {
        return "PageInfoEntity{" +
                "pageInfoId=" + pageInfoId +
                ", className='" + className + '\'' +
                ", pageName='" + pageName + '\'' +
                ", occurrenceTime=" + occurrenceTime +
                ", openOrClose=" + openOrClose +
                '}';
    }
}
