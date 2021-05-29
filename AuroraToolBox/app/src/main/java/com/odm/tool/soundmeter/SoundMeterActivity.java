package com.odm.tool.soundmeter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.media.AudioManager;

import com.odm.tool.R;

import java.io.File;
import java.io.IOException;

public class SoundMeterActivity extends Activity {
    private float volume = 10000;
    private SoundDiscView soundDiscView;
    private MyMediaRecorder mRecorder;
    private static final int msgWhat = 0x1001;
    private static final int refreshTime = 100;


    private ImageButton backButton;
    private TextView quiteTextView;
    private TextView indoorTextView;
    private TextView noisyTextView;
    private TextView harmfulTextView;
    private TextView extremelyTextView;
    // add bug:TEWBB-934 chenyu 202009107 start
    private Context mContext;
    private AudioManager mAudioManger;
    // add bug:TEWBB-934 chenyu 202009107 end

    private static String mSoundMeterPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_meter);
        mRecorder = new MyMediaRecorder();
        mSoundMeterPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "SoundMeter" + File.separator;
        File recFile = new File(mSoundMeterPath);
        if (!recFile.exists()) {
            recFile.mkdirs();
        }
        quiteTextView = findViewById(R.id.quiteTextView);
        indoorTextView = findViewById(R.id.indoorTextView);
        noisyTextView = findViewById(R.id.noisyTextView);
        harmfulTextView = findViewById(R.id.harmfulTextView);
        extremelyTextView = findViewById(R.id.extremelyTextView);

        backButton = findViewById(R.id.button_cancel);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeMessages(msgWhat);
                mRecorder.delete();
                finish();
            }
        });
        
        // add bug:TEWBB-934 chenyu 202009107 start
        mContext = SoundMeterActivity.this;
        mAudioManger = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
        // add bug:TEWBB-934 chenyu 202009107 end
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.hasMessages(msgWhat)) {
                return;
            }
            volume = mRecorder.getMaxAmplitude();  //获取声压值
            if(volume > 0 && volume < 1000000) {
                World.setDbCount(20 * (float)(Math.log10(volume)));  //将声压值转为分贝值
                soundDiscView.refresh();
                chooseHightLightText(World.dbCount);
            }
            handler.sendEmptyMessageDelayed(msgWhat, refreshTime);
        }
    };

    private void startListenAudio() {
        handler.sendEmptyMessageDelayed(msgWhat, refreshTime);
    }

    /**
     * 开始记录
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }else{
                Toast.makeText(this, "启动录音失败", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, "录音机已被占用或录音权限被禁止", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // add bug:TEWBB-934 chenyu 202009107 start
        if(!mAudioManger.getActiveRecordingConfigurations().isEmpty()){
            //Log.i("SoundMeterActivity","Recording resources are occupied by other applications!");
            Toast.makeText(mContext, mContext.getString(R.string.open_app_failed_warning), Toast.LENGTH_LONG).show();
            handler.removeMessages(msgWhat);
            mRecorder.delete();
            finish();
        }
        // add bug:TEWBB-934 chenyu 202009107 end
        
        soundDiscView = (SoundDiscView) findViewById(R.id.soundDiscView);
        File file = createFile("temp.amr");
        if (file != null) {
            Log.v("file", "file =" + file.getAbsolutePath());
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), "创建文件失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 停止记录
     */
    @Override
    protected void onPause() {
        super.onPause();
        mRecorder.delete(); //停止记录并删除录音文件
        handler.removeMessages(msgWhat);
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(msgWhat);
        mRecorder.delete();
        super.onDestroy();
    }

    private void chooseHightLightText(float db) {
        quiteTextView.setTextColor(Color.BLACK);
        indoorTextView.setTextColor(Color.BLACK);
        noisyTextView.setTextColor(Color.BLACK);
        harmfulTextView.setTextColor(Color.BLACK);
        extremelyTextView.setTextColor(Color.BLACK);

        if (db <= 45) {
            quiteTextView.setTextColor(Color.RED);
        } else if (db <= 60) {
            indoorTextView.setTextColor(Color.RED);
        } else if (db <= 80) {
            noisyTextView.setTextColor(Color.RED);
        } else if (db <= 115) {
            harmfulTextView.setTextColor(Color.RED);
        } else if (db <= 140) {
            extremelyTextView.setTextColor(Color.RED);
        }
    }

    private File createFile(String fileName) {
        if (mSoundMeterPath == null) {
            return null;
        }

        File myCaptureFile = new File(mSoundMeterPath + fileName);
        if (myCaptureFile.exists()) {
            myCaptureFile.delete();
        }
        try {
            myCaptureFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile;
    }
}
