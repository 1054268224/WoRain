package com.cydroid.systemmanager.antivirus;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.internal.telephony.SmsApplication;
import com.cydroid.softmanager.R;
import com.intel.security.SecurityContext;
import com.intel.security.vsm.RealTimeScan;
import com.intel.security.vsm.ScanObserver;
import com.intel.security.vsm.ScanResult;
import com.intel.security.vsm.ScanTask;
import com.intel.security.vsm.Threat;
import com.intel.security.vsm.VirusScan;
import com.intel.security.vsm.content.ScanApplication;
import com.intel.security.vsm.content.ScanMultimediaMessage;
import com.intel.security.vsm.content.ScanPath;
import com.intel.security.vsm.content.ScanSource;
import com.intel.security.vsm.content.ScanTextMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cyee.app.CyeeAlertDialog;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;
import com.cydroid.systemmanager.utils.UnitUtil;

/**
 * Foreground service to keep application alive.
 */
public class ForegroundService extends Service
{
//    ScanApplication, ScanApplications, ScanInboxMultimediaMessages, ScanInboxTextMessages, ScanMultimediaMessage, ScanMultimediaMessages, ScanPath, ScanTextMessage, ScanTextMessages
	private static final String  TAG = "antivirus-Service:";
	private  static final int DIALOG_SHOW = 201;
	 private  ArrayList<ThreatInfo> mScanResultList = new ArrayList<ThreatInfo>();
	 private  ArrayList<ScanPath> mScanResultFileList = new ArrayList<ScanPath>();
	 private  ArrayList<ScanApplication> mScanApplication = new ArrayList<ScanApplication>();
	 private  ArrayList<ScanTextMessage> mScanResultTextMsgList = new ArrayList<ScanTextMessage>();
	 private  ArrayList<ScanMultimediaMessage> mScanResultMutiMsgList = new ArrayList<ScanMultimediaMessage>();
	private  DiaLogHandler mDialogHandler;
	private   Context  mContext;
	 private  Resources mRes;
	 private SharedPreferences mRealTimePreferences;

	  //guoxt modify for CR01775652 begin	
	  private static final int  TYPE_TEXT_MESSAGE = 0;
	 private static final int  TYPE_MULT_MESSAGE = 1;
      private static final int TYPE_APP = 2;
	  private static final int TYPE_FILE = 3;
	  private static final int MSG_CONTENT_LENGTH = 10;
	  private static final int FILE_NAME_LENGTH = 10;
	  private static final String MESSAGE_INTENT = "com.action.antivirus.del.text.msg";
	  private  String  path = "";
	  CyeeAlertDialog permDialog;
	  private SharedPreferences mMonitorAppInstallPreferences;
	  private  VirusScan mVirusScan =  null;
	    private ScanTask mScanTask;
		boolean	mDissmissFlag = true;
		 
	  


    @Override
    public void onCreate() {
        mContext = this;
        mRes = mContext.getResources();
		mRealTimePreferences = getApplicationContext().getSharedPreferences("auto_update_key",
                			Context.MODE_PRIVATE);
        mMonitorAppInstallPreferences = mContext.getSharedPreferences(AntiVirusPrefsFragment.AUTO_UPDATE_KEY,
                			Context.MODE_PRIVATE);
        startDialogThread();

    }

