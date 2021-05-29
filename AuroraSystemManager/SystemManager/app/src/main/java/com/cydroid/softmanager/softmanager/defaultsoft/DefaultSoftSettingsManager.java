/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.defaultsoft;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.inputmethod.InputMethodInfo;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSoftSettingsManager implements PackageChangedCallback {
    private static final String TAG = "DefaultSoftSettingsManager";
    private static final boolean DEBUG = false;
    private static DefaultSoftSettingsManager sInstance;

    private Context mContext;
    private boolean mFirstTime = true;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;

    private final List<DefaultSoftItemInfo> mDefaultSoftItems = new ArrayList<>();
    private final List<DefaultSoftInfo> mDefaultSoftInfos = new ArrayList<>();

    private final Map<String, WeakReference<StateChangeCallback>> mCallbacks = new HashMap<>();

    public static synchronized DefaultSoftSettingsManager getInstance() {
        if (null == sInstance) {
            sInstance = new DefaultSoftSettingsManager();
        }
        return sInstance;
    }

    private DefaultSoftSettingsManager() {
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
        updateDefaultSoftSettings();
    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        mFirstTime = false;
        mContext = context.getApplicationContext();
        loadDefaultSoftItems();
        loadDefaultSoftApps();
        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
    }

    private void loadDefaultSoftItems() {
        int[] titleResIds = new int[]{R.string.ui_def_soft_inputmethod,
                R.string.ui_def_soft_browser,
                R.string.ui_def_soft_contact,
                R.string.ui_def_soft_launcher,
                R.string.ui_def_soft_camera,
                R.string.ui_def_soft_photos,
                R.string.ui_def_soft_music,
                R.string.ui_def_soft_video,
                R.string.ui_def_soft_reading};

        int[] resIds = new int[]{R.drawable.icon_def_input,
                R.drawable.icon_def_brower,
                R.drawable.icon_read_contacts,
                R.drawable.icon_def_launcher,
                R.drawable.icon_def_camera,
                R.drawable.icon_def_image,
                R.drawable.icon_def_music,
                R.drawable.icon_def_video,
                R.drawable.icon_def_reader};

        for (int i = 0; i < titleResIds.length; ++i) {
            DefaultSoftItemInfo item = new DefaultSoftItemInfo();
            item.setTitleResId(titleResIds[i]);
            item.setIconResId(resIds[i]);
            mDefaultSoftItems.add(item);
        }
    }

    private void loadDefaultSoftApps() {
        releaseDefaultSoftInfos();
        loadDefaultInputMethodInfo(mContext);
        loadOtherDefaultSoftInfos(mContext);
    }

    private void releaseDefaultSoftInfos() {
        for (DefaultSoftInfo info : mDefaultSoftInfos) {
            info.clear();
        }
        mDefaultSoftInfos.clear();
    }

    private void loadDefaultInputMethodInfo(Context context) {
        DefaultSoftInfo defInfo = new DefaultSoftInfo();
        mDefaultSoftInfos.add(defInfo);

        final String defInputMethodId = DefaultSoftUtils.getDefaultInputMethodId(mContext);
        final List<InputMethodInfo> imInfos = DefaultSoftUtils.getEnabledInputMethodList(mContext);
        for (int i = 0; i < imInfos.size(); ++i) {
            InputMethodInfo info = imInfos.get(i);
            DefaultSoftResolveInfo ri = new DefaultSoftResolveInfo();
            ri.setPackageName(info.getPackageName());
            ri.setTitle(info.loadLabel(context.getPackageManager()).toString());
            ri.setUnique(info.getId());
            defInfo.addMatch(ri);
            defInfo.addToMapEntry(ri);

            if (info.getId().equals(defInputMethodId)) {
                ri.setBestMatched(true);
                defInfo.setBestMatch(ri);
            }
        }
    }

    private void loadOtherDefaultSoftInfos(Context context) {
        for (int i = 1; i < mDefaultSoftItems.size(); ++i) {
            DefaultSoftInfo defInfo = new DefaultSoftInfo();
            mDefaultSoftInfos.add(defInfo);
            //peri modify for 58595 start
            DefaultSoftUtils.loadMatchResolves(context, DefMrgSoftIntent.getDefIntent(i), defInfo, i);
            //peri modify for 58595 end
        }
    }

    private void updateDefaultSoftSettings() {
        loadDefaultSoftApps();
    }

    public synchronized List<DefaultSoftItemInfo> getDefaultSoftItems() {
        for (int i = 0; i < mDefaultSoftItems.size()
                && i < mDefaultSoftInfos.size(); ++i) {
            DefaultSoftItemInfo item = mDefaultSoftItems.get(i);
            DefaultSoftInfo defInfo = mDefaultSoftInfos.get(i);

            DefaultSoftResolveInfo bestMatch = defInfo.getBestMatch();
            if (i != 0 && defInfo.getMatches().isEmpty()) {
                item.setSummary("");
                continue;
            }
            item.setHasDefaultSoftApp(true);
            if (bestMatch != null) {
                item.setSummary(bestMatch.getTitle());
            } else {
                item.setSummary(mContext.getResources().getString(R.string.text_software_def_not_set));
            }
        }
        return mDefaultSoftItems;
    }

    /*
    public synchronized DefaultSoftInfo getDefaultSoftInfoByIndex(int index) {
        return new DefaultSoftInfo(mDefaultSoftInfos.get(index));
    }
    */
    public synchronized List<DefaultSoftResolveInfo> getDefaultSoftMatchesByItemIndex(int index) {
        List<DefaultSoftResolveInfo> result = new ArrayList<DefaultSoftResolveInfo>();
        DefaultSoftInfo defaultSoftInfo = mDefaultSoftInfos.get(index);
        if (null != defaultSoftInfo) {
            result.addAll(defaultSoftInfo.getMatches());
        }
        return result;
    }

    public synchronized void setDefInputMethod(int index) {
        DefaultSoftInfo defInfo = mDefaultSoftInfos.get(0);
        DefaultSoftResolveInfo curBestMatch = defInfo.getBestMatch();
        if (curBestMatch != null) {
            curBestMatch.setBestMatched(false);
        }

        final List<InputMethodInfo> imInfos = DefaultSoftUtils.getEnabledInputMethodList(mContext);
        if (imInfos.size() > index && index >= 0) {
            DefaultSoftUtils.setDefaultInputMethodId(mContext, imInfos.get(index).getId());

            DefaultSoftResolveInfo defaultSoftResolveInfo = defInfo.getMatches().get(index);
            defaultSoftResolveInfo.setBestMatched(true);
            defInfo.setBestMatch(defaultSoftResolveInfo);
        }
    }

    //fengpeipei add for 58595 start
    public synchronized void setDefBrowserMethod(int index) {
        PackageManager pm = mContext.getPackageManager();
        DefaultSoftInfo defInfo = mDefaultSoftInfos.get(1);
        DefaultSoftResolveInfo curBestMatch = defInfo.getBestMatch();
        if (curBestMatch != null) {
            pm.clearPackagePreferredActivities(curBestMatch.getResolveInfo().activityInfo.packageName);
            curBestMatch.setBestMatched(false);
        }

        DefaultSoftResolveInfo info = defInfo.getMatches().get(index);
        if (defInfo.getMatches().size() > index && index >= 0) {
            final boolean forWork = true;
            final UserHandle managedProfile = DefaultSoftUtils.getManagedProfile(UserManager.get(mContext));
            final int userId = forWork && managedProfile != null ? managedProfile.getIdentifier()
                    : UserHandle.myUserId();
            if (info.getPackageName().equals("")) {
                boolean result = pm.setDefaultBrowserPackageNameAsUser(null, userId);
            } else {
                String name = info.getResolveInfo().activityInfo.packageName;
                boolean result = pm.setDefaultBrowserPackageNameAsUser(name, userId);
            }
            info.setBestMatched(true);
            defInfo.setBestMatch(info);
        }
    }
    //fengpeipei add for 58595 end



    // guoxt modify for CSW1703CX-1227 begin
    public synchronized void setCXDefBrowserMethod() {
        PackageManager pm = mContext.getPackageManager();
        DefaultSoftInfo defInfo = mDefaultSoftInfos.get(1);
        int index = 0;
        for(int i=0;i<defInfo.getMatches().size();i++){
            DefaultSoftResolveInfo info = defInfo.getMatches().get(i);
            if(info.getPackageName().equals("com.ume.browser")){
                index = i;
                break;
            }
        }
        DefaultSoftResolveInfo curBestMatch = defInfo.getBestMatch();
        if (curBestMatch != null) {
            pm.clearPackagePreferredActivities(curBestMatch.getResolveInfo().activityInfo.packageName);
            curBestMatch.setBestMatched(false);
        }

        DefaultSoftResolveInfo info = defInfo.getMatches().get(index);
        if (defInfo.getMatches().size() > index && index >= 0) {
            final boolean forWork = true;
            final UserHandle managedProfile = DefaultSoftUtils.getManagedProfile(UserManager.get(mContext));
            final int userId = forWork && managedProfile != null ? managedProfile.getIdentifier()
                    : UserHandle.myUserId();
            if (info.getPackageName().equals("")) {
                boolean result = pm.setDefaultBrowserPackageNameAsUser(null, userId);
            } else {
                String name = info.getResolveInfo().activityInfo.packageName;
                boolean result = pm.setDefaultBrowserPackageNameAsUser(name, userId);
            }
            info.setBestMatched(true);
            defInfo.setBestMatch(info);
        }
    }
    // guoxt modify for CSW1703CX-1227 begin

    public synchronized void setDefSoft(int pos, int defaultSoftItemIndex) {
        PackageManager pm = mContext.getPackageManager();
        DefaultSoftInfo defInfo = mDefaultSoftInfos.get(defaultSoftItemIndex);
        DefaultSoftResolveInfo curBestMatch = defInfo.getBestMatch();
        if (curBestMatch != null) {
            pm.clearPackagePreferredActivities(curBestMatch.getResolveInfo().activityInfo.packageName);
            curBestMatch.setBestMatched(false);
        }

        Intent intent = DefMrgSoftIntent.getDefIntent(defaultSoftItemIndex);
        if (pos != 0 && pos < defInfo.getMatches().size()) {
            DefaultSoftResolveInfo resolve = defInfo.getMatches().get(pos);
            if (defaultSoftItemIndex == DefMrgSoftIntent.DEF_MUSIC) {
                for (String type : DefMrgSoftIntent.getMusicDataTypes()) {
                     /*guoxt modify for CSW1702A-2320 begin */
                    intent.setDataAndType(Uri.parse("file:///android_asset/cydata"), type);
                     /*guoxt modify for CSW1702A-2320 end */
                    DefaultSoftUtils.setDefault(mContext, intent, resolve.getResolveInfo(), pm, defInfo);
                }
            } else if (defaultSoftItemIndex == DefMrgSoftIntent.DEF_HOME) {
                DefaultSoftUtils.setDefaultHome(pm, resolve.getResolveInfo());
                //Chenyee guoxt modify for CSW1707TL-23 begin
            } else if(defaultSoftItemIndex == DefMrgSoftIntent.DEF_READER){
                DefaultSoftUtils.setDefaultReader(mContext, intent, resolve.getResolveInfo(), pm, defInfo);
                //Chenyee guoxt modify for CSW1707TL-23 end
            }else{
                DefaultSoftUtils.setDefault(mContext, intent, resolve.getResolveInfo(), pm, defInfo);
            }
        }
    }

    public synchronized String getDefaultSoftItemNameByIndex(int index) {
        // Chenyee xionghg 20180327 add for CSW1702A-3299 begin
        // 目前只有DefaultSoftDetailActivity类会调用该方法，且已捕获该异常
        if (index >= mDefaultSoftItems.size()) {
            throw new IllegalArgumentException("mDefaultSoftItems is not initialized, size=" +
                    mDefaultSoftItems.size() + ", index=" + index);
        }
        // Chenyee xionghg 20180327 add for CSW1702A-3299 end
        String name = "";
        DefaultSoftItemInfo item = mDefaultSoftItems.get(index);
        if (null != item) {
            int titleResId = item.getTitleResId();
            name = mContext.getResources().getString(titleResId);
        }
        return name;
    }

    @Override
    public synchronized void addPackage(String packageName) {
        Log.d(TAG, "addPackage packageName:" + packageName);
        updateDefaultSoftSettings();
    }

    @Override
    public synchronized void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        updateDefaultSoftSettings();
    }

    @Override
    public synchronized void changePackage(String packageName) {
        Log.d(DEBUG, TAG, "changePackage packageName:" + packageName);
        // 默认软件设置界面每次进入都会取最新值，这里更新非常耗时，没有意义
        // updateDefaultSoftSettings();
    }

    public void setAppsChangeCallBack(String key, StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = new WeakReference<>(callback);
        mCallbacks.put(key, cb);
    }

    public void unsetAppsChangeCallBack(String key) {
        mCallbacks.remove(key);
    }

    private void notifyAppsChange() {
        if (mCallbacks != null) {
            for (WeakReference<StateChangeCallback> cb : mCallbacks.values()) {
                StateChangeCallback callback = cb.get();
                if (callback != null) {
                    callback.onStateChange();
                }
            }
        }
    }

    public synchronized void setNotHasDefaultSoftToRomApp() {
        // guoxt modify for CR01777236 begin
        for (int i = 2; i < mDefaultSoftInfos.size(); ++i) {
            // guoxt modify for CR01777236 end
            DefaultSoftInfo defaultSoftInfo = mDefaultSoftInfos.get(i);
            if (!hasDefaultSoftApp(defaultSoftInfo)) {
                setDefaultSoftToRomApp(i, defaultSoftInfo);
            }
        }
        // guoxt modify for CSW1703CX-1227 begin
        if(Consts.cyCXFlag){
            DefaultSoftInfo defaultSoftInfo = mDefaultSoftInfos.get(1);
            if (!hasDefaultSoftApp(defaultSoftInfo)) {
                setCXDefBrowserMethod();
            }
        }
        // guoxt modify for CSW1703CX-1227 end
    }

    private boolean hasDefaultSoftApp(DefaultSoftInfo defaultSoftInfo) {
        return defaultSoftInfo.getBestMatch() != null;
    }

    private void setDefaultSoftToRomApp(int defaultSoftItemIndex,
                                        DefaultSoftInfo defaultSoftInfo) {
        List<DefaultSoftResolveInfo> infos = getDefaultSoftMatchesByItemIndex(defaultSoftItemIndex);
        for (int i = 0; i < infos.size(); ++i) {
            DefaultSoftResolveInfo info = infos.get(i);
            String packageName = info.getPackageName();
            /*guoxt modify for # 30171 begin */
            if (defaultSoftItemIndex == DefMrgSoftIntent.DEF_MUSIC
                    && packageName.equals("com.cydroid.video")) {
                continue;
            }
            /*guoxt modify for # 30171 end */
            Log.d(TAG, "setDefaultSoftToRomApp packageName:" + packageName);
            if (null != packageName && !packageName.isEmpty()
                    && DefaultSoftUtils.isDefaultSoftRomApp(mContext, defaultSoftItemIndex, packageName)) {

                setDefSoft(i, defaultSoftItemIndex);
                break;
            }
        }
    }

    //Chenyee guot modify for CSW1703BA-214 begin
    public  boolean getDefaultSerachapp(int search) {
        DefaultSoftInfo defaultSoftInfo = mDefaultSoftInfos.get(search);
        if (!hasDefaultSoftApp(defaultSoftInfo)) {
            setDefaultSoftToRomApp(search, defaultSoftInfo);
        }else {
            if (defaultSoftInfo.getBestMatch() != null){
                if(!defaultSoftInfo.getBestMatch().getPackageName().equals("com.google.android.googlequicksearchbox")){
                    Log.d(TAG, "search app  return:" + "false" );
                    return false;
                }else{
                    Log.d(TAG, "search app return:" + "true" );
                    return true;
                }
            }
        }
        return  false;
    }
    //Chenyee guot modify for CSW1703BA-214 end



    //Chenyee guot modify for CSW1703BA-214 begin
    public  boolean getDefaultGallery(int search) {
        DefaultSoftInfo defaultSoftInfo = mDefaultSoftInfos.get(search);
        if (!hasDefaultSoftApp(defaultSoftInfo)) {
            setDefaultSoftToRomApp(search, defaultSoftInfo);
        }else {
            if (defaultSoftInfo.getBestMatch() != null){
                if(!defaultSoftInfo.getBestMatch().getPackageName().equals("com.google.android.apps.photos")){
                    Log.d(TAG, "search app  return:" + "false" );
                    return false;
                }else{
                    Log.d(TAG, "search app return:" + "true" );
                    return true;
                }
            }
        }
        return  false;
    }
    //Chenyee guot modify for CSW1703BA-214 end
}