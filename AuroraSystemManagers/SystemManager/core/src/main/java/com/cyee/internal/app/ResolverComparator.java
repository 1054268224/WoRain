/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.text.TextUtils;
import com.cyee.utils.Log;
import android.graphics.drawable.Drawable;
import com.cyee.internal.app.CyeeResolverActivity.ResolvedComponentInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.cyee.internal.R;

/**
 * Ranks and compares packages based on usage stats.
 */
class ResolverComparator implements Comparator<ResolvedComponentInfo> {
    private static final String TAG = "ResolverComparator";

    private static final boolean DEBUG = false;

    // Two weeks
    private static final long USAGE_STATS_PERIOD = 1000 * 60 * 60 * 24 * 14;

    private static final long RECENCY_TIME_PERIOD = 1000 * 60 * 60 * 12;

    private static final float RECENCY_MULTIPLIER = 3.f;

    //微信好友
    private static final String COMPONENT_SHARE_TO_IMGUI = "com.tencent.mm.ui.tools.ShareImgUI";

    //微信朋友圈
    private static final String COMPONENT_SHARE_TO_TIMELINEUI = "com.tencent.mm.ui.tools.ShareToTimeLineUI";

    //QQ好友
    private static final String COMPONENT_SHARE_TO_QQFELLOW = "com.tencent.mobileqq.activity.JumpActivity";

    //QQ空间
    private static final String COMPONENT_SHARE_TO_QZONE = "com.qzonex.module.operation.ui.QZonePublishMoodActivity";

    //微博
    private static final String COMPONENT_SHARE_TO_WEIBO = "com.sina.weibo.composerinde.ComposerDispatchActivity";

    //微信收藏
    private static final String COMPONENT_SHARE_TO_FAVORITEUI = "com.tencent.mm.ui.tools.AddFavoriteUI";

    //头条
    private static final String COMPONENT_SHARE_TO_TOUTIAO = "com.ss.android.article.base.feature.search.SearchActivity";

    //QQ浏览器
    private static final String COMPONENT_SHARE_TO_QQBROWSER = "com.tencent.mtt.businesscenter.intent.IntentDispatcherActivity";

    //电子邮件
    private static final String COMPONENT_SHARE_TO_EMAIL = "com.kingsoft.email.activity.ComposeActivityEmail";

    //短信
    private static final String COMPONENT_SHARE_TO_MMS = "com.android.mms.ui.ComposeMessageActivity";

    //记事本
    private static final String COMPONENT_SHARE_TO_NOTE = "com.gionee.note.app.NewNoteActivity";

    //蓝牙
    private static final String COMPONENT_SHARE_TO_BLUETOOTH = "com.android.bluetooth.opp.BluetoothOppLauncherActivity";

    //高德地图
    private static final String COMPONENT_SHARE_TO_AUTONAVA = "com.autonavi.map.activity.SplashActivity";

    private final Collator mCollator;
    private final boolean mHttp;
    private final PackageManager mPm;
    //private final UsageStatsManager mUsm;
    private final Map<String, UsageStats> mStats = null;
    private final long mCurrentTime;
    private final long mSinceTime;
    private final LinkedHashMap<ComponentName, ScoredTarget> mScoredTargets = new LinkedHashMap<>();
    private final String mReferrerPackage;
    private final List<String> mSpecialApps = new ArrayList<String>();
    private final List<Integer> mSpecialIcons = new ArrayList<Integer>();

    public ResolverComparator(Context context, Intent intent, String referrerPackage) {
        mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
        String scheme = intent.getScheme();
        mHttp = "http".equals(scheme) || "https".equals(scheme);
        mReferrerPackage = referrerPackage;

        mPm = context.getPackageManager();


        mCurrentTime = System.currentTimeMillis();
        mSinceTime = mCurrentTime - USAGE_STATS_PERIOD;
        //Log.v(TAG, "performance--- query start");
        //mStats = mUsm.queryAndAggregateUsageStats(mSinceTime, mCurrentTime);
        //Log.v(TAG, "performance--- query end");
        initSpecialApps();
    }

