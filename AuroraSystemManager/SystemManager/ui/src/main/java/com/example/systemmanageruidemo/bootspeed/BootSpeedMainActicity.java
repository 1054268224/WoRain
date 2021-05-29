package com.example.systemmanageruidemo.bootspeed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.actionpresent.BootSpeedPresent;
import com.example.systemmanageruidemo.actionview.BootSpeedView;
import com.example.systemmanageruidemo.adapter.RecyclerAdapter;
import com.example.systemmanageruidemo.bean.DataBean;
import com.example.systemmanageruidemo.bootspeed.adapter.BootAdapter;
import com.example.systemmanageruidemo.bootspeed.adapter.BootItem;

import java.util.ArrayList;
import java.util.List;

public class BootSpeedMainActicity extends BaseSupportProxyActivity<BootSpeedPresent> implements BootSpeedView {
    private Context mContext;
    private TextView bootPercent;
    private TextView bootTitle;
    private Button mBtnBootSpeed;

    private BootAdapter mAdaper;
    private RecyclerView mBootRecyclerView;
    private BootItem softItem;
    private List<BootItem> softItems = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();
        setContentView(R.layout.activity_boot_speed_acticity);
        initView();
        initData();
    }

    private void initView() {
        bootPercent = (TextView) findViewById(R.id.total_boot_percent);
        bootTitle = (TextView) findViewById(R.id.boot_speed_title);
        mBtnBootSpeed = (Button) findViewById(R.id.one_speed_btn);
        mBootRecyclerView = (RecyclerView) findViewById(R.id.boot_speed_item_view);
        mBootRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /*数据模拟*/
    private void initData() {
        mAdaper = new BootAdapter(mContext);
        mAdaper.setBootItemList(softItems);

        mBootRecyclerView.setAdapter(mAdaper);
        requestScore();
        initData(softItems);

        mBtnBootSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BootItem> selects = new ArrayList();
                for (BootItem item : softItems) {
                    if (item.getTrue()) {
                        selects.add(item);
                    }
                }
                if (selects.size() == 0) {
                    Toast.makeText(mContext, "no selected", Toast.LENGTH_LONG).show();
                } else {
                    selectListdo(softItems);
                }
            }
        });


    }

    BootSpeedPresent presenter;

    @Override
    public void setPresenter(BootSpeedPresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public BootSpeedPresent getPresenter(BootSpeedPresent presenter) {
        return presenter;
    }

    @Override
    public void requestScore() {
        presenter.onRequestScore();
    }

    @Override
    public void onResponseScore(int percent) {
        bootPercent.setText(String.valueOf(percent));
    }

    @Override
    public void initData(List<BootItem> list) {
        presenter.onInitData(list);
    }

    @Override
    public void selectListdo(List<BootItem> list) {
        presenter.onSelectListdo(list);
    }

    @Override
    public void onRefresh(List<BootItem> list) {
        bootTitle.setText(UnitUtil.getStr(mContext, R.string.app_running) + softItems.size() + UnitUtil.getStr(mContext, R.string.app_running_end));
        mAdaper.notifyDataSetChanged();
    }
}