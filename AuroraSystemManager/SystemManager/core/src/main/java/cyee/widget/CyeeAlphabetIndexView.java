package cyee.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;

/**
 * 字母表控件
 * 
 * @author HuangZhiYuan
 * @since 04/05/2012
 */
public final class CyeeAlphabetIndexView extends AbsListIndexer {
	private static final String TAG = "CyeeAlphabetIndexView";
	private static final int INVALID_INDEX = -1;

	// Gionee:huangzy 20120903 add for CR00682623 start
	private static final String LETTER_DISPLAY_AREA_REPRESENTER = "M";
	// Gionee:huangzy 20120903 add for CR00682623 end

	private final String[] mAlphabet;
	private final int ALPHABET_LEN;

	private Paint mPaint;
	// private int mTransparentBgcolor;
	private int mCurrentShowingBgcolor;

	private int mLetterTextSize;
	// private int mLetterHeight;
	// private int mLetterWidth;

	private int mEnableLetterColor;
	private int mDisableLetterColor;
	private int mShowingLetterColor;
	private final int mCurrentLetterColor;

	private int mShowingLetterIndex = INVALID_INDEX;
	private int mTouchingAlphbetIndex = INVALID_INDEX;
	private int mPreTouchingLetterIndex = INVALID_INDEX;

	private final int mTouchingLetterTextSize;
	private final int mTouchingLeftOffset;

	private boolean mIsTouching = false;

	private static final int DEFAULT_PADDING_RIGHT = 5;// dp
	private static final int DEFAULT_PADDING_TOP = 5;// dp
	private static final int DEFAULT_TOUCHING_LEFT = 75;// dp

	private final int mInitialLetterTextSize;
	/**
	 * 点击响应区域，字母的宽度+右边距
	 */
	private int mTouchWidth = 50;
	/**
	 * 默认右边距 5dp
	 */
	private int mRightPadding = 15;
	
	public CyeeAlphabetIndexView(Context context) {
		this(context, null, 0);
	}

