package com.cydroid.softmanager.powersaver.interfaces;

import com.cydroid.softmanager.powersaver.mode.ModeItemInfo;

interface IPowerService { 
        float getModeConfigWeight(in int mode);
        float getModeDefaultWeight(in int mode);
        List<ModeItemInfo> getConfigList(in int mode);
        void setConfigList(in int mode,in List<ModeItemInfo> configList);
        boolean isConfigDiffFromDefault(in int mode);
}