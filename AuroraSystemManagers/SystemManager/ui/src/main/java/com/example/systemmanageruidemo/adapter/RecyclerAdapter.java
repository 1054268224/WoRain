package com.example.systemmanageruidemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.bean.DataBean;

import java.util.List;

import static com.example.systemmanageruidemo.RubbishCleanerMainActivity.calculate;

public class RecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private Context mContext;
    private List<DataBean> mDataBeanList;
    private LayoutInflater mInflater;
    private OnScrollListener mOnScrollListener;

    public RecyclerAdapter(Context context, List<DataBean> dataBeanList) {
        mContext = context;
        mDataBeanList = dataBeanList;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case DataBean.PARENT_ITEM:
                view = mInflater.inflate(R.layout.rub_item_parent, parent, false);
                return new ParentViewHolder(mContext, view);
            case DataBean.CHILD_ITEM:
                view = mInflater.inflate(R.layout.rub_item_child, parent, false);
                return new ChildViewHolder(mContext, view, RecyclerAdapter.this);
            default:
                view = mInflater.inflate(R.layout.rub_item_parent, parent, false);
                return new ParentViewHolder(mContext, view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {

        int re[] = chaifen(position, mDataBeanList);
        switch (getItemViewType(position)) {
            case DataBean.PARENT_ITEM:
                ParentViewHolder parentViewHolder = (ParentViewHolder) holder;
                parentViewHolder.bindView(mDataBeanList.get(re[0]), position, itemClickListener);
                break;
            case DataBean.CHILD_ITEM:
                ChildViewHolder childViewHolder = (ChildViewHolder) holder;
                childViewHolder.bindView(mDataBeanList.get(re[0]).getChildren().get(re[1]), position);
        }

    }


    int[] chaifen(int position, List<DataBean> list) {
        int[] re;
        int i = 0, j = 0;
        int tem = position;
        for (DataBean dataBean : list) {
            tem = tem - 1;
            if (tem < 0) {
                j = -1;
                break;
            } else {
                tem = tem - (dataBean.getChildren() == null ? 0 : dataBean.getChildren().size());
                if (tem < 0) {
                    j = tem + (dataBean.getChildren() == null ? 0 : dataBean.getChildren().size());
                    break;
                }
            }
            i++;
        }
        re = new int[]{i, j};
        return re;
    }

    int sum(List<DataBean> list) {
        int sum = 0;
        for (DataBean dataBean : list) {
            sum += (1 + (dataBean.getChildren() == null ? 0 : dataBean.getChildren().size()));
        }
        return sum;
    }

    @Override
    public int getItemCount() {

        return sum(mDataBeanList);
    }


    @Override
    public int getItemViewType(int position) {
        int re[] = chaifen(position, mDataBeanList);
        if (re[1] == -1) return mDataBeanList.get(re[0]).getType();
        else return mDataBeanList.get(re[0]).getChildren().get(re[1]).getType();
    }

    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onExpandChildren(DataBean bean) {
            for (DataBean.DataBeanChild child : bean.getChildren()) {
                child.setHide(false);
            }
            notifyDataSetChanged();
        }

        @Override
        public void onHideChildren(DataBean bean) {
            for (DataBean.DataBeanChild child : bean.getChildren()) {
                child.setHide(true);
            }
            notifyDataSetChanged();
        }

        @Override
        public void onCheckChange(DataBean bean, boolean ischeck) {
            for (DataBean.DataBeanChild child : bean.getChildren()) {
                child.setChildTrue(ischeck);
            }
            calculate(bean);
            notifyDataSetChanged();
        }
    };

    private List<DataBean.DataBeanChild> getChildDataBean(DataBean bean) {

        return bean.getChildren();
    }

    public interface OnScrollListener {
        void scrollTo(int pos);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }
}
