package com.cydroid.softmanager.powersaver.fuelgauge;

import java.text.NumberFormat;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceFrameLayout;
import android.view.View;
import android.view.ViewGroup;

import com.cydroid.softmanager.R;

public class Utils {
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    public static void prepareCustomPreferencesList(ViewGroup parent, View child, View list,
            boolean ignoreSidePadding) {
        final boolean movePadding = list.getScrollBarStyle() == View.SCROLLBARS_OUTSIDE_OVERLAY;
        if (movePadding) {
            final Resources res = list.getResources();
            final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);
            final int paddingBottom = res
                    .getDimensionPixelSize(com.android.internal.R.dimen.preference_fragment_padding_bottom);

            if (parent instanceof PreferenceFrameLayout) {
                ((PreferenceFrameLayout.LayoutParams) child.getLayoutParams()).removeBorders = true;

                final int effectivePaddingSide = ignoreSidePadding ? 0 : paddingSide;
                list.setPaddingRelative(effectivePaddingSide, 0, effectivePaddingSide, paddingBottom);
            } else {
                list.setPaddingRelative(paddingSide, 0, paddingSide, paddingBottom);
            }
        }
    }

    /** Formats the ratio of amount/total as a percentage. */
    public static String formatPercentage(long amount, long total) {
        return formatPercentage(((double) amount) / total);
    }

    /** Formats an integer from 0..100 as a percentage. */
    public static String formatPercentage(int percentage) {
        return formatPercentage(((double) percentage) / 100.0);
    }

    /** Formats a double from 0.0..1.0 as a percentage. */
    private static String formatPercentage(double percentage) {
        return NumberFormat.getPercentInstance().format(percentage);
    }

    /**
     * Returns elapsed time for the given millis, in the following format: 2d 5h 40m 29s
     * 
     * @param context
     *            the application context
     * @param millis
     *            the elapsed time in milli seconds
     * @param withSeconds
     *            include seconds?
     * @return the formatted elapsed time
     */
    public static String formatElapsedTime(Context context, double millis, boolean withSeconds) {
        StringBuilder sb = new StringBuilder();
        int seconds = (int) Math.floor(millis / 1000);
        if (!withSeconds) {
            // Round up.
            seconds += 30;
        }

        int days = 0, hours = 0, minutes = 0;
        if (seconds >= SECONDS_PER_DAY) {
            days = seconds / SECONDS_PER_DAY;
            seconds -= days * SECONDS_PER_DAY;
        }
        if (seconds >= SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds >= SECONDS_PER_MINUTE) {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds -= minutes * SECONDS_PER_MINUTE;
        }
        if (withSeconds) {
            if (days > 0) {
                sb.append(context.getString(R.string.battery_history_days, days, hours, minutes, seconds));
            } else if (hours > 0) {
                sb.append(context.getString(R.string.battery_history_hours, hours, minutes, seconds));
            } else if (minutes > 0) {
                sb.append(context.getString(R.string.battery_history_minutes, minutes, seconds));
            } else {
                if (seconds > 0) {
                    sb.append(context.getString(R.string.battery_history_seconds, seconds));
                } else {
                    seconds = 1;
                    sb.append("<" + context.getString(R.string.battery_history_seconds, seconds));
                }
            }
        } else {
            if (days > 0) {
                sb.append(context.getString(R.string.battery_history_days_no_seconds, days, hours, minutes));
            } else if (hours > 0) {
                sb.append(context.getString(R.string.battery_history_hours_no_seconds, hours, minutes));
            } else {
                sb.append(context.getString(R.string.battery_history_minutes_no_seconds, minutes));
            }
        }
        return sb.toString();
    }
}
