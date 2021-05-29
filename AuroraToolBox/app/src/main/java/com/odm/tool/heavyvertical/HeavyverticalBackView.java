package com.odm.tool.heavyvertical;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

import com.odm.tool.R;

public class HeavyverticalBackView extends View {

    private int mViewWidth,mViewHeight; //控件的宽和高
    private Context mContext;
    private Bitmap mBitmapProtractor;


    public HeavyverticalBackView(Context context) {
        this(context,null);
    }

    public HeavyverticalBackView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HeavyverticalBackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext =context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        Bitmap protractor = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.plumb_bg);
        Matrix matrix = new Matrix();
        matrix.setScale(0.925f,0.925f,protractor.getWidth()/2, protractor.getHeight()/2 );
        mBitmapProtractor = Bitmap.createBitmap(protractor,0,0,protractor.getWidth(), protractor.getHeight(),matrix,true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mViewWidth/2, mBitmapProtractor.getHeight());//将画布中心移动到控件底部中间
        drawProtractor(canvas);
    }

    private void drawProtractor(Canvas canvas) {
        canvas.drawBitmap(mBitmapProtractor,-mBitmapProtractor.getWidth()/2,-mBitmapProtractor.getHeight() ,null);
        canvas.save();
    }

}
