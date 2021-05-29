package com.cydroid.systemmanager.antivirus;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.Telephony;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.intel.security.SecurityContext;
import com.intel.security.vsm.RealTimeScan;
import com.intel.security.vsm.ScanResult;
import com.intel.security.vsm.ScanStrategy;
import com.intel.security.vsm.ScanTask;
import com.intel.security.vsm.Threat;
import com.intel.security.vsm.UpdateTask;
import com.intel.security.vsm.VirusScan;
import com.intel.security.vsm.content.ScanApplication;
import com.intel.security.vsm.content.ScanApplications;
import com.intel.security.vsm.content.ScanCombination;
import com.intel.security.vsm.content.ScanMultimediaMessage;
import com.intel.security.vsm.content.ScanMultimediaMessages;
import com.intel.security.vsm.content.ScanPath;
import com.intel.security.vsm.content.ScanSource;
import com.intel.security.vsm.content.ScanTextMessage;
import com.intel.security.vsm.content.ScanTextMessages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import com.cydroid.systemmanager.BaseActivity;
import com.cydroid.systemmanager.RuntimePermissionsManager;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UiUtils;
import com.cydroid.systemmanager.utils.UnitUtil;

//import android.support.annotation.NonNull;

public class AntivirusActivity extends BaseActivity {
    private static final String TAG = "Antivirus_log:";

    public static final String SCAN_TIME = "scan_time";
    public static final String SCAN_ACTION = "scan_action";
    private static boolean WRITE_LOG_FILE = false;
    private static final String INTENT_CMD_ACTION = "ACTION";
    private static final String INTENT_CMD_FILE = "APPEND";
    private static final String INTENT_SCAN_RESULT = "com.qvssdk.demo.SCANRESULT";
    private static final String INTENT_SCAN_TOTAL = "com.qvssdk.demo.SCANTOTAL";
    private static final String INTENT_SCAN_UNKNOWN = "com.qvssdk.demo.SCANUNKNOWN";
    private static final String MESSAGE_INTENT = "com.action.antivirus.del.text.msg";
    private static final int ANTIVIRUSLAYOUT = 1;
    private static final int SCANLAYOUT = 2;
    private static final int RESULTLAYOUT = 3;
    private static final int CLEANDONELAYOUT = 4;

    //guoxt modify for CR01775652 begin
    private static final int TYPE_TEXT_MESSAGE = 0;
    private static final int TYPE_MULT_MESSAGE = 1;
    private static final int TYPE_APP = 2;
    private static final int TYPE_FILE = 3;
    //guoxt modify for CR01775652 end

    private int mWhichView = ANTIVIRUSLAYOUT;
    private int mAppend = 0;
    private int mAction = 0;
    private int mScanTotal = 0;
    private int mScanUnknown = 0;
    /**
     * 杀毒引擎的调用接口
     */
    //guoxt modify for CR01775652 begin
    private ArrayList<ThreatInfo> mScanResultList = new ArrayList<ThreatInfo>();
    //guoxt modify for CR01775652 end
    private ArrayList<String> mScanPkgNameList = new ArrayList<String>();
    private String mLogFileDir = null;
    private static final String mLogFileName = "scan_result";
    private StringBuilder mLogBuffer = new StringBuilder();
    private int mScanType;
    private boolean mDismiss = true;
    private Resources mRes;
    private Context mContext;
    private TextView mLastScanTextView, mScanSugesTextView;
    private LinearLayout mButtonContainer;
    private Button mQuickScanButton, mAllScanButton;
    private SharedPreferences mScanTimePreferences;
    private CyeeActionBar mActionbar;
    private View mMainView;
    private RelativeLayout mAntiVirusLayout, mScanLayout, mCleanDoneLayout, mResultTopview;
    private LinearLayout mResultLayout;
    private Animation mScaningAnim;
    private ImageView mAntiVirusScaning;
    private Button mCancelButton;
    private TextView mScanItemTextView;
    private TextView mCleandone;
    private Button mCleanDoneButton;
    private TextView mVirusNum;
    private ListView mVirusListView = null;
    private CyeeButton mClean = null;
    private TextView mVirusSupport;
    private TextView mCleanVirusSupport;
    private TextView mScanVirusSupport;
    private TextView mResultVirusSupport;


    // Gionee <houjie> <2015-11-13> add for CR01565278 begin
    private ImageView mVirusLogo;
    private ImageView mVirusScaningBg;
    private ImageView mVirusCleanDoneLogo;
    // Gionee <houjie> <2015-11-13> add for CR01565278 end

    // Gionee <xuwen><2015-07-29> add for CR01527472 begin
    CyeeAlertDialog mPermDialog;

    // Gionee <xuwen><2015-07-29> add for CR01527472 end
    // Gionee <yangxinruo><2015-10-14> add for CR01568183 begin
    private TextView mScanPercentTextView;
    // Gionee <yangxinruo><2015-10-14> add for CR01568183 end

