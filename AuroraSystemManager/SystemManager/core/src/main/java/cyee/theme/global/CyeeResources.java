package cyee.theme.global;

import java.io.InputStream;

import android.annotation.ArrayRes;
import android.annotation.ColorRes;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.util.TypedValue;

public class CyeeResources extends Resources implements ICyeeResource {

    private final static String LOGTAG = "CyeeResources";
    private final Resources mResources;
    private final Context mCxt;

    public CyeeResources(Context cxt, AssetManager assets, Resources resources) {
        super(assets, resources.getDisplayMetrics(), resources
                .getConfiguration());
        mCxt = cxt;
        mResources = resources;
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        Log.d(LOGTAG, "getDrawable id=" + id);

        int newId = getTemplateResourceById(id, "drawable");
        Log.d(LOGTAG, "getDrawable newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getDrawable(newId);
        }

        return mResources.getDrawable(id);
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        
        int newId = getTemplateResourceById(id, "drawable");
        Log.d(LOGTAG, "openRawResource 1 newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.openRawResource(newId);
        }
        
        return mResources.openRawResource(id);
    }
    
    @Override
    public InputStream openRawResource(int id, TypedValue value)
            throws NotFoundException {
        
        int newId = getTemplateResourceById(id, "drawable");
        Log.d(LOGTAG, "openRawResource 2 newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.openRawResource(newId, value);
        }
        
        return mResources.openRawResource(id, value);
    }
    
    // @Override
    // public Drawable getDrawable(int id, Theme theme) throws NotFoundException
    // {
    // int newId = 0;
    // Drawable able = null;
    // try {
    // newId = getTemplateResourceById(id, "drawable");
    // } catch (NotFoundException e) {
    // able = super.getDrawable(id, theme);
    // } finally {
    //
    // }
    // if (null != able) {
    // return able;
    // } else if (newId != 0) {
    // return super.getDrawable(newId, theme);
    // }
    //
    // return mResources.getDrawable(id, theme);
    // }

    /*
     * parma id:此id目前必须传入的是当前资源的id。例如调用这个函数时的id，不应该是通过名字找到的id，而应该是直接的资源id
     */
    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        Log.d(LOGTAG, "getDrawable start");
        Drawable able = null;
        try {
            able = super.getDrawable(id, theme);
        } catch (Exception e) {
            e.printStackTrace();
            able = mResources.getDrawable(id, theme);
        }

