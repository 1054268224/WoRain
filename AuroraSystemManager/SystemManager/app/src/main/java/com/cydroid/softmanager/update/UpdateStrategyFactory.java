/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.update;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.utils.Log;

public abstract class UpdateStrategyFactory {
    private static final String TAG = "UpdateStrategyFactory";
    
    protected final List<Class<?>> mUpdateStrategyList = new ArrayList<Class<?>>();
    
    public int getUpdateStrategyCount() {
        return mUpdateStrategyList.size();
    }

    public UpdateStrategy createUpdateStrategy(int index) {
        Class<?> updateStrategyclass = mUpdateStrategyList.get(index);
        if (updateStrategyclass != null) {
            return createUpdateStrategy(updateStrategyclass);
        }
        return null;
    }

    private UpdateStrategy createUpdateStrategy(Class<?> updateStrategyclass) {
        UpdateStrategy updateStrategy = null;
        try {
            Constructor con = updateStrategyclass.getConstructor();
            updateStrategy = (UpdateStrategy) con.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "createUpdateStrategy exception:" + e.toString());
        }
        return updateStrategy;
    }
}
