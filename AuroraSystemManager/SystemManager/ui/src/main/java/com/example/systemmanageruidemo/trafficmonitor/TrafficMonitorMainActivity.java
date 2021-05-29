package com.example.systemmanageruidemo.trafficmonitor;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.testdata.TrafficMonitorTestData;
import com.example.systemmanageruidemo.actionpresent.TrafficMonitorPresent;
import com.example.systemmanageruidemo.actionview.TrafficMonitorView;
import com.example.systemmanageruidemo.trafficmonitor.adapter.TraPagerAdater;
import com.example.systemmanageruidemo.trafficmonitor.adapter.TraRecyAdapter;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class TrafficMonitorMainActivity extends BaseSupportProxyActivity<TrafficMonitorPresent> implements TrafficMonitorView<TrafficMonitorPresent> {
    private Context mContext;
    private TraPagerAdater pagerAdater;
    private TraRecyAdapter recyAdapter;
    private TraPagerBean data = new TraPagerBean();
    private List<TraRecyBean> recyBeanList = new ArrayList<>();
    private TabLayout tab;
    private ViewPager vp;
    private RecyclerView rv;
    private RelativeLayout nullSimCardView;

    private TrafficMonitorTestData test;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();

//        test = new TrafficMonitorTestData(this);
//        setPresenter(test);

        setContentView(R.layout.activity_traffic_monitor_main);
        initView();
        requestSIM(data);
        initData(recyBeanList);

    }

    private void initView() {
        tab = (TabLayout) findViewById(R.id.traffic_tab);
        vp = (ViewPager) findViewById(R.id.traffic_viewpager);
        rv = (RecyclerView) findViewById(R.id.traffic_item_view);
        nullSimCardView = (RelativeLayout) findViewById(R.id.null_simcard_view);
        pagerAdater = new TraPagerAdater(mContext);
        recyAdapter = new TraRecyAdapter(mContext, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.getTag() instanceof TraRecyBean) {
                    appChangeState((TraRecyBean) buttonView.getTag(), isChecked);
                }
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(mContext));
        pagerAdater.setDatas(data.getList());
        recyAdapter.setDatas(recyBeanList);
        vp.setAdapter(pagerAdater);
        rv.setAdapter(recyAdapter);
    }

    TrafficMonitorPresent presenter;

    @Override
    public void setPresenter(TrafficMonitorPresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public TrafficMonitorPresent getPresenter(TrafficMonitorPresent presenter) {
        return presenter;
    }


    @Override
    public void requestSIM(TraPagerBean object) {
        presenter.onRequestSIM(object);
    }

    @Override
    public void onResponseSIM(TraPagerBean object) {
        int count = object.getSimCardScount();
        if ( count == 0){
            nullSimCardView.setVisibility(View.VISIBLE);
        }else {
            vp.setVisibility(View.VISIBLE);
            pagerAdater.setDatas(object.getList());
            pagerAdater.notifyDataSetChanged();
            tab.setupWithViewPager(vp);
        }
    }

    @Override
    public void initData(List<TraRecyBean> list) {
        presenter.onInitData(list);
    }

    @Override
    public void onRefresh(List<TraRecyBean> list) {
        pagerAdater.setDatas(data.getList());
        pagerAdater.notifyDataSetChanged();
        recyAdapter.notifyDataSetChanged();
    }

    @Override
    public void appChangeState(TraRecyBean object, boolean ischecked) {
        presenter.onAppChangeState(object, ischecked);
    }
}