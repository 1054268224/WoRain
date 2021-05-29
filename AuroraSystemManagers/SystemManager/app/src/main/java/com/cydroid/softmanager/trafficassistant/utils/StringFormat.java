package com.cydroid.softmanager.trafficassistant.utils;

import android.content.Context;
import android.text.format.Formatter;
import java.text.DecimalFormat;

public class StringFormat {

    public static String getStringFormat(double value) {
        DecimalFormat decimalformat = new DecimalFormat("##0.00");
        return decimalformat.format(value);
    }

    public static String getStringFormat(double value, int num) {
        String pattern = "";
        switch (num) {
            case 0:
                pattern = "##0";
                break;

            case 1:
                pattern = "##0.0";
                break;

            case 2:
                pattern = "##0.00";
                break;

            default:
                pattern = "##0.00";
                break;
        }
        DecimalFormat format = new DecimalFormat(pattern);
        return format.format(value);
    }

		//Gionee guoxt 2014-07-15 modify for CR01321885 begin
   public static String getStringFormatA(double value){
		String valueStr = String.valueOf(value);
		String vlaueFormat = valueStr.substring(0, valueStr.indexOf(".")+2);
		return vlaueFormat;
	}
	//Gionee guoxt 2014-07-15 modify for CR01321885 end

    public static int getUnitByMaxValue(double maxValue) {
        if (maxValue < Constant.KB) {
            return Constant.UNIT_B;
        } else if ((maxValue >= Constant.KB) && (maxValue < Constant.MB)) {
            return Constant.UNIT_KB;
        } else if ((maxValue >= Constant.MB) && (maxValue < Constant.GB)) {
            return Constant.UNIT_MB;
        } else if ((maxValue >= Constant.GB) && (maxValue < Constant.TB)) {
            return Constant.UNIT_GB;
        } else if (maxValue >= Constant.TB) {
            return Constant.UNIT_TB;
        }

        return Constant.UNIT_KB; // by default
    }

    private static double convert(double number, int unit) {
        double result = 0.0;
        switch (unit) {
            case Constant.UNIT_B:
                result = number;
                break;
            case Constant.UNIT_KB:
                result = number / Constant.KB;
                break;
            case Constant.UNIT_MB:
                result = number / Constant.MB;
                break;
            case Constant.UNIT_GB:
                result = number / Constant.GB;
                break;
            case Constant.UNIT_TB:
                result = number / Constant.TB;
                break;
            default:
                break;
        }

        return result;
    }

    public static String getUnit(int unit) {
        String unitString = "";
        switch (unit) {
            case Constant.UNIT_B:
                unitString = Constant.STRING_UNIT_B;
                break;
            case Constant.UNIT_KB:
                unitString = Constant.STRING_UNIT_KB;
                break;
            case Constant.UNIT_MB:
                unitString = Constant.STRING_UNIT_MB;
                break;
            case Constant.UNIT_GB:
                unitString = Constant.STRING_UNIT_GB;
                break;
            case Constant.UNIT_TB:
                unitString = Constant.STRING_UNIT_TB;
                break;
            default:
                unitString = Constant.STRING_UNIT_KB;
        }

        return unitString;
    }

    public static String getUnitStyle(int unit) {
        String unitString = "";
        switch (unit) {
            case Constant.UNIT_B:
                unitString = Constant.STRING_UNIT_B;
                break;
            case Constant.UNIT_KB:
                unitString = Constant.NOTI_STRING_UNIT_KB;
                break;
            case Constant.UNIT_MB:
                unitString = Constant.NOTI_STRING_UNIT_MB;
                break;
            case Constant.UNIT_GB:
                unitString = Constant.NOTI_STRING_UNIT_GB;
                break;
            case Constant.UNIT_TB:
                unitString = Constant.NOTI_STRING_UNIT_TB;
                break;
            default:
                unitString = Constant.NOTI_STRING_UNIT_KB;
        }

        return unitString;
    }

    public static double formatDataSize(double number, int unit) {
        double result = convert(number, unit);
	//Gionee songpeng 2014-07-14 modify for CR01321577 begin
	return Double.parseDouble(getStringFormatA(result).replace(",", "."));
	//Gionee songpeng 2014-07-14 modify for CR01321577 end
        //return Double.parseDouble(String.format("%.2f", result));
    }

    public static String getUnitStringByValue(double values) {
        int unit = getUnitByMaxValue(values);
        double newValue = formatDataSize(values, unit);
        String unitString = getUnit(unit);
        return newValue + unitString;
    }

    public static String getUnitStringByValue(double values, int num) {
        int unit = getUnitByMaxValue(values);
        if (unit == Constant.UNIT_B) {
            return "0" + Constant.NOTI_STRING_UNIT_MB;
        }
        double newValue = formatDataSize(values, unit);
        String unitString = getUnitStyle(unit);
        return getStringFormat(newValue, num) + unitString;
    }

    /*
     * public static String getUnitStringByValue(long values){ int unit =
     * getUnitByMaxValue(values); if(unit == Constant.UNIT_B){ return "0" +
     * Constant.NOTI_STRING_UNIT_MB; } long newValue =
     * (long)formatDataSize(values,unit); String unitString =
     * getUnitStyle(unit); return newValue + unitString; }
     */
    public static String format(Context context, long data) {
        return Formatter.formatFileSize(context, data).replace(" ", "");
    }
}