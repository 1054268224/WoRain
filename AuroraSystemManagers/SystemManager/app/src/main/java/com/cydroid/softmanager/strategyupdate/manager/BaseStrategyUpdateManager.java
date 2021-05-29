package com.cydroid.softmanager.strategyupdate.manager;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.cydroid.softmanager.strategyupdate.listener.StrategyUpdateCallBackManager;
import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;

import android.content.Context;
import com.cydroid.softmanager.utils.Log;

/**
 * Created by mengjk on 17-5-17.
 */
public abstract class BaseStrategyUpdateManager {
    private static final String TAG = BaseStrategyUpdateManager.class.getSimpleName();
    // only accept class of AbstractUpdateStrategy or those class which extends AbstractUpdateStrategy
    protected final Map<String, Class<? extends IStrategy>> strategyClassMap = new HashMap<String, Class<? extends IStrategy>>();
    protected final Map<String, SoftReference<IStrategy>> strategyMap = new HashMap<String, SoftReference<IStrategy>>();

    private final Context mContext;
    public BaseStrategyUpdateManager(Context context) {
        Log.d(TAG, "getSimpleName super invoke");
        this.mContext=context;
    }

    public IStrategy createStrategy(String type) {
        if (strategyClassMap.containsKey(type)) {
            if (!strategyMap.containsKey(type)) {
                Log.d(TAG, "type =" + type + " strategyMap not init this type! so create It");
                Class<?> updateStrategyclass = strategyClassMap.get(type);
                return createUpdateStrategy(type, updateStrategyclass);
            }
            IStrategy updateStrategy = strategyMap.get(type).get();
            if (updateStrategy != null) {
                Log.d(TAG, "type =" + type + " strategyMap inner Strategy have been cache return It!");
                return updateStrategy;
            } else {
                Log.d(TAG, "type =" + type
                        + " strategyMap inner  Strategy have been recycle! need to recreate it!");
                strategyMap.remove(type);
                Class<?> updateStrategyclass = strategyClassMap.get(type);
                return createUpdateStrategy(type, updateStrategyclass);
            }
        } else {
            Log.d(TAG, "type =" + type
                    + " this type have not been support yet please add class to the strategyClassMap!");
            return null;
        }
    }

    private IStrategy createUpdateStrategy(String type, Class<?> updateStrategyclass) {
        IStrategy updateStrategy = null;
        try {
            Constructor<?> con = updateStrategyclass.getConstructor();
            updateStrategy = (IStrategy) con.newInstance();
            if (!strategyMap.containsKey(type)) {
                SoftReference<IStrategy> soft = new SoftReference<IStrategy>(updateStrategy);
                strategyMap.put(type, soft);
            }
        } catch (Exception e) {
            Log.e(TAG, "createUpdateStrategy exception:" + e.toString());
        }
        return updateStrategy;
    }

    // if your factory work have done ,invoke this method to release cache
    public void clearStrategyCache() {
        Log.d(TAG, "clearStrategyCache");
        strategyMap.clear();
    }
    
    public String getStrategyRequestBody(Context context) {
        JSONArray array = new JSONArray();
        try {
            for (String key : strategyClassMap.keySet()) {
                IStrategy strategy = (IStrategy) createStrategy(key);
                if (strategy != null) {
                    JSONObject requestBody = strategy.getStrategyRequestBody(context);
                    array.put(requestBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return array.toString();
    }
    
    public boolean parse(String data) {
        boolean success=false;
        Log.d(TAG, "parse:" + data);
        try {
            JSONTokener tokener = new JSONTokener(data);
            Object obj = tokener.nextValue();
            if (obj instanceof JSONArray) {
                JSONArray array = (JSONArray) obj;
                parseArrayData(array);
                success=true;
            } else if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                success=parseSingleData(jsonObj, 0);
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

    private void parseArrayData(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                parseSingleData(array.getJSONObject(i), i);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "array " + array.toString() + " item parse error! index=" + i + e.toString());
            }
        }
    }

    private boolean parseSingleData(JSONObject item, int index) {
        if (item != null) {
           if (item.has("t")) {// correct data
                try {
                    String type = item.getString("t");
                    String json = item.toString();
                    IStrategy strategy = createStrategy(type);
                    if (strategy != null) {
                        strategy.parseJson(mContext, json, null);
                    }
                    StrategyUpdateCallBackManager.getInstance(mContext).updatedStrategy(type, strategy);
                } catch (Exception e) {
                    Log.e(TAG,
                            "array " + item.toString() + " item parse error! index=" + index + e.toString());
                }
                return true;
            }else  if (item.has("r")) {
                Log.e(TAG, "--result data error" + " in " + index + "  data:" + item.toString());
                return false;
            }
        }
        return false;
    }

    private void parseOtherData(Object obj) {
        Log.e(TAG, "unknow data" + obj == null ? "null" : obj.toString());
    }

}
