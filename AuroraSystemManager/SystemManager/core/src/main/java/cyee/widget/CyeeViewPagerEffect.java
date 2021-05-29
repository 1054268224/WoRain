package cyee.widget;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.cyee.utils.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * 
 File Description:
 * This is a effect class for {@link ListView} in {@link CyeeViewPager}.
 * If you want to implement the shift effect of ListView in ViewPager, you can use this class to help.
 * @author: Gionee-lihq
 * @see: 2013-10-16 Change List:
 */
public class CyeeViewPagerEffect {
    private final String TAG = "CyeeViewPagerEffect->";
    private final float DISTANCE = 33f;
    private final boolean DEBUG = true;
    private State mState;
    private int oldPage;
    private final HashMap<Integer, Object> mObject = new LinkedHashMap<Integer, Object>();
    
    public CyeeViewPagerEffect() {
        
    }

    private enum State {
        IDLE,
        GOING_LEFT,
        GOING_RIGHT
    }
    
    public void effect(int currentItem, int position, float positionOffset, int positionOffsetPixels) {
        if (DEBUG) {
            Log.d(TAG, "currentItem: " + currentItem);
            Log.d(TAG, "position: " + position);
            Log.d(TAG, "positionOffset: " + positionOffset);
            Log.d(TAG, "mObject size: " + mObject.size());
        }
        
        if (mState == State.IDLE && positionOffset > 0) {
            oldPage = currentItem;
            mState = position == oldPage ? State.GOING_LEFT : State.GOING_RIGHT;
        }
        
        boolean goingLeft = position == oldPage;               
        if (mState == State.GOING_LEFT && !goingLeft) {
            mState = State.GOING_RIGHT;
        } else if (mState == State.GOING_RIGHT && goingLeft) {
            mState = State.GOING_LEFT;
        }
        
        float effectOffset = positionOffset;
        if (mState == State.GOING_LEFT) {
            Log.d(TAG, "going left: ");
            if (effectOffset > 0.998f) {
                effectOffset = 1;
            }
        } else if (mState == State.GOING_RIGHT) {
            Log.d(TAG, "going right: ");
        }
        
        View left = findViewFromObject(position);
        View right = findViewFromObject(position + 1);
        
        effectLeft(left, effectOffset);
        effectRight(right, effectOffset);
        
        if (positionOffset == 0) {
            mState = State.IDLE;
            revert(right);
            revert(left);
        }
    }
    
    private void effectLeft(View left, float effectOffset) {
        if (left != null && left instanceof ListView) {
            ListView listView = ( ListView ) left;
            Log.d(TAG, "effectLeft listView.getChildCount()="+listView.getChildCount());
            for (int i = 0; i < listView.getChildCount(); i++) {
                View child = listView.getChildAt(i);
                float tran = 0;
                if (mState == State.GOING_RIGHT) {
                    tran = -effectOffset * i * i * DISTANCE;
                    child.setTranslationX(tran);
                }
            }
        } 
    }
    
    private void effectRight(View right, float effectOffset) {
        if (right != null && right instanceof ListView) {
            ListView listView = ( ListView ) right;
            Log.d(TAG, "effectRight listView.getChildCount()="+listView.getChildCount());
            for (int i = 0; i < listView.getChildCount(); i++) {
                View child = listView.getChildAt(i);
                float tran = 0;
                if (mState == State.GOING_LEFT) {
                    tran = (1-effectOffset) * i * i * DISTANCE;
                    child.setTranslationX(tran);
                }
            }
        }
    }
    
    private void revert(View view) {
        if (view != null && view instanceof ListView) {
            ListView listView = ( ListView ) view;
            Log.d(TAG, "revert listView.getChildCount()="+listView.getChildCount());
            for (int i = 0; i < listView.getChildCount(); i++) {
                View child = listView.getChildAt(i);
                child.setTranslationX(0.0f);
            }
        }
    }
    public void setObjectForPosition(Object obj, int position) {
        if (obj instanceof CyeeListView) {
            CyeeListView listView = ( CyeeListView ) obj;
            // Gionee <lihq> <2013-11-19> add for CR00873172 begin
            //add enable control.
            listView.setViewPagerEffectEnable(true);
            // Gionee <lihq> <2013-11-19> add for CR00873172 end
            if (listView.getDivider() != null) {
            	listView.setDivider(null);
            	listView.setDividerHeight(0);
            	listView.setModifiedDiveder(true);
            }
        }
        mObject.put(Integer.valueOf(position), obj);
    }
    
    public Object getObjectForPosition(int position) {
        return mObject.get(Integer.valueOf(position));
    }
    
    public View findViewFromObject(int position) {
        Object o = mObject.get(Integer.valueOf(position));
        return ( View )o;
    }
}
