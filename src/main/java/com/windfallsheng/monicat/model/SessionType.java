package com.windfallsheng.monicat.model;

import android.support.annotation.IntDef;

import com.windfallsheng.monicat.common.MonicatConstants;

@IntDef({MonicatConstants.APP_LAUNCH, MonicatConstants.APP_RESTART,
        MonicatConstants.APP_BACKGROUND, MonicatConstants.APP_EXIT})
@interface SessionType {
}
