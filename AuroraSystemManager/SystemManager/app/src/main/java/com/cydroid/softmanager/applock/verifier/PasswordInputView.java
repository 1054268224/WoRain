 package com.cydroid.softmanager.applock.verifier;
 
 /**
  * @author chenml
  * Date 2016-03-25
  * */
 import java.util.List;
 
 import cyee.changecolors.ChameleonColorManager;
 import cyee.widget.CyeeEditText;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.os.Message;
 import android.text.InputFilter;
 import android.text.InputType;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.TypedValue;
 import android.view.ActionMode;
 import android.view.inputmethod.InputMethodManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import static android.graphics.Paint.ANTI_ALIAS_FLAG;
 
 import com.cydroid.softmanager.utils.Log;
 import com.cydroid.softmanager.R;
 
 public class PasswordInputView extends CyeeEditText {
     private static final String TAG = "PasswordInputView";
     
     private static final int DEFAULT_BACKGROUND_COLOR = 0xfffdfdfd;
     private static final int DEFAULT_PAINT_COLOR = 0xaa000000;
     private static final int PASSWORD_LENGTH = 4;
     private static final int  DEFAULT_PASSWORD_RADIUS = 9;
     private static final int MSG_UPDATE = 1;
 
     private int mPasswordColor = DEFAULT_PAINT_COLOR;
     private float mPasswordRadius = DEFAULT_PASSWORD_RADIUS;
 
     private final Paint mPasswordPaint = new Paint(ANTI_ALIAS_FLAG);
     private final Paint mBorderPaint = new Paint(ANTI_ALIAS_FLAG);
     private int mTextLength;
 
     public PasswordInputView(Context context) {
         super(context);
 
         DisplayMetrics dm = getResources().getDisplayMetrics();
         mPasswordRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mPasswordRadius, dm);
 
         int backgroudColor = ChameleonColorManager.isNeedChangeColor() ? 
                 ChameleonColorManager.getBackgroudColor_B1()
                 : DEFAULT_BACKGROUND_COLOR;
         setBackgroundColor(backgroudColor);
          
         mPasswordColor = ChameleonColorManager.isNeedChangeColor() ? 
                 ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1() 
                 : DEFAULT_PAINT_COLOR;
  
         mBorderPaint.setStrokeWidth(3);
         mBorderPaint.setStyle(Style.STROKE);
         mBorderPaint.setColor(mPasswordColor);
         mPasswordPaint.setStrokeWidth(10);
         mPasswordPaint.setStyle(Paint.Style.FILL);
         mPasswordPaint.setColor(mPasswordColor);
 
         setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
 //        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
         setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
 
         setFocusable(true);
         requestFocus();
         InputMethodManager mImm = (InputMethodManager) context.getSystemService(
              Context.INPUT_METHOD_SERVICE);
         mImm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
 
 
         setCustomSelectionActionModeCallback(new ActionMode.Callback() { 
             @Override
             public boolean onPrepareActionMode(ActionMode mode, Menu menu) { 
                 return false;
             } 
             @Override
             public void onDestroyActionMode(ActionMode mode) {  
             } 
             @Override
             public boolean onCreateActionMode(ActionMode mode, Menu menu) { 
                 return false;
             } 
             @Override
             public boolean onActionItemClicked(ActionMode mode, MenuItem item) { 
                 return false;
             }
         });
     }
     
     public PasswordInputView(Context context, AttributeSet attrs) {
         super(context, attrs);
         Log.i(TAG, "   -----  PasswordInputView  -----");
         DisplayMetrics dm = getResources().getDisplayMetrics();
         mPasswordRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mPasswordRadius, dm);
 
         TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0);
         mPasswordColor = a.getColor(R.styleable.PasswordInputView_passwordColor, DEFAULT_PAINT_COLOR);
         mPasswordRadius = a.getDimension(R.styleable.PasswordInputView_passwordRadius, DEFAULT_PASSWORD_RADIUS);
         a.recycle();
 
         int backgroudColor = ChameleonColorManager.isNeedChangeColor() ? 
             ChameleonColorManager.getBackgroudColor_B1()
             : DEFAULT_BACKGROUND_COLOR;
         setBackgroundColor(backgroudColor);
          
         mPasswordColor = ChameleonColorManager.isNeedChangeColor() ? 
             ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()
             : DEFAULT_PAINT_COLOR;
 
         mBorderPaint.setStrokeWidth(3);
         mBorderPaint.setStyle(Style.STROKE);
         mBorderPaint.setColor(mPasswordColor);
         mPasswordPaint.setStrokeWidth(10);
         mPasswordPaint.setStyle(Paint.Style.FILL);
         mPasswordPaint.setColor(mPasswordColor);
 
         setFocusable(true);
         requestFocus();
         InputMethodManager mImm = (InputMethodManager) context.getSystemService(
             Context.INPUT_METHOD_SERVICE);
         mImm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
     }
     
     @Override
     protected void onDraw(Canvas canvas) {
         int width = getWidth();
         int height = getHeight();
         Log.i(TAG, "onDraw---- width  = "+width+"; height = "+height);
         
         float cx, cy = height/ 2.0f;
         float half = width / (float)PASSWORD_LENGTH / 2.0f;
         for (int i = 0; i < mTextLength; i++) {
             cx = width * i / (float)PASSWORD_LENGTH + half;
             Log.i(TAG, "solid circle： No. =" + i + ";  cx  :" + cx + " ; cy : "   + cy);
             canvas.drawCircle(cx, cy, mPasswordRadius, mPasswordPaint);
         }
 
         for (int i = 0; i < PASSWORD_LENGTH - mTextLength; i++) {
             cx = width * (i + mTextLength) / (float)PASSWORD_LENGTH + half;
             //Log.i(TAG, "hollow  circle： No. = " + (i + mTextLength) + ";  cx  :" + cx + " ; cy : " + cy);
             canvas.drawCircle(cx, cy, mPasswordRadius, mBorderPaint);
         }
     }
 
     @Override
     protected void onTextChanged(CharSequence text, int start,  int lengthBefore, int lengthAfter) {
         super.onTextChanged(text, start, lengthBefore, lengthAfter);
         this.mTextLength = text.toString().length();
         Log.i(TAG, "onTextChanged---- textLength  = " + mTextLength);
         invalidate();
         if (mTextLength == PASSWORD_LENGTH) {
             Message mesg = mHandler.obtainMessage(MSG_UPDATE);
             mHandler.removeMessages(MSG_UPDATE);
             mHandler.sendMessage(mesg);
         }
     }
 
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             if (msg.what == MSG_UPDATE) {
                 if (mTextChangeLisenter != null) {
                     try {
                         Thread.sleep(60);
                     } catch (Exception e) {
                     }
                     mTextChangeLisenter.onTextChanged();
                 }
             }
         }
     };
     
     private onTextChangedLisenter mTextChangeLisenter;
     
     public interface onTextChangedLisenter {
         void onTextChanged();
     }
     
     public void setOnTextChangedLisenter(onTextChangedLisenter lisenter) {
         mTextChangeLisenter = lisenter;
     }
 }
 
