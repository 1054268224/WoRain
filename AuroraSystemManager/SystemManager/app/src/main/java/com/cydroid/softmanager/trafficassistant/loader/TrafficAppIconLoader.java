package com.cydroid.softmanager.trafficassistant.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.trafficassistant.model.TrafficNetworkControlAppInfo;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhaocaili on 18-9-18.
 */

public class TrafficAppIconLoader {
    private final Context mContext;
    private static ExecutorService sExecutorService;

    private final Map<String, SoftReference<Drawable>> mImageCache;

    public TrafficAppIconLoader(Context context) {
        mContext = context;
        mImageCache = new ConcurrentHashMap<String, SoftReference<Drawable>>();
    }

    public void loadAppIcon(List<TrafficNetworkControlAppInfo> mData, ImageLoadCompleteCallback callback){
        sExecutorService = Executors.newFixedThreadPool(10);
        sExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < mData.size() - 1){
                    TrafficNetworkControlAppInfo info = mData.get(i);
                    Drawable drawable = loadAppIconDrawableFromCache(mData, i);
                    if (drawable == null){
                        drawable = loadAppIconDrawable(mContext, info.getAppPkgName());
                        if (null != drawable) {
                            mImageCache.put(info.getAppPkgName(), new SoftReference<Drawable>(drawable));
                        }
                        info.setIcon(drawable);
                    }
                    i++;
                }
                callback.loadComplete(mData);
            }
        });
    }

    public interface ImageLoadCompleteCallback {
        void loadComplete(List<TrafficNetworkControlAppInfo> data);
    }

    private Drawable loadAppIconDrawableFromCache(List<TrafficNetworkControlAppInfo> mData, int position){
        if (position < 0 || position >= mData.size() - 1){
            return null;
        }
        String packageName = mData.get(position).getAppPkgName();
        if (mImageCache.containsKey(packageName)) {
            SoftReference<Drawable> softReference = mImageCache.get(packageName);
            if (null != softReference && null != softReference.get()) {
                Drawable drawable = softReference.get();
                mData.get(position).setIcon(drawable);
                return drawable;
            } else {
                mImageCache.remove(packageName);
            }
        }
        return null;
    }

    private Drawable loadAppIconDrawable(Context context, String pkgName) {
        Drawable drawable = null;
        ApplicationInfo info = HelperUtils.getApplicationInfo(context, pkgName);
        if (null != info) {
            drawable = HelperUtils.loadIcon(context, info);
        }
        return drawable;
    }
}
