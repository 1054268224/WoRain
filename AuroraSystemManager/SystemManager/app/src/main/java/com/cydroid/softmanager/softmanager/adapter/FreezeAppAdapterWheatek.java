package com.cydroid.softmanager.softmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppInfo;
import com.example.systemmanageruidemo.softmanager.adapter.SoftAdapter;

import org.w3c.dom.Text;

import java.util.List;

public class FreezeAppAdapterWheatek extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private StateChangeCallback callback;
    private List<FreezeAppInfo> freezeNormalApps;
    private List<FreezeAppInfo> freezeCautiousApps;

    public FreezeAppAdapterWheatek(Context context, StateChangeCallback callback, List<FreezeAppInfo> freezeNormalApps, List<FreezeAppInfo> freezeCautiousApps) {
        this.context = context;
        this.callback = callback;
        this.freezeNormalApps = freezeNormalApps;
        this.freezeCautiousApps = freezeCautiousApps;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(viewType,parent,false);
        if (viewType == R.layout.blank){
            return new NullViewHolder(view);
        } else {
            return new FreezeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NullViewHolder){
            bindNullViewHoler((NullViewHolder) holder);
        } else if (holder instanceof FreezeViewHolder) {
            bindFreezeViewHolder((FreezeViewHolder) holder,position);
        }

    }

    private void bindFreezeViewHolder(FreezeViewHolder holder, int position) {
        FreezeAppInfo freezeAppInfo = freezeNormalApps.get(position);
        holder.imageView.setImageDrawable(freezeAppInfo.getIcon());
        holder.textView.setText(freezeAppInfo.getTitle());
        holder.checkBox.setChecked(freezeAppInfo.getCheckStaus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (freezeAppInfo.getCheckStaus()){
                    freezeAppInfo.setCheckStatus(false);
                } else {
                    freezeAppInfo.setCheckStatus(true);
                }
                holder.checkBox.setChecked(freezeAppInfo.getCheckStaus());
            }
        });
    }

    private void bindNullViewHoler(NullViewHolder holder) {
    }

    class NullViewHolder extends RecyclerView.ViewHolder {

        public NullViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class FreezeViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        CheckBox checkBox;

        public FreezeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.soft_image);
            textView = (TextView) itemView.findViewById(R.id.soft_name);
            checkBox = (CheckBox) itemView.findViewById(R.id.wheatek_checkbox);
        }
    }

    @Override
    public int getItemCount() {
        return freezeNormalApps.size() > 0 ? freezeNormalApps.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (freezeNormalApps.size() == 0){
            return R.layout.blank;
        } else {
            return R.layout.freezeapp_item;
        }
    }
}
