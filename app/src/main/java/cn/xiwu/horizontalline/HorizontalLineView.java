package cn.xiwu.horizontalline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


import java.util.ArrayList;
import java.util.List;

/**
 * Created xiwu on 2018/3/14.
 */

public class HorizontalLineView extends View
{
    private VelocityTracker mVelocityTracker;
    private int mTouchX;
    private int mTouchY;
    private int mMoveX;
    private int mMeasureWidth;
    private int mMeasureHeight;
    private Scroller mScroller;
    /**
     * 一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件
     */
    private double mTouchSlop;
    //背景颜色
    private int mBgColor = getColor(R.color.white);
    //默认文字颜色
    private int mTextColorDefault = getColor(R.color.green);
    private int mTextColorSelected = getColor(R.color.orange);
    //折线图中折线的颜色
    private int mLinecolor = getColor(R.color.orange_deep);
    //线的总长度
    private int monthLineWidth;
    //xy坐标轴文字,线上字体默认大小
    private int mTextSizeDefault = spToPx(14);
    //选中线上字体默认的大小
    private int mTextSizeSelected = spToPx(16);
    //线上字距离点的距离
    private int mTextdistancePoint = dpToPx(4.5f);
    //字体所占高度
    private int xValueTextHeight = dpToPx(16);
    private OnSelectedChangedListener mOnSelectedChangedListener;
    private int mMaxFlingVelocity;

    //线上面的数字
    List<Float> mValueTotalList = new ArrayList<>();
    //X轴上面的数字
    List<String> mXValueList = new ArrayList<>();

    //画X轴上面的文字
    private Paint mXTextPaint;
    //画折线对应的画笔
    private Paint mLinePaint;
    //点击选中线上的点
    private int mIsSelected = -1;
    //y轴坐标对应的数据
    private float yValue = -1f;
    //y轴坐标对应的数据
    private float yValue2 = -1;
    //y轴坐标对应的数据
    private float yValue3 = -1f;
    //线上点数值最大值
    private float maxValue = 1;
    //文字的高度
    //private int mTextHeight;
    private int mPaddingTop = 0;
    private int mPaddingBottom = 0;
    private IOnScrollStateListener mIOnScrollStateListener;
    //标志，滚动到底部
    private boolean mIsStop;
    //标志，防止加载更多调用多次
    private boolean mIsScrollBottom;
    //画点上面的文字
    private Paint mValueTextPaint;

    public HorizontalLineView(Context context)
    {
        this(context, null);
    }

