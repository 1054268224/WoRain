package cyee.widget;

import java.util.ArrayList;
import java.util.List;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.os.Handler;
import android.os.Message;
import com.cyee.utils.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
/**
 * 
 File Description:
 * This class is used by {@link CyeeListView}, to help {@link CyeeListView} to 
 * implements the items stretch when over scroll top or bottom.
 * @author: Gionee-lihq
 * @see: 2013-10-31 Change List:
 * 
 * 1. Create this file at 2013-10-31.
 * 2. Add for CR00873172
 * 3. Add new method {overAnimation} 2013-11-15
 */
public class CyeeStretchAnimationa {

    private final String TAG = "CyeeStretchAnimationa->";
    private boolean mRunning = false;
    private boolean mGoUp = true;
    private final List<ChildView> mChildren = new ArrayList<CyeeStretchAnimationa.ChildView>();
    private final int DURATION = 200; 
    private final Interpolator mInterpolator;
    
    private boolean mLastUpdate = false;
    
    private int mMotionY = -1;
    private final int DISTANCE = 40;
    
    // Gionee <lihq> <2013-11-18> add for CR00953002 begin
    private final List<Integer> mFromTopHeight = new ArrayList<Integer>();
    private final List<Integer> mFromBottomHeight = new ArrayList<Integer>();
	// Gionee <lihq> <2013-11-18> add for CR00953002 end
    public CyeeStretchAnimationa() {
        mInterpolator = new LinearInterpolator();
    }
    
    public void addChildren(List<View> children) {
    	if (children.size() <= 0) {
    		return;
    	}
    	if (mGoUp && children.size() > mFromTopHeight.size()) {
    		initOriginHeight(children, mFromTopHeight);
    	}
    	if (!mGoUp && children.size() > mFromBottomHeight.size()) {
    		initOriginHeight(children, mFromBottomHeight);
    	}
        mChildren.clear();
        int count = children.size();
        for (int i = 0; i < count; i++) {
            View child = children.get(i);
            // Gionee <lihq> <2013-11-18> add for CR00953002 begin
            int originHeight = mGoUp ? mFromTopHeight.get(i) : mFromBottomHeight.get(i);
            mChildren.add(new ChildView(child, i, count, originHeight));
            //mChildren.add(new ChildView(child, i, count));
			// Gionee <lihq> <2013-11-18> add for CR00953002 end
        }
    }
    // Gionee <lihq> <2013-11-18> add for CR00953002 begin
    private void initOriginHeight(List<View> children, List<Integer> target) {
    	int count = children.size();
    	int i = target.size();
        for (; i < count; i++) {
            View child = children.get(i);
            target.add(child.getLayoutParams().height);
        }
    }
	// Gionee <lihq> <2013-11-18> add for CR00953002 end
    
    public void overScroll(int motionY, int motionPostion, int deltaY) {
        // TODO handle over scroll, to change children view height.
        mRunning = true;
        if (Math.abs(motionY - mMotionY) > DISTANCE && motionPostion < mChildren.size()) {
            mMotionY = motionY;
            for (int i = 0; i < motionPostion; i++) {
                ChildView child = mChildren.get(i);
                if (child != null && child.mChild.getVisibility() != View.GONE) {
                    child.computeCurHeight(deltaY);
                    child.changeChildLayout();
                }
            }
        }
    }
    
	public void revertViewSize() {
		for (int i = 0; i < mChildren.size(); i++) {
			ChildView child = mChildren.get(i);
			child.mCurHeight = child.mRawHeight;
			child.revertLayoutParams();
		}
		mRunning = false;
		mLastUpdate = true;
		mChildren.clear();
	}
	
    public boolean isRunning() {
        return mRunning;
    }
    
    public boolean isGoUp() {
        return mGoUp;
    }
    
