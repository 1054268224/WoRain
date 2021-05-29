package com.cydroid.systemmanager.rubbishcleaner.util;

public class StringFilterUtil {

	public static String filterAlphabet(String string) {
		String str = string;
		str = str.replaceAll("[^(A-Za-z)]", "");
		/*guoxt modify for CSW1705A-1114 begin*/
        if(str.equals("")){
			str = string;
			str = str.replaceAll("[^(\u4e00-\u9fa5)]", "");
		}
		/*guoxt modify for CSW1705A-1114 end*/
		return str;
	}

	public static String getNumStr(String string) {
		String[] str = string.split(filterAlphabet(string));
		return str[0];
	}
}
