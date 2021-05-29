package com.odm.tool.protractor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.odm.tool.R;

public class ProtractorBackView extends View {

    private int mViewWidth,mViewHeight; //控件的宽和高
    private Context mContext;
    private Bitmap mBitmapProtractor;


    public ProtractorBackView(Context context) {
        this(context,null);
    }

    public ProtractorBackView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ProtractorBackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext =context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mBitmapProtractor = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.protractor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mViewWidth/2,mViewHeight - 16);//将画布中心移动到控件底部中间
        drawProtractor(canvas);
    }

    private void drawProtractor(Canvas canvas) {
        canvas.drawBitmap(mBitmapProtractor,-mBitmapProtractor.getWidth()/2,-mBitmapProtractor.getHeight() + 15 ,null);
        canvas.save();
    }

}
