package com.cydroid.softmanager.softmanager.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.model.ItemInfo;
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

public class SoftManagerIconLoader{
    private final Context mContext;
    private final AsyncAppIconLoader mAsyncAppIconLoader;
    private final List<ItemInfo> mData;
    private ApplicationInfo mApplicationInfo;
    private static final ExecutorService sExecutorService = Executors.newFixedThreadPool(10);

    private final Map<String, SoftReference<Drawable>> mImageCache;

    public SoftManagerIconLoader(Context context, Object data) {
        mContext = context;
        mData = (List<ItemInfo>)data;
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
        mImageCache = new ConcurrentHashMap<String, SoftReference<Drawable>>();
    }

    public Object getListDatas(){
        return mData;
    }

    public void loadAppIcon(ImageLoadCompleteCallback callback){
        sExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < mData.size()){
                    ItemInfo info = mData.get(i);
                    Drawable drawable = loadAppIconDrawableFromCache(info.getPackageName(), i);
                    if (drawable == null){
                        drawable = loadAppIconDrawable(mContext, info.getPackageName());
                        if (null != drawable) {
                            mImageCache.put(info.getPackageName(), new SoftReference<Drawable>(drawable));
                        }
                        setItemIcon(i, drawable);
                    }
                    i++;
                }
                callback.loadComplete();
            }
        });
    }

    public interface ImageLoadCompleteCallback {
        void loadComplete();
    }

    private Drawable loadAppIconDrawableFromCache(String packageName, int position){
        if (mImageCache.containsKey(packageName)) {
            SoftReference<Drawable> softReference = mImageCache.get(packageName);
            if (null != softReference && null != softReference.get()) {
                Drawable drawable = softReference.get();
                setItemIcon(position, drawable);
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

    private void setItemIcon(int position, Drawable drawable){
        if (position >= 0 && position < mData.size()){
            mData.get(position).setIcon(drawable);
        }
    }
}
