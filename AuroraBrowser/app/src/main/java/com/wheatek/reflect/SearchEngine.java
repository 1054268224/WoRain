package com.wheatek.reflect;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class SearchEngine {
    private static Class cls;
    private Object object;
    private Context context;

    static {
        try {
            cls = Class.forName("com.mediatek.common.search.SearchEngine");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public SearchEngine(Object object, Context context) {
        this.object = object;
        this.context = context;
    }

    public String getName() throws Exception {
        Method m = cls.getMethod("getName");
        return (String) m.invoke(object);
    }

    public CharSequence getLabel() throws Exception {
        Method m = cls.getMethod("getLabel");
        return (CharSequence) m.invoke(object);
    }

    public String getSearchUriForQuery(String query) throws Exception {
        Method m = cls.getMethod("getSearchUriForQuery", String.class);
        return (String) m.invoke(object, query);
    }

    public String getSuggestUriForQuery(String query) throws Exception {
        Method m = cls.getMethod("getSuggestUriForQuery", String.class);
        return (String) m.invoke(object, query);
    }

    public boolean supportsSuggestions() throws Exception {
        Method m = cls.getMethod("supportsSuggestions");
        return (boolean) m.invoke(object);
    }

    public String getFaviconUri() throws Exception {
        Method m = cls.getMethod("getFaviconUri");
        return (String) m.invoke(object);
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchEngine that = (SearchEngine) o;
        return Objects.equals(object, that.object);
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }
}
