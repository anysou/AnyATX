/*
 * Copyright (C) 2012 Capricorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.anysou.as_floatlibrary.menu;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.anysou.as_floatlibrary.FloatBallManager;
import cn.anysou.as_floatlibrary.FloatUtil;


/***
 * 浮动菜单
 */
public class FloatMenu extends FrameLayout {
    private MenuLayout mMenuLayout;

    private ImageView mIconView;        //图标
    private int mPosition;
    private int mItemSize;              //每个菜单图片的尺寸
    private int mSize;                  //菜单尺寸
    private int mDuration = 250;

    private FloatBallManager floatBallManager;
    private WindowManager.LayoutParams mLayoutParams;   //浮动层参数
    private boolean isAdded = false;
    private int mBallSize;
    private FloatMenuCfg mConfig;       //菜单参数
    private boolean mListenBackEvent = true;

    public FloatMenu(Context context, final FloatBallManager floatBallManager, FloatMenuCfg config) {
        super(context);
        this.floatBallManager = floatBallManager;
        if (config == null) return;
        mConfig = config;
        mItemSize = mConfig.mItemSize;
        mSize = mConfig.mSize;
        init(context);
        mMenuLayout.setChildSize(mItemSize);
    }

    // 菜单创建
    private void init(Context context) {
        initLayoutParams(context);      //获取浮动层信息
        mLayoutParams.height = mSize;   //菜单尺寸大小
        mLayoutParams.width = mSize;
        addMenuLayout(context);         //添加菜单布局
        addControllLayout(context);     //添加菜单控制布局
        mIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });  //整个菜单范围内设置了点击事件，关闭菜单
        if (mListenBackEvent) {
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    int action = event.getAction();
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            FloatMenu.this.floatBallManager.closeMenu();
                            return true;
                        }
                    }
                    return false;
                }
            });
            setFocusableInTouchMode(true);
        }
    }

    // 获取浮动层显示信息
    private void initLayoutParams(Context context) {
        mLayoutParams = FloatUtil.getLayoutParams(context, mListenBackEvent);
    }
    // 添加菜单浮动层
    private void addMenuLayout(Context context) {
        mMenuLayout = new MenuLayout(context);  //子菜单布局
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(mSize, mSize);  //菜单尺寸
        addView(mMenuLayout, layoutParams);     //添加菜单
        mMenuLayout.setVisibility(INVISIBLE);   //此视图不可见，但仍占用空间用于布局。
    }
    // 添加控制层 每个子图标
    private void addControllLayout(Context context) {
        mIconView = new ImageView(context);
        LayoutParams sublayoutParams = new LayoutParams(mBallSize, mBallSize);
        addView(mIconView, sublayoutParams);
    }


    @SuppressWarnings("deprecation")
    public void removeViewTreeObserver(ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    // 获取菜单范围尺寸
    public int getSize() {
        return mSize;
    }

    // 关闭菜单
    public void closeMenu() {
        if (mMenuLayout.isExpanded()) {
            toggle(mDuration);
        }
    }

    // 移除菜单
    public void remove() {
        floatBallManager.reset(); //浮球复位，菜单清除
        mMenuLayout.setExpand(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (mMenuLayout.isExpanded()) {
                    toggle(mDuration);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    //添加到窗口上
    public void attachToWindow(WindowManager windowManager) {
        if (!isAdded) {
            mBallSize = floatBallManager.getBallSize();
            mLayoutParams.x = floatBallManager.floatballX;
            //mLayoutParams.y = floatBallManager.floatballY - mSize / 2;
            mLayoutParams.y = floatBallManager.floatballY;
            mPosition = computeMenuLayout(mLayoutParams);
            refreshPathMenu(mPosition);
            toggle(mDuration);
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        if (isAdded) {
            toggle(0);
            mMenuLayout.setVisibility(GONE);
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
        }
    }



    private void toggle(final int duration) {
        //duration==0 indicate that close the menu, so if it has closed, do nothing.
        if (!mMenuLayout.isExpanded() && duration <= 0) return;
        mMenuLayout.setVisibility(VISIBLE);
        if (getWidth() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMenuLayout.switchState(mPosition, duration);
                    removeViewTreeObserver(this);
                }
            });
        } else {
            mMenuLayout.switchState(mPosition, duration);
        }
    }

    public boolean isMoving() {
        return mMenuLayout.isMoving();
    }

    public void addItem(final MenuItem menuItem) {
        if (mConfig == null) return;
        ImageView imageview = new ImageView(getContext());
        imageview.setBackgroundDrawable(menuItem.mDrawable);
        mMenuLayout.addView(imageview);
        imageview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMenuLayout.isMoving())
                    menuItem.action();
            }
        });
    }

    public void removeAllItemViews() {
        mMenuLayout.removeAllViews();
    }

    /**
     * 根据按钮位置改变子菜单方向
     */
    public void refreshPathMenu(int position) {
        LayoutParams menuLp = (LayoutParams) mMenuLayout.getLayoutParams();
        LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();

        switch (position) {
            case LEFT_TOP://左上
                iconLp.gravity = Gravity.LEFT | Gravity.TOP;
                menuLp.gravity = Gravity.LEFT | Gravity.TOP;
                mMenuLayout.setArc(0, 90, position);
                break;
            case LEFT_CENTER://左中
                iconLp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                menuLp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                mMenuLayout.setArc(270, 270 + 180, position);
                break;
            case LEFT_BOTTOM://左下
                iconLp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                menuLp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                mMenuLayout.setArc(270, 360, position);
                break;
            case RIGHT_TOP://右上
                iconLp.gravity = Gravity.RIGHT | Gravity.TOP;
                menuLp.gravity = Gravity.RIGHT | Gravity.TOP;
                mMenuLayout.setArc(90, 180, position);
                break;
            case RIGHT_CENTER://右中
                iconLp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                menuLp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                mMenuLayout.setArc(90, 270, position);
                break;
            case RIGHT_BOTTOM://右下
                iconLp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                menuLp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                mMenuLayout.setArc(180, 270, position);
                break;

            case CENTER_TOP://上中
                iconLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                menuLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                mMenuLayout.setArc(0, 180, position);
                break;
            case CENTER_BOTTOM://下中
                iconLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                menuLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                mMenuLayout.setArc(180, 360, position);
                break;
            case CENTER:
                iconLp.gravity = Gravity.CENTER;
                menuLp.gravity = Gravity.CENTER;
                mMenuLayout.setArc(0, 360, position);
                break;
        }
        mIconView.setLayoutParams(iconLp);
        mMenuLayout.setLayoutParams(menuLp);
    }

    /**
     * 计算菜单中各个view的位置
     * @return
     */
    public int computeMenuLayout(WindowManager.LayoutParams layoutParams) {
        int position = FloatMenu.RIGHT_CENTER;
        final int halfBallSize = mBallSize / 3;  //原始 /2
        final int screenWidth = floatBallManager.mScreenWidth;
        final int screenHeight = floatBallManager.mScreenHeight;
        final int floatballCenterY = floatBallManager.floatballY + halfBallSize;

        int wmX = floatBallManager.floatballX;
        int wmY = floatballCenterY;

        if (wmX <= screenWidth / 3) //左边  竖区域
        {
            wmX = 0;
            if (wmY <= mSize / 2) {
                position = FloatMenu.LEFT_TOP;//左上
                wmY = floatballCenterY - halfBallSize;
            } else if (wmY > screenHeight - mSize / 2) {
                position = FloatMenu.LEFT_BOTTOM;//左下
                wmY = floatballCenterY - mSize + halfBallSize;
            } else {
                position = FloatMenu.LEFT_CENTER;//左中
                wmY = floatballCenterY - mSize / 2;
            }
        } else if (wmX >= screenWidth * 2 / 3)//右边竖区域
        {
            wmX = screenWidth - mSize;
            if (wmY <= mSize / 2) {
                position = FloatMenu.RIGHT_TOP;//右上
                wmY = floatballCenterY - halfBallSize;
            } else if (wmY > screenHeight - mSize / 2) {
                position = FloatMenu.RIGHT_BOTTOM;//右下
                wmY = floatballCenterY - mSize + halfBallSize;
            } else {
                position = FloatMenu.RIGHT_CENTER;//右中
                wmY = floatballCenterY - mSize / 2;
            }
        }
        layoutParams.x = wmX;
        layoutParams.y = wmY;
        return position;
    }

    public static final int LEFT_TOP = 1;
    public static final int CENTER_TOP = 2;
    public static final int RIGHT_TOP = 3;
    public static final int LEFT_CENTER = 4;
    public static final int CENTER = 5;
    public static final int RIGHT_CENTER = 6;
    public static final int LEFT_BOTTOM = 7;
    public static final int CENTER_BOTTOM = 8;
    public static final int RIGHT_BOTTOM = 9;
}