	public CyeeAlphabetIndexView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CyeeAlphabetIndexView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CyeeAlphabetIndexView,
				com.cyee.internal.R.style.CyeeAlphabetIndexViewStyle, 0);
		Resources res = getResources();

		mInitialLetterTextSize = a.getDimensionPixelSize(R.styleable.CyeeAlphabetIndexView_cyeeSectionFontSize,
				toRawTextSize(12));
		mLetterTextSize = mInitialLetterTextSize;
		
		mTouchingLetterTextSize = a.getDimensionPixelSize(
				R.styleable.CyeeAlphabetIndexView_cyeeTouchingLetterFontSize, toRawTextSize(30));
		mTouchingLeftOffset = a.getDimensionPixelOffset(
				R.styleable.CyeeAlphabetIndexView_cyeeTouchingLeftOffset, (int) (mContext.getResources()
						.getDisplayMetrics().density * DEFAULT_TOUCHING_LEFT));

		int defalutEnableLetterColor = res.getColor(com.cyee.internal.R.color.cyee_content_color_secondary_on_backgroud_c2);
		mEnableLetterColor = a.getColor(R.styleable.CyeeAlphabetIndexView_cyeeEnableSectionColor,
				defalutEnableLetterColor);

		int defalultDisableLetterColor = res.getColor(com.cyee.internal.R.color.cyee_content_color_thirdly_on_backgroud_c3);
		mDisableLetterColor = a.getColor(R.styleable.CyeeAlphabetIndexView_cyeeDisableSectionColor,
				defalultDisableLetterColor);

		int defalutShowingLetterColor = res.getColor(com.cyee.internal.R.color.cyee_accent_color_g1);
		mShowingLetterColor = a.getColor(R.styleable.CyeeAlphabetIndexView_cyeeShowingLetterColor,
				defalutShowingLetterColor);

		mCurrentLetterColor = 0xffffffff;
		mCurrentShowingBgcolor = mShowingLetterColor;

		mRightPadding = a.getDimensionPixelSize(com.android.internal.R.styleable.View_paddingRight,
				(int) mContext.getResources().getDisplayMetrics().density * DEFAULT_PADDING_RIGHT);
		int topPadding = a.getDimensionPixelSize(com.android.internal.R.styleable.View_paddingTop,
				(int) mContext.getResources().getDisplayMetrics().density * DEFAULT_PADDING_TOP);
		setPadding(getPaddingLeft(), topPadding, mRightPadding, getPaddingBottom());

		a.recycle();

		mAlphabet = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
				"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
		ALPHABET_LEN = mAlphabet.length;
		ChangeViewColorWithChameleon();
		init(context);
		adjustLetterTextSize();
		adjustPadding();
		getTouchWidth();
	}

	/**
	 * 获取点击响应区域
	 * 通过字母的大小+右边距获得，同时保证最小的点击区域
	 */
    private void getTouchWidth() {
        int initPaddingRight = (int) mContext.getResources().getDisplayMetrics().density * DEFAULT_PADDING_RIGHT;
        int tmpPaddingRight = mRightPadding;

        if (mRightPadding < initPaddingRight) {
            tmpPaddingRight = initPaddingRight;
        }
        int tmpTextSize = mLetterTextSize;
        if (mLetterTextSize < mInitialLetterTextSize) {
            tmpTextSize = mInitialLetterTextSize;
        }
        mTouchWidth = tmpPaddingRight + tmpTextSize;
    }
	
	private void adjustPadding() {
		int paddingBottom = getPaddingBottom();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingLeft = getPaddingLeft();
		if (paddingTop < mMaxCircleRadius) {
			paddingTop = (int) mMaxCircleRadius;
		}
		if (paddingBottom < mMaxCircleRadius) {
			paddingBottom = (int) mMaxCircleRadius;
		}
		setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	}

	protected void init(Context context) {
		initPaint();
		initCircleParam();
	}

	/**
	 * Assign a size and position to a view and all of its descendants
	 *
	 * <p>
	 * This is the second phase of the layout mechanism. (The first is measuring). In this phase, each parent
	 * calls layout on all of its children to position them. This is typically done using the child
	 * measurements that were stored in the measure pass().
	 * </p>
	 *
	 * <p>
	 * Derived classes should not override this method. Derived classes with children should override
	 * onLayout. In that method, they should call layout on each of their children.
	 * </p>
	 *
	 * @param l
	 *            Left position, relative to parent
	 * @param t
	 *            Top position, relative to parent
	 * @param r
	 *            Right position, relative to parent
	 * @param b
	 *            Bottom position, relative to parent
	 */
	@Override
	public void layout(int l, int t, int r, int b) {
		super.layout(l, t, r, b);

		if (getHeight() > 0) {
			adjustLetterTextSize();
			initLetterHolders(getHeight());
			updateSelctedItemParam(mShowingLetterIndex);
		}
	}

	// gionee maxw add begin
	private int mTouchY = 0;
	// gionee maxw add end

	/**
	 * Implement this method to handle touch screen motion events.
	 * <p>
	 * If this method is used to detect click actions, it is recommended that the actions be performed by
	 * implementing and calling {@link #performClick()}. This will ensure consistent system behavior,
	 * including:
	 * <ul>
	 * <li>obeying click sound preferences
	 * <li>dispatching OnClickListener calls
	 * <li>handling {@link AccessibilityNodeInfo#ACTION_CLICK ACTION_CLICK} when accessibility features are
	 * enabled
	 * </ul>
	 *
	 * @param event
	 *            The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        // gionee maxw add begin
        mTouchY = y;
        // gionee maxw add end
        mTouchingAlphbetIndex = getTouchingIndex(y);

        if (!mIsTouching && getWidth() - x > mTouchWidth) {
            if (mIsTouching) {
                mIsTouching = false;
            }
        } else {
            switch (action) {
            case MotionEvent.ACTION_DOWN:
                cancelFling();
                mIsTouching = true;
                moveList();
                countShowingLetterIndex();
                startAnimator(true);
                break;
            case MotionEvent.ACTION_MOVE:
                mIsTouching = true;
                moveList();
                if (countShowingLetterIndex()) {
                    if (mAnimator != null) {
                        mAnimator.cancel();
                        mCurrentSelectedItemParams = createEndValue(true,
                                mLetterHolders[mShowingLetterIndex]);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Gionee weidong 2017-3-29 modify for 92381 begin
                countShowingLetterIndex();
                startAnimator(false);
                // Gionee weidong 2017-3-29 modify for 92381 end
                mIsTouching = false;
                break;
            default:
                break;
            }
        }

        invalidate();

        return mIsTouching;
    }

	private void moveList() {
		if (mIsTouching) {
			if (mPreTouchingLetterIndex != mTouchingAlphbetIndex
					|| mShowingLetterIndex != mTouchingAlphbetIndex) {
				mPreTouchingLetterIndex = mTouchingAlphbetIndex;
				int index = toSectionIndex(mTouchingAlphbetIndex);
				if (index == -1) {
					return;
				}
				moveListToSection(index);
			}
		}
	}

	private boolean countShowingLetterIndex() {
		if (null == mSectionIndexer || null == mList || mLetterHolders == null || mLetterHolders.length == 0) {
			return false;
		}

		int shallShowing = 0;
		if (mIsTouching) {
			shallShowing = mTouchingAlphbetIndex;
		} else {
			int position = mList.getFirstVisiblePosition() - mListOffset;
			position = position < 0 ? 0 : position;
			int sectionIndex = mSectionIndexer.getSectionForPosition(position);
			shallShowing = toAlphbetIndex(sectionIndex);
		}

		if (mShowingLetterIndex == shallShowing) {
			return false;
		} else {
			mShowingLetterIndex = shallShowing;
		}

		if (mCurrentSelectedItemParams == null) {
			initSelctedItemParam(shallShowing);
		} else {
			updateSelctedItemParam(shallShowing);
		}

		return true;
	}

	private void updateSelctedItemParam(int index) {
		if (index < 0) {
			return;
		}

		LetterHolder holder = mLetterHolders[index];
		float circleRadius = mMinCircleRadius;
		int textSize = mLetterTextSize;
		float textY = holder.getTextTop();
		float circleCenterY = computeCircleCenterY(textY, circleRadius, textSize);
		mCurrentSelectedItemParams.setTextY(textY);
		mCurrentSelectedItemParams.setCircleCenterY(circleCenterY);
        //Chenyee <CY_Widget> hushengsong 2018-07-14 modify for SWW1618OTA-737 begin
        float textX = holder.getNavigationTextLeft();
        float circleCenterX = holder.getNavigationTextLeft();
        mCurrentSelectedItemParams.setTextX(textX);
        mCurrentSelectedItemParams.setCircleCenterX(circleCenterX);
        //Chenyee <CY_Widget> hushengsong 2018-07-14 modify for SWW1618OTA-737 end
		// Chenyee <Cyee_Widget> hushengsong 2018-08-12 modify for CSW1802A-1113 begin
		mCurrentSelectedItemParams.setTextSize(mLetterTextSize);
		mCurrentSelectedItemParams.setCircleRadius(circleRadius);
		//Chenyee <Cyee_Widget> hushengsong 2018-08-12 modify for CSW1802A-1113 end
	}

    /**
     * 更新高亮字母
     */
    public void invalidateShowingLetterIndex() {
        if (countShowingLetterIndex()) {
            if (mShowingLetterIndex != INVALID_INDEX) {
                final LetterHolder holder = mLetterHolders[mShowingLetterIndex];
                mCurrentSelectedItemParams = createEndValue(false, holder);
                if (!mIsTouching) {
                    if (isAnimationRunning()) {
                        mAnimator.cancel();
                        mEndValue = mCurrentSelectedItemParams;
                    } else {
                        Log.d(TAG, "isAnimationRunning is false");
                    }
                }
            }
            invalidate();
        }
    }

	protected int getTouchingIndex(int touchingY) {

		if (touchingY < mLetterHolders[0].mOrigRect.top) {
			return 0;
		} else if (touchingY > mLetterHolders[ALPHABET_LEN - 1].mOrigRect.bottom) {
			return ALPHABET_LEN - 1;
		}

		int start = 0;
		int end = ALPHABET_LEN - 1;
		int mid = (start + end) >> 1;

		Rect rect = new Rect(mLetterHolders[mid].mOrigRect);

		while (!rect.contains(rect.left, touchingY) && end > start) {
			if (touchingY < rect.top) {
				end = mid - 1;
			} else {
				start = mid + 1;
			}

			mid = (int) (((long) start + (long) end) / 2);
			rect = mLetterHolders[mid].mOrigRect;
		}

		return mid;
	}

	// ======================List Section======================
	/* The related list view and adapter. */
	private ListView mList;
	private SectionIndexer mSectionIndexer;

	/* The real sections got from list. */
	private String[] mSectionStrings = new String[] {null};

	/*
	 * When it is an header-footer list, represents the count of headers, 0 in normal list.
	 */
	private int mListOffset;

	/**
	 * Send an ACTION_CANCEL message to stop list fling.
	 */
	private void cancelFling() {
		if (null == mList)
			return;

		MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
		mList.onTouchEvent(cancelFling);
		cancelFling.recycle();
	}

	/**
	 * Set the related list view and get indexer information from it, every time when you change the data of
	 * list, you should call it to reset the list.
	 * 
	 * @param listView
	 *            The related list view.
	 */
	public void setList(ListView listView) {
		setList(listView, null);
	}

	/**
	 * Set the related list view and get indexer information from it, every time when you change the data of
	 * list, you should call it to reset the list.
	 * 
	 * @param listView
	 *            The related list view.
	 * @param scrollListener
	 *            the scroll listener
	 */
	public void setList(ListView listView, AbsListView.OnScrollListener scrollListener) {
		if (listView != null) {
			mList = listView;

			/* Disable fast scroller and hide the vertical scroll bar. */
			mList.setFastScrollEnabled(false);
			mList.setVerticalScrollBarEnabled(false);
			if (scrollListener != null) {
				mList.setOnScrollListener(scrollListener);
			}

			/* Get section indexers information form list. */
			initSections(mList);
			countShowingLetterIndex();
			invalidate();
		} else {
			throw new IllegalArgumentException("Can not set a null list!");
		}
	}

	/**
	 * set the SectionIndexer
	 * 
	 * @param indexer
	 *            the SectionIndexer to set
	 */
	public void updateIndexer(SectionIndexer indexer) {
		mSectionIndexer = indexer;
		initSections();
		countShowingLetterIndex();
		invalidate();
	}

	private void initSections() {
		if (mSectionIndexer == null) {
			return;
		}
		Object[] sections = mSectionIndexer.getSections();
//		Object[] sections = mAlphabet;
		if (null != sections && 0 < sections.length) {
			mSectionStrings = new String[sections.length];
			boolean setNull = true;
			for (int i = 0; i < mSectionStrings.length; ++i) {
				mSectionStrings[i] = sections[i].toString();
				setNull = true;
				for (int j = 0; j < mAlphabet.length; ++j) {
					if (mAlphabet[j].equalsIgnoreCase(mSectionStrings[i])) {
						setNull = false;
						break;
					}
				}
				if (setNull) {
					mSectionStrings[i] = null;
				}
			}
		} else {
			mSectionStrings = new String[] {null};
		}
		pickDisableSection();
	}

	/**
	 * Get sections and section indexers, then initialize adapter with the list adapter, in the end of the
	 * function, the absent array will be updated.
	 */
	private void initSections(ListView listView) {
		Adapter adapter = listView.getAdapter();
		mSectionIndexer = null;

		if (adapter instanceof HeaderViewListAdapter) {
			mListOffset = ((HeaderViewListAdapter) adapter).getHeadersCount();
			adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		}

		if (adapter instanceof SectionIndexer) {
			mSectionIndexer = (SectionIndexer) adapter;
			Object[] sections = mSectionIndexer.getSections();
			if (null != sections && 0 < sections.length) {
				mSectionStrings = new String[sections.length];
				boolean setNull = true;
				for (int i = 0; i < mSectionStrings.length; ++i) {
					mSectionStrings[i] = sections[i].toString();
					setNull = true;
					for (int j = 0; j < mAlphabet.length; ++j) {
						if (mAlphabet[j].equalsIgnoreCase(mSectionStrings[i])) {
							setNull = false;
							break;
						}
					}
					if (setNull) {
						mSectionStrings[i] = null;
					}
				}
			} else {
				mSectionStrings = new String[] {null};
            }

		}

		pickDisableSection();
	}

	private int toSectionIndex(int alphbetIndex) {
		if (alphbetIndex >= 0 && alphbetIndex < mAlphabet.length) {
			String letter = mAlphabet[alphbetIndex];
			for (int i = 0; i < mSectionStrings.length; i++) {
				if (null == mSectionStrings[i]) {
					continue;
				}
				if (letter.equalsIgnoreCase("#")) {
					return mSectionStrings.length - 1;
				} else if (letter.compareToIgnoreCase(mSectionStrings[i]) == 0) {
					return i;
				}
			}
		}

		return INVALID_INDEX;
	}

	private int toAlphbetIndex(int sectionIndex) {
		if (sectionIndex >= 0 && sectionIndex < mSectionStrings.length) {
			String section = mSectionStrings[sectionIndex];

			for (int i = 0; i < mAlphabet.length; i++) {

				if (mAlphabet[i].equalsIgnoreCase(section)) {
					return i;
				}
			}
		} else {
			// TO DEBUG
			Log.d(TAG, "toAlphbetIndex=-1, sectionIndex=" + sectionIndex + "mSectionStrings.length="
					+ mSectionStrings.length);
		}

		return INVALID_INDEX;
	}

	/**
	 * Move the list to the start position of the current selected section.
	 * 
	 * @param fullSection
	 *            The index in list sections.
	 */
	private void moveListToSection(int sectionIndex) {
		if (null == mList || null == mSectionIndexer || INVALID_INDEX == sectionIndex) {
			return;
		}

		int position = mSectionIndexer.getPositionForSection(sectionIndex);
		/*
		 * Add mListOffset for all list view, because it will be 0 if it has no header.
		 */
		// Gionee shaozj 2006-7-6 modify for CR01494602 begin
		mList.setSelectionFromTop(position + mListOffset, -1);
		// Gionee shaozj 2006-7-6 modify for CR01494602 end
	}

	private void ChangeViewColorWithChameleon() {
		if (ChameleonColorManager.isNeedChangeColor(mContext)) {
			mEnableLetterColor = ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
			mDisableLetterColor = ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3();
			mShowingLetterColor = ChameleonColorManager.getAccentColor_G1();
			mCurrentShowingBgcolor = ChameleonColorManager.getAccentColor_G1();
		}
	}

	// *************************List Section*************************

	class LetterHolder {
		Rect mDrawRect;
		Rect mOrigRect;
		String mLetter;
		boolean mIsEnable = false;
		private int index = INVALID_INDEX;

		public LetterHolder(int left, int top, int right, int bottom, String letter, int index) {
			this(new Rect(left, top, right, bottom), letter, index);
		}

		public LetterHolder(Rect origRect, String letter, int index) {
			mDrawRect = new Rect(origRect);
			mDrawRect.left -= mTouchingLeftOffset;
			mDrawRect.right -= mTouchingLeftOffset;
			mOrigRect = new Rect(origRect);

			mLetter = letter;
			this.index = index;
		}

		public int getTextTop() {
			return mDrawRect.top / 2 + mDrawRect.bottom / 2;
		}

		public int getNavigationTextLeft() {
			return mOrigRect.left + mOrigRect.width() / 2;
		}

		public int getNavigationCircleLeft() {
			return mDrawRect.left + mDrawRect.width() / 2;
		}

		/*public void resetOffset() {
		    mDrawRect.left = mOrigRect.left;
		    mDrawRect.right = mOrigRect.right;
		}

		public void offsetDrawLeft() {
		    mDrawRect.left = mOrigRect.left - mTouchingLeftOffset;
		    mDrawRect.right = mOrigRect.right - mTouchingLeftOffset;
		}*/

		public void setEnable(boolean isEnable) {
			mIsEnable = isEnable;
		}

		public boolean isEnable() {
			return mIsEnable;
		}

		public Rect getOrigRect() {
			return mOrigRect;
		}

		public Rect getDrawRect() {
			return mDrawRect;
		}

		public int getIndex() {
			return index;
		}
	}

	LetterHolder[] mLetterHolders;

	private void initLetterHolders(int viewHeight) {
		final int alphbetLen = mAlphabet.length;
		if (alphbetLen <= 0)
			return;

		int sectionTopOffset = getPaddingTop();
		int realHeight = viewHeight - sectionTopOffset - getPaddingBottom();
		int sectionHeight = realHeight / alphbetLen;
		sectionTopOffset += ((realHeight % alphbetLen) / 2);
		int leftOffset = getWidth() - sectionHeight - getPaddingRight();
		mLetterHolders = new LetterHolder[alphbetLen];

		int right = leftOffset + sectionHeight;
		int top = sectionTopOffset;
		int bottom = top + sectionHeight;

		for (int i = 0; i < alphbetLen; ++i) {
			mLetterHolders[i] = new LetterHolder(leftOffset, top, right, bottom, mAlphabet[i], i);

			top += sectionHeight;
			bottom += sectionHeight;
		}

		pickDisableSection();

		initSelctedItemParam(mShowingLetterIndex);
	}

	private boolean pickDisableSection() {
		if (null == mLetterHolders) {
			return false;
		}

		for (int i = 0; i < mLetterHolders.length; ++i) {
			mLetterHolders[i].setEnable(false);
			if (null != mSectionStrings && mSectionStrings.length > 0) {
				for (int j = 0; j < mSectionStrings.length; j++) {
					if (mLetterHolders[i].mLetter.equalsIgnoreCase(mSectionStrings[j])) {
						mLetterHolders[i].setEnable(true);
						break;
					}
				}
			} else {
				mLetterHolders[i].setEnable(false);
			}
		}
		return true;
	}

	/**
	 * 是否正在运行
	 */
	public boolean isBusying() {
		return mIsTouching;
	}

	/**
	 * 获取字母表的内容
	 * 
	 * @return 字母表内容，字符串数组的形式
	 */
	public String[] getAlphabet() {
		return mAlphabet;
	}

	/**
	 * 设置不可用的字母颜色
	 * 
	 * @param color
	 *            不可用字母颜色
	 */
	public void setDisableLetterColor(int color) {
		mDisableLetterColor = color;
	}

	/**
	 * 设置可用字母的颜色
	 * 
	 * @param color
	 *            可用字母颜色
	 */
	public void setEnableLetterColor(int color) {
		mEnableLetterColor = color;
	}

	/**
	 * 设置高亮的字母颜色
	 * 
	 * @param color
	 *            高亮色
	 */
	public void setShowingLetterColor(int color) {
		mShowingLetterColor = color;
		mCurrentShowingBgcolor = mShowingLetterColor;
	}

	// ---------------------------- about animation-----------------------

	private static final int ANIMATOR_DURATION = 200;
	private ValueAnimator mAnimator;
	private Paint mCirclePaint;
	private Paint mSelectedLetterPaint;
	private SelectedItemParam mCurrentSelectedItemParams;
	private float mMinCircleRadius;
	private float mMaxCircleRadius;

	private void initCircleParam() {
		Rect bounds = new Rect();
		Paint tempPaint = new Paint();

		tempPaint.setTextSize(mTouchingLetterTextSize);
		tempPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
		mMaxCircleRadius = Math.min(bounds.width(), bounds.height());

		tempPaint.setTextSize(mLetterTextSize);
		tempPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
		mMinCircleRadius = Math.min(bounds.width(), bounds.height());
	}

	private void initPaint() {
		if (mPaint == null) {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.setTextSize(mLetterTextSize);
		}

		if (mCirclePaint == null) {
			mCirclePaint = new Paint();
			mCirclePaint.setAntiAlias(true);
			mCirclePaint.setColor(mShowingLetterColor);
		}

		if (mSelectedLetterPaint == null) {
			mSelectedLetterPaint = new Paint();
			mSelectedLetterPaint.setAntiAlias(true);
			mSelectedLetterPaint.setTextAlign(Paint.Align.CENTER);
			mSelectedLetterPaint.setColor(mCurrentLetterColor);
		}
	}

	private boolean isAnimatorPrepareing = false;
	private SelectedItemParam mEndValue;
	
	
	private void startAnimator(final boolean show) {
		if (mAnimator == null) {
			mAnimator = new ValueAnimator();
		} else {
			mAnimator.cancel();
		}
		final int showingLetterIndex = mShowingLetterIndex;
		if (showingLetterIndex < 0) {
			return;
		}
		initAnimator(show, showingLetterIndex);

		mAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animator) {
				isAnimatorPrepareing = false;
				LetterHolder holder = mLetterHolders[showingLetterIndex];
				SelectedItemParam beginValue = createBeginValue(show, holder);
				mEndValue = createEndValue(show, holder);
				SelectedItemParam middleValue = createMiddleValue(holder);
				mAnimator.setObjectValues(beginValue, middleValue, mEndValue);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				isAnimatorPrepareing = false;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				isAnimatorPrepareing = false;
			}
		});
