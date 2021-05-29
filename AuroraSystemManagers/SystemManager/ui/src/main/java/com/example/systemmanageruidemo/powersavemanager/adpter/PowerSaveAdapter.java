package com.example.systemmanageruidemo.powersavemanager.adpter;

import android.content.Context;
import android.telecom.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.systemmanageruidemo.R;
import java.util.ArrayList;
import java.util.List;


public class PowerSaveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<PowerItem.Mode> listA = new ArrayList<>();
    List<PowerItem.Detail> listB = new ArrayList<>();

    private Context context;

    public PowerSaveAdapter(Context context) {
        this.context = context;
    }

    public List<PowerItem.Mode> getListA() {
        return listA;
    }

    public void setListA(List<PowerItem.Mode> listA) {
        this.listA = listA;
    }

    public List<PowerItem.Detail> getListB() {
        return listB;
    }

    public void setListB(List<PowerItem.Detail> listB) {
        this.listB = listB;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(viewType, parent, false);

        if (viewType == R.layout.blank) {
            return new NullHolder(view);
        } else if (viewType == R.layout.powersave_mode_item) {
            return new ModeViewHolder(view);
        } else {
            return new DetailViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NullHolder) {
            bindViewHolderNULL((NullHolder) holder);
        } else if (holder instanceof ModeViewHolder) {
            bindViewHolderA(((ModeViewHolder) holder), position);
        } else {
            bindViewHolderB(((DetailViewHolder) holder), position);
        }
    }

    private void bindViewHolderNULL(NullHolder holder) {

    }

    private void bindViewHolderA(ModeViewHolder modeHolder, int position) {
        PowerItem.Mode mode = listA.get(position);
        modeHolder.modeTitle.setText(mode.getTitle());
        modeHolder.modeDesc.setText(mode.getDescription());
        modeHolder.detail.setText(mode.getDetail());
        modeHolder.aSwitch.setChecked(mode.getTrue());

        modeHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mListener != null) {
                    mListener.onSwitchClick(isChecked, position);
                    for (PowerItem.Mode mode1 : listA) {
                        mode1.setTrue(false);
                    }
                }
            }
        });
    }

    private void bindViewHolderB(DetailViewHolder dHolder, int position) {
        PowerItem.Detail detail = listB.get(position - listA.size());
        dHolder.title.setText(detail.getTitle());
        dHolder.desc.setText(detail.getDesc());
        dHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onClick(position);
                }

            }
        });
    }

    public interface OnItemClickListener {
        void onSwitchClick(boolean modeState, int position);
        void onClick(int position);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }


    @Override
    public int getItemCount() {
        return listA.size() + listB.size() > 0 ? listA.size() + listB.size() : 1;

    }

    @Override
    public int getItemViewType(int position) {
         if (listA.size() + listB.size() == 0) {
            return R.layout.blank;
        }
        return position - listA.size() >= 0 ? R.layout.powersave_detail_item : R.layout.powersave_mode_item;
    }

    class NullHolder extends  RecyclerView.ViewHolder {

        public NullHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ModeViewHolder extends RecyclerView.ViewHolder {
        private TextView modeTitle;
        private TextView modeDesc;
        private TextView detail;
        private Switch aSwitch;

        public ModeViewHolder(@NonNull View itemView) {
            super(itemView);
            modeTitle = (TextView) itemView.findViewById(R.id.power_mode_title);
            modeDesc = (TextView) itemView.findViewById(R.id.power_mode_desc);
            detail = (TextView) itemView.findViewById(R.id.power_mode_detail);
            aSwitch = (Switch) itemView.findViewById(R.id.switch_act);

        }
    }

    class DetailViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView desc;
        private TextView btn;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.power_title);
            desc = itemView.findViewById(R.id.power_desc);
            btn = itemView.findViewById(R.id.btn_act);
        }
    }
}