        return able;
    }
    
    @Override
    public CharSequence getQuantityText(int id, int quantity)
            throws NotFoundException {
        Log.d(LOGTAG, "getQuantityText start");
        int newId = getTemplateResourceById(id, "string");
        Log.d(LOGTAG, "getQuantityText newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getQuantityText(id, quantity);
        }

        return mResources.getQuantityText(id, quantity);
    }
    
    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        Log.d(LOGTAG, "getStringArray start");
        String[] array = null;
        try {
            array = super.getStringArray(id);
        } catch (Exception e) {
            e.printStackTrace();
            array = mResources.getStringArray(id);
        }

        return array;
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        Log.d(LOGTAG, "getXml start");
        int newId = getTemplateResourceById(id, "xml");
        Log.d(LOGTAG, "getLayout newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getXml(newId);
        }

        return mResources.getXml(id);
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        Log.d(LOGTAG, "getAnimation start");
        int newId = getTemplateResourceById(id, "anim");
        Log.d(LOGTAG, "getAnimation newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getAnimation(newId);
        }

        return mResources.getAnimation(id);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        Log.d(LOGTAG, "getLayout start");
        int newId = getTemplateResourceById(id, "layout");
        Log.d(LOGTAG, "getLayout newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getLayout(newId);
        }

        return mResources.getLayout(id);
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        Log.d(LOGTAG, "getTextArray start");
        int newId = getTemplateResourceById(id, "array");

        if (newId != 0) {
            return super.getTextArray(newId);
        }

        return mResources.getTextArray(id);
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        Log.d(LOGTAG, "getIdentifier start");
        int resId = 0;

        try {
            resId = super.getIdentifier(name, defType, defPackage);
        } catch (Exception e) {
            Log.e(LOGTAG, "getValue try catch");
            e.printStackTrace();
            Log.e(LOGTAG, "getValue try catch end");
        } finally {
            if (resId == 0) {
                resId = mResources.getIdentifier(name, defType, defPackage);
            }
        }
        Log.e(LOGTAG, "getIdentifier getValue name=" + name + ";defType="
                + defType + ";defPackage=" + defPackage + ";resId=" + resId);

        return resId;
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        Log.d(LOGTAG, "getColor start");
        int newId = getTemplateResourceById(id, "color");
        Log.d(LOGTAG, "getColor newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getColor(newId);
        }

        return mResources.getColor(id);
    }
    
    @Override
    @SuppressLint("NewApi")
    public AssetFileDescriptor openRawResourceFd(int id)
            throws NotFoundException {
        Log.d(LOGTAG, "openRawResourceFd start");
        AssetFileDescriptor afd = null;
        try {
            afd = super.openRawResourceFd(id);
        } catch (Exception e) {
            e.printStackTrace();
            afd = mResources.openRawResourceFd(id);
        }
        return afd;
    }
    
    @Override
    @SuppressLint("NewApi")
    public Movie getMovie(int id) throws NotFoundException {
        Log.d(LOGTAG, "getMovie start");
        Movie movie = null;
        try {
            movie = super.getMovie(id);
        } catch (Exception e) {
            e.printStackTrace();
            movie = mResources.getMovie(id);
        }
        return movie;
    }
    
    @Override
    @SuppressLint("NewApi")
    public int getColor(@ColorRes int id, @Nullable Theme theme) throws NotFoundException {
        int newId = getTemplateResourceById(id, "color");
        Log.d(LOGTAG, "getColor 2 newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getColor(newId, theme);
        }
        return mResources.getColor(id, theme);
    }
    
    @Override
    @SuppressLint("NewApi")
    public int[] getIntArray(@ArrayRes int id) throws NotFoundException {
        Log.d(LOGTAG, "getIntArray start");
        int[] array = null;
        try {
            array = super.getIntArray(id);
        } catch (Exception e) {
            e.printStackTrace();
            array = mResources.getIntArray(id);
        }
        return array;
    }
    
    @Override
    @SuppressLint("NewApi")
    public float getFraction(int id, int base, int pbase) {
        Log.d(LOGTAG, "getInteger start");
        float val = -1;
        try {
            val = super.getFraction(id, base, pbase);
        } catch (Exception e) {
            e.printStackTrace();
            val = mResources.getFraction(id, base, pbase);
        }
        return val;
    }

    @Override
    @SuppressLint("NewApi")
    public int getInteger(int id) throws NotFoundException {
        Log.d(LOGTAG, "getInteger start");
        int val = -1;
        try {
            val = super.getInteger(id);
        } catch (Exception e) {
            e.printStackTrace();
            val = mResources.getInteger(id);
        }
        return val;
    }

    @Override
    @SuppressLint("NewApi")
    public float getFloat(int id) {
        Log.d(LOGTAG, "getInteger start");
        float val = -1;
        try {
            val = super.getFloat(id);
        } catch (Exception e) {
            e.printStackTrace();
            val = mResources.getFloat(id);
        }
        return val;
    }

    @Override
    @SuppressLint("NewApi")
    public String getString(int id, Object... formatArgs)
            throws NotFoundException {
        Log.d(LOGTAG, "getString start");
        String val = "";
        try {
            val = super.getString(id, formatArgs);
        } catch (Exception e) {
            e.printStackTrace();
            val = mResources.getString(id, formatArgs);
        }
        return val;
    }

    @Override
    @SuppressLint("NewApi")
    public CharSequence getText(int id, CharSequence def) {
        Log.d(LOGTAG, "getText start");
        CharSequence text = "";
        try {
            text = super.getText(id, def);
        } catch (Exception e) {
            e.printStackTrace();
            text = mResources.getText(id, def);
        }
        return text;
    }

    @Override
    @SuppressLint("NewApi")
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        Log.d(LOGTAG, "getDimensionPixelOffset start");
        int offset = -1;
        try {
            offset = super.getDimensionPixelOffset(id);
        } catch (Exception e) {
            e.printStackTrace();
            offset = mResources.getDimensionPixelOffset(id);
        }
        return offset;
    }

    @Override
    @SuppressLint("NewApi")
    public Drawable getDrawableForDensity(int id, int density)
            throws NotFoundException {
        Log.d(LOGTAG, "getDrawableForDensity start 2");
        Drawable drawable;
        try {
            drawable = super.getDrawableForDensity(id, density);
        } catch (Exception e) {
            e.printStackTrace();
            drawable = mResources.getDrawableForDensity(id, density);
        }
        return drawable;
    }

    @Override
    @SuppressLint("NewApi")
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        Log.d(LOGTAG, "getDrawableForDensity start 3");
        Drawable drawable;
        try {
            drawable = super.getDrawableForDensity(id, density, theme);
        } catch (Exception e) {
            e.printStackTrace();
            drawable = mResources.getDrawableForDensity(id, density, theme);
        }
        return drawable;
    }

    @Override
    @SuppressLint("NewApi")
    public void getValueForDensity(int id, int density, TypedValue outValue,
            boolean resolveRefs) throws NotFoundException {
        Log.d(LOGTAG, "getValueForDensity start");
        try {
            super.getValueForDensity(id, density, outValue, resolveRefs);
        } catch (Exception e) {
            e.printStackTrace();
            mResources.getValueForDensity(id, density, outValue, resolveRefs);
        }
    }

    @Override
    @SuppressLint("NewApi")
    public ColorStateList getColorStateList(int id, Theme theme)
            throws NotFoundException {
        Log.d(LOGTAG, "getColorStateList start 2");
        int newId = getTemplateResourceById(id, "color");
        Log.d(LOGTAG, "getColorStateList 2 newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getColorStateList(newId, theme);
        }
        return mResources.getColorStateList(id, theme);
    }
    
    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
            throws NotFoundException {
        Log.d(LOGTAG, "getQuantityString start ...");
        String val;
        try {
            val = super.getQuantityString(id, quantity, formatArgs);
        } catch (Exception e) {
            e.printStackTrace();
            val = mResources.getQuantityString(id, quantity, formatArgs);
        }
        return val;
    }

    @Override
    public String getQuantityString(int id, int quantity)
            throws NotFoundException {
        Log.d(LOGTAG, "getQuantityString start");
        String val;
        try {
            val = super.getQuantityString(id, quantity);
        } catch (Exception e) {
            e.printStackTrace();
            val = mResources.getQuantityString(id, quantity);
        }
        return val;
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        Log.d(LOGTAG, "getBoolean start");
        int newId = getTemplateResourceById(id, "bool");
        Log.d(LOGTAG, "getBoolean newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getBoolean(newId);
        }

        return mResources.getBoolean(id);
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        Log.d(LOGTAG, "getColorStateList start");
        int newId = getTemplateResourceById(id, "color");
        Log.d(LOGTAG, "getColorStateList newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getColorStateList(newId);
        }
        return mResources.getColorStateList(id);
    }

    @Override
    public String getString(int id) throws NotFoundException {
        Log.d(LOGTAG, "getString start");
        int newId = getTemplateResourceById(id, "string");
        Log.d(LOGTAG, "getString newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getString(newId);
        }

        return mResources.getString(id);
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
        Log.d(LOGTAG, "getText id=" + id);
        return getString(id);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        Log.d(LOGTAG, "getDimension start");
        int newId = getTemplateResourceById(id, "dimen");

        if (newId != 0) {
            return super.getDimension(newId);
        }

        return mResources.getDimension(id);
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        Log.d(LOGTAG, "getDimensionPixelSize start");
        int newId = getTemplateResourceById(id, "dimen");
        Log.d(LOGTAG, "getDimensionPixelSize newId=" + newId + ";id=" + id);
        if (newId != 0) {
            return super.getDimensionPixelSize(newId);
        }

        return mResources.getDimensionPixelSize(id);
    }
    
    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        try {
            super.getValue(id, outValue, resolveRefs);
        } catch (Exception e) {
            Log.e(LOGTAG, "getValue try catch");
            mResources.getValue(id, outValue, resolveRefs);
            Log.e(LOGTAG, "getValue try catch end");
        }
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        Log.d(LOGTAG, "getValue start");
        String newName = getTemplateResourceByName(name);

        if (!TextUtils.isEmpty(newName)) {
            super.getValue(newName, outValue, resolveRefs);

            return;
        }

        mResources.getValue(name, outValue, resolveRefs);
    }

    public int getSuperIdentifier(String name, String defType, String defPackage) {
        return super.getIdentifier(name, defType, defPackage);
    }

    @Override
    public Resources getSuperResources() {
        return this;
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        Log.d(LOGTAG, "getResourceEntryName start resid="+resid);
        
        String resName = "";
        try {
            resName = super.getResourceEntryName(resid);
        } catch (Exception e) {
            e.printStackTrace();
            resName = mResources.getResourceEntryName(resid);
        }
        
        return resName;
    }
    
    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        TypedArray a = null;
        Log.d(LOGTAG, "obtainTypedArray start id="+id);
        try {
            a = super.obtainTypedArray(id);
        } catch (Exception e) {
            e.printStackTrace();
            a = mResources.obtainTypedArray(id);
        }
        
        return a;
    }
    
    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        TypedArray a = null;
        Log.d(LOGTAG, "obtainAttributes start");
        try {
            a = super.obtainAttributes(set, attrs);
        } catch (Exception e) {
            e.printStackTrace();
            a = mResources.obtainAttributes(set, attrs);
        }
        
        return a;
    }
    
    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        Log.d(LOGTAG, "getResourceTypeName start resid="+resid);
        String resName = "";
        try {
            resName = super.getResourceTypeName(resid);
        } catch (Exception e) {
            e.printStackTrace();
            resName = mResources.getResourceTypeName(resid);
        }
        
        return resName;
    }
    
    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        Log.d(LOGTAG, "getResourcePackageName start resid="+resid);
        
        String resName = "";
        try {
            resName = super.getResourcePackageName(resid);
        } catch (Exception e) {
            e.printStackTrace();
            resName = mResources.getResourcePackageName(resid);
        }
        
        return resName;
    }
    
    @Override
    public String getResourceName(int resid) throws NotFoundException {
        Log.d(LOGTAG, "getResourceName start resid="+resid);
        String resName = "";
        try {
            resName = super.getResourceName(resid);
        } catch (Exception e) {
            e.printStackTrace();
            resName = mResources.getResourceName(resid);
        }
        
        return resName;
    }
    
    
    