    private void initialize()
    {
        InputStream license = null;
        try
        {
            license = getResources().openRawResource(R.raw.license_isec_gionee);
        }
        catch (Exception e)
        {
          Log.d(TAG, "license Exception" +e );
        }

        try
        {
            SecurityContext.initialize(getApplicationContext(), license,
                    new SecurityContext.InitializationCallback()
                    {
                        @Override
                        public void onInitialized()
                        {
                            Log.d("guoxt:", "Intel mobile security SDK initialized");
                        }
                    });	


            boolean realTimeflag = mRealTimePreferences.getBoolean("real_time_monitor_key", false);

			//get service
            if(realTimeflag){
                VirusScan mVirusScan = (VirusScan) SecurityContext
                    .getService(getApplicationContext(), SecurityContext.VIRUS_SCAN);
                RealTimeScan realTimeScan = mVirusScan.getRealTimeScan();

                String[] types = new String[] {RealTimeScan.REAL_TIME_SCAN_MESSAGE,
                    RealTimeScan.REAL_TIME_SCAN_PACKAGE, RealTimeScan.REAL_TIME_SCAN_FILE};
                    realTimeScan.enable(RealTimeScan.REAL_TIME_SCAN_MESSAGE);
                    realTimeScan.enable(RealTimeScan.REAL_TIME_SCAN_PACKAGE);
                    realTimeScan.enable(RealTimeScan.REAL_TIME_SCAN_FILE);
                for (String scanType : types){
                    if (realTimeScan.isEnabled(scanType)){
                    Log.d(TAG, scanType + " is enabled sucess!");
                    realTimeScan.setScanObserver(scanType, sRealTimeScanObserver);
                }
            }

		}
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception  Exception!", e);
        }
        finally
        {
            try
            {
                license.close();
            }
            catch (IOException e)
            {
            Log.d(TAG, "Exception  Exception!", e);
            }
        }
    }

//guoxt modify for CR01775652 end	

	

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
                    createDialog(mScanResultList);
                    break;

            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
		ServiceUtil.handleStartForegroundServices(ForegroundService.this);
			new Thread(new Runnable()        
			 {            
			   @Override            
			   public void run()             
			   {    initialize();  
			    }        
			   }).start(); 
        return 0;
    }





	
