package com.mediatek.browser.ext;

import android.content.Context;

/*import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader.OperatorFactoryInfo;*/

import com.wheatek.reflect.OperatorCustomizationFactoryLoader;
import com.wheatek.reflect.OperatorFactoryInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory base for operator customization.
 */
public class OpBrowserCustomizationFactoryBase {

    private static final List<Object> sOperatorFactoryInfoList
            = new ArrayList<Object>();

    static OpBrowserCustomizationFactoryBase sFactory = null;
    static {
        sOperatorFactoryInfoList.add(OperatorFactoryInfo.getOperatorFactoryInfo("OP07Browser.apk",
                "com.mediatek.op07.browser.Op07BrowserCustomizationFactory",
                "com.mediatek.op07.browser",
                "OP07",
                "SEGDEFAULT",
                "SPEC0407"
        ));
    }

    /**
     * get instance of operator customization factory.
     *
     * @param context host app context.
     * @return instance of operator customization factory.
     */
    public static synchronized OpBrowserCustomizationFactoryBase getOpFactory(Context context) throws Exception {
        if (sFactory == null) {
            sFactory = (OpBrowserCustomizationFactoryBase) OperatorCustomizationFactoryLoader
                    .loadFactory(context, sOperatorFactoryInfoList);
            sFactory = null;
            if (sFactory == null) {
                sFactory = new OpBrowserCustomizationFactoryBase();
            }
        }
        return sFactory;
    }

    /**
     * reset the operator customization factory.
     */
    public static synchronized void resetOpFactory() {
        sFactory = null;
    }

    /**
     * get default bookmark plugin.
     *
     * @return default bookmark plugin.
     */
    public IBrowserBookmarkExt makeBrowserBookmarkExt() {
        return new DefaultBrowserBookmarkExt();
    }

    /**
     * get default download plugin.
     *
     * @return default download plugin.
     */
    public IBrowserDownloadExt makeBrowserDownloadExt() {
        return new DefaultBrowserDownloadExt();
    }

    /**
     * get default history plugin.
     *
     * @return default history plugin.
     */
    public IBrowserHistoryExt makeBrowserHistoryExt() {
        return new DefaultBrowserHistoryExt();
    }

    /**
     * get default misc feature plugin.
     *
     * @return default misc feature plugin.
     */
    public IBrowserMiscExt makeBrowserMiscExt() {
        return new DefaultBrowserMiscExt();
    }

    /**
     * get default regional phone plugin.
     *
     * @return default regional phone plugin.
     */
    public IBrowserRegionalPhoneExt makeBrowserRegionalPhoneExt() {
        return new DefaultBrowserRegionalPhoneExt();
    }

    /**
     * get default settings plugin.
     *
     * @return default settings plugin.
     */
    public IBrowserSettingExt makeBrowserSettingExt() {
        return new DefaultBrowserSettingExt();
    }

    /**
     * get default site navigation plugin.
     *
     * @return default site navigation plugin.
     */
    public IBrowserSiteNavigationExt makeBrowserSiteNavigationExt() {
        return new DefaultBrowserSiteNavigationExt();
    }

    /**
     * get default browser url plugin.
     *
     * @return default browser url plugin.
     */
    public IBrowserUrlExt makeBrowserUrlExt() {
        return new DefaultBrowserUrlExt();
    }

    /**
     * get default network state plugin.
     *
     * @return default network state plugin.
     */
    public INetworkStateHandlerExt makeNetworkStateHandlerExt() {
        return new DefaultNetworkStateHandlerExt();
    }
}
