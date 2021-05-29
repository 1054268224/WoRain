package cyee.theme.global;

import java.lang.reflect.Method;

import cyee.app.CyeeActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.cyee.utils.Log;

import com.chenyee.fulltheme.resinfo.FullThemeResInfoManager;

public class CyeeThemeManager {

    private static final String TAG = "CyeeThemeManager";
    private static CyeeThemeManager sInstance;
    private String mCurThemePath;
    private AssetManager mCurAssetManager;
    private String mFullThemePath;
    private String mFullThemeApkName;

    public static synchronized CyeeThemeManager getInstance(Context cxt) {
        if (null == sInstance) {
            sInstance = new CyeeThemeManager(cxt);
        }

        return sInstance;
    }

    private CyeeThemeManager(Context cxt) {
        getFullThemePath(cxt);
    }

    public void onDestroy() {
        sInstance = null;
        mCurAssetManager = null;
        mCurThemePath = null;
        mFullThemeApkName = null;
    }

    public Resources loadCyeeThemeResources(CyeeActivity cxt) {
        String apkPath = getThemePath();

        Log.d(TAG,"loadCyeeThemeResources apkPath="+apkPath);
        AssetManager assetManager = loadCyeeResources(apkPath);
        Resources res = null;
        
        Log.e(TAG,"loadCyeeThemeResources assetManager="+assetManager);
        if (assetManager != null) {
            Resources superRes = cxt.getSuperResources();
            res = new CyeeResources(cxt, assetManager, superRes);
        }
        
        return res;
    }
    
    public String getCyeeThemeApkPackageName(Context cxt) {
        mFullThemeApkName = FullThemeResInfoManager.getInstance()
                .getApkPackageName(cxt);
        Log.e(TAG, "getCyeeThemeApkPackageName mFullThemeApkName=" + mFullThemeApkName);
        
        return mFullThemeApkName;
    }
    
    public AssetManager getCurrentAssertManager() {
        return mCurAssetManager;
    }

    public AssetManager loadCyeeResources(String apkPath) {
        if (null != mCurAssetManager && apkPath.equals(mCurThemePath)) {
            return mCurAssetManager;
        }

        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod(
                    "addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apkPath);
            mCurThemePath = apkPath;
            mCurAssetManager = assetManager;
            Log.e(TAG, "loadCyeeResources mCurThemePath=" + mCurThemePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return assetManager;
    }

    private void getFullThemePath(Context cxt) {
//        mFullThemePath = FullThemeResInfoManager.getInstance()
//                .getFullThemeApkPath(cxt);
        Log.e(TAG, "getFullThemePath mFullThemePath=" + mFullThemePath);
    }

    public String getThemePath() {
        return mFullThemePath;
    }

    public boolean isM2017() {
        return false;//FullThemeResInfoManager.getInstance().isM2017();
    }
    
    public boolean existResInTheme(Context cxt, Resources res, String resName) {
        boolean exist = false;

        exist = FullThemeResInfoManager.getInstance().needReLoadNewRes(cxt,
                res, resName);

        Log.d(TAG, "existResInTheme resName="+resName+";exist="+exist);
        
        return exist;
    }

}
