package com.wheatek.proxy.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cydroid.softmanager.R;
import com.cydroid.systemmanager.BaseWheatekActivity;
import com.example.systemmanageruidemo.view.SlideIndexBar;

import com.mediatek.security.datamanager.CheckedPermRecord;

import com.mediatek.security.service.INetworkDataControllerService;
import com.wheatek.utils.AlerDialogUtils;
import com.wheatek.utils.HanziToPinyin;
import com.wheatek.utils.NetworkControllerUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NetWorkConTrollerActivity extends BaseWheatekActivity {
    private RecyclerView mRecycleview;
    private MAdapter madapter = new MAdapter();
    private SlideIndexBar mSlideindexbar;
    static final String TAG = NetWorkConTrollerActivity.class.getSimpleName();
    private INetworkDataControllerService mNetworkDataControllerBinder;
    private boolean mShouldUnbind;
    List<CheckedPermRecord> mlist_o = new ArrayList<>();
    List<ViewData> mlist2 = new ArrayList<>();
    Comparator comparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (o1.startsWith("#")) {
                return 1;
            }
            if (o2.startsWith("#")) {
                return -1;
            }
            return o1.compareTo(o2);
        }
    };
    //    Comparator comparator2 = new Comparator<ViewData>() {
//        @Override
//        public int compare(ViewData o1, ViewData o2) {
//            if (hanziToPinyin.transliterate(o1.name).toUpperCase().substring(0, 1).startsWith("#")) {
//                return 1;
//            }
//            if (hanziToPinyin.transliterate(o2.name).toUpperCase().substring(0, 1).startsWith("#")) {
//                return -1;
//            }
//            return hanziToPinyin.transliterate(o1.name).toUpperCase().substring(0, 1).compareTo(hanziToPinyin.transliterate(o2.name).toUpperCase().substring(0, 1));
//        }
//    };
    Map<String, Integer> map = new TreeMap<>(comparator);
    private String[] pmmision;
    private MOnSelectChangeListener mselectchangelis = new MOnSelectChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newwork_controll_activity);
        pmmision = getResources().getStringArray(R.array.permissionresult);
        initView();
        loadData();

    }

    private void loadData() {
        bindService();
    }

    private void initView() {
        mRecycleview = findViewById(R.id.recycleview);
        mRecycleview.setLayoutManager(new LinearLayoutManager(this));
        mRecycleview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRecycleview.requestFocus();
                return false;
            }
        });
        mRecycleview.setAdapter(madapter);
        mSlideindexbar = findViewById(R.id.slideindexbar);
