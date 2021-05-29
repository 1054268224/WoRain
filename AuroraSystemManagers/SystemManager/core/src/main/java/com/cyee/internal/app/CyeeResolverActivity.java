/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyee.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.internal.content.PackageMonitor;
import com.cyee.internal.R;
import com.cyee.internal.util.ReflectionUtils;
import com.cyee.utils.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeAutoMaticPageGridView;
import cyee.widget.CyeeBaseAutoAdapter;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeListView;

//Gionee:zhang_xin 2012-10-23 add for CR00717539 start
//Gionee:zhang_xin 2012-10-23 add for CR00717539 end
// Gionee fengjianyi 2012-12-26 add for CR00751916 start
//import com.mediatek.common.featureoption.FeatureOption;
// Gionee fengjianyi 2012-12-26 add for CR00751916 end

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to.  It is not normally used directly by application developers.
 */
public class CyeeResolverActivity extends CyeeAlertActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "CyeeResolverActivity";

    private int mLaunchedFromUid;
    private ResolveListAdapter mAdapter;
    private PackageManager mPm;
    private boolean mAlwaysUseOption;
    private boolean mResolvingHome = false;
    //private GridView mGrid;
    private CyeeAutoMaticPageGridView mAutoMaticGridView;
    private CyeeButton mAlwaysButton;
    private CyeeButton mOnceButton;
    private int mIconDpi;
    private int mIconSize;
    private int mMaxColumns;
    private int mLastSelected = GridView.INVALID_POSITION;
    //Gionee:zhang_xin 2012-10-23 add for CR00717539 start
    private final boolean mIsSupportAppSort = SystemProperties.get("ro.gn.appsort.default").equals("yes");
    //Gionee:zhang_xin 2012-10-23 add for CR00717539 end
    private boolean mSafeForwardingMode;
    private final ArrayList<Intent> mIntents = new ArrayList<>();
    private ResolverComparator mResolverComparator;
    private View mProfileView;
    private int mProfileSwitchMessageId = -1;
    /// M:
