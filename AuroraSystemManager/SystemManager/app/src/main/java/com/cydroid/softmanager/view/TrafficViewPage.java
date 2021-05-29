//Gionee: mengdw <2016-08-23> add log for CR01747432  begin
package com.cydroid.softmanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import com.cydroid.softmanager.utils.Log;

public class TrafficViewPage extends ViewPager {
    private static final String TAG = "TrafficViewPage";
    
    public TrafficViewPage(Context context) {
        super(context);
    }

    public TrafficViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ignored) {
            Log.d(TAG, "dispatchTouchEvent IllegalArgumentException ignored=" + ignored.toString());
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "dispatchTouchEvent ArrayIndexOutOfBoundsException e=" + e.toString());
        }
        return false;
    }
}
//Gionee: mengdw <2016-08-23> add log for CR01747432  end