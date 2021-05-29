/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.receiver.ThemeChangeReceiver;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncAppIconLoader implements ThemeChangedCallback {
    private static final String TAG = "AsyncAppIconLoader";
    private static AsyncAppIconLoader sInstance;
    private static final ExecutorService sExecutorService = Executors.newFixedThreadPool(10);

    private final Map<String, SoftReference<Drawable>> mImageCache;

    private ThemeChangeReceiver mThemeChangeReceiver;

    public static synchronized AsyncAppIconLoader getInstance() {
        if (null == sInstance) {
            sInstance = new AsyncAppIconLoader();
        }
        return sInstance;
    }

    private AsyncAppIconLoader() {
        // Chenyee xionghg 2017-09-18 modify for 209267 begin
        // Because "mImageCache.put()" in line77 is not thread safe
        // mImageCache = new HashMap<String, SoftReference<Drawable>>();
        mImageCache = new ConcurrentHashMap<String, SoftReference<Drawable>>();
        // Chenyee xionghg 2017-09-18 modify for 209267 end
    }

    public synchronized Drawable loadAppIconDrawable(final Context context, final String packageName,
                                                     final ImageCallback imageCallback) {
        setThemeChangeReceiver(context);

        if (mImageCache.containsKey(packageName)) {
            SoftReference<Drawable> softReference = mImageCache.get(packageName);
            if (null != softReference && null != softReference.get()) {
                return softReference.get();
            } else {
                mImageCache.remove(packageName);
            }
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Drawable) message.obj, packageName);
            }
        };

        sExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = loadAppIconDrawable(context, packageName);
                if (null != drawable) {
                    mImageCache.put(packageName, new SoftReference<Drawable>(drawable));
                }
                Message message = handler.obtainMessage(0, drawable);
                handler.sendMessage(message);
            }
        });
        return null;
    }

    private void setThemeChangeReceiver(Context context) {
        if (mThemeChangeReceiver != null) {
            return;
        }
        Log.d(TAG, "set theme change receiver");
        mThemeChangeReceiver = new ThemeChangeReceiver(context.getApplicationContext(), this);
        mThemeChangeReceiver.registerThemeChangeReceiver();
    }

    private static Drawable loadAppIconDrawable(Context context, String pkgName) {
        Drawable drawable = null;
        ApplicationInfo info = HelperUtils.getApplicationInfo(context, pkgName);
        if (null != info) {
            drawable = HelperUtils.loadIcon(context, info);
        }
        return drawable;
    }

    public interface ImageCallback {
        void imageLoaded(Drawable imageDrawable, String pkgName);
    }

    public synchronized void resetCache() {
        mImageCache.clear();
    }

    @Override
    public void changeTheme(String category) {
        Log.d(TAG, "theme changed category = " + category + " clean icon cache");
        resetCache();
    }
}
