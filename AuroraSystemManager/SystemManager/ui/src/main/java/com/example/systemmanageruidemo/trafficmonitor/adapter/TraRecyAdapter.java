package com.example.systemmanageruidemo.trafficmonitor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;

import java.util.List;

public class TraRecyAdapter extends RecyclerView.Adapter<TraRecyAdapter.VH> {
    private Context context;
    private List<TraRecyBean> datas;
    private CompoundButton.OnCheckedChangeListener listener;

    public TraRecyAdapter(Context context, CompoundButton.OnCheckedChangeListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public List<TraRecyBean> getDatas() {
        return datas;
    }

    public void setDatas(List<TraRecyBean> datas) {
        this.datas = datas;
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.traffic_item,parent,false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TraRecyBean data = datas.get(position);
        holder.imageView.setImageDrawable(data.getImageId());
        holder.name.setText(data.getName());

        if(data.isInvalidControlApp()){
            holder.aSwitch.setVisibility(View.GONE);
        }else {
            holder.aSwitch.setVisibility(View.VISIBLE);
        }
        holder.aSwitch.setOnCheckedChangeListener(null);
        holder.aSwitch.setChecked(data.isIslimit());
        holder.aSwitch.setTag(data);
        holder.aSwitch.setOnCheckedChangeListener(listener);

        holder.softFlow.setText(UnitUtil.convertStorage(data.getUsedTraSize()));
    }


    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    static class VH extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name;
        TextView softFlow;
        Switch aSwitch;


        public VH(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.soft_image);
            name = (TextView) itemView.findViewById(R.id.soft_name);
            softFlow = (TextView) itemView.findViewById(R.id.soft_flow);
            aSwitch = (Switch) itemView.findViewById(R.id.switch_act);
        }
    }
}
