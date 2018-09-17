package com.bihucj.mcandroid.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.bihucj.mcandroid.utils.DensityUtil;

/**
 * Created by 孟晨 on 2018/9/17.
 */

/*在Image3DView的构造函数中初始化了一个Camera和Matrix对象，用于在后面对图片进行3D操作。
然后在initImageViewBitmap()方法中初始化了一些必要的信息，比如对当前图片进行截图，
以用于后续的立体操作，得到当前图片的宽度等。然后还提供了一个setRotateData()方法，
用于设置当前图片的下标和滚动距离，有了这两样数据就可以通过computeRotateData()方法来计算旋转角度的一些数据，
以及通过isImageVisible()方法来判断出当前图片是否可见了，具体详细的算法逻辑你可以阅读代码来慢慢分析。
接下来当图片需要绘制到屏幕上的时候就会调用onDraw()方法，在onDraw()方法中会进行判断，
如果当前图片可见就调用computeRotateData()方法来计算旋转时所需要的各种数据，之后再通过Camera和Matrix来执行旋转操作就可以了。*/

public class Image3DView extends android.support.v7.widget.AppCompatImageView {
    /**
     * 旋转角度的基准值
     */
    private static final float BASE_DEGREE = 50f;
    /**
     * 旋转深度的基准值
     */
    private static final float BASE_DEEP = 150f;
    private Camera mCamera;
    private Matrix mMaxtrix;
    private Bitmap mBitmap;
    /**
     * 当前图片对应的下标
     */
    private int mIndex;
    /**
     * 在前图片在X轴方向滚动的距离
     */
    private int mScrollX;
    /**
     * Image3DSwitchView控件的宽度
     */
    private int mLayoutWidth;
    /**
     * 当前图片的宽度
     */
    private int mWidth;
    /**
     * 当前旋转的角度
     */
    private float mRotateDegree;
    /**
     * 旋转的中心点
     */
    private float mDx;
    /**
     * 旋转的深度
     */
    private float mDeep;

    private Context context;

