package com.example.systemmanageruidemo.setting;

import android.content.Context;
import android.hardware.radio.V1_0.SrvccState;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.actionpresent.SettingPresent;
import com.example.systemmanageruidemo.actionview.SettingView;
import com.example.systemmanageruidemo.setting.adapter.SettingAdapter;
import com.example.systemmanageruidemo.setting.bean.BaseItem;
import com.example.systemmanageruidemo.setting.bean.BtnItem;
import com.example.systemmanageruidemo.setting.bean.SetItem;
import com.example.systemmanageruidemo.setting.bean.SwitchItem;
import com.example.systemmanageruidemo.setting.bean.TBtnItem;
import com.example.systemmanageruidemo.setting.bean.TSwitchItem;

import java.util.ArrayList;
import java.util.List;


public class SettingMainActivity extends BaseSupportProxyActivity<SettingPresent> implements SettingView {
    private Context mContext;
    private RecyclerView recyclerView;
    private SettingAdapter adapter = new SettingAdapter(getRealContext());
    private BaseItem item;
    private List<BaseItem> items = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();
        setContentView(R.layout.activity_setting_main);
        recyclerView = (RecyclerView) findViewById(R.id.setting_item_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        initData();
        adapter.setItems(items);


    }


    private void initData() {
       items.add(Item(1,R.string.rub_set));
       items.add(bItem(2,R.string.rub_size));
       items.add(swItem(3,R.string.package_delete,false));
       items.add(swItem(4,R.string.file_delete,false));
       items.add(Item(5,R.string.powermanager_set));
       items.add(tSwItem(6,R.string.smart_save,R.string.smart_save_detail,false));
       items.add(tSwItem(7,R.string.light,R.string.light_detai,false));
       items.add(tBItem(8,R.string.screen_sleep,R.string.screen_sleep_detail));
       items.add(swItem(9,R.string.wifi,false));
       items.add(swItem(10,R.string.bluetooth,false));
       items.add(swItem(11,R.string.power_per,false));
       items.add(Item(12,R.string.traffic_set));
       items.add(tBItem(13,R.string.flow_share,R.string.flow_share_detail));
       items.add(Item(14,R.string.monitor_set));
       items.add(bItem(15,R.string.flow_monitor));
       items.add(bItem(16,R.string.power_monitor));
       items.add(bItem(17,R.string.app_monitor));
       items.add(bItem(18,R.string.high_power_monitor));
       items.add(bItem(19,R.string.memory_moniter));
    }

    /*获取String资源*/
    private String str(int id){
        return UnitUtil.getStr(mContext,id);
    }

    /*设置item*/
    private BaseItem Item(int id, int idStr){
        return new SetItem(id,str(idStr));
    }

    /*单 行文字btn item*/
    private BaseItem bItem(int id, int idStr){
        return new BtnItem(id,str(idStr));
    }

    /*双 行文字btn item*/
    private BaseItem tBItem(int id, int idStr1, int idStr2){
        return new TBtnItem(id,str(idStr1),str(idStr2));
    }

    /*单 行文字switch item*/
    private BaseItem swItem(int id, int idStr, boolean b){
        return new SwitchItem(id,str(idStr),b);
    }

    /*双 行文字switch item*/
    private BaseItem tSwItem(int id, int idStr1, int idStr2,boolean b){
        return new TSwitchItem(id,str(idStr1),str(idStr2),b);
    }

    SettingPresent presenter;

    @Override
    public void setPresenter(SettingPresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public SettingPresent getPresenter(SettingPresent presenter) {
        return presenter;
    }
}