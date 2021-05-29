package cyee.widget;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.view.View;
import android.view.View.OnClickListener;
//Gionee <xuhz> <2013-07-19> add for CR00838236 begin
import android.view.Gravity;
import cyee.widget.CyeeButton;
import android.widget.LinearLayout;
import com.cyee.internal.R;
//Gionee <xuhz> <2013-07-19> add for CR00838236 end

/**
 * 
 * @author liushuai
 *
 */
public class CyeeSmartFullScreenLayout extends CyeeSmartLayout implements OnClickListener {
	
	public static String USER_DEGREE = "user_full_screen_guide_degree";
	
	private final long TIME_INTERNAL = 3 * 1000;

	public CyeeSmartFullScreenLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public CyeeSmartFullScreenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	
//	public SmartFullScreenLayout(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle, USER_DEGREE);
//		init();
//	}

	private void init() {
		// TODO Auto-generated method stub
		setOnClickListener(this);
		
		//Gionee <xuhz> <2013-07-19> add for CR00838236 begin
		initPositiveBtn();
		//Gionee <xuhz> <2013-07-19> add for CR00838236 end
	}

	@Override
	public void onClick(View v) {
		//Gionee <xuhz> <2013-07-19> modify for CR00838236 begin
		if (mShowPositiveBtn) {
			return;
		}
		onClickEvent(v);
		//Gionee <xuhz> <2013-07-19> modify for CR00838236 end
	}

	@Override
	protected void processUserDegree(){
		if(mGoneTime - mVisibleTime > TIME_INTERNAL){
			mUserDegree -= (120 - mSmartLayoutDegree)/6;
			if(mUserDegree < 0){
				mUserDegree = 0;
			}
		} else {
			mUserDegree += mSmartLayoutDegree/6;
			if(mUserDegree >= 100){
				mUserDegree = 99;
			}
		}		
		saveUserDegree(mUserDegree);
	}
	
	@Override
	protected int getUserDegree() {
		// TODO Auto-generated method stub
		int userDegree = LOW_DEGREE;
		try {
			userDegree = Settings.System.getInt(mContext.getContentResolver(), USER_DEGREE);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			Settings.System.putInt(mContext.getContentResolver(), USER_DEGREE, LOW_DEGREE);
			e.printStackTrace();
		}
		return userDegree;
	}

	@Override
	protected boolean saveUserDegree(int userDegree) {
		// TODO Auto-generated method stub
		return Settings.System.putInt(mContext.getContentResolver(), USER_DEGREE, userDegree);
	}

	//Gionee <xuhz> <2013-07-19> add for CR00838236 begin
	private boolean mShowPositiveBtn = false;
	private CyeeButton mPositiveBtn;
	private LinearLayout mLayoutView;
	private LayoutParams mParams;
	
	public void setPositiveBtnVisible(boolean flg) {
		if (mLayoutView == null) {
			return;
		}
		
		mShowPositiveBtn = flg;
		
		if (mShowPositiveBtn) {
			mLayoutView.setVisibility(View.VISIBLE);
		} else {
			mLayoutView.setVisibility(View.GONE);
		}
	}
	
	private void initPositiveBtn() {
		mLayoutView = new LinearLayout(mContext);
		mPositiveBtn = new CyeeButton(mContext);
		mPositiveBtn.setText(com.cyee.internal.R.string.cyee_smart_full_btn_label);
		mPositiveBtn.setButtonStyle(CyeeButton.BUTTON_RECOM_STYLE);
		
		mPositiveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickEvent(v);
			}
		});
		
		mLayoutView.setOrientation(VERTICAL);
		LayoutParams buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
		mLayoutView.addView(mPositiveBtn, buttonParams);
		
		mParams = new LayoutParams(LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT);
		mParams.gravity = Gravity.BOTTOM;
		mParams.bottomMargin = mContext.getResources().getDimensionPixelSize(com.cyee.internal.R.dimen.cyee_smart_full_btn_bottom_margin);
		addView(mLayoutView, mParams);
		
		setPositiveBtnVisible(true);
	}
	
	// Gionee <zhangxx> <2013-12-04> add for CR00967397 begin
	public CyeeButton getOperateButton() {
		return mPositiveBtn;
	}
	// Gionee <zhangxx> <2013-12-04> add for CR00967397 end

	private void onClickEvent(View v) {
		setVisibility(View.GONE);
		mGoneTime = System.currentTimeMillis();
		Log.d("test", "mGoneTime = " + mGoneTime);
		processUserDegree();
	}
	
	@Override
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		super.setVisibility(visibility);
		
		if (mLayoutView == null) {
			return;
		}
		
		if (mShowPositiveBtn) {
			mLayoutView.setVisibility(View.VISIBLE);
		} else {
			mLayoutView.setVisibility(View.GONE);
		}
	}
	//Gionee <xuhz> <2013-07-19> add for CR00838236 end
}