//    private IRCSePriorityExt mRCSePriorityExt = null;
    
    
    
    private static final boolean RO_GN_CYEE_CLONE_SUPPORT = SystemProperties.get("ro.gn.cyee.clone.support","no").equals("yes");
    private boolean mRegistered;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        @Override public void onSomePackagesChanged() {
            Log.v(TAG, "causion,onSomePackagesChanged!");
            mAdapter.handlePackagesChanged();
            if (mProfileView != null) {
                bindProfileView();
            }
        }
    };

    /**
     * Get the string resource to be used as a label for the link to the resolver activity for an
     * action.
     *
     * @param action The action to resolve
     *
     * @return The string resource to be used as a label
     */
    public static int getLabelRes(String action) {
        return ActionTitle.forAction(action).labelRes;
    }
    
    private enum ActionTitle {
        VIEW(Intent.ACTION_VIEW,
                com.android.internal.R.string.whichViewApplication,
                com.android.internal.R.string.whichViewApplicationNamed,
                com.android.internal.R.string.whichViewApplicationLabel),
        EDIT(Intent.ACTION_EDIT,
                com.android.internal.R.string.whichEditApplication,
                com.android.internal.R.string.whichEditApplicationNamed,
                com.android.internal.R.string.whichEditApplicationLabel),
        SEND(Intent.ACTION_SEND,
                com.android.internal.R.string.whichSendApplication,
                com.android.internal.R.string.whichSendApplicationNamed,
                com.android.internal.R.string.whichSendApplicationLabel),
        SENDTO(Intent.ACTION_SENDTO,
                com.android.internal.R.string.whichSendToApplication,
                com.android.internal.R.string.whichSendToApplicationNamed,
                com.android.internal.R.string.whichSendToApplicationLabel),
        SEND_MULTIPLE(Intent.ACTION_SEND_MULTIPLE,
                com.android.internal.R.string.whichSendApplication,
                com.android.internal.R.string.whichSendApplicationNamed,
                com.android.internal.R.string.whichSendApplicationLabel),
        CAPTURE_IMAGE(MediaStore.ACTION_IMAGE_CAPTURE,
                com.android.internal.R.string.whichImageCaptureApplication,
                com.android.internal.R.string.whichImageCaptureApplicationNamed,
                com.android.internal.R.string.whichImageCaptureApplicationLabel),
        DEFAULT(null,
                com.android.internal.R.string.whichApplication,
                com.android.internal.R.string.whichApplicationNamed,
                com.android.internal.R.string.whichApplicationLabel),
        HOME(Intent.ACTION_MAIN,
                com.android.internal.R.string.whichHomeApplication,
                com.android.internal.R.string.whichHomeApplicationNamed,
                com.android.internal.R.string.whichHomeApplicationLabel);

        public final String action;
        public final int titleRes;
        public final int namedTitleRes;
        public final int labelRes;

        ActionTitle(String action, int titleRes, int namedTitleRes, int labelRes) {
            this.action = action;
            this.titleRes = titleRes;
            this.namedTitleRes = namedTitleRes;
            this.labelRes = labelRes;
        }

        public static ActionTitle forAction(String action) {
            for (ActionTitle title : values()) {
                if (title != HOME && action != null && action.equals(title.action)) {
                    return title;
                }
            }
            return DEFAULT;
        }
    }
    
    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        // The resolver activity is set to be hidden from recent tasks.
        // we don't want this attribute to be propagated to the next activity
        // being launched.  Note that if the original Intent also had this
        // flag set, we are now losing it.  That should be a very rare case
        // and we can live with this.
        intent.setFlags(intent.getFlags()&~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    void safelyStartActivity(TargetInfo cti) {
        // If needed, show that intent is forwarded
        // from managed profile to owner or other way around.
        if (mProfileSwitchMessageId != -1) {
            Toast.makeText(this, getString(mProfileSwitchMessageId), Toast.LENGTH_LONG).show();
        }

        if (!mSafeForwardingMode) {
            if (cti.start(this, null)) {
                onActivityStarted(cti);
            }
            return;
        }
        try {
            if (cti.startAsCaller(this, null, UserHandle.USER_NULL)) {
                onActivityStarted(cti);
            }
        } catch (RuntimeException e) {
            String launchedFromPackage;
            try {
                launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(
                        getActivityToken());
            } catch (RemoteException e2) {
                launchedFromPackage = "??";
            }
        }
    }
    
    void onActivityStarted(TargetInfo cti) {
        // Do nothing
    }

    // Gionee jiaoyuan 2016-09-01 add for CR01739550 start
    private long firstTime = System.currentTimeMillis();
    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();   
        if (secondTime - firstTime > 500) {   
            super.onBackPressed();
            firstTime = secondTime; 
        }
    }
    // Gionee jiaoyuan 2016-09-01 add for CR01739550 end
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// Gionee fengjianyi 2013-01-16 modify for CR00751916 start
    	/*
        onCreate(savedInstanceState, makeMyIntent(),
                getResources().getText(com.android.internal.R.string.whichApplication),
                null, null, true);
        */
    	CharSequence title;
        title = getResources().getText(R.string.cyee_whichApplication);
        final Intent intent = makeMyIntent();
        final Set<String> categories = intent.getCategories();
        if (Intent.ACTION_MAIN.equals(intent.getAction())
                && categories != null
                && categories.size() == 1
                && categories.contains(Intent.CATEGORY_HOME)) {
            // Note: this field is not set to true in the compatibility version.
            mResolvingHome = true;
        }
        onCreate(savedInstanceState, intent, title, null, null, true);
    	// Gionee fengjianyi 2012-01-16 modify for CR00751916 end
    }

    /**
     * Compatibility version for other bundled services that use this overload without
     * a default title resource
     */
    protected void onCreate(Bundle savedInstanceState, Intent intent,
            CharSequence title, Intent[] initialIntents,
            List<ResolveInfo> rList, boolean alwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, alwaysUseOption);
    }
    
    protected void onCreate(Bundle savedInstanceState, Intent intent,
            CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList,
            boolean alwaysUseOption) {
        Log.e(TAG,"onCreate start");
        if (isLightTheme()) {
            //Gionee <20130830><cheny> modify for CR00874735 begin
            setTheme(com.cyee.internal.R.style.Theme_Cyee_Light_Dialog_Alert);
        } else {
            setTheme(com.cyee.internal.R.style.Theme_Cyee_Dark_Dialog_Alert);
            //Gionee <20130830><cheny> modify for CR00874735 end
        }
        ChameleonColorManager.getInstance().register(this, false);
        ChameleonColorManager.getInstance().onCreate(this);
        super.onCreate(savedInstanceState);

        // Determine whether we should show that intent is forwarded
        // from managed profile to owner or other way around.
        setProfileSwitchMessageId(intent.getContentUserHint());

        try {
            mLaunchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(
                    getActivityToken());
        } catch (RemoteException e) {
            mLaunchedFromUid = -1;
        }
        mPm = getPackageManager();
        mAlwaysUseOption = alwaysUseOption;
        mMaxColumns = getResources().getInteger(R.integer.cyee_config_maxResolverActivityColumns);

        // Add our initial intent as the first item, regardless of what else has already been added.
        mIntents.add(0, new Intent(intent));

        final String referrerPackage = getReferrerPackageName();

        mResolverComparator = new ResolverComparator(this, getTargetIntent(), referrerPackage);


        CyeeAlertController.AlertParams ap = mAlertParams;

        ap.mTitle = title;

        mPackageMonitor.register(this, getMainLooper(), false);
        mRegistered = true;

        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();
        mIconSize = am.getLauncherLargeIconSize();

        mAdapter = createAdapter(this,mIntents, initialIntents, rList,
                mLaunchedFromUid, alwaysUseOption);

        int count = mAdapter.getUnfilteredCount();
        Log.e(TAG,"onCreate count="+count);
        Log.e(TAG,"alwaysUseOption:" + alwaysUseOption);

        if (mLaunchedFromUid < 0 || UserHandle.isIsolated(mLaunchedFromUid)) {
            // Gulp!
            finish();
            return;
        } else if (count > 1 || null != mAdapter.getOtherProfile()) {
            Log.e(TAG, "getLayoutInflater before");
            ap.mView = getLayoutInflater().inflate(R.layout.cyee_resolver_grid_auto_paged, null);
            Log.e(TAG, "getLayoutInflater end");
            //mGrid = (GridView) ap.mView.findViewById(R.id.cyee_resolver_grid);
            //mGrid.setAdapter(mAdapter);
            mAutoMaticGridView = (CyeeAutoMaticPageGridView) ap.mView.findViewById(R.id.cyee_auto_matic);
            mAutoMaticGridView.setAdapter(mAdapter);
            final float scale = getResources().getDisplayMetrics().density;
            //mGrid.setVerticalSpacing((int) (6 * scale + 0.5f));
            //mGrid.setHorizontalSpacing((int) (32 * scale + 0.5f));

            mAutoMaticGridView.setOnItemClickListener(this);
            mAutoMaticGridView.setOnItemLongClickListener(new ItemLongClickListener());

            if (alwaysUseOption) {
                mAutoMaticGridView.setChoiceMode(CyeeListView.CHOICE_MODE_SINGLE);
            }

            //resizeGrid();
            //Gionee <zhang_xin><2013-05-08> add for CR00809845 begin
            mAutoMaticGridView.setItemChecked(0);
            mLastSelected = 0;
            //Gionee <zhang_xin><2013-05-08> add for CR00809845 end
        } else if (count == 1) {
            callStrictModeMethod("disableDeathOnFileUriExposure");
            try{
                safelyStartActivity(mAdapter.targetInfoForPosition(0, false));
            }finally {
                callStrictModeMethod("enableDeathOnFileUriExposure");
            }
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        } else {
            ap.mMessage = getResources().getText(R.string.cyee_noApplications);
        }

        setupAlert();

        if (alwaysUseOption) {
            //Gionee <hanbj> <20110406> modify for CR00986146 begin
            final ViewGroup buttonPanelLayout = (ViewGroup) findViewById(R.id.cyee_buttonPanel);
            final ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.cyee_button_bar);
            if (buttonPanelLayout != null && buttonLayout != null) {
                buttonPanelLayout.setVisibility(View.VISIBLE);
                //Gionee <hanbj> <20110406> modify for CR00986146 end
                buttonLayout.setVisibility(View.VISIBLE);
                mAlwaysButton = (CyeeButton) buttonLayout.findViewById(R.id.cyee_button_always);
                mOnceButton = (CyeeButton) buttonLayout.findViewById(R.id.cyee_button_once);
                //Gionee <zhang_xin><2013-05-08> add for CR00809845 begin
                if (count > 1) {
                    mAlwaysButton.setEnabled(true);
                    mOnceButton.setEnabled(true);
                }
                //Gionee <zhang_xin><2013-05-08> add for CR00809845 end
            } else {
                mAlwaysUseOption = false;
            }
        }
        mProfileView = findViewById(R.id.cyee_profile_button);
        if (mProfileView != null) {
            mProfileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DisplayResolveInfo dri = mAdapter.getOtherProfile();
                    if (dri == null) {
                        return;
                    }

                    // Do not show the profile switch message anymore.
                    mProfileSwitchMessageId = -1;

                    onIntentSelected(dri.getResolveInfo(), dri.getResolvedIntent(), false);
                    finish();
                }
            });
            bindProfileView();
        }
        Log.e(TAG,"onCreate end 1");
        overridePendingTransition(0, 0);
        Log.e(TAG,"onCreate end");
    }

    protected final void setAdditionalTargets(Intent[] intents) {
        if (intents != null) {
            for (Intent intent : intents) {
                mIntents.add(intent);
            }
        }
    }
    
    private String getReferrerPackageName() {
        final Uri referrer = (Uri)ReflectionUtils.invokeMethod(this, "getReferrer", null, null);
        if (referrer != null && "android-app".equals(referrer.getScheme())) {
            return referrer.getHost();
        }
        return null;
    }

    void bindProfileView() {
        final DisplayResolveInfo dri = mAdapter.getOtherProfile();
        if (dri != null) {
            mProfileView.setVisibility(View.VISIBLE);
            final TextView text = (TextView) mProfileView.findViewById(R.id.cyee_profile_button);
            text.setText(dri.getDisplayLabel());
        } else {
            mProfileView.setVisibility(View.GONE);
        }
    }

    private void setProfileSwitchMessageId(int contentUserHint) {
        if (contentUserHint != UserHandle.USER_CURRENT &&
                contentUserHint != UserHandle.myUserId()) {
            UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
            UserInfo originUserInfo = userManager.getUserInfo(contentUserHint);
            boolean originIsManaged = originUserInfo != null && originUserInfo.isManagedProfile();
            boolean targetIsManaged = userManager.isManagedProfile();
            if (originIsManaged && !targetIsManaged) {
                mProfileSwitchMessageId = com.android.internal.R.string.forward_intent_to_owner;
            } else if (!originIsManaged && targetIsManaged) {
                mProfileSwitchMessageId = com.android.internal.R.string.forward_intent_to_work;
            }
        }
    }

    private boolean isCTS(Intent intent) {
       if (intent == null) return false;
       Parcelable targetParcelable = intent.getParcelableExtra(Intent.EXTRA_INTENT);
       if (targetParcelable == null || !(targetParcelable instanceof Intent)) return false;
       String action = ((Intent)targetParcelable).getAction();
        return "com.android.cts.verifier.managedprovisioning.CROSS_PROFILE_TO_WORK".equals(action);
    }

    /**
     * Turn on launch mode that is safe to use when forwarding intents received from
     * applications and running in system processes.  This mode uses Activity.startActivityAsCaller
     * instead of the normal Activity.startActivity for launching the activity selected
     * by the user.
     *
     * <p>This mode is set to true by default if the activity is initialized through
     * {@link #onCreate(android.os.Bundle)}.  If a subclass calls one of the other onCreate
     * methods, it is set to false by default.  You must set it before calling one of the
     * more detailed onCreate methods, so that it will be set correctly in the case where
     * there is only one intent to resolve and it is thus started immediately.</p>
     */
    public void setSafeForwardingMode(boolean safeForwarding) {
        mSafeForwardingMode = safeForwarding;
    }
    
    void resizeGrid() {
        //final int itemCount = mAdapter.getCount();
        //mGrid.setNumColumns(Math.min(itemCount, mMaxColumns));
    }

    Drawable getIcon(Resources res, int resId) {
        Drawable result;
        try {
            result = res.getDrawableForDensity(resId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            result = null;
        }

        return result;
    }

    Drawable loadIconForResolveInfo(ResolveInfo ri) {
        Drawable dr;
        try {
            if (ri.resolvePackageName != null && ri.icon != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            final int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return ri.loadIcon(mPm);
    }

    @Override
    protected void onRestart() {
        Log.e(TAG, "onRestart start");
        super.onRestart();
        if (!mRegistered) {
            mPackageMonitor.register(this, getMainLooper(), false);
            mRegistered = true;
        }
        if (mProfileView != null) {
            bindProfileView();
        }
        Log.e(TAG, "onRestart end");
        //mAdapter.handlePackagesChanged();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy start ");
        if (mRegistered) {
            mPackageMonitor.unregister();
            mRegistered = false;
        }
        ChameleonColorManager.getInstance().onDestroy(this);
        ChameleonColorManager.getInstance().unregister();
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mAlwaysUseOption) {
            final int checkedPos = mAutoMaticGridView.getCheckedItemPosition();
            final boolean enabled = checkedPos != GridView.INVALID_POSITION;
            mLastSelected = checkedPos;
            mAlwaysButton.setEnabled(enabled);
            mOnceButton.setEnabled(enabled);
            if (enabled) {
                mAutoMaticGridView.setItemChecked(checkedPos);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int checkedPos = mAutoMaticGridView.getCheckedItemPosition();
        final boolean hasValidSelection = checkedPos != GridView.INVALID_POSITION;
        if (mAlwaysUseOption && (!hasValidSelection || mLastSelected != checkedPos)) {
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                //mGrid.smoothScrollToPosition(checkedPos);
            }
            mLastSelected = checkedPos;
        } else {
            startSelected(position, false);
        }
    }

    private boolean hasManagedProfile() {
        UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
        if (userManager == null) {
            return false;
        }

        try {
            List<UserInfo> profiles = userManager.getProfiles(getUserId());
            for (UserInfo userInfo : profiles) {
                if (userInfo != null && userInfo.isManagedProfile()) {
                    return true;
                }
            }
        } catch (SecurityException e) {
            return false;
        }
        return false;
    }

    private boolean supportsManagedProfiles(ResolveInfo resolveInfo) {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
                    resolveInfo.activityInfo.packageName, 0 /* default flags */);
            return appInfo.targetSdkVersion >= Build.VERSION_CODES.LOLLIPOP;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public void onButtonClick(View v) {
        final int id = v.getId();
        final int checkedIndex = mAutoMaticGridView.getCheckedItemPosition();
        startSelected(checkedIndex, id == R.id.cyee_button_always);
        final Intent intent = mAdapter.targetInfoForPosition(checkedIndex, false).getResolvedIntent();
        String target = intent.getComponent().getPackageName();
        if (!RO_GN_CYEE_CLONE_SUPPORT || !isDualInstanceApp(target) ||
            getWechatAvatar(this.getContentResolver(), "display_wechat_avatar", 0) == 0) {
            dismiss();
        }
    }

    private int mPosIndex = 0;
    private boolean mAlways = false;
    
    void startSelected(final int which, final boolean always) {
        final ResolveInfo ri = mAdapter.targetInfoForPosition(which, false).getResolveInfo();
        final Intent intent = mAdapter.targetInfoForPosition(which, false).getResolvedIntent();
        if (mResolvingHome && hasManagedProfile() && !supportsManagedProfiles(ri)) {
            Toast.makeText(this, String.format(getResources().getString(
                    com.android.internal.R.string.activity_resolver_work_profiles_support),
                    ri.activityInfo.loadLabel(getPackageManager()).toString()),
                    Toast.LENGTH_LONG).show();
            return;
        }        

        //Gionee <dual_weixin> <liuran> <Dec 28, 2015> add for  begin
        String target = intent.getComponent().getPackageName();
       	mAlways = always;
        Log.v("TAG", "select:" + target);
        boolean shouldDealAsDualApp = RO_GN_CYEE_CLONE_SUPPORT && isDualInstanceApp(target) &&
            getWechatAvatar(this.getContentResolver(), "display_wechat_avatar", 0) != 0;
        if (shouldDealAsDualApp){
                WindowManager.LayoutParams lp = getWindow().getAttributes(); 
                lp.alpha = 0f; 
                getWindow().setAttributes(lp);
                mPosIndex = which;
                Intent dualChooser = new Intent("android.intent.action.DUAL_INSTANCE_CHOOSER");
                dualChooser.putExtra("CLONE_PKG_NAME", target) ;
                startActivityForResult(dualChooser, REQ_CLONE_CHOOSE);
        }
        //Gionee <dual_weixin> <liuran> <Dec 28, 2015> add for  end
        else {
            onIntentSelected(ri, intent, always);
            //Gionee <hanbj> <20140523> add for CR01269828 begin
            sendCyeeIntent(which,always);
            //Gionee <hanbj> <20140523> add for CR01269828 end
            finish();
        }
    }

    private int getWechatAvatar(ContentResolver resolver, String name, int defaultValue) {
        int result = 0;

        try {
            Class cls = Class.forName("cyee.provider.CyeeSettings");
            Method method = cls.getMethod("getInt", ContentResolver.class, String.class, int.class);
            result = (int) method.invoke(CyeeResolverActivity.class, resolver, name, defaultValue);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    //Gionee <hanbj> <20140523> add for CR01269828 begin
    void sendCyeeIntent(int which, boolean always) {
        if (!always || mAdapter.mDisplayList == null || mAdapter.mDisplayList.size() <= 0) {
            return;
        }
        
        Intent intent = new Intent("com.gionee.intent.action.SET_DEFAULT_APP_FROM_FRAMEWORK");
        DisplayResolveInfo dri = mAdapter.mDisplayList.get(which);
        String pkgName = dri.getResolvedComponentName().getPackageName();
        intent.putExtra("packagename", pkgName);
        Log.w("CyeeResolverActivity : sendCyeeIntent packagename = ", pkgName);
        sendBroadcast(intent);
    }
    //Gionee <hanbj> <20140523> add for CR01269828 end

    boolean shouldGetActivityMetadata() {
        return false;
    }
    
    protected void onIntentSelected(ResolveInfo ri, Intent intent, boolean alwaysCheck) {
        if (alwaysCheck) {
            // Build a reasonable intent filter, based on what matched.
            IntentFilter filter = new IntentFilter();

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

            int cat = ri.match&IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = intent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = intent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Log.w("CyeeResolverActivity", e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                // We need the data specification if there was no type,
                // OR if the scheme is not one of our magical "file:"
                // or "content:" schemes (see IntentFilter for the reason).
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                                && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());
    
                    // Look through the resolved filter to determine which part
                    // of it matched the original Intent.
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
                    Iterator<PatternMatcher> pIt = ri.filter.pathsIterator();
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
                final int N = mAdapter.mDisplayList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i=0; i<N; i++) {
                    ResolveInfo r = mAdapter.mDisplayList.get(i).getResolveInfo();
                    set[i] = new ComponentName(r.activityInfo.packageName,
                            r.activityInfo.name);
                    if (r.match > bestMatch) bestMatch = r.match;
                }
                getPackageManager().addPreferredActivity(filter, bestMatch, set,
                        intent.getComponent());
            }
            // Gionee <huangwenting> <2013-9-25> add for CR00907723 begin
            for (int i=0; i<mAdapter.getCount(); i++) {
                if (mAdapter.mDisplayList.get(i).getResolveInfo().activityInfo.packageName.equals("com.gionee.navil")) {
                    Intent systemuiintent = new Intent("com.gionee.systemui.broadcast.default.launcher");
                    systemuiintent.putExtra("visible", !ri.activityInfo.packageName.equals("com.gionee.navil"));
                    sendBroadcast(systemuiintent);
                    break;
                }
            }
            // Gionee <huangwenting> <2013-9-25> add for CR00907723 end

        }

        if (intent != null) {
            Log.d(TAG, "onIntentSelected mSafeForwardingMode="
                    + mSafeForwardingMode);
            callStrictModeMethod("disableDeathOnFileUriExposure");
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                callStrictModeMethod("enableDeathOnFileUriExposure");
            }

        }
    }

    // Gionee <weidong> <2016-12-12> add for 30400 start
    private void callStrictModeMethod(String methodName) {
        Class<?> verClass = null;

        try {
            verClass = Class.forName("android.os.StrictMode");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Method method = verClass.getMethod(methodName);
            method.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Gionee <weidong> <2016-12-12> add for 30400 end
    
    void showAppDetails(ResolveInfo ri) {
        Intent in = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
                .setData(Uri.fromParts("package", ri.activityInfo.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    /*
     * weidong modified
     * private final class DisplayResolveInfo {
        ResolveInfo ri;
        CharSequence displayLabel;
        Drawable displayIcon;
        CharSequence extendedInfo;
        Intent origIntent;

        DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                CharSequence pInfo, Intent pOrigIntent) {
            ri = pri;
            displayLabel = pLabel;
            extendedInfo = pInfo;
            origIntent = pOrigIntent;
        }
    }*/
    
    public Intent getTargetIntent() {
        return mIntents.isEmpty() ? null : mIntents.get(0);
    }

    /**
     * Check a simple match for the component of two ResolveInfos.
     */
    static boolean resolveInfoMatch(ResolveInfo lhs, ResolveInfo rhs) {
        return lhs == null ? rhs == null
                : lhs.activityInfo == null ? rhs.activityInfo == null
                : Objects.equals(lhs.activityInfo.name, rhs.activityInfo.name)
                && Objects.equals(lhs.activityInfo.packageName, rhs.activityInfo.packageName);
    }
    
    /**
     * Replace me in subclasses!
     */
    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        return defIntent;
    }
    
    ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents,
            Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid,
            boolean filterLastUsed) {
        return new ResolveListAdapter(context, payloadIntents, initialIntents, rList,
                launchedFromUid, filterLastUsed);
    }
    
    
    class ResolveListAdapter extends CyeeBaseAutoAdapter {
        
        private static final String TAG = "ResolveListAdapter";

        private final Intent[] mInitialIntents;
        private final List<ResolveInfo> mBaseResolveList;
        private final List<Intent> mIntents;
        private final int mLaunchedFromUid;
        private final LayoutInflater mInflater;

        private List<ResolveInfo> mCurrentResolveList;
        private final List<DisplayResolveInfo> mDisplayList;
        List<ResolvedComponentInfo> mOrigResolveList;
        
        private ResolveInfo mLastChosen;
        private int mLastChosenPosition = -1;
        private boolean mFilterLastUsed;
        private DisplayResolveInfo mOtherProfile;
        private boolean mHasExtendedInfo;
        
        public DisplayResolveInfo getOtherProfile() {
            return mOtherProfile;
        }
        
        
        public ResolveListAdapter(Context context, List<Intent> payloadIntents,
                Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid,boolean filterLastUsed) {
            mIntents = payloadIntents;
            mInitialIntents = initialIntents;
            mBaseResolveList = rList;
            mLaunchedFromUid = launchedFromUid;
            mInflater = LayoutInflater.from(context);
            mDisplayList = new ArrayList<>();
            mFilterLastUsed = filterLastUsed;
            Log.v(TAG, "launchedFromUid:" + launchedFromUid + " filterLastUsed:" + filterLastUsed);
            rebuildList();
        }

        public void handlePackagesChanged() {
            final int oldItemCount = getCount();
            rebuildList();
            notifyDataSetChanged();
            final int newItemCount = getCount();
            // / M: [ALPS00344799] The JE pops up when you press Power key
            if (newItemCount == 0) {
                // We no longer have any items... just finish the activity.
                finish();
            } else if (newItemCount != oldItemCount) {
                if (mAlwaysUseOption) {
                    final int checkedPos = mAutoMaticGridView.getCheckedItemPosition();
                    final boolean enabled = checkedPos != GridView.INVALID_POSITION;
                    if (enabled && checkedPos >= newItemCount) {
                        Log.w("ResolverActivity",
                                "handlePackagesChanged: checkedPos "
                                        + checkedPos + " >= newItemCount "
                                        + newItemCount + ", disable buttons");
                        mAlwaysButton.setEnabled(false);
                        mOnceButton.setEnabled(false);
                    }
                }
                // / @}
                //resizeGrid();
            }
        }

        private void rebuildList() {
            List<ResolvedComponentInfo> currentResolveList = null;
            try {
                final Intent primaryIntent = getTargetIntent();
                mLastChosen = AppGlobals.getPackageManager().getLastChosenActivity(
                        primaryIntent, primaryIntent.resolveTypeIfNeeded(getContentResolver()),
                        PackageManager.MATCH_DEFAULT_ONLY);
                if (mLastChosen != null) {
                    Log.v(TAG, "mLastChosen  " + mLastChosen.activityInfo.packageName + ":" + mLastChosen.activityInfo.name);
                } 
            } catch (RemoteException re) {
                Log.d(TAG, "Error calling setLastChosenActivity\n" + re);
            }
            
            // Clear the value of mOtherProfile from previous call.
            mOtherProfile = null;
            // Gionee <weidong> <2016-3-24> modify for CR01651319 begin
            if (null != mDisplayList) {
                mDisplayList.clear();
            }
            // Gionee <weidong> <2016-3-24> modify for CR01651319 end
            if (mBaseResolveList != null) {
                currentResolveList = mOrigResolveList = new ArrayList<>();
                addResolveListDedupe(currentResolveList, getTargetIntent(), mBaseResolveList);
            } else {
                
                final boolean shouldGetResolvedFilter = shouldGetResolvedFilter();
                final boolean shouldGetActivityMetadata = shouldGetActivityMetadata();
                
                for (int i = 0, N = mIntents.size(); i < N; i++) {
                    final Intent intent = mIntents.get(i);
                    intent.setComponent(null);
                    final List<ResolveInfo> infos = mPm.queryIntentActivities(intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                            | (shouldGetResolvedFilter ? PackageManager.GET_RESOLVED_FILTER : 0)
                            | (shouldGetActivityMetadata ? PackageManager.GET_META_DATA : 0));
                    if (infos != null) {
                        if (currentResolveList == null) {
                            currentResolveList = mOrigResolveList = new ArrayList<>();
                        }
                        addResolveListDedupe(currentResolveList, intent, infos);
                    }
                }
                
                // Filter out any activities that the launched uid does not
                // have permission for.  We don't do this when we have an explicit
                // list of resolved activities, because that only happens when
                // we are being subclassed, so we can safely launch whatever
                // they gave us.
                if (currentResolveList != null) {
                    for (int i=currentResolveList.size()-1; i >= 0; i--) {
                        ResolvedComponentInfo info;
                        ActivityInfo ai = currentResolveList.get(i).getResolveInfoAt(0).activityInfo;
                        Log.v(TAG, "currentResolveList  " + currentResolveList.get(i).name);
                        int granted = ActivityManager.checkComponentPermission(
                                ai.permission, mLaunchedFromUid,
                                ai.applicationInfo.uid, ai.exported);
                        if (granted != PackageManager.PERMISSION_GRANTED) {
                         // Access not allowed!
                            if (mOrigResolveList == currentResolveList) {
                                mOrigResolveList = new ArrayList<>(mOrigResolveList);
                            }
                            Log.v(TAG, "remove from currentResolveList because of uid " + currentResolveList.get(i).name);
                            currentResolveList.remove(i);
                        }
                    }
                }
            }
            int N;
            if ((currentResolveList != null) && ((N = currentResolveList.size()) > 0)) {
             // Only display the first matches that are either of equal
                // priority or have asked to be default options.
                ResolvedComponentInfo rci0 = currentResolveList.get(0);
                ResolveInfo r0 = rci0.getResolveInfoAt(0);
                for (int i=1; i<N; i++) {
                    ResolveInfo ri = currentResolveList.get(i).getResolveInfoAt(0);
                    if (true) Log.v(
                        "ResolveListActivity",
                        r0.activityInfo.name + "=" +
                        r0.priority + "/" + r0.isDefault + " vs " +
                        ri.activityInfo.name + "=" +
                        ri.priority + "/" + ri.isDefault);
                   if (r0.priority != ri.priority ||
                        r0.isDefault != ri.isDefault) {
                        while (i < N) {
                            if (mOrigResolveList == currentResolveList) {
                                mOrigResolveList = new ArrayList<>(mOrigResolveList);
                            }
                            Log.v(TAG, "remove from currentResolveList because of priority:" + currentResolveList.get(i).name);
                            currentResolveList.remove(i);
                            N--;
                        }
                        Log.v(TAG, "N:" + N);
                    }
                }
                Log.v(TAG,"before Collections.sort");
                //Gionee:zhang_xin 2012-10-23 modify for CR00717539 start
                if (!mIsSupportAppSort) {
                    if (N > 1) {
                        // ResolveInfo.DisplayNameComparator rComparator =
                        // new ResolveInfo.DisplayNameComparator(mPm);
                        //mResolverComparator.compute(currentResolveList);
                        Collections.sort(currentResolveList,
                               mResolverComparator);
                    }
                }
                //Gionee:zhang_xin 2012-10-23 modify for CR00717539 end
                Log.v(TAG,"after Collections.sort");
//                /// M @{
//                mRCSePriorityExt = MPlugin.createInstance(IRCSePriorityExt.class.getName(), CyeeResolverActivity.this);
//                Log.d(TAG , "RCSe Plugin initiated " + mRCSePriorityExt);
//                if (mRCSePriorityExt != null) {
//                    for (ResolvedComponentInfo info : currentResolveList) {
//                        int count = info.getCount();
//                        boolean found = false;
//                        for (int index = 0; index < count; index++) {
//                            ApplicationInfo applicationInfo = info.getResolveInfoAt(index)
//                                .activityInfo.applicationInfo;
//                            if (applicationInfo.packageName.equals("com.orangelabs.rcs")) {
//                                Log.d(TAG, "rebuild list after sort");
//                                currentResolveList.remove(info);
//                                currentResolveList.add(0, info);
//                                found = true;
//                                break;
//                            }
//                        }
//                        if (found == true) {
//                            break;
//                        }
//                    }
//                }
//                ArrayList<String> packageNames = new ArrayList<String>();
                /// M @}
                
                // First put the initial items at the top.
                if (mInitialIntents != null) {
                    for (int i=0; i<mInitialIntents.length; i++) {
                        Intent ii = mInitialIntents[i];
                        if (ii == null) {
                            continue;
                        }
                        ActivityInfo ai = ii.resolveActivityInfo(
                                getPackageManager(), 0);
                        if (ai == null) {
                            Log.w("CyeeResolverActivity", "No activity found for "
                                    + ii);
                            continue;
                        }
                        ResolveInfo ri = new ResolveInfo();
                        ri.activityInfo = ai;
                        UserManager userManager =
                                (UserManager) getSystemService(Context.USER_SERVICE);
                        if (ii instanceof LabeledIntent) {
                            LabeledIntent li = (LabeledIntent)ii;
                            ri.resolvePackageName = li.getSourcePackage();
                            ri.labelRes = li.getLabelResource();
                            ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                            ri.icon = li.getIconResource();
//                            ri.iconResourceId = ri.icon;
                            
                          /// M @{
//                            if (mRCSePriorityExt != null) {
//                               //Add package names in list to resort on RCSe basis
//                               packageNames.add(li.getSourcePackage());
//                            }
                            /// M @}
                        }
                        if (userManager.isManagedProfile()) {
                            ri.noResourceId = true;
                            ri.icon = 0;
                        }
                        addResolveInfo(new DisplayResolveInfo(ii, ri,
                                ri.loadLabel(getPackageManager()), null, ii));
                        Log.v(TAG, "add initialIntent " + ri.activityInfo.packageName + ":" + ri.activityInfo.name);
                    }
                }
                
              /// M @{
//                if (mRCSePriorityExt != null) {
//                    int rcseIndex = -1;
//                    Log.d(TAG, "mRCSePriorityExt to sort the list");
//                    //Resort the share list and add RCSe on top
//                    rcseIndex = mRCSePriorityExt.sortTheListForRCSe(packageNames);
//                    if (rcseIndex != -1) {
//                        Log.d(TAG, "mRCSePriorityExt to sort the list index is" + rcseIndex);
//                        DisplayResolveInfo rcseInfo = mDisplayList.get(rcseIndex);
//                        mDisplayList.remove(rcseIndex);
//                        mDisplayList.add(0, rcseInfo);
//                    }
//                }
                /// M @}
                
                
                // Check for applications with same name and use application name or
                // package name if necessary
                rci0 = currentResolveList.get(0);
                r0 = rci0.getResolveInfoAt(0);
                int start = 0;
                CharSequence r0Label =  r0.loadLabel(mPm);
                mHasExtendedInfo = false;
                
                Log.v(TAG,"processGroup before");
                
                for (int i = 1; i < N; i++) {
                    if (r0Label == null) {
                        r0Label = r0.activityInfo.packageName;
                    }
                    ResolvedComponentInfo rci = currentResolveList.get(i);
                    ResolveInfo ri = rci.getResolveInfoAt(0);
                    CharSequence riLabel = ri.loadLabel(mPm);
                    
                    if (riLabel == null) {
                        riLabel = ri.activityInfo.packageName;
                    }
                    if (riLabel.equals(r0Label)) {
                        continue;
                    }
                    Log.v(TAG, "process the same applications:" + start + "~" + (i-1));
                    processGroup(currentResolveList, start, (i-1), rci0, r0Label);
                    rci0 = rci;
                    r0 = ri;
                    r0Label = riLabel;
                    start = i;
                }
                // Process last group
                processGroup(currentResolveList, start, (N-1), rci0, r0Label);
            }
            Log.v(TAG,"processGroup after");
         // Layout doesn't handle both profile button and last chosen
            // so disable last chosen if profile button is present.
            if (mOtherProfile != null && mLastChosenPosition >= 0) {
                mLastChosenPosition = -1;
                mFilterLastUsed = false;
            }

            onListRebuilt();
            
        }
        
        private void addResolveListDedupe(List<ResolvedComponentInfo> into, Intent intent,
                List<ResolveInfo> from) {
            final int fromCount = from.size();
            final int intoCount = into.size();
            for (int i = 0; i < fromCount; i++) {
                final ResolveInfo newInfo = from.get(i);
                boolean found = false;
                // Only loop to the end of into as it was before we started; no dupes in from.
                for (int j = 0; j < intoCount; j++) {
                    final ResolvedComponentInfo rci = into.get(i);
                    if (isSameResolvedComponent(newInfo, rci)) {
                        found = true;
                        rci.add(intent, newInfo);
                        break;
                    }
                }
                if (!found) {
                    into.add(new ResolvedComponentInfo(new ComponentName(
                            newInfo.activityInfo.packageName, newInfo.activityInfo.name),
                            intent, newInfo));
                }
            }
        }
        
        private boolean isSameResolvedComponent(ResolveInfo a, ResolvedComponentInfo b) {
            final ActivityInfo ai = a.activityInfo;
            return ai.packageName.equals(b.name.getPackageName())
                    && ai.name.equals(b.name.getClassName());
        }
        
        public void onListRebuilt() {
            // This space for rent
        }
        
        public boolean shouldGetResolvedFilter() {
            return mFilterLastUsed;
        }
        
        private void processGroup(List<ResolvedComponentInfo> rList, int start, int end, ResolvedComponentInfo ro,
                CharSequence roLabel) {
            // Process labels from start to i
            int num = end - start+1;
            if (num == 1) {
             // No duplicate labels. Use label for entry at start
                addResolveInfoWithAlternates(ro, null, roLabel);
            } else {
                mHasExtendedInfo = true;
                boolean usePkg = false;
                CharSequence startApp = ro.getResolveInfoAt(0).activityInfo.applicationInfo.loadLabel(mPm);
                if (startApp == null) {
                    usePkg = true;
                }
                if (!usePkg) {
                    // Use HashSet to track duplicates
                    HashSet<CharSequence> duplicates =
                        new HashSet<CharSequence>();
                    duplicates.add(startApp);
                    for (int j = start+1; j <= end ; j++) {
                        ResolveInfo jRi = rList.get(j).getResolveInfoAt(0);
                        CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(mPm);
                        if ( (jApp == null) || (duplicates.contains(jApp))) {
                            usePkg = true;
                            break;
                        } else {
                            duplicates.add(jApp);
                        }
                    }
                    // Clear HashSet for later use
                    duplicates.clear();
                }
                for (int k = start; k <= end; k++) {
                    final ResolvedComponentInfo rci = rList.get(k);
                    final ResolveInfo add = rci.getResolveInfoAt(0);
                    final CharSequence extraInfo;
                    if (usePkg) {
                        // Use application name for all entries from start to end-1
                        extraInfo = add.activityInfo.packageName;
                    } else {
                        // Use package name for all entries from start to end-1
                        extraInfo = add.activityInfo.applicationInfo.loadLabel(mPm);
                    }
                    Log.v(TAG, "add the same group : " + rci.name);
                    addResolveInfoWithAlternates(rci, extraInfo, roLabel);
                }
            }
        }

        private void addResolveInfoWithAlternates(ResolvedComponentInfo rci,
                CharSequence extraInfo, CharSequence roLabel) {
            final int count = rci.getCount();
            final Intent intent = rci.getIntentAt(0);
            final ResolveInfo add = rci.getResolveInfoAt(0);
            final Intent replaceIntent = getReplacementIntent(add.activityInfo, intent);
            final DisplayResolveInfo dri = new DisplayResolveInfo(intent, add, roLabel,
                    extraInfo, replaceIntent);
            addResolveInfo(dri);
            if (replaceIntent == intent) {
                // Only add alternates if we didn't get a specific replacement from
                // the caller. If we have one it trumps potential alternates.
                for (int i = 1, N = count; i < N; i++) {
                    final Intent altIntent = rci.getIntentAt(i);
                    dri.addAlternateSourceIntent(altIntent);
                }
            }
            updateLastChosenPosition(add);
        }
        
        private void updateLastChosenPosition(ResolveInfo info) {
            if (mLastChosen != null
                    && mLastChosen.activityInfo.packageName.equals(info.activityInfo.packageName)
                    && mLastChosen.activityInfo.name.equals(info.activityInfo.name)) {
                mLastChosenPosition = mDisplayList.size() - 1;
            }
        }
        
        private void addResolveInfo(DisplayResolveInfo dri) {
            if (dri.mResolveInfo.targetUserId != UserHandle.USER_CURRENT && mOtherProfile == null) {
                // So far we only support a single other profile at a time.
                // The first one we see gets special treatment.
                mOtherProfile = dri;
            } else {
                mDisplayList.add(dri);
            }
        }
        
        public ResolveInfo resolveInfoForPosition(int position, boolean filtered) {
            return ((TargetInfo)(filtered ? getItem(position) : mDisplayList.get(position)))
                    .getResolveInfo();
        }

        public TargetInfo targetInfoForPosition(int position, boolean filtered) {
            return (TargetInfo) (filtered ? getItem(position) : mDisplayList.get(position));
        }
        
        public int getCount() {
            int result = mDisplayList.size();
            if (mFilterLastUsed && mLastChosenPosition >= 0) {
                result--;
            }
            return result;
        }
        
        public int getUnfilteredCount() {
            return mDisplayList.size();
        }

        public int getDisplayInfoCount() {
            return mDisplayList.size();
        }
        
        public Object getItem(int position) {
            if (mFilterLastUsed && mLastChosenPosition >= 0 && position >= mLastChosenPosition) {
                position++;
            }
            return mDisplayList.get(position);
        }

        public boolean hasExtendedInfo() {
            return mHasExtendedInfo;
        }
        
        public boolean hasResolvedTarget(ResolveInfo info) {
            for (int i = 0, N = mDisplayList.size(); i < N; i++) {
                if (resolveInfoMatch(info, mDisplayList.get(i).getResolveInfo())) {
                    return true;
                }
            }
            return false;
        }

        protected int getDisplayResolveInfoCount() {
            return mDisplayList.size();
        }
        
        protected DisplayResolveInfo getDisplayResolveInfo(int index) {
            // Used to query services. We only query services for primary targets, not alternates.
            return mDisplayList.get(index);
        }
        
        public boolean showsExtendedInfo(TargetInfo info) {
            return !TextUtils.isEmpty(info.getExtendedInfo());
        }
        
//        public Intent intentForPosition(int position) {
//            if (mDisplayList == null) {
//                return null;
//            }
//
//            DisplayResolveInfo dri = mDisplayList.get(position);
//            
//            Intent intent = new Intent(dri.origIntent != null
//                    ? dri.origIntent : mIntent);
//            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
//                    |Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
//            ActivityInfo ai = dri.ri.activityInfo;
//            intent.setComponent(new ComponentName(
//                    ai.applicationInfo.packageName, ai.name));
//            return intent;
//        }

        public long getItemId(int position) {
            return position;
        }

        public View getItemView(int position, ViewGroup parent) {
            View view;
            //if (convertView == null) {
            	// Gionee fengjianyi 2012-12-26 modify for CR00751916 start
                //view = mInflater.inflate(
                //        com.android.internal.R.layout.resolve_list_item, parent, false);
                	if (isLightTheme()) {
                        view = mInflater.inflate(R.layout.cyee_resolve_list_item_light, parent, false);
                	} else {
                        view = mInflater.inflate(R.layout.cyee_resolve_list_item_dark, parent, false);
                	}

            	// Gionee fengjianyi 2012-12-26 modify for CR00751916 end

                // Fix the icon size even if we have different sized resources
                ImageView icon = (ImageView)view.findViewById(com.android.internal.R.id.icon);
                ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) icon.getLayoutParams();
                lp.width = lp.height = mIconSize;
            //} else {
            //    view = convertView;
            //}
            onBindView(view, (TargetInfo) getItem(position));
            
            return view;
        }

        public final void bindView(int position, View view) {
            onBindView(view, (TargetInfo)getItem(position));
        }
        
        private final void onBindView(View view, TargetInfo info) {
            TextView text = (TextView)view.findViewById(com.android.internal.R.id.text1);
            TextView text2 = (TextView)view.findViewById(com.android.internal.R.id.text2);
            ImageView icon = (ImageView)view.findViewById(com.android.internal.R.id.icon);
            text.setText(info.getDisplayLabel());
            if (mHasExtendedInfo) {
                text2.setVisibility(View.VISIBLE);
                text2.setText(info.getExtendedInfo());
            } else {
                text2.setVisibility(View.GONE);
            }
            if (info instanceof DisplayResolveInfo
                    && !((DisplayResolveInfo) info).hasDisplayIcon()) {
                new LoadAdapterIconTask((DisplayResolveInfo) info, icon).execute();
                Log.v(TAG, "performance--- load icon" + info.getDisplayLabel());
            } else {
                icon.setImageDrawable(info.getDisplayIcon());
            }
        }
        
        public DisplayResolveInfo getDisplayInfoAt(int index) {
            return mDisplayList.get(index);
        }
        
    }

    class LoadAdapterIconTask extends LoadIconTask {
        public LoadAdapterIconTask(DisplayResolveInfo dri, ImageView icon) {
            super(dri, icon);
        }

        @Override
        protected void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            if (mProfileView != null && mAdapter.getOtherProfile() == mDisplayResolveInfo) {
                bindProfileView();
            }
            //mAdapter.notifyDataSetChanged();
        }
    }
    
    abstract class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        protected final DisplayResolveInfo mDisplayResolveInfo;
        private final ResolveInfo mResolveInfo;
        private final ImageView mIcon;

        public LoadIconTask(DisplayResolveInfo dri, ImageView icon) {
            mDisplayResolveInfo = dri;
            mResolveInfo = dri.getResolveInfo();
            mIcon = icon;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            /// M: Add log for ALPS01628038
            Log.v(TAG, "LoadIconTask doInBackground Enter:");
            Drawable icon = mResolverComparator.getSpecialAppIcon(CyeeResolverActivity.this, mResolveInfo);
            if (icon == null) {
                icon = loadIconForResolveInfo(mResolveInfo);
            }
            /// M: Add log for ALPS01628038
            Log.v(TAG, "LoadIconTask doInBackground Leave: " + icon);

            return icon;
        }

        @Override
        protected void onPostExecute(Drawable d) {
            mDisplayResolveInfo.setDisplayIcon(d);
            mIcon.setImageDrawable(d);        }
    }

    
    
    class ItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //Gionee <bug> <hanbj> <20140415> add for CR01173233 begin
            finish();
            //Gionee <bug> <hanbj> <20140415> add for CR01173233 end
            ResolveInfo ri = mAdapter.resolveInfoForPosition(position, true);//mAdapter.getDisplayInfoAt(position).getResolveInfo();
            showAppDetails(ri);
            return true;
        }

    }

	// Gionee fengjianyi 2012-12-26 add for CR00751916 start
    private boolean isLightTheme() {
            return true;
    }
	// Gionee fengjianyi 2012-12-26 add for CR00751916 end
    
    
    //Gionee <dual_weixin> <liuran> <Dec 29, 2015> add for  begin
    private final static int REQ_CLONE_CHOOSE = 0;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CLONE_CHOOSE:
                if (resultCode == RESULT_OK) {
                    boolean selectClone = false;
                    if(data != null){
                        selectClone  = data.getBooleanExtra("KEY_SEL_CLONE", false);
                    }		
                    Log.v("ARA", "clone choosen=" + selectClone);
                    ResolveInfo ri = mAdapter.resolveInfoForPosition(mPosIndex,false);
                    Intent intent = ((TargetInfo)(mAdapter.getItem(mPosIndex))).getResolvedIntent();
                    if(selectClone){
                        intent.addCategory("android.intent.category.CLONED");
                    }
                    onIntentSelected(ri, intent, mAlways);
                    //Gionee <hanbj> <20140523> add for CR01269828 begin
                    sendCyeeIntent(mPosIndex,mAlways);
                    //Gionee <hanbj> <20140523> add for CR01269828 end
                }
                finish();
                break;
            default:
                break;
	}
    }
    
    
    private boolean isDualInstanceApp(String pkg){
    	PackageManager pm = getPackageManager();
    	List<PackageInfo> dualPackageList =  pm.getInstalledPackages(0x10000000);
    	
    	if(dualPackageList != null && dualPackageList.size() > 0){
    		
    		for(PackageInfo pi : dualPackageList){
    			if(pi.packageName.equals(pkg)){
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
	//Gionee <dual_weixin> <liuran> <Dec 29, 2015> add for  end
 
    static final boolean isSpecificUriMatch(int match) {
        match = match&IntentFilter.MATCH_CATEGORY_MASK;
        return match >= IntentFilter.MATCH_CATEGORY_HOST
                && match <= IntentFilter.MATCH_CATEGORY_PATH;
    }
    
    
    /**
     * A single target as represented in the chooser.
     */
    public interface TargetInfo {
        /**
         * Get the resolved intent that represents this target. Note that this may not be the
         * intent that will be launched by calling one of the <code>start</code> methods provided;
         * this is the intent that will be credited with the launch.
         *
         * @return the resolved intent for this target
         */
        Intent getResolvedIntent();

        /**
         * Get the resolved component name that represents this target. Note that this may not
         * be the component that will be directly launched by calling one of the <code>start</code>
         * methods provided; this is the component that will be credited with the launch.
         *
         * @return the resolved ComponentName for this target
         */
        ComponentName getResolvedComponentName();

        /**
         * Start the activity referenced by this target.
         *
         * @param activity calling Activity performing the launch
         * @param options ActivityOptions bundle
         * @return true if the start completed successfully
         */
        boolean start(AppCompatActivity activity, Bundle options);

        /**
         * Start the activity referenced by this target as if the ResolverActivity's caller
         * was performing the start operation.
         *
         * @param activity calling Activity (actually) performing the launch
         * @param options ActivityOptions bundle
         * @param userId userId to start as or {@link UserHandle#USER_NULL} for activity's caller
         * @return true if the start completed successfully
         */
        boolean startAsCaller(AppCompatActivity activity, Bundle options, int userId);

        /**
         * Start the activity referenced by this target as a given user.
         *
         * @param activity calling activity performing the launch
         * @param options ActivityOptions bundle
         * @param user handle for the user to start the activity as
         * @return true if the start completed successfully
         */
        boolean startAsUser(AppCompatActivity activity, Bundle options, UserHandle user);

        /**
         * Return the ResolveInfo about how and why this target matched the original query
         * for available targets.
         *
         * @return ResolveInfo representing this target's match
         */
        ResolveInfo getResolveInfo();

        /**
         * Return the human-readable text label for this target.
         *
         * @return user-visible target label
         */
        CharSequence getDisplayLabel();

        /**
         * Return any extended info for this target. This may be used to disambiguate
         * otherwise identical targets.
         *
         * @return human-readable disambig string or null if none present
         */
        CharSequence getExtendedInfo();

        /**
         * @return The drawable that should be used to represent this target
         */
        Drawable getDisplayIcon();

        /**
         * @return The (small) icon to badge the target with
         */
        Drawable getBadgeIcon();

        /**
         * @return The content description for the badge icon
         */
        CharSequence getBadgeContentDescription();

        /**
         * Clone this target with the given fill-in information.
         */
        TargetInfo cloneFilledIn(Intent fillInIntent, int flags);

        /**
         * @return the list of supported source intents deduped against this single target
         */
        List<Intent> getAllSourceIntents();
    }
    
    void selfStartAsCaller(Intent intent, AppCompatActivity activity, Bundle options,
                           boolean flag, int userId) {

        Class<?>[] paramTypes;
        Object[] paramObjs;

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            paramTypes = new Class<?>[] { Intent.class, Bundle.class,
                    boolean.class, int.class };
            paramObjs = new Object[] { intent, options, flag, userId };
        } else {
            paramTypes = new Class<?>[] { Intent.class, Bundle.class, int.class };
            paramObjs = new Object[] { intent, options, userId };
        }
        ReflectionUtils.invokeMethod(activity, "startActivityAsCaller",
                paramTypes, paramObjs);
    }
    
    
    final class DisplayResolveInfo implements TargetInfo {
        private final ResolveInfo mResolveInfo;
        private final CharSequence mDisplayLabel;
        private Drawable mDisplayIcon;
        private final CharSequence mExtendedInfo;
        private final Intent mResolvedIntent;
        private final List<Intent> mSourceIntents = new ArrayList<>();

        DisplayResolveInfo(Intent originalIntent, ResolveInfo pri, CharSequence pLabel,
                CharSequence pInfo, Intent pOrigIntent) {
            mSourceIntents.add(originalIntent);
            mResolveInfo = pri;
            mDisplayLabel = pLabel;
            mExtendedInfo = pInfo;

            final Intent intent = new Intent(pOrigIntent != null ? pOrigIntent :
                    getReplacementIntent(pri.activityInfo, getTargetIntent()));
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                    | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            final ActivityInfo ai = mResolveInfo.activityInfo;
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));

            mResolvedIntent = intent;
        }

        private DisplayResolveInfo(DisplayResolveInfo other, Intent fillInIntent, int flags) {
            mSourceIntents.addAll(other.getAllSourceIntents());
            mResolveInfo = other.mResolveInfo;
            mDisplayLabel = other.mDisplayLabel;
            mDisplayIcon = other.mDisplayIcon;
            mExtendedInfo = other.mExtendedInfo;
            mResolvedIntent = new Intent(other.mResolvedIntent);
            mResolvedIntent.fillIn(fillInIntent, flags);
        }

        public ResolveInfo getResolveInfo() {
            return mResolveInfo;
        }

        public CharSequence getDisplayLabel() {
            return mDisplayLabel;
        }

        public Drawable getDisplayIcon() {
            return mDisplayIcon;
        }

        public Drawable getBadgeIcon() {
            return null;
        }

        @Override
        public CharSequence getBadgeContentDescription() {
            return null;
        }

        @Override
        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new DisplayResolveInfo(this, fillInIntent, flags);
        }

        @Override
        public List<Intent> getAllSourceIntents() {
            return mSourceIntents;
        }

        public void addAlternateSourceIntent(Intent alt) {
            mSourceIntents.add(alt);
        }

        public void setDisplayIcon(Drawable icon) {
            mDisplayIcon = icon;
        }

        public boolean hasDisplayIcon() {
            return mDisplayIcon != null;
        }

        public CharSequence getExtendedInfo() {
            return mExtendedInfo;
        }

        public Intent getResolvedIntent() {
            return mResolvedIntent;
        }

        @Override
        public ComponentName getResolvedComponentName() {
            return new ComponentName(mResolveInfo.activityInfo.packageName,
                    mResolveInfo.activityInfo.name);
        }

        @Override
        public boolean start(AppCompatActivity activity, Bundle options) {
            callStrictModeMethod("disableDeathOnFileUriExposure");
            try {
                activity.startActivity(mResolvedIntent, options);
            } finally {
                callStrictModeMethod("enableDeathOnFileUriExposure");
            }
            return true;
        }

        @Override
        public boolean startAsCaller(AppCompatActivity activity, Bundle options,
                int userId) {
            selfStartAsCaller(mResolvedIntent, activity, options, false, userId);

            return true;
        }

        @Override
        public boolean startAsUser(AppCompatActivity activity, Bundle options, UserHandle user) {
            activity.startActivityAsUser(mResolvedIntent, options, user);
            return false;
        }
    }
    
    static final class ResolvedComponentInfo {
        public final ComponentName name;
        private final List<Intent> mIntents = new ArrayList<>();
        private final List<ResolveInfo> mResolveInfos = new ArrayList<>();

        public ResolvedComponentInfo(ComponentName name, Intent intent, ResolveInfo info) {
            this.name = name;
            add(intent, info);
        }

        public void add(Intent intent, ResolveInfo info) {
            mIntents.add(intent);
            mResolveInfos.add(info);
        }

        public int getCount() {
            return mIntents.size();
        }

        public Intent getIntentAt(int index) {
            return index >= 0 ? mIntents.get(index) : null;
        }

        public ResolveInfo getResolveInfoAt(int index) {
            return index >= 0 ? mResolveInfos.get(index) : null;
        }

        public int findIntent(Intent intent) {
            for (int i = 0, N = mIntents.size(); i < N; i++) {
                if (intent.equals(mIntents.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        public int findResolveInfo(ResolveInfo info) {
            for (int i = 0, N = mResolveInfos.size(); i < N; i++) {
                if (info.equals(mResolveInfos.get(i))) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    @Override
    public void finish() {
        Log.e(TAG,"finish start ");
        if (mAdapter == null) {
            finishFromActivity();
            return;
        }
        if (mAdapter.getCount() > 1) {
            super.finish();
        }else {
            finishFromActivity();
        }
    }
}

