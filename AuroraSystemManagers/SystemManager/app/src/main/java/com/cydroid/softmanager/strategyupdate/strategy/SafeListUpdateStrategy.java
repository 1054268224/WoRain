// Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
package com.cydroid.softmanager.strategyupdate.strategy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.content.ContentValues;
import android.provider.Settings;
import android.widget.Toast;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cydroid.softmanager.R;

public class SafeListUpdateStrategy implements IStrategy {
    private static final int SERVER_STATUS_SAME_LIST = 11111;

    private static final String BROADCAST_INSTALLED_SAFELIST_ADDED = "com.gionee.detection.applist.update.action";

    private static final String BROADCAST_SAFELIST_UPDATED = "com.cydroid.softmanager.update.SAFELIST_UPDATED";

    private static final String TAG = "SafeListUpdateStrategy";

    private static final String SAFELIST_VERSION_TAG = "safelist_data_version";

    public static final String TYPE = "safelist";

    private static final String SAFELIST_TEST = "safelist_update_test";

    private final HashMap<String, Integer> mPkgMap = new HashMap<String, Integer>();

    private String mAVLRulesVersion = "";


    private boolean isTestMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(), SAFELIST_TEST, 0) == 1;
    }

    @Override
    public void setUpdateSuccess(Context context) {
        Log.d(TAG, "get SafeList list successful!");
    }
    
    @Override
    public void parseJson(Context context, String json, ParseStrategyCallBack callBack) {
        mPkgMap.clear();
        try {
            JSONObject jsonObj = new JSONObject(json);
            // updateVersion(context, jsonObj);
            Log.d(TAG, "response data:" + jsonObj.toString());
            if (updateVersion(context, jsonObj) == SERVER_STATUS_SAME_LIST) {
                return;
            }

            JSONArray dataArray = jsonObj.getJSONArray("d");
            if (dataArray == null) {
                Log.d(TAG, "can not parse key = d from json data!!!");
                return;
            }
            for (int j = 0; j < dataArray.length(); j++) {
                JSONObject dataObj = dataArray.getJSONObject(j);
                if (dataObj == null) {
                    continue;
                }
                String pkgName = dataObj.getString("p");
                int pkgStatus = dataObj.getInt("t");
                mPkgMap.put(pkgName, pkgStatus);
            }
            Log.d(TAG, "get list size=" + mPkgMap.size());
            if (mPkgMap.isEmpty()) {
                Log.d(TAG, "empty list from aora store,cancel update");
                return;
            }
            ArrayList<String> pkgList = new ArrayList<String>();
            pkgList.addAll(mPkgMap.keySet());
            if (isTestMode(context)) {
                Log.d(TAG, "TESTMODE on,skip AVL update");
                mAVLRulesVersion = "20170401.test.abc";
            } else {
                mAVLRulesVersion = updateAVLRules(context, pkgList);
                Log.d(TAG, "mAVLRulesVersion="+mAVLRulesVersion);
                if (mAVLRulesVersion.isEmpty()) {
                    Log.d(TAG, "AVL update failed,cancel update");
                    return;
                }
            }
            notifyUpdateSuccess(context);
            HashMap<String, Integer> diffMap = getSafeListDiffStatusMap(
                    HelperUtils.getSafedListMap(context, HelperUtils.SAFED_LIST_FLAG_GET_ALL), mPkgMap);
            if (diffMap.isEmpty()) {
                Log.d(TAG, "safelist same as last update ,no need save to DB");
                return;
            }
            ArrayList<String> oldPkgList = new ArrayList<String>();
            for (String pkg : HelperUtils.getSafedList(context, HelperUtils.SAFED_LIST_FLAG_GET_ALL)) {
                oldPkgList.add(pkg);
            }
            ArrayList<String> diffPkgList = getSafeListDiffCountArray(oldPkgList, pkgList);
            updateLocalProviderDatabase(context, mPkgMap);
            notifySafedListUpdated(context, diffMap);
            notifySafedListUpdatedToSecurityDetection(context, diffPkgList);
        } catch (Exception e) {
            Log.d(TAG, "can not parse safelist json e=" + e.toString() + "\n" + e.getMessage() + "\n");
            for (StackTraceElement ele : e.getStackTrace()) {
                Log.d(TAG, ele.toString());
            }
        }
    }
    
    private void notifyUpdateSuccess(final Context context){
        Log.d(TAG, "notifyUpdateSuccess mAVLRulesVersion="+mAVLRulesVersion);
        //notify
        if (!mAVLRulesVersion.isEmpty()) {
            Handler handler=new Handler(context.getMainLooper());
            handler.post(new Runnable() {
                
                @Override
                public void run() {
                    Toast.makeText(context,
                            context.getString(R.string.security_safed_list_updated_toast, mAVLRulesVersion),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private int updateVersion(Context context, JSONObject jsonObj) {
        try {
            int version = jsonObj.getInt("ver");
            if (version != SERVER_STATUS_SAME_LIST) {
                Log.d(TAG, "safelist server report no update");
                Settings.System.putInt(context.getContentResolver(), SAFELIST_VERSION_TAG, version);
            }

            return version;
        } catch (Exception e) {
            Log.d(TAG, "can not parse key = ver from json data!!!");
            return 0;
        }
    }

    private HashMap<String, Integer> getSafeListDiffStatusMap(HashMap<String, Integer> oldMap,
            HashMap<String, Integer> newMap) {
        HashMap<String, Integer> resMap = new HashMap<String, Integer>();
        for (Entry<String, Integer> entry : oldMap.entrySet()) {
            String oldPkgname = entry.getKey();
            int oldPkgStatus = entry.getValue();
            if (!newMap.containsKey(oldPkgname)) {
                resMap.put(oldPkgname, -oldPkgStatus);
            }
        }
        for (Entry<String, Integer> entry : newMap.entrySet()) {
            String newPkgName = entry.getKey();
            int newPkgStatus = entry.getValue();
            if (!oldMap.containsKey(newPkgName)) {
                resMap.put(newPkgName, newPkgStatus);
            } else if (!oldMap.get(newPkgName).equals(newPkgStatus)) {
                resMap.put(newPkgName, newPkgStatus);
            }
        }
        return resMap;
    }

    private void notifySafedListUpdated(Context context, HashMap<String, Integer> diffMap) {
        if (diffMap.isEmpty()) {
            Log.d(TAG, "same safe list map ,do not notify");
            return;
        }
        HelperUtils.dumpMap(TAG, "safelist diff map", diffMap);
//        ArrayList<String> installedDiffPkgList = new ArrayList<String>();
//        ArrayList<Integer> diffPkgStatusList = new ArrayList<Integer>();
//        for (Entry<String, Integer> diffMapEntry : diffMap.entrySet()) {
//            installedDiffPkgList.add(diffMapEntry.getKey());
//            diffPkgStatusList.add(diffMapEntry.getValue());
//        }
        Intent toLauncherBdIntent = new Intent(BROADCAST_SAFELIST_UPDATED);
//        toLauncherBdIntent.putStringArrayListExtra("pkgnames", installedDiffPkgList);
//        toLauncherBdIntent.putIntegerArrayListExtra("statuses", diffPkgStatusList);

        // just send broadcast to cyee luncher with out any extra data
        context.sendBroadcast(toLauncherBdIntent);
    }

    private ArrayList<String> getSafeListDiffCountArray(ArrayList<String> oldPkgList,
            ArrayList<String> newPkgList) {
        ArrayList<String> diffPkgList = (ArrayList<String>) newPkgList.clone();
        diffPkgList.removeAll(oldPkgList);
        Log.d(TAG, "oldlist size:" + oldPkgList.size() + " difflist size:" + diffPkgList.size());
        return diffPkgList;
    }

    private void notifySafedListUpdatedToSecurityDetection(Context context, ArrayList<String> diffPkgNames) {
        if (diffPkgNames.isEmpty()) {
            Log.d(TAG, "same safe list ,do not notify");
            return;
        }
        ArrayList<String> installedDiffPkgList = new ArrayList<String>();
        for (String pkg : diffPkgNames) {
            ApplicationInfo appInfo = HelperUtils.getApplicationInfo(context, pkg);
            if (appInfo != null) {
                Log.d(TAG, "new pkg:" + pkg + " found installed,should be scaned");
                installedDiffPkgList.add(pkg);
            }
        }
        Log.d(TAG, "diff size:" + diffPkgNames.size() + " send broadcast to securitydetector size:"
                + installedDiffPkgList.size());
        if (installedDiffPkgList.size() > 0) {
            Intent bdIntent = new Intent(BROADCAST_INSTALLED_SAFELIST_ADDED);
            bdIntent.putStringArrayListExtra("DetecionAppList", installedDiffPkgList);
            context.sendBroadcast(bdIntent);
        }
    }

    private void updateLocalProviderDatabase(Context context, HashMap<String, Integer> pkgMap) {
        context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI, "usertype='" + TYPE + "'", null);
        for (Entry<String, Integer> appEntry : pkgMap.entrySet()) {
            ContentValues values = new ContentValues();
            values.put("usertype", TYPE);
            values.put("packagename", appEntry.getKey());
            values.put("status", appEntry.getValue());
            context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
        }
        Log.d(TAG, "update " + pkgMap.size() + " apps to safed list!");
    }

    private String updateAVLRules(Context context, ArrayList<String> pkgList) {
        return "";
    }

    private void recordSetFinancialApps(boolean res) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        StringBuilder sb = new StringBuilder();
        sb.append("\n<---AVL setEnableFinancialApps " + dateStr + "--->").append("\nResult:" + res);
        SmartCleanInfoWriter.writeToDataFile(sb.toString());
    }

    private void recordUpdateRulesData(int res, String newFinVer) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        StringBuilder sb = new StringBuilder();
        sb.append("\n<---AVL updateFinancialAppRule " + dateStr + "--->").append("\nResult:" + res)
                .append("\nFinancialAppRuleVersion:" + newFinVer);
        SmartCleanInfoWriter.writeToDataFile(sb.toString());
    }



    @Override
    public boolean isNotify() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public int getVersion(Context context) {
        final int VERSION = Settings.System.getInt(context.getContentResolver(), SAFELIST_VERSION_TAG, 0);
        return VERSION;
    }

    @Override
    public JSONObject getStrategyRequestBody(Context context) {
        PackageManager manager;
        PackageInfo info = null;
        manager = context.getPackageManager();
        String appVersion = "";
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            appVersion = String.valueOf(info.versionCode);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "can not find " + context.getPackageName());
            appVersion = "";
        }
        String imei = HelperUtils.getImei(context, true);
        String model = HelperUtils.getModel();
        int listVersion = getVersion(context);

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("APP_VERSION", appVersion);// 系统管家版本
            jsonObj.put("MODEL", model); // 设备型号
            jsonObj.put("IMEI", imei); // 设备IMEI
            jsonObj.put("LIST_VERSION", listVersion); // 金融列表Version
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return jsonObj;
    }
}
