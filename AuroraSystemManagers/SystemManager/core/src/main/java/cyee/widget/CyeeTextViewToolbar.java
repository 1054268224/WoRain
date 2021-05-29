package cyee.widget;

import android.graphics.Color;
import android.text.Layout;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.cyee.internal.R;

abstract class CyeeTextViewToolbar extends CyeeViewToolbar {

    protected static final int ID_PASTE = android.R.id.paste;

    protected final int ID_PASTE_STR = com.cyee.internal.R.string.cyee_paste;

    protected TextView mItemPaste;

    private int mScreenX;
    private int mScreenY;
    private int mLineHeight;

    protected CyeeEditText mEditText;

    CyeeTextViewToolbar(CyeeEditText hostView) {
        super(hostView);
        this.mEditText = hostView;        
    }

    protected void initToolbarItem() {
        // init past view
        mItemPaste = initToolbarItem(ID_PASTE, ID_PASTE_STR);
    }

    void show() {
        if (!mShowing) {
            calculateScreenPosition();
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            showInternal(mScreenX, mScreenY, mLineHeight, start != end);
        }
    }

    void move() {
        if (mShowing) {
            calculateScreenPosition();
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            moveInternal(mScreenX, mScreenY, mLineHeight, start != end);
        }
    }

    private void calculateScreenPosition() {
        int[] location = new int[2];
        mEditText.getLocationOnScreen(location);
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();
        Layout layout = mEditText.getLayout();
        if (layout == null ) {
        	// mEditText.assumeLayout();
        	// layout = mEditText.getLayout();
        	return;
        }
        int line = layout.getLineForOffset(start);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineBottom(line);
        mLineHeight = bottom - top;
        mScreenY = top + mLineHeight / 2 + location[1] + mEditText.getTotalPaddingTop() - mEditText.getScrollY();
        if (start == end) {
            mScreenX = Math.round(layout.getPrimaryHorizontal(start)) + location[0] + mEditText.getTotalPaddingLeft() - mEditText.getScrollX();
        } else {
            int left = Math.round(layout.getPrimaryHorizontal(start));
            int right;
            int lineEnd = layout.getLineForOffset(end);
            if (line == lineEnd) {
                right = Math.round(layout.getPrimaryHorizontal(end));
            } else {
                right = Math.round(layout.getLineRight(line));
            }
            mScreenX = (left + right) / 2 + location[0] + mEditText.getTotalPaddingLeft() - mEditText.getScrollX();
        }
        mScreenY = Math.max(location[1], mScreenY);
    }

    protected TextView initToolbarItem(int id, int textResId) {
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        // Gionee zhangxx 2013-03-29 modify for CR00791013 begin
        // textView.setTextAppearance(mContext, R.style.TextAppearance_GioneeView_MediumSecond);
        textView.setTextSize(16);
        textView.setTextColor(mContext.getResources().getColor(com.cyee.internal.R.color.cyee_editor_toolbar_text_color));
        // Gionee zhangxx 2013-03-29 modify for CR00791013 end
        textView.setId(id);
        textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        textView.setOnClickListener(getOnClickListener());
        return textView;
    }

    protected abstract OnClickListener getOnClickListener();

}
