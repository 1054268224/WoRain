// Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
package com.cydroid.softmanager.powersaver.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.interfaces.ISelectedDataChangedListener;
import com.cydroid.softmanager.powersaver.mode.ModeItemInfo;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItem;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeAlertDialog;
import cyee.app.CyeeAlertDialog.Builder;
import cyee.widget.CyeeSeekBar;
import cyee.widget.CyeeTextView;

public class PowerModeItemAdapter extends BaseAdapter {
    private static final String TAG = "PowerModeItemAdapter";

    private final Context mContext;
    private final ISelectedDataChangedListener mListener;

    private final List<ModeItemInfo> mConfigList;
    private final List<String> mOriginValueList;
    private CyeeAlertDialog mAlertDialog;
    private int mDefaultFlag = 0;

    public PowerModeItemAdapter(Context context, ISelectedDataChangedListener listener,
                                List<ModeItemInfo> configList) {
        mContext = context;
        mListener = listener;
        mConfigList = configList;
        mOriginValueList = new ArrayList<String>();
        for (ModeItemInfo itemConfig : configList) {
            if (itemConfig.defaultVal.equals(itemConfig.configVal)) {
                mDefaultFlag++;
            }
            mOriginValueList.add(itemConfig.configVal);
        }
    }

    public int getElementValues() {
        return 0;
    }

