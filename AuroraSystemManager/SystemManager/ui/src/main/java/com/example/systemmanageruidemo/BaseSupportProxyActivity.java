package com.example.systemmanageruidemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.systemmanageruidemo.actionpresent.PresentI;
import com.example.systemmanageruidemo.actionview.ViewAction;

/**
 * 当自己作为remoteactivity时，能获得非空的context环境。
 */
public abstract class BaseSupportProxyActivity<D extends PresentI> extends AppCompatActivity implements ViewAction<D> {

    private static final String TAG = "DLBasePluginActivity";

    /**
     * 代理activity，可以当作Context来使用，会根据需要来决定是否指向this
     */
    protected AppCompatActivity mProxyActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (null != mProxyActivity) {
        } else
            super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mProxyActivity) {
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        if (null != mProxyActivity) {
        } else

            super.onStart();
    }

    @Override
    public void onRestart() {
        if (null != mProxyActivity) {
        } else
            super.onRestart();
    }

    @Override
    public void onResume() {
        if (null != mProxyActivity) {
        } else
            super.onResume();
    }

    @Override
    public void onPause() {
        if (null != mProxyActivity) {
        } else
            super.onPause();
    }

    @Override
    public void onStop() {
        if (null != mProxyActivity) {
        } else
            super.onStop();
    }

    @Override
    public void onDestroy() {
        if (null != mProxyActivity) {
        } else
            super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != mProxyActivity) {
        } else
            super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (null != mProxyActivity) {
        } else
            super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (null != mProxyActivity) {
        } else
            super.onNewIntent(intent);
    }


    public BaseSupportProxyActivity attach(AppCompatActivity proxyActivity) {
        mProxyActivity = (AppCompatActivity) proxyActivity;
        return this;
    }

    @Override
    public void setContentView(View view) {
        if (null != mProxyActivity) {
            mProxyActivity.setContentView(view);
        } else {
            super.setContentView(view);
        }

    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (null != mProxyActivity) {
            mProxyActivity.setContentView(view, params);
        } else {
            super.setContentView(view);
        }

    }

    @Override
    public void setContentView(int layoutResID) {
        if (null != mProxyActivity) {
            mProxyActivity.setContentView(layoutResID);
        } else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        if (null != mProxyActivity) {
            mProxyActivity.addContentView(view, params);
        } else {
            super.setContentView(view);
        }
    }

    @Override
    public <T extends View> T findViewById(int id) {
        if (null != mProxyActivity) {
            return mProxyActivity.findViewById(id);
        } else {
            return super.findViewById(id);
        }
    }

    @Override
    public Intent getIntent() {
        if (null != mProxyActivity) {
            return mProxyActivity.getIntent();
        } else {
            return super.getIntent();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if (null != mProxyActivity) {
            return mProxyActivity.getClassLoader();
        } else {
            return super.getClassLoader();
        }

    }

    @Override
    public Resources getResources() {
        if (null != mProxyActivity) {
            return mProxyActivity.getResources();
        } else {
            return super.getResources();
        }

    }

    @Override
    public String getPackageName() {
        if (null != mProxyActivity) {
            return mProxyActivity.getPackageName();
        } else {
            return super.getPackageName();
        }
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        if (null != mProxyActivity) {
            return mProxyActivity.getLayoutInflater();
        } else {
            return super.getLayoutInflater();
        }

    }

    @Override
    public MenuInflater getMenuInflater() {
        if (null != mProxyActivity) {
            return mProxyActivity.getMenuInflater();
        } else {
            return super.getMenuInflater();
        }

    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (null != mProxyActivity) {
            return mProxyActivity.getSharedPreferences(name, mode);
        } else {
            return super.getSharedPreferences(name, mode);
        }
    }

    @Override
    public Context getApplicationContext() {
        if (null != mProxyActivity) {
            return mProxyActivity.getApplicationContext();
        } else {
            return super.getApplicationContext();
        }
    }

    @Override
    public WindowManager getWindowManager() {
        if (null != mProxyActivity) {
            return mProxyActivity.getWindowManager();
        } else {
            return super.getWindowManager();
        }

    }

    @Override
    public Window getWindow() {
        if (null != mProxyActivity) {
            return mProxyActivity.getWindow();
        } else {
            return super.getWindow();
        }

    }

    @Override
    public Object getSystemService(String name) {
        if (null != mProxyActivity) {
            return mProxyActivity.getSystemService(name);
        } else {
            return super.getSystemService(name);
        }
    }

    @Override
    public void finish() {
        if (null != mProxyActivity) {
            mProxyActivity.finish();
        } else {
            super.finish();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (null != mProxyActivity) {
            mProxyActivity.startActivity(intent);
        } else {
            super.startActivity(intent);
        }
    }
    @Override
    public void startActivityForResult(Intent intent,int requestcode) {
        if (null != mProxyActivity) {
            mProxyActivity.startActivityForResult(intent,requestcode);
        } else {
            super.startActivityForResult(intent,requestcode);
        }
    }

    public AppCompatActivity getRealContext() {
        if (null != mProxyActivity) {
            return mProxyActivity;
        } else {
            return this;
        }
    }

    public Intent actionNewActivity(Context context, Class<?> classes) {
        Intent re = null;
        if (null != mProxyActivity) {
            String name = classes.getName().substring(classes.getName().lastIndexOf('.') + 1);
            String proxyname = mProxyActivity.getClass().getName().substring(0, mProxyActivity.getClass().getName().lastIndexOf('.')) + ".Host" + name;
            if (interpolator != null) {
                if (interpolator.isfif(proxyname)) {
                    proxyname = interpolator.fifname(proxyname);
                }
            }
            try {
                re = new Intent(context, Class.forName(proxyname));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            re = new Intent(this, classes);
        }
        return re;
    }

    public ActivityNameInterpolator interpolator;

    public interface ActivityNameInterpolator {
        abstract boolean isfif(String name);

        abstract String fifname(String name);
    }

}
