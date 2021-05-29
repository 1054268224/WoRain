package com.cydroid.softmanager.powersaver.mode.item;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.service.wallpaper.WallpaperService;

public class PowerModeItemDarkWallpaper extends PowerModeItem {

    private static final String WALLPAPER_SAVE_PATH = "/data/data/com.cydroid.softmanager/";
    private static final String WALLPAPER_SAVE_FILENAME = "wallpaper.png";

    private static final String RES_STRING_DARK_WALLPAPER = "dark_warpaper";

    public PowerModeItemDarkWallpaper(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    public String getSavedLiveWallpaper() {
        return mProviderHelper.getString(mCheckpointKey, "");
    }

    private boolean isUseLiveWallpaper() {
        WallpaperInfo info = getWallpaperInfo();
        return info != null;
    }

    private WallpaperInfo getWallpaperInfo() {
        WallpaperManager wpm = WallpaperManager.getInstance(mContext);
        WallpaperInfo info = wpm.getWallpaperInfo();
        return info;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        saveStaticWallpaper(saveKey);
        if (isUseLiveWallpaper()) {
            saveLiveWallpaperInfo(saveKey);
        }
    }

    private void saveStaticWallpaper(String saveKey) {
        Log.d(TAG, "save Original Wallpaper");
        WallpaperManager wm = WallpaperManager.getInstance(mContext);
        Bitmap bitmap = wm.getBitmap();
        try {
            saveBitmap(bitmap, WALLPAPER_SAVE_PATH, WALLPAPER_SAVE_FILENAME);
            mProviderHelper.putString(saveKey, WALLPAPER_SAVE_PATH + WALLPAPER_SAVE_FILENAME);
        } catch (Exception e) {
            Log.d(TAG, "save Original Wallpaper, save bitmap error: " + e.toString());
            return;
        }
    }

    private void saveLiveWallpaperInfo(String saveKey) {
        WallpaperInfo wallpaperInfo = getWallpaperInfo();
        if (wallpaperInfo == null) {
            return;
        }
        String pkgName = wallpaperInfo.getPackageName();
        String clsName = wallpaperInfo.getServiceName();
        if (pkgName == null) {
            pkgName = "";
        }
        if (clsName == null) {
            clsName = "";
        }
        Log.d(TAG, "live wallpaper pkgName = " + pkgName + ", clsName = " + clsName);
        mProviderHelper.putString(saveKey, pkgName + "/" + clsName);
    }

    private void saveBitmap(Bitmap bm, String path, String picName) throws Exception {
        File file = new File(path, picName);
        FileOutputStream out = null;
        try {
            Log.d(TAG, "saveBitmap size:" + bm.getWidth() + "x" + bm.getHeight() + " bytesCount:"
                    + bm.getByteCount());
            out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            if (file.exists()) {
                Log.d(TAG, "wallpaper save to " + file.getAbsolutePath());
            } else {
                Log.d(TAG, "saved file " + file.getAbsolutePath() + " not exist ,save failed!");
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "not found file: " + e.toString());
        } catch (IOException e) {
            Log.d(TAG, "flush outstream error or close error" + e.toString());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        String configVal = mProviderHelper.getString(saveKey, RES_STRING_DARK_WALLPAPER);
        if (configVal.equals(RES_STRING_DARK_WALLPAPER)) {
            Log.d(TAG, "----->set dark wallpaper");
            setWallpaper(BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources()
                    .getIdentifier(RES_STRING_DARK_WALLPAPER, "drawable", mContext.getPackageName())));
            return true;
        }
        Log.d(TAG, "restore static Wallpaper");
        String path = WALLPAPER_SAVE_PATH + WALLPAPER_SAVE_FILENAME;
        setWallpaper(BitmapFactory.decodeFile(path));
        if (!configVal.equals(WALLPAPER_SAVE_PATH + WALLPAPER_SAVE_FILENAME)) {
//            String pkgName = mProviderHelper.getString(PowerConsts.LIVE_WALLPAPER_PKGNAME, null);
//            String clsName = mProviderHelper.getString(PowerConsts.LIVE_WALLPAPER_CLASSNAME, null);
            String[] livePaperInfo = configVal.split("/", 2);
            if (livePaperInfo.length < 2) {
                Log.d(TAG, "invali live wallpaper record " + configVal);
                return true;
            }
            Log.d(TAG, " restore Live Wallpaper -----> pkgName: " + livePaperInfo[0] + ", clsName:"
                    + livePaperInfo[1]);
            setLiveWallPaper(livePaperInfo[0], livePaperInfo[1]);
        }
        return true;
    }

    private void setLiveWallPaper(String pkgName, String clsName) {
        WallpaperManager wallpapermanager = WallpaperManager.getInstance(mContext);
        Intent intent = new Intent(WallpaperService.SERVICE_INTERFACE);
        intent.setClassName(pkgName, clsName);
        try {
            Class<WallpaperManager> c = WallpaperManager.class;
            Method method = c.getMethod("getIWallpaperManager");
            method.setAccessible(true);
            Object obj = method.invoke(wallpapermanager);
            ((IWallpaperManager) obj).setWallpaperComponent(intent.getComponent());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(TAG, "setLiveWallPaper other exception " + e);
            e.printStackTrace();
        }
    }

    private void setWallpaper(Bitmap bitmap) {
        Log.d(TAG, "----->setWallpaper()");
        if (bitmap == null) {
            Log.d(TAG, " setWallpaper  bitmap == null");
            return;
        }
        WallpaperManager wm = WallpaperManager.getInstance(mContext);
        try {
            wm.setBitmap(bitmap);
            Log.d(TAG,
                    " -------> setWallpaper Success, bitmap.getHeight = " + bitmap.getHeight()
                            + ", bitmap.getWidth = " + bitmap.getWidth() + " bitmap.getByteCount="
                            + bitmap.getByteCount());
        } catch (Exception e) {
            Log.e(TAG, "----->setWallpaper() throw Exception", e);
            return;
        }
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

}
