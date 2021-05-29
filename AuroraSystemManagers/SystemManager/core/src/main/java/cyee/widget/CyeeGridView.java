package cyee.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.GridView;

import com.cyee.internal.widget.MultiChoiceScrollListener;

public class CyeeGridView extends GridView {

    private MultiChoiceScrollListener mScrollListener = null;
    private GestureDetector mGesture = null;
    
    public CyeeGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                // TODO Auto-generated method stub
                if (mScrollListener != null) {
                    mScrollListener.onScroll(e1, e2, distanceX, distanceY);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        };
        mGesture = new GestureDetector(mContext, gestureListener);
    }

    public void setMultiChoiceScrollListener(MultiChoiceScrollListener listener) {
        this.mScrollListener = listener;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGesture.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
}