//    public TypedArray obtainStyledAttributes(int resid, int[] attrs)
//            throws NotFoundException {
//        return
//    }
    
    private int noJudgeGetResourceById(int id, String type)
            throws NotFoundException {
        String resourceName = mResources.getResourceEntryName(id);

        int resourceId = 0;
        String defPackageName = CyeeThemeManager.getInstance(mCxt)
                .getCyeeThemeApkPackageName(mCxt);

        try {
            resourceId = getSuperIdentifier(resourceName, type, defPackageName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resourceId;
    }

    private int getTemplateResourceById(int id, String type)
            throws NotFoundException {
        Log.d(LOGTAG, "getTemplateResourceById id=" + id + ";type=" + type);
        String resourceName = mResources.getResourceEntryName(id);
        Log.d(LOGTAG, "getTemplateResourceById resourceName=" + resourceName);

        int resourceId = id;
        if (CyeeThemeManager.getInstance(mCxt).existResInTheme(mCxt, this,
                resourceName)) {
            String defPackageName = CyeeThemeManager.getInstance(mCxt)
                    .getCyeeThemeApkPackageName(mCxt);
            resourceId = getSuperIdentifier(resourceName, type, defPackageName);

            return resourceId;
        }

        return 0;
    }

    private String getTemplateResourceByName(String resName)
            throws NotFoundException {
        String newName = "";

        if (CyeeThemeManager.getInstance(mCxt).existResInTheme(mCxt, this,
                resName)) {
            return resName;
        }

        return newName;
    }
}
