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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cyee.internal.util.ReflectionUtils;
import com.android.internal.app.IntentForwarderActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import com.cyee.utils.Log;
import android.view.ViewGroup;
import android.os.ResultReceiver;

import androidx.appcompat.app.AppCompatActivity;

public class CyeeChooserActivity extends CyeeResolverActivity {
    
    private static final String TAG = "CyeeChooserActivity";

    private static final boolean DEBUG = true;

    private Bundle mReplacementExtras;
    private IntentSender mChosenComponentSender;
    private RefinementResultReceiver mRefinementResultReceiver;
    private Intent mReferrerFillInIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Log.v(TAG, "performance----onCreate----");
        Parcelable targetParcelable = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (!(targetParcelable instanceof Intent)) {
            Log.w("CyeeChooserActivity", "Target is not an intent: " + targetParcelable);
            finish();
            return;
        }
        Intent target = (Intent)targetParcelable;
        if (target != null) {
            modifyTargetIntent(target);
        }
        
        String extralField = (String) ReflectionUtils.getFieldValue(
                Intent.class, "EXTRA_ALTERNATE_INTENTS");
        Parcelable[] targetsParcelable = null;
        if (null != extralField) {
            targetsParcelable = intent.getParcelableArrayExtra(extralField);
        }
        
        if (targetsParcelable != null) {
            final boolean offset = target == null;
            Intent[] additionalTargets = new Intent[offset ? targetsParcelable.length - 1
                    : targetsParcelable.length];
            for (int i = 0; i < targetsParcelable.length; i++) {
                if (!(targetsParcelable[i] instanceof Intent)) {
                    Log.w(TAG, "EXTRA_ALTERNATE_INTENTS array entry #" + i
                            + " is not an Intent: " + targetsParcelable[i]);
                    finish();
                    super.onCreate(null);
                    return;
                }
                final Intent additionalTarget = (Intent) targetsParcelable[i];
                if (i == 0 && target == null) {
                    target = additionalTarget;
                    modifyTargetIntent(target);
                } else {
                    additionalTargets[offset ? i - 1 : i] = additionalTarget;
                    modifyTargetIntent(additionalTarget);
                }
            }
            setAdditionalTargets(additionalTargets);
        }
        
        mReplacementExtras = intent.getBundleExtra(Intent.EXTRA_REPLACEMENT_EXTRAS);
        CharSequence title = intent.getCharSequenceExtra(Intent.EXTRA_TITLE);
        if (title == null) {
            title = getResources().getText(com.cyee.internal.R.string.cyee_chooseActivity);
        }
        Parcelable[] pa = intent.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
        Intent[] initialIntents = null;
        
        if (pa != null) {
            initialIntents = new Intent[pa.length];
            Log.e("CyeeChooserActivity","pa.length="+pa.length);
            for (int i=0; i<pa.length; i++) {
                if (!(pa[i] instanceof Intent)) {
                    Log.w("CyeeChooserActivity", "Initial intent #" + i
                            + " not an Intent: " + pa[i]);
                    finish();
                    super.onCreate(null);
                    return;
                }
                final Intent in = (Intent) pa[i];
                modifyTargetIntent(in);
                initialIntents[i] = in;
            }
        }
        
        final Uri referrer = (Uri)ReflectionUtils.invokeMethod(this, "getReferrer", null, null);
        
        if (referrer != null) {
            mReferrerFillInIntent = new Intent().putExtra(Intent.EXTRA_REFERRER, referrer);
        }
        
        String componentSender = (String) ReflectionUtils.getFieldValue(
                Intent.class, "EXTRA_CHOSEN_COMPONENT_INTENT_SENDER");
        if (null != componentSender) {
            mChosenComponentSender = intent.getParcelableExtra(componentSender);
        }
        
        String refinementSender = (String) ReflectionUtils.getFieldValue(
                Intent.class, "EXTRA_CHOOSER_REFINEMENT_INTENT_SENDER");
        if (null != refinementSender) {
            mChosenComponentSender = intent.getParcelableExtra(refinementSender);
        }
        setSafeForwardingMode(true);

