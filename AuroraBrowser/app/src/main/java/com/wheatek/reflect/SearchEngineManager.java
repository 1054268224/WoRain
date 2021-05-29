package com.wheatek.reflect;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SearchEngineManager {
    public static final String SEARCH_ENGINE_SERVICE = getConst("SEARCH_ENGINE_SERVICE");
    private static Class cls;
    private Object object;
    private Context context;

    public SearchEngineManager(Context context) {
        this.context = context;
        object = context.getSystemService(SearchEngineManager.SEARCH_ENGINE_SERVICE);
    }

    static {
        try {
            cls = Class.forName("com.mediatek.search.SearchEngineManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getConst(String name) {
        try {
            Class<?> cls = Class.forName("com.mediatek.search.SearchEngineManager");
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public SearchEngine getDefault() throws Exception {
        Method m = cls.getMethod("getDefault");
        return new SearchEngine(m.invoke(object), context);
    }

    public SearchEngine getByName(String name) throws Exception {
        Method m = cls.getMethod("getByName", String.class);
        return new SearchEngine(m.invoke(object, name), context);
    }

    public SearchEngine getBestMatch(String s, String searchEngineFavicon) throws Exception {
        Method m = cls.getMethod("getBestMatch", String.class, String.class);
        return new SearchEngine(m.invoke(object, s, searchEngineFavicon), context);
    }

    public List<SearchEngine> getAvailables() throws Exception {
        Method m = cls.getMethod("getAvailables");
        List<Object> real = (List<Object>) m.invoke(object);
        List<SearchEngine> re = new ArrayList<>();
        for (Object o : real) {
            re.add(new SearchEngine(o, context));
        }
        return re;
    }
}
