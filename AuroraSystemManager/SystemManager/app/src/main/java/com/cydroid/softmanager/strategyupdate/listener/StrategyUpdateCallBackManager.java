package com.cydroid.softmanager.strategyupdate.listener;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.os.Handler;
/**
 * Created by mengjk on 17-5-16.
 */
public class StrategyUpdateCallBackManager {
    private static final String TAG = StrategyUpdateCallBackManager.class.getSimpleName();
    private volatile static StrategyUpdateCallBackManager mInstance;
    private final List<StrategyUpdateCallBack> mUpdateCallBackList = new ArrayList<StrategyUpdateCallBack>();
    private final Context mContext;

    private StrategyUpdateCallBackManager(Context context) {
        mContext = context.getApplicationContext();
        initCallBack();
    }

    public static synchronized StrategyUpdateCallBackManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (StrategyUpdateCallBackManager.class) {
                mInstance = new StrategyUpdateCallBackManager(context);
            }
        }
        return mInstance;
    }

    private void initCallBack() {
        Log.d(TAG, "initCallBack");
        addCallBack(new AutoBootStrategyCompletedCallBack());// autobootlistener
        addCallBack(new TryUpdateCountCallBack());// try update count
    }

    public synchronized void addCallBack(StrategyUpdateCallBack callBack) {
        Log.d(TAG, "addCallBack");
        if (!mUpdateCallBackList.contains(callBack)) {
            mUpdateCallBackList.add(callBack);
        }
    }

    public synchronized void removeCallBack(StrategyUpdateCallBack callBack) {
        Log.d(TAG, "removeCallBack");
        mUpdateCallBackList.remove(callBack);
    }

    public void updateStart() {
        Log.d(TAG, "updateStart nofity callBackCount-" + mUpdateCallBackList.size());
        for (StrategyUpdateCallBack callBack : mUpdateCallBackList) {
            callBack.notifyUpdateStart(mContext);
        }
    }

    public void updatedStrategy(final String type, final IStrategy strategy) {
        Log.d(TAG, "updatedStrategy nofity callBackCount-" + mUpdateCallBackList.size());
        for (StrategyUpdateCallBack callBack : mUpdateCallBackList) {
            callBack.notifyStrategyParseCompleted(mContext, type, strategy);
        }
    }

    public void updateFail() {
        Log.d(TAG, "updateFail nofity callBackCount-" + mUpdateCallBackList.size());
        for (StrategyUpdateCallBack callBack : mUpdateCallBackList) {
            callBack.notifyUpdateFail(mContext);
        }
    }

    public void updateCompleted() {
        Log.d(TAG, "updateCompleted nofity callBackCount-" + mUpdateCallBackList.size());
        for (StrategyUpdateCallBack callBack : mUpdateCallBackList) {
            callBack.notifyUpdateCompleted(mContext);
        }
    }

    public synchronized void deInit() {
        Log.d(TAG, "deInit");
        mInstance = null;
        mUpdateCallBackList.clear();
    }

}
