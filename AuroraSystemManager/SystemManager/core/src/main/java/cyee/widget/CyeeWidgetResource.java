package cyee.widget;

import android.content.Context;
import com.cyee.utils.Log;

public class CyeeWidgetResource {

    public static int getIdentifierById(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "id", context.getPackageName());
    }
    
    public static int getIdentifierByDrawable(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "drawable", context.getPackageName());
    }
    
    public static int getIdentifierByLayout(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "layout", context.getPackageName());
    }
    
    public static int getIdentifierByAnim(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "anim", context.getPackageName());
    }
    
    public static int getIdentifierByAttr(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "attr", context.getPackageName());
    }
    
    public static int getIdentifierByBool(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "bool", context.getPackageName());
    }
    
    public static int getIdentifierByColor(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "color", context.getPackageName());
    }
    
    public static int getIdentifierByDimen(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "dimen", context.getPackageName());
    }
    
    public static int getIdentifierByString(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "string", context.getPackageName());
    }
    
    public static int getIdentifierByStyle(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "style", context.getPackageName());
    }

    /**
     * get the ID of an Integer resource
     *
     * @param context
     *            the Context of the Integer resource
     * @param idName
     *            the name of the resource
     * @return the ID of the resource
     */
    public static int getIdentifierByInteger(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "integer", context.getPackageName());
    }
}
