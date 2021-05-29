package cyee.widget;


import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.ChangeColorUtil;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.widget.TextView;

public class CyeeTextView extends TextView {
	
	private static final String TAG = "CyeeTextView";
	
	public CyeeTextView(Context context) {
		this(context, null);
	}

	public CyeeTextView(Context context, AttributeSet attrs) {
		this(context, attrs, com.android.internal.R.attr.textViewStyle);

	}

	public CyeeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public CyeeTextView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		
		changeColor();
		
	}

	private void changeColor() {
		if(ChameleonColorManager.isNeedChangeColor(mContext)){

			ChangeColorUtil.changeTextViewTextColor(this);
			
		}
	}

}
