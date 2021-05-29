package cyee.changecolors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.cyee.utils.Log;

public class ChangeColorReceiver extends BroadcastReceiver{
	
	private boolean mRestart = true;
	private OnChangeColorListener mOnChangeColorListener;
    //Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
	private OnChangeColorListenerWithParams mOnChangeColorListenerWithParams;
    //Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end
	
	private static final String TAG = "Chameleon";
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "start -> ");
        if(mRestart){
        	restartApplication(context);
        } else {
            Log.d(TAG, "Restart Activitys");
        	ChameleonColorManager.getInstance().init();
        	
        	if(!ChameleonColorManager.getInstance().isNeedChangeColor(context)){
        	    ChameleonColorManager.getInstance().clearDrawableCaches(context); 		
        	}

        	if(mOnChangeColorListener != null){
        		mOnChangeColorListener.onChangeColor();
        	}
        	
            //Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
        	if(mOnChangeColorListenerWithParams != null && intent != null){
        		int changeColorType = intent.getIntExtra(ColorConfigConstants.CHANGE_COLOR_TYPE,
        				ColorConfigConstants.TYPE_DEFAULT_CHANGE_COLOR);
        		mOnChangeColorListenerWithParams.onChangeColor(changeColorType);
        	}
            //Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end
        }    
    }

    /**
     * 设置变色的回掉接口
     * @param changeColorListener 变色回调接口的实现
     */
    public void setOnChangeColorListener(OnChangeColorListener changeColorListener){
    	mOnChangeColorListener = changeColorListener;
    }
    
    //Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
    /**
     * 设置变色的回掉接口
     * @param changeColorListener 变色回调接口的实现
     */
    public void setOnChangeColorListenerWithParams(OnChangeColorListenerWithParams changeColorListenerWithParams){
    	mOnChangeColorListenerWithParams = changeColorListenerWithParams;
    }
    //Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end
    
    public void setRestart(boolean restart){
    	mRestart = restart;
    }
    
    private void restartApplication(Context context) {
        String packageName = context.getPackageName();
        Log.d(TAG, "Restart Application : " + packageName);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(context.getPackageName());
    }
}
