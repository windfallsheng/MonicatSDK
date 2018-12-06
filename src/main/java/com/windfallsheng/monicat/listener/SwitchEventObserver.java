package com.windfallsheng.monicat.listener;

import com.windfallsheng.monicat.model.SwitchEvent;


/**
 * CreateDate: 2018/4/9
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 应用前后台切换的监听
 * <p>
 * Version:
 */
public interface SwitchEventObserver {

    /**
     * 应用前后台切换变化
     *
     * @param switchEvent
     */
    void switchEventChanged(SwitchEvent switchEvent);

}
