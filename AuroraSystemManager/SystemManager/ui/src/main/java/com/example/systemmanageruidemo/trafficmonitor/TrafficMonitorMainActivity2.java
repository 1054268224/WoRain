package com.example.systemmanageruidemo.trafficmonitor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.actionpresent.TrafficMonitorPresent2;
import com.example.systemmanageruidemo.actionview.TrafficMonitorView2;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;
import com.example.systemmanageruidemo.view.ChartView;

import java.util.List;

public class TrafficMonitorMainActivity2 extends BaseSupportProxyActivity<TrafficMonitorPresent2> implements TrafficMonitorView2 {
    private Context mContext;
    private FrameLayout mNosim;
    private LinearLayout mHassim;
    private FrameLayout mDoublesim;
    private ViewPager mTrafficViewpager;
    private FrameLayout mSinglesim;
    private LinearLayout mNosettrafficLay;
    private Button mSettrafficBtn;
    private LinearLayout mSettrafficLay;
    private TextView mSettextTrafficText;
    private LinearLayout mSavetrafficLay;
    private TextView mSavetextTrafficText;
    private TextView mSaveresult;
    private LinearLayout mProtectedtrafficLay;
    private TextView mProtectedtextTrafficText;
    private TextView mUsetrafficTextHint;
    private ChartView mCharview;
    private TextView mAppuseTrafficTextHint;
    private RecyclerView mRecycleview;
    private PagerAdapter msimViewPagerAdapter = new MViewPager();
    private ViewPager.OnPageChangeListener mObPageListenter = new MPageListenter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();
        setContentView(R.layout.activity_traffic_monitor_main2);
        initView();
        loaddata();
    }

    TraPagerBean traPagerBean = new TraPagerBean();

    private void loaddata() {
        requestSIM(traPagerBean);
    }

    int simcount;
    int currentSim;

    private void setdata(TraPagerBean object) {
        if (object != null) {
            simcount = object.getSimCardScount();
            if (simcount == 0) {
                showNosim();
            } else if (simcount == 1) {
                showSingleSim();
            } else if (simcount == 1) {
                showMultSim();
            }
            if (simcount != 0) {
                initBlowSimView();
                changeCurrentSim(currentSim);
            }

        }
    }

    private void initBlowSimView() {

    }

    /**
     * sim 卡对应的下方数据
     *
     * @param currentSim
     */
    private void changeCurrentSim(int currentSim) {
        TraPagerBean.SIMBean object = traPagerBean.getList().get(currentSim);
        if (object.isIssetted()) {
            mNosettrafficLay.setVisibility(View.GONE);
            mSettrafficLay.setVisibility(View.VISIBLE);
        } else {
            mNosettrafficLay.setVisibility(View.VISIBLE);
            mSettrafficLay.setVisibility(View.GONE);
        }
        requestChartData(currentSim);
        requestAppusetData(currentSim);
    }

    private void requestAppusetData(int currentSim) {
    }

    private void requestChartData(int currentSim) {
    }

    public void onResponseChartData() {

    }

    public void onResponseAppusetData() {

    }

    public void showMultSim() {
        mHassim.setVisibility(View.VISIBLE);
        mSinglesim.setVisibility(View.GONE);
        mDoublesim.setVisibility(View.VISIBLE);
        mTrafficViewpager.setAdapter(msimViewPagerAdapter);
        mTrafficViewpager.addOnPageChangeListener(mObPageListenter);
    }

    public void showSingleSim() {
        mHassim.setVisibility(View.VISIBLE);
        mSinglesim.setVisibility(View.VISIBLE);
        mDoublesim.setVisibility(View.GONE);
        currentSim = 0;
        bindSimCard(currentSim, mSinglesim);
    }

    public void showNosim() {
        mNosim.setVisibility(View.VISIBLE);
        mHassim.setVisibility(View.GONE);
    }

    private void initView() {
        mNosim = findViewById(R.id.nosim);
        mHassim = findViewById(R.id.hassim);
        mDoublesim = findViewById(R.id.doublesim);
        mTrafficViewpager = findViewById(R.id.traffic_viewpager);
        mSinglesim = findViewById(R.id.singlesim);
        mNosettrafficLay = findViewById(R.id.nosettraffic_lay);
        mSettrafficBtn = findViewById(R.id.settraffic_btn);
        mSettrafficLay = findViewById(R.id.settraffic_lay);
        mSettextTrafficText = findViewById(R.id.settext_traffic_text);
        mSavetrafficLay = findViewById(R.id.savetraffic_lay);
        mSavetextTrafficText = findViewById(R.id.savetext_traffic_text);
        mSaveresult = findViewById(R.id.saveresult);
        mProtectedtrafficLay = findViewById(R.id.protectedtraffic_lay);
        mProtectedtextTrafficText = findViewById(R.id.protectedtext_traffic_text);
        mUsetrafficTextHint = findViewById(R.id.usetraffic_text_hint);
        mCharview = findViewById(R.id.charview);
        mAppuseTrafficTextHint = findViewById(R.id.appuse_traffic_text_hint);
        mRecycleview = findViewById(R.id.recycleview);
        mSavetrafficLay.setOnClickListener(view -> {
            Intent intent = new Intent("android.settings.DATA_SAVER_SETTINGS");
            startActivity(intent);
        });
        mProtectedtrafficLay.setOnClickListener(view -> {
            Intent intent = new Intent("com.wheatek.security.NETWORK_DATA_CONTROLLER");
//            Intent intent = new Intent("com.mediatek.security.NETWORK_DATA_CONTROLLER");
            startActivity(intent);
        });
    }

    TrafficMonitorPresent2 presenter;

    @Override
    public void setPresenter(TrafficMonitorPresent2 presenter) {
        this.presenter = presenter;
    }

    @Override
    public TrafficMonitorPresent2 getPresenter(TrafficMonitorPresent2 presenter) {
        return presenter;
    }


    @Override
    public void requestSIM(TraPagerBean object) {
        presenter.onRequestSIM(object);
    }

    @Override
    public void onResponseSIM(TraPagerBean object) {
        setdata(object);
    }

    @Override
    public void initData(List<TraRecyBean> list) {
        presenter.onInitData(list);
    }

    @Override
    public void onRefresh(List<TraRecyBean> list) {

    }

    @Override
    public void appChangeState(TraRecyBean object, boolean ischecked) {
        presenter.onAppChangeState(object, ischecked);
    }

    private class MViewPager extends PagerAdapter {

        @Override
        public int getCount() {
            return simcount;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
//            return super.instantiateItem(container, position);
            View view = createSimCard(container);
            bindSimCard(position, view);
            return view;
        }

        private View createSimCard(ViewGroup container) {
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.traffic_simcard_container, container, false);
            return view;
        }
    }

    public static class SimViewHolder {
        private TextView mNameSim;
        private TextView mNameCmc;
        private TextView mPhoneNumber;
        private TextView mSurplusFlow;
        private TextView mUnit;
        private TextView mAlltraff;
        private TextView mUsetraff;

        private SimViewHolder(View view) {
            mNameSim = view.findViewById(R.id.name_sim);
            mNameCmc = view.findViewById(R.id.name_cmc);
            mPhoneNumber = view.findViewById(R.id.phone_number);
            mSurplusFlow = view.findViewById(R.id.surplus_flow);
            mUnit = view.findViewById(R.id.unit);
            mAlltraff = view.findViewById(R.id.alltraff);
            mUsetraff = view.findViewById(R.id.usetraff);
        }
    }

    private void bindSimCard(int position, View viewGroup) {
        TraPagerBean.SIMBean data = traPagerBean.getList().get(position);
        SimViewHolder simViewHolder = new SimViewHolder(viewGroup);
        // todo

    }

    private class MPageListenter implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentSim = position;
            changeCurrentSim(currentSim);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }


}