package com.windfallsheng.monicat.action;

import com.windfallsheng.monicat.base.UploadStrategy;
import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.UploadDataObserver;
import com.windfallsheng.monicat.model.BatchInfo;
import com.windfallsheng.monicat.util.LogUtils;

/**
 * @author lzsheng
 */
abstract class BaseStatisticsManager implements UploadDataObserver {

    private UploadStrategy mUploadStrategy;
    private BatchDataChangeListener mBatchDataChangeListener;
    /**
     * 数据库缓存中是否还有数据；
     */
    private boolean hasDataInCache = true;

    public void setBatchDataChangeListener(BatchDataChangeListener batchDataChangeListener) {
        mBatchDataChangeListener = batchDataChangeListener;
    }

    /**
     * 处理批量上传策略和即时上传这两种特殊策略的情况；
     */
    protected void handleStatisticsByStrategy() {
        if (mUploadStrategy == null) {
            mUploadStrategy = MonicatManager.getInstance().getUploadStrategy();
        }
        if (mUploadStrategy == UploadStrategy.BATCH) {
            int batchCount = 0;
            if (hasDataInCache) {
                // 如果数据库中还有数据，需要查询总数；
                int totalCount = queryCacheTotalCount();
                if (totalCount > 0) {
                    batchCount += totalCount;
                }
                // 查询过后就可以设置为false;
                hasDataInCache = false;
            }
            // 新增一条数据成功时，传递参数值增加1；
            batchCount += 1;
            BatchInfo batchInfo = newBatchInfo(batchCount);
            if (mBatchDataChangeListener != null) {
                mBatchDataChangeListener.onBatchDataChanged(batchInfo);
            }
            return;
        }
        if (mUploadStrategy == UploadStrategy.INSTANT) {
            LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->saveEventInfo()_INSTANT");
            uploadCacheData();
        }
    }

    /**
     * 查询表中的缓存总数；
     */
    abstract int queryCacheTotalCount();

    /**
     * 构造各自的BatchInfo对象；
     */
    abstract BatchInfo newBatchInfo(int count);

    /**
     * 上传数据，如果数据没有上传成功，需要修改{@link this#hasDataInCache}为false，上传成功删除缓存数据;
     */
    abstract void uploadCacheData();

}
