package cyee.widget;

import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import cyee.theme.global.GlobalThemeConfigConstants;
import cyee.theme.global.ICyeeResource;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

//weidong begin
public class CyeeEditModeView extends RelativeLayout {

    private final Context mCxt;
    private CyeeButton mLeftBtn, mRightBtn;
    private String mLeftBtnTxt, mRightBtnTxt;
    private EditModeClickListener mClickListener;
    private ColorStateList mTxtColor;
    private int mBackgroundColor;

    public CyeeEditModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCxt = context;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CyeeEditModeView);
        mLeftBtnTxt = a
                .getString(R.styleable.CyeeEditModeView_cyeeEditModeLeftBtnTxt);
        mRightBtnTxt = a
                .getString(R.styleable.CyeeEditModeView_cyeeEditModeRightBtnTxt);
        // Gionee <weidong> <2016-05-26> add for 13577 begin
        mTxtColor = getTitleTxtColor();
        int color = a.getColor(
                R.styleable.CyeeEditModeView_cyeeEditModeBtnTxtColor, -1);
        if (color != -1) {
            mTxtColor = (ColorStateList) getResources()
                    .getColorStateList(color);
        }
        // Gionee <weidong> <2016-05-26> add for 13577 end
        TypedArray bar = context.obtainStyledAttributes(null,
                R.styleable.CyeeActionBar, com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
        
        int bgcolor = getResources().getColor(android.R.color.darker_gray);
        try {
            bgcolor = bar.getColor(R.styleable.CyeeActionBar_cyeebackground,
                    bgcolor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mBackgroundColor = a.getColor(
                R.styleable.CyeeEditModeView_cyeeEditModeBackground, bgcolor);

        bar.recycle();
        a.recycle();

        initViews();
        initClickListener();
        // Gionee <weidong> <2016-05-04> add for CR01683201 begin
        setElevation(getResources().getDimension(com.cyee.internal.R.dimen.cyee_actionbar_elevation));
        // Gionee <weidong> <2016-05-04> add for CR01683201 end
        
        if (ChameleonColorManager.getInstance().getCyeeThemeType(mCxt) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Resources iCyeeRes = mCxt.getResources();
            Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_bg);
            setElevation(getResources().getDimension(com.cyee.internal.R.dimen.cyee_global_theme_actionbar_elevation));
            setBackground(bgDrawable);
            if(ChameleonColorManager.isNeedChangeColor(mContext)) {
                mTxtColor = ColorStateList.valueOf(ChameleonColorManager
                        .getContentColorPrimaryOnAppbar_T1());
            }
        } else if (ChameleonColorManager.isNeedChangeColor(context)) {
            mBackgroundColor = ChameleonColorManager.getAppbarColor_A1();
            mTxtColor = ColorStateList.valueOf(ChameleonColorManager
                    .getContentColorPrimaryOnAppbar_T1());
            setBackgroundColor(mBackgroundColor);
        }
        mLeftBtn.setTextColor(mTxtColor);
        mRightBtn.setTextColor(mTxtColor);
    }

    // Gionee <weidong> <2016-05-26> add for 13577 begin
    private ColorStateList getTitleTxtColor() {
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.CyeeActionBar, com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
        int titleStyleRes = a.getResourceId(R.styleable.CyeeActionBar_cyeetitleTextStyle, 0);
        a.recycle();

        TypedArray txtAppearance = getContext().obtainStyledAttributes(
                titleStyleRes, com.android.internal.R.styleable.TextAppearance);
        ColorStateList txtColor = null;
        if (null == txtAppearance) {
            txtColor = (ColorStateList) getResources().getColorStateList(com.cyee.internal.R.color.cyee_actionbar_title_color_dark);
        } else {
            txtColor = txtAppearance
                    .getColorStateList(com.android.internal.R.styleable.TextAppearance_textColor);
            txtAppearance.recycle();
        }

        return txtColor;
    }
    // Gionee <weidong> <2016-05-26> add for 13577 end

    private void initViews() {
        addLeftButton();
        addRightButton();
        setBackgroundColor(mBackgroundColor);
    }

    private void addRightButton() {
        LayoutInflater inflater = (LayoutInflater) mCxt
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout.LayoutParams rParams;
        mRightBtn = (CyeeButton) inflater.inflate(com.cyee.internal.R.layout.cyee_edit_mode_btn, this,
                false);
        if (TextUtils.isEmpty(mRightBtnTxt)) {
            mRightBtnTxt = mCxt.getResources().getString(
                    com.cyee.internal.R.string.cyee_edit_mode_rightbtn_txt);
        }
        rParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        rParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        mRightBtn.setText(mRightBtnTxt);
        if (null != mTxtColor) {
            mRightBtn.setTextColor(mTxtColor);
        }
        addView(mRightBtn, rParams);
    }

    private void addLeftButton() {
        LayoutInflater inflater = (LayoutInflater) mCxt
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLeftBtn = (CyeeButton) inflater.inflate(com.cyee.internal.R.layout.cyee_edit_mode_btn, this,
                false);
        if (TextUtils.isEmpty(mLeftBtnTxt)) {
            mLeftBtnTxt = mCxt.getResources().getString(
                    com.cyee.internal.R.string.cyee_edit_mode_leftbtn_txt);
        }
        RelativeLayout.LayoutParams lParams;
        lParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        lParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        mLeftBtn.setText(mLeftBtnTxt);
        if (null != mTxtColor) {
            mLeftBtn.setTextColor(mTxtColor);
        }
        addView(mLeftBtn, lParams);
    }

    private void initClickListener() {
        mLeftBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (null == mClickListener) {
                    return;
                }
                mClickListener.leftBtnClick();
            }
        });

        mRightBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (null == mClickListener) {
                    return;
                }
                mClickListener.rightBtnClick();
            }
        });
    }

    public void setEditModeBackgroud(int color) {
        setBackgroundColor(color);
    }

    public void setEditModeTextColor(int color) {
        mTxtColor = ColorStateList.valueOf(color);
        mLeftBtn.setTextColor(color);
        mRightBtn.setTextColor(color);
    }

    public void setEditModeTextColor(ColorStateList color) {
        if (null == color) {
            return;
        }
        mTxtColor = color;
        mLeftBtn.setTextColor(color);
        mRightBtn.setTextColor(color);
    }

    public void setEditModeBtnTxt(String leftbtntxt, String rightbtntxt) {
        mLeftBtn.setText(leftbtntxt);
        mRightBtn.setText(rightbtntxt);
    }

    public void setEditModeBtnClickListener(EditModeClickListener listener) {
        mClickListener = listener;
    }

    public interface EditModeClickListener {
        void leftBtnClick();

        void rightBtnClick();
    }
}
// weidong end