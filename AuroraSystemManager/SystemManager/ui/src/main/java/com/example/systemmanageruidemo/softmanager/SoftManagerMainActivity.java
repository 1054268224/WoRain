package com.example.systemmanageruidemo.softmanager;

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
import com.example.systemmanageruidemo.MainActivity;
import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.actionpresent.SoftManagerPresent;
import com.example.systemmanageruidemo.actionview.SoftManagerView;
import com.example.systemmanageruidemo.softmanager.adapter.SoftAdapter;
import com.example.systemmanageruidemo.softmanager.adapter.SoftItem;

import java.util.ArrayList;
import java.util.List;

public class SoftManagerMainActivity extends BaseSupportProxyActivity<SoftManagerPresent> implements SoftManagerView {
    private Context mContext;

    private List<SoftItem> softItemList = new ArrayList<>();
    private Button mBtnSoftClean;
    private SoftAdapter softAdapter;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();
        setContentView(R.layout.activity_soft_manager_main);
        initView();
        initData();
    }
    
    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.softmanager_item_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        softAdapter = new SoftAdapter(mContext);
        softAdapter.setmSoftItemList(softItemList);
        recyclerView.setAdapter(softAdapter);
        mBtnSoftClean = (Button) findViewById(R.id.soft_one_clean_btn);
    }

    /*测试数据*/
    private void initData() {
        initData(softItemList);
        mBtnSoftClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "垃圾清理完成", Toast.LENGTH_LONG).show();
                selectListdo(softItemList);
            }
        });

    }

    SoftManagerPresent presenter;

    @Override
    public void setPresenter(SoftManagerPresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public SoftManagerPresent getPresenter(SoftManagerPresent presenter) {
        return presenter;
    }

    @Override
    public void initData(List<SoftItem> list) {
        presenter.onInitData(list);
    }

    @Override
    public void selectListdo(List<SoftItem> list) {
        presenter.onSelectListdo(list);
    }

    @Override
    public void onRefresh(List<SoftItem> list) {
        if (list.size() == 0 ){
            mBtnSoftClean.setEnabled(false);
            mBtnSoftClean.setFocusable(false);
            mBtnSoftClean.setBackground(getResources().getDrawable(R.drawable.one_clean_disable));
            mBtnSoftClean.setTextColor(getResources().getColor(R.color.item_gray));
        }
        softAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFinishPosition(boolean success, String packagename) {
        Toast.makeText(mContext, packagename + "卸载完毕", Toast.LENGTH_LONG).show();
    }
}