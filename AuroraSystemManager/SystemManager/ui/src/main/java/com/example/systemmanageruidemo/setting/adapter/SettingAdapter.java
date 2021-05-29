package com.example.systemmanageruidemo.setting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.setting.ItemTypeDef;
import com.example.systemmanageruidemo.setting.bean.BaseItem;
import com.example.systemmanageruidemo.setting.bean.BtnItem;
import com.example.systemmanageruidemo.setting.bean.SetItem;
import com.example.systemmanageruidemo.setting.bean.SwitchItem;
import com.example.systemmanageruidemo.setting.bean.TBtnItem;
import com.example.systemmanageruidemo.setting.bean.TSwitchItem;


import java.util.ArrayList;
import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.BaseVH> {
    private Context mContext;
    private List<BaseItem> items = new ArrayList<>();

    public SettingAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public List<BaseItem> getItems() {
        return items;
    }

    public void setItems(List<BaseItem> items) {
        this.items = items;
    }



    @NonNull
    @Override
    public BaseVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(viewType,parent,false);
        if (viewType == R.layout.setting_item1){
            return new SetVH(view,ItemTypeDef.Type.ITEM1);
        }else if (viewType == R.layout.setting_item2){
            return new BtnVH(view,ItemTypeDef.Type.ITEM2);
        }else if (viewType == R.layout.setting_item3){
            return new SwitchVH(view,ItemTypeDef.Type.ITEM3);
        }else if (viewType == R.layout.setting_item4){
            return new TSwitchVH(view,ItemTypeDef.Type.ITEM4);
        }else {
            return new TBtnVH(view,ItemTypeDef.Type.ITEM5);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseVH holder, int position) {
        BaseItem baseItem = items.get(position);

        if (holder instanceof SetVH){
            bindSetVH((SetVH)holder,(SetItem)baseItem,position);
        }else if (holder instanceof BtnVH){
            bindBtnVH((BtnVH)holder,(BtnItem)baseItem,position);
        }else if (holder instanceof SwitchVH){
            bindSwitchVH((SwitchVH)holder,(SwitchItem)baseItem,position);
        }else if (holder instanceof TSwitchVH){
            bindTSwitchVH((TSwitchVH)holder,(TSwitchItem)baseItem,position);
        }else {
            bindTBtnVH((TBtnVH)holder,(TBtnItem)baseItem,position);
        }

    }


    public void bindSetVH(SetVH SetVH, SetItem setItem, int position){
        SetVH.title.setText(setItem.getTitle());
    }

    public void bindBtnVH(BtnVH BtnVH, BtnItem btnItem, int position){
        BtnVH.title.setText(btnItem.getTitle());
        BtnVH.mBtnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onclick(position);
                }
            }
        });
    }
    
    public void bindSwitchVH(SwitchVH SwitchVH, SwitchItem switchItem, int position){
        SwitchVH.title.setText(switchItem.getTitle());
        SwitchVH.aSwitch.setChecked(switchItem.getTrue());
        SwitchVH.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchItem.setTrue(false);
            }
        });
    }
    
    public void bindTSwitchVH(TSwitchVH tSwitchVH, TSwitchItem tSwitchItem, int position){
        tSwitchVH.title.setText(tSwitchItem.getTitle());
        tSwitchVH.desc.setText(tSwitchItem.getDesc());
        tSwitchVH.aSwitch.setChecked(tSwitchItem.getTrue());
        tSwitchVH.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tSwitchItem.setTrue(false);
            }
        });
    }

    public void bindTBtnVH(TBtnVH tBtnVH, TBtnItem tBtnItem, int position){
        tBtnVH.title.setText(tBtnItem.getTitle());
        tBtnVH.desc.setText(tBtnItem.getDesc());
        tBtnVH.mBtnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onclick(position);
                }
            }
        });
    }

    public interface OnItemClickListener{
        void switchOnclick(boolean isTrue, int position);
        void onclick(int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        int code = items.get(position).typeCode();
        if (code == 1){
            return R.layout.setting_item1;
        }else if (code == 2){
            return R.layout.setting_item2;
        }else if (code == 3){
            return R.layout.setting_item3;
        }else if (code == 4){
            return R.layout.setting_item4;
        }else {
            return R.layout.setting_item5;
        }
    }

    class SetVH extends BaseVH {
        private TextView title;


        public SetVH(@NonNull View itemView, ItemTypeDef.Type type) {
            super(itemView, type);
            title = (TextView) itemView.findViewById(R.id.item1_title);

        }
    }


    class BtnVH extends BaseVH {
        private TextView title;
        private TextView mBtnAct;

        public BtnVH(@NonNull View itemView, ItemTypeDef.Type type) {
            super(itemView, type);
            title = (TextView) itemView.findViewById(R.id.item2_title);
            mBtnAct = (TextView) itemView.findViewById(R.id.btn_act);
        }
    }

    class SwitchVH extends BaseVH {
        private TextView title;
        private Switch aSwitch;

        public SwitchVH(@NonNull View itemView, ItemTypeDef.Type type) {
            super(itemView, type);
            title = (TextView) itemView.findViewById(R.id.item3_title);
            aSwitch = (Switch) itemView.findViewById(R.id.switch_act);
        }
    }

    class TSwitchVH extends BaseVH {
        private TextView title;
        private TextView desc;
        private Switch aSwitch;

        public TSwitchVH(@NonNull View itemView, ItemTypeDef.Type type) {
            super(itemView, type);

            title = (TextView) itemView.findViewById(R.id.item4_title);
            desc = (TextView) itemView.findViewById(R.id.item4_desc);
            aSwitch = (Switch) itemView.findViewById(R.id.switch_act);

        }
    }

    class TBtnVH extends BaseVH {
        private TextView title;
        private TextView desc;
        private TextView mBtnAct;

        public TBtnVH(@NonNull View itemView, ItemTypeDef.Type type) {
            super(itemView, type);
            title = (TextView) itemView.findViewById(R.id.item5_title);
            desc = (TextView) itemView.findViewById(R.id.item5_desc);
            mBtnAct = (TextView) itemView.findViewById(R.id.btn_act);
        }
    }

    abstract class BaseVH extends RecyclerView.ViewHolder {
        private ItemTypeDef.Type type;
        private View root;

        public BaseVH(@NonNull View itemView, ItemTypeDef.Type type) {
            super(itemView);
            this.root = itemView;
            this.type = type;
        }

        public ItemTypeDef.Type getType(){ return type; }
    }
}
