package cn.anysou.as_floatlibrary.floatball;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.buyi.huxq17.serviceagency.ServiceAgency;
import com.buyi.huxq17.serviceagency.exception.AgencyException;

import cn.anysou.as_floatlibrary.FloatBallManager;
import cn.anysou.as_floatlibrary.FloatUtil;
import cn.anysou.as_floatlibrary.LocationService;
import cn.anysou.as_floatlibrary.R;
import cn.anysou.as_floatlibrary.runner.ICarrier;
import cn.anysou.as_floatlibrary.runner.OnceRunnable;
import cn.anysou.as_floatlibrary.runner.ScrollRunner;
import cn.anysou.as_floatlibrary.utils.MotionVelocityUtil;
import cn.anysou.as_floatlibrary.utils.Util;

/**
 * 浮球的形成
 */
public class FloatBall extends FrameLayout implements ICarrier {

    private FloatBallManager floatBallManager;          //浮动球管理者
    private ImageView imageView;                        //浮动图片
    public DraggableFlagView mDraggableFlagView;       //可拖动标志视图（小红点）

    private WindowManager.LayoutParams mLayoutParams;   //浮动窗口参数
    private WindowManager windowManager;                //WINDOWS 管理
    private boolean isFirst = true;                     //
    private boolean isAdded = false;
    private int mTouchSlop;                             //触发移动事件的最短距离
    /**
     * flag a touch is click event 标记触摸为单击事件
     */
    private boolean isClick;
    private int mDownX, mDownY, mLastX, mLastY;
    private int mSize;
    private ScrollRunner mRunner;                       //滚动条
    private int mVelocityX, mVelocityY;
    private MotionVelocityUtil mVelocity;               //运动速度
    private boolean sleep = false;
    private FloatBallCfg mConfig;
    private boolean mHideHalfLater = true;
    private boolean mLayoutChanged = false;
    private int mSleepX = -1;
    private boolean isLocationServiceEnable;        //浮点位置记忆是否有效
    private OnceRunnable mSleepRunnable = new OnceRunnable() {
        @Override
        public void onRun() {
            if (mHideHalfLater && !sleep && isAdded) {
                sleep = true;
                moveToEdge(false, sleep);
                mSleepX = mLayoutParams.x;
            }
        }
    };

    // 浮球创建        上下文创建在哪            浮动管理                     浮动配置
    public FloatBall(Context context, FloatBallManager floatBallManager, FloatBallCfg config) {
        super(context);
        this.floatBallManager = floatBallManager;
        mConfig = config;
        try {
            //需要记录并在再次打开app的时候恢复位置
            ServiceAgency.getService(LocationService.class);
            isLocationServiceEnable = true;
        } catch (AgencyException e) {
            isLocationServiceEnable = false;
        }
        init(context);
    }

    // 初始化浮球
    private void init(final Context context) {
        if(!this.floatBallManager.FloatBallNew) {
            imageView = new ImageView(context);
            final Drawable icon = mConfig.mIcon;   //图标
            mSize = mConfig.mSize;                 //尺寸
            Util.setBackground(imageView, icon);   //设置背景图片
            addView(imageView, new ViewGroup.LayoutParams(mSize, mSize));  //添加VIEW
        }
        else {  //新模式
            //浮动窗口
            LayoutInflater.from(context).inflate(R.layout.float_littlemonk_layout, this);
            mSize = mConfig.mSize;                 //尺寸
            //浮动窗口图标
            imageView = (ImageView) findViewById(R.id.float_id);
            mDraggableFlagView = (DraggableFlagView) findViewById(R.id.main_dfv);

//            mDraggableFlagView.setOnDraggableFlagViewListener(new DraggableFlagView.OnDraggableFlagViewListener() {
//                @Override
//                public void onFlagDismiss(DraggableFlagView view) {
//                    //小红点消失的一些操作
//                    Toast.makeText(context,"小红点消失",Toast.LENGTH_SHORT).show();
//                }
//            });

            setObtainVisibility(false);  //不显示红点
        }

        initLayoutParams(context);             //获取显示浮球的相关参数

        //它获得的是触发移动事件的最短距离，如果小于这个距离就不触发移动控件
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mRunner = new ScrollRunner(this);       //滚动条
        mVelocity = new MotionVelocityUtil(context);   //运动速度
    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatUtil.getLayoutParams(context);
    }


