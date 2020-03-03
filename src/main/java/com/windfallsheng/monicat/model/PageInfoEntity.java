package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class PageInfoEntity {

    /**
     * 本地数据库存储的ID；
     */
    private int pageInfoId;
    /**
     * 页面的唯一标识，也即页面的全路径名称；
     */
    private String className;
    /**
     * 自定义的页面名称，即标注名；
     */
    private String labelName;
    /**
     * 触发时间；
     */
    private long triggeringTime;
    /**
     * 打开或者关闭状态；
     */
    private int pageStatus;
    /**
     * 数据是否已上传到后台；
     */
    private int uploadeStatus;

    public PageInfoEntity(String className, String pageName, long triggeringTime,
                          @PageStatus int pageStatus, @UploadeStatus int uploadeStatus) {
        this.className = className;
        this.labelName = pageName;
        this.triggeringTime = triggeringTime;
        this.pageStatus = pageStatus;
        this.uploadeStatus = uploadeStatus;
    }

    @Override
    public String toString() {
        return "PageInfoEntity{" +
                "pageInfoId=" + pageInfoId +
                ", className='" + className + '\'' +
                ", remarkName='" + labelName + '\'' +
                ", triggeringTime=" + triggeringTime +
                ", pageStatus=" + pageStatus +
                ", uploadeStatus=" + uploadeStatus +
                '}';
    }
}
