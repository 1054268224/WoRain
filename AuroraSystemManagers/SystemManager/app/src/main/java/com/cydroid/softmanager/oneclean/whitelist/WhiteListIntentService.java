package com.cydroid.softmanager.oneclean.whitelist;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WhiteListIntentService extends IntentService {

    private static final String TAG = "WhiteListIntentService";

    private static final String ACTION_SAVE_SYSTEM_WHITE_APPS = "save_system_white_apps";
    private static final String ACTION_SAVE_DEFAULT_USER_WHITE_APPS= "save_default_user_white_apps";

    private Context mAppContext;
    private MainProcessSettingsProviderHelper mHelper;

    public WhiteListIntentService() {
        super("WhiteListIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this.getApplicationContext();
        mHelper = new MainProcessSettingsProviderHelper(mAppContext);
    }

    // 暂时不会用到这里
    public static void startActionSaveSystemWhiteApps(Context context) {
        Intent intent = new Intent(context, WhiteListIntentService.class);
        intent.setAction(ACTION_SAVE_SYSTEM_WHITE_APPS);
        ServiceUtil.startForegroundService(context,intent);
    }

    /**
     * Starts this service to perform action save_default_user_white_apps.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSaveDefaultUserWhiteApps(Context context) {
        Intent intent = new Intent(context, WhiteListIntentService.class);
        intent.setAction(ACTION_SAVE_DEFAULT_USER_WHITE_APPS);
        ServiceUtil.startForegroundService(context,intent);
    }

    /**
     * 该方法运行于主进程的工作子线程，当主进程和remote进程各启动一次该服务后，该方法会被顺序调用两次，
     * 过滤掉后面一次调用，即可实现后台单次初始化用户白名单
     *
     * @param intent  启动Intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ServiceUtil.handleStartForegroundServices(this);
            final String action = intent.getAction();
            Log.d(TAG, "onHandleIntent: action=" + action);
            if (ACTION_SAVE_SYSTEM_WHITE_APPS.equals(action)) {
                handleSaveSystemWhiteApps();
            } else if (ACTION_SAVE_DEFAULT_USER_WHITE_APPS.equals(action)) {
                handleSaveDefaultUserWhiteApps();
            }
        }
    }

    // 暂时没有用到这里
    private void handleSaveSystemWhiteApps() {
        String[] whiteListDefault = mAppContext.getResources().getStringArray(R.array.oneclean_whitelist);

        Log.d(TAG, "load system white apps to db begin, count=" + whiteListDefault.length);
        for (String packageName : whiteListDefault) {
            ContentValues cv = new ContentValues();
            cv.put("packagename", packageName);
            cv.put("usertype", "oneclean");
            cv.put("status", "1");
            mAppContext.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, cv);
        }
        mHelper.putBoolean(WhiteListManager.PREF_KEY_IS_SYSTEM_WHITELIST_INITING, false);
        Log.d(TAG, "load system white apps to db end");
    }

    //guoxt modify for CSW1805A-1353 begin
    private int getVersion() {
        int soVersion = 0;
        try {
            PackageManager packageManager = mAppContext.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(
                    mAppContext.getPackageName(), 0);
            soVersion = packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return soVersion;
    }
    //guoxt modify for CSW1805A-1353 end

    private void handleSaveDefaultUserWhiteApps() {

        final int inited = mHelper.getInt(WhiteListManager.PREF_KEY_IS_USER_WHITELIST_INITED, 0);

        // Chenyee xionghg 20171130 modify for SW17W13SK-35 begin
        if (getVersion() <= inited) {
            Log.d(TAG, "getVersion: "+ getVersion()+ " inited:" + inited);
            return;
        }
        // mHelper.putBoolean(WhiteListManager.PREF_KEY_IS_USER_WHITELIST_INITING, true);

        String[] defaultUserWhiteListApps = mAppContext.getResources()
                .getStringArray(R.array.default_user_whitelist);

        Log.d(TAG, "load default user white apps to db begin");
        for (String packageName : defaultUserWhiteListApps) {
            Log.d(TAG, "add default user whitelisted app: " + packageName);
            WhiteListUtils.removeUserWhiteApp(mAppContext, packageName);
            WhiteListUtils.addUserWhiteApp(mAppContext, packageName);
        }
        //Chenyee <guoxt> <2018-6-26> add for CSW1707ST-43 begin
        if (Consts.cySTFlag){
            String[] customUserWhiteListST = mAppContext.getResources()
                    .getStringArray(R.array.default_user_whitelist_ST);

            for (String packageName : customUserWhiteListST) {
                WhiteListUtils.removeUserWhiteApp(mAppContext, packageName);
                WhiteListUtils.addUserWhiteApp(mAppContext, packageName);
            }
        }
        //Chenyee <guoxt> <2018-6-26> add for CSW1707ST-43 end

        // mHelper.putBoolean(WhiteListManager.PREF_KEY_IS_USER_WHITELIST_INITING, false);
        mHelper.putInt(WhiteListManager.PREF_KEY_IS_USER_WHITELIST_INITED, getVersion());
        // notify systemui
        WhiteListUtils.sendUserWhiteListChangeBroadcast(mAppContext);
        // Chenyee xionghg 20171130 modify for SW17W13SK-35 end
        Log.d(TAG, "load default user white apps to db end");
    }
}