    /**
     * 设置小红点是否显示
     */
    public void setObtainVisibility(boolean visibility) {
        if (visibility) {
            mDraggableFlagView.setVisibility(View.VISIBLE);
        } else {
            mDraggableFlagView.setVisibility(View.GONE);
        }
    }
    /**
     * 获取小红点数量
     */
    public int getObtainNumber() {
        String oldText = mDraggableFlagView.getText();
        int Number = 0;
        try {
            Number = Integer.parseInt(oldText);
        }  catch (Exception e){
            Number = 0;
        }
        return Number;
    }
    /**
     * 设置小红点数量
     */
    public void setObtainNumber(int number) {
        mDraggableFlagView.setText(number + "");
    }
    /**
     * 设置小红点数量+1
     */
    public void addObtainNumer() {
        String oldText = mDraggableFlagView.getText();
        int Number = 0;
        try {
            Number = Integer.parseInt(oldText)+1;
        }  catch (Exception e){
            Number = 1;
        }
        mDraggableFlagView.setText(Number+"");
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            onConfigurationChanged(null);
        }
    }

    public void attachToWindow(WindowManager windowManager) {
        this.windowManager = windowManager;
        if (!isAdded) {
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        this.windowManager = null;
        if (isAdded) {
            removeSleepRunnable();
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
            sleep = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int curX = mLayoutParams.x;
        if (sleep && curX != mSleepX && !mRunner.isRunning()) {
            sleep = false;
            postSleepRunnable();
        }
        if (mRunner.isRunning()) {
            mLayoutChanged = false;
        }
        if (height != 0 && isFirst || mLayoutChanged) {
            if (isFirst && height != 0) {
                location(width, height);
            } else {
                moveToEdge(false, sleep);
            }
            isFirst = false;
            mLayoutChanged = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        floatBallManager.floatballX = mLayoutParams.x;
        floatBallManager.floatballY = mLayoutParams.y;
    }

    private void location(int width, int height) {
        FloatBallCfg.Gravity cfgGravity = mConfig.mGravity;
        mHideHalfLater = mConfig.mHideHalfLater;
        int gravity = cfgGravity.getGravity();
        int x;
        int y;
        int topLimit = 0;
        int bottomLimit = floatBallManager.mScreenHeight - height;
        int statusBarHeight = floatBallManager.getStatusBarHeight();
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            x = 0;
        } else {
            x = floatBallManager.mScreenWidth - width;
        }
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            y = topLimit;
        } else if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            y = floatBallManager.mScreenHeight - height - statusBarHeight;
        } else {
            y = floatBallManager.mScreenHeight / 2 - height / 2 - statusBarHeight;
        }
        y = mConfig.mOffsetY != 0 ? y + mConfig.mOffsetY : y;
        if (y < 0) y = topLimit;
        if (y > bottomLimit)
            y = topLimit;
        if (isLocationServiceEnable) {
            LocationService locationService = ServiceAgency.getService(LocationService.class);
            int[] location = locationService.onRestoreLocation();
            if (location.length == 2) {
                int locationX = location[0];
                int locationY = location[1];
                if (locationX != -1 && locationY != -1) {
                    onLocation(locationX, locationY);
                    return;
                }
            }
        }
        onLocation(x, y);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutChanged = true;
        floatBallManager.onConfigurationChanged(newConfig);
        moveToEdge(false, false);
        postSleepRunnable();
    }

    public void onLayoutChange() {
        mLayoutChanged = true;
        requestLayout();
    }

    //设置悬浮窗的Touch监听
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        mVelocity.acquireVelocityTracker(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchUp();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void touchDown(int x, int y) {
        mDownX = x;
        mDownY = y;
        mLastX = mDownX;
        mLastY = mDownY;
        isClick = true;
        removeSleepRunnable();
    }

    private void touchMove(int x, int y) {
        int totalDeltaX = x - mDownX;
        int totalDeltaY = y - mDownY;
        int deltaX = x - mLastX;
        int deltaY = y - mLastY;
        if (Math.abs(totalDeltaX) > mTouchSlop || Math.abs(totalDeltaY) > mTouchSlop) {
            isClick = false;
        }
        mLastX = x;
        mLastY = y;
        if (!isClick) {
            onMove(deltaX, deltaY);
        }
    }

    private void touchUp() {
        mVelocity.computeCurrentVelocity();
        mVelocityX = (int) mVelocity.getXVelocity();
        mVelocityY = (int) mVelocity.getYVelocity();
        mVelocity.releaseVelocityTracker();
        if (sleep) {
            wakeUp();
        } else {
            if (isClick) {
                onClick();
            } else {
                moveToEdge(true, false);
            }
        }
        mVelocityX = 0;
        mVelocityY = 0;
    }

    private void moveToX(boolean smooth, int destX) {
        int statusBarHeight = floatBallManager.getStatusBarHeight();
        final int screenHeight = floatBallManager.mScreenHeight - statusBarHeight;
        int height = getHeight();
        int destY = 0;
        if (mLayoutParams.y < 0) {
            destY = 0 - mLayoutParams.y;
        } else if (mLayoutParams.y > screenHeight - height) {
            destY = screenHeight - height - mLayoutParams.y;
        }
        if (smooth) {
            int dx = destX - mLayoutParams.x;
            int duration = getScrollDuration(Math.abs(dx));
            mRunner.start(dx, destY, duration);
        } else {
            onMove(destX - mLayoutParams.x, destY);
            postSleepRunnable();
        }
    }

    private void wakeUp() {
        final int screenWidth = floatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        destX = mLayoutParams.x < centerX ? 0 : screenWidth - width;
        sleep = false;
        moveToX(true, destX);
    }

    private void moveToEdge(boolean smooth, boolean forceSleep) {
        final int screenWidth = floatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        final int minVelocity = mVelocity.getMinVelocity();
        if (mLayoutParams.x < centerX) {
            sleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX < 0 || mLayoutParams.x < 0;
            destX = sleep ? -halfWidth : 0;
        } else {
            sleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX > 0 || mLayoutParams.x > screenWidth - width;
            destX = sleep ? screenWidth - halfWidth : screenWidth - width;
        }
        if (sleep) {
            mSleepX = destX;
        }
        moveToX(smooth, destX);
    }

    private int getScrollDuration(int distance) {
        return (int) (250 * (1.0f * distance / 800));
    }

    private void onMove(int deltaX, int deltaY) {
        mLayoutParams.x += deltaX;
        mLayoutParams.y += deltaY;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public void onLocation(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public void onMove(int lastX, int lastY, int curX, int curY) {
        onMove(curX - lastX, curY - lastY);
    }

    @Override
    public void onDone() {
        postSleepRunnable();
        if (isLocationServiceEnable) {
            LocationService locationService = ServiceAgency.getService(LocationService.class);
            locationService.onLocationChanged(mLayoutParams.x, mLayoutParams.y);
        }
    }

    private void moveTo(int x, int y) {
        mLayoutParams.x += x - mLayoutParams.x;
        mLayoutParams.y += y - mLayoutParams.y;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public int getSize() {
        return mSize;
    }

    private void onClick() {
        floatBallManager.floatballX = mLayoutParams.x;
        floatBallManager.floatballY = mLayoutParams.y;
        floatBallManager.onFloatBallClick();
    }

    private void removeSleepRunnable() {
        mSleepRunnable.removeSelf(this);
    }

    public void postSleepRunnable() {
        if (mHideHalfLater && !sleep && isAdded) {
            mSleepRunnable.postDelaySelf(this, 3000);
        }
    }
}