        super.onCreate(savedInstanceState, target, title, initialIntents, null, false);
    }
    

    @Override
    protected void onResume() {
        Log.v(TAG, "performance---onResume---");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRefinementResultReceiver != null) {
            mRefinementResultReceiver.destroy();
            mRefinementResultReceiver = null;
        }
    }
    
    @Override
    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        Intent result = defIntent;
        if (mReplacementExtras != null) {
            final Bundle replExtras = mReplacementExtras.getBundle(aInfo.packageName);
            if (replExtras != null) {
                result = new Intent(defIntent);
                result.putExtras(replExtras);
            }
        }
//        if (aInfo.name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_USER_OWNER)
//                || aInfo.name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE)) {
//            result = Intent.createChooser(result,
//                    getIntent().getCharSequenceExtra(Intent.EXTRA_TITLE));
//        }
        return result;
    }
    
    @Override
    void onActivityStarted(TargetInfo cti) {
        if (mChosenComponentSender != null) {
            final ComponentName target = cti.getResolvedComponentName();
            if (target != null) {
                String chosen = (String) ReflectionUtils.getFieldValue(
                        Intent.class, "EXTRA_CHOSEN_COMPONENT");
                if (null != chosen) {
                    final Intent fillIn = new Intent().putExtra(chosen, target);
                    try {
                        mChosenComponentSender.sendIntent(this, Activity.RESULT_OK, fillIn, null, null);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    @Override
    boolean shouldGetActivityMetadata() {
        return true;
    }
    
    void onRefinementResult(TargetInfo selectedTarget, Intent matchingIntent) {
        if (mRefinementResultReceiver != null) {
            mRefinementResultReceiver.destroy();
            mRefinementResultReceiver = null;
        }

        if (selectedTarget == null) {
            Log.e(TAG, "Refinement result intent did not match any known targets; canceling");
        } else if (!checkTargetSourceIntent(selectedTarget, matchingIntent)) {
            Log.e(TAG, "onRefinementResult: Selected target " + selectedTarget
                    + " cannot match refined source intent " + matchingIntent);
        } /*else if (super.onTargetSelected(selectedTarget.cloneFilledIn(matchingIntent, 0), false)) {
            finish();
            return;
        }*/
        onRefinementCanceled();
    }

    void onRefinementCanceled() {
        if (mRefinementResultReceiver != null) {
            mRefinementResultReceiver.destroy();
            mRefinementResultReceiver = null;
        }
        finish();
    }

    boolean checkTargetSourceIntent(TargetInfo target, Intent matchingIntent) {
        final List<Intent> targetIntents = target.getAllSourceIntents();
        for (int i = 0, N = targetIntents.size(); i < N; i++) {
            final Intent targetIntent = targetIntents.get(i);
            if (targetIntent.filterEquals(matchingIntent)) {
                return true;
            }
        }
        return false;
    }
    

    @Override
    ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents,
            Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid,
            boolean filterLastUsed) {
        final ChooserListAdapter adapter = new ChooserListAdapter(context, payloadIntents,
                initialIntents, rList, launchedFromUid, filterLastUsed);
        if (DEBUG) Log.d(TAG, "Adapter created; querying services");
//        queryTargetServices(adapter);
        return adapter;
    }
    
    
    static class RefinementResultReceiver extends ResultReceiver {
        private CyeeChooserActivity mChooserActivity;
        private TargetInfo mSelectedTarget;

        public RefinementResultReceiver(CyeeChooserActivity host, TargetInfo target,
                Handler handler) {
            super(handler);
            mChooserActivity = host;
            mSelectedTarget = target;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (mChooserActivity == null) {
                Log.e(TAG, "Destroyed RefinementResultReceiver received a result");
                return;
            }
            if (resultData == null) {
                Log.e(TAG, "RefinementResultReceiver received null resultData");
                return;
            }

            switch (resultCode) {
                case RESULT_CANCELED:
                    mChooserActivity.onRefinementCanceled();
                    break;
                case RESULT_OK:
                    Parcelable intentParcelable = resultData.getParcelable(Intent.EXTRA_INTENT);
                    if (intentParcelable instanceof Intent) {
                        mChooserActivity.onRefinementResult(mSelectedTarget,
                                (Intent) intentParcelable);
                    } else {
                        Log.e(TAG, "RefinementResultReceiver received RESULT_OK but no Intent"
                                + " in resultData with key Intent.EXTRA_INTENT");
                    }
                    break;
                default:
                    Log.w(TAG, "Unknown result code " + resultCode
                            + " sent to RefinementResultReceiver");
                    break;
            }
        }

        public void destroy() {
            mChooserActivity = null;
            mSelectedTarget = null;
        }
    }
    
    private void modifyTargetIntent(Intent in) {
        final String action = in.getAction();
        if (Intent.ACTION_SEND.equals(action) ||
                Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            in.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
    }
    
    final class ChooserTargetInfo implements TargetInfo {
        
        
        
        private final DisplayResolveInfo mSourceInfo;
        private final ResolveInfo mBackupResolveInfo;
//        private final ChooserTarget mChooserTarget;
        private Drawable mBadgeIcon = null;
        private CharSequence mBadgeContentDescription;
        private Drawable mDisplayIcon;
        private final Intent mFillInIntent;
        private final int mFillInFlags;
        private final float mModifiedScore;

//        android.service.chooser.ChooserTarget
        
        public ChooserTargetInfo(DisplayResolveInfo sourceInfo, Object chooserTarget,
                float modifiedScore) {
//            ChooserTarget
            mSourceInfo = sourceInfo;
//            mChooserTarget = chooserTarget;
            mModifiedScore = modifiedScore;
            if (sourceInfo != null) {
                final ResolveInfo ri = sourceInfo.getResolveInfo();
                if (ri != null) {
                    final ActivityInfo ai = ri.activityInfo;
                    if (ai != null && ai.applicationInfo != null) {
                        final PackageManager pm = getPackageManager();
                        mBadgeIcon = pm.getApplicationIcon(ai.applicationInfo);
                        mBadgeContentDescription = pm.getApplicationLabel(ai.applicationInfo);
                    }
                }
            }
//            import android.service.chooser.ChooserTarget;
            
            
//            Class cl = ReflectionUtils.getClass("");
//            cl.newInstance() = mSourceInfo;
//            final Icon icon = (Class)chooserTarget.getIcon();
//            // TODO do this in the background
//            mDisplayIcon = icon != null ? icon.loadDrawable(CyeeChooserActivity.this) : null;

            if (sourceInfo != null) {
                mBackupResolveInfo = null;
            } else {
                mBackupResolveInfo = getPackageManager().resolveActivity(getResolvedIntent(), 0);
            }

            mFillInIntent = null;
            mFillInFlags = 0;
        }

        private ChooserTargetInfo(ChooserTargetInfo other, Intent fillInIntent, int flags) {
            mSourceInfo = other.mSourceInfo;
            mBackupResolveInfo = other.mBackupResolveInfo;
//            mChooserTarget = other.mChooserTarget;
            mBadgeIcon = other.mBadgeIcon;
            mBadgeContentDescription = other.mBadgeContentDescription;
            mDisplayIcon = other.mDisplayIcon;
            mFillInIntent = fillInIntent;
            mFillInFlags = flags;
            mModifiedScore = other.mModifiedScore;
        }

        public float getModifiedScore() {
            return mModifiedScore;
        }

        @Override
        public Intent getResolvedIntent() {
            if (mSourceInfo != null) {
                return mSourceInfo.getResolvedIntent();
            }
            return getTargetIntent();
        }

        @Override
        public ComponentName getResolvedComponentName() {
            if (mSourceInfo != null) {
                return mSourceInfo.getResolvedComponentName();
            } else if (mBackupResolveInfo != null) {
                return new ComponentName(mBackupResolveInfo.activityInfo.packageName,
                        mBackupResolveInfo.activityInfo.name);
            }
            return null;
        }

        private Intent getBaseIntentToSend() {
            Intent result = mSourceInfo != null
                    ? mSourceInfo.getResolvedIntent() : getTargetIntent();
            if (result == null) {
                Log.e(TAG, "ChooserTargetInfo: no base intent available to send");
            } else {
                result = new Intent(result);
                if (mFillInIntent != null) {
                    result.fillIn(mFillInIntent, mFillInFlags);
                }
                result.fillIn(mReferrerFillInIntent, 0);
            }
            return result;
        }

        @Override
        public boolean start(AppCompatActivity activity, Bundle options) {
            throw new RuntimeException("ChooserTargets should be started as caller.");
        }

        @Override
        public boolean startAsCaller(AppCompatActivity activity, Bundle options,
                int userId) {
            final Intent intent = getBaseIntentToSend();
            if (intent == null) {
                return false;
            }
            // intent.setComponent(mChooserTarget.getComponentName());
            // intent.putExtras(mChooserTarget.getIntentExtras());
            selfStartAsCaller(intent, activity, options, true, userId);
            
            return true;
        }

        @Override
        public boolean startAsUser(AppCompatActivity activity, Bundle options, UserHandle user) {
            throw new RuntimeException("ChooserTargets should be started as caller.");
        }

        @Override
        public ResolveInfo getResolveInfo() {
            return mSourceInfo != null ? mSourceInfo.getResolveInfo() : mBackupResolveInfo;
        }

        @Override
        public CharSequence getDisplayLabel() {
//            return mChooserTarget.getTitle();
            return "";
        }

        @Override
        public CharSequence getExtendedInfo() {
            return mSourceInfo != null ? mSourceInfo.getExtendedInfo() : null;
        }

        @Override
        public Drawable getDisplayIcon() {
            return mDisplayIcon;
        }

        @Override
        public Drawable getBadgeIcon() {
            return mBadgeIcon;
        }

        @Override
        public CharSequence getBadgeContentDescription() {
            return mBadgeContentDescription;
        }

        @Override
        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new ChooserTargetInfo(this, fillInIntent, flags);
        }

        @Override
        public List<Intent> getAllSourceIntents() {
            final List<Intent> results = new ArrayList<>();
            if (mSourceInfo != null) {
                // We only queried the service for the first one in our sourceinfo.
                results.add(mSourceInfo.getAllSourceIntents().get(0));
            }
            return results;
        }
    }
    
    public class ChooserListAdapter extends ResolveListAdapter {
        public static final int TARGET_BAD = -1;
        public static final int TARGET_CALLER = 0;
        public static final int TARGET_SERVICE = 1;
        public static final int TARGET_STANDARD = 2;

        private static final int MAX_SERVICE_TARGETS = 8;

        private final List<ChooserTargetInfo> mServiceTargets = new ArrayList<>();
        private final List<TargetInfo> mCallerTargets = new ArrayList<>();

        private final float mLateFee = 1.f;

//        private final BaseChooserTargetComparator mBaseTargetComparator
//                = new BaseChooserTargetComparator();

        public ChooserListAdapter(Context context, List<Intent> payloadIntents,
                Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid,
                boolean filterLastUsed) {
            // Don't send the initial intents through the shared ResolverActivity path,
            // we want to separate them into a different section.
            super(context, payloadIntents, null, rList, launchedFromUid, filterLastUsed);
            if (initialIntents != null) {
                final PackageManager pm = getPackageManager();
                for (int i = 0; i < initialIntents.length; i++) {
                    final Intent ii = initialIntents[i];
                    if (ii == null) {
                        continue;
                    }
                    final ActivityInfo ai = ii.resolveActivityInfo(pm, 0);
                    if (ai == null) {
                        Log.w(TAG, "No activity found for " + ii);
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
                        ri.iconResourceId = ri.icon;
                    }
                    if (userManager.isManagedProfile()) {
                        ri.noResourceId = true;
                        ri.icon = 0;
                    }
                    mCallerTargets.add(new DisplayResolveInfo(ii, ri,
                            ri.loadLabel(pm), null, ii));
                }
            }
        }

        @Override
        public boolean showsExtendedInfo(TargetInfo info) {
            // Reserve space to show extended info if any one of the items in the adapter has
            // extended info. This keeps grid item sizes uniform.
            return hasExtendedInfo();
        }

        @Override
        public void onListRebuilt() {
            if (mServiceTargets != null) {
                pruneServiceTargets();
            }
        }

        @Override
        public boolean shouldGetResolvedFilter() {
            return true;
        }

        @Override
        public int getCount() {
            return super.getCount() + getServiceTargetCount() + getCallerTargetCount();
        }

        @Override
        public int getUnfilteredCount() {
            return super.getUnfilteredCount() + getServiceTargetCount() + getCallerTargetCount();
        }

        public int getCallerTargetCount() {
            return mCallerTargets.size();
        }

        public int getServiceTargetCount() {
            return Math.min(mServiceTargets.size(), MAX_SERVICE_TARGETS);
        }

        public int getStandardTargetCount() {
            return super.getCount();
        }

        public int getPositionTargetType(int position) {
            int offset = 0;

            final int callerTargetCount = getCallerTargetCount();
            if (position < callerTargetCount) {
                return TARGET_CALLER;
            }
            offset += callerTargetCount;

            final int serviceTargetCount = getServiceTargetCount();
            if (position - offset < serviceTargetCount) {
                return TARGET_SERVICE;
            }
            offset += serviceTargetCount;

            final int standardTargetCount = super.getCount();
            if (position - offset < standardTargetCount) {
                return TARGET_STANDARD;
            }

            return TARGET_BAD;
        }

        @Override
        public TargetInfo getItem(int position) {
            return targetInfoForPosition(position, true);
        }

        @Override
        public TargetInfo targetInfoForPosition(int position, boolean filtered) {
            int offset = 0;

            final int callerTargetCount = getCallerTargetCount();
            if (position < callerTargetCount) {
                return mCallerTargets.get(position);
            }
            offset += callerTargetCount;

            final int serviceTargetCount = getServiceTargetCount();
            if (position - offset < serviceTargetCount) {
                return mServiceTargets.get(position - offset);
            }
            offset += serviceTargetCount;

            return filtered ? (TargetInfo)(super.getItem(position - offset))
                    : getDisplayInfoAt(position - offset);
        }

//        public void addServiceResults(DisplayResolveInfo origTarget, List<ChooserTarget> targets) {
//            if (DEBUG) Log.d(TAG, "addServiceResults " + origTarget + ", " + targets.size()
//                    + " targets");
//            final float parentScore = getScore(origTarget);
//            Collections.sort(targets, mBaseTargetComparator);
//            float lastScore = 0;
//            for (int i = 0, N = targets.size(); i < N; i++) {
//                final ChooserTarget target = targets.get(i);
//                float targetScore = target.getScore();
//                targetScore *= parentScore;
//                targetScore *= mLateFee;
//                if (i > 0 && targetScore >= lastScore) {
//                    // Apply a decay so that the top app can't crowd out everything else.
//                    // This incents ChooserTargetServices to define what's truly better.
//                    targetScore = lastScore * 0.95f;
//                }
//                insertServiceTarget(new ChooserTargetInfo(origTarget, target, targetScore));
//
//                if (DEBUG) {
//                    Log.d(TAG, " => " + target.toString() + " score=" + targetScore
//                            + " base=" + target.getScore()
//                            + " lastScore=" + lastScore
//                            + " parentScore=" + parentScore
//                            + " lateFee=" + mLateFee);
//                }
//
//                lastScore = targetScore;
//            }
//
//            mLateFee *= 0.95f;
//
//            notifyDataSetChanged();
//        }

//        private void insertServiceTarget(ChooserTargetInfo chooserTargetInfo) {
//            final float newScore = chooserTargetInfo.getModifiedScore();
//            for (int i = 0, N = mServiceTargets.size(); i < N; i++) {
//                final ChooserTargetInfo serviceTarget = mServiceTargets.get(i);
//                if (newScore > serviceTarget.getModifiedScore()) {
//                    mServiceTargets.add(i, chooserTargetInfo);
//                    return;
//                }
//            }
//            mServiceTargets.add(chooserTargetInfo);
//        }

        private void pruneServiceTargets() {
            if (DEBUG) Log.d(TAG, "pruneServiceTargets");
            for (int i = mServiceTargets.size() - 1; i >= 0; i--) {
                final ChooserTargetInfo cti = mServiceTargets.get(i);
                if (!hasResolvedTarget(cti.getResolveInfo())) {
                    if (DEBUG) Log.d(TAG, " => " + i + " " + cti);
                    mServiceTargets.remove(i);
                }
            }
        }
    }
}