    //guoxt modify for CR01775652 begin
    private VirusScan mVirusScan = null;
    private static final int REQ_CODE_PERMISSION = 1;
    private long mThreatCount = 0;
    private RealTimeScan mRealTimeScan = null;
    private UpdateTask mUpdateTask = null;
    private ScanTask mScanTask;
    private ThreatInfo threatinfoToast = null;
    //guoxt modify for CR01775652 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setElevation(getCyeeActionBar(), 0);
        // Gionee: <houjie> <2015-12-26> add for CR01614703 begin
        if (redirectToPermissionCheckIfNeeded()) {
            return;
        }
        // Gionee: <houjie> <2015-12-26> add for CR01614703 end
        setContentView(R.layout.antivirus_activity);
        mContext = this;
        initAnim();
        initView(this);
        setSecondClass(AntivirusSettingActivity.class);
        setFirstLayoutVisibility(View.GONE);
        chameleonColorProcess();

        //guoxt modify for CR01775652 begin
        waitSDKInitialize();
        //guoxt modify for CR01775652 end
    }

    private boolean isSystemApp(Context context) {
        if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return false;
        } else {
            return true;
        }
    }

    //class MyScanObserver implements ScanObserver
    final MyScanObserver myScanObserver = new MyScanObserver("scan all") {
        private String mFrom;
        private long mOldTime = 0;
        private int count = 0;
        String eventName = "Antivirus";

        @Override
        public void onStarted() {
            Log.d("guoxt", "[" + mFrom + "] start scan.");
            mOldTime = System.currentTimeMillis();
            count = 0;
        }

        @Override
        public void onCompleted(int i) {
            long lTime = System.currentTimeMillis() - mOldTime;

            Log.d("guoxt:", "[" + mFrom + "] " + " Scan completed. Result " + i + ", consumed time " + lTime + " ms" +
                    " , scanned items " + count);

            handler.sendEmptyMessage(0);

            //showScanResult2();
            count = 0;
        }

        @Override
        public void onScanned(ScanSource source, ScanResult scanResult) {
            count++;

            if (scanResult.getThreat() != null) {
                Threat threat = scanResult.getThreat();
                mThreatCount++;

                // Log.d(TAG, "mThreatCount" + mThreatCount+",[" + threat.getName() + "] " + source.toString() + " is a threat!"  + "threat.getVariant():"+ threat.getVariant());
                ThreatInfo threatInfo = new ThreatInfo();
                threatInfo.source = source;

                if (source instanceof ScanMultimediaMessage) {
                    threatInfo.type = TYPE_MULT_MESSAGE;
                    eventName = "SM_Antivirus_SMS";
                } else if (source instanceof ScanTextMessage) {
                    threatInfo.type = TYPE_TEXT_MESSAGE;
                    eventName = "SM_Antivirus_MMS";
                } else if (source instanceof ScanPath) {
                    threatInfo.type = TYPE_FILE;
                    eventName = "SM_Antivirus_File";
                } else if (source instanceof ScanApplication) {
                    threatInfo.type = TYPE_APP;
                    eventName = "SM_Antivirus_App";
                }
                threatInfo.thread = threat;
                mScanResultList.add(threatInfo);

            } else {
                Log.d(TAG, "progress:" + (int) (mScanTask.getState().getProgress() * 100) + "   ,packageName:" + source.toString() + ",is clean");
            }
            showCurrentScan(source.toString(), (int) (mScanTask.getState().getProgress() * 100));
            Object cloudErrCode = scanResult.getMeta(ScanResult.CLOUD_SCAN_ERROR);
            if (cloudErrCode != null && (cloudErrCode instanceof Integer)) {
                Log.d(TAG, "[" + mFrom + "] " + "Cloud scan error code is " + ((Integer) cloudErrCode).intValue());
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int results : grantResults) {
            if (results != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        waitSDKInitialize();
    }

    private void waitSDKInitialize() {
        if (SecurityContext.isInitialized(getApplicationContext())) {
            SDKInitialized();
        } else {
            Log.d("guoxt:", "Initialize");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mScanButtons.setVisibility(View.GONE);
                    //mInitProgressBar.setVisibility(View.VISIBLE);
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SecurityContext.waitUntilInitialized(getApplicationContext());

                    SDKInitialized();
                }
            }).start();
        }
    }

    private void SDKInitialized() {
        mVirusScan = (VirusScan) SecurityContext
                .getService(getApplicationContext(), SecurityContext.VIRUS_SCAN);
        mRealTimeScan = mVirusScan.getRealTimeScan();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mScanButtons.setVisibility(View.VISIBLE);
                //mInitProgressBar.setVisibility(View.GONE);
                // renderRealTimeOption();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
        if (mWhichView == SCANLAYOUT) {
            mActionbar.setDisplayHomeAsUpEnabled(false);
            mActionbar.setDisplayShowCustomEnabled(false);
        } else if (mWhichView == RESULTLAYOUT) {
            refreshResultLayout();
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        // initStatistic();
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mAntiVirusLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mScanLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mResultTopview.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mCleanDoneLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mLastScanTextView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mScanSugesTextView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());

            mQuickScanButton.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mAllScanButton.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mVirusSupport.setTextColor(ChameleonColorManager.getContentColorThirdlyOnAppbar_T3());
            mCleanVirusSupport.setTextColor(ChameleonColorManager.getContentColorThirdlyOnAppbar_T3());
            mCancelButton.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mCleanDoneButton.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());

            // Gionee <houjie> <2015-11-13> add for CR01565278 begin
            mVirusLogo.getBackground().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mVirusCleanDoneLogo.getBackground()
                    .setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mAntiVirusScaning.getDrawable()
                    .setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mVirusScaningBg.getDrawable().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            // Gionee <houjie> <2015-11-13> add for CR01565278 end
        }
    }

    private void initData() {
        mScanTimePreferences = this.getSharedPreferences(SCAN_TIME, Context.MODE_PRIVATE);
        long lasttime = mScanTimePreferences.getLong(SCAN_TIME, 0);
        long currenttime = System.currentTimeMillis();
        if (lasttime == 0 || currenttime - lasttime <= 0) {
            mLastScanTextView.setText(mRes.getString(R.string.antivirus_none_scan));
            mScanSugesTextView.setText(mRes.getString(R.string.antivirus_scan_sugest));
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
            Date curDate = new Date(lasttime);// 获取当前时间
            String str = formatter.format(curDate);
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(R.string.last_scan_time));
            builder.append(str);
            mLastScanTextView.setText(builder.toString());
            mScanSugesTextView.setText(mRes.getString(R.string.last_scan_suggest));
        }
    }

    @Override
    public boolean isUsingCustomActionBar() {
        return true;
    }

    private void initView(Context context) {
        mRes = getResources();
        mActionbar = getCyeeActionBar();

        mLastScanTextView = (TextView) findViewById(R.id.last_scan_time);
        mScanSugesTextView = (TextView) findViewById(R.id.antivirus_sugest);
        mQuickScanButton = (Button) findViewById(R.id.quik_scan);
        mAllScanButton = (Button) findViewById(R.id.all_scan);
        // Gionee xionghonggang 2017-02-23 add for 81312 begin
        if (Locale.getDefault().getLanguage().toLowerCase().equals("ru")) {
            mQuickScanButton.setTextSize(14);
            mAllScanButton.setTextSize(14);
            mQuickScanButton.setGravity(Gravity.CENTER);
            mAllScanButton.setGravity(Gravity.CENTER);
        }
        // Gionee xionghonggang 2017-02-23 add for 81312 end        
        mMainView = findViewById(R.id.antivirus_main_layout);

        mAntiVirusLayout = (RelativeLayout) findViewById(R.id.antivirus_layout);
        mScanLayout = (RelativeLayout) findViewById(R.id.scan_layout);
        mResultLayout = (LinearLayout) findViewById(R.id.result_layout);
        mResultTopview = (RelativeLayout) findViewById(R.id.find_virus_logo);

        mCleanDoneLayout = (RelativeLayout) findViewById(R.id.clean_done_layout);
        initViSibility();

        mAntiVirusScaning = (ImageView) findViewById(R.id.antivirus_scaning);
        mCancelButton = (Button) findViewById(R.id.scan_cancel);
        mScanItemTextView = (TextView) findViewById(R.id.scan_virus_title);

        mCleandone = (TextView) findViewById(R.id.clean_virus_done);
        mCleanDoneButton = (Button) findViewById(R.id.clean_done);

        mVirusNum = (TextView) findViewById(R.id.find_virus_num);
        mVirusListView = (ListView) findViewById(R.id.listview);
        mClean = (CyeeButton) findViewById(R.id.clean_virus);

        mVirusSupport = (TextView) findViewById(R.id.antivirus_support);

        mCleanVirusSupport = (TextView) findViewById(R.id.clean_antivirus_support);
        mScanVirusSupport = (TextView) findViewById(R.id.scan_antivirus_support);
        mResultVirusSupport = (TextView) findViewById(R.id.result_antivirus_support);


        // Gionee <yangxinruo><2015-10-14> add for CR01568183 begin
        mScanPercentTextView = (TextView) findViewById(R.id.scaning_percent);
        // Gionee <yangxinruo><2015-10-14> add for CR01568183 end

        // Gionee <houjie> <2015-11-13> add for CR01565278 begin
        mVirusLogo = (ImageView) findViewById(R.id.antivirus_logo);
        mVirusCleanDoneLogo = (ImageView) findViewById(R.id.virus_clean_done_logo);
        mVirusScaningBg = (ImageView) findViewById(R.id.antivirus_scaning_bg);
        // Gionee <houjie> <2015-11-13> add for CR01565278 end

        setClickListener();
    }

    private void initViSibility() {
        mAntiVirusLayout.setVisibility(View.VISIBLE);
        mScanLayout.setVisibility(View.GONE);
        mResultLayout.setVisibility(View.GONE);
        mCleanDoneLayout.setVisibility(View.GONE);
    }

    private void initStatistic() {
        mScanResultList.clear();
        mScanPkgNameList.clear();
        mScanTotal = 0;
        mScanUnknown = 0;
    }

    private void setClickListener() {
        quickScanClickListener();
        allScanClickListener();
        cancelClickListener();
        cleanDoneClickListener();
        setCleanListener();
        mVirusSupport.setMovementMethod(LinkMovementMethod.getInstance());
        mCleanVirusSupport.setMovementMethod(LinkMovementMethod.getInstance());
        mScanVirusSupport.setMovementMethod(LinkMovementMethod.getInstance());
        mResultVirusSupport.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initAnim() {
        mScaningAnim = AnimationUtils.loadAnimation(this, R.anim.antivirus_scaning);
        LinearInterpolator lin = new LinearInterpolator();
        mScaningAnim.setInterpolator(lin);
    }

    private void allScanClickListener() {
        mAllScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mWhichView = SCANLAYOUT;
                mScanType = 2;
                mActionbar.setDisplayHomeAsUpEnabled(false);
                mActionbar.setDisplayShowCustomEnabled(false);
                mAntiVirusLayout.setVisibility(View.GONE);
                mScanLayout.setVisibility(View.VISIBLE);
                mResultLayout.setVisibility(View.GONE);
                mCleanDoneLayout.setVisibility(View.GONE);
                if (mScaningAnim != null) {
                    mAntiVirusScaning.startAnimation(mScaningAnim);
                }
                bindService();
            }
        });
    }

    private void quickScanClickListener() {
        mQuickScanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhichView = SCANLAYOUT;
                mScanType = 1;
                mActionbar.setDisplayHomeAsUpEnabled(false);
                mActionbar.setDisplayShowCustomEnabled(false);
                mAntiVirusLayout.setVisibility(View.GONE);
                mScanLayout.setVisibility(View.VISIBLE);
                mResultLayout.setVisibility(View.GONE);
                mCleanDoneLayout.setVisibility(View.GONE);
                if (mScaningAnim != null) {
                    mAntiVirusScaning.startAnimation(mScaningAnim);
                }
                //guoxt modify for CR01775652 begin
                //bindService();
                scanInstall();
                //guoxt modify for CR01775652 end

            }
        });
    }

    private void cancelClickListener() {
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDismiss = false;
                animPause();
                // scanPause();
                createDialog();
            }
        });
    }

    private void cleanDoneClickListener() {
        mCleanDoneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cleanDoneClickListener click");
                AntivirusActivity.this.finish();
            }
        });
    }

    private void setCleanListener() {
        mClean.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //guoxt modify for CR01775652 begin
                boolean flag = delAll();
                if (flag) {
                    mWhichView = CLEANDONELAYOUT;
                    mActionbar.setDisplayHomeAsUpEnabled(true);
                    mActionbar.setDisplayShowCustomEnabled(false);
                    mAntiVirusLayout.setVisibility(View.GONE);
                    mScanLayout.setVisibility(View.GONE);
                    mResultLayout.setVisibility(View.GONE);
                    mCleanDoneLayout.setVisibility(View.VISIBLE);
                    mCleandone.setText(mRes.getString(R.string.clean_virus_done_txt1));
                    long currentTime = System.currentTimeMillis();
                    mScanTimePreferences.edit().putLong(AntivirusActivity.SCAN_TIME, currentTime).commit();
                } else {
                    mClean.setVisibility(View.GONE);

                }
                //guoxt modify for CR01775652 end
            }
        });
    }

    private void bindService() {
        mLogFileDir = getApplicationContext().getFilesDir().getAbsolutePath();
        Intent cmd = getIntent();
        mAction = cmd.getIntExtra(INTENT_CMD_ACTION, 0);
        mAppend = cmd.getIntExtra(INTENT_CMD_FILE, 1);

        //guoxt modify for CR01775652 begin
        scanAll();
        //guoxt modify for CR01775652 end
    }

    //guoxt modify for CR01775652 begin
    //scall all apps and sdcard path
    public void scanAll() {
        if (mVirusScan == null) {
            Log.d(TAG, "mVirusScan is nulll");
            return;
        }

        if (mScanTask != null) {
            Log.d(TAG, "You have started a scan task, please wait...");
            return;
        }

        mScanTask = mVirusScan.scan(
                new ScanCombination() {
                    @Override
                    public Collection<ScanSource> getSources() {
                        Collection<ScanSource> c = new LinkedHashSet<ScanSource>();
                        //if (Build.VERSION.SDK_INT >= 23 && !isSystemApp(getApplicationContext()))
                        if (Build.VERSION.SDK_INT >= 23) {
                            Log.d("guoxt:", "VERSION.SDK_INT >= 23");
                            // "/sdcard" is a soft link to "/storage/emualted/0".
                            // But on android M, the file state of "/storage/emualted" has been changed
                            // to "drwx--x--x root     sdcard_rw".
                            // So the third party application can't find ""/storage/emualted/0" in directory
                            // "/storage/emualted" any more.
                            // File sdcard = Environment.getExternalStorageDirectory();
                            //c.add(new ScanPath(sdcard.getPath()));

                            StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);

                            String[] mPathList = mStorageManager.getVolumePaths();

                            int len = mPathList.length;

                            for (int i = 0; i < len; i++) {
                                if (!mStorageManager.getVolumeState(mPathList[i]).equals("not_present")) {
                                    //SDCardlist.add(mPathList[i]);
                                    c.add(new ScanPath(mPathList[i]));
                                    Log.e("guoxt:", mPathList[i]);
                                }
                            }

                        } else {
                            Log.d("guoxt:", "VERSION.SDK_INT < 23");
                        }
                        //c.add(new ScanPath("/")); //root directory
                        c.add(new ScanApplications(false)); //all apps ,downloaded
                        c.add(new ScanTextMessages());     //sms
                        c.add(new ScanMultimediaMessages());//multi meidia
                        return c;
                    }
                }, new ScanStrategy() {
                    @Override
                    public int getTechnology(ScanSource scanSource) {
                        return TECHNOLOGY_SIGNATURES;
                    }
                }
                , myScanObserver);
    }

    //scall all apps and sdcard path
    public void scanInstall() {
        if (mVirusScan == null) {
            Log.d(TAG, "mVirusScan is nulll");
            return;
        }

        if (mScanTask != null) {
            Log.d(TAG, "You have started a scan task, please...");
            return;
        }

        mScanTask = mVirusScan.scan(new ScanApplications(false),
                new ScanStrategy() {
                    @Override
                    public int getTechnology(ScanSource scanSource) {
                        return TECHNOLOGY_SIGNATURES;
                    }
                }
                , myScanObserver);
    }
    //guoxt modify for CR01775652 end

    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    showScanResult();
                    break;
                default:
                    break;
            }
        }
    };

    private void showScanResult() {
        //guoxt modify for CR01775652 begin
        //filterUnknownItem();
        //guoxt modify for CR01775652 end
        if (mScanResultList.size() <= 0) {
            mWhichView = CLEANDONELAYOUT;
            mActionbar.setDisplayHomeAsUpEnabled(true);
            mActionbar.setDisplayShowCustomEnabled(false);
            mAntiVirusLayout.setVisibility(View.GONE);
            mScanLayout.setVisibility(View.GONE);
            mResultLayout.setVisibility(View.GONE);
            mCleanDoneLayout.setVisibility(View.VISIBLE);
            mCleandone.setText(mRes.getString(R.string.clean_virus_done_txt));
            long currentTime = System.currentTimeMillis();
            mScanTimePreferences.edit().putLong(AntivirusActivity.SCAN_TIME, currentTime).commit();
        } else {
            mWhichView = RESULTLAYOUT;
            refreshResultLayout();
        }
    }

    //guoxt modify for CR01775652 begin
    private boolean allSMSVirus() {
        boolean allSMSVirus = true;
        for (int i = 0; i < mScanResultList.size(); i++) {
            ThreatInfo threatinfo = mScanResultList.get(i);
            if (threatinfo.type != TYPE_TEXT_MESSAGE) {
                allSMSVirus = false;
                return allSMSVirus;
            }
        }

        return allSMSVirus;
    }
    //guoxt modify for CR01775652 end

    private void refreshResultLayout() {
        mActionbar.setDisplayHomeAsUpEnabled(true);
        mActionbar.setDisplayShowCustomEnabled(false);
        mAntiVirusLayout.setVisibility(View.GONE);
        mScanLayout.setVisibility(View.GONE);
        mResultLayout.setVisibility(View.VISIBLE);
        mCleanDoneLayout.setVisibility(View.GONE);
        updateTextView();
        updateListView();
    }

    private void showScanResult2() {
        Intent intent = new Intent(getApplicationContext(), ScanningExceptionActivity.class);
        // intent.putStringArrayListExtra(INTENT_SCAN_RESULT, mScanResultList);
        intent.putExtra(INTENT_SCAN_TOTAL, mScanTotal);
        intent.putExtra(INTENT_SCAN_UNKNOWN, mScanUnknown);
        startActivity(intent);
        AntivirusActivity.this.finish();
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

    // Gionee <yangxinruo> <2015-10-14> modify for CR01568183 begin
    private void showCurrentScan(final String scanResult, final int percent) {
        mScanItemTextView.post(new Runnable() {
            @Override
            public void run() {
                // Gionee <xionghg> <2017-05-09> add for 129667 begin
                if (gnODMflag) {
                    return;
                }
                // Gionee <xionghg> <2017-05-09> add for 129667 end
                mScanItemTextView.setText(scanResult);
            }
        });
        mScanPercentTextView.post(new Runnable() {
            @Override
            public void run() {
                mScanPercentTextView.setText(percent + "%");
            }
        });
    }

    // Gionee <yangxinruo> <2015-10-14> modify for CR01568183 end


    private void initLogFile() {
        if ((WRITE_LOG_FILE) && (mAppend == 0)) {
            File log = new File(mLogFileDir, mLogFileName);
            if (log.exists()) {
                if (!log.delete()) {
                    Log.e(TAG, "Log file:" + log.getAbsolutePath() + " delete failed.");
                }
            }
        }
    }

    private String getRiskClassText(int riskClass) {
        String text = null;
        switch (riskClass) {
            case Threat.RISK_HIGH:
                text = mRes.getString(R.string.high_risk);
                break;
            case Threat.RISK_MEDIUM:
                text = mRes.getString(R.string.danger);
                break;
            case Threat.RISK_LOW:
                text = mRes.getString(R.string.lowdanger);
                break;
        }
        return text;
    }

    private void scanStop() {
        Log.i(TAG, "Scan stopped.");
        if (mScanTask == null) {
            return;
        }
        int r = -1;
        mScanTask.cancel();
    }

    private void animPause() {
        mAntiVirusScaning.clearAnimation();
    }

    private void animResume() {
        mAntiVirusScaning.startAnimation(mScaningAnim);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWhichView == SCANLAYOUT) {
                mDismiss = false;
                animPause();
                //scanPause();
                createDialog();
                return true;
            }
            /*   else {
                   Intent intent = new Intent("gionee.intent.action.KILL_PROCESS");
                   intent.putExtra("processid", android.os.Process.myPid());
                   this.sendBroadcast(intent);
               }*/

        }
        return super.onKeyDown(keyCode, event);
    }

    private void createDialog() {
        // Gionee <xuwen><2015-07-29> modify for CR01527472 begin
        if (null != mPermDialog && mPermDialog.isShowing()) {
            return;
        }

        mPermDialog = new CyeeAlertDialog.Builder(mContext, CyeeAlertDialog.THEME_CYEE_FULLSCREEN)
                .create();
        // mPermDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        // WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mPermDialog.setTitle(mRes.getString(R.string.cancel_scan_virus));
        String message = mRes.getString(R.string.cancel_scan_virusmessage);
        mPermDialog.setMessage(message);

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        scanStop();
                        mDismiss = true;
                        AntivirusActivity.this.finish();
                        break;
                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        animResume();
                        // scanResume();
                        if (!mDismiss) {
                            mDismiss = true;
                            if (mScanType == 1) {
                                //scanApp();
                            } else {
                                scanAll();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        mPermDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, mRes.getString(R.string.ok), dialogClickLsn);
        mPermDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, mRes.getString(R.string.cancel),
                dialogClickLsn);
        mPermDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                animResume();
                // scanResume();
                if (!mDismiss) {
                    mDismiss = true;
                    if (mScanType == 1) {
                        // scanApp();
                    } else {
                        scanAll();
                    }
                }
            }
        });

        mPermDialog.show();
        // Gionee <xuwen><2015-07-29> modify for CR01527472 end
    }

    private void updateTextView() {
        StringBuilder builder = new StringBuilder();
        builder.append(mRes.getString(R.string.find_dangerous_item));
        builder.append(mScanResultList.size());
        builder.append(mRes.getString(R.string.ge));
        mVirusNum.setText(builder.toString());
    }

    private void updateListView() {
        mVirusListView.setAdapter(new ListViewAdapter());
    }

    private String getPackageName(String s) {
        int start = s.indexOf("(");
        int end = s.indexOf(")");
        if (start != -1 && end > start) {
            return s.substring(start + 1, end);
        }
        return null;
    }

    public String getRiskString(int position) {
        ThreatInfo threatinfo = mScanResultList.get(position);
        Threat threat = threatinfo.thread;
        return getRiskClassText(threat.getRiskLevel()) + "(" + threatinfo.source.toString() + ")";
        //String scanResult = mScanResultList.get(position).getName();
        //String packageInfo = scanResult.substring(0, scanResult.indexOf(" "));
        //String packageName = getPackageName(packageInfo);
        /*
        if (scanResult == null) {
            int start = scanResult.indexOf(" ");
            int end = scanResult.indexOf("[");
            if (start > -1 && end > -1 && start < end) {
                return scanResult.substring(start + 1, end);
            }
        } else {
            int start = scanResult.indexOf(")");
            int end = scanResult.indexOf("[");
            if (start > -1 && end > -1 && start < end) {
                return scanResult.substring(start + 1, end);
            }
        }
        */
        //return threat.getRiskLevel() + " ";

        //return " ";
    }

    private void delApkFile(String path) {
        // Gionee <xuwen><2015-07-28> modify for CR01527111 begin
        Intent intent = new Intent(UnitUtil.APK_DELETE_ACTION);
        intent.putExtra(UnitUtil.APK_DELETE_PACKAGE_PATH_KEY, path);
        sendBroadcast(intent);
        // Gionee <xuwen><2015-07-28> modify for CR01527111 end
    }

    private void uninstallApp(String name) {
        Intent intent = new Intent();
        intent.putExtra("DELETE_PACKAGE_NAME", name);
        intent.setAction("com.chenyee.action.DELETE_PACKAGE");
        sendBroadcast(intent);
    }

    private void delOneImp(String scanResult) {
        // String packageInfo = scanResult.substring(0, scanResult.indexOf(" "));
        String packageName = scanResult;//getPackageName(packageInfo);
        Log.d(TAG, "delete patch:" + packageName);
        if (packageName == null || AntivirusUtil.getApplicationInfo(mContext, packageName) == null) {
            String packagePath = packageName;

            delApkFile(packagePath);
        } else {
            uninstallApp(packageName);
        }
    }

    private void delOne(int pos) {
        String scanResult = mScanResultList.get(pos).source.toString();
        Log.d(TAG, scanResult);
        delOneImp(scanResult);
    }

    private boolean delAll() {
        boolean delAllFlag = true;
        String msgidStr = "";
        String mmsidStr = "";
        for (int i = 0; i < mScanResultList.size(); i++) {
            int type = mScanResultList.get(i).type;
            ThreatInfo threatInfo = mScanResultList.get(i);

            if (type == TYPE_APP || type == TYPE_FILE) {
                delOne(i);
            } else if (type == TYPE_TEXT_MESSAGE) {
                ScanTextMessage mScanTextMessage = (ScanTextMessage) threatInfo.source;
                int msgid = mScanTextMessage.getMsgId();
                msgidStr += msgid + "|";
            } else if (type == TYPE_MULT_MESSAGE) {
                ScanMultimediaMessage mScanMultimediaMessage = (ScanMultimediaMessage) threatInfo.source;
                int mmsid = mScanMultimediaMessage.getMsgId();
                mmsidStr += mmsid + "|";
            }
        }
        Log.d(TAG, "text_msg:" + msgidStr + ",text_mms:" + mmsidStr);
        if (mmsidStr != "" || msgidStr != "") {
            Intent deleteMsg = new Intent(MESSAGE_INTENT);
            if (msgidStr != "") {
                deleteMsg.putExtra("text_msg", msgidStr);
            }
            if (mmsidStr != "") {
                deleteMsg.putExtra("text_mms", mmsidStr);
            }
            mContext.sendBroadcast(deleteMsg);
        }
        mScanResultList.clear();

        updateListView();
        return delAllFlag;
    }


