package cyee.widget;

import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;


/**
 * Represents a push-button widget. Push-buttons can be
 * pressed, or clicked, by the user to perform an action.

 * <p>A typical use of a push-button in an activity would be the following:
 * </p>
 *
 * <pre>
 * public class MyActivity extends Activity {
 *     protected void onCreate(Bundle icicle) {
 *         super.onCreate(icicle);
 *
 *         setContentView(com.cyee.internal.R.layout(this,"content_layout_id"));
 *
 *         final CyeeButton button = (CyeeButton) findViewById(R.id.button_id);
 *         button.setOnClickListener(new View.OnClickListener() {
 *             public void onClick(View v) {
 *                 // Perform action on click
 *             }
 *         });
 *     }
 * }</pre>
 *
 * <p>However, instead of applying an {@link android.view.View.OnClickListener OnClickListener} to
 * the button in your activity, you can assign a method to your button in the XML layout,
 * using the {@link android.R.attr#onClick android:onClick} attribute. For example:</p>
 *
 * <pre>
 * &lt;CyeeButton
 *     android:layout_height="wrap_content"
 *     android:layout_width="wrap_content"
 *     android:text="@string/self_destruct"
 *     android:onClick="selfDestruct" /&gt;</pre>
 *
 * <p>Now, when a user clicks the button, the Android system calls the activity's {@code
 * selfDestruct(View)} method. In order for this to work, the method must be public and accept
 * a {@link android.view.View} as its only parameter. For example:</p>
 *
 * <pre>
 * public void selfDestruct(View view) {
 *     // Kabloey
 * }</pre>
 *
 * <p>The {@link android.view.View} passed into the method is a reference to the widget
 * that was clicked.</p>
 *
 * <h3>CyeeButton style</h3>
 *
 * <p>Every CyeeButton is styled using the system's default button background, which is often different
 * from one device to another and from one version of the platform to another. If you're not
 * satisfied with the default button style and want to customize it to match the design of your
 * application, then you can replace the button's background image with a <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html#StateList">state list drawable</a>.
 * A state list drawable is a drawable resource defined in XML that changes its image based on
 * the current state of the button. Once you've defined a state list drawable in XML, you can apply
 * it to your CyeeButton with the {@link android.R.attr#background android:background}
 * attribute. For more information and an example, see <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html#StateList">State List
 * Drawable</a>.</p>
 *
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/button.html">Buttons</a>
 * guide.</p>
 *
 * <p><strong>XML attributes</strong></p>
 * <p>
 * See {@link android.R.styleable#CyeeButton CyeeButton Attributes},
 * {@link android.R.styleable#TextView TextView Attributes},
 * {@link android.R.styleable#View View Attributes}
 * </p>
 */
@RemoteView
public class CyeeButton extends TextView {
	
	private static final String TAG = "CyeeButton";
	
    private final float mSmallFontSize;
    private int mOldBtnWidth;
    private int mOldBtnHeight;
    private final boolean isSetBackground;
    
    public CyeeButton(Context context) {
        this(context, null);
    }
    
