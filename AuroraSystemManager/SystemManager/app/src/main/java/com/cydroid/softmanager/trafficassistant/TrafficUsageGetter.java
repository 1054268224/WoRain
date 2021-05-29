package com.cydroid.softmanager.trafficassistant;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import com.cydroid.softmanager.IDataStatistic;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;

public class TrafficUsageGetter implements IDataStatistic {

    @Override
    public CharSequence getStatisticData(Context context) {
        //String[] splits = TrafficAssistantMainActivity.getTrafficInfo(context).split("-");
        TrafficSettingControler trafficSettingControler = TrafficSettingControler.getInstance(context);
        int simIndex = TrafficassistantUtil.getActivatedSimCardNo(context);
        int activateSindex = simIndex > 0 ? simIndex : 0;
        String flow = trafficSettingControler.getNotificationFlow(activateSindex);
        int type = trafficSettingControler.getNotificationType();
        Spanned result = null;
        String str = "";
        switch (type) {
            case TrafficSettingControler.NOTIFI_TYPE_NO_SETTED:
                str = context.getString(R.string.traffic_no_setting);
                result = Html.fromHtml(context.getString(R.string.traffic_no_setting));
                break;
            case TrafficSettingControler.NOTIFI_TYPE_SURPLUS:
                str = context.getString(R.string.traffic_remained);
                result = Html.fromHtml(String.format(str, flow));
                break;
            case TrafficSettingControler.NOTIFI_TYPE_EXCEED:
                str = context.getString(R.string.traffic_exceed);
                result = Html.fromHtml(String.format(str, flow));
                break;
            default:
                break;
        }
        return result;
    }

}