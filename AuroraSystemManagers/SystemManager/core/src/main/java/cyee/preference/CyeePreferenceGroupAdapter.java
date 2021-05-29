package cyee.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cyee.preference.CyeePreference.OnPreferenceChangeInternalListener;
import android.content.Context;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class CyeePreferenceGroupAdapter extends BaseAdapter implements OnPreferenceChangeInternalListener {
    
    private static final String TAG = "PreferenceGroupAdapter";

    /**
     * The group that we are providing data from.
     */
    private final CyeePreferenceGroup mPreferenceGroup;
    
    /**
     * Maps a position into this adapter -> {@link Preference}. These
     * {@link Preference}s don't have to be direct children of this
     * {@link PreferenceGroup}, they can be grand children or younger)
     */
    private List<CyeePreference> mPreferenceList;
    
    /**
     * List of unique Preference and its subclasses' names. This is used to find
     * out how many types of views this adapter can return. Once the count is
     * returned, this cannot be modified (since the ListView only checks the
     * count once--when the adapter is being set). We will not recycle views for
     * Preference subclasses seen after the count has been returned.
     */
    private final ArrayList<PreferenceLayout> mPreferenceLayouts;

    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();

    /**
     * Blocks the mPreferenceClassNames from being changed anymore.
     */
    private boolean mHasReturnedViewTypeCount = false;
    
    private volatile boolean mIsSyncing = false;
    
    private final Handler mHandler = new Handler();
    
    private final Runnable mSyncRunnable = new Runnable() {
        public void run() {
            syncMyPreferences();
        }
    };

    private static class PreferenceLayout implements Comparable<PreferenceLayout> {
        private int resId;
        private int widgetResId;
        private String name;

        public int compareTo(PreferenceLayout other) {
            int compareNames = name.compareTo(other.name);
            if (compareNames == 0) {
                if (resId == other.resId) {
                    if (widgetResId == other.widgetResId) {
                        return 0;
                    } else {
                        return widgetResId - other.widgetResId;
                    }
                } else {
                    return resId - other.resId;
                }
            } else {
                return compareNames;
            }
        }
    }

    public CyeePreferenceGroupAdapter(CyeePreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        // If this group gets or loses any children, let us know
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferenceList = new ArrayList<CyeePreference>();
        mPreferenceLayouts = new ArrayList<PreferenceLayout>();

        syncMyPreferences();
    }
    
    // Gionee zhangxx 2012-12-12 add for CR00715173 begin
    public CyeePreferenceGroupAdapter(CyeePreferenceGroup preferenceGroup, Context context, boolean isGioneeStyle) {
        mPreferenceGroup = preferenceGroup;
        // If this group gets or loses any children, let us know
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferenceList = new ArrayList<CyeePreference>();
        mPreferenceLayouts = new ArrayList<PreferenceLayout>();

        mContext = context;
        mIsGioneeStyle = isGioneeStyle;
        syncMyPreferences();
    }    
    // Gionee zhangxx 2012-12-12 add for CR00715173 end

    private void syncMyPreferences() {
        synchronized(this) {
            if (mIsSyncing) {
                return;
            }

            mIsSyncing = true;
        }

        List<CyeePreference> newPreferenceList = new ArrayList<CyeePreference>(mPreferenceList.size());
        flattenPreferenceGroup(newPreferenceList, mPreferenceGroup);
        mPreferenceList = newPreferenceList;
        
        // Gionee zhangxx 2012-12-12 add for CR00715173 begin
        if (mIsGioneeStyle) {
            // Gionee <weidong><2015-1-17> modify for CR01438181 begin
            mPreferenceType = getPreferenceType(mPreferenceList);
            // Gionee <weidong><2015-1-17> modify for CR01438181 end
        }
        // Gionee zhangxx 2012-12-12 add for CR00715173 end
        notifyDataSetChanged();

        synchronized(this) {
            mIsSyncing = false;
            notifyAll();
        }
    }

    enum ItemType {
        NORMAL_PREFERENCE, CATEGORY_PREFERENCE
    }

    enum PosType {
        TOP_POS, MID_POS, END_POS
    }

    static class PreferenceType {
        public ItemType itemType;
        public PosType posType;
    }
    
    // Gionee zhangxx 2012-12-12 add for CR00715173 begin
    private PreferenceType[] mPreferenceType;
    private Context mContext;
    private boolean mIsGioneeStyle = false;
    
    // Gionee <weidong><2015-1-17> modify for CR01438181 begin
    private PreferenceType[] getPreferenceType(List<CyeePreference> preferences) {
        int size = preferences.size();
        if (preferences == null || size <= 0) {
            return null;
        }
        PreferenceType[] types = new PreferenceType[size];
        for (int i = 0; i < size; i++) {
            PreferenceType type = new PreferenceType();
            type.posType = PosType.MID_POS;
            if(i == 0) {
                type.posType = PosType.TOP_POS;
            }
            type.itemType = ItemType.NORMAL_PREFERENCE;
            if (preferences.get(i) instanceof CyeePreferenceCategory) {
                type.itemType = ItemType.CATEGORY_PREFERENCE;
                if (i > 0) {
                    types[i - 1].posType = PosType.END_POS;
                }
            }
            if(i == size - 1) {
                type.posType = PosType.END_POS;
            }
            types[i] = type;
        }

        return types;
    }
    
    // Gionee <weidong><2015-1-17> modify for CR01438181 end

    private void flattenPreferenceGroup(List<CyeePreference> preferences, CyeePreferenceGroup group) {
        // TODO: shouldn't always?
        group.sortPreferences();

        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final CyeePreference preference = group.getPreference(i);
            
            preferences.add(preference);
            
            if (!mHasReturnedViewTypeCount && !preference.hasSpecifiedLayout()) {
                addPreferenceClassName(preference);
            }
            
            if (preference instanceof CyeePreferenceGroup) {
                final CyeePreferenceGroup preferenceAsGroup = (CyeePreferenceGroup) preference;
                if (preferenceAsGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, preferenceAsGroup);
                }
            }

            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    /**
     * Creates a string that includes the preference name, layout id and widget layout id.
     * If a particular preference type uses 2 different resources, they will be treated as
     * different view types.
     */
    private PreferenceLayout createPreferenceLayout(CyeePreference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    private void addPreferenceClassName(CyeePreference preference) {
        final PreferenceLayout pl = createPreferenceLayout(preference, null);
        int insertPos = Collections.binarySearch(mPreferenceLayouts, pl);

        // Only insert if it doesn't exist (when it is negative).
        if (insertPos < 0) {
            // Convert to insert index
            insertPos = insertPos * -1 - 1;
            mPreferenceLayouts.add(insertPos, pl);
        }
    }
    
    public int getCount() {
        return mPreferenceList.size();
    }

    public CyeePreference getItem(int position) {
        if (position < 0 || position >= getCount()) return null;
        return mPreferenceList.get(position);
    }

    public long getItemId(int position) {
        if (position < 0 || position >= getCount()) return ListView.INVALID_ROW_ID;
        return this.getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CyeePreference preference = this.getItem(position);
        // Build a PreferenceLayout to compare with known ones that are cacheable.
        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout);

        // If it's not one of the cached ones, set the convertView to null so that
        // the layout gets re-created by the Preference.
        if (Collections.binarySearch(mPreferenceLayouts, mTempPreferenceLayout) < 0) {
            convertView = null;
        }
        // Gionee zhangxx 2012-12-12 add for CR00715173 begin
        if (mIsGioneeStyle) {
            // Gionee <weidong><2015-1-17> modify for CR01438181 begin
            preference.setPreferenceType(mPreferenceType[position]);
            View view = preference.getView(convertView, parent);
            // Gionee <weidong><2015-1-17> modify for CR01438181 end
            return view;
        } else {
            // Gionee zhangxx 2012-12-12 add for CR00715173 end
            return preference.getView(convertView, parent);
            // Gionee zhangxx 2012-12-12 add for CR00715173 begin
        }
        // Gionee zhangxx 2012-12-12 add for CR00715173 end
    }

    @Override
    public boolean isEnabled(int position) {
        if (position < 0 || position >= getCount()) return true;
        return this.getItem(position).isSelectable();
    }

    @Override
    public boolean areAllItemsEnabled() {
        // There should always be a preference group, and these groups are always
        // disabled
        return false;
    }

    public void onPreferenceChange(CyeePreference preference) {
        notifyDataSetChanged();
    }

    public void onPreferenceHierarchyChange(CyeePreference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true;
        }
        
        final CyeePreference preference = this.getItem(position);
        if (preference.hasSpecifiedLayout()) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout);

        int viewType = Collections.binarySearch(mPreferenceLayouts, mTempPreferenceLayout);
        if (viewType < 0) {
            // This is a class that was seen after we returned the count, so
            // don't recycle it.
            return IGNORE_ITEM_VIEW_TYPE;
        } else {
            return viewType;
        }
    }

    @Override
    public int getViewTypeCount() {
        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true;
        }
        
        return Math.max(1, mPreferenceLayouts.size());
    }

}
