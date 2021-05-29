/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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
package com.android.browser.search;

import com.android.browser.R;
import com.wheatek.reflect.SearchEngineManager;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchEngines {

    private static final String TAG = "SearchEngines";

    public static SearchEngine getDefaultSearchEngine(Context context) {
        return DefaultSearchEngine.create(context);
    }

    public static List<com.wheatek.reflect.SearchEngine>
        getSearchEngineInfos(Context context) throws Exception {
        ArrayList<com.wheatek.reflect.SearchEngine> searchEngineInfos =
            new ArrayList<com.wheatek.reflect.SearchEngine>();
        Resources res = context.getResources();
        String[] searchEngines = res.getStringArray(R.array.search_engines);
        SearchEngineManager searchEngineManger = new SearchEngineManager(context);
        for (int i = 0; i < searchEngines.length; i++) {
            String name = searchEngines[i];
            com.wheatek.reflect.SearchEngine info = searchEngineManger.getByName(name);
            searchEngineInfos.add(info);
        }
        return searchEngineInfos;
    }

    public static SearchEngine get(Context context, String name) throws Exception {
        // TODO: cache
        SearchEngine defaultSearchEngine = getDefaultSearchEngine(context);
        if (TextUtils.isEmpty(name)
                || (defaultSearchEngine != null && name.equals(defaultSearchEngine.getName()))) {
            return defaultSearchEngine;
        }
        com.wheatek.reflect.SearchEngine searchEngineInfo =
                                getSearchEngineInfo(context, name);
        if (searchEngineInfo == null) return defaultSearchEngine;
        return new OpenSearchSearchEngine(context, searchEngineInfo);
    }

    public static com.wheatek.reflect.SearchEngine getSearchEngineInfo(Context context,
                                                                              String name) throws Exception {
        SearchEngineManager searchEngineManager = new SearchEngineManager(context);
        return searchEngineManager.getByName(name);
       /* try {
            SearchEngineManager searchEngineManger = (SearchEngineManager) context
                    .getSystemService(SearchEngineManager.SEARCH_ENGINE_SERVICE);
            return searchEngineManger.getByName(name);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Cannot load search engine " + name, exception);
            return null;
        }*/
    }

}
