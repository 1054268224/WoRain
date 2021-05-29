package cyee.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import com.cyee.internal.widget.tabhost.CyeePagerAdapter;
import com.cyee.internal.widget.tabhost.CyeeViewPager;
import cyee.widget.CyeeListView;
import com.cyee.internal.widget.tabhost.CyeeViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.cyee.utils.Log;
import android.graphics.Color;
import android.view.Gravity;
import android.database.DataSetObserver;

import com.cyee.internal.util.DensityUtil;
import com.cyee.internal.R;

public class CyeeAutoMaticPageGridView extends LinearLayout {

    private static final int GRID_VIEW_COLUMNS_DEFAULT = 4;
    private static final int GRID_VIEW_LINES_DEFAULT = 1;
    private static final int RADIO_BUTTON_PADDING_IN_DP = 5;
    private static final int GRID_VIEW_PADDING_RIGHT = 20;
    private static final int GRID_VIEW_PADDING_LEFT = 20;
    private static final int GRID_VIEW_COLUMN_WIDTH_IN_DP = 80;
    private static final int GRID_VERTICAL_SPACING = 16;
    private static final int GRID_HORIZONTAL_SPACING = 0;
    private static final int GRID_INDICATOR_MARGIN_TOP = 16;
    private static final int GRID_INDICATOR_MARGIN_BOTTOM = 16;
    private static final int DEFAULT_LOAD_PAGE_COUNT = 6;
    // 分页所用
    private CyeeViewPager mViewPager;
    // 导航点
    private RadioGroup mRadioGroup;

    // 每页的GidView
    private final List<GridView> mGrids = new ArrayList<GridView>();

    // 自定义行数
    private int lines = 0;
    // 自定义列数
    private int column = 0;
    // 自定义按钮样式
    private int btn_res;
    // 自定义属性是否显示导航点
    private boolean btn_isvisible;
    // 页数-需要动态计算
    private int mPages = 0;

    private int currentItem = GridView.INVALID_POSITION;
    private int previousItem = GridView.INVALID_POSITION;

    // choiceMode
    private int mChoiceMode = CyeeListView.CHOICE_MODE_NONE;

    // 适配器
    private CyeeBaseAutoAdapter mAdapter;
    private Context mContext;

    private AdapterView.OnItemClickListener onItemClickCallBack;
    private AdapterView.OnItemLongClickListener onItemLongClickCallBack;

    private CyeePagerObserver mObserver;

    private static final String TAG = "CyeeAutoMaticPageGridView";

    public CyeeAutoMaticPageGridView(Context context) {
        this(context, null);
    }

    public CyeeAutoMaticPageGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CyeeAutoMaticPageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // 初始化控件
    private void init(Context context, AttributeSet attrs) {
        // 加载自定义属性
        mContext = context;
        int[] styleable = {R.attr.cyeeAutoLine, R.attr.cyeeAutoColumn, R.attr.cyeeAutoButton,
                R.attr.cyeeAutoButtonVisible};
        final TypedArray array = context.obtainStyledAttributes(attrs, styleable,
                R.attr.cyeeAutoMaticGridViewStyle, 0);
        lines = array.getInteger(R.styleable.CyeeAutoMaticPageGridView_cyeeAutoLine,
                GRID_VIEW_LINES_DEFAULT);// 行数
        column = array.getInteger(R.styleable.CyeeAutoMaticPageGridView_cyeeAutoColumn,
                GRID_VIEW_COLUMNS_DEFAULT);// 列数
        btn_res = array.getResourceId(R.styleable.CyeeAutoMaticPageGridView_cyeeAutoButton, 0);// btn的样式
        btn_isvisible = array.getBoolean(R.styleable.CyeeAutoMaticPageGridView_cyeeAutoButtonVisible, true);// 默认true
        array.recycle();

        setOrientation(LinearLayout.VERTICAL);
        mViewPager = new MyCyeeViewPager(context);
        mViewPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mViewPager.setPadding(DensityUtil.dip2px(getContext(), GRID_VIEW_PADDING_LEFT), 0,
                DensityUtil.dip2px(getContext(), GRID_VIEW_PADDING_RIGHT), 0);
        addView(mViewPager);
        mViewPager.setOffscreenPageLimit(DEFAULT_LOAD_PAGE_COUNT);
        // 导航点用
        mRadioGroup = new RadioGroup(context);
        mRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = DensityUtil.dip2px(getContext(), GRID_INDICATOR_MARGIN_TOP);
        params.bottomMargin = DensityUtil.dip2px(getContext(), GRID_INDICATOR_MARGIN_BOTTOM);
        mRadioGroup.setLayoutParams(params);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        addView(mRadioGroup);
        // 如果不显示的话就隐藏
        if (!btn_isvisible) {
            mRadioGroup.setVisibility(View.INVISIBLE);
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickCallBack) {
        this.onItemClickCallBack = onItemClickCallBack;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickCallBack) {
        this.onItemLongClickCallBack = onItemLongClickCallBack;
    }

    public int getCheckedItemPosition() {
        return currentItem;
    }

    // 设置适配器
    public void setAdapter(CyeeBaseAutoAdapter baseAdapter) {
        if (this.mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        this.mAdapter = baseAdapter;

        if (this.mAdapter != null) {
            if (mObserver == null) {
                mObserver = new CyeePagerObserver();
            }
            mAdapter.registerDataSetObserver(mObserver);
        }

        // 计算页数
        if ((mAdapter.getCount() % (column * lines)) > 0) {
            mPages = (mAdapter.getCount() / (column * lines)) + 1; // 多一页
        } else {
            mPages = mAdapter.getCount() / (column * lines);
        }
        Log.v(TAG, "performance--- buildContent start");
        buildContents();
        Log.v(TAG, "performance--- buildContent end");
        // 添加radioButton
        Log.v(TAG, "performance--- add RadioButton start");
        addRadioButton(mPages);
        Log.v(TAG, "performance--- add RadioButton end");
        this.post(new Runnable() {
            @Override
            public void run() {
                // 显示viewpager
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getContext());
                mViewPager.setAdapter(viewPagerAdapter);
                // 设置联动
                initLinkAgeEvent();
            }
        });
    }

    // 添加RadioButton
    private void addRadioButton(int pages) {
        for (int i = 0; i < pages; i++) {
            RadioButton radioButton = new RadioButton(getContext());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            int paddingInPX = DensityUtil.dip2px(getContext(), RADIO_BUTTON_PADDING_IN_DP);
            params.leftMargin = paddingInPX;
            params.rightMargin = paddingInPX;
            radioButton.setId(i);// 设置Id,方便联动
            radioButton.setClickable(false);
            radioButton.setLayoutParams(params);
            if (btn_res != 0) { // 设置按钮样式
                radioButton.setButtonDrawable(btn_res);
                radioButton.getLayoutParams().height = radioButton.getButtonDrawable().getIntrinsicHeight();
            }
            mRadioGroup.addView(radioButton);
        }
    }

    // 给当前页计算数据数量
    private List<View> getAdapterData(int position) {
        List<View> itemViews = new ArrayList<>();
        Log.v(TAG, "performance---load item start");
        if (position == mPages - 1) { // 如果等于最后一页
            for (int i = position * (lines * column); i < mAdapter.getCount(); i++) {
                itemViews.add(mAdapter.getItemView(i, null));
            }
        } else {
            for (int i = position * (lines * column); i < position * (lines * column) + (lines * column); i++) {
                itemViews.add(mAdapter.getItemView(i, null));
            }
        }
        Log.v(TAG, "performance---load item end");
        return itemViews;
    }

    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        int size = mGrids.size();
        for (int i = 0; i < size; i++) {
            mGrids.get(i).setChoiceMode(mChoiceMode);
        }
    }

    public void setItemChecked(int position) {
        if (position == currentItem) return;
        int newIndex = position % (lines * column);
        int newPage = position / (lines * column);
        int curIndex = currentItem % (lines * column);
        int curPage = currentItem / (lines * column);

        previousItem = currentItem;
        currentItem = position;
        mGrids.get(curPage).setItemChecked(curIndex, false);
        mViewPager.setCurrentItem(newPage);
        mGrids.get(newPage).setItemChecked(newIndex, true);
    }

