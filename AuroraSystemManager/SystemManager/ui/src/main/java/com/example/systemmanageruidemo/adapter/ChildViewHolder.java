package com.example.systemmanageruidemo.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.bean.DataBean;

import static com.example.systemmanageruidemo.RubbishCleanerMainActivity.calculate;

public class ChildViewHolder extends BaseViewHolder {

    private Context mContext;
    private View view;
    private TextView childLeftText;
    private TextView childRightText;
    private CheckBox childCheckBox;
    private ImageView childImage;

    RecyclerAdapter adapter;

    public ChildViewHolder(Context context, View itemView, RecyclerAdapter adapter) {
        super(itemView);
        mContext = context;
        view = itemView;
        this.adapter = adapter;
    }

    public void bindView(final DataBean.DataBeanChild dataBean, final int pos) {
        if (dataBean.isHide()) {
            itemView.findViewById(R.id.rub_view).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.rub_view).setVisibility(View.VISIBLE);
        }
        childLeftText = (TextView) view.findViewById(R.id.child_left_text);
        childRightText = (TextView) view.findViewById(R.id.child_right_text);
        childCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
        childImage = (ImageView) view.findViewById(R.id.soft_image);

        childLeftText.setText(dataBean.getChildLeftTxt());
        childRightText.setText(UnitUtil.convertStorage(dataBean.getChildRightTxt()));
        childCheckBox.setChecked(dataBean.getChildTrue());
        childImage.setImageDrawable(dataBean.getImageId());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataBean.getChildTrue()){
                    dataBean.setChildTrue(false);
                } else {
                    dataBean.setChildTrue(true);
                }
                calculate(dataBean.getParent());
                adapter.notifyDataSetChanged();
            }
        });

    }
}
