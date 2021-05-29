package com.cydroid.systemmanager.antivirus;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.util.HashMap;

import com.cydroid.systemmanager.utils.Log;

public class ConfigLoader {
	private static final boolean DEBUG = false;

	private static final String TAG = "ConfigLoader";
	private static final String _KEY = "key";
	private static final String _VALUE = "value";

	private static final int STATUS_ROOT = 0;
	private static final int STATUS_LEVEL_1 = 1;
	private static final int STATUS_LEVEL_1_GLOBAL = STATUS_LEVEL_1;

	public ConfigLoader(Context c) {
		mContext = c;
	}

	/** 加载全局配置文件。为了通用，将加载 XML 的逻辑单独提出来，这样可以同时支持从 SD 卡上加载和从资源里加载。 */
	public boolean load() {

		XmlResourceParser parser = null;
		try {
			mParser = parser = mContext.getAssets().openXmlResourceParser(
					"res/xml/config.xml");
			loadConfig();
			return true;
		} catch (IOException e) {
			if (DEBUG) {
				Log.e(TAG, "", e);
			}
		} finally {
			if (parser != null) {
				parser.close();
			}
		}

		return false;
	}

	public String getGlobalOptions(String key) {
		return mGlobalOptions.get(key);
	}

	/** 读取 XML 的代码 */
	private void loadConfig() {
		try {
			// 读取 XML 的状态机，记录读到第几层哪个节了
			int status = STATUS_ROOT;
			int event = mParser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name = mParser.getName();
				if (event == XmlPullParser.START_TAG) {
					switch (status) {
					case STATUS_ROOT:
						// 全局设置
						if ("Global".equals(name)) {
							status = STATUS_LEVEL_1_GLOBAL;
							break;
						}
						break;
					case STATUS_LEVEL_1_GLOBAL:
						parseGlobalConfig(name);
						break;
					}
				} else {
					if (event == XmlPullParser.END_TAG) {
						switch (status) {
						case STATUS_LEVEL_1_GLOBAL:
							if ("Global".equals(name)) {
								status = STATUS_ROOT;
							}
							break;
						}
					}
				}
				event = mParser.next();
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "", e);
			}
		}
	}

	private void parseGlobalConfig(String name) {
		if ("Item".equals(name)) {
			String key = mParser.getAttributeValue(null, _KEY);
			String value = mParser.getAttributeValue(null, _VALUE);
			mGlobalOptions.put(key, value);

			if (DEBUG) {
				Log.i(TAG, "[Global] " + key + "=" + value);
			}
		}
	}

	final Context mContext;
	private XmlPullParser mParser = null;
	private HashMap<String, String> mGlobalOptions = new HashMap<String, String>();
}