    public HorizontalLineView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public HorizontalLineView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr)
    {

        setScrollContainer(true);
        setFocusableInTouchMode(true);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalLineView, defStyleAttr, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++)
        {
            int attr = array.getIndex(i);
            Log.d("3699linecolor", attr + "");
            switch (attr)
            {
                case R.styleable.HorizontalLineView_textcolordefault://默认字的颜色
                    mTextColorDefault = array.getColor(attr, mTextColorDefault);
                    Log.d("3699linecorleo------",mTextColorDefault+"");
                    break;
                case R.styleable.HorizontalLineView_linecolor://默认字的颜色
                    mLinecolor = array.getColor(attr, mTextColorSelected);
                    Log.d("3699linecorleo",mLinecolor+"");
                    break;
                case R.styleable.HorizontalLineView_textcolorselected://选中字的颜色
                    mTextColorSelected = array.getColor(attr, mTextColorSelected);
                    break;
                case R.styleable.HorizontalLineView_textsizedefault://默认文字大小
                    mTextSizeDefault = (int) array.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mTextSizeDefault, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.HorizontalLineView_textsizeselected://选中字体大小
                    mTextSizeSelected = (int) array.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mTextSizeSelected, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.HorizontalLineView_bg://背景颜色
                    mBgColor = array.getColor(attr, mBgColor);
                    break;
            }
        }
        array.recycle();
        initPaint();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mTouchX = (int) event.getX();
                mTouchY = (int) event.getY();
                mMoveX = mTouchX;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (monthLineWidth > mMeasureWidth)
                {
                    int dx = (int) event.getX() - mMoveX;
                    if (dx > 0)
                    { // 右滑
                        Log.v("3699右滑", "********" + mScroller.getFinalX());
                        if (mScroller.getFinalX() > 0)
                        {
                            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), -dx, 0, 400);
                        }
                        else
                        {
                            mScroller.setFinalX(0);
                            if (mIOnScrollStateListener != null && !mIsScrollBottom)
                            {
                                Log.v("3699加载185", "0000000000000");
                                mIsScrollBottom = true;
                                mIOnScrollStateListener.onScrollbottom();
                            }
                        }
                    }
                    else
                    { //左滑
                        Log.v("3699左滑", "---------" + (mScroller.getFinalX() + mMeasureWidth - dx < monthLineWidth) + "++++" + monthLineWidth +
                                "·······" + (mScroller.getFinalX() + mMeasureWidth - dx));
                        if (mScroller.getFinalX() + mMeasureWidth - dx < monthLineWidth)
                        {
                            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), -dx, 0, 400);
                        }
                        else
                        {
                            mScroller.setFinalX(monthLineWidth - mMeasureWidth);
                        }
                    }
                    mMoveX = (int) event.getX();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (monthLineWidth > mMeasureWidth)
                {
                    final MotionEvent vtev = MotionEvent.obtain(event);
                    final ViewConfiguration vc = ViewConfiguration.get(getContext());
                    mTouchSlop = vc.getScaledTouchSlop();
                    mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
                    mVelocityTracker.addMovement(vtev);
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                    int max = Math.max(Math.abs(mScroller.getCurrX()), Math.abs(monthLineWidth - mMeasureWidth - mScroller.getCurrX()));
                    mIsStop = false;
                    mScroller.fling(mScroller.getFinalX(), mScroller.getFinalY()
                            , (int) -mVelocityTracker.getXVelocity(), (int) -mVelocityTracker.getYVelocity(),
                            0, monthLineWidth - mMeasureWidth, mScroller.getFinalY(), mScroller.getFinalY());
                    //手指抬起时，根据滚动偏移量初始化位置
                    float lv = ((mScroller.getFinalX() - mMeasureWidth / 10) * 1.00f) / (mMeasureWidth / 5);
                    Log.v("3699max", max + "*****" + mScroller.getCurrX() + "--->" + ((int) lv) * mMeasureWidth / 5 + "-----lv>" + lv
                            + "   getFinalX:" + mScroller.getFinalX());
                    Log.v("3699lv", lv - ((int) lv) + "");

                    if (mScroller.getFinalX() < 0)
                    {
                        Log.v("3699145", "1451451545145");
                        mScroller.abortAnimation();
                        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), -mScroller.getCurrX(), 0, 400);
                    }
                    else if (mScroller.getFinalX() > monthLineWidth - mMeasureWidth)
                    {
                        Log.v("3699145", "151151151151151");
                        mScroller.abortAnimation();
                        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), monthLineWidth - mMeasureWidth - mScroller.getCurrX(), 0, 400);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    mIsScrollBottom = false;
                    Log.v("3699up", "***" + mScroller.getFinalX());
                    int mUpX = (int) event.getX();
                    int mUpY = (int) event.getY();
                    //模拟点击操作
                    if (Math.abs(mUpX - mTouchX) <= mTouchSlop && Math.abs(mUpY - mTouchY) <= mTouchSlop)
                    {
                        for (int i = 0; i < mValueTotalList.size(); i++)
                        {
                            if (Math.abs((mScroller.getCurrX() + mUpX) - (mMeasureWidth / 10 + (i * mMeasureWidth / 5))) <= 15)
                            {
                                setSelected(i);
                                Log.v("3699点击", "" + i);
                                return super.onTouchEvent(event);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void drawLineAndPoint(Canvas canvas)
    {
        Log.w("3699lineview", mIsSelected + "*" + yValue2 + "*" + yValue3);
        for (int i = 0; i < mValueTotalList.size(); i++)
        {
            if (maxValue < mValueTotalList.get(i))
            {
                maxValue = mValueTotalList.get(i);
            }
        }
        for (int i = 0; i < mValueTotalList.size(); i++)
        {
            float valuei = (maxValue - mValueTotalList.get(i)) * mMeasureHeight / maxValue + xValueTextHeight + mPaddingTop;
            String testString = String.valueOf(mValueTotalList.get(i));
            mValueTextPaint.setColor(mIsSelected == i ? mTextColorSelected : mTextColorDefault);
            mXTextPaint.setColor(mIsSelected == i ? mTextColorSelected : mTextColorDefault);
            mValueTextPaint.setTextSize(mIsSelected == i ? mTextSizeSelected : mTextSizeDefault);
            Rect bounds = getTextBounds(testString, mValueTextPaint);
            Rect bou = getTextBounds(mXValueList.get(i), mValueTextPaint);
            canvas.drawText(mXValueList.get(i),
                    mMeasureWidth / 10 + i * mMeasureWidth / 5 - bou.width() / 2, mMeasureHeight + 2 * xValueTextHeight + mPaddingTop,
                    mXTextPaint);

            if (i == 0)
            {
                canvas.drawCircle(mMeasureWidth / 10, valuei, dpToPx(3.5f), mLinePaint);
                canvas.drawText(testString, mMeasureWidth / 10 - bounds.width() / 2, valuei - mTextdistancePoint, mValueTextPaint);
            }
            else
            {
                float valuei1 = (maxValue - mValueTotalList.get(i - 1)) * mMeasureHeight / maxValue + xValueTextHeight + mPaddingTop;
                canvas.drawLine(mMeasureWidth / 10 + (i - 1) * mMeasureWidth / 5, valuei1
                        , mMeasureWidth / 10 + i * mMeasureWidth / 5, valuei, mLinePaint);// 画线
                canvas.drawCircle(mMeasureWidth / 10 + i * mMeasureWidth / 5, valuei, dpToPx(3.5f), mLinePaint);
                Log.v("3699318", bounds.width() + "");
                canvas.drawText(testString,
                        mMeasureWidth / 10 + i * mMeasureWidth / 5 - bounds.width() / 2, valuei - mTextdistancePoint,
                        mValueTextPaint);
            }
        }
    }

    /**
     * 获取丈量文本的矩形
     *
     * @param text
     * @param paint
     * @return
     */
    private Rect getTextBounds(String text, Paint paint)
    {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    /**
     * 初始化畫筆
     */
    private void initPaint()
    {
        mValueTextPaint = new Paint();
        mValueTextPaint.setAntiAlias(true);
        mValueTextPaint.setTextSize(mTextSizeDefault);
        //mValueTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mValueTextPaint.setColor(mTextColorDefault);
        //mValueTextPaint.setStyle(Paint.Style.STROKE);
        mValueTextPaint.setStyle(Paint.Style.FILL);

        mXTextPaint = new Paint();
        mXTextPaint.setAntiAlias(true);
        mXTextPaint.setTextSize(mTextSizeDefault);
        //mValueTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mXTextPaint.setColor(mTextColorDefault);
        //mValueTextPaint.setStyle(Paint.Style.STROKE);
        mXTextPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setColor(mLinecolor);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(dpToPx(1.3f));
    }

    @Override
    public void computeScroll()
    {
        Log.v("3699|||", "|||||||||||");
        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset())
        {
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        if (mScroller.isFinished() && !mIsStop)
        {
            mIsStop = true;
            /*if (mIOnScrollStateListener!=null){
                mIOnScrollStateListener.onScrollStop();
            }*/
            int lv = mScroller.getFinalX() % (mMeasureWidth / 5);
            int n = mScroller.getFinalX() / (mMeasureWidth / 5);

            Log.v("3699停止", mScroller.getCurrX() + "    " + mScroller.getFinalX() + "      " + mMeasureWidth / 5 + "   lv:" + lv);
            //if (Math.abs(lv - ((int) lv)) < 0.1)
            {
                mScroller.abortAnimation();
                if (lv > mMeasureWidth / 10)//+
                {
                    mScroller.startScroll(mScroller.getFinalX(), mScroller.getCurrY(),
                            (mMeasureWidth / 5 - lv), 0, 400);
                    if (mIOnScrollStateListener != null)
                    {
                        mIOnScrollStateListener.onScrollStop(n + 1);
                    }
                }
                else
                {
                    mScroller.startScroll(mScroller.getFinalX(), mScroller.getCurrY(),
                            -lv, 0, 400);
                    if (mIOnScrollStateListener != null)
                    {
                        mIOnScrollStateListener.onScrollStop(n);
                    }
                }
            }
        }
        super.computeScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        mMeasureWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMeasureHeight = MeasureSpec.getSize(heightMeasureSpec) - mTextdistancePoint - 2 * xValueTextHeight - mPaddingBottom - mPaddingTop;
        //初始化Item
        initItems();
        Log.v("3699measure", mMeasureWidth + "****" + mPaddingTop + "<----->" + mMeasureHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initItems()
    {
        monthLineWidth = 0;
        monthLineWidth += (mValueTotalList.size()) * mMeasureWidth / 5;
        //重绘
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        drawLineAndPoint(canvas);
    }

    public void setSelected(int position)
    {
        mIsSelected = position;
        if (mOnSelectedChangedListener != null)
        {
            mOnSelectedChangedListener.onChanged(position);
        }
        invalidate();
    }

    public HorizontalLineView color(int colorDefault, int colorSelected, int linecolor)
    {
        this.mTextColorDefault = getColor(colorDefault);
        this.mTextColorSelected = getColor(colorSelected);
        this.mLinecolor = getColor(linecolor);
        return this;
    }

    public HorizontalLineView textSize(int textSizeDefault, int textSizeSelected)
    {
        this.mTextSizeDefault = textSizeDefault;
        this.mTextSizeSelected = textSizeSelected;
        return this;
    }

    public HorizontalLineView value(ArrayList<Float> values)
    {
        Log.d("3699selected", values.size() + "<----va    " + mValueTotalList.size() + "<----yuan     " + mIsSelected + "<--------->");
        mPaddingBottom = this.getPaddingBottom();
        mPaddingTop = this.getPaddingTop();
        mMeasureHeight = mMeasureHeight - mPaddingTop - mPaddingBottom;
        int i = values.size() - mValueTotalList.size();
        if (mIsSelected >= 0)
        {
            mIsSelected += i;
        }
        this.mValueTotalList.clear();
        this.mValueTotalList.addAll(values);
        invalidate();
        return this;
    }

    public HorizontalLineView xvalue(ArrayList<String> values)
    {
        this.mXValueList = values;
        initItems();
        return this;
    }

    public HorizontalLineView listener(OnSelectedChangedListener listener)
    {
        this.mOnSelectedChangedListener = listener;
        return this;
    }

    /***
     * 获取颜色值
     ***/
    public int getColor(int resId)
    {
        return getResources().getColor(resId);
    }

    /**
     * dp转化成为px
     *
     * @param dp
     * @return
     */
    private int dpToPx(float dp)
    {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }

    /**
     * sp转化为px
     *
     * @param sp
     * @return
     */
    private int spToPx(int sp)
    {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (scaledDensity * sp + 0.5f * (sp >= 0 ? 1 : -1));
    }

    public interface OnSelectedChangedListener
    {
        void onChanged(int position);
    }

    public HorizontalLineView setIOnScrollStateListener(IOnScrollStateListener listener)
    {
        this.mIOnScrollStateListener = listener;
        return this;
    }

    public interface IOnScrollStateListener
    {
        void onScrolling(int firstposition);

        void onScrollStop(int firstposition);

        void onScrollbottom();
    }
}