public  ScanObserver sRealTimeScanObserver = new ScanObserver()
    {
        @Override
        public void onStarted()
        {
            Log.d(TAG, "onStarted");

        }

        @Override
        public void onCompleted(int result)
        {
            Log.d(TAG, "onCompleted");

				if (mScanResultList.size() > 0 ) {
                Message msg = mDialogHandler.obtainMessage(DIALOG_SHOW);
                mDialogHandler.sendMessage(msg);
            } 
            
        }

        @Override
        public void onScanned(ScanSource source, ScanResult result)
        {
            if (result.getThreat() != null)
            {
                String msg = String.format("%s, Threat found %s .", source.toString(),
                        result.getThreat().getName());
                Log.d(TAG, msg);
				Threat threat = result.getThreat();
				ThreatInfo threatInfo = new ThreatInfo();
				threatInfo.source= source;
				if(source instanceof ScanMultimediaMessage){
					 Log.d(TAG,"source instanceof ScanMultimediaMessage");
					ScanMultimediaMessage mScanMultimediaMessage = (ScanMultimediaMessage) source;
					threatInfo.type = TYPE_MULT_MESSAGE;
					mScanResultMutiMsgList.add(mScanMultimediaMessage);
					
				}else if(source instanceof ScanTextMessage){
				  Log.d(TAG,"source instanceof ScanTextMessage");
					ScanTextMessage mScanTextMessage = (ScanTextMessage) source;
					threatInfo.type = TYPE_TEXT_MESSAGE;
					mScanResultTextMsgList.add(mScanTextMessage);

				 } else if (source instanceof ScanPath){
				      Log.d(TAG,"source instanceof ScanPath");
				    ScanPath mScanFile = (ScanPath) source;
				 	threatInfo.type = TYPE_FILE;
					mScanResultFileList.add(mScanFile);
				}else if (source instanceof ScanApplication){
				      Log.d(TAG,"source instanceof ScanApplication");
				    ScanApplication mAppFile = (ScanApplication) source;
				 	threatInfo.type = TYPE_APP;
					mScanApplication.add(mAppFile);
				}

				 threatInfo.thread   = result.getThreat();

				mScanResultList.add(threatInfo);
			
            }
            else
            {
                String msg = String.format("%s is clean.", source.toString());
                Log.d(TAG, msg);
            }


	

            Object cloudErrCode = result.getMeta(ScanResult.CLOUD_SCAN_ERROR);
            if (cloudErrCode != null && (cloudErrCode instanceof Integer))
            {
                Log.d(TAG, "Cloud scan error code is " + ((Integer)cloudErrCode).intValue());
            }
        }
    };



    private  void createDialog(ArrayList<ThreatInfo> scanResult) {
            String appNameString = "";

			int mMsgTextLength = 0;
			int mFileLength = 0;
			int mMsgMutiLength = 0;
			int mAppLength = 0;
			int mMsgCount =0;
			int MFileCount =0;
			mMsgTextLength  =   mScanResultTextMsgList.size();
			mMsgMutiLength = mScanResultMutiMsgList.size();
			mFileLength = mScanResultFileList.size(); 
			mAppLength = mScanApplication.size(); 
			mMsgCount =  mMsgTextLength  + mMsgMutiLength;
			MFileCount = mFileLength     + mAppLength;

			Log.d("guoxt:"," muti-msgLength" +mMsgCount + " fileLength: "+  MFileCount + " muiMsgLength" + mMsgCount
				 + "        muti-mScanResultList.size(): " +mScanResultList.size());


            if (mScanResultList.size() == 1 ){
                ThreatInfo threatInfo = mScanResultList.get(0);
                if(threatInfo.type == TYPE_FILE){
                     appNameString = threatInfo.source.toString();
					 Log.d(TAG,"appNameString:" +appNameString);
					 appNameString= mContext.getResources().getString(R.string.antivirus_file)
					 				+ getFileName(appNameString);
					 
				}else if(threatInfo.type == TYPE_APP){
					appNameString = threatInfo.source.toString();
				    if(AntivirusUtil.getApplicationInfo(mContext, appNameString) == null){
					    appNameString= mContext.getResources().getString(R.string.antivirus_file)
								   + getFileName(appNameString);

					}else{
					    appNameString= mContext.getResources().getString(R.string.antivirus_app)
								   + appNameString;
				    	}

				}else if(threatInfo.type == TYPE_TEXT_MESSAGE){

				    ScanTextMessage mTextMessage =  (ScanTextMessage)(mScanResultTextMsgList.get(0));
				    int msgid = mTextMessage.getMsgId();
				    appNameString=mContext.getResources().getString(R.string.antivirus_message)
				   				+ getMsgContent(msgid);

				}else if(threatInfo.type == TYPE_MULT_MESSAGE){
				   ScanMultimediaMessage  mMutitMessage =  (ScanMultimediaMessage)(mScanResultMutiMsgList.get(0));

				    int msgid = mMutitMessage.getMsgId();
					String content = getMutiMsgContent(msgid);
					if(content.equals("")){
					   content = mContext.getResources().getString(R.string.MMS_Unknown);
					}

				   appNameString=mContext.getResources().getString(R.string.antivirus_message)
				   				+ content;

				}
			}else{

				if(mMsgCount == 0 && MFileCount >1){
					 if(mScanResultFileList.size()!=0){
					 	appNameString = mScanResultFileList.get(0).getPath();
					 }else{
					 	appNameString = mScanApplication.get(0).toString();
					 }
					 appNameString= getFileName(appNameString)
					 				+  mContext.getResources().getString(R.string.antivirus_files,MFileCount);

				}else if(mMsgCount >1 && MFileCount ==0){
				       ScanTextMessage  mTextMessage =  null;
					   ScanMultimediaMessage  mMutitMessage =  null;
					   if(mMsgTextLength >1){
						     mTextMessage = (ScanTextMessage)(mScanResultTextMsgList.get(0));
						     int msgid = mTextMessage.getMsgId();
						      Log.d(TAG,"msgid" +msgid);
						      appNameString= getMsgContent(msgid)
						   					+ mContext.getResources().getString(R.string.antivirus_messages,mMsgCount);
					}else{

							 mMutitMessage = (ScanMultimediaMessage)(mScanResultMutiMsgList.get(0));
						     int msgid = mMutitMessage.getMsgId();
							 String content = getMutiMsgContent(msgid);
						      Log.d(TAG,"msgid" +msgid);
							  if(content == ""){
							  	  content = mContext.getResources().getString(R.string.MMS_Unknown);
							  	}
						      appNameString= content
						   					+ mContext.getResources().getString(R.string.antivirus_messages,mMsgCount);
					}


				}else{
					 if(mScanResultFileList.size()!=0){
					 	appNameString = mScanResultFileList.get(0).getPath();
					 }else{
					 	appNameString = mScanApplication.get(0).toString();
					 }
					 	appNameString= getFileName(appNameString)
										+  mContext.getResources().getString(R.string.antivirus_files,mScanResultList.size() );

				    }

			}


		if(permDialog != null){
        	if(permDialog.isShowing()){
				mDissmissFlag = false;
				permDialog.dismiss();
			}
		}


         permDialog = new CyeeAlertDialog.Builder(mContext,
                CyeeAlertDialog.THEME_CYEE_FULLSCREEN).create();
        permDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        permDialog.setTitle(mRes.getString(R.string.monitor_install_title));


        String virusNameString = "";//getRiskClassText(scanResult.riskClass);
        String message = appNameString + " " + mRes.getString(R.string.monitor_install_message) + virusNameString;
        message += "\n" + mRes.getString(R.string.monitor_install_message3);
        permDialog.setMessage(message);
        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
						mDissmissFlag = true;
                        delFileOrMsg();
                        break;

                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }

        };

        permDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, mRes.getString(R.string.monitor_delete),
                dialogClickLsn);
        permDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, mRes.getString(R.string.monitor_cancel),
                dialogClickLsn);
        permDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                //MonitorAppInstallService.this.stopSelf();
                if(mDissmissFlag){
		            mScanResultList.clear();
					mScanApplication.clear();
					mScanResultFileList.clear();
					mScanResultTextMsgList.clear();
					mScanResultMutiMsgList.clear();

                }

				mDissmissFlag = true;
            }
        });

        permDialog.show();
    }


	private  String getMsgContent(int msgid) {
		Cursor isRead =  mContext.getContentResolver().query(Uri.parse("content://sms/"), null, "_id=" + msgid,  null, null);
		String msgContent = "";
		isRead.moveToFirst();
		msgContent=	isRead.getString(isRead.getColumnIndex("body")).trim();
		String from =	isRead.getString(isRead.getColumnIndex("address")).trim();
		isRead.close();
		if(msgContent.length() > MSG_CONTENT_LENGTH){
			msgContent = msgContent.substring(0, MSG_CONTENT_LENGTH);

			}
		return  msgContent;

		}

	private  String getMutiMsgContent(int msgid) {
		String body = "";
		String selectionPart = "mid=" + msgid;
		Uri uri = Uri.parse("content://mms/part");
		Cursor cursor = mContext.getContentResolver().query(uri, null,
			selectionPart, null, null);
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
			} catch (IOException e) {}
			finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {}
				}
			}
			return sb.toString();
		}

		private  String getFileName(String fileName) {
			//guoxt modify for 47561 begin
			if( AntivirusUtil.getApplicationInfo(mContext, fileName) != null){
				return fileName;
			}
			//guoxt modify for 47561 end
		     Log.d(TAG,fileName);
			 String[] contentsList = fileName.split("/");
			 String appNameString = contentsList[contentsList.length-1]; 
			 Log.d(TAG,appNameString);
			 if(appNameString.length() > FILE_NAME_LENGTH){ 
			     if(appNameString.contains(".")){
				 	//guoxt mdofiy for 55725 begin 
			         String[] nameList = appNameString.split("\\.");
					 if(nameList == null || nameList.length <0){
					 	 return appNameString;

					 	}
					//guoxt mdofiy for 55725 end 
					
					 if(nameList[0].length() > FILE_NAME_LENGTH){
						appNameString = nameList[0].substring(0, FILE_NAME_LENGTH) + "."  + nameList[nameList.length-1];
					 }else{
					 	appNameString = appNameString +"."  + nameList[nameList.length-1];
					}
			     }else{
			 		appNameString.substring(0, FILE_NAME_LENGTH) ;

			    }
			
			}else{
					return appNameString;

				}

		return  appNameString;



		}



		private String getDefaultPackage() {
			ComponentName appName  = null;
			try
            {
                appName = SmsApplication.getDefaultSmsApplication(mContext, true);
            }
            catch (Exception e)
            {
            	Log.d(TAG, "Exception  Exception!", e);
            }
			
			if (appName != null) {
				return appName.getPackageName();
			}
			return null;
		}


    private  boolean checkGioneSmsApp() {
		boolean flag = true;
		if(!getDefaultPackage().equals("com.android.mms")){
			 Toast.makeText(this, mContext.getResources().getString(R.string.not_gionee_SMS)  , Toast.LENGTH_LONG).show();
			 flag = false;
		}
	
		return flag;
    	}

	 private  void  jumptoSmsActivity() {
		  Intent intent = new Intent(Intent.ACTION_VIEW);
		 intent.setAction("android.intent.action.MAIN");
		  intent.setType("vnd.android-dir/mms-sms");
		  startActivity(intent);
    	}

 private void uninstallApp(String name) {
        Intent intent = new Intent();
        intent.putExtra("DELETE_PACKAGE_NAME", name);
        intent.setAction("com.chenyee.action.DELETE_PACKAGE");
        sendBroadcast(intent);
    }