    private void initSpecialApps() {
        mSpecialApps.add(COMPONENT_SHARE_TO_IMGUI);
        mSpecialApps.add(COMPONENT_SHARE_TO_TIMELINEUI);
        mSpecialApps.add(COMPONENT_SHARE_TO_QQFELLOW);
        mSpecialApps.add(COMPONENT_SHARE_TO_QZONE);
        mSpecialApps.add(COMPONENT_SHARE_TO_WEIBO);
        mSpecialApps.add(COMPONENT_SHARE_TO_FAVORITEUI);
        mSpecialApps.add(COMPONENT_SHARE_TO_TOUTIAO);
        mSpecialApps.add(COMPONENT_SHARE_TO_QQBROWSER);
        mSpecialApps.add(COMPONENT_SHARE_TO_EMAIL);
        mSpecialApps.add(COMPONENT_SHARE_TO_MMS);
        mSpecialApps.add(COMPONENT_SHARE_TO_NOTE);
        mSpecialApps.add(COMPONENT_SHARE_TO_BLUETOOTH);
        mSpecialApps.add(COMPONENT_SHARE_TO_AUTONAVA);

        mSpecialIcons.add(R.drawable.cyee_share_to_wechat);
        mSpecialIcons.add(R.drawable.cyee_share_to_timeline);
        mSpecialIcons.add(R.drawable.cyee_share_to_qq);
        mSpecialIcons.add(R.drawable.cyee_share_to_qqzone);
        mSpecialIcons.add(R.drawable.cyee_share_to_weibo);
        mSpecialIcons.add(R.drawable.cyee_share_to_weichat_favorite);
        mSpecialIcons.add(R.drawable.cyee_share_to_toutiao);
        mSpecialIcons.add(R.drawable.cyee_share_to_qqbrowser);
        mSpecialIcons.add(R.drawable.cyee_share_to_email);
        mSpecialIcons.add(R.drawable.cyee_share_to_mms);
        mSpecialIcons.add(R.drawable.cyee_share_to_note);
        mSpecialIcons.add(R.drawable.cyee_share_to_bluetooth);
        mSpecialIcons.add(R.drawable.cyee_share_to_autonava);
    }

    public Drawable getSpecialAppIcon(Context context, ResolveInfo resolveInfo) {
        if (context == null) return null;
        int index = mSpecialApps.indexOf(resolveInfo.activityInfo.name);
        if (index < 0 || index >= mSpecialIcons.size()) {
            return null;
        } else {
          return context.getResources().getDrawable(mSpecialIcons.get(index));  
        }
    }

    public void compute(List<ResolvedComponentInfo> targets) {
        mScoredTargets.clear();

        final long recentSinceTime = mCurrentTime - RECENCY_TIME_PERIOD;

        long mostRecentlyUsedTime = recentSinceTime + 1;
        long mostTimeSpent = 1;
        int mostLaunched = 1;

        for (ResolvedComponentInfo target : targets) {
            final ScoredTarget scoredTarget
                    = new ScoredTarget(target.getResolveInfoAt(0).activityInfo);
            mScoredTargets.put(target.name, scoredTarget);
            final UsageStats pkStats = mStats.get(target.name.getPackageName());
            if (pkStats != null) {
                // Only count recency for apps that weren't the caller
                // since the caller is always the most recent.
                // Persistent processes muck this up, so omit them too.
                if (!target.name.getPackageName().equals(mReferrerPackage)
                        && !isPersistentProcess(target)) {
                    final long lastTimeUsed = pkStats.getLastTimeUsed();
                    scoredTarget.lastTimeUsed = lastTimeUsed;
                    if (lastTimeUsed > mostRecentlyUsedTime) {
                        mostRecentlyUsedTime = lastTimeUsed;
                    }
                }
                final long timeSpent = pkStats.getTotalTimeInForeground();
                scoredTarget.timeSpent = timeSpent;
                if (timeSpent > mostTimeSpent) {
                    mostTimeSpent = timeSpent;
                }
                final int launched = pkStats.mLaunchCount;
                scoredTarget.launchCount = launched;
                if (launched > mostLaunched) {
                    mostLaunched = launched;
                }
            }
        }


        if (DEBUG) {
            Log.d(TAG, "compute - mostRecentlyUsedTime: " + mostRecentlyUsedTime
                    + " mostTimeSpent: " + mostTimeSpent
                    + " recentSinceTime: " + recentSinceTime
                    + " mostLaunched: " + mostLaunched);
        }

        for (ScoredTarget target : mScoredTargets.values()) {
            final float recency = (float) Math.max(target.lastTimeUsed - recentSinceTime, 0)
                    / (mostRecentlyUsedTime - recentSinceTime);
            final float recencyScore = recency * recency * RECENCY_MULTIPLIER;
            final float usageTimeScore = (float) target.timeSpent / mostTimeSpent;
            final float launchCountScore = (float) target.launchCount / mostLaunched;

            target.score = recencyScore + usageTimeScore + launchCountScore;
            if (DEBUG) {
                Log.d(TAG, "Scores: recencyScore: " + recencyScore
                        + " usageTimeScore: " + usageTimeScore
                        + " launchCountScore: " + launchCountScore
                        + " - " + target);
            }
        }
    }