    public CyeeButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public CyeeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getCyeeButtonStyle(context, attrs);
        setCyeeButtonBg(context);
        setBackgroundColorFilter(mBgColor);
        isSetBackground = isSetBackground(context);
        mSmallFontSize = getResources().getDimension(com.cyee.internal.R.dimen.cyee_loading_button_small_size);
        changeColor();
    }

    /**
     * param color is value of res, not res id
     */
    public void setBackgroundColorFilter(int color) {
        Drawable bgDrawable = getBackground();
        int defaultColor = getResources().getColor(com.cyee.internal.R.color.cyee_button_normal_bg_color);
        if (color == defaultColor) {
            return ;
        }
        bgDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
    
    private void setCyeeButtonBg(Context context) {
        if (mButtonStyle == BUTTON_ROUND_CORNER_STYLE) {
            setBackground(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_btn_round_corner_default_bg_ripple,context.getTheme()));
            setMinHeight((int)getResources().getDimension(com.cyee.internal.R.dimen.cyee_round_corner_button__corner_radius) * 2);
        }
    }

    private void getCyeeButtonStyle(Context context, AttributeSet attrs) {
        int start = com.cyee.internal.R.attr.cyee_button_style;
        int bgColor = com.cyee.internal.R.attr.cyee_button_bg_color;
        int[] styleable = {start, bgColor};

        TypedArray typedArray = context.obtainStyledAttributes(attrs, styleable,
                com.cyee.internal.R.attr.cyeeButtonStyle, 0);

        mButtonStyle = typedArray.getInt(R.styleable.CyeeButtonStyle_cyee_button_style, BUTTON_NORMAL_STYLE);
        mBgColor = typedArray.getColor(R.styleable.CyeeButtonStyle_cyee_button_bg_color, getResources().getColor(com.cyee.internal.R.color.cyee_button_normal_bg_color));
        
        typedArray.recycle();
    }

    private boolean isSetBackground(Context context) {
        boolean ret = false;

        if(!(ChameleonColorManager.getInstance().getCyeeThemeType(context) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME)) {
            return ret;
        }

        Drawable[] componet = getCompoundDrawables();
        for (int i = 0; i < componet.length; i++) {
            if(null != componet[i]) {
                return true;
            }
        }
        componet = getCompoundDrawablesRelative();
        for (int i = 0; i < componet.length; i++) {
            if(null != componet[i]) {
                return true;
            }
        }
        
        return ret;
    }
    
    private void changeColor() {
        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            if (!isSetBackground) {
                Drawable background = getBackground();
                if (background != null && background instanceof RippleDrawable) {
                    RippleDrawable rippleDrawable = ((RippleDrawable) background);
                    int nums = rippleDrawable.getNumberOfLayers();
                    if(nums >= 1) {
                        int id = rippleDrawable.getId(0);
                        int defaultId = com.cyee.internal.R.id.cyee_button_item;
                        if (id == defaultId) {
                            setBackground(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_button_bg_selector));
                        }
                    }
                }
            }
            if(ChameleonColorManager.isNeedChangeColor(mContext)) {
                ColorStateList textColors = new ColorStateList(new int[][] {
                        { - android.R.attr.state_enabled },
                        { android.R.attr.state_enabled }
                }, 
                new int[] {
                        ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3(), 
                        ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()
                });
                setTextColor(textColors);
            }
        } else if(ChameleonColorManager.isNeedChangeColor(mContext)){

            Drawable backgroundDrawable = getBackground();

            if(backgroundDrawable != null && backgroundDrawable instanceof RippleDrawable){

                RippleDrawable rippleDrawable = ((RippleDrawable) backgroundDrawable);
                rippleDrawable.setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));

                if(rippleDrawable.getNumberOfLayers() > 0){
                    Drawable drawable = rippleDrawable.getDrawable(0);
                    if(drawable != null && drawable instanceof StateListDrawable){
                        changeStateListDrawable((StateListDrawable) drawable);
                    } else {      			
                        rippleDrawable.setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), PorterDuff.Mode.SRC_IN);
                    }     			
                } else {      			
                    rippleDrawable.setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), PorterDuff.Mode.SRC_IN);
                } 
            }

            ColorStateList textColors = new ColorStateList(new int[][] {
                    { - android.R.attr.state_enabled },
                    { android.R.attr.state_enabled }
            }, 
            new int[] {
                    ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3(), 
                    ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()
            });
            setTextColor(textColors);
        }
    }

    private void changeStateListDrawable(StateListDrawable stateListDrawble) {
        for (int index = 0; index < stateListDrawble.getStateCount(); index++) {
            Drawable drawable = stateListDrawble.getStateDrawable(index);
            if (drawable instanceof LayerDrawable) {
                LayerDrawable layerdrawable = (LayerDrawable) drawable;

                int cnt = layerdrawable.getNumberOfLayers();
                if (cnt >= 2) {
                    Drawable layout1 = layerdrawable.getDrawable(1);
                    layout1.setColorFilter(
                            ChameleonColorManager.getPopupBackgroudColor_B2(),
                            PorterDuff.Mode.SRC_IN);
                }
            } else {
                drawable.setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), PorterDuff.Mode.SRC_IN);
            }
        }
    }
	
	private boolean stateIsDisable(int[] myDrawableState) {
        for(int index = 0; index < myDrawableState.length; index ++){
        	if(myDrawableState[index] == - android.R.attr.state_enabled){
        		return true;
        	}
        }
        return false;
	}

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CyeeButton.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CyeeButton.class.getName());
    }

    // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
    public static final int BUTTON_NORMAL_STYLE = 0;
    public static final int BUTTON_RECOM_STYLE = 1;
    public static final int BUTTON_CONTRA_STYLE = 2;
    public static final int BUTTON_LOADING_INFINITY_STYLE = 4;
    public static final int BUTTON_LOADING_STYLE = 5;
    public static final int BUTTON_ROUND_CORNER_STYLE = 6;
    private int mButtonStyle;
    private int mBgColor;
    
