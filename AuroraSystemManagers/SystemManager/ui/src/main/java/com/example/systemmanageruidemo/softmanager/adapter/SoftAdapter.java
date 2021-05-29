package com.example.systemmanageruidemo.softmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.R;

import java.util.List;

public class SoftAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SoftItem> mSoftItemList;

    public List<SoftItem> getmSoftItemList() {
        return mSoftItemList;
    }

    public void setmSoftItemList(List<SoftItem> mSoftItemList) {
        this.mSoftItemList = mSoftItemList;
    }

    private Context mContext;

    public SoftAdapter(Context context) {
        mContext = context;
    }

    @NonNull

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(viewType, parent, false);
        if (viewType == R.layout.uninstall_apps_blank_layout) {
            return new NullViewHolder(view);
        } else {
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NullViewHolder) {
            bindNullViewHoler((NullViewHolder) holder);
        } else if (holder instanceof ViewHolder) {
            bindViewHolder((ViewHolder) holder, position);
        }
    }

    private void bindNullViewHoler(NullViewHolder holder) {
    }

    private void bindViewHolder(ViewHolder viewHolder, int position) {
        SoftItem softItem = mSoftItemList.get(position);
        viewHolder.softImage.setImageDrawable(softItem.getImageId());
        viewHolder.softName.setText(softItem.getName());
        viewHolder.softMemory.setText(softItem.getMomory());
        viewHolder.softCheckBox.setChecked(softItem.getTrue());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (softItem.getTrue()) {
                    softItem.setTrue(false);
                } else {
                    softItem.setTrue(true);
                }
                viewHolder.softCheckBox.setChecked(softItem.getTrue());
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView softImage;
        TextView softName;
        TextView softMemory;
        CheckBox softCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            softImage = (ImageView) itemView.findViewById(R.id.soft_image);
            softName = (TextView) itemView.findViewById(R.id.soft_name);
            softMemory = (TextView) itemView.findViewById(R.id.soft_memory);
            softCheckBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    class NullViewHolder extends RecyclerView.ViewHolder {

        public NullViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return mSoftItemList.size() > 0 ? mSoftItemList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mSoftItemList.size() == 0) {
            return R.layout.uninstall_apps_blank_layout;
        } else {
            return R.layout.softmanager_item;
        }
    }
}
