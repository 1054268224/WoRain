package com.cydroid.softmanager.strategyupdate.manager;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.cydroid.softmanager.strategyupdate.listener.StrategyUpdateCallBackManager;
import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.SafeListUpdateStrategy;

import android.content.Context;
import com.cydroid.softmanager.utils.Log;
/**
 * Created by mengjk on 17-5-16.
 */
public class SafeListStrategyUpdateManager extends BaseStrategyUpdateManager {
    private static final String TAG=SafeListStrategyUpdateManager.class.getSimpleName();
    private final Context mContext;
    public SafeListStrategyUpdateManager(Context context) {
        super(context.getApplicationContext());
        this.mContext=context.getApplicationContext();
        addUpdateStrategy();
    }
    private void addUpdateStrategy() {
        strategyClassMap.put(SafeListUpdateStrategy.TYPE , SafeListUpdateStrategy.class);
    }
    
    @Override
    public String getStrategyRequestBody(Context context) {
        IStrategy strategy = (IStrategy) createStrategy(SafeListUpdateStrategy.TYPE);
        if (strategy != null) {
            JSONObject requestBody = strategy.getStrategyRequestBody(context);
            return requestBody.toString();
        }
        return "";
    }
    @Override
    public boolean parse(String data) {
        boolean success=false;
        Log.d(TAG, "parse:" + data);
        try {
            JSONTokener tokener = new JSONTokener(data);
            Object obj = tokener.nextValue();
            if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                success=parseData(jsonObj);
            } else {
                parseOtherData(obj);
                success=false;
            }
        } catch (Exception e) {
            Log.e(TAG, "parse error!" + e.toString());
            success=false;
        }
        clearStrategyCache();
        return success;
    }

    private boolean parseData(JSONObject item) {
        if (item != null) {
           if (item.has("r")) {
                Log.e(TAG, "--result data error data:" + item.toString());
                return false;
            }else{
                try {
                    String json = item.toString();
                    IStrategy strategy = createStrategy(SafeListUpdateStrategy.TYPE);
                    if (strategy != null) {
                        strategy.parseJson(mContext, json, null);
                    }
                    StrategyUpdateCallBackManager.getInstance(mContext).updatedStrategy(SafeListUpdateStrategy.TYPE, strategy);
                } catch (Exception e) {
                    Log.e(TAG," parse error! index=" + e.toString());
                }
                return true;
            }
        }
        return false;
    }

    private void parseOtherData(Object obj) {
        Log.e(TAG, "unknow data" + obj == null ? "null" : obj.toString());
    }


}
