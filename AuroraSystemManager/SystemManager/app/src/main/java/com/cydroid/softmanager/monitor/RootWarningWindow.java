//Gionee <jianghuan> <2014-04-01> modify for CR01097010 begin
package com.cydroid.softmanager.monitor;

import java.io.File;
import java.util.ArrayList;

import com.cydroid.softmanager.common.ExecuteAsRoot;
import com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity;
import com.cydroid.softmanager.trafficassistant.TrafficPopWindows;
import com.cydroid.softmanager.utils.Log;

import com.cydroid.softmanager.R;

import cyee.app.CyeeAlertDialog;
import cyee.app.CyeeActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

public class RootWarningWindow extends CyeeActivity {

    private static final int MSG_OK = 0;
    private Context mContext;
    private int rootFlag;
    private Handler mHandler;
    private SharedPreferences mShare;
    private static final String binSuPath = "/system/bin/su";
    private static final String xbinSuPath = "/system/xbin/su";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        mContext = this;
        mShare = mContext.getSharedPreferences(RootMonitor.ROOT, Context.MODE_MULTI_PROCESS);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = this.getIntent();
        rootFlag = intent.getIntExtra("rootflag", 0);

        initHandler(mContext);

        popDialog();
    }

    private void initHandler(Context context) {

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_OK:
                        // Gionee <jianghuan> <2014-06-24> modify for CR01286963
                        // begin
                        if (checkResult()) {
                            Toast.makeText(mContext, R.string.toast_repair_fail, Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(mContext, R.string.toast_repair_success, Toast.LENGTH_LONG).show();
                            SetSharePreIntValue(mShare, RootMonitor.ROOT_LEVEL, 0);
                            SetSharePreBooleanValue(mShare, RootMonitor.ROOT, false/* true */);
                        }

                        // SetSharePreBooleanValue(mShare, RootMonitor.ROOT,
                        // false/*true*/);
                        // SetSharePreIntValue(mShare, RootMonitor.ROOT_LEVEL, 0);
                        // Gionee <jianghuan> <2014-06-24> modify for CR01286963 end
                        break;
                    default:
                        break;
                }
            }

        };
    }

    // Gionee <jianghuan> <2014-06-24> add for CR01286963 begin
    private boolean checkResult() {
        boolean exist = false;
        File file1 = new File(binSuPath);
        File file2 = new File(xbinSuPath);
        if (file1.exists() || file2.exists()) {
            exist = true;
        }

        return exist;
    }

    // Gionee <jianghuan> <2014-06-24> add for CR01286963 end
    private void popDialog() {
        CyeeAlertDialog alertDialog = new CyeeAlertDialog.Builder(mContext)
                // .setIcon(R.drawable.permission_manager)
                .setTitle(getTitle(mContext, rootFlag))
                .setMessage(getContent(mContext, rootFlag))
                .setPositiveButton(getPositiveButton(mContext, rootFlag),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO Auto-generated method stub
                                onButtonEvent(mContext, rootFlag);
                                RootWarningWindow.this.finish();

                            }
                        })
                .setNegativeButton(getNegativeButton(mContext, rootFlag),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO Auto-generated method stub
                                SetSharePreBooleanValue(mShare, RootMonitor.ROOT, true);
                                RootWarningWindow.this.finish();

                            }
                        }).create();

        alertDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

                    SetSharePreBooleanValue(mShare, RootMonitor.ROOT, true);
                    dialog.dismiss();
                    RootWarningWindow.this.finish();

                }
                return false;
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void SetSharePreBooleanValue(SharedPreferences share, String key, boolean value) {
        share.edit().putBoolean(key, value).commit();
    }

    private void SetSharePreIntValue(SharedPreferences share, String key, int value) {
        share.edit().putInt(key, value).commit();
    }

    private String getTitle(Context context, int flag) {
        String title = "";
        if (1 == flag) {
            title = context.getString(R.string.root_repair_title);
        } else if (2 == flag) {
            title = context.getString(R.string.root_repair_title);
        }

        return title;
    }

    private String getContent(Context context, int flag) {
        String content = "";
        if (1 == flag) {
            content = context.getString(R.string.root_repair_content);
        } else if (2 == flag) {
            content = context.getString(R.string.root_ota_repair_content);
        }

        return content;
    }

    private String getPositiveButton(Context context, int flag) {
        String positiveButton = "";
        if (1 == flag) {
            positiveButton = context.getString(R.string.button_repair);
        } else if (2 == flag) {
            positiveButton = context.getString(R.string.button_ota_repair);
        }

        return positiveButton;
    }

    private String getNegativeButton(Context context, int flag) {
        String negativeButton = "";
        if (1 == flag) {
            negativeButton = context.getString(R.string.button_close);
        } else if (2 == flag) {
            negativeButton = context.getString(R.string.button_close);
        }

        return negativeButton;
    }

    private int getIcon(int flag) {
        int icon = 0;
        if (1 == flag) {
            icon = R.drawable.permission_manager;
        } else if (2 == flag) {
            icon = R.drawable.permission_manager;
        }
        return icon;
    }

    private void onButtonEvent(Context context, int flag) {

        if (1 == flag) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    ArrayList<String> array = new ArrayList<String>();
                    array.add("mount -o remount rw /system");
                    array.add("chattr -i /system/bin/su");
                    array.add("chattr -i /system/xbin/su");
                    array.add("rm -rf /system/xbin/su");
                    array.add("rm -rf /system/bin/su");
                    array.add("pm uninstall com.qihoo.root");
                    array.add("mount -o remount ro /system");
                    ExecuteAsRoot.execute(array);
                    try {
                        SystemClock.sleep(2000);
                    } catch (Exception ex) {

                    }
                    sendMessage(MSG_OK);

                    // YouJuAgent.onEvent(mContext, "Root_Recover");
                }
            }).start();

        } else if (2 == flag) {
            try {
                Intent intent = new Intent("gn.com.android.update.action.recover.system");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.d("TAG", ex + "");
            }
        }
    }

    private void sendMessage(int message) {
        Message m = new Message();
        m.what = message;
        mHandler.sendMessage(m);
    }
}
// Gionee <jianghuan> <2014-04-01> modify for CR01097010 end
