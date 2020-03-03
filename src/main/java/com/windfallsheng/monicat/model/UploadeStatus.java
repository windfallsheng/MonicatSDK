package com.windfallsheng.monicat.model;

import android.support.annotation.IntDef;

import com.windfallsheng.monicat.common.MonicatConstants;

@IntDef({MonicatConstants.UPLOADABLE, MonicatConstants.UPLOADED,
        MonicatConstants.UPLOAD_FAILED})
@interface UploadeStatus {
}
