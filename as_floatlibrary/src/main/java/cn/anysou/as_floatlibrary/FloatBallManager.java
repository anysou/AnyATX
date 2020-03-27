package cn.anysou.as_floatlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import cn.anysou.as_floatlibrary.floatball.FloatBall;
import cn.anysou.as_floatlibrary.floatball.FloatBallCfg;
import cn.anysou.as_floatlibrary.floatball.StatusBarView;
import cn.anysou.as_floatlibrary.menu.FloatMenu;
import cn.anysou.as_floatlibrary.menu.FloatMenuCfg;
import cn.anysou.as_floatlibrary.menu.MenuItem;

/***
 * 浮动球 管理类
 * 参考 https://github.com/huxq17/FloatBall
 */
public class FloatBallManager {
    public int mScreenWidth, mScreenHeight;     //屏幕的尺寸
    private IFloatBallPermission mPermission;   //权限
    private OnFloatBallClickListener mFloatballClickListener;   //浮球点击事件监听
    private WindowManager mWindowManager;       //WINDOW管理

    public FloatBall floatBall;                 //浮球
    private FloatMenu floatMenu;                //浮球菜单
    private StatusBarView statusBarView;        //状态栏视图
    public int floatballX, floatballY;          //浮球的坐标
    public boolean isShowing = false;           //浮球是否显示
    private List<MenuItem> menuItems = new ArrayList<>();   //菜单列表
    private Context mContext;                   //上下文
    private Activity mActivity;                 //Activity

    public boolean FloatBallNew = true;        //浮动球新模式(ADD添加小红点)

    //设置是否为新模式
    public void setFloatBallMode(boolean NewMode) {
        this.FloatBallNew = NewMode;
    }

    //==== 浮球 在 application 整个APP
    public FloatBallManager(Context application, FloatBallCfg ballCfg) {
        this(application, ballCfg, null);
    }
    public FloatBallManager(Context application, FloatBallCfg ballCfg, FloatMenuCfg menuCfg) {
        mContext = application.getApplicationContext();
        FloatUtil.inSingleActivity = false;    //浮动球不是在单一的Activity
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();  //屏幕尺寸

        floatBall = new FloatBall(mContext, this, ballCfg);  //建立浮球
        floatMenu = new FloatMenu(mContext, this, menuCfg);  //建立浮球菜单
        statusBarView = new StatusBarView(mContext, this);   //状态栏视图
    }
    //==== 浮球 在 activity 指定页面
    public FloatBallManager(Activity activity, FloatBallCfg ballCfg) {
        this(activity, ballCfg, null);
    }
    public FloatBallManager(Activity activity, FloatBallCfg ballCfg, FloatMenuCfg menuCfg) {
        mActivity = activity;
        FloatUtil.inSingleActivity = true;   //在单一的Activity
        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        floatBall = new FloatBall(mActivity, this, ballCfg);
        floatMenu = new FloatMenu(mActivity, this, menuCfg);
        statusBarView = new StatusBarView(mActivity, this);
    }


    //============ 菜单管理 ========================
    // 获取菜单的数量
    public int getMenuItemSize() {
        return menuItems != null ? menuItems.size() : 0;
    }
    // 菜单绑定形成
    public void buildMenu() {
        inflateMenuItem();
    }
    private void inflateMenuItem() {
        floatMenu.removeAllItemViews();
        for (MenuItem item : menuItems) {
            floatMenu.addItem(item);
        }
        floatMenu.closeMenu();
    }
    /**
     * 添加一个菜单条目     *
     * @param item
     */
    public FloatBallManager addMenuItem(MenuItem item) {
        menuItems.add(item);
        return this;
    }
    /**
     * 设置菜单     *
     * @param items
     */
    public FloatBallManager setMenu(List<MenuItem> items) {
        menuItems = items;
        return this;
    }
    //关闭菜单
    public void closeMenu() {
        floatMenu.closeMenu();
    }


    //============== 浮球管理 =============================

    // 浮球复位，菜单清除
    public void reset() {
        floatBall.setVisibility(View.VISIBLE);
        floatBall.postSleepRunnable();
        floatMenu.detachFromWindow(mWindowManager);
    }

    // 显示 浮球
    public void show() {
        if (mActivity == null) {
            if (mPermission == null) {
                return;
            }
            if (!mPermission.hasFloatBallPermission(mContext)) {
                mPermission.onRequestFloatBallPermission();
                return;
            }
        }
        if (isShowing) return;
        isShowing = true;
        floatBall.setVisibility(View.VISIBLE);
        statusBarView.attachToWindow(mWindowManager);
        floatBall.attachToWindow(mWindowManager);
        floatMenu.detachFromWindow(mWindowManager);
    }

    // 隐藏浮球
    public void hide() {
        floatMenu.closeMenu();  //关闭菜单
        if (!isShowing) return;
        isShowing = false;
        floatBall.detachFromWindow(mWindowManager);
        floatMenu.detachFromWindow(mWindowManager);
        statusBarView.detachFromWindow(mWindowManager);
    }

    /**
     * 设置小红点是否显示
     */
    public void setObrainVisibility(Boolean visibility) {
        if (floatBall == null) return;
        floatBall.setObtainVisibility(visibility);
    }
    /**
     * 增加红点数量
     */
    public void addObtainNumer() {
        if (floatBall == null) return;
        floatBall.addObtainNumer();
    }
    /**
     * 设置红点数量
     */
    public void setObtainNumber(int number) {
        if (floatBall == null) return;
        floatBall.setObtainNumber(number);
    }

    //获取浮球的大小
    public int getBallSize() {
        return floatBall.getSize();
    }


    //屏幕尺寸
    public void computeScreenSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point point = new Point();
            mWindowManager.getDefaultDisplay().getSize(point);
            mScreenWidth = point.x;
            mScreenHeight = point.y;
        } else {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        }
    }

    // 获取 状态栏高度
    public int getStatusBarHeight() {
        return statusBarView.getStatusBarHeight();
    }

    // 当 状态栏高度 改变
    public void onStatusBarHeightChange() {
        floatBall.onLayoutChange();
    }


    //当配置修改
    public void onConfigurationChanged(Configuration newConfig) {
        computeScreenSize();
        reset();
    }

    //浮球设置权限
    public void setPermission(IFloatBallPermission iPermission) {
        this.mPermission = iPermission;
    }


    //设置浮球监听
    public void setOnFloatBallClickListener(OnFloatBallClickListener listener) {
        mFloatballClickListener = listener;
    }
    //监听浮球点击事件
    public interface OnFloatBallClickListener {
        void onFloatBallClick();
    }
    //当浮动球被点击
    public void onFloatBallClick() {
        if (menuItems != null && menuItems.size() > 0) {
            floatMenu.attachToWindow(mWindowManager);
        } else {
            if (mFloatballClickListener != null) {
                mFloatballClickListener.onFloatBallClick();
            }
        }
    }

    // 浮球权限接口
    public interface IFloatBallPermission {
        /**
         * request the permission of floatball,just use {@link #requestFloatBallPermission(Activity)},
         * or use your custom method.
         *
         * @return return true if requested the permission
         * @see #requestFloatBallPermission(Activity)
         */
        boolean onRequestFloatBallPermission();

        /**
         * detect whether allow  using floatball here or not.
         *
         * @return
         */
        boolean hasFloatBallPermission(Context context);

        /**
         * request floatball permission
         */
        void requestFloatBallPermission(Activity activity);
    }


}
