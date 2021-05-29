package cyee.forcetouch;

import java.util.List;

import android.content.Context;
import android.view.View;

public class CyeeForceTouchController {

    private final Context mContext;
    private final CyeeForceTouchPreviewController mPreviewController;
    private final CyeeForceTouchQuickMenuController mQuickMenuController;

    public CyeeForceTouchController(Context context) {
        mContext = context;
        Object synObj = new Object();
        mQuickMenuController = new CyeeForceTouchQuickMenuController(context);
        mPreviewController = new CyeeForceTouchPreviewController(context);
        mQuickMenuController.setSynObj(synObj);
        mPreviewController.setSynObj(synObj);
        mPreviewController
                .setForceTouchMenuType(CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW);
    }

    public void setCyeeForceTouchClickCallback(final int type,
            CyeeForceTouchClickCallback callback) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            mPreviewController.setCyeeForceTouchClickCallback(callback);
        } else {
            mQuickMenuController.setCyeeForceTouchClickCallback(callback);
        }
    }

    public CyeeForceTouchClickCallback getCyeeForceTouchClickCallback(
            final int type) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            return mPreviewController.getCyeeForceTouchClickCallback();
        } else {
            return mQuickMenuController.getCyeeForceTouchClickCallback();
        }
    }

    public void setCyeeForceTouchMenuCallback(final int type,
            CyeeForceTouchMenuCallback callback) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            mPreviewController.setCyeeForceTouchMenuCallback(callback);
        } else {
            mQuickMenuController.setCyeeForceTouchMenuCallback(callback);
        }
    }

    public void setForceTouchPreviewCallback(
            CyeeForceTouchPreviewCallback callback) {
        mPreviewController.setForceTouchPreviewCallback(callback);
    }

    public void setCyeeForceTouchControllerCallback(
            CyeeForceTouchControllerCallback callback) {
        mQuickMenuController.setCyeeForceTouchControllerCallback(callback);
    }

    public void registerForceTouchViews(final int type, final List<View> views) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            mPreviewController.registerForceTouchViews(views);
        } else {
            mQuickMenuController.registerForceTouchViews(views);
        }
    }

    public void registerForceTouchView(final int type, final View view) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            mPreviewController.registerForceTouchView(view);
        } else {
            mQuickMenuController.registerForceTouchView(view);
        }
    }

    public void unregisterForceTouchView(View view) {
        mPreviewController.unregisterForceTouchView(view);
        mQuickMenuController.unregisterForceTouchView(view);
    }

    public void cancelForceTouch(View view) {
        mPreviewController.cancelForceTouch(view);
        mQuickMenuController.cancelForceTouch(view);
    }

    public int getForceTouchState(int type) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            return mPreviewController.getTouchState().getValue();
        } else {
            return mQuickMenuController.getTouchState().getValue();
        }
    }

    public void setEnableForceTouch(int type, boolean enable) {
        if (type == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            mPreviewController.setEnableForceTouch(enable);
        } else {
            mQuickMenuController.setEnableForceTouch(enable);
        }
    }

    public void setEnableForceTouch(boolean enable) {
        if (null != mPreviewController) {
            mPreviewController.setEnableForceTouch(enable);
        }
        if (null != mQuickMenuController) {
            mQuickMenuController.setEnableForceTouch(enable);
        }
    }

    public void dismissForceTouchWindow() {
        if (null != mPreviewController) {
            mPreviewController.dismiss();
        }
        if (null != mQuickMenuController) {
            mQuickMenuController.dismiss();
        }
    }
    
    public void dismissForceTouchWindowWithNoAnimation() {
        if (null != mQuickMenuController) {
            mQuickMenuController.dismissWithNoAnimation();            
        }
    }

    public void onDestroyForceTouch() {
        mPreviewController.onDestroy();
        mQuickMenuController.onDestroy();
        CyeeForceTouchConfig.getInstance(mContext).onDestroyForceTouchConfig();
    }
}
