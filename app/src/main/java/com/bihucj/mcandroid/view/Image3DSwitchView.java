package com.bihucj.mcandroid.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by 孟晨 on 2018/9/17.
 */

/*在onLayout()方法首先要判断子视图个数是不是大于等于5，如果不足5个则图片轮播器无法正常显示，直接return掉。
如果大于等于5个，就会通过一个for循环来为每个子视图分配显示的位置，而每个子视图都是一个Image3DView，
在for循环中又会调用Image3DView的initImageViewBitmap()方法来为每个控件执行初始化操作，
之后会调用refreshImageShowing()方法来刷新图片的显示状态。
接着当手指在Image3DSwitchView控件上滑动的时候就会进入到onTouchEvent()方法中，
当手指按下时会记录按下时的横坐标，然后当手指滑动时会计算出滑动的距离，并调用scrollBy()方法来进行滚动，
当手指离开屏幕时会距离当前滑动的距离和速度来决定，是滚动到下一张图片，还是滚动到上一张图片，
还是滚动回原图片。分别调用的方法是scrollToNext()、scrollToPrevious()和scrollBack()。
在scrollToNext()方法中会先计算一下还需滚动的距离，然后进行一下边界检查，防止当前图片的下标超出合理范围，
接着会调用beginScroll()方法来进行滚动。在beginScroll()方法中其实就是调用了Scroller的startScroll()方法来执行滚动操作的，
当滚动结束后还会调用requestLayout()方法来要求重新布局，之后onLayout()方法就会重新执行，每个图片的位置也就会跟着改变了。
至于scrollToPrevious()和scrollBack()方法的原理也是一样的，这里就不再重复分析了。
那么在onLayout()方法的最后调用的refreshImageShowing()方法到底执行了什么操作呢？其实就是遍历了一下每个Image3DView控件，
然后调用它的setRotateData()方法，并把图片的下标和滚动距离传进去，这样每张图片就知道应该如何进行旋转了。*/

public class Image3DSwitchView extends ViewGroup {

    /**
     * 图片左右两边的空白间距
     */
    public static final int IMAGE_PADDING = 10;
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    /**
     * 滚动到下一张图片的速度
     */
    private static final int SNAP_VELOCITY = 600;
    /**
     * 表示滚动到下一张图片这个动作
     */
    private static final int SCROLL_NEXT = 0;
    /**
     * 表示滚动到上一张图片这个动作
     */
    private static final int SCROLL_PREVIOUS = 1;
    /**
     * 表示滚动回原图片这个动作
     */
    private static final int SCROLL_BACK = 2;
    private static Handler handler = new Handler();
    /**
     * 控件宽度
     */
    public static int mWidth;
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    /**
     * 图片滚动监听器，当图片发生滚动时回调这个接口
     */
    private OnImageSwitchListener mListener;
    /**
     * 记录当前的触摸状态
     */
    private int mTouchState = TOUCH_STATE_REST;
    /**
     * 记录被判定为滚动运动的最小滚动值
     */
    private int mTouchSlop;
    /**
     * 记录控件高度
     */
    private int mHeight;
    /**
     * 记录每张图片的宽度
     */
    private int mImageWidth;
    /**
     * 记录图片的总数量
     */
    private int mCount;
    /**
     * 记录当前显示图片的坐标
     */
    private int mCurrentImage;
    /**
     * 记录上次触摸的横坐标值
     */
    private float mLastMotionX;
    /**
     * 是否强制重新布局
     */
    private boolean forceToRelayout;
    private int[] mItems;