    static boolean isPersistentProcess(ResolvedComponentInfo rci) {
        if (rci != null && rci.getCount() > 0) {
            return (rci.getResolveInfoAt(0).activityInfo.applicationInfo.flags &
                    ApplicationInfo.FLAG_PERSISTENT) != 0;
        }
        return false;
    }


    @Override
    public int compare(ResolvedComponentInfo lhsp, ResolvedComponentInfo rhsp) {
        final ResolveInfo lhs = lhsp.getResolveInfoAt(0);
        final ResolveInfo rhs = rhsp.getResolveInfoAt(0);

        //do some special operation at first
        if (mSpecialApps.contains(lhs.activityInfo.name) &&
                  !mSpecialApps.contains(rhs.activityInfo.name)) {
            return -1;
        }else if (!mSpecialApps.contains(lhs.activityInfo.name) &&
                  mSpecialApps.contains(rhs.activityInfo.name)){
            return 1;
        } else if (mSpecialApps.contains(lhs.activityInfo.name) &&
                  mSpecialApps.contains(rhs.activityInfo.name)) {
            return mSpecialApps.indexOf(lhs.activityInfo.name) - mSpecialApps.indexOf(rhs.activityInfo.name);
        }

        // We want to put the one targeted to another user at the end of the dialog.
        if (lhs.targetUserId != UserHandle.USER_CURRENT) {
            return 1;
        }

        if (mHttp) {
            // Special case: we want filters that match URI paths/schemes to be
            // ordered before others.  This is for the case when opening URIs,
            // to make native apps go above browsers.
            final boolean lhsSpecific = CyeeResolverActivity.isSpecificUriMatch(lhs.match);
            final boolean rhsSpecific = CyeeResolverActivity.isSpecificUriMatch(rhs.match);
            if (lhsSpecific != rhsSpecific) {
                return lhsSpecific ? -1 : 1;
            }
        }

        if (mStats != null) {
            final ScoredTarget lhsTarget = mScoredTargets.get(new ComponentName(
                    lhs.activityInfo.packageName, lhs.activityInfo.name));
            final ScoredTarget rhsTarget = mScoredTargets.get(new ComponentName(
                    rhs.activityInfo.packageName, rhs.activityInfo.name));
            final float diff = rhsTarget.score - lhsTarget.score;

            if (diff != 0) {
                return diff > 0 ? 1 : -1;
            }
        }

        CharSequence  sa = lhs.loadLabel(mPm);
        if (sa == null) sa = lhs.activityInfo.name;
        CharSequence  sb = rhs.loadLabel(mPm);
        if (sb == null) sb = rhs.activityInfo.name;

        return mCollator.compare(sa.toString().trim(), sb.toString().trim());
    }

    public float getScore(ComponentName name) {
        final ScoredTarget target = mScoredTargets.get(name);
        if (target != null) {
            return target.score;
        }
        return 0;
    }

    static class ScoredTarget {
        public final ComponentInfo componentInfo;
        public float score;
        public long lastTimeUsed;
        public long timeSpent;
        public long launchCount;

        public ScoredTarget(ComponentInfo ci) {
            componentInfo = ci;
        }

        @Override
        public String toString() {
            return "ScoredTarget{" + componentInfo
                    + " score: " + score
                    + " lastTimeUsed: " + lastTimeUsed
                    + " timeSpent: " + timeSpent
                    + " launchCount: " + launchCount
                    + "}";
        }
    }
}
