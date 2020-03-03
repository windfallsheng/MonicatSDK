package com.windfallsheng.monicat.listener;

import com.windfallsheng.monicat.model.BatchInfo;

/**
 * Created by lzsheng on 2018/5/17.
 */

public interface BatchDataChangeListener {

    void onBatchDataChanged(BatchInfo batchInfo);
}
