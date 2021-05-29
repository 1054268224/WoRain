package com.cydroid.systemmanager.antivirus;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.intel.security.SecurityContext;
import com.intel.security.vsm.ScanResult;
import com.intel.security.vsm.ScanStrategy;
import com.intel.security.vsm.ScanTask;
import com.intel.security.vsm.Threat;
import com.intel.security.vsm.VirusScan;
import com.intel.security.vsm.content.ScanApplication;
import com.intel.security.vsm.content.ScanSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import cyee.app.CyeeAlertDialog;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;

public class MonitorAppInstallService extends Service {
    private static final String TAG = "MonitorAppInstallService";
    private static boolean WRITE_LOG_FILE = false;
    private static final int DIALOG_SHOW = 201;
    private static final String INTENT_CMD_ACTION = "ACTION";
    private static final String INTENT_CMD_FILE = "APPEND";
    public static final String INTENT_SCAN_RESULT = "com.qvssdk.demo.SCANRESULT";
    public static final String INTENT_SCAN_TOTAL = "com.qvssdk.demo.SCANTOTAL";
    public static final String INTENT_SCAN_UNKNOWN = "com.qvssdk.demo.SCANUNKNOWN";
    private Context mContext;
    private Resources mRes;
    private String mPkgName;
    /** 杀毒引擎的调用接口 */
    private int mAppend = 0;
    private int mAction = 0;
    private int mScanTotal = 0;
    private int mScanUnknown = 0;
    private ArrayList<ScanResult> mScanResultList = new ArrayList<ScanResult>();
    private String mLogFileDir = null;
    private static final String mLogFileName = "scan_result";
    private StringBuilder mLogBuffer = new StringBuilder();

    private Handler mDialogHandler;
    private ScanResult mScanResult;
    private SharedPreferences mMonitorAppInstallPreferences;
	private  VirusScan mVirusScan =  null;
	    private ScanTask mScanTask;
    @Override
    public void onCreate() {
        mContext = this;
        mRes = mContext.getResources();
        mMonitorAppInstallPreferences = mContext.getSharedPreferences(AntiVirusPrefsFragment.AUTO_UPDATE_KEY,
                Context.MODE_PRIVATE);
		mVirusScan =  (VirusScan) SecurityContext
                .getService(getApplicationContext(), SecurityContext.VIRUS_SCAN);
        startDialogThread();
        Log.i(TAG, "onCreate---->");
    }

    private void startDialogThread() {
        new Thread(new DialogRunner()).start();
    }

    private class DialogRunner implements Runnable {

        @Override
        public void run() {
            Looper.prepare();
            mDialogHandler = new DiaLogHandler();
            Looper.loop();
        }
    }

    private class DiaLogHandler extends Handler {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DIALOG_SHOW:
                    //createDialog(mScanResult);
                    break;

            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //initData(intent);
        if (intent != null) {
            mPkgName = intent.getStringExtra("pkgname");
        }
        ServiceUtil.handleStartForegroundServices(MonitorAppInstallService.this);//xuanyuadd
		//guoxt modify for 49338 begin
		//scanInstall();
		//guoxt modify for 49338 end
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy---->");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind---->");
        // TODO Auto-generated method stub
        return null;
    }

    private void initData(Intent intent) {
        mLogFileDir = getApplicationContext().getFilesDir().getAbsolutePath();
        mAction = 0;// intent.getIntExtra(INTENT_CMD_ACTION, 0);
        mAppend = 1;// intent.getIntExtra(INTENT_CMD_FILE, 1);
    }




    //class MyScanObserver implements ScanObserver
    final MyScanObserver myScanObserver = new MyScanObserver("scan app")
    {
        private String mFrom;
        private long mOldTime = 0;
        private int count = 0;
		//String str = str1;
		
	

        @Override
        public void onStarted()
        {
            Log.d(TAG, "[" + mFrom + "] start scan.");
            mOldTime = System.currentTimeMillis();
            count = 0;
        }

        @Override
        public void onCompleted(int i)
        {

            long lTime = System.currentTimeMillis() - mOldTime;

            Log.d(TAG, "[" + mFrom + "] " + " Scan completed. Result " + i + ", consumed time " + lTime + " ms" +
                    " , scanned items " + count);
			 //handler.sendEmptyMessage(0);

			 //checkScanResult();
			//animPause();
			//showScanResult2();
            count = 0;

        }

        @Override
        public void onScanned(ScanSource source, ScanResult scanResult)
        {
            count++;

            if (scanResult.getThreat() != null)
            {
                Threat threat = scanResult.getThreat();
                //mThreatCount++;
                Log.d(TAG, "mThreatCount" +",[" + mFrom + "] " + source.toString() + " is a threat!");
				mScanResultList.add(scanResult);

            if (mScanResultList.size() > 0 ) {
                Message msg = mDialogHandler.obtainMessage(DIALOG_SHOW);
                mDialogHandler.sendMessage(msg);
            } else {
                MonitorAppInstallService.this.stopSelf();
            }
			
            }
            else
            {
               //Log.d(TAG, "[" + mFrom + "] " + source.toString() + " is clean.");
               Log.d(TAG, "progress:" + (int) (mScanTask.getState().getProgress() * 100) + "   ,packageName:" + source.toString() + ",is clean");
            }

            //Log.d("guoxt:", "progress:" + (int) (mScanTask.getState().getProgress() * 100) + "packageName:" + source.toString());
			 //showCurrentScan(source.toString(),(int) (mScanTask.getState().getProgress() * 100));	

            Object cloudErrCode = scanResult.getMeta(ScanResult.CLOUD_SCAN_ERROR);
            if (cloudErrCode != null && (cloudErrCode instanceof Integer))
            {
                Log.d(TAG, "[" + mFrom + "] " + "Cloud scan error code is " + ((Integer)cloudErrCode).intValue());
            }
        }
    };

