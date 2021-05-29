package cyee.changecolors;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AppCompatActivity;

import com.cyee.utils.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import cyee.theme.global.CyeeThemeManager;

public class ChameleonColorManager implements OnChangeColorListener,
        OnChangeColorListenerWithParams {

    private static final String TAG = "Chameleon";

    private Context mContext;

    //变色时需要重启的activity列表
    private final ArrayList<AppCompatActivity> mActivityList = new ArrayList<AppCompatActivity>();
    //变色时不需要变色的activity列表
    private final ArrayList<AppCompatActivity> mNoChangeColorActivityList = new ArrayList<AppCompatActivity>();
    
    private final ArrayList<OnChangeColorListener> mOnChangeColorListenerList = new ArrayList<OnChangeColorListener>();
    // Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
    private final ArrayList<OnChangeColorListenerWithParams> mOnChangeColorListenerWithParamsList = new ArrayList<OnChangeColorListenerWithParams>();
    // Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end

    private final String CHAMELEON_ACTION = "cyee.intent.action.chameleon.CHANGE_COLOR";

    private ChangeColorReceiver mChangeColorReceiver;
    private IntentFilter mIntentFilter;

    private static ChameleonColorManager sChameleonColorManager;
    // 是否需要变色
    private static boolean sIsNeedChangeColor = false;

    // 顶部ActionBar背景颜色
    private static int sAppbarColorA1;
    // 应用的主区域背景颜色
    private static int sBackgroudColorB1;

    private static int sPopupBackgroudColorB2;
    // EditText背景颜色
    private static int sEditTextBackgroudColorB3;
    // Button背景颜色
    private static int sButtonBackgroudColorB4;
    // Statusbar背景颜色
    private static int sStatusbarBackgroudColorS1;
    // 系统虚拟按键背景颜色
    private static int sSystemNavigationBackgroudColorS2;
    // 强调色（多用于小控件Enable状态颜色）
    private static int sAccentColorG1;
    // 强调色（多用于小控件Enable状态颜色,G1的50%透明）
    private static int sAccentColorG2;
    // 顶部ActionBar前景颜色（如：文字、图标等）
    private static int sContentColorPrimaryOnAppbarT1;
    // 顶部ActionBar前景颜色（如：文字、图标等）二档
    private static int sContentColorSecondaryOnAppbarT2;
    // 顶部ActionBar前景颜色（如：文字、图标等）三档
    private static int sContentColorThirdlyOnAppbarT3;
    // 顶部ActionBar前景颜色（如：文字、图标等）四档
    private static int sContentColorForthlyOnAppbarT4;
    // 应用的主区域上前景颜色（如：文字、图标等）
    private static int sContentColorPrimaryOnBackgroudC1;
    // 应用的主区域上前景颜色（如：文字、图标等）二档
    private static int sContentColorSecondaryOnBackgroudC2;
    // 应用的主区域上前景颜色（如：文字、图标等）三档
    private static int sContentColorThirdlyOnBackgroudC3;
    // 状态栏上的前景颜色
    private static int sContentColorOnStatusbarS3;
    // 自定义框架列表类分割线颜色
    private static int sCustomCategoryDividerC1;
    
    // //强调色上的文字颜色
    // private static int sContentColorOnAccentColorF1;
    // 主题类型（深色主题、浅色主题）
    private static int sThemeType;
    // 是否是省电模式
    private static boolean sIsPowerSavingMode;
    private static boolean sIsRegistered = false;
    
    
    public enum CyeeThemeType {
        DEFAULT_THEME, NORMAL_THEME, SAVEMODE_THEME, GLOBAL_THEME
    }

    public static synchronized ChameleonColorManager getInstance() {
        if (sChameleonColorManager == null) {
            sChameleonColorManager = new ChameleonColorManager();
        }
        return sChameleonColorManager;
    }

    public void register(Context context) {
        register(context, true);
    }

    public void register(Context context, boolean restart) {
        // Gionee <zhaoyulong> <2015-08-04> add for CR01528942 begin
        if (context == null) {
            return;
        }
        Log.v(TAG, context.getPackageName() + ":"
                + context.getClass().getName()
                + " Register Chameleon, restart = " + restart);
        if (context == mContext || context.getApplicationContext() == mContext) {
            return;
        }
        // Gionee <zhaoyulong> <2015-08-04> add for CR01528942 end
        if (!(context instanceof Application)) {
            Log.e(TAG, "Context must be an Application");
            mContext = context.getApplicationContext();
        } else {
            mContext = context;
        }
        sIsRegistered = true;
        mIntentFilter = new IntentFilter(CHAMELEON_ACTION);
        mChangeColorReceiver = new ChangeColorReceiver();
        mChangeColorReceiver.setRestart(restart);
        mChangeColorReceiver.setOnChangeColorListener(this);
        mChangeColorReceiver.setOnChangeColorListenerWithParams(this);
        mContext.registerReceiver(mChangeColorReceiver, mIntentFilter);
        init();
    }

    public void unregister() {
        if (mContext != null) {
            Log.v(TAG, "unregistered:" + mContext.getClass().getName());
            mContext.unregisterReceiver(mChangeColorReceiver);
            mContext = null;
        } else {
            Log.v(TAG, "unregistered: null");
        }
    }

    public void registerNoChangeColor(AppCompatActivity activity) {
        mNoChangeColorActivityList.add(activity);
    }

    public void unregisterNoChangeColor(AppCompatActivity activity) {
        mNoChangeColorActivityList.remove(activity);
    }
    
    public void onCreate(AppCompatActivity activity) {
        mActivityList.add(activity);
    }

    public void onDestroy(AppCompatActivity activity) {
        mActivityList.remove(activity);
    }

    public void addOnChangeColorListener(
            OnChangeColorListener onChangeColorListener) {
        mOnChangeColorListenerList.add(onChangeColorListener);
    }

    public void removeOnChangeColorListener(
            OnChangeColorListener onChangeColorListener) {
        mOnChangeColorListenerList.remove(onChangeColorListener);
    }

    // Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
    public void addOnChangeColorListenerWithParams(
            OnChangeColorListenerWithParams onchangeColorListenerWithParams) {
        mOnChangeColorListenerWithParamsList
                .add(onchangeColorListenerWithParams);
    }

    public void removeOnChangeColorListenerWithParams(
            OnChangeColorListenerWithParams onChangeColorListenerWithParams) {
        mOnChangeColorListenerWithParamsList
                .remove(onChangeColorListenerWithParams);
    }

    // Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end
    /** {@inheritDoc} */
    @Override
    public void onChangeColor() {
        // TODO Auto-generated method stub
        for (Activity activity : mActivityList) {
            if (activity != null && activity.getParent() == null) {
                Log.d(TAG, "Restart Activity  : "
                        + activity.getComponentName().getClassName());
                activity.recreate();
            }
        }

        for (OnChangeColorListener onChangeColorListerner : mOnChangeColorListenerList) {
            if (onChangeColorListerner != null) {
                onChangeColorListerner.onChangeColor();
            }
        }
    }

    // Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
    /** {@inheritDoc} */
    @Override
    public void onChangeColor(int changeColorType) {
        // TODO Auto-generated method stub
        for (Activity activity : mActivityList) {
            if (activity != null && activity.getParent() == null) {
                Log.d(TAG, "Restart Activity  : "
                        + activity.getComponentName().getClassName());
                activity.recreate();
            }
        }

        for (OnChangeColorListenerWithParams onChangeColorListernerWithParams : mOnChangeColorListenerWithParamsList) {
            if (onChangeColorListernerWithParams != null) {
                onChangeColorListernerWithParams.onChangeColor(changeColorType);
            }
        }
    }

    // Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end

    public void init() {
        // Log.d("liushuai", "ChameleonColorManager.init begin" +
        // System.currentTimeMillis());
        // Gionee <zhaoyulong> <2015-07-01> add for CR01510132 begin
        Log.v(TAG, "context==null?:" + (mContext == null));
        if (mContext == null) {
            return;
        }

        // Gionee <zhaoyulong> <2015-07-01> add for CR01510132 end

        if (!getDataFromSettings()) {
            Log.d(TAG, "init-->getDataFromSettings return false");
            if (!getDataFromChameleon()) {
                Log.d(TAG, "No data in the database");
                sIsPowerSavingMode = false;
                sIsNeedChangeColor = false;
            }
        }
    }

    private boolean getDataFromSettings() {
        String allDataStr = null;

        try {
            allDataStr = Settings.System.getString(
                    mContext.getContentResolver(), "cyee_color_data");
        } catch (Exception e) {
            Log.d(TAG, "getDataFromSettings failed");
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(allDataStr)) {
            Log.d(TAG, "getDataFromSettings-->allDataStr == empty");
            sIsPowerSavingMode = false;
            sIsNeedChangeColor = false;
            return true;
        }

        String[] arr1 = allDataStr.split(";");
        String targetDataStr = "";
        switch (arr1.length) {
            case 1:
                targetDataStr = arr1[0];
                break;
            case 2:
                targetDataStr = arr1[1];
                break;
            case 3:
                targetDataStr = arr1[2];
                break;
            default:
                break;
        }

        String[] arr2 = targetDataStr.split(",");
        int id = Integer.parseInt(arr2[0]);
        sAppbarColorA1 = Integer.parseInt(arr2[1]);
        sBackgroudColorB1 = Integer.parseInt(arr2[2]);
        sPopupBackgroudColorB2 = Integer.parseInt(arr2[3]);
        sEditTextBackgroudColorB3 = Integer.parseInt(arr2[4]);
        sButtonBackgroudColorB4 = Integer.parseInt(arr2[5]);
        sStatusbarBackgroudColorS1 = Integer.parseInt(arr2[6]);
        sSystemNavigationBackgroudColorS2 = Integer.parseInt(arr2[7]);
        sAccentColorG1 = Integer.parseInt(arr2[8]);
        sAccentColorG2 = Integer.parseInt(arr2[9]);
        sContentColorPrimaryOnAppbarT1 = Integer.parseInt(arr2[10]);
        sContentColorSecondaryOnAppbarT2 = Integer.parseInt(arr2[11]);
        sContentColorThirdlyOnAppbarT3 = Integer.parseInt(arr2[12]);
        sContentColorForthlyOnAppbarT4 = Integer.parseInt(arr2[13]);
        sContentColorPrimaryOnBackgroudC1 = Integer.parseInt(arr2[14]);
        sContentColorSecondaryOnBackgroudC2 = Integer.parseInt(arr2[15]);
        sContentColorThirdlyOnBackgroudC3 = Integer.parseInt(arr2[16]);
        sContentColorOnStatusbarS3 = Integer.parseInt(arr2[17]);
        sThemeType = Integer.parseInt(arr2[18]);
        sCustomCategoryDividerC1 = getCustomDividerC1();
        // sContentColorOnAccentColorF1 = Integer.parseInt(arr2[19]);

        sIsPowerSavingMode = id == ColorConfigConstants.POWER_SAVING_ID;
        sIsNeedChangeColor = true;
        Log.d(TAG, "G1=" + sAccentColorG1 + "; B1=" + sBackgroudColorB1);
        return true;
    }

    private boolean getDataFromChameleon() {

        boolean result = false ;
        ContentResolver cr;
        try {
            cr = mContext.getContentResolver();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Uri uri = Uri.parse("content://com.cyee.chameleon."
                + "provider/colorConfiguration");

        Cursor cursor = null;

        try {
            cursor = cr.query(uri, null, null, null, null);
        } catch (Exception e) {
            Log.d(TAG, "getDataFromChameleon query failed");
            e.printStackTrace();
        }
        
        if (cursor != null && cursor.moveToFirst()) {

            sAppbarColorA1 = getColorFromCursor(cursor,
                    ColorConfigConstants.APPBAR_COLOR_A1,
                    ColorConfigConstants.DEFAULT_APPBAR_COLOR_A1);
            sBackgroudColorB1 = getColorFromCursor(cursor,
                    ColorConfigConstants.BACKGROUND_COLOR_B1,
                    ColorConfigConstants.DEFAULT_BACKGROUND_COLOR_B1);
            sPopupBackgroudColorB2 = getColorFromCursor(cursor,
                    ColorConfigConstants.POPUP_BACKGROUND_COLOR_B2,
                    ColorConfigConstants.DEFAULT_POPUP_BACKGROUND_COLOR_B2);
            sEditTextBackgroudColorB3 = getColorFromCursor(cursor,
                    ColorConfigConstants.EDIT_TEXT_BACKGROUND_COLOR_B3,
                    ColorConfigConstants.DEFAULT_EDIT_TEXT_BACKGROUND_COLOR_B3);
            sButtonBackgroudColorB4 = getColorFromCursor(cursor,
                    ColorConfigConstants.BUTTON_BACKGROUND_COLOR_B4,
                    ColorConfigConstants.DEFAULT_BUTTON_BACKGROUND_COLOR_B4);
            sStatusbarBackgroudColorS1 = getColorFromCursor(cursor,
                    ColorConfigConstants.STATUSBAR_BACKGROUND_COLOR_S1,
                    ColorConfigConstants.DEFAULT_STATUSBAR_BACKGROUND_COLOR_S1);
            sSystemNavigationBackgroudColorS2 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.SYSTEM_NAVIGATION_BACKGROUND_COLOR_S2,
                    ColorConfigConstants.DEFAULT_SYSTEM_NAVIGATION_BACKGROUND_COLOR_S2);

            sAccentColorG1 = getColorFromCursor(cursor,
                    ColorConfigConstants.ACCENT_COLOR_G1,
                    ColorConfigConstants.DEFAULT_ACCENT_COLOR_G1);

            sAccentColorG2 = getColorFromCursor(cursor,
                    ColorConfigConstants.ACCENT_COLOR_G2,
                    ColorConfigConstants.DEFAULT_ACCENT_COLOR_G2);

            sContentColorPrimaryOnAppbarT1 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_PRIMARY_ON_APPBAR_T1,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_PRIMARY_ON_APPBAR_T1);
            sContentColorSecondaryOnAppbarT2 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_SECONDARY_ON_APPBAR_T2,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_SECONDARY_ON_APPBAR_T2);
            sContentColorThirdlyOnAppbarT3 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_THIRDLY_ON_APPBAR_T3,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_THIRDLY_ON_APPBAR_T3);
            sContentColorForthlyOnAppbarT4 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_FORTHLY_ON_APPBAR_T4,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_FORTHLY_ON_APPBAR_T4);

            sContentColorPrimaryOnBackgroudC1 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_PRIMARY_ON_BACKGROUD_C1,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_PRIMARY_ON_BACKGROUD_C1);
            sContentColorSecondaryOnBackgroudC2 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_SECONDARY_ON_BACKGROUD_C2,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_SECONDARY_ON_BACKGROUD_C2);
            sContentColorThirdlyOnBackgroudC3 = getColorFromCursor(
                    cursor,
                    ColorConfigConstants.CONTENT_COLOR_THIRDLY_ON_BACKGROUD_C3,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_THIRDLY_ON_BACKGROUD_C3);
            sContentColorOnStatusbarS3 = getColorFromCursor(cursor,
                    ColorConfigConstants.CONTENT_COLOR_ON_STATUSBAR_S3,
                    ColorConfigConstants.DEFAULT_CONTENT_COLOR_ON_STATUSBAR_S3);
            sThemeType = getColorFromCursor(cursor,
                    ColorConfigConstants.THEME_TYPE,
                    ColorConfigConstants.DEFAULT_THEME_TYPE);
            sCustomCategoryDividerC1 = getCustomDividerC1();
            
            sIsPowerSavingMode = getColorFromCursor(cursor,
                    ColorConfigConstants.ID, ColorConfigConstants.DEFAULT_ID) == ColorConfigConstants.POWER_SAVING_ID;

            sIsNeedChangeColor = true;

            Log.d(TAG, "G1=" + sAccentColorG1 + "; B1=" + sBackgroudColorB1);

            result = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;

    }

    private int getCustomDividerC1() {
        if (getThemeType() == ColorConfigConstants.THEME_TYPE_DARK) {
            return getOverlapColor(getPopupBackgroudColor_B2(), 0xee000000);
        } else {
            return mContext.getResources().getColor(com.cyee.internal.R.color.cyee_color_category_divider);
        }
    }
    
    private int getColorFromCursor(Cursor cursor, String columnName) {
        return getColorFromCursor(cursor, columnName, 0);
    }

    private int getColorFromCursor(Cursor cursor, String columnName,
            int defaultColor) {
        int color = defaultColor;
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            color = cursor.getInt(index);
        }

        return color;
    }

    public static boolean isNeedChangeColor() {
        return sIsNeedChangeColor;
    }

    public CyeeThemeType getCyeeThemeType(Context cxt) {
        CyeeThemeType type = CyeeThemeType.DEFAULT_THEME;

        if (sIsRegistered && !isInNoChangeColorActivityList(cxt) && !TextUtils.isEmpty(CyeeThemeManager.getInstance(cxt)
                .getThemePath())) {
            type = CyeeThemeType.GLOBAL_THEME;
        } else if (sIsPowerSavingMode) {
            type = CyeeThemeType.SAVEMODE_THEME;
        } else if (sIsNeedChangeColor) {
            type = CyeeThemeType.NORMAL_THEME;
        }

        return type;
    }

    // Gionee <weidong> <2016-08-16> add for CR01747481 begin
    public static boolean isNeedChangeColor(Context cxt) {
        boolean ret = false;
        // 是否在需要重启的activity列表中
        ret = isInActivityList(cxt,
                ChameleonColorManager.getInstance().mActivityList);
        if (!ret) {
            //是否在不需要变色的activity列表中
            ret = !isInActivityList(
                    cxt,
                    ChameleonColorManager.getInstance().mNoChangeColorActivityList);
        }

        return sIsNeedChangeColor && ret;
    }

    private boolean isInNoChangeColorActivityList(Context cxt) {
        boolean isIn = false;
        isIn = isInActivityList(cxt,
                ChameleonColorManager.getInstance().mNoChangeColorActivityList);

        return isIn;
    }
    
    private static Context getBaseContext(Context cxt) {
        Context baseCxt = null;

        if (cxt instanceof AppCompatActivity) {
            return cxt;
        } else if (cxt instanceof ContextThemeWrapper) {
            baseCxt = ((ContextThemeWrapper) cxt).getBaseContext();
        } else if (cxt instanceof ContextWrapper) {
            baseCxt = ((ContextWrapper) cxt).getBaseContext();
        } else {
            return cxt;
        }
        return getBaseContext(baseCxt);
    }

    private static boolean isInActivityList(Context cxt,
            ArrayList<AppCompatActivity> activityList) {
        boolean ret = false;

        if (cxt instanceof AppCompatActivity) {
            ret = activityList.contains(cxt);
        } else {
            Context baseCxt = getBaseContext(cxt);
            for (AppCompatActivity a : activityList) {
                ret = a.equals(baseCxt);
                if (ret) {
                    break;
                }
            }
        }

        return ret;
    }
    // Gionee <weidong> <2016-08-16> add for CR01747481 end

    public static int getAppbarColor_A1() {
        return sAppbarColorA1;
    }

    public static int getBackgroudColor_B1() {
        return sBackgroudColorB1;
    }

    public static int getPopupBackgroudColor_B2() {
        return sPopupBackgroudColorB2;
    }

    public static int getEditTextBackgroudColor_B3() {
        return sEditTextBackgroudColorB3;
    }

    public static int getButtonBackgroudColor_B4() {
        return sButtonBackgroudColorB4;
    }

    public static int getStatusbarBackgroudColor_S1() {
        return sStatusbarBackgroudColorS1;
    }

    public static int getSystemNavigationBackgroudColor_S2() {
        return sSystemNavigationBackgroudColorS2;
    }

    public static int getAccentColor_G1() {
        return sAccentColorG1;
    }

    public static int getAccentColor_G2() {
        return sAccentColorG2;
    }

    public static int getContentColorPrimaryOnAppbar_T1() {
        return sContentColorPrimaryOnAppbarT1;
    }

    public static int getContentColorSecondaryOnAppbar_T2() {
        return sContentColorSecondaryOnAppbarT2;
    }

    public static int getContentColorThirdlyOnAppbar_T3() {
        return sContentColorThirdlyOnAppbarT3;
    }

    public static int getContentColorForthlyOnAppbar_T4() {
        return sContentColorForthlyOnAppbarT4;
    }

    public static int getContentColorPrimaryOnBackgroud_C1() {
        return sContentColorPrimaryOnBackgroudC1;
    }

    public static int getContentColorSecondaryOnBackgroud_C2() {
        return sContentColorSecondaryOnBackgroudC2;
    }

    public static int getContentColorThirdlyOnBackgroud_C3() {
        return sContentColorThirdlyOnBackgroudC3;
    }

    public static int getContentColorOnStatusbar_S3() {
        return sContentColorOnStatusbarS3;
    }

    public static int getCustomCategoryDividerC1() {
        return sCustomCategoryDividerC1;
    }
    
    // public static int getContentColorOnAccentColorF1() {
    // return sContentColorOnAccentColorF1;
    // }

    public static int getThemeType() {
        return sThemeType;
    }

    public static boolean isPowerSavingMode() {
        return sIsPowerSavingMode;
    }

    public static void clearDrawableCaches(final Context context) {
        try {
            Class resource;
            resource = Class.forName("android.content.res.Resources");
            Method method=resource.getMethod("cyeeClearDrawableCaches");

            method.invoke(context.getResources());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 应用颜色 + 颜色
     * 
     * @param originalColor
     *            原始颜色
     * @param maskColor
     *            要加的颜色
     * @return
     */
    public static int getOverlapColor(int originalColor, int maskColor) {
        int overlapColor = 0;
        float opaque = (((maskColor & 0xff000000) >> 24) & 0x0000ff) / 255.0f;
        int originalRedColor = (originalColor & 0xff0000) >> 16;
        int originalGreenColor = (originalColor & 0x00ff00) >> 8;
        int originalBlueColor = (originalColor & 0x0000ff);

        int maskRedColor = (maskColor & 0xff0000) >> 16;
        int maskGreenColor = (maskColor & 0x00ff00) >> 8;
        int maskBlueColor = (maskColor & 0x0000ff);

        int overlapRedColor = (int) (originalRedColor * (1 - opaque) + maskRedColor * opaque);
        int overlapGreenColor = (int) (originalGreenColor * (1 - opaque) + maskGreenColor * opaque);
        int overlapBlueColor = (int) (originalBlueColor * (1 - opaque) + maskBlueColor * opaque);

        overlapColor = (overlapRedColor << 16) + (overlapGreenColor << 8) + overlapBlueColor + 0xff000000;

        return overlapColor;
    }
    
    /**
     * 判断是否为明，暗主题
     * @param rgb 需要判断的色值
     * @return >204 说明颜色比较淡，暂认为是名主题
     */
    private static int toGrey(int rgb) {
        int blue = (rgb & 0x000000FF) >> 0;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }
    
    /**
     * 判断是否为明主题，由以前的framework层提供，非变色方法
     * @param grey
     * @return true 是名主题
     */
    public static boolean isLightTheme(int grey) {
        int LIGHT_THEME_GREY_THRESHOLD = 204; //名主题阀值
        boolean isLight = false;
        
        if (toGrey(grey) > LIGHT_THEME_GREY_THRESHOLD) {
            isLight = true;
        }
        
        return isLight;
    }
    
}
