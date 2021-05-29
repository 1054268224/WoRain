/*
 * Copyright (C) 2013 Gionee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cyee.widget;

import java.util.HashSet;
import java.util.Set;

import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import com.cyee.utils.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cyee.internal.widget.MultiChoiceScrollListener;

public abstract class CyeeMultiChoiceAdapterHelperBase implements
        OnItemLongClickListener, OnItemClickListener {
    protected static final String TAG = "CyeeMultiChoiceAdapterHelperBase";
    private static final String BUNDLE_KEY = "mca_selection";
    private static final String BUNDLE_NEDD_ANIMATION = "mca_need_animation";
    private static final String BUNDLE_IN_ACTION_MODE_NOW = "in_action_mode_now";
    private static final String BUNDLE_IN_ACTION_MODE_PRE = "in_action_mode_pre";
    private static final String BUNDLE_MOVED = "moved";
    
    private static final int MOVEX = 48; // dp
    private static final int MOVE_TIME = 250; // ms
    private final Set<Long> mCheckedItems = new HashSet<Long>();
    protected AdapterView<? super CyeeMultiChoiceBaseAdapter> mAdapterView;
    protected BaseAdapter mOwner;
    private OnItemClickListener mItemClickListener;
    private CyeeTextView mTitleView;
    private CyeeButton mSelectAllBtn;

    protected boolean mIsInActionModeNow = false;
    protected boolean mIsInActionModePre = false;
    protected boolean mIsMoved = false;
    private boolean mNeedNotify = true;
	
    protected CyeeMultiChoiceAdapterHelperBase(BaseAdapter owner) {
        this.mOwner = owner;
    }

    public void restoreSelectionFromSavedInstanceState(Bundle savedInstanceState) {
//        if (savedInstanceState == null) {
//            return;
//        }
//        long[] array = savedInstanceState.getLongArray(BUNDLE_KEY);
//        mCheckedItems.clear();
//        if (array != null) {
//            for (long id : array) {
//                mCheckedItems.add(id);
//            }
//        }
//        mIsInActionModeNow = savedInstanceState
//                .getBoolean(BUNDLE_IN_ACTION_MODE_NOW, false);
//        mIsInActionModePre = savedInstanceState
//                .getBoolean(BUNDLE_IN_ACTION_MODE_PRE, false);
//        mIsMoved = savedInstanceState
//                .getBoolean(BUNDLE_MOVED, false);
    }

    public void setAdapterView(AdapterView<? super BaseAdapter> adapterView) {
        this.mAdapterView = adapterView;
        checkActivity();
        adapterView.setOnItemLongClickListener(this);
        adapterView.setOnItemClickListener(this);
        adapterView.setAdapter(mOwner);
        
        if (mAdapterView instanceof CyeeListView) {
            ((CyeeListView) adapterView)
                    .setMultiChoiceScrollListener((MultiChoiceScrollListener) mOwner);
        } else if (mAdapterView instanceof CyeeGridView) {
            ((CyeeGridView) adapterView)
                    .setMultiChoiceScrollListener((MultiChoiceScrollListener) mOwner);
        }
        // Gionee <weidong> <2017-07-20> add for 157559 begin
        if (mIsInActionModeNow) {
            // Gionee <weidong> <2017-07-20> add for 157559 end
            View view = getActionModeCustomView();
            startActionMode(view);
            updateActionModeCustomView();
        }
    }

    private ColorStateList getTitleTxtColor() {
        TypedArray a = getContext().obtainStyledAttributes(null,R.styleable.CyeeActionBar,com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
        int titleStyleRes = a.getResourceId(R.styleable.CyeeActionBar_cyeetitleTextStyle, 0);
        a.recycle();

        TypedArray txtAppearance = getContext().obtainStyledAttributes(titleStyleRes,
                com.android.internal.R.styleable.TextAppearance);
        ColorStateList txtColor = txtAppearance
                .getColorStateList(com.android.internal.R.styleable.TextAppearance_textColor);
        txtAppearance.recycle();
        
        return txtColor;
    }
        
    private View getActionModeCustomView() {
        View view;
        int defaultActionModeLayout = com.cyee.internal.R.layout.cyee_multichoice_select_action_mode_layout;
        view = LayoutInflater.from(getContext()).inflate(
                defaultActionModeLayout, null);
        mTitleView = (CyeeTextView) view.findViewById(com.cyee.internal.R.id.cyee_multichoice_selectedCount);
        
        mSelectAllBtn = (CyeeButton) view
                .findViewById(com.cyee.internal.R.id.cyee_multichoice_selectall);
        
        ColorStateList color = getTitleTxtColor();
        mTitleView.setTextColor(color);
        mSelectAllBtn.setTextColor(color);
        
        if (ChameleonColorManager.isNeedChangeColor(getContext())) {
            mSelectAllBtn.setTextColor(ChameleonColorManager
                    .getContentColorPrimaryOnAppbar_T1());
            mTitleView.setTextColor(ChameleonColorManager
                    .getContentColorPrimaryOnAppbar_T1());
        }
        mSelectAllBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                doClickSelectAllBtn();
            }
        });

        return view;
    }

    private void doClickSelectAllBtn() {
        int selCnt = getCheckedItemCount();
        int totalCnt = mOwner.getCount();
        int checkableCnt = getCheckableItemCount();
        int correctedPosition = 0;
        boolean wasSelected = false;
        Log.d(TAG,"doClickSelectAllBtn selCnt="+selCnt+";checkableCnt="+checkableCnt);
        
        if (selCnt == checkableCnt) {
            for (int i = 0; i < totalCnt; i++) {
                correctedPosition = correctPositionAccountingForHeader(
                        mAdapterView, i);
                wasSelected = isChecked(correctedPosition);
                if (wasSelected) {
                    mCheckedItems.remove((long) correctedPosition);
                }
            }
        } else {
            boolean isCheckable = true;
            CyeeMultiChoiceAdapter adapter = (CyeeMultiChoiceAdapter) mOwner;
            for (int i = 0; i < totalCnt; i++) {
                correctedPosition = correctPositionAccountingForHeader(
                        mAdapterView, i);
                wasSelected = isChecked(correctedPosition);
                isCheckable = adapter.isItemCheckable(correctedPosition);
                if (!wasSelected && isCheckable) {
                    mCheckedItems.add((long) correctedPosition);
                }
            }
        }
        mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    private void updateActionModeCustomView() {
        updateActionModeCustomViewWithoutMenu();
        updateActionMode();
    }

    private void updateActionModeCustomViewWithoutMenu() {
        if (null == mTitleView || null == mSelectAllBtn) {
            return;
        }
        int count = getCheckedItemCount();
        int checkableCnt = getCheckableItemCount();
        mTitleView.setText(getContext().getResources().getString(
                com.cyee.internal.R.string.cyee_multichoice_select_text, count));
        if (count == checkableCnt) {
            mSelectAllBtn.setText(getContext().getResources().getString(
                    com.cyee.internal.R.string.cyee_multichoice_cancel_select_all));
        } else {
            mSelectAllBtn.setText(getContext().getResources().getString(
                    com.cyee.internal.R.string.cyee_multichoice_select_all));
        }
    }

    public void checkActivity() {
        Context context = getContext();
        if (context instanceof ListActivity) {
            throw new RuntimeException(
                    "ListView cannot belong to an activity which subclasses ListActivity");
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void save(Bundle outState) {
        long[] array = new long[mCheckedItems.size()];
        int i = 0;
        for (Long id : mCheckedItems) {
            array[i++] = id;
        }
        outState.putLongArray(BUNDLE_KEY, array);
        outState.putBoolean(BUNDLE_IN_ACTION_MODE_NOW, mIsInActionModeNow);
        outState.putBoolean(BUNDLE_NEDD_ANIMATION, mIsInActionModePre);
        outState.putBoolean(BUNDLE_MOVED, mIsMoved);
    }

    public void setItemChecked(long handle, boolean checked) {
        if (checked) {
            checkItem(handle);
        } else {
            uncheckItem(handle);
        }
    }

    /**
     * 更新lsitview的当前条目
     * @param handle
     * @param checked
     */
    public void setItemCheckedWithUpdate(long handle, boolean checked) {
        int pos = (int) handle;
        if (!((CyeeMultiChoiceAdapter) mOwner).isItemCheckable(pos)) {
            return;
        }
        if (checked) {
            checkItem(handle, true);
        } else {
            uncheckItem(handle, true);
        }
    }

    public void checkItem(long handle, boolean updateOne) {
        boolean wasSelected = isChecked(handle);
        Log.d(TAG, "checkItem wasSelected = "+wasSelected+";isActionModeStarted()="+isActionModeStarted());
        if (wasSelected) {
            return;
        }
        if (!isActionModeStarted()) {
            View view = getActionModeCustomView();
            startActionMode(view);
        }
        mCheckedItems.add((long) handle);
        if (updateOne) {
            updateOneItem(handle, true);
            updateActionModeCustomViewWithoutMenu();
        } else {
            mOwner.notifyDataSetChanged();
            updateActionModeCustomView();
        }
    }

    public void checkItem(long handle) {
        checkItem(handle, false);
    }

    public void uncheckItem(long handle, boolean updateOne) {
        boolean wasSelected = isChecked(handle);
        if (!wasSelected) {
            return;
        }
        mCheckedItems.remove(handle);
        if (updateOne) {
            updateOneItem(handle, false);
            updateActionModeCustomViewWithoutMenu();
        } else {
            mOwner.notifyDataSetChanged();
            updateActionModeCustomView();
        }
    }

    public void updateOneItem(long handle, boolean checked) {
        int pos = (int) handle;
        int visFirstPos = mAdapterView.getFirstVisiblePosition();
        int visEndPos = mAdapterView.getLastVisiblePosition();

        if (pos >= visFirstPos && pos <= visEndPos) {
            View view = mAdapterView.getChildAt(pos - visFirstPos);
            CyeeCheckBox holder = (CyeeCheckBox) view
                    .getTag(com.cyee.internal.R.id.cyee_checkbox_key_tag);
            if (null != holder) {
                holder.setChecked(checked);
            }
        }
    }

    public void uncheckItem(long handle) {
        uncheckItem(handle, false);
    }

    public Set<Long> getCheckedItems() {
        // Return a copy to prevent concurrent modification problems
        return new HashSet<Long>(mCheckedItems);
    }

    public int getCheckedItemCount() {
        return mCheckedItems.size();
    }

    public boolean isChecked(long handle) {
        return mCheckedItems.contains(handle);
    }

    public Context getContext() {
        return this.mAdapterView.getContext();
    }

    protected abstract void setActionModeTitle(String title);

    protected abstract boolean isActionModeStarted();

    protected abstract void startActionMode(View customView);

    protected abstract void finishActionMode();

    protected abstract void clearActionMode();

    protected abstract void updateActionMode();

    //
    // OnItemLongClickListener implementation
    //

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        Log.d(TAG, "onItemLongClick isActionModeStarted = "+isActionModeStarted());
        if (isActionModeStarted()) {
            return false;
        }
        return doItemLongClick(adapterView, view, position, id);
    }

    private boolean doItemLongClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        CyeeMultiChoiceAdapter adapter = (CyeeMultiChoiceAdapter) mOwner;
        boolean isCheckable = adapter.isItemCheckable(position);
        Log.d(TAG, "onItemLongClick isCheckable = "+isCheckable+";position="+position);
        if (!isCheckable) {
            return false;
        }
        int correctedPosition = correctPositionAccountingForHeader(adapterView,
                position);
        long handle = positionToSelectionHandle(correctedPosition);
        boolean wasChecked = isChecked(handle);
        Log.d(TAG, "onItemLongClick wasChecked = "+wasChecked+";handle="+handle);
        setItemChecked(handle, !wasChecked);
        return true;
    }

    private int correctPositionAccountingForHeader(AdapterView<?> adapterView,
            int position) {
        ListView listView = (adapterView instanceof ListView) ? (ListView) adapterView
                : null;
        int headersCount = listView == null ? 0 : listView
                .getHeaderViewsCount();
        if (headersCount > 0) {
            position -= listView.getHeaderViewsCount();
        }
        return position;
    }

    protected long positionToSelectionHandle(int position) {
        return position;
    }

    //
    // ActionMode.Callback related methods
    //

    public void onDestroyActionMode() {
        mCheckedItems.clear();
        clearActionMode();
        mIsInActionModePre = mIsInActionModeNow;
        mIsInActionModeNow = false;
        mIsMoved = false;
        mNeedNotify = true;
        mOwner.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        Log.d(TAG, "onItemClick isActionModeStarted = "+isActionModeStarted());
        if (isActionModeStarted()) {
            doItemLongClick(adapterView, view, position, id);
            return;
        }
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(adapterView, view, position, id);
        }
    }

    public View getView(int position, View viewWithoutSelection, int mode) {
        ViewGroup root = (ViewGroup) viewWithoutSelection;
        CyeeCheckBox checkboxView = (CyeeCheckBox) root
                .findViewById(android.R.id.checkbox);

        viewWithoutSelection.setTag(com.cyee.internal.R.id.cyee_checkbox_key_tag, checkboxView);

        long handle = positionToSelectionHandle(position);
        checkboxView.setChecked(isChecked(handle));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) checkboxView
                .getLayoutParams();
        if (mode == CyeeMultiChoiceAdapter.MODE_GRID_VIEW) {

            int frame = com.cyee.internal.R.id.cyee_customView;
            params.addRule(RelativeLayout.ALIGN_END, frame);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.CENTER_VERTICAL);
            checkboxView.setPadding(0, 0, 0, 0);
        }
        CyeeMultiChoiceAdapter adapter = (CyeeMultiChoiceAdapter) mOwner;
        if (mIsInActionModeNow) {
            if (adapter.isItemCheckable((int) handle)) {
                displayMultichoiceView(position, checkboxView, View.VISIBLE, false);
            } else {
                displayMultichoiceView(position, checkboxView, View.GONE, false);
            }
        } else {
            if (/*mIsInActionModePre && !mIsMoved && */adapter.isItemCheckable((int) handle)) {
                displayMultichoiceView(position, checkboxView, View.GONE, false);
            } else {
                displayMultichoiceView(position, checkboxView, View.GONE, false);
            }
        }
        return viewWithoutSelection;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (null == e1 || null == e2) {
            return false;
        }
        if (Math.abs(e1.getY() - e2.getY()) > 0) {
            mIsMoved = true;
        }
        return false;
    }

    private void displayMultichoiceView(final int position, final View view, final int visible,
            boolean needAnimal) {
        if (null == view) {
            return;
        }

        if (!needAnimal) {
            view.setVisibility(visible);
            if (visible == View.GONE) {
                view.setVisibility(visible);
                if (mNeedNotify) {
                    mNeedNotify = false;
                }
            }
            return;
        }

        ObjectAnimator translateAnimator = null;
        int moveX = dip2px(getContext(), MOVEX);
        if (visible == View.VISIBLE) {
            translateAnimator = ObjectAnimator.ofFloat(view, "translationX", moveX, 0);
            view.setVisibility(visible);
        } else {
            translateAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, moveX);
        }
        translateAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // TODO Auto-generated method stub
                if (visible == View.GONE) {
                    view.setVisibility(visible);
                    view.setTranslationX(0);
                    if (mNeedNotify) {
                        mNeedNotify = false;
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                // TODO Auto-generated method stub

            }
        });
        if (null != translateAnimator) {
            translateAnimator.setDuration(MOVE_TIME);
            translateAnimator.setInterpolator(new DecelerateInterpolator());
            translateAnimator.start();
        }
    }

    public View addMultichoiceView(View view, int mode) {
        if (null == view) {
            return null;
        }

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int containerId = com.cyee.internal.R.layout.cyee_multichoice_container;
        RelativeLayout multichoiceContainer = (RelativeLayout) inflater
                .inflate(containerId, null);
        FrameLayout frame = (FrameLayout) multichoiceContainer
                .findViewById(com.cyee.internal.R.id.cyee_customView);
        if (mode == CyeeMultiChoiceAdapter.MODE_GRID_VIEW) {
            multichoiceContainer.setGravity(Gravity.CENTER);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frame
                    .getLayoutParams();
            params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            params.removeRule(RelativeLayout.START_OF);
        }
        frame.addView(view);
        return multichoiceContainer;
    }

    public void enterMultiChoiceMode() {
        if (!isActionModeStarted()) {
            View view = getActionModeCustomView();
            startActionMode(view);
        }
        mAdapterView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mOwner.notifyDataSetChanged();
                updateActionModeCustomView();
            }
        }, 75);
    }

    private int getCheckableItemCount() {
        int cnt = 0;
        CyeeMultiChoiceAdapter adapter = (CyeeMultiChoiceAdapter) mOwner;
        int itemCnt = mOwner.getCount();
        boolean isCheckable = false;

        for (int i = 0; i < itemCnt; i++) {
            int correctedPosition = correctPositionAccountingForHeader(
                    mAdapterView, i);
            isCheckable = adapter.isItemCheckable(correctedPosition);
            if (isCheckable) {
                cnt++;
            }
        }

        return cnt;
    }

    
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
