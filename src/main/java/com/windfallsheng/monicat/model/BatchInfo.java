package com.windfallsheng.monicat.model;

public class BatchInfo {

    /**
     *
     */
    private String className;
    /**
     *
     */
    private int count;


    public BatchInfo(String className, int count) {
        this.className = className;
        this.count = count;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "BatchInfo{" +
                "className='" + className + '\'' +
                ", count=" + count +
                '}';
    }
}
