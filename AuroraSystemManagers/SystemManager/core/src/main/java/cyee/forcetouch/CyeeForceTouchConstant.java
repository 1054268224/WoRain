package cyee.forcetouch;

public class CyeeForceTouchConstant {
    public final static int MENU_TYPE_QUICK_MENU = 1;
    public final static int MENU_TYPE_CONTENT_PREVIEW = MENU_TYPE_QUICK_MENU + 1;
    public final static int MENU_TYPE_DESKTOP_QUICK_MENU = MENU_TYPE_CONTENT_PREVIEW + 1;
    
    public final static int PREVIEW_INDICATOR_ANIM_TIME = 200;
    public final static int MENU_ANIM_TIME = 200;
    public final static int SMS_MENU_ANIM_TIME = 200;
    public final static int DISMISS_WIN_ANIM_TIME = 100;
    
    public final static int CONTENT_PREVIEW_HEIGHT = 455;//dp
    public final static int QUICK_MENU_PREVIEW_HEIGHT = 64; //dp
    public final static int MENU_ITEM_HEIGHT = 64; //dp
    public final static int DESK_TOP_MENU_WIDTH = 230; //dp
    
    public final static int PREVIEW_PADDING = 10; //dp
    
    public final static int MOCK_LONG_PRESS_TIME = 2500; //ms
    
    public final static int MENU_DISPLAY_MOVE_DISTANCE = 38; //dp
    public final static int MENU_DISPEAR_MOVE_DISTANCE = 20; //dp
    
    public final static int MIN_FLING_VELOCITY = 500; //dp/s
    
    public final static int MENU_SORT_ORDER = 0;
    public final static int MENU_SORT_REVERT_ORDER = MENU_SORT_ORDER + 1;
    
    public final static int MIN_FLING_DISTANCE = 250; //px
    
    public final static int TOUCH_PREVIEW_DELAY_TIME = 200; //ms
    
    public final static int PREVIEW_INDICATOR_GONE_DISTANCE = 15; // dp
    
    public static final String EFFECT_NAME_LOCKSCREEN_UNLOCK_CODE_TAP = "LOCKSCREEN_UNLOCK_CODE_TAP";
    public static final String EFFECT_NAME_VIRTUAL_KEY_LONG_PRESS = "VIRTUAL_KEY_LONG_PRESS";
    public static final String BUTTON_ON = "BUTTON_ON";
    public static final String BUTTON_OFF = "BUTTON_OFF";
    public static final String LOCKSCREEN_UNLOCK_CODE_ERROR = "LOCKSCREEN_UNLOCK_CODE_ERROR";
    public static final long VIBRATE_TIME_SHORT = 150;
    public static final long VIBRATE_DELAY = 100;
    public static final int UNFOLD_SUBMENU_ANIM_TIME = 300;
}