    @Override
    public int getCount() {
        return mConfigList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PowerModeItemHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.power_mode_item_adapter_layout,
                    parent, false);
            holder = new PowerModeItemHolder();
            convertView.setTag(holder);
        } else {
            holder = (PowerModeItemHolder) convertView.getTag();
        }

        holder.mTitle = (CyeeTextView) convertView.findViewById(R.id.title);
        holder.mSummary = (CyeeTextView) convertView.findViewById(R.id.summary);
        holder.mConfigValue = (CyeeTextView) convertView.findViewById(R.id.config_value);
        holder.mConfig = mConfigList.get(position);
        // holder.mOriginConfigValue = holder.mConfig.configVal;
        ArrayList<String> vals = holder.mConfig.candidateVals;
        holder.mIsRangeValue = PowerModeUtils.isNumeric(vals.get(0)) && vals.get(1).equals(PowerModeItem.RANGE_SYMBOL);

        ModeItemInfo config = holder.mConfig;
        Log.d(TAG, "show title=" + config.title + " summary=" + config.summary);
        holder.mTitle.setText(config.title);
        if (config.summary.isEmpty()) {
            holder.mSummary.setVisibility(View.GONE);
        } else {
            holder.mSummary.setVisibility(View.VISIBLE);
        }
        holder.mSummary.setText(config.summary);
        String configValStr = "";
        for (int i = 0; i < config.candidateVals.size(); i++) {
            if (config.candidateVals.get(i).equals(config.configVal)) {
                configValStr = config.candidateValDecs.get(i);
                break;
            }
        }
        String disVal = configValStr;
        if (!config.configVal.equals(PowerModeItem.SWITCH_PASS)) {
            if (holder.mIsRangeValue) {
                int ratio = (int) (Float.parseFloat(config.configVal)
                        / Float.parseFloat(config.candidateVals.get(2)) * 100);
                disVal = "" + ratio;
            }

            if (!config.format.isEmpty()) {
                Log.d(TAG, "get display str= " + config.format + " for " + disVal + " " + config.configVal);
                configValStr = String.format(config.format, disVal);
            }
        }

        holder.mConfigValue.setText(configValStr);
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mIsRangeValue) {
                    showProgressConfigDialog(holder.mConfig);
                } else {
                    showSingleChoiceConfigDialog(holder.mConfig);
                }
            }
        });
        return convertView;
    }

    private void setNewValue(ModeItemInfo config, String val) {
        if (config.configVal.equals(val)) {
            return;
        }
        if (val.equals(config.defaultVal)) {
            mDefaultFlag++;
        } else if (config.configVal.equals(config.defaultVal)) {
            mDefaultFlag--;
        }
        config.configVal = val;
        mListener.onDataChanged(null);
    }

    private void showProgressConfigDialog(final ModeItemInfo config) {
        ArrayList<String> vals = config.candidateVals;
        ArrayList<String> valDecs = config.candidateValDecs;
        int minVal = Integer.parseInt(vals.get(0));
        int maxVal = Integer.parseInt(vals.get(2));
        int currentVal = 0;
        if (PowerModeUtils.isNumeric(config.configVal)) {
            currentVal = Integer.parseInt(config.configVal);
        } else if (PowerModeUtils.isNumeric(config.defaultVal)) {
            currentVal = Integer.parseInt(config.defaultVal);
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout dialogLayout = (LinearLayout) inflater.inflate(R.layout.power_mode_item_seekbar_dialog,
                null);
        CyeeSeekBar seekBar = (CyeeSeekBar) dialogLayout.findViewById(R.id.seekbar);
        seekBar.setMax(maxVal - minVal);
        seekBar.setProgress(currentVal - minVal);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setNewValue(config, String.valueOf(progress));
                PowerModeItemAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAlertDialog.dismiss();
            }

        });
        Builder builder = new CyeeAlertDialog.Builder(mContext).setTitle(config.title).setView(dialogLayout);
        if (vals.get(vals.size() - 1).equals(PowerModeItem.SWITCH_PASS)) {
            builder.setPositiveButton(valDecs.get(vals.size() - 1), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setNewValue(config, PowerModeItem.SWITCH_PASS);
                    PowerModeItemAdapter.this.notifyDataSetChanged();
                    mAlertDialog.dismiss();
                }
            });
        }
        builder.setNegativeButton(R.string.mode_item_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void showSingleChoiceConfigDialog(final ModeItemInfo config) {
        final ArrayList<String> vals = config.candidateVals;
        ArrayList<String> valsStr = config.candidateValDecs;
        int selectedIndex = vals.size() - 1;
        for (int i = 0; i < vals.size(); i++) {
            if (config.configVal.equals(vals.get(i))) {
                selectedIndex = i;
            }
        }

        CharSequence[] dialogMsg;
        String[] valsArray = new String[valsStr.size()];
        //chenyee zhaocaili modify for CSW1803A-1667 begin
        valsStr = setArabiaLanguage(config, valsStr);
        //chenyee zhaocaili modify for CSW1803A-1667 end
        valsArray = valsStr.toArray(valsArray);
        mAlertDialog = new CyeeAlertDialog.Builder(mContext).setTitle(config.title)
                .setSingleChoiceItems(valsArray, selectedIndex, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectVal = vals.get(which);
                        setNewValue(config, selectVal);
                        PowerModeItemAdapter.this.notifyDataSetChanged();
                        mAlertDialog.dismiss();
                    }

                }).setNegativeButton(R.string.mode_item_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                    }
                }).create();
        mAlertDialog.show();
    }

    //chenyee zhaocaili modify for CSW1803A-1667 begin
    private ArrayList<String> setArabiaLanguage(ModeItemInfo item, ArrayList<String> valList){
        Resources res = mContext.getResources();
        if (valList != null && valList.size() >= 8 && item.title.equals(res.getString(R.string.set_sleep_time))){
            Configuration config = res.getConfiguration();
            String language = config.locale.getLanguage();
            String country = config.locale.getCountry();
            if (country.equals("DZ") && language.equals("ar")){
                valList.set(3, "دقيقتين");
                valList.set(6, "دقيقة 30");
            }
        }
        return valList;
    }
    //chenyee zhaocaili modify for CSW1803A-1667 end

    private static class PowerModeItemHolder {
        ModeItemInfo mConfig;
        CyeeTextView mTitle;
        CyeeTextView mSummary;
        CyeeTextView mConfigValue;
        // String mOriginConfigValue;
        boolean mIsRangeValue;
    }

    public void resetConfig() {
        for (ModeItemInfo config : mConfigList) {
            setNewValue(config, config.defaultVal);
        }
        notifyDataSetChanged();
    }

    public boolean isConfigChanged() {
        for (int i = 0; i < mConfigList.size(); i++) {
            if (!mConfigList.get(i).configVal.equals(mOriginValueList.get(i)))
                return true;
        }
        return false;
    }

    public boolean isDefault() {
        return mConfigList.size() == mDefaultFlag;
    }

    public List<ModeItemInfo> getChangedConfiged() {
        ArrayList<ModeItemInfo> res = new ArrayList<ModeItemInfo>();
        for (int i = 0; i < mConfigList.size(); i++) {
            if (!mConfigList.get(i).configVal.equals(mOriginValueList.get(i)))
                res.add(mConfigList.get(i));
        }
        return res;
    }
}
// Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
