package com.example.systemmanageruidemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.example.systemmanageruidemo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartView extends View {
    private static final long DEFALUTY = 10000;
    private static final long DEFAULTX = 10000;
    private int mwidth;
    private int mheight;
    /**
     * 坐标轴与数据间距
     */
    public static int leftmargin = 10;
    /**
     * 坐标轴与数据间距
     */
    public static int topmargin = 5;
    /**
     * 坐标轴与数据间距
     */
    public static int rightmargin = 5;
    /**
     * 坐标轴与数据间距
     */
    public static int bottommargin = 8;
    /**
     * 数据集
     */
    private List<Info> mInfos;

    {
        setmInfos(testinfo());
    }

    private List<Info> testinfo() {
        List<Info> re = new ArrayList<>();
        re.add(new Info(0, 0));
        re.add(new Info(1, 2));
        re.add(new Info(3, 5));
        re.add(new Info(4, 10));
        re.add(new Info(6, 3));
        re.add(new Info(7, 7));
        re.add(new Info(8, 2));
        re.add(new Info(5, 1));

        return re;
    }

    private boolean noneedrefreshinfo;

    public List<Info> getmInfos() {
        return mInfos;
    }

    public void setMchartConfig(ChartViewConfig mchartConfig) {
        this.mchartConfig = mchartConfig;
        init();
        if (isAttachedToWindow())
            invalidate();
    }

    public void setmInfos(List<Info> mInfos) {
        this.mInfos = mInfos;
        Collections.sort(mInfos, (o1, o2) -> {
            return ((int) (o1.x - o2.x));
        });
        if (isAttachedToWindow())
            invalidate();
    }

    public ChartViewConfig getMchartConfig() {
        return mchartConfig;
    }

    public static class Info {
        public long x;
        public long y;

        public Info(long x, long y) {
            this.x = x;
            this.y = y;
        }
    }

    public interface TextFormatter {
        String textXFormatter(long data);

        String textYFormatter(long data);

        String textXAndYFormatter(long x, long y);
    }

    public static class ChartViewConfig {
        public int showXcount = 2;
        public int showYcount = 5;
        public int showXtextColor = Color.GRAY;
        public int showYtextColor = Color.GRAY;
        public int lineColor = Color.BLUE;
        public int[] gradienColor = new int[]{Color.RED, Color.GREEN};
        public int extraColor;
        public int coordinateColor = Color.BLACK;
        public float showXtextSize = 12;
        public float showYtextSize = 12;
        public float lineSize = 3;
        public float extraSize = 12;
        public float coordinateSize = 5;
        Context mcontext;
        public TextFormatter textFormatter = new TextFormatter() {
            @Override
            public String textXFormatter(long data) {
                return data + "";
            }

            @Override
            public String textYFormatter(long data) {
                return data + "";
            }

            @Override
            public String textXAndYFormatter(long x, long y) {
                return "(" + x + "," + Y + ")";
            }
        };
        public boolean isShowExtra = false;

        ChartViewConfig(Context context) {
            this.mcontext = context;
            showXtextColor = mcontext.getResources().getColor(R.color.textcolor_hint_wyh_traf, null);
            showYtextColor = mcontext.getResources().getColor(R.color.textcolor_hint_wyh_traf, null);
            lineColor = mcontext.getResources().getColor(R.color.colorPrimary_wyh_traf, null);
            gradienColor = new int[]{mcontext.getResources().getColor(R.color.gradient_end_chart, null),
                    mcontext.getResources().getColor(R.color.gradient_start_chart, null)};
            coordinateColor = mcontext.getResources().getColor(R.color.textcolor_hint_wyh_traf, null);
            showXtextSize = mcontext.getResources().getDisplayMetrics().density * 10;
            showYtextSize = mcontext.getResources().getDisplayMetrics().density * 10;
            lineSize = mcontext.getResources().getDisplayMetrics().density * 1;
            extraSize = mcontext.getResources().getDisplayMetrics().density * 20;
            coordinateSize = mcontext.getResources().getDisplayMetrics().density * 1;
        }
    }

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Paint textXPaint, textYPaint, linePaint, gradientPaint, extraPaint, coordinatePaint;

    ChartViewConfig mchartConfig = new ChartViewConfig(mContext);

    private void init() {
        noneedrefreshinfo = false;
        textXPaint = new Paint();
        textYPaint = new Paint();
        linePaint = new Paint();
        gradientPaint = new Paint();
        extraPaint = new Paint();
        coordinatePaint = new Paint();
        if (mchartConfig != null) {
            textXPaint.setColor(mchartConfig.showXtextColor);
            textXPaint.setTextSize(mchartConfig.showXtextSize);
            textYPaint.setColor(mchartConfig.showYtextColor);
            textYPaint.setTextSize(mchartConfig.showYtextSize);
            textYPaint.setTextAlign(Paint.Align.RIGHT);
            linePaint.setColor(mchartConfig.lineColor);
            linePaint.setStrokeWidth(mchartConfig.lineSize);
            extraPaint.setColor(mchartConfig.extraColor);
            extraPaint.setTextSize(mchartConfig.extraSize);
            coordinatePaint.setColor(mchartConfig.coordinateColor);
            coordinatePaint.setStrokeWidth(mchartConfig.coordinateSize);
            if (mchartConfig.gradienColor != null) {
                float[] p = new float[mchartConfig.gradienColor.length];
                for (int i = 0; i < mchartConfig.gradienColor.length; i++) {
                    p[i] = 0.0f + i * (mheight / (mchartConfig.gradienColor.length - 1));
                }
                LinearGradient lg = new LinearGradient(0, 0, 0, mheight, mchartConfig.gradienColor, p, Shader.TileMode.CLAMP);
                gradientPaint.setShader(lg);
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mwidth = getWidth();
        mheight = getHeight();
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initmInfos(mInfos);
        drawcoordinatePaint(canvas);
//        if(mInfos.size()<2){
//            return;
//        }
        drawGradient(canvas);
        calmyLine();
        drawTransGradient(canvas);
        drawmyLine(canvas);
//        drawExtra(canvas);
    }

    private void drawTransGradient(Canvas canvas) {
        int width = realcooywidth;
        int height = realcooyheight;
        Bitmap curPic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < curPic.getWidth(); i++) {
            for (int i1 = 0; i1 < curPic.getHeight(); i1++) {
                if (!upline(i + eenx, i1 + eeny)) {
                    curPic.setPixel(i, i1, Color.WHITE);
                }
            }
        }
        canvas.drawBitmap(curPic, eenx, eeny, new Paint());

    }

    private void drawExtra(Canvas canvas) {
    }

    List<String> sy, sx;
    float[] xx, xy, yx, yy;
    int realcooywidth;
    int realcooyheight;
    int startXline;
    int endYline;
    long ymax, ymin, xmax, xmin;
    float eenx, eeny;
    int offsetx, offsety;

    private void initmInfos(List<Info> mInfos) {
        if (mInfos == null || mchartConfig == null || mchartConfig.showYcount < 1 || mchartConfig.showYcount < 1 || noneedrefreshinfo)
            return;
        noneedrefreshinfo = true;

        if (mInfos.size() == 0) {
            mInfos.add(new Info(0, 0));
        }
        ymax = Collections.max(mInfos, (o1, o2) -> ((int) (o1.y - o2.y))).y;
        ymin = Collections.min(mInfos, (o1, o2) -> ((int) (o1.y - o2.y))).y;
        xmax = Collections.max(mInfos, (o1, o2) -> ((int) (o1.x - o2.x))).x;
        xmin = Collections.min(mInfos, (o1, o2) -> ((int) (o1.x - o2.x))).x;
        if (ymax == ymin) {
            ymax = DEFALUTY;
        }
        if (xmax == xmin) {
            xmax = DEFAULTX;
        }
        long[] py = new long[mchartConfig.showYcount];
        sy = new ArrayList();
        for (int i = 0; i < mchartConfig.showYcount; i++) {
            py[py.length - 1 - i] = ymin + i * (ymax - ymin) / (mchartConfig.showYcount - 1);
        }
        for (long n : py) {
            sy.add(mchartConfig.textFormatter.textYFormatter(n));
        }
        long[] px = new long[mchartConfig.showXcount];
        sx = new ArrayList();
        for (int i = 0; i < mchartConfig.showXcount; i++) {
            px[i] = xmin + i * (xmax - xmin) / (mchartConfig.showXcount - 1);
            sx.add(mchartConfig.textFormatter.textXFormatter(px[i]));
        }

        String maxlengthstr = Collections.max(sy, (o1, o2) -> o1.length() - o2.length());
        String maxlengthstr2 = Collections.max(sx, (o1, o2) -> o1.length() - o2.length());
        Rect YRect = new Rect();
        Rect XRect = new Rect();
        textYPaint.getTextBounds(maxlengthstr, 0, maxlengthstr.length(), YRect);
        textXPaint.getTextBounds(maxlengthstr2, 0, maxlengthstr2.length(), XRect);

        // 计算每一个坐标轴数据的起始坐标
        yx = new float[mchartConfig.showYcount];
        yy = new float[mchartConfig.showYcount];
        startXline = ((int) (getResources().getDisplayMetrics().density * leftmargin + 0.5f) + YRect.width());
        float ri = getResources().getDisplayMetrics().density * rightmargin;
        eenx = startXline + getResources().getDisplayMetrics().density * rightmargin;
        eeny = getResources().getDisplayMetrics().density * bottommargin;
        realcooyheight = mheight - ((int) (getResources().getDisplayMetrics().density * topmargin + 0.5f) + XRect.height()) - 2 * ((int) eeny);
        offsety = 2 * ((int) eeny);
        endYline = realcooyheight + offsety;
        for (int i = 0; i < mchartConfig.showYcount; i++) {
            yy[i] = XRect.height() + eeny + i * ((realcooyheight - XRect.height()) / (mchartConfig.showYcount - 1));
            yx[i] = YRect.width();
        }
        xx = new float[mchartConfig.showXcount];
        xy = new float[mchartConfig.showXcount];
        realcooywidth = mwidth - ((int) eenx) - ((int) ri) - (XRect.width() / 2);
        offsetx = XRect.width() / 2;
        for (int i = 0; i < mchartConfig.showXcount; i++) {
            xx[i] = eenx + i * (realcooywidth - 2 - offsetx) / (mchartConfig.showXcount - 1);
            xy[i] = mheight;
        }
    }

    private void drawGradient(Canvas canvas) {
        canvas.drawRect(eenx, eeny,
                eenx + realcooywidth,
                eeny + realcooyheight, gradientPaint);
    }

    private boolean upline(float x, float y) {
        for (int i = 0; i < points.size() - 1; i++) {
            if (x >= (points.get(i).pointx) && x < (points.get(i + 1).pointx))
                return getupline(points.get(i), points.get(i + 1), x, y);
        }
        return false;
    }

    private boolean getupline(LinePoint linePoint, LinePoint linePoint1, float x, float y) {
        boolean re = (y) >= ((linePoint1.pointy - linePoint.pointy) * (x - (linePoint.pointx)) / (linePoint1.pointx - linePoint.pointx) + linePoint.pointy);
        return re;
    }

    class LinePoint {
        float pointx, pointy;

        public LinePoint(float pointx, float pointy) {
            this.pointx = pointx;
            this.pointy = pointy;
        }
    }

    List<LinePoint> points = new ArrayList<>();

    private void calmyLine() {
        if (mInfos == null || mInfos.size() < 2 || mchartConfig == null || mchartConfig.showYcount < 1 || mchartConfig.showYcount < 1
                || yx == null || yy == null || xx == null || xy == null)
            return;
        points.clear();
        for (Info mInfo : mInfos) {
            points.add(new LinePoint(getvax(mInfo.x), getvay(mInfo.y)));
        }
    }

    private void drawmyLine(Canvas canvas) {
        float thestartpointx = -1;
        float thestartpointy = -1;
        float thestartpointx2 = -1;
        float thestartpointy2 = -1;
        for (LinePoint point : points) {
            if (thestartpointx == -1 && thestartpointy == -1) {
                thestartpointx = point.pointx;
                thestartpointy = point.pointy;
            } else {
                thestartpointx2 = point.pointx;
                thestartpointy2 = point.pointy;
                canvas.drawLine(thestartpointx, thestartpointy, thestartpointx2, thestartpointy2, linePaint);
                thestartpointx = thestartpointx2;
                thestartpointy = thestartpointy2;
            }
        }
    }

    private float getvax(long theva) {
        return getva(xmin, xmax, theva, eenx,
                eenx + realcooywidth);
    }

    private float getvay(long theva) {
        return getva(ymin, ymax, theva, eeny + realcooyheight,
                eeny);
    }

    public float getva(long sx, long ex, long theva, float ssx, float eex) {
        return ssx + (theva - sx + 0.0f) / (ex - sx) * (eex - ssx);
    }

    private void drawcoordinatePaint(Canvas canvas) {
        if (mInfos == null || mchartConfig == null || mchartConfig.showYcount < 1 || mchartConfig.showYcount < 1 ||
                yx == null || yy == null || xx == null || xy == null)
            return;
        for (int i = 0; i < mchartConfig.showYcount; i++) {
            canvas.drawText(sy.get(i), yx[i], yy[i], textYPaint);
        }
        for (int i = 0; i < mchartConfig.showXcount; i++) {
            canvas.drawText(sx.get(i), xx[i], xy[i], textXPaint);
        }
        canvas.drawLine(startXline, 0, startXline, realcooyheight + offsety, coordinatePaint);
        canvas.drawLine(startXline, endYline, eenx + realcooywidth + offsetx, endYline, coordinatePaint);
    }
}
