package com.windfallsheng.monicat.net;

/**
 * CreateDate: 2018/4/23
 * Author: lzsheng
 * Description: 进度条监听器
 * Version:
 */
public interface ProgressListener {
    /**
     * 显示进度
     *
     * @param mProgress
     */
    public void onProgress(int mProgress, long contentSize);

    /**
     * 完成状态
     *
     * @param totalSize
     */
    public void onDone(long totalSize);
}
