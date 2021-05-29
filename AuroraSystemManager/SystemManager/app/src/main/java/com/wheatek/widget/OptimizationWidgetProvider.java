package com.wheatek.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cydroid.softmanager.R;
import com.wheatek.proxy.ui.HostMainActicity;
import com.wheatek.proxy.ui.HostOptimiseActivity;

public class OptimizationWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager wm, int[] widgetIds) {
        super.onUpdate(context, wm, widgetIds);

        for (int widgetId : widgetIds) {
            final String packageName = context.getPackageName();
            final RemoteViews widget = new RemoteViews(packageName, R.layout.optwidget);
            final Intent openApp = new Intent(context, HostOptimiseActivity.class);
            final PendingIntent pi = PendingIntent.getActivity(context, 0, openApp, 0);
            widget.setOnClickPendingIntent(R.id.optwidgetlay, pi);
            wm.updateAppWidget(widgetId, widget);
        }
    }
}
