/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.update;

import android.content.Context;

import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;

public interface UpdateStrategy {
    HttpRequestBase getHttpRequest(Context context) throws Exception;

    void setUpdateSuccess(Context context);

    void pareseJson(Context context, String json);

    boolean ifNeedNotifyUpdateSuccessed();

    Map<String, String> getResultParams();

    boolean isNeedSecurityConnection();
}
