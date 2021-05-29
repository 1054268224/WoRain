package com.cydroid.softmanager.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.cydroid.softmanager.R;

public class MiniTabFragment extends Fragment {
    private Button mLeftButton;
    private Button mRightButon;
    private int mClickedColor;
    private int mUnclickedColor;
    private boolean mIsLeft = true;
    private onClickListener mListener;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mini_tab_layout, container, false);
        mLeftButton = (Button) view.findViewById(R.id.mini_tab_left);
        mRightButon = (Button) view.findViewById(R.id.mini_tab_right);
        mLeftButton.setOnClickListener(new LeftButtonListener());
        mRightButon.setOnClickListener(new RightButtonListener());

        Resources res = getActivity().getResources();
        if (res != null) {
            mClickedColor = res.getColor(R.color.mini_tab_clicked);
            mUnclickedColor = res.getColor(R.color.mini_tab_unclicked);
        }

        initButtons();

        return view;
    }

    /*
     * @Override public void onStart() { super.onStart(); initButtons(); }
     */

    public void setLeftButtonText(CharSequence str) {
        mLeftButton.setText(str);
    }

    public void setRightButtonText(CharSequence str) {
        mRightButon.setText(str);
    }

    public void setLeftButtonText(int resid) {
        mLeftButton.setText(resid);
    }

    public void setRightButtonText(int resid) {
        mRightButon.setText(resid);
    }

    public void setButtonListener(onClickListener listener) {
        mListener = listener;
    }

    public boolean isLeftButton() {
        return mIsLeft;
    }

    private void initButtons() {
        setButtonAndTextColor(LEFT);
    }

    private void setButtonAndTextColor(int position) {
        switch (position) {
            case LEFT:
                mLeftButton.setBackgroundResource(R.drawable.permission_left_press);
                mLeftButton.setTextColor(mClickedColor);
                mRightButon.setBackgroundResource(R.drawable.permission_right_normal);
                mRightButon.setTextColor(mUnclickedColor);
                break;
            case RIGHT:
                mLeftButton.setBackgroundResource(R.drawable.permission_left_normal);
                mLeftButton.setTextColor(mUnclickedColor);
                mRightButon.setBackgroundResource(R.drawable.permission_right_press);
                mRightButon.setTextColor(mClickedColor);
                break;
            default:
                break;
        }
    }

    private class LeftButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mLeftButton != null && mRightButon != null) {
                mIsLeft = true;
                setButtonAndTextColor(LEFT);
                if (mListener != null) {
                    mListener.onClick(LEFT);
                }
            }
        }
    }

    private class RightButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mLeftButton != null && mRightButon != null) {
                mIsLeft = false;
                setButtonAndTextColor(RIGHT);
                if (mListener != null) {
                    mListener.onClick(RIGHT);
                }
            }
        }
    }

    public interface onClickListener {
        void onClick(int position);
    }

}
