package com.wheatek.proxy.ui;

import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerDataManager;
import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerDelListener;
import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerScanListener;
import com.cydroid.systemmanager.rubbishcleaner.common.CleanTypeConst;
import com.example.systemmanageruidemo.RubbishCleanerMainActivity;
import com.example.systemmanageruidemo.actionpresent.RubbishCleanPresent;
import com.example.systemmanageruidemo.actionview.RubbishCleanView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.example.systemmanageruidemo.bean.DataBean;
import com.keniu.security.CleanMasterSDK;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeListView;

import com.cydroid.systemmanager.BaseActivity;
import com.cydroid.systemmanager.RuntimePermissionsManager;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.StringFilterUtil;
import com.cydroid.systemmanager.ui.UiAnimator;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UiUtils;
import com.cydroid.systemmanager.utils.Util;

import static com.cydroid.systemmanager.rubbishcleaner.common.MsgConst.DATA_MANAGER_TYPE_NORMAL;

public class HostRubbishCleanerMainActivity extends HostProxyActivity<RubbishCleanView> implements RubbishCleanPresent, RubbishCleanerScanListener,
        RubbishCleanerDelListener {
    {
        attach(new RubbishCleanerMainActivity());
    }

    RubbishCleanView viewAvtion;

    @Override
    public void setViewAction(RubbishCleanView viewAvtion) {
        this.viewAvtion = viewAvtion;
    }

    @Override
    public RubbishCleanView getViewAction() {
        return viewAvtion;
    }

    private Context mContext;
    private Resources mResources;
    private static boolean DEBUG = true;
    private static String TAG = HostRubbishCleanerMainActivity.class.getSimpleName();

    private RubbishCleanerDataManager mRubbishCleanerDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            CleanMasterSDK.getInstance().Initialize(getApplicationContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        initFields();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRubbishCleanerDataManager != null) {
            mRubbishCleanerDataManager.unregisterRubbishCleanerScanListener(this);
            mRubbishCleanerDataManager.unregisterRubbishCleanerDelListener(this);
            mRubbishCleanerDataManager.unBindKSCleanerService(this);
            mRubbishCleanerDataManager.recycle();
            mRubbishCleanerDataManager = null;
        }

    }

    private void initFields() {
        mContext = this;
        mResources = getResources();
        mRubbishCleanerDataManager = RubbishCleanerDataManager.getInstance();
        mRubbishCleanerDataManager.recycle();
        mRubbishCleanerDataManager.init(this, DATA_MANAGER_TYPE_NORMAL, this);
        mRubbishCleanerDataManager.registerRubbishCleanerDelListener(this);

    }

    private void startScanRubbish() {
        mRubbishCleanerDataManager.bindKSCleanerService(this);
    }

    private void startDeleteRubbish() {

        new Thread() {
            public void run() {
                if (null != mRubbishCleanerDataManager) {
                    mRubbishCleanerDataManager.startDeleteRubbish(DATA_MANAGER_TYPE_NORMAL);
                }
            }
        }.start();
    }

    @Override
    public void onScanStart() {
        Log.d(TAG, "??");
        i = 0;
    }

    @Override
    public void onScanItem(int group, Object obj) {
        Log.d(TAG, "??" + obj.toString());
    }

    @Override
    public void onFindItem(int group, Object obj) {
        Log.d(TAG, "??findlshy" + obj.toString());
        if (list != null) {
            viewAvtion.onFinditem(list.get(group), group, null);
        }
    }

    private void calculatetoBeanRef(int group) {
        // 标记选中
        for (DataBean.DataBeanChild child : list.get(group).getChildren()) {
            for (RubbishInfo info : mRubbishCleanerDataManager.getGroupData(DATA_MANAGER_TYPE_NORMAL, group)) {
                if (child.getId() == info.db_id) {
                    info.isChecked = child.getChildTrue();
                    break;
                }
            }
        }
    }

    private void calculatetoBean(int group) {
        synchronized (list.get(group).getChildren()) {
            Iterator<DataBean.DataBeanChild> iterator = list.get(group).getChildren().iterator();
            while (iterator.hasNext()) {
                DataBean.DataBeanChild theitem = iterator.next();
                boolean isyes = false;
                for (RubbishInfo info : mRubbishCleanerDataManager.getGroupData(DATA_MANAGER_TYPE_NORMAL, group)) {
                    if (theitem.getId() == info.db_id) {
                        isyes = true;
                        break;
                    }
                }
                if (!isyes) {
                    iterator.remove();
                }
            }
            for (RubbishInfo info : mRubbishCleanerDataManager.getGroupData(DATA_MANAGER_TYPE_NORMAL, group)) {
                boolean isnin = false;
                DataBean.DataBeanChild thechild = null;
                for (DataBean.DataBeanChild child : list.get(group).getChildren()) {
                    if (child.getId() == info.db_id) {
                        isnin = true;
                        thechild = child;
                        break;
                    }
                }
                if (isnin && thechild != null) {
                    thechild.setId(info.db_id);  // 需要标记选中的标识符。
                    thechild.setChildTrue(info.isChecked);
                    thechild.setChildLeftTxt(info.name);
                    thechild.setChildRightTxt(info.size);
                    thechild.setImageId(info.icon);
                } else {
                    DataBean.DataBeanChild child = new DataBean.DataBeanChild();
                    child.setId(info.db_id);  // 需要标记选中的标识符。
                    child.setChildTrue(true);
                    info.isChecked = true;
                    child.setParent(list.get(group));
                    child.setChildLeftTxt(info.name);
                    child.setChildRightTxt(info.size);
                    child.setImageId(info.icon);
                    list.get(group).getChildren().add(child);
                }
            }
        }
    }

    int i = 0;

    @Override
    public void onScanEnd(int group) {
        Log.d(TAG, "??group" + group);
        i++;
        if (list != null) {
            calculatetoBean(group);
            if (i == list.size()) {
                viewAvtion.onFiniesScan();
            }
        }

    }

    @Override
    public void onDelItem(Object obj) {
        Log.d(TAG, "" + obj.toString());
    }

    @Override
    public void onDelItemByDialog(Object obj) {
        Log.d(TAG, "" + obj.toString());
    }

    @Override
    public void onDelEnd() {
        Log.d(TAG, "end");
        for (int i1 = 0; i1 < list.size(); i1++) {
            calculatetoBean(i1);
        }
        refresh(list);
    }

    List<DataBean> list;

    @Override
    public void onInitData(List<DataBean> list) {
        this.list = list;
        startScanRubbish();
    }

    @Override
    public void refresh(List<DataBean> list) {

        viewAvtion.onRefresh(list);
    }

    @Override
    public void onCleanRubbish(List<DataBean> list) {
        for (int i1 = 0; i1 < list.size(); i1++) {
            calculatetoBeanRef(i1);
        }
        startDeleteRubbish();
    }


}