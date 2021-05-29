package com.cydroid.framework;

import java.util.List;

import android.content.Context;

import com.cydroid.framework.provider.SIMInfo;

public interface ISIMInfo {

    List<SIMInfo> getInsertedSIMList(Context ctx);

    List<SIMInfo> getAllSIMList(Context ctx);

    SIMInfo getSIMInfoById(Context ctx, long SIMId);

    SIMInfo getSIMInfoByName(Context ctx, String SIMName);

    SIMInfo getSIMInfoBySlot(Context ctx, int cardSlot);

    SIMInfo getSIMInfoByICCId(Context ctx, String iccid);

    int getSlotById(Context ctx, long SimId);

    int getSlotByName(Context ctx, String SIMName);

    int getInsertedSIMCount(Context ctx);

    int getAllSIMCount(Context ctx);
    
    int getDefaultDataSubId();
}