//		mAnimator.setStartDelay(60);
		mAnimator.start();
		isAnimatorPrepareing = true;
	}

	private void initAnimator(boolean isShow, int showingLetterIndex) {
		LetterHolder holder = mLetterHolders[showingLetterIndex];

		SelectedItemParam beginValue = createBeginValue(isShow, holder);
		SelectedItemParam endValue = createEndValue(isShow, holder);
		SelectedItemParam middleValue = createMiddleValue(holder);
		mAnimator.setObjectValues(beginValue, middleValue, endValue);
		mAnimator.setDuration(ANIMATOR_DURATION);
		mAnimator.setEvaluator(new MyTypeEvaluator());
		mAnimator.setInterpolator(new DecelerateInterpolator());
		mAnimator.addUpdateListener(new MyAnimatorUpdateListener());
		mAnimator.setTarget(this);
	}

//	private int computeAnimatorLetterIndex() {
//		if(mLetterHolders[mTouchingAlphbetIndex].isEnable()) {
//			return mTouchingAlphbetIndex;
//		} else {
//			int position = mSectionIndexer.getPositionForSection(toSectionIndex(mTouchingAlphbetIndex));
//			int sectionIndex = mSectionIndexer.getSectionForPosition(position);
//			int index = toAlphbetIndex(sectionIndex);
//			return index;
//		}
//	}

	private SelectedItemParam createBeginValue(boolean isShow, LetterHolder holder) {
		float circleCenterX = 0;
		float circleCenterY = 0;
		float circleRadius = 0;
		float textX = 0;
		float textY = 0;
		int textSize = 0;

		if (isShow) {
			textX = holder.getNavigationTextLeft();
			textY = holder.getTextTop();
			circleCenterX = holder.getNavigationTextLeft();
			circleRadius = mMinCircleRadius;
			textSize = mLetterTextSize;
			circleCenterY = computeCircleCenterY(textY, circleRadius, textSize);
		} else {
			circleCenterX = holder.getNavigationCircleLeft();
			circleRadius = mMaxCircleRadius;
			textX = holder.getNavigationCircleLeft();
			textY = mTouchY;
			if (holder.getIndex() == 0) {
				if (mTouchY < holder.getTextTop()) {
					textY = holder.getTextTop();
				}
			}
			if (holder.getIndex() == mLetterHolders.length - 1) {
				if (mTouchY > holder.getTextTop()) {
					textY = holder.getTextTop();
				}
			}
			textSize = mTouchingLetterTextSize;
			circleCenterY = computeCircleCenterY(mTouchY, circleRadius, textSize);
		}

		return new SelectedItemParam(circleCenterX, circleRadius, circleCenterY, textX, textY, textSize);
	}

	private float computeCircleCenterY(float textY, float circleRadius, int textSize) {
		Paint tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		tempPaint.setTextSize(textSize);
		FontMetricsInt fontMetrics = tempPaint.getFontMetricsInt();
//		int baseline = targetRect.top + (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;  
		float circleCenterY = textY + (fontMetrics.bottom + fontMetrics.top) / 2;
		return circleCenterY;
	}

	private SelectedItemParam createMiddleValue(LetterHolder holder) {
		float circleCenterX = holder.getNavigationCircleLeft();
		float circleRadius = mMaxCircleRadius;
		float textX = holder.getNavigationCircleLeft();
		float textY = holder.getTextTop();
		int textSize = mTouchingLetterTextSize;
		float circleCenterY = computeCircleCenterY(textY, circleRadius, textSize);

		return new SelectedItemParam(circleCenterX, circleRadius, circleCenterY, textX, textY, textSize);

	}

	private SelectedItemParam createEndValue(boolean isShow, LetterHolder holder) {
		float circleCenterX = 0;
		float circleCenterY = 0;
		float circleRadius = 0;
		float textX = 0;
		float textY = 0;
		int textSize = 0;

		if (isShow) {
			circleCenterX = holder.getNavigationCircleLeft();
			circleRadius = mMaxCircleRadius;
			textX = holder.getNavigationCircleLeft();
			textY = mTouchY;
			if (holder.getIndex() == 0) {
				if (mTouchY < holder.getTextTop()) {
					textY = holder.getTextTop();
				}
			}
			if (holder.getIndex() == mLetterHolders.length - 1) {
				if (mTouchY > holder.getTextTop()) {
					textY = holder.getTextTop();
				}
			}

			textSize = mTouchingLetterTextSize;
			circleCenterY = computeCircleCenterY(textY, circleRadius, textSize);
		} else {
			circleCenterX = holder.getNavigationTextLeft();
			circleRadius = mMinCircleRadius;
			textX = holder.getNavigationTextLeft();
			textY = holder.getTextTop();
			textSize = mLetterTextSize;
			circleCenterY = computeCircleCenterY(textY, circleRadius, textSize);
		}

		return new SelectedItemParam(circleCenterX, circleRadius, circleCenterY, textX, textY, textSize);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawAlphabet(canvas);

		drawSelectedItem(canvas);

	}

	private void drawAlphabet(Canvas canvas) {
		LetterHolder hodler = null;
		int paintColor;
		int textTop;
		int textLeft;
		for (int i = 0; i < mLetterHolders.length; ++i) {
			if (mShowingLetterIndex == i) {
				continue;
			}

			hodler = mLetterHolders[i];
			textTop = hodler.getTextTop();
			textLeft = hodler.getNavigationTextLeft();

			if (hodler.isEnable()) {
				paintColor = mEnableLetterColor;
			} else {
				paintColor = mDisableLetterColor;
			}
			mPaint.setTextSize(mLetterTextSize);
			mPaint.setColor(paintColor);
			canvas.drawText(hodler.mLetter, textLeft, textTop, mPaint);

		}

	}

	public boolean isAnimationRunning() {
		if (mAnimator == null) {
			return false;
		}
		return mAnimator.isRunning();
	}

	private void drawSelectedItem(Canvas canvas) {
		if (mShowingLetterIndex < 0) {
			return;
		}
		doDrawCircle(canvas);
		doDrawSelectedLetter(canvas);
	}

	private void doDrawSelectedLetter(Canvas canvas) {
		LetterHolder holder = mLetterHolders[mShowingLetterIndex];
		mSelectedLetterPaint.setTextSize(mCurrentSelectedItemParams.getTextSize());

		float y = mTouchY;
		if (isAnimationRunning()) {
			y = mCurrentSelectedItemParams.getTextY();
		} else {
			if (!mIsTouching) {
				if (!isAnimatorPrepareing) {
					y = mCurrentSelectedItemParams.getTextY();
				}
			}
		}

		if (mShowingLetterIndex == 0) {
			if (y < mCurrentSelectedItemParams.getTextY()) {
				y = mCurrentSelectedItemParams.getTextY();
			}
		}
		if (mShowingLetterIndex == mLetterHolders.length - 1) {
			if (y > mCurrentSelectedItemParams.getTextY()) {
				y = mCurrentSelectedItemParams.getTextY();
			}
		}
		canvas.drawText(holder.mLetter, mCurrentSelectedItemParams.textX, y, mSelectedLetterPaint);
	}

	private void doDrawCircle(Canvas canvas) {
		float cy = computeCircleCenterY(mTouchY, mCurrentSelectedItemParams.getCircleRadius(),
				mCurrentSelectedItemParams.getTextSize());
		if (isAnimationRunning()) {
			cy = mCurrentSelectedItemParams.getCircleCenterY();
		} else {
			if (!mIsTouching) {
				if (!isAnimatorPrepareing) {
					cy = mCurrentSelectedItemParams.getCircleCenterY();
				}
			}
		}

		if (mShowingLetterIndex == 0) {
			if (mTouchY < mCurrentSelectedItemParams.getTextY()) {
				cy = computeCircleCenterY(mCurrentSelectedItemParams.getTextY(),
						mCurrentSelectedItemParams.getCircleRadius(),
						mCurrentSelectedItemParams.getTextSize());
			}
		}
		if (mShowingLetterIndex == mLetterHolders.length - 1) {
			if (mTouchY > mCurrentSelectedItemParams.getTextY()) {
				cy = computeCircleCenterY(mCurrentSelectedItemParams.getTextY(),
						mCurrentSelectedItemParams.getCircleRadius(),
						mCurrentSelectedItemParams.getTextSize());
			}
		}
		canvas.drawCircle(mCurrentSelectedItemParams.getCircleCenterX(), cy,
				mCurrentSelectedItemParams.getCircleRadius(), mCirclePaint);
	}

	private void initSelctedItemParam(int index) {
		if (mCurrentSelectedItemParams != null || index < 0 || mLetterHolders == null
				|| mLetterHolders.length == 0) {
			return;
		}
		LetterHolder holder = mLetterHolders[index];
		float circleCenterX = holder.getNavigationTextLeft();
		float circleRadius = mMinCircleRadius;
		float textX = holder.getNavigationTextLeft();
		int textSize = mLetterTextSize;
		float textY = holder.getTextTop();
		float circleCenterY = computeCircleCenterY(textY, circleRadius, textSize);
		mCurrentSelectedItemParams = new SelectedItemParam(circleCenterX, circleRadius, circleCenterY, textX,
				textY, textSize);
	}

	class SelectedItemParam {
		private float circleCenterX;
		private float circleRadius;
		private float circleCenterY;
		private float textX;
		private float textY;
		private int textSize;

		public SelectedItemParam() {
		}

		public SelectedItemParam(float circleCenterX, float circleRadius, float circleCenterY, float textX,
				float textY, int textSize) {
			this.circleCenterX = circleCenterX;
			this.circleRadius = circleRadius;
			this.circleCenterY = circleCenterY;
			this.textX = textX;
			this.textY = textY;
			this.textSize = textSize;
		}

		public float getCircleCenterY() {
			return circleCenterY;
		}

		public void setCircleCenterY(float circleCenterY) {
			this.circleCenterY = circleCenterY;
		}

		public float getTextY() {
			return textY;
		}

		public void setTextY(float textY) {
			this.textY = textY;
		}

		public float getCircleCenterX() {
			return circleCenterX;
		}

		public void setCircleCenterX(float circleCenterX) {
			this.circleCenterX = circleCenterX;
		}

		public float getCircleRadius() {
			return circleRadius;
		}

		public void setCircleRadius(float circleRadius) {
			this.circleRadius = circleRadius;
		}

		public float getTextX() {
			return textX;
		}

		public void setTextX(float textX) {
			this.textX = textX;
		}

		public int getTextSize() {
			return textSize;
		}

		public void setTextSize(int textSize) {
			this.textSize = textSize;
		}

		@Override
		public String toString() {
			return "SelectedItemParam [circleCenterX=" + circleCenterX + ", circleRadius=" + circleRadius
					+ ", textX=" + textX + ", textSize=" + textSize + "]";
		}

	}

	class MyTypeEvaluator implements TypeEvaluator<SelectedItemParam> {

		@Override
		public SelectedItemParam evaluate(float fraction, SelectedItemParam startValue,
				SelectedItemParam endValue) {
			float circleCenterX = computeCircleCenterX(startValue, endValue, fraction);
			float circleRadius = computeCircleRadius(startValue, endValue, fraction);
			float textX = computeTextX(startValue, endValue, fraction);
			int textSize = computeTextSize(startValue, endValue, fraction);
			float textY = computeTextY(startValue, endValue, fraction);
			float circleCenterY = computeCicleCenterY(startValue, endValue, fraction);
			SelectedItemParam value = new SelectedItemParam(circleCenterX, circleRadius, circleCenterY,
					textX, textY, textSize);
			return value;
		}

		private float computeTextY(SelectedItemParam start, SelectedItemParam end, float fraction) {
			float diff = end.getTextY() - start.getTextY();
			return diff * fraction + start.getTextY();
		}

		private float computeCicleCenterY(SelectedItemParam start, SelectedItemParam end, float fraction) {
			float diff = end.getCircleCenterY() - start.getCircleCenterY();
			return diff * fraction + start.getCircleCenterY();
		}

		private int computeTextSize(SelectedItemParam start, SelectedItemParam end, float fraction) {
			int diff = end.getTextSize() - start.getTextSize();
			int textSize = (int) (diff * fraction + start.getTextSize());
			return textSize;
		}

		private float computeTextX(SelectedItemParam start, SelectedItemParam end, float fraction) {
			float diff = end.getTextX() - start.getTextX();
			return diff * fraction + start.getTextX();
		}

		private float computeCircleRadius(SelectedItemParam start, SelectedItemParam end, float fraction) {
			float diff = end.getCircleRadius() - start.getCircleRadius();
			return diff * fraction + start.getCircleRadius();
		}

		private float computeCircleCenterX(SelectedItemParam start, SelectedItemParam end, float fraction) {
			float diff = end.getCircleCenterX() - start.getCircleCenterX();
			return diff * fraction + start.getCircleCenterX();
		}

	}

	class MyAnimatorUpdateListener implements AnimatorUpdateListener {

		@Override
		public void onAnimationUpdate(ValueAnimator animator) {
			mCurrentSelectedItemParams = (SelectedItemParam) animator.getAnimatedValue();
			invalidate();
		}

	}

	/**
	 * 横竖屏切换时的回调
	 */
	public void onConfigurationChanged(Configuration newConfig) {
		adjustLetterTextSize();
		getTouchWidth();
	}

    private void adjustLetterTextSize() {
        int screenHeigh = mContext.getResources().getDisplayMetrics().heightPixels;
        int screenWidht = mContext.getResources().getDisplayMetrics().widthPixels;

        // Gionee <weidong><2017-3-20> modify for 81313 begin
        if (mAlphabet.length <= 0) {
            Log.d(TAG, "alphbetLen is zero");
            return;
        }
        int sectionTopOffset = getPaddingTop();
        int realHeight = getHeight() - sectionTopOffset
                - getPaddingBottom();
        int sectionHeight = realHeight / mAlphabet.length;
        // Gionee <weidong><2017-3-20> modify for 81313 end
        
        if (sectionHeight > mInitialLetterTextSize) {
            sectionHeight = mInitialLetterTextSize;
        }
        mLetterTextSize = sectionHeight;

        initCircleParam();
        mPaint.setTextSize(mLetterTextSize);
    }

}
