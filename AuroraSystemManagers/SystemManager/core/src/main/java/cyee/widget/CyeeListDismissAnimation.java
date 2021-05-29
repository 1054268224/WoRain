package cyee.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import com.cyee.internal.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

/**
 * @Description Class for animation of ListView item dismiss.
 * @version 2013-11-29
 * @author Gionee-LIHQ
 * 
 */
public class CyeeListDismissAnimation {

    private AnimatorSet mAnimatorSet;
    private final AbsListView mListView;
    private final int DURATION = 200;
    private List<View> mViews;
    private OnCyeeItemDismissCallback mCallback;
    
    public CyeeListDismissAnimation(AbsListView listView) {
        mListView = listView;
    }
    
    public void setCyeeItemDismissCallback(OnCyeeItemDismissCallback callback) {
        mCallback = callback;
    }
    
    public void startAnimation(Collection<Integer> positions) {
        final List<Integer> positionsCopy = new ArrayList<Integer>(positions);
        if (mListView == null) {
            throw new IllegalStateException(
                    "Call setListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }

        mViews = getVisibleViewsForPositions(positionsCopy);

        if (!mViews.isEmpty()) {
            List<Animator> animators = new ArrayList<Animator>();
            boolean continu = false;
            int pos = -2;
            for (final View view : mViews) {
                int childPos = mListView.getPositionForView(view);
                continu = pos == (childPos - 1);
                pos = childPos;
                animators.add(createAnimatorForView(view, continu));
            }
            Animator[] animatorsArray = new Animator[animators.size()];
            for (int i = 0; i < animatorsArray.length; i++) {
                animatorsArray[i] = animators.get(i);
            }
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setDuration(DURATION);
            mAnimatorSet.playTogether(animatorsArray);
            mAnimatorSet.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator arg0) {
                }

                @Override
                public void onAnimationRepeat(Animator arg0) {
                }

                @Override
                public void onAnimationEnd(Animator arg0) {
                    if (mCallback != null) {
                        invokeCallback(positionsCopy);
                    }
                }

                @Override
                public void onAnimationCancel(Animator arg0) {
                }
            });
            mAnimatorSet.start();
        } else {
            if (mCallback != null) {
                invokeCallback(positionsCopy);
            }
        }
    }
    public void endAnimation() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }
    }
    private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < mListView.getChildCount(); i++) {
            View child = mListView.getChildAt(i);
            if (positions.contains(mListView.getPositionForView(child))) {
                views.add(child);
            }
        }
        return views;
    }
    
    private Animator createAnimatorForView(final View view, final boolean continu) {
        final LayoutParams lp = view.getLayoutParams();
        final int originalHeight = view.getHeight();

        final Drawable oriDrawable = view.getBackground();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // Gionee <gaoj> <2013-9-2> modify for CR00882129 begin
                if (continu) {
                    view.setBackgroundResource(com.cyee.internal.R.drawable.cyee_listview_delete_bg);
                } else {
                    view.setBackgroundResource(com.cyee.internal.R.drawable.cyee_listview_delete_top_bg);
                }
                /* view.setBackgroundColor(Color.parseColor("#a0a0a0")); */
                // Gionee <gaoj> <2013-9-2> modify for CR00882129 end
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                // Gionee <lihq> <2013-11-29> modify for CR00873172 begin
                // end animation, must revert the origin status, to keep the view attributes
                //lp.height = 0;
                lp.height = originalHeight;
                // Gionee <lihq> <2013-11-29> modify for CR00873172 end
                view.setLayoutParams(lp);
                view.setBackground(oriDrawable);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });

        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                view.setLayoutParams(lp);
            }
        });
        return animator;
    }
    
    private void invokeCallback(Collection<Integer> positions) {
        ArrayList<Integer> positionsList = new ArrayList<Integer>(positions);
        Collections.sort(positionsList);
        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = positionsList.get(positionsList.size() - 1 - i);
        }
        mCallback.onDismiss(mListView, dismissPositions);
    }
}
