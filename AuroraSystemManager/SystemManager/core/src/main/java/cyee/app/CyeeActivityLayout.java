package cyee.app;

import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import com.cyee.utils.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

/**
 * 此类用于在application里面提前初始化控件布局时，获取控件布局的id
 * 
 * @author weidong
 */
public class CyeeActivityLayout {

    private static final String TAG = "CyeeActivityLayout";

    private static CyeeActivityLayout mInstance;
    private final Context mCxt;

    public enum LayoutType {
        DialogType(1), OverlayType(2), NormalType(3);

        private final int mVal;

        LayoutType(int val) {
            mVal = val;
        }

        public int getVal() {
            return this.mVal;
        }
    }

    /**
     * 获取当前类的单例对象
     * 
     * @param cxt
     * @return 类的单例对象
     */
    public static CyeeActivityLayout getInstance(Context cxt) {
        if (null == mInstance) {
            synchronized (CyeeActivityLayout.class) {
                if (null == mInstance) {
                    mInstance = new CyeeActivityLayout(cxt);
                }
            }
        }

        return mInstance;
    }

    private CyeeActivityLayout(Context cxt) {
        mCxt = cxt;
    }

    /**
     * 根据传入的类型，获取不同的控件布局资源id
     * 
     * @param typeId
     *            1普通的布局; 2dialog形式的布局 ; 3覆盖模式的布局
     * @return 控件布局的资源id
     */
    public int getActivityLayoutId(LayoutType typeId) {
        int resId = com.cyee.internal.R.layout.cyee_screen_action_bar;

        switch (typeId) {
        case DialogType:
            resId = com.cyee.internal.R.layout.cyee_screen_dialog;
            break;
        case OverlayType:
            resId = com.cyee.internal.R.layout.cyee_screen_action_bar_overlay;
            break;
        case NormalType:
        default:
            resId = com.cyee.internal.R.layout.cyee_screen_action_bar;
            break;
        }

        return resId;
    }

    /**
     * 提前获取actionbar视图容器
     * 
     * @param cxt
     * @param cyeeActivityLayout
     *            CyeeActivity的容器布局
     * @return actionbarView的容器
     */
    public ViewGroup getActionBarContainerView(Context cxt, ViewGroup cyeeActivityLayout) {
        ViewStub viewStub = (ViewStub) cyeeActivityLayout.findViewById(com.cyee.internal.R.id.cyee_actionbar_container_stub);
        ViewGroup mActionbarContainerView = null;
        if (viewStub != null) {
            mActionbarContainerView = (ViewGroup) viewStub.inflate();
        }

        return mActionbarContainerView;
    }

}
