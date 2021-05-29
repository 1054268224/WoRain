package cyee.widget;

import java.util.List;

import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.cyee.internal.app.CyeeResolverActivity.TargetInfo;

public abstract class CyeeBaseAutoAdapter extends BaseAdapter {

    //public abstract int getCount(); //返回数据数量

    //public abstract TargetInfo getItem(int position); //当前Item的数据

    public abstract View getItemView(int position, ViewGroup parent); //返回Item的布局

    public View getView(int position, View convertView, ViewGroup parent) {
        return getItemView(position, parent);
    }
}
