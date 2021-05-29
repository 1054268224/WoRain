package com.odm.tool;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
//add bug:TELWL-421 wushanfei 20201216 start
/*import android.view.WindowInsetsController;*/

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowManager;
//add bug:TELWL-421 wushanfei 20201216 end
public class MainActivity extends Activity {
    private final String TAG = "OdmToolBox";

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int MY_PERMISSIONS_REQUEST_MULTI = 4;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    List<String> mPermissionList = new ArrayList<>();

    private GridView mGridview;
    private String[] activityStrings;
    private TypedArray activityIcon;
    private TypedArray activityTitle;

    private GridAdapter mGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //add bug:TELWL-421 wushanfei 20201216 start
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsCompat controller = ViewCompat.getRootWindowInsets(mGridview);
            *//*final WindowInsetsController insetsController = getWindow().getInsetsController();*//*
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
            if (con != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }*/
        //add bug:TELWL-421 wushanfei 20201216 end
        getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.black));
        mGridview = findViewById(R.id.tool_grid);
        initData();

        checkPermissions();
    }

    private void initData() {
        activityStrings = getResources().getStringArray(R.array.actions_class);
        activityIcon = getResources().obtainTypedArray(R.array.actions_images);
        activityTitle = getResources().obtainTypedArray(R.array.actions_strings);
        mGridAdapter = new GridAdapter(this,activityStrings, activityIcon, activityTitle);
        mGridview.setAdapter(mGridAdapter);
    }

    private void checkPermissions() {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST_MULTI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult, requestCode:" + requestCode);
        if(permissions == null || permissions.length == 0 ||
                grantResults == null || grantResults.length == 0){
            Log.d(TAG, "onRequestPermissionsResult, Permission or grant res null");
            return;
        }
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:                
                break;
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                break;
            case MY_PERMISSIONS_REQUEST_MULTI:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //modify BUG_ID:EWSY-738 sunshiwei 20190218 start
                        //boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                        //if (showRequestPermission) {
                        Toast.makeText(MainActivity.this,R.string.toolbox_storage_permission_deny,Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"PERMISSION NOT GRANTED");
                            finish();
                        //}
                        //modify BUG_ID:EWSY-738 sunshiwei 20190218 end
                    }
                }
                break;
            default:
                // do nothing
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