    public void setGoUp(boolean up) {
        mGoUp = up;
    }
    
    
    public boolean isLastUpdate() {
        boolean last = mLastUpdate;
        if (mLastUpdate) {
            mLastUpdate = false;
        }
        return last;
    }
    
    public class ChildView {
        View mChild;
        int mRawHeight;
        int mToHeight;
        int mCurHeight;
        int mDHeight;
        final int MULTIPLE = 10;
        // Gionee <lihq> <2013-11-18> add for CR00953002 begin
        int mOriginLPheight = 0;
        // Gionee <lihq> <2013-11-18> add for CR00953002 end
        public ChildView(View child, int index, int count, int originHeight) {
            mChild = child;
            // Gionee <lihq> <2013-11-18> add for CR00953002 begin
            mOriginLPheight = originHeight;
            Log.d(TAG,"mOriginLPheight->" + mOriginLPheight);
            // Gionee <lihq> <2013-11-18> add for CR00953002 end
            mRawHeight = mCurHeight = child.getMeasuredHeight();
            mToHeight = mRawHeight + mRawHeight * 3 / MULTIPLE - (mRawHeight * 3 / MULTIPLE * index / count);
            mDHeight = mToHeight - mRawHeight;
        }
        
        public void changeChildLayout() {
            if (mChild.getMeasuredHeight() != mCurHeight) {
                LayoutParams lp = mChild.getLayoutParams();
                lp.height = mCurHeight;
                mChild.setLayoutParams(lp);
            }
        }
        
        // Gionee <lihq> <2013-11-18> add for CR00953002 begin
        public void revertLayoutParams() {
        	LayoutParams lp = mChild.getLayoutParams();
        	lp.height = mOriginLPheight;
        	mChild.setLayoutParams(lp);
        }
        // Gionee <lihq> <2013-11-18> add for CR00953002 end
        
        public void computeCurHeight(int deltaY) {
            // TODO compute current height of child view by deltaY.
            if (mCurHeight <= mToHeight) {
                if (Math.abs(deltaY) > 100) {
                    mCurHeight += mDHeight / 4;
                } else {
                    mCurHeight += 4;
                }
                if (mCurHeight > mToHeight) {
                    mCurHeight = mToHeight;
                }
            }
        }
    }
    
    public void overAnimation(float increase, boolean autoOver) {
    	int count = 0;
    	if (autoOver) {
    		count = mChildren.size() / 2;
    	} else {
    		count = mChildren.size();
    	}
    	// Gionee <lihq> <2013-11-18> add for CR00953462 begin
    	if (count == 0) {
    		return;
    	}
    	// Gionee <lihq> <2013-11-18> add for CR00953462 end
    	Animator[] animators = new Animator[count];
    	for (int i = 0; i < animators.length; i++ ) {
    		ChildView view = mChildren.get(i);
    		if (autoOver) {
    			animators[i] = createAnimator(view, (int)(view.mRawHeight * increase));
    		} else {
    			animators[i] = createAnimator(view, (int)(view.mCurHeight));
    		}
    	}
    	AnimatorSet animatorSet = new AnimatorSet();
    	animatorSet.setDuration(DURATION);
    	animatorSet.playTogether(animators);
    	animatorSet.start();
    }
    
    private Animator createAnimator(final ChildView view, int startHeight) {
    	ValueAnimator animator = ValueAnimator.ofInt(startHeight, view.mRawHeight);
    	animator.setInterpolator(new LinearInterpolator());
    	animator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				mRunning = true;
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				view.mCurHeight = view.mRawHeight;
				// Gionee <lihq> <2013-11-18> modify for CR00953002 begin
				//view.changeChildLayout();
				view.revertLayoutParams();
				// Gionee <lihq> <2013-11-18> modify for CR00953002 end
				mRunning = false;
				mLastUpdate = true;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
    	animator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				view.mCurHeight = (Integer) animation.getAnimatedValue();
				view.changeChildLayout();
			}
		});
    	return animator;
    }
}