//scall all apps and sdcard path
  public void scanInstall()
    {
        if (mVirusScan == null)
        {
         Toast.makeText(this, "mVirusScan is nulll", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (mScanTask != null)
        {
            Toast.makeText(this, "You have started a scan task, please wait...", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        mScanTask = mVirusScan.scan(new ScanApplication(mPkgName),
          new ScanStrategy()
			{
			@Override
			public int getTechnology(ScanSource scanSource)
			{
			return TECHNOLOGY_SIGNATURES;
			}
			}
 

		,myScanObserver);

    }


    private static void dump(ScanResult r) {
        final String tag = "dump";
       // String fmt = String.format("file:%s riskClass: %d level: %d", r.fileInfo.filePath, r.riskClass,
               // r.fileInfo.level);

        //Log.i(tag, fmt);
    }

    private void flushLogBuffer() {
        if (WRITE_LOG_FILE == true) {
            OutputStream output = null;
            try {
                output = new FileOutputStream(new File(mLogFileDir, mLogFileName), true);
                output.write(mLogBuffer.toString().getBytes("utf-8"));
                output.flush();
            } catch (IOException e) {
                Log.e(TAG, "", e);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException ex) {

                    }
                }
            }
            mLogBuffer.delete(0, mLogBuffer.length());
        }
    }


    private void initLogFile() {
        if ((WRITE_LOG_FILE == true) && (mAppend == 0)) {
            File log = new File(mLogFileDir, mLogFileName);
            if (log.exists() == true) {
                if (log.delete() == false) {
                    Log.e(TAG, "Log file:" + log.getAbsolutePath() + " delete failed.");
                }
            }
        }
    }


    private void initStatistic() {
        mScanResultList.clear();
        mScanTotal = 0;
        mScanUnknown = 0;
    }



    private void createDialog(ScanResult scanResult) {
        CyeeAlertDialog permDialog = new CyeeAlertDialog.Builder(mContext,
                CyeeAlertDialog.THEME_CYEE_FULLSCREEN).create();
        permDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //permDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        permDialog.setTitle(mRes.getString(R.string.monitor_install_title));
        ApplicationInfo info = getApplicationInfo(mContext, mPkgName);
        String appNameString = loadLabel(mContext, info);
        String virusNameString = "";//getRiskClassText(scanResult.riskClass);
        String message = appNameString + mRes.getString(R.string.monitor_install_message) + virusNameString;
        message += "\n" + mRes.getString(R.string.monitor_install_message2);
        permDialog.setMessage(message);
        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        uninstallApp(mPkgName);
                        MonitorAppInstallService.this.stopSelf();
                        break;

                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        MonitorAppInstallService.this.stopSelf();
                        break;
                    default:
                        break;
                }
            }

        };

        permDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, mRes.getString(R.string.monitor_uninstall),
                dialogClickLsn);
        permDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, mRes.getString(R.string.monitor_cancel),
                dialogClickLsn);
        permDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                MonitorAppInstallService.this.stopSelf();
            }
        });
        permDialog.show();
    }

    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static String loadLabel(Context context, ApplicationInfo info) {
        // Gionee <xuwen> <2015-08-25> add for CR01547240 begin
        if (null == info) {
            return "";
        }
        // Gionee <xuwen> <2015-08-25> add for CR01547240 end
        String result = info.loadLabel(context.getPackageManager()).toString();
        if (result == null) {
            result = info.packageName;
        }
        return result;
    }

    private void uninstallApp(String name) {
        Intent intent = new Intent();
        intent.putExtra("DELETE_PACKAGE_NAME", name);
        intent.setAction("com.chenyee.action.DELETE_PACKAGE");
        sendBroadcast(intent);
    }

}