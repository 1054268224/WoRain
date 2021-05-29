package com.cydroid.softmanager.powersaver.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.internal.os.BatterySipper;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.powersaver.activities.BatteryUseRankActivity;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeTextView;

public class UsageSummaryFragment extends Fragment implements OnItemClickListener {
    private static final String TAG = "UsageSummaryFragment";

    private static final boolean DEBUG = true;
    private static final int MAX_ITEMS_TO_LIST = 10;

    private Context mContext;
    private CyeeTextView mHeader;
    private ListView mListView;
    private SummaryAppListAdapter mAdapter;
    //Gionee <jiangsj><20170417> add for 113665 begin
    private int textLayoutDirection;
    //Gionee <jiangsj><20170417> add for 113665 end
    private int mDataType = -1;

    private BatteryUseRankActivity mBatteryUseRankActivity = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BatteryUseRankActivity) {
            mBatteryUseRankActivity = (BatteryUseRankActivity) activity;
        }
        mContext = activity;
    }

    // 安卓O代码优化，不采用直接设置mDataType，因为某些情况下fragment可能被系统回收
    private static final String EXTRA_DATA_TYPE = "com.cydroid.softmanager.data_type";

    public static UsageSummaryFragment newInstance(int dataType) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_DATA_TYPE, dataType);
        UsageSummaryFragment fragment = new UsageSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<ExtendedBatterySipper> getExtendedBatterySipperList(int type) {
        ArrayList<ExtendedBatterySipper> res = new ArrayList<ExtendedBatterySipper>();
        if (mBatteryUseRankActivity == null) {
            return res;
        }
        return mBatteryUseRankActivity.getExtendedBatterySipperList(type);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Gionee <jiangsj><20170417> add for 113665 begin
        textLayoutDirection = TextUtils.getLayoutDirectionFromLocale(getResources().getConfiguration().locale);
//        //Gionee <jiangsj><20170417> add for 113665 end
//        mDataType = getArguments().getInt(EXTRA_DATA_TYPE);
        mAdapter = new SummaryAppListAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.powersaver_usage_summary_fragment, container, false);
        //Gionee <jiangsj> <20170419> add for 113672 begin
        if (view != null && textLayoutDirection == 1) {
            view.setRotationY(180);
        }
        //Gionee <jiangsj> <20170419> add for 113672 end
        mListView = (ListView) view.findViewById(R.id.usage_summary_app_list);
        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        // Gionee <yangxinruo> <2015-11-23> add for CR01596184 begin
        mHeader = (CyeeTextView) view.findViewById(R.id.header_text);
        // Gionee <yangxinruo> <2015-11-23> add for CR01596184 end
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "UsageSummaryFragment onResume mDataType = " + mDataType);
//        mAbort = false;
        //Gionee <jiangsj><20170417> add for 113665 begin
        textLayoutDirection = TextUtils.getLayoutDirectionFromLocale(getResources().getConfiguration().locale);
        //Gionee <jiangsj><20170417> add for 113665 end
        refreshList();
    }

    private void refreshList() {
        mAdapter.setItemData(getExtendedBatterySipperList(mDataType), mDataType);
    }

    @Override
    public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
        if (mAdapter == null) {
            return;
        }
        ExtendedBatterySipper exSipper = mAdapter.getItem(paramInt);
        // Gionee <yangxinruo> <2016-3-3> modify for CR01645219 begin
        if (exSipper == null) {
            return;
        }
        BatterySipper sipper = exSipper.batterSipper;
        if (sipper == null) {
            return;
        }
        Log.d(DEBUG, TAG, "sipper drainType == " + sipper.drainType);
        if (sipper.drainType == BatterySipper.DrainType.APP) {
            String packageName = exSipper.getDefaultPackageName();
            if (packageName == null) {
                Log.d(DEBUG, TAG, "onItemClick(), mAdapter == null");
                return;
            }
        }
        NewPowerUsageDetailFragment.startBatteryDetailPage(mContext,
                mBatteryUseRankActivity.getBatteryHelper(), mBatteryUseRankActivity.getStatsType(), exSipper,
                true);
    }

    public class SummaryAppListAdapter extends BaseAdapter {
        private final Context mContext;
        private final List<ExtendedBatterySipper> mListItemData = new ArrayList<ExtendedBatterySipper>();
        private final AsyncAppIconLoader mAsyncAppIconLoader;
        private int mType = BatteryUseRankActivity.TYPE_SOFTWARE_LIST;

        public SummaryAppListAdapter(Context context) {
            mContext = context;
            mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
        }

        @Override
        public int getCount() {
            if (mListItemData != null) {
                return mListItemData.size();
            }
            return 0;
        }

        @Override
        public ExtendedBatterySipper getItem(int paramInt) {
            if (mListItemData == null) {
                return null;
            }
            return mListItemData.get(paramInt);
        }

        @Override
        public long getItemId(int paramInt) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            final ViewCache viewCache;

            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.powersaver_app_percentage_item, parent,
                        false);

                viewCache = new ViewCache();
                view.setTag(viewCache);
            } else {
                viewCache = (ViewCache) view.getTag();
            }

            ExtendedBatterySipper exSipper = mListItemData.get(position);

            viewCache.mIcon = (ImageView) view.findViewById(android.R.id.icon);
            if (exSipper.getIconId() > 0) {
                viewCache.mIcon.setImageResource(exSipper.getIconId());
            } else {
                setIconImage(viewCache.mIcon, exSipper.getDefaultPackageName());
            }
            if (mType == BatteryUseRankActivity.TYPE_HARDWARE_LIST
                    && ChameleonColorManager.isNeedChangeColor() && UiUtils.isSpecialStyleModel()) {
                int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
                viewCache.mIcon.getDrawable().setTint(color_T1);
            }
            // Gionee <liuyb> <2014-09-20> modify for CR01386162 begin
            String pkgLable = exSipper.getName();
            viewCache.mText = (TextView) view.findViewById(R.id.title);
            //Gionee <jiangsj><20170417> modify for 113665 begin
            if (textLayoutDirection == 1) {
                viewCache.mText.setText("\u200F" + pkgLable);
            } else {
                viewCache.mText.setText(pkgLable);
            }
            //Gionee <jiangsj><20170417> modify for 113665 end
            // Gionee <liuyb> <2014-09-20> modify for CR01386162 end

            viewCache.mProgress = (ProgressBar) view.findViewById(R.id.progress);
            viewCache.mProgress.setProgress(exSipper.getPercent() / 10);

            viewCache.mTextSummary = (TextView) view.findViewById(R.id.summary);
            viewCache.mTextSummary.setText(mContext.getResources().getString(R.string.percentage,
                    (double) exSipper.getPercent() / 10d));
            return view;
        }

        private void setIconImage(final ImageView icon, final String packageName) {
            icon.setTag(packageName);
            Drawable cachedImage = mAsyncAppIconLoader.loadAppIconDrawable(mContext, packageName,
                    new ImageCallback() {
                        public void imageLoaded(Drawable imageDrawable, String pkgName) {
                            if (!pkgName.equals(icon.getTag())) {
                                return;
                            }
                            if (null != imageDrawable) {
                                icon.setImageDrawable(imageDrawable);
                            } else {
                                // can not found pkg icon
                                icon.setImageDrawable(mContext.getPackageManager().getDefaultActivityIcon());
                            }
                        }
                    });
            if (null != cachedImage) {
                icon.setImageDrawable(cachedImage);
            }
        }

        public void setItemData(List<ExtendedBatterySipper> list, int type) {
            mType = type;
            mListItemData.clear();
            Collections.sort(list, mComparator);
            int percentOfTotal = 0;
            for (ExtendedBatterySipper sipper : list) {
                percentOfTotal += sipper.getPercent();
                mListItemData.add(sipper);
            }
            String headerTitle = "";
            if (list.size() > 0) {
                // Chenyee <liyuchong> <2018-5-26> modify for add space around colons for condor feedback begin
                // Gionee <yangxinruo> <2016-4-19> modify for CR01680614 begin
                if (mType == BatteryUseRankActivity.TYPE_HARDWARE_LIST) {
                    headerTitle = getResources().getString(R.string.hardware) + " : " + mContext.getResources()
                            .getString(R.string.percentage, (double) percentOfTotal / 10d);
                } else if (mType == BatteryUseRankActivity.TYPE_SOFTWARE_LIST) {
                    headerTitle = getResources().getString(R.string.software) + " : " + mContext.getResources()
                            .getString(R.string.percentage, (double) percentOfTotal / 10d);
                }
                // Gionee <yangxinruo> <2016-4-19> modify for CR01680614 end
                // Chenyee <liyuchong> <2018-5-26> modify for add space around colons for condor feedback end
            }
            mHeader.setText(headerTitle);
            // Gionee <yangxinruo> <2015-11-23> add for CR01596184 end
            notifyDataSetChanged();
        }
    }

    private final Comparator<ExtendedBatterySipper> mComparator = new Comparator<ExtendedBatterySipper>() {
        @Override
        public int compare(ExtendedBatterySipper paramT1, ExtendedBatterySipper paramT2) {
            BigDecimal d1 = new BigDecimal(Double.toString(paramT1.getSortValue()));
            BigDecimal d2 = new BigDecimal(Double.toString(paramT2.getSortValue()));
            double d3 = d1.subtract(d2).doubleValue();
            if (d3 > 0.0000d) {
                return -1;
            } else if (d3 < 0.0000d) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public void setDataType(int position) {
        mDataType = position;
    }
}
