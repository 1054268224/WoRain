package com.example.systemmanageruidemo.trafficmonitor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;

import java.util.List;

public class TraPagerAdater extends PagerAdapter {
    private Context context;
    private List<TraPagerBean.SIMBean> datas;

    public TraPagerAdater(Context context) {
        this.context = context;
    }


    public List<TraPagerBean.SIMBean> getDatas() {
        return datas;
    }

    public void setDatas(List<TraPagerBean.SIMBean> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = View.inflate(context, R.layout.traffic_viewpager, null);

        TextView flowSize = (TextView) view.findViewById(R.id.flow_size);
        TextView flowUnit = (TextView) view.findViewById(R.id.flow_unit);
        TextView surplusFlow = (TextView) view.findViewById(R.id.surplus_flow);
        TextView usedFlow = (TextView) view.findViewById(R.id.used_flow_today);
        TextView traPackage = (TextView) view.findViewById(R.id.total_flow);

        String[] re = UnitUtil.convertStorage4(datas.get(position).getSurplusFlow());
        flowSize.setText(re[0]);
        flowUnit.setText(re[1]);
        usedFlow.setText(UnitUtil.convertStorage(datas.get(position).getUsedFlow()));
        traPackage.setText(UnitUtil.convertStorage3(datas.get(position).getTraPack()));

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return datas.get(position).getName();
    }
}
