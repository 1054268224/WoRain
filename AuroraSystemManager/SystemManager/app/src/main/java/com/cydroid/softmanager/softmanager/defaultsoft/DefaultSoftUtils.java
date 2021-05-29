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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.PatternMatcher;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DefaultSoftUtils {
    private static final String TAG = "DefaultSoftUtils";

    private static final String SETTINGS_KEY_DEF_INPUT_METHOD = Settings.Secure.DEFAULT_INPUT_METHOD;
    private static final List<String> mIgnoreApps = new ArrayList<>();

    static {
       // mIgnoreApps.add("com.cydroid.kidshome");
    }

    public static final String getDefaultInputMethodId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                SETTINGS_KEY_DEF_INPUT_METHOD);
    }

    public static final void setDefaultInputMethodId(Context context, String id) {
        Settings.Secure.putString(context.getContentResolver(), SETTINGS_KEY_DEF_INPUT_METHOD,
                id);
    }

    public static final List<InputMethodInfo> getEnabledInputMethodList(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // Gionee xionghonggang 2017-03-10 modify for 80332 begin
        List<InputMethodInfo> imInfos = imm.getEnabledInputMethodList();
        String googleVoiceInputMethodId =
                "com.google.android.googlequicksearchbox/com.google.android.voicesearch.ime.VoiceInputMethodService";
        for (int i = imInfos.size() - 1; i >= 0; i--) {
            if (imInfos.get(i).getId().equals(googleVoiceInputMethodId)) {
                imInfos.remove(i);
            }
        }
        return imInfos;
        // return imm.getEnabledInputMethodList();
        // Gionee xionghonggang 2017-03-10 modify for 80332 end
    }

    //peri modify for 58595 start
    public static void loadMatchResolves(Context context, Intent intent, DefaultSoftInfo defInfo, int index) {
        PackageManager pm = context.getPackageManager();
        ResolveInfo best = context.getPackageManager().resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        if (null != best && ((null == best.activityInfo
                || best.activityInfo.packageName.equals("android")))) {
            best = null;
        }
        List<ResolveInfo> riList;

        if (index == 1) { // i==1 browser
            riList = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
            // Gionee xionghg 2017-05-04 add for 129196 begin
            final UserHandle managedProfile = getManagedProfile(UserManager.get(context));
            final int userId = managedProfile != null ? managedProfile.getIdentifier()
                    : UserHandle.myUserId();
            String defaultPackage = pm.getDefaultBrowserPackageNameAsUser(userId);

            for (int i = 0; i < riList.size(); ++i) {
                ResolveInfo ri = riList.get(i);
                if (!ri.handleAllWebDataURI) {
                    Log.d(TAG, "load browser apps: pkgName: " + ri.activityInfo.packageName +
                            ", handleAllWebDataURI: " + ri.handleAllWebDataURI);
                    riList.remove(i);
                    --i;
                    continue;
                }
                if (defaultPackage != null && defaultPackage.equals(ri.activityInfo.packageName)) {
                    best = ri;
                }
            }
            // Gionee xionghg 2017-05-04 add for 129196 end
        } else {
            riList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY
                    | PackageManager.GET_RESOLVED_FILTER);
        }
        //peri modify for 58595 end
        if (riList.isEmpty()) {
            return;
        }

        matchRiFilter(riList);
        ResolveInfo.DisplayNameComparator rComparator = new ResolveInfo.DisplayNameComparator(pm);
        Collections.sort(riList, rComparator);

        if (riList.size() > 1) {
            addNotSetDefInfo(context, defInfo);
        }

        addRisToDefInfo(context, defInfo, riList, best);
    }

    // ResolveInfo list which gets from PM queryIntentActivities method is ordered from best to worst match
    private static List<ResolveInfo> matchRiFilter(List<ResolveInfo> riList) {
        ResolveInfo r0 = riList.get(0);
        for (int j = riList.size() - 1; j >= 1; --j) {
            ResolveInfo riInfo = riList.get(j);
            if (r0.priority != riInfo.priority || r0.isDefault != riInfo.isDefault) {
                riList.remove(j);
            }
        }

        for (int i = 0; i < riList.size(); ++i) {
            ResolveInfo ri = riList.get(i);
            if (mIgnoreApps.contains(ri.activityInfo.packageName)) {
                riList.remove(i);
                --i;
            }
        }
        return riList;
    }

    private static void addNotSetDefInfo(Context context, DefaultSoftInfo defInfo) {
        Resources res = context.getResources();
        DefaultSoftResolveInfo empty = defInfo.getByUnique(context.getPackageName());
        if (null == empty) {
            empty = new DefaultSoftResolveInfo();
            empty.setUnique(context.getPackageName());
            empty.setTitle(res.getString(R.string.title_software_def_not_set));
            empty.setPackageName("");
            defInfo.addEmptyMatch(empty);
            defInfo.addToMapEntry(empty);
        }
    }

    private static void addRisToDefInfo(Context context, DefaultSoftInfo defInfo,
                                        List<ResolveInfo> riList, ResolveInfo best) {
        // Chenyee xionghg 20171214 modify for SW17W16A-2504 begin
        // 如微信等有两个可以查看图片的组件，以包名作为唯一判断依据显然不合适，会导致设置默认软件失败，改为组件名
        for (int i = 0; i < riList.size(); ++i) {
            ResolveInfo riInfo = riList.get(i);
            // String unique = riInfo.activityInfo.applicationInfo.packageName;
            String unique = riInfo.activityInfo.packageName + "/" + riInfo.activityInfo.name;
            DefaultSoftResolveInfo resolveInfo = defInfo.getByUnique(unique);

            if (resolveInfo == null) {
                resolveInfo = new DefaultSoftResolveInfo(riInfo, context);
                // resolveInfo.setPackageName(unique);
                resolveInfo.setPackageName(riInfo.activityInfo.packageName);
                resolveInfo.setUnique(unique);
                defInfo.addMatch(resolveInfo);
                defInfo.addToMapEntry(resolveInfo);
            } else {
                resolveInfo.setBestMatched(false);
            }
            if (best != null && best.activityInfo.packageName.equals(riInfo.activityInfo.packageName)) {
                resolveInfo.setBestMatched(true);
                defInfo.setBestMatch(resolveInfo);
            }
        }
        // Chenyee xionghg 20171214 modify for SW17W16A-2504 end
    }

    // Chenyee xionghg add for default home begin
    private static final IntentFilter HOME_FILTER;

    static {
        HOME_FILTER = new IntentFilter(Intent.ACTION_MAIN);
        HOME_FILTER.addCategory(Intent.CATEGORY_HOME);
        HOME_FILTER.addCategory(Intent.CATEGORY_DEFAULT);
    }

    public static void setDefaultHome(PackageManager pm, ResolveInfo ri) {
        if (!TextUtils.isEmpty(ri.activityInfo.name)) {
            final ComponentName component = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            Log.d(TAG, "setDefaultHome:" + component);
            final List<ResolveInfo> homeActivities = new ArrayList<>();
            pm.getHomeActivities(homeActivities);
            final List<ComponentName> allComponents = new ArrayList<>();
            for (ResolveInfo info : homeActivities) {
                final ActivityInfo appInfo = info.activityInfo;
                ComponentName activityName = new ComponentName(appInfo.packageName, appInfo.name);
                allComponents.add(activityName);
            }
            pm.replacePreferredActivity(
                    HOME_FILTER,
                    IntentFilter.MATCH_CATEGORY_EMPTY,
                    allComponents.toArray(new ComponentName[0]),
                    component);
        }
    }
    // Chenyee xionghg add for default home end


    //Chenyee guoxt modify for CSW1707TL-23 begin
    @SuppressWarnings("deprecation")
    public static void setDefaultReader(Context context, Intent intent, ResolveInfo ri, PackageManager pm,
                                  DefaultSoftInfo defInfo) {
        String packageName = ri.activityInfo.packageName;
        String clsName = ri.activityInfo.name;
        Log.d(TAG, "setDefault intent:" + intent + ", pkgName:" + ri.activityInfo.packageName);

        IntentFilter filter = new IntentFilter();
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        intent.setComponent(new ComponentName(packageName, clsName));
        if (intent.getAction() != null) {
            filter.addAction(intent.getAction());
        }
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String cat : categories) {
                filter.addCategory(cat);
            }
        }
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        int cat = ri.match & IntentFilter.MATCH_CATEGORY_MASK;
        Uri data = intent.getData();
        if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
            String mimeType = intent.resolveType(context);
            if (mimeType != null) {
                try {
                    filter.addDataType(mimeType);
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    filter = null;
                }
            }
        }
        if (data != null && data.getScheme() != null) {
            // We need the data specification if there was no type,
            // OR if the scheme is not one of our magical "file:"
            // or "content:" schemes (see IntentFilter for the reason).
            if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                    || (!"file".equals(data.getScheme()) && !"content".equals(data.getScheme()))) {
                filter.addDataScheme(data.getScheme());

                // Look through the resolved filter to determine which part
                // of it matched the original Intent.
                Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                if (pIt != null) {
                    String ssp = data.getSchemeSpecificPart();
                    while (ssp != null && pIt.hasNext()) {
                        PatternMatcher p = pIt.next();
                        if (p.match(ssp)) {
                            filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
                Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                if (aIt != null) {
                    while (aIt.hasNext()) {
                        IntentFilter.AuthorityEntry a = aIt.next();
                        if (a.match(data) >= 0) {
                            int port = a.getPort();
                            filter.addDataAuthority(a.getHost(),
                                    port >= 0 ? Integer.toString(port) : null);
                            break;
                        }
                    }
                }
                pIt = ri.filter.pathsIterator();
                if (pIt != null) {
                    String path = data.getPath();
                    while (path != null && pIt.hasNext()) {
                        PatternMatcher p = pIt.next();
                        if (p.match(path)) {
                            filter.addDataPath(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
            }
        }

        List<ResolveInfo>  riList = pm.queryIntentActivities(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_READER), PackageManager.MATCH_DEFAULT_ONLY
                | PackageManager.GET_RESOLVED_FILTER);

        if (filter != null) {
             int count = defInfo.getMatches().size() - 1;
            ComponentName[] set;
            if(riList.size()>count){
                set = new ComponentName[count+1];

            }else {
                set = new ComponentName[count];

            }

            int bestMatch = 0;
            for (int i = 0; i < count; i++) {
                DefaultSoftResolveInfo info = defInfo.getMatches().get(i + 1);
                ResolveInfo r = info.getResolveInfo();
                // set[i] = new ComponentName(packageName, clsName);
                set[i] = new ComponentName(r.activityInfo.packageName,
                        r.activityInfo.name);
                if (r.match > bestMatch) {
                    bestMatch = r.match;
                }
            }

                for (int i =0;i<riList.size();i++){
                    ResolveInfo info = riList.get(i);
                    if(info.activityInfo.packageName.equals("com.android.chrome")
                     && info.activityInfo.name.equals("com.google.android.apps.chrome.VrIntentDispatcher")){
                        set[count] = new ComponentName(info.activityInfo.packageName,
                                info.activityInfo.name);
                        Log.d(TAG,"set[count]" + set[count]);
                    }
                }

            Log.d(TAG, "setDefault packageName:" + intent.getComponent().getPackageName());
            pm.addPreferredActivity(filter, bestMatch, set, intent.getComponent());
        }
    }
    //Chenyee guoxt modify for CSW1707TL-23 end
    @SuppressWarnings("deprecation")
    public static void setDefault(Context context, Intent intent, ResolveInfo ri, PackageManager pm,
                                  DefaultSoftInfo defInfo) {
        String packageName = ri.activityInfo.packageName;
        String clsName = ri.activityInfo.name;
        Log.d(TAG, "setDefault intent:" + intent + ", pkgName:" + ri.activityInfo.packageName);

        IntentFilter filter = new IntentFilter();
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        intent.setComponent(new ComponentName(packageName, clsName));
        if (intent.getAction() != null) {
            filter.addAction(intent.getAction());
        }
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String cat : categories) {
                filter.addCategory(cat);
            }
        }
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        int cat = ri.match & IntentFilter.MATCH_CATEGORY_MASK;
        Uri data = intent.getData();
        if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
            String mimeType = intent.resolveType(context);
            if (mimeType != null) {
                try {
                    filter.addDataType(mimeType);
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    filter = null;
                }
            }
        }
        if (data != null && data.getScheme() != null) {
            // We need the data specification if there was no type,
            // OR if the scheme is not one of our magical "file:"
            // or "content:" schemes (see IntentFilter for the reason).
            if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                    || (!"file".equals(data.getScheme()) && !"content".equals(data.getScheme()))) {
                filter.addDataScheme(data.getScheme());

                // Look through the resolved filter to determine which part
                // of it matched the original Intent.
                Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                if (pIt != null) {
                    String ssp = data.getSchemeSpecificPart();
                    while (ssp != null && pIt.hasNext()) {
                        PatternMatcher p = pIt.next();
                        if (p.match(ssp)) {
                            filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
                Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                if (aIt != null) {
                    while (aIt.hasNext()) {
                        IntentFilter.AuthorityEntry a = aIt.next();
                        if (a.match(data) >= 0) {
                            int port = a.getPort();
                            filter.addDataAuthority(a.getHost(),
                                    port >= 0 ? Integer.toString(port) : null);
                            break;
                        }
                    }
                }
                pIt = ri.filter.pathsIterator();
                if (pIt != null) {
                    String path = data.getPath();
                    while (path != null && pIt.hasNext()) {
                        PatternMatcher p = pIt.next();
                        if (p.match(path)) {
                            filter.addDataPath(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
            }
        }

        if (filter != null) {
            final int count = defInfo.getMatches().size() - 1;
            ComponentName[] set = new ComponentName[count];
            int bestMatch = 0;
            for (int i = 0; i < count; i++) {
                DefaultSoftResolveInfo info = defInfo.getMatches().get(i + 1);
                ResolveInfo r = info.getResolveInfo();
                // set[i] = new ComponentName(packageName, clsName);
                set[i] = new ComponentName(r.activityInfo.packageName,
                        r.activityInfo.name);
                if (r.match > bestMatch) {
                    bestMatch = r.match;
                }
            }
            Log.d(TAG, "setDefault packageName:" + intent.getComponent().getPackageName());
            pm.addPreferredActivity(filter, bestMatch, set, intent.getComponent());
        }
    }

    public static boolean isSystemApp(Context context, String packageName) {
        ApplicationInfo appInfo = HelperUtils.getApplicationInfo(context, packageName);
        return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ||
                ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public static boolean isDefaultSoftRomApp(Context context, int defaultSoftItemIndex,
                                              String packageName) {
        String[] defaultSoftRomApps = context.getResources().getStringArray(R.array.default_soft_rom_app);
        return packageName.equals(defaultSoftRomApps[defaultSoftItemIndex - 1]);
    }

    //fengpeipei add for 58595 start

    /**
     * Returns the managed profile of the current user or null if none found.
     */
    public static UserHandle getManagedProfile(UserManager userManager) {
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        final int count = userProfiles.size();
        for (int i = 0; i < count; i++) {
            final UserHandle profile = userProfiles.get(i);
            if (profile.getIdentifier() == userManager.getUserHandle()) {
                continue;
            }
            final UserInfo userInfo = userManager.getUserInfo(profile.getIdentifier());
            if (userInfo.isManagedProfile()) {
                return profile;
            }
        }
        return null;
    }
    //fengpeipei add for 58595 end
}