/*
    private boolean delAll() {
		boolean delAllFlag = true;
        for (int i = 0; i < mScanResultList.size(); i++) {
			int type = mScanResultList.get(i).type;
			Log.d(TAG,mScanResultList.get(i).source.toString() + ",type" + type);
			
			if(type != TYPE_MESSAGE){
				delOne(i);
			}else{
			   delAllFlag = false;
			}
            
        }
        if(!delAllFlag){
	        int count = mScanResultList.size();
	        for (int i = 0; i < count; ++i) {
	            int type = mScanResultList.get(i).type;
	            if (type != TYPE_MESSAGE) {
	                mScanResultList.remove(i);
	                --i;
	                --count;
	            }
	        }
		 
        }else{
        	mScanResultList.clear();
        }
        updateListView();
		return delAllFlag;
    }
    */

    private class ListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mScanResultList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.virus_display_layout, parent,
                        false);
                viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
                viewHolder.mSummary = (TextView) convertView.findViewById(R.id.summary);
                viewHolder.smsFlag = (ImageView) convertView.findViewById(R.id.smsFlag);
                // viewHolder.mCheckBox = (CheckBox)
                // convertView.findViewById(R.id.checkbox);
                convertView.setTag(viewHolder);
                // Gionee xionghg 2017-04-19 modify for 114283 begin
                // convertView.setId(position);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            convertView.setId(position);
            // Gionee xionghg 2017-04-19 modify for 114283 end

            viewHolder.mTitle.setText(getResultTitle(position));
            viewHolder.mSummary.setText(getRiskString(position));

            viewHolder.mSummary.setVisibility(View.VISIBLE);
            viewHolder.smsFlag.setVisibility(View.GONE);

            if (iSsmsVirus(position)) {
                Log.d(TAG, getRiskString(position) + "type:" + mScanResultList.get(position).type + "   :" + position);
                viewHolder.smsFlag.setVisibility(View.VISIBLE);
                viewHolder.smsFlag.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_anti_info));
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "MODE TEST onClick change button at " + (Integer) v.getId());
                        createDialog1((Integer) v.getId());
                    }
                });
                // Gionee xionghg 2017-04-19 modify for 114283 begin
            } else {
                convertView.setOnClickListener(null);
            }
            // Gionee xionghg 2017-04-19 modify for 114283 end
            String pkgName = mScanResultList.get(position).thread.getName();
            Log.d(TAG, "pkgName: " + pkgName + "position:" + position);

            if (pkgName == null) {
                viewHolder.mIcon.setImageDrawable(loadIcon(mContext, null));
            } else {
                viewHolder.mIcon.setImageDrawable(loadIcon(mContext, getApplicationInfo(mContext, pkgName)));
            }
            // Gionee <houjie> <2015-09-24> modify for CR01559303 end

            return convertView;
        }
    }

    public CyeeAlertDialog createDialog1(int position) {
        ThreatInfo threatinfo = mScanResultList.get(position);
        String body = "";
        threatinfoToast = threatinfo;
        Log.d(TAG, "createDialog1:type= " + threatinfo.type);
        if (threatinfo.type != TYPE_TEXT_MESSAGE && threatinfo.type != TYPE_MULT_MESSAGE) {
            return null;
        }
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.antivirus_message));
        builder.setMessage("");
        builder.setPositiveButton(mContext.getResources().getString(R.string.delete_manual), listener);

        String packageName = threatinfo.thread.getName();
        String msgid = getMsgId(threatinfo.source.toString());
        if (threatinfo.source instanceof ScanTextMessage) {
            body = getTextMsgBody(msgid);
        } else if (threatinfo.source instanceof ScanMultimediaMessage) {
            body = getMutiMsgContent(msgid);
        }
        if (body == "") {
            body = mContext.getResources().getString(R.string.MMS_Unknown);
        }

        builder.setMessage(body);
        return builder.show();
    }

    private String getTextMsgBody(String msgid) {
        Cursor isRead = mContext.getContentResolver().query(Uri.parse("content://sms/"), null, "_id=" + msgid, null, null);
        if (isRead == null || isRead.getCount() <= 0) {
            return "";
        }

        isRead.moveToFirst();
        String body = isRead.getString(isRead.getColumnIndex("body")).trim();
        // String from =  isRead.getString(isRead.getColumnIndex("address")).trim();
        isRead.close();
        return body;
    }

    private String getMutiMsgContent(String msgid) {
        String body = "";
        String selectionPart = "mid=" + msgid;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = mContext.getContentResolver().query(uri, null,
                selectionPart, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return "";
        }
        if (cursor.moveToFirst()) {
            do {
                String partId = cursor.getString(cursor.getColumnIndex("_id"));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                if ("text/plain".equals(type)) {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));
                    if (data != null) {
                        // implementation of this method below
                        body = getMmsText(partId);
                    } else {
                        body = cursor.getString(cursor.getColumnIndex("text"));
                    }
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return body;
    }


    private String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = mContext.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case CyeeAlertDialog.BUTTON_POSITIVE:
                    // Chenyee xionghg 2017-08-31 modify for 200803 begin
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setAction("android.intent.action.MAIN");
                        intent.setType("vnd.android-dir/mms-sms");
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.w(TAG, "no native mms app, try to start default mms app");
                        String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(mContext);
                        if (defaultSmsPackageName != null) {
                            Intent intent2 = mContext.getApplicationContext().getPackageManager()
                                    .getLaunchIntentForPackage(defaultSmsPackageName);
                            startActivity(intent2);
                        } else {
                            Log.e(TAG, "start default mms app failed, not found");
                        }
                    }
                    sleep(2000);
                    // Chenyee xionghg 2017-08-31 modify for 200803 end
                    break;
                case CyeeAlertDialog.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
            }
        }
    };

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception ex) {
        }
    }

    private DialogInterface.OnClickListener listenerConfirm = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case CyeeAlertDialog.BUTTON_POSITIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };

    private String getFileName(String s) {
        int start = s.lastIndexOf("/");
        if (start != -1) {
            int end = s.indexOf(" ");
            // Gionee <xuwen> <2015-08-25> modify for CR01547240 begin
            if (end != -1 && end > start) {
                return s.substring(start + 1, end);
            } else {
                return s.substring(start + 1, s.length());
            }
            // Gionee <xuwen> <2015-08-25> modify for CR01547240 end
        }
        return null;
    }

    private String getMsgId(String s) {
        int start = s.indexOf("(");
        int end = s.indexOf(")");
        String stringId = s.substring(start + 1, end);
        // return Integer.valueOf(stringId);
        return stringId;
    }

    private class ViewHolder {
        public ImageView mIcon;
        public TextView mTitle;
        public TextView mSummary;
        public CheckBox mCheckBox;
        public ImageView smsFlag;
    }

    private boolean iSsmsVirus(int position) {
        if (mScanResultList.get(position).type == TYPE_TEXT_MESSAGE
                || mScanResultList.get(position).type == TYPE_MULT_MESSAGE) {
            return true;
        } else {
            return false;
        }
    }

    private String getResultTitle(int position) {
        String title;
        //String scanResult = mScanResultList.get(position);
        Threat threat = mScanResultList.get(position).thread;
        //String packageInfo = scanResult.substring(0, scanResult.indexOf(" "));
        String packageName = threat.getName();
        Log.d(TAG, "scanResult:" + packageName);

        title = packageName;
        /*
        if (threat == null) {
            title = getFileName(mScanResultList.get(position));
        } else {
            ApplicationInfo info = getApplicationInfo(mContext, scanResult);
            if (info == null) {
                title = getFileName(mScanResultList.get(position));
            } else {
                title = loadLabel(mContext, info);
            }

        }
        */

        return title;
    }

    /**
     * To get application info by package name
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String loadLabel(Context context, ApplicationInfo info) {
        String result = info.loadLabel(context.getPackageManager()).toString();
        if (result == null) {
            result = info.packageName;
        }
        return result;
    }

    public static Drawable loadIcon(Context context, ApplicationInfo info) {
        Drawable result = null;
        if (info == null) {
            result = context.getResources().getDrawable(R.drawable.mime_unknown);

        } else {
            result = info.loadIcon(context.getPackageManager());
            if (result == null) {
                result = context.getResources().getDrawable(R.drawable.mime_unknown);
            }
        }

        return result;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "enter onDestroy");
        super.onDestroy();
    }

    // Gionee: <houjie> <2015-12-26> add for CR01614703 begin
    private boolean redirectToPermissionCheckIfNeeded() {
        if (RuntimePermissionsManager.isBuildSysNeedRequiredPermissions()
                && RuntimePermissionsManager.hasNeedRequiredPermissions(this)) {
            RuntimePermissionsManager.redirectToPermissionCheck(this);
            finish();
            return true;
        }
        return false;
    }
    // Gionee: <houjie> <2015-12-26> add for CR01614703 end
}