    public Image3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mCamera = new Camera();
        mMaxtrix = new Matrix();
    }

    /**
     * 初始化Image3DView所需要的信息，包括图片宽度，截取背景图等。
     */
    public void initImageViewBitmap() {
        if (mBitmap == null) {
            setDrawingCacheEnabled(true);
            buildDrawingCache();
            mBitmap = getDrawingCache();
        }
        mLayoutWidth = Image3DSwitchView.mWidth;
        mWidth = getWidth() + Image3DSwitchView.IMAGE_PADDING * 2;
    }

    /**
     * 设置旋转角度。
     *
     * @param index   当前图片的下标
     * @param scrollX 当前图片在X轴方向滚动的距离
     */
    public void setRotateData(int index, int scrollX) {
        mIndex = index;
        mScrollX = scrollX;
    }

    /**
     * 回收当前的Bitmap对象，以释放内存。
     */
    public void recycleBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = null;
        initImageViewBitmap();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = null;
        initImageViewBitmap();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = null;
        initImageViewBitmap();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = null;
        initImageViewBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            // 如果Bitmap对象还不存在，先使用父类的onDraw方法进行绘制
            super.onDraw(canvas);
        } else {
            if (isImageVisible()) {
                //获取圆角img
                mBitmap = getRoundBitmap(mBitmap, DensityUtil.dip2px(context, 5));
                // 绘图时需要注意，只有当图片可见的时候才进行绘制，这样可以节省运算效率
                computeRotateData();
                mCamera.save();
                mCamera.translate(0.0f, 0.0f, mDeep);
                mCamera.rotateY(mRotateDegree);
                mCamera.getMatrix(mMaxtrix);
                mCamera.restore();
                mMaxtrix.preTranslate(-mDx, -getHeight() / 2);
                mMaxtrix.postTranslate(mDx, getHeight() / 2);
                canvas.drawBitmap(mBitmap, mMaxtrix, null);
            }
        }
    }

    /**
     * 在这里计算所有旋转所需要的数据。
     */
    private void computeRotateData() {
        float degreePerPix = BASE_DEGREE / mWidth;
        float deepPerPix = BASE_DEEP / ((mLayoutWidth - mWidth) / 2);
        switch (mIndex) {
            case 0:
                mDx = mWidth;
                mRotateDegree = 360f - (2 * mWidth + mScrollX) * degreePerPix;
                if (mScrollX < -mWidth) {
                    mDeep = 0;
                } else {
                    mDeep = (mWidth + mScrollX) * deepPerPix;
                }
                break;
            case 1:
                if (mScrollX > 0) {
                    mDx = mWidth;
                    mRotateDegree = (360f - BASE_DEGREE) - mScrollX * degreePerPix;
                    mDeep = mScrollX * deepPerPix;
                } else {
                    if (mScrollX < -mWidth) {
                        mDx = -Image3DSwitchView.IMAGE_PADDING * 2;
                        mRotateDegree = (-mScrollX - mWidth) * degreePerPix;
                    } else {
                        mDx = mWidth;
                        mRotateDegree = 360f - (mWidth + mScrollX) * degreePerPix;
                    }
                    mDeep = 0;
                }
                break;
            case 2:
                if (mScrollX > 0) {
                    mDx = mWidth;
                    mRotateDegree = 360f - mScrollX * degreePerPix;
                    mDeep = 0;
                    if (mScrollX > mWidth) {
                        mDeep = (mScrollX - mWidth) * deepPerPix;
                    }
                } else {
                    mDx = -Image3DSwitchView.IMAGE_PADDING * 2;
                    mRotateDegree = -mScrollX * degreePerPix;
                    mDeep = 0;
                    if (mScrollX < -mWidth) {
                        mDeep = -(mWidth + mScrollX) * deepPerPix;
                    }
                }
                break;
            case 3:
                if (mScrollX < 0) {
                    mDx = -Image3DSwitchView.IMAGE_PADDING * 2;
                    mRotateDegree = BASE_DEGREE - mScrollX * degreePerPix;
                    mDeep = -mScrollX * deepPerPix;
                } else {
                    if (mScrollX > mWidth) {
                        mDx = mWidth;
                        mRotateDegree = 360f - (mScrollX - mWidth) * degreePerPix;
                    } else {
                        mDx = -Image3DSwitchView.IMAGE_PADDING * 2;
                        mRotateDegree = BASE_DEGREE - mScrollX * degreePerPix;
                    }
                    mDeep = 0;
                }
                break;
            case 4:
                mDx = -Image3DSwitchView.IMAGE_PADDING * 2;
                mRotateDegree = (2 * mWidth - mScrollX) * degreePerPix;
                if (mScrollX > mWidth) {
                    mDeep = 0;
                } else {
                    mDeep = (mWidth - mScrollX) * deepPerPix;
                }
                break;
        }
    }

    /**
     * 判断当前图片是否可见。
     *
     * @return 当前图片可见返回true，不可见返回false。
     */
    private boolean isImageVisible() {
        boolean isVisible = false;
        switch (mIndex) {
            case 0:
                if (mScrollX < (mLayoutWidth - mWidth) / 2 - mWidth) {
                    isVisible = true;
                } else {
                    isVisible = false;
                }
                break;
            case 1:
                if (mScrollX > (mLayoutWidth - mWidth) / 2) {
                    isVisible = false;
                } else {
                    isVisible = true;
                }
                break;
            case 2:
                if (mScrollX > mLayoutWidth / 2 + mWidth / 2
                        || mScrollX < -mLayoutWidth / 2 - mWidth / 2) {
                    isVisible = false;
                } else {
                    isVisible = true;
                }
                break;
            case 3:
                if (mScrollX < -(mLayoutWidth - mWidth) / 2) {
                    isVisible = false;
                } else {
                    isVisible = true;
                }
                break;
            case 4:
                if (mScrollX > mWidth - (mLayoutWidth - mWidth) / 2) {
                    isVisible = true;
                } else {
                    isVisible = false;
                }
                break;
        }
        return isVisible;
    }


    //圆角img
    private Paint paint;

    private Bitmap getRoundBitmap(Bitmap bitmap, int roundPx) {
        paint = new Paint();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bitmap.getWidth();

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;


    }

}