//        mSlideindexbar.setVisibility(View.GONE);
    }

    private class MAdapter extends RecyclerView.Adapter<MViewHolder> {
        @NonNull
        @Override
        public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(NetWorkConTrollerActivity.this).inflate(R.layout.network_controller_item_container, parent, false);
            return new MViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
            holder.mAppName.setText(mlist2.get(position).name);
            holder.mAppIcon.setImageDrawable(mlist2.get(position).icon);
            holder.mPermissionResult.setText(mlist2.get(position).status);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showmDialog(v, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mlist2.size();
        }
    }

    Dialog alertDialog;

    private void showmDialog(View v, final int position) {
        if (alertDialog == null) {
            alertDialog = AlerDialogUtils.createSingleSelectDialog(((ViewGroup) getWindow().getDecorView()),
                    this, mlist2.get(position).name, pmmision, mlist2.get(position).getStatusindex(),
                    mselectchangelis);
        } else {
            AlerDialogUtils.refreshdialogdata(alertDialog, mlist2.get(position).name, mlist2.get(position).getStatusindex());
        }
        mselectchangelis.position = position;
        if (!alertDialog.isShowing())
            alertDialog.show();
    }

    int statuetoposition(int status) {
        int re = 0;
        switch (status) {
            case CheckedPermRecord.STATUS_DENIED:
                re = 2;
                break;
            case CheckedPermRecord.STATUS_WIFI_ONLY:
                re = 1;
                break;
            case CheckedPermRecord.STATUS_GRANTED:
                re = 0;
                break;
            default:
                re = 2;
                break;
        }
        return re;
    }

    int positiontostatue(int position) {
        int re = 0;
        switch (position) {
            case 2:
                re = CheckedPermRecord.STATUS_DENIED;
                break;
            case 1:
                re = CheckedPermRecord.STATUS_WIFI_ONLY;
                break;
            case 0:
                re = CheckedPermRecord.STATUS_GRANTED;
                break;
            default:
                re = CheckedPermRecord.STATUS_DENIED;
                break;
        }
        return re;
    }

    private String statustopositionstr(int status) {
        return pmmision[statuetoposition(status)];
    }

    private class MViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout mNetworkControllerItemLay;
        private ImageView mAppIcon;
        private TextView mAppName;
        private TextView mPermissionResult;

        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View itemView) {
            mNetworkControllerItemLay = itemView.findViewById(R.id.network_controller_item_lay);
            mAppIcon = itemView.findViewById(R.id.app_icon);
            mAppName = itemView.findViewById(R.id.app_name);
            mPermissionResult = itemView.findViewById(R.id.permission_result);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected, ComponentName = " + name);
            mNetworkDataControllerBinder = null;

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected, ComponentName = " + name);
            mNetworkDataControllerBinder = INetworkDataControllerService.Stub.asInterface(service);
            try {
                mlist_o = mNetworkDataControllerBinder.getNetworkDataRecordList();
                pingjiedata();
                runOnUiThread(() -> {
                    refresh();
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onServiceConnected, ComponentName = " + mNetworkDataControllerBinder);
        }
    };
    HanziToPinyin hanziToPinyin = HanziToPinyin.getInstance();

    private void pingjiedata() {
        mlist2.clear();
        map.clear();
        for (CheckedPermRecord checkedPermRecord : mlist_o) {
            ViewData viewData = new ViewData();
            String name = NetworkControllerUtils.getApplicationName(NetWorkConTrollerActivity.this, checkedPermRecord.getPackageName());
            if (name == null || name.length() < 1) continue;
            viewData.setPackageName(checkedPermRecord.getPackageName());
            viewData.setIcon(NetworkControllerUtils.getApplicationIcon(NetWorkConTrollerActivity.this, checkedPermRecord.getPackageName()));
            viewData.setName(name);
            viewData.setOrigin(checkedPermRecord);
            viewData.setStatus(statustopositionstr(checkedPermRecord.getStatus()));
            viewData.setStatusindex(statuetoposition(checkedPermRecord.getStatus()));
            mlist2.add(viewData);
        }
        mlist2.sort((o1, o2) -> {
            return hanziToPinyin.transliterate(o1.name).toUpperCase().substring(0, 1).compareTo(hanziToPinyin.transliterate(o2.name).toUpperCase().substring(0, 1));
//            return o1.name.compareTo(o2.name);
        });
        if (mlist2.size() > 15) {
            int pos = 0;
            for (ViewData viewData : mlist2) {
                if (viewData.name.length() > 0) {
                    String charse = hanziToPinyin.transliterate(viewData.name).toUpperCase().substring(0, 1);
                    if (map.getOrDefault(charse, -1) == -1) {
                        map.put(charse, new Integer(pos));
                    }
                }
                pos++;
            }
            int current = 0;
            for (String indexstring : mSlideindexbar.getIndexstrings()) {
                if (map.getOrDefault(indexstring, -1) == -1) {
                    map.put(indexstring, new Integer(current));
                } else {
                    current = map.get(indexstring);
                }
            }
//        Iterator<String> it = map.keySet().iterator();
//        while (it.hasNext()) {
//            String key = it.next();
//            int theee = map.getOrDefault(key, 0);
//            if (theee > current) {
//                current = theee;
//            }
//            map.put(key, new Integer(current));
//        }
            mSlideindexbar.setonSlideActionListener(new SlideIndexBar.onSlideActionListener() {
                @Override
                public void onResetAction() {

                }

                @Override
                public void onMoveAction(String actioncCharindex) {
                    if (map.getOrDefault(actioncCharindex, -1) != -1)
                        mRecycleview.scrollToPosition(map.get(actioncCharindex));
                }

                @Override
                public void onClickAction(String actioncCharindex) {
                    if (map.getOrDefault(actioncCharindex, -1) != -1)
                        mRecycleview.scrollToPosition(map.get(actioncCharindex));
                }
            });
        } else {
            mSlideindexbar.setVisibility(View.GONE);
        }
    }

    private void refresh() {
        madapter.notifyDataSetChanged();
    }

    private void bindService() {
        Intent intent = new Intent("com.mediatek.security.START_SERVICE");
        intent.setClassName("com.mediatek.security.service",
                "com.mediatek.security.service.NetworkDataControllerService");
        mShouldUnbind = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (mShouldUnbind) {
            unbindService(mServiceConnection);
            mShouldUnbind = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
    }

    private class ViewData {
        String name;
        Drawable icon;
        String status;
        int statusindex;
        CheckedPermRecord origin;

        public CheckedPermRecord getOrigin() {
            return origin;
        }

        public void setOrigin(CheckedPermRecord origin) {
            this.origin = origin;
        }

        public int getStatusindex() {
            return statusindex;
        }

        public void setStatusindex(int statusindex) {
            this.statusindex = statusindex;
        }

        String packageName;

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private class MOnSelectChangeListener implements AlerDialogUtils.OnSelectChangeListener {
        int position;

        @Override
        public void OnSelectChange(int i) {
            mlist2.get(position).getOrigin().setStatus(positiontostatue(i));
            mlist2.get(position).setStatusindex(i);
            mlist2.get(position).setStatus(statustopositionstr(mlist2.get(position).getOrigin().getStatus()));
            madapter.notifyItemChanged(position);
            try {
                mNetworkDataControllerBinder.modifyNetworkDateRecord(mlist2.get(position).getOrigin());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