    private void buildContents() {
        for (int position = 0; position < mPages; position++) {
            CustomGridView gridView = new CustomGridView(mContext);
            gridView.setVerticalScrollBarEnabled(false);
            gridView.setNumColumns(column);// 设置列数
            final float scale = getResources().getDisplayMetrics().density;
            gridView.setVerticalSpacing(DensityUtil.dip2px(getContext(), GRID_VERTICAL_SPACING));
            gridView.setHorizontalSpacing(DensityUtil.dip2px(getContext(), GRID_HORIZONTAL_SPACING));
            GridViewAdapter gridAdapter = new GridViewAdapter(getAdapterData(position), position);
            gridView.setAdapter(gridAdapter);
            mGrids.add(gridView);
            gridView.setChoiceMode(mChoiceMode);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GridViewAdapter gridAdapter = (GridViewAdapter) parent.getAdapter();
                    setItemChecked(gridAdapter.mCurrentPage
                                * (lines * column) + position);
                    if (onItemClickCallBack != null) {
                        onItemClickCallBack.onItemClick(parent, view, currentItem, id);
                    }
                }
            });
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    GridViewAdapter gridAdapter = (GridViewAdapter) parent.getAdapter();
                    if (onItemLongClickCallBack != null) {
                        onItemLongClickCallBack.onItemLongClick(parent, view, gridAdapter.mCurrentPage
                                * (lines * column) + position, id);
                    }
                    return true;
                }
            });
        }
        //只有1页没必要显示指示器
        if (mPages == 1) {
            mRadioGroup.setVisibility(INVISIBLE);
        }else {
            mRadioGroup.setVisibility(VISIBLE);
        }
    }

    private void computeAndResetData() {
        if ((mAdapter.getCount() % (column * lines)) > 0) {
            mPages = (mAdapter.getCount() / (column * lines)) + 1; // 多一页
        } else {
            mPages = mAdapter.getCount() / (column * lines);
        }
        currentItem = 0;
        previousItem = 0;
        mGrids.clear();
        removeView(mViewPager);
        mRadioGroup.removeAllViews();
    }

    void dataSetChanged() {
        computeAndResetData();
        mViewPager = new MyCyeeViewPager(getContext());
        mViewPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mViewPager.setPadding(DensityUtil.dip2px(getContext(), GRID_VIEW_PADDING_LEFT), 0,
                DensityUtil.dip2px(getContext(), GRID_VIEW_PADDING_RIGHT), 0);
        addView(mViewPager, 0);
        buildContents();
        addRadioButton(mPages);
        this.post(new Runnable() {
            @Override
            public void run() {
                // 显示viewpager
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getContext());
                mViewPager.setAdapter(viewPagerAdapter);
                // 设置联动
                initLinkAgeEvent();
            }
        });    
}

     //CyeePagerObserver
     private class CyeePagerObserver extends DataSetObserver {
         @Override
         public void onChanged() {
             dataSetChanged();
         }
         @Override
         public void onInvalidated() {
             dataSetChanged();
         }
    }

    // ViewPager适配器
    private class ViewPagerAdapter extends CyeePagerAdapter {
        private final Context context;

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mPages;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mGrids.get(position));
            return mGrids.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((GridView) object);
        }
    }

    private class MyCyeeViewPager extends CyeeViewPager {

        public MyCyeeViewPager(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        public MyCyeeViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            int height = 0;
            // 下面遍历所有child的高度
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, heightMeasureSpec);
                int h = child.getMeasuredHeight();
                if (h > height) // 采用最大的view的高度。
                    height = h;
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    // GridView的适配器
    private class GridViewAdapter extends BaseAdapter {

        private List<View> mViews;// 数据量
        public int mCurrentPage; // 当前页

        public GridViewAdapter(List<View> views, int currentPage) {
            this.mViews = views;
            this.mCurrentPage = currentPage;
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mViews.get(position);
        }

        public void setItemViews(List<View> views) {
            mViews = views;
        }
    }

    // 自定义GridView,禁止滑动
    private class CustomGridView extends GridView {

        public CustomGridView(Context context) {
            super(context);
        }

        public CustomGridView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomGridView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public CustomGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                return true;//禁止GridView进行滑动
            }
            return super.dispatchTouchEvent(ev);
        }
    }

    // 初始化联动联动事件
    @SuppressWarnings("deprecation")
    private void initLinkAgeEvent() {
        // 默认选中第一个
        mViewPager.setCurrentItem(0);
        int cnt = mRadioGroup.getChildCount();
        if(cnt <= 0) {
            return ;
        }
        mRadioGroup.check(mRadioGroup.getChildAt(0).getId());			
        // 滑动换页事件
        mViewPager.setOnPageChangeListener(new CyeeViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mRadioGroup.check(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