    public Image3DSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed || forceToRelayout) {
            mCount = getChildCount();
            // 图片数量必须大于5，不然无法正常显示
            if (mCount < 5) {
                return;
            }
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            // 每张图片的宽度设定为控件宽度的百分之六十
            mImageWidth = (int) (mWidth * 0.6);
            if (mCurrentImage >= 0 && mCurrentImage < mCount) {
                mScroller.abortAnimation();
                setScrollX(0);
                int left = -mImageWidth * 2 + (mWidth - mImageWidth) / 2;
                // 分别获取每个位置上应该显示的图片下标
                int[] items = {getIndexForItem(1), getIndexForItem(2),
                        getIndexForItem(3), getIndexForItem(4),
                        getIndexForItem(5)};
                mItems = items;
                // 通过循环为每张图片设定位置
                for (int i = 0; i < items.length; i++) {
                    Image3DView childView = (Image3DView) getChildAt(items[i]);
                    childView.layout(left + IMAGE_PADDING, 0, left
                            + mImageWidth - IMAGE_PADDING, mHeight);
                    childView.initImageViewBitmap();
                    left = left + mImageWidth;
                }
                refreshImageShowing();
            }
            forceToRelayout = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScroller.isFinished()) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            int action = event.getAction();
            float x = event.getX();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // 记录按下时的横坐标
                    mLastMotionX = x;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int disX = (int) (mLastMotionX - x);
                    mLastMotionX = x;
                    scrollBy(disX, 0);
                    // 当发生移动时刷新图片的显示状态
                    refreshImageShowing();
                    break;
                case MotionEvent.ACTION_UP:
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    if (shouldScrollToNext(velocityX)) {
                        // 滚动到下一张图
                        scrollToNext();
                    } else if (shouldScrollToPrevious(velocityX)) {
                        // 滚动到上一张图
                        scrollToPrevious();
                    } else {
                        // 滚动回当前图片
                        scrollBack();
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * 根据当前的触摸状态来决定是否屏蔽子控件的交互能力。
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        float x = ev.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_MOVE:
                int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            refreshImageShowing();
            postInvalidate();
        }
    }

    /**
     * 设置图片滚动的监听器，每当有图片滚动时会回调此接口。
     *
     * @param listener 图片滚动监听器
     */
    public void setOnImageSwitchListener(OnImageSwitchListener listener) {
        mListener = listener;
    }

    /**
     * 设置当前显示图片的下标，注意如果该值小于零或大于等于图片的总数量，图片则无法正常显示。
     *
     * @param currentImage 图片的下标
     */
    public void setCurrentImage(int currentImage) {
        mCurrentImage = currentImage;
        requestLayout();
    }

    /**
     * 滚动到下一张图片。
     */
    public void scrollToNext() {
        if (mScroller.isFinished()) {
            int disX = mImageWidth - getScrollX();
            checkImageSwitchBorder(SCROLL_NEXT);
            if (mListener != null) {
                mListener.onImageSwitch(mCurrentImage);
            }
            beginScroll(getScrollX(), 0, disX, 0, SCROLL_NEXT);
        }
    }

    /**
     * 滚动到上一张图片。
     */
    public void scrollToPrevious() {
        if (mScroller.isFinished()) {
            int disX = -mImageWidth - getScrollX();
            checkImageSwitchBorder(SCROLL_PREVIOUS);
            if (mListener != null) {
                mListener.onImageSwitch(mCurrentImage);
            }
            beginScroll(getScrollX(), 0, disX, 0, SCROLL_PREVIOUS);
        }
    }

    /**
     * 滚动回原图片。
     */
    public void scrollBack() {
        if (mScroller.isFinished()) {
            beginScroll(getScrollX(), 0, -getScrollX(), 0, SCROLL_BACK);
        }
    }

    /**
     * 回收所有图片对象，释放内存。
     */
    public void clear() {
        for (int i = 0; i < mCount; i++) {
            Image3DView childView = (Image3DView) getChildAt(i);
            childView.recycleBitmap();
        }
    }

    /**
     * 让控件中的所有图片开始滚动。
     */
    private void beginScroll(int startX, int startY, int dx, int dy,
                             final int action) {
        int duration = (int) (700f / mImageWidth * Math.abs(dx));
        mScroller.startScroll(startX, startY, dx, dy, duration);
        invalidate();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (action == SCROLL_NEXT || action == SCROLL_PREVIOUS) {
                    forceToRelayout = true;
                    requestLayout();
                }
            }
        }, duration);
    }

    /**
     * 根据当前图片的下标和传入的item参数，来判断item位置上应该显示哪张图片。
     *
     * @param item 取值范围是1-5
     * @return 对应item位置上应该显示哪张图片。
     */
    private int getIndexForItem(int item) {
        int index = -1;
        index = mCurrentImage + item - 3;
        while (index < 0) {
            index = index + mCount;
        }
        while (index > mCount - 1) {
            index = index - mCount;
        }
        return index;
    }

    /**
     * 刷新所有图片的显示状态，包括当前的旋转角度。
     */
    private void refreshImageShowing() {
        for (int i = 0; i < mItems.length; i++) {
            Image3DView childView = (Image3DView) getChildAt(mItems[i]);
            childView.setRotateData(i, getScrollX());
            childView.invalidate();
        }
    }

    /**
     * 检查图片的边界，防止图片的下标超出规定范围。
     */
    private void checkImageSwitchBorder(int action) {
        if (action == SCROLL_NEXT && ++mCurrentImage >= mCount) {
            mCurrentImage = 0;
        } else if (action == SCROLL_PREVIOUS && --mCurrentImage < 0) {
            mCurrentImage = mCount - 1;
        }
    }

    /**
     * 判断是否应该滚动到下一张图片。
     */
    private boolean shouldScrollToNext(int velocityX) {
        return velocityX < -SNAP_VELOCITY || getScrollX() > mImageWidth / 2;
    }

    /**
     * 判断是否应该滚动到上一张图片。
     */
    private boolean shouldScrollToPrevious(int velocityX) {
        return velocityX > SNAP_VELOCITY || getScrollX() < -mImageWidth / 2;
    }

    /**
     * 图片滚动的监听器
     */
    public interface OnImageSwitchListener {

        /**
         * 当图片滚动时会回调此方法
         *
         * @param currentImage 当前图片的坐标
         */
        void onImageSwitch(int currentImage);

    }
}