private void delApkFile(String path) {
	// Gionee <xuwen><2015-07-28> modify for CR01527111 begin
	Intent intent = new Intent(UnitUtil.APK_DELETE_ACTION);
	intent.putExtra(UnitUtil.APK_DELETE_PACKAGE_PATH_KEY, path);
	sendBroadcast(intent);
	// Gionee <xuwen><2015-07-28> modify for CR01527111 end
}

private void delOneImp(String scanResult) {
		
	String packageName = scanResult; //getPackageName(packageInfo);
	Log.d(TAG,"delete patch:" + packageName);
	if (packageName == null || AntivirusUtil.getApplicationInfo(mContext, packageName) == null) {
		String packagePath = packageName;
		delApkFile(packagePath);
	}
	else{
		uninstallApp(packageName);
	}
}


private  void delFileOrMsg() {

	String msgidStr = "";
	String mmsidStr = "";
	for (int i = 0; i < mScanResultList.size(); i++) {
		int type = mScanResultList.get(i).type;
		ThreatInfo threatInfo = mScanResultList.get(i);

		if(type == TYPE_APP || type == TYPE_FILE){
			delOneImp(threatInfo.source.toString());
		}else if(type ==  TYPE_TEXT_MESSAGE){
			ScanTextMessage mScanTextMessage = (ScanTextMessage) threatInfo.source;
			int msgid = mScanTextMessage.getMsgId();
			msgidStr += msgid + "|";		
		    
		}else if(type ==  TYPE_MULT_MESSAGE){
			ScanMultimediaMessage mScanMultimediaMessage = (ScanMultimediaMessage) threatInfo.source;
			int mmsid = mScanMultimediaMessage.getMsgId();
			mmsidStr += mmsid + "|";	  
		}		
	}
	Log.d(TAG,"text_msg:" +msgidStr + ",text_mms:" + mmsidStr);
	if(mmsidStr != "" || msgidStr != ""){
	    Intent deleteMsg = new Intent(MESSAGE_INTENT);
	    if(msgidStr != ""){
	        deleteMsg.putExtra("text_msg", msgidStr);
	    }
	    if(mmsidStr != ""){
	        deleteMsg.putExtra("text_mms", mmsidStr);
	    }
	    mContext.sendBroadcast(deleteMsg);
		jumptoSmsActivity();
	}

}


    private  void delFile(String path) {
		if(path == ""){
			return;
		}
        // Gionee <xuwen><2015-07-28> modify for CR01527111 begin
        Intent intent = new Intent(UnitUtil.APK_DELETE_ACTION);
        intent.putExtra(UnitUtil.APK_DELETE_PACKAGE_PATH_KEY, path);
        mContext.sendBroadcast(intent);
        // Gionee <xuwen><2015-07-28> modify for CR01527111 end
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
