package com.example.systemmanageruidemo.adapter;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.bean.DataBean;

public class ParentViewHolder extends BaseViewHolder {

    private Context mContext;
    private View view;
    private RelativeLayout containerLayout;
    private TextView parentLeftView;
    private TextView parentRightView;
    private CheckBox parentCheckBox;
    private ImageView expand;
    private View parentDashedView;
    private TextView bootTitle;

    public ParentViewHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;
        view = itemView;
        containerLayout = (RelativeLayout) view.findViewById(R.id.container);
        parentLeftView = (TextView) view.findViewById(R.id.parent_left_text);
        parentRightView = (TextView) view.findViewById(R.id.parent_right_text);
        parentCheckBox = (CheckBox) view.findViewById(R.id.checkbox);

        expand = (ImageView) view.findViewById(R.id.expand);
        parentDashedView = view.findViewById(R.id.dash_view);
    }

    public void bindView(final DataBean dataBean, final int pos, final ItemClickListener listener) {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) expand.getLayoutParams();
        expand.setLayoutParams(params);
        parentLeftView.setText(dataBean.getParentLeftTxt());
        parentRightView.setText(UnitUtil.convertStorage(dataBean.getParentRightTxt()));
        parentCheckBox.setOnCheckedChangeListener(null);
        parentCheckBox.setChecked(dataBean.getParentTrue());
        if (dataBean.isExpand()) {
            expand.setRotation(90);
        } else {
            expand.setRotation(0);
        }

        parentCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataBean.setParentTrue(isChecked);
                if (listener != null) {
                    listener.onCheckChange(dataBean, isChecked);
                }
            }
        });
        containerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                assert (view == containerLayout);
                if (listener != null) {
                    if (dataBean.isExpand()) {
                        listener.onHideChildren(dataBean);
                        dataBean.setExpand(false);
                        rotationExpandIcon(90, 0);
                    } else {
                        listener.onExpandChildren(dataBean);
                        dataBean.setExpand(true);
                        rotationExpandIcon(0, 90);
                    }
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void rotationExpandIcon(float from, float to) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
            valueAnimator.setDuration(500);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    expand.setRotation((Float) valueAnimator.getAnimatedValue());
                }
            });
            valueAnimator.start();
        }
    }

    public abstract class A implements CompoundButton.OnCheckedChangeListener {
        DataBean dataBean;
        int pos;
        ItemClickListener listener;

        public DataBean getDataBean() {
            return dataBean;
        }

        public void setDataBean(DataBean dataBean) {
            this.dataBean = dataBean;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public ItemClickListener getListener() {
            return listener;
        }

        public void setListener(ItemClickListener listener) {
            this.listener = listener;
        }
    }
}