//    private Drawable mOldBackgrounddraDrawable;
    private CharSequence mOldText;
    private ColorStateList mOldTextColorStateList;
    private AnimationDrawable mAnimationDrawable;

    public void setButtonStyle(int style) {
        mButtonStyle = style;

        switch (style) {
            case BUTTON_ROUND_CORNER_STYLE:
                setRoundCornerButton();
                break;
            case BUTTON_NORMAL_STYLE:
                break;
            case BUTTON_RECOM_STYLE:
                // Gionee <gaoj> <2013-9-13> add for CR00899138 begin
//                setTextColor(mContext.getResources().getColor(R.color.cyee_button_text_color));
                // Gionee <gaoj> <2013-9-13> add for CR00899138 end
//                setBackgroundResource(R.drawable.cyee_btn_recom);
                break;
            case BUTTON_CONTRA_STYLE:
                break;
            case BUTTON_LOADING_INFINITY_STYLE:
            case BUTTON_LOADING_STYLE:
//                setBackgroundResource(R.drawable.cyee_btn_loading);
                break;
            default:
                break;
        }
    }
    // Gionee <zhangxx> <2013-05-31> add for CR00811583 end

    // Gionee <fenglp> <2013-08-02> modify for CR00812456 begin
//    @Override
//    public void setOnClickListener(OnClickListener l) {
//        if (mButtonStyle == BUTTON_LOADING_STYLE || mButtonStyle == BUTTON_LOADING_INFINITY_STYLE) {
//            final OnClickListener oriListener = l;
//            OnClickListener wrapOnClickListener = new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mOldBtnWidth == 0 || mOldBtnHeight == 0) {
//                        mOldBtnWidth = getWidth();
//                        mOldBtnHeight = getHeight();
//                    }
//                    if (mOldText == null) {
//                        mOldText = getText();
//                    }
//                    if (mOldTextColorStateList == null) {
//                        mOldTextColorStateList = getTextColors();
//                    }
//                    if (mButtonStyle == BUTTON_LOADING_INFINITY_STYLE) {
////                    setTextColor(android.R.color.transparent);
//                        setText("");
//                        setBackgroundResource(R.drawable.cyee_btn_loading_bg);
//                        mAnimationDrawable = (AnimationDrawable) getBackground();
//                        mAnimationDrawable.start();
//                    }
//                    oriListener.onClick(v);
//                }
//            };
//            super.setOnClickListener(wrapOnClickListener);
//        } else {
//            super.setOnClickListener(l);
//        }
//    }

    private void setRoundCornerButton() {
        setCyeeButtonBg(getContext());
        changeColor();
    }

    public void setUpdate(int val) {
//        setTextColor(getResources().getColor(R.color.cyee_loading_button_text_color));
//        String valStr = val + "%";
//        Spannable text = new SpannableString(valStr);
//        int index = valStr.indexOf("%");
//        text.setSpan(new AbsoluteSizeSpan((int) mSmallFontSize), index, valStr.length(),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        setText(text);
    }

    public void reset() {
//        if (mOldText != null) {
//            setText(mOldText);
//        }
//        if (mOldTextColorStateList != null) {
//            setTextColor(mOldTextColorStateList);
//        }
//        setBackgroundResource(R.drawable.cyee_btn_loading);
//        if (mAnimationDrawable != null) {
//            mAnimationDrawable.stop();
//        }
    }
    // Gionee <fenglp> <2013-08-02> modify for CR00812456 end
}
