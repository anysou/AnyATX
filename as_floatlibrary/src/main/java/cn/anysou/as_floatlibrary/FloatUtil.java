package cn.anysou.as_floatlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * 浮动球 显示参数 处理单元 , 根据版本不同确定显示在哪一层
 *
 */

public class FloatUtil {

    public static boolean inSingleActivity;

    public static WindowManager.LayoutParams getLayoutParams(Context context) {
        return getLayoutParams(context, false);
    }

    public static WindowManager.LayoutParams getLayoutParams(Context context, boolean listenBackEvent) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        //设置Window flag
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        if (listenBackEvent) {  //设置浮动窗口不可聚焦
            layoutParams.flags = layoutParams.flags & ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }

        //设置 window type
        if (context == null || !(context instanceof Activity)) {
            final int sdkInt = Build.VERSION.SDK_INT;
            if (sdkInt < Build.VERSION_CODES.KITKAT) {  //19
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else if (sdkInt < Build.VERSION_CODES.N_MR1) { //25
                if ("Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                } else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                }
            } else if (sdkInt < Build.VERSION_CODES.O) {  //26
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {//8.0以后
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        }

        //设置图片格式，效果为背景透明
        layoutParams.format = PixelFormat.RGBA_8888;
        //调整悬浮窗显示的停靠位置为左侧置顶
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        //设置悬浮窗的长得宽
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        return layoutParams;
    }

    // 获取状态栏层的参数
    public static WindowManager.LayoutParams getStatusBarLayoutParams(Context context) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        if (context == null || !(context instanceof Activity)) {
            final int sdkInt = Build.VERSION.SDK_INT;
            if (sdkInt < Build.VERSION_CODES.KITKAT) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else if (sdkInt < Build.VERSION_CODES.N_MR1) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            } else if (sdkInt < Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {//8.0以后
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        }
        return layoutParams;
    }


}
