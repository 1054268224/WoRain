/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot.v1;

import java.util.Map;

public interface AutoBootOptionStrategy {
    Map<String, AutoBootOptionItem> queryAutoBootOptions();

    AutoBootOptionItem queryAutoBootOption(String packageName);
}