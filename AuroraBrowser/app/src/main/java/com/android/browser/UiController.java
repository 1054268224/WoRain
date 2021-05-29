/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.browser;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.util.List;


/**
 * UI aspect of the controller
 */
public interface UiController {

    UI getUi();

    WebView getCurrentWebView();

    WebView getCurrentTopWebView();

    Tab getCurrentTab();

    TabControl getTabControl();

    List<Tab> getTabs();

    Tab openTabToHomePage() throws Exception;

    Tab openIncognitoTab() throws Exception;

    Tab openTab(String url, boolean incognito, boolean setActive,
            boolean useCurrent) throws Exception;

    void setActiveTab(Tab tab) throws Exception;

    boolean switchToTab(Tab tab) throws Exception;

    void closeCurrentTab() throws Exception;

    void closeTab(Tab tab) throws Exception;

    void closeOtherTabs() throws Exception;

    void stopLoading();

    Intent createBookmarkCurrentPageIntent(boolean canBeAnEdit);

    void bookmarksOrHistoryPicker(UI.ComboViews startView);

    void bookmarkCurrentPage();

    void editUrl();

    void handleNewIntent(Intent intent) throws Exception;

    boolean shouldShowErrorConsole();

    void hideCustomView();

    void attachSubWindow(Tab tab);

    void removeSubWindow(Tab tab);

    boolean isInCustomActionMode();

    void endActionMode();

    void shareCurrentPage();

    void updateMenuState(Tab tab, Menu menu);

    boolean onOptionsItemSelected(MenuItem item) throws Exception;

    void loadUrl(Tab tab, String url) throws Exception;

    void setBlockEvents(boolean block);

    Activity getActivity();

    void showPageInfo();

    void openPreferences();

    void findOnPage();

    void toggleUserAgent();

    BrowserSettings getSettings();

    boolean supportsVoice();

    void startVoiceRecognizer();

}
