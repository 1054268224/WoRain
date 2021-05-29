package cyee.widget;

import java.util.Set;

import cyee.widget.CyeeExpandableListView.OnChildClickListener;
import cyee.widget.CyeeExpandableListView.OnGroupClickListener;
import android.os.Bundle;

public interface CyeeExpandableMultiChoiceAdapter {
    void setAdapterView(CyeeExpandableListView adapterView);
    void setOnGroupClickListener(OnGroupClickListener listener);
    void setOnChildClickListener(OnChildClickListener listener);
    void save(Bundle outState);
    void setChildChecked(long position, boolean checked);
    void setGroupChecked(int position, boolean checked);
    int getCheckedChildCount(int groupPosition);
    void getCheckedChildIndex(int groupPosition, Set<Integer> result);
    boolean isGroupChecked(int position);
    boolean isChildChecked(long position);
    boolean isGroupCheckable(int position);
    boolean isChildCheckable(long position);
    boolean hasItemSelected();
    void enterMultiChoiceMode();
}
