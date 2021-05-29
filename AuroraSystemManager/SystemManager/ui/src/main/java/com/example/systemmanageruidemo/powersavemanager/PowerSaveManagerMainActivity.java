package com.example.systemmanageruidemo.powersavemanager;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.R;

import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.powersavemanager.adpter.PowerItem;
import com.example.systemmanageruidemo.powersavemanager.adpter.PowerSaveAdapter;

import java.util.ArrayList;
import java.util.List;

import com.example.systemmanageruidemo.actionpresent.PowerSavaManagerPresent;
import com.example.systemmanageruidemo.actionview.PowerSavaManagerView;


public class PowerSaveManagerMainActivity extends BaseSupportProxyActivity<PowerSavaManagerPresent> implements PowerSavaManagerView {
    private Context mContext;
    private RecyclerView recyclerView;
    private PowerSaveAdapter mAdapter;

    private List<PowerItem.Detail> details = new ArrayList<>();
    private List<PowerItem.Mode> modes = new ArrayList<>();

    private TextView batteryLevel;
    private TextView batteryTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();
        setContentView(R.layout.activity_power_save_manager_main);
        recyclerView = findViewById(R.id.powersave_item_view);
        mAdapter = new PowerSaveAdapter(mContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(mAdapter);
        initView();
    }

    private void initView() {

        /*省电管理，电池显示百分比；以及可使用时间*/
        batteryLevel = findViewById(R.id.battery_level);
        batteryTime = findViewById(R.id.power_usedtime);
        batteryLevel.setText(UnitUtil.getStr(mContext, R.string.battery_level_test));
        batteryTime.setText(UnitUtil.getStr(mContext, R.string.battery_state) + UnitUtil.getStr(mContext, R.string.normal_mode_flow));

        /*省电模式*/
        PowerItem.Mode norMode = new PowerItem.Mode(UnitUtil.getStr(mContext, R.string.normal_mode_title), UnitUtil.getStr(mContext, R.string.normal_mode_desc), UnitUtil.getStr(mContext, R.string.normal_mode_detail), false);
        modes.add(norMode);
        PowerItem.Mode dayilyMode = new PowerItem.Mode(UnitUtil.getStr(mContext, R.string.daily_save_mode_title), UnitUtil.getStr(mContext, R.string.daily_save_mode_desc), UnitUtil.getStr(mContext, R.string.daily_save_mode_detail) + UnitUtil.getStr(mContext, R.string.daily_save_mode_flow), false);
        modes.add(dayilyMode);
        PowerItem.Mode ultimateMode = new PowerItem.Mode(UnitUtil.getStr(mContext, R.string.ultimate_save_mode_title), UnitUtil.getStr(mContext, R.string.ultimate_save_mode_desc), UnitUtil.getStr(mContext, R.string.ultimate_save_mode_detail) + UnitUtil.getStr(mContext, R.string.ultimate_save_mode_flow), false);
        modes.add(ultimateMode);

        /*耗电详情*/
        PowerItem.Detail detail = new PowerItem.Detail(UnitUtil.getStr(mContext, R.string.power_detail), UnitUtil.getStr(mContext, R.string.power_detail_desc));
        details.add(detail);

        mAdapter.setListA(modes);
        mAdapter.setListB(details);

        /*点击监听*/
        mAdapter.setOnItemClickListener(new PowerSaveAdapter.OnItemClickListener() {
            @Override
            public void onSwitchClick(boolean modeState, int position) {
                mAdapter.getListA().get(position).setTrue(modeState);
                if (mAdapter.getListA().get(position).getTrue()) {
                    Toast.makeText(mContext, "点击事件  mode", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onClick(int position) {
                mAdapter.getListB().get(position - mAdapter.getListA().size());
                Toast.makeText(mContext, "点击事件 耗电详情 ", Toast.LENGTH_SHORT).show();

            }
        });


    }

    PowerSavaManagerPresent presenter;

    @Override
    public void setPresenter(PowerSavaManagerPresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public PowerSavaManagerPresent getPresenter(PowerSavaManagerPresent presenter) {
        return presenter;
    }


    @Override
    public void requestPower() {
        presenter.onRequestPower();
    }

    @Override
    public void onResponse(int percent, String hint, int strategy) {

    }

    @Override
    public void changeStrategy(int strategy) {
        presenter.onChangeStrategy(strategy);
    }
}