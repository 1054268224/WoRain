package com.example.systemmanageruidemo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.actionpresent.RubbishCleanPresent;
import com.example.systemmanageruidemo.actionview.RubbishCleanView;
import com.example.systemmanageruidemo.adapter.RecyclerAdapter;
import com.example.systemmanageruidemo.bean.DataBean;

import java.util.ArrayList;
import java.util.List;

public class RubbishCleanerMainActivity extends BaseSupportProxyActivity<RubbishCleanPresent> implements RubbishCleanView {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;

    private DataBean dataBean;
    private List<DataBean> dataBeanList = new ArrayList<>();
    private DataBean.DataBeanChild child;

    private TextView mRubbishSize;
    private TextView mRubbishUnit;
    private Button mBtnRubclean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getRealContext();

        setContentView(R.layout.activity_rubbish_main);
        initView();
        initData();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rub_item_view);
        mRubbishSize = (TextView) findViewById(R.id.total_rubbish_size);
        mRubbishUnit = (TextView) findViewById(R.id.total_rubbish_unit);
        mBtnRubclean = (Button) findViewById(R.id.rub_one_cleaner_btn);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new RecyclerAdapter(mContext, dataBeanList);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void initData() {
        String[] type = {UnitUtil.getStr(mContext, R.string.cache_type), UnitUtil.getStr(mContext, R.string.ad_type),
                UnitUtil.getStr(mContext, R.string.unload_type), UnitUtil.getStr(mContext, R.string.package_type)};
        /*rubbish item*/
        for (int i = 1; i <= 4; i++) {
            dataBean = new DataBean(type[i - 1], 0, false);
//            dataBean.setChildren(new ArrayList<>());
//            for (int i1 = 0; i1 < 4; i1++) {
//                child = new DataBean.DataBeanChild(mContext.getResources().getDrawable(R.drawable.boot_speed_new), "应用" + i, ((long) (Math.random() * 1000)), true);
//                child.setParent(dataBean);
//                dataBean.getChildren().add(child);
//            }
            dataBeanList.add(dataBean);
        }
        mBtnRubclean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanRubbish(dataBeanList);
            }
        });

        initData(dataBeanList);
//        onRefresh(dataBeanList);

    }

    RubbishCleanPresent presenter;

    @Override
    public void setPresenter(RubbishCleanPresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public RubbishCleanPresent getPresenter(RubbishCleanPresent presenter) {
        return presenter;
    }

    @Override
    public void initData(List<DataBean> list) {
        presenter.onInitData(list);
    }

    boolean finishedscan = false;

    @Override
    public void onFiniesScan() {
        finishedscan = true;
        long total = 0;
        for (DataBean bean : dataBeanList) {
            total += calculate(bean);
        }
        mRubbishSize.setText(UnitUtil.convertStorage(total));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh(List<DataBean> list) {
        long total = 0;
        for (DataBean bean : list) {
            total += calculate(bean);
        }
        if (!finishedscan) {
            mRubbishSize.setText(UnitUtil.convertStorage(total));
        }
        mAdapter.notifyDataSetChanged();
    }

    public static long calculate(DataBean bean) {
        long total = 0;
        int i = 0;
        for (DataBean.DataBeanChild child : bean.getChildren()) {
            if (child.getChildTrue()) {
                i++;
                total += child.getChildRightTxt();
            }
        }
        bean.setParentRightTxt(total);
        if (i == bean.getChildren().size()) {
            bean.setParentTrue(true);
        }
        if (i == 0) {
            bean.setParentTrue(false);
        }
        return total;
    }

    @Override
    public void cleanRubbish(List<DataBean> list) {
        presenter.onCleanRubbish(list);

    }

    @Override
    public void onFinditem(DataBean dataBean, int index, DataBean.DataBeanChild dataBeanChild) {
        onRefresh(dataBeanList);
    }

    @Override
    public void onDeleteitem(DataBean dataBean, int index, DataBean.DataBeanChild dataBeanChild) {
        onRefresh(dataBeanList);
    }
}