package cn.anysou.as_floatlibrary.infowindows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;

import cn.anysou.as_floatlibrary.FloatUtil;
import cn.anysou.as_floatlibrary.InfoManager;
import cn.anysou.as_floatlibrary.R;


public class InfoView extends FrameLayout {

    private InfoManager infoManager;                    //信息框管理者
    private WindowManager.LayoutParams mLayoutParams;   //信息框窗口参数
    private WindowManager windowManager;                //WINDOWS 管理
    private boolean isAdded = false;

    public InfoView(final Context context, InfoManager infoManager) {
        super(context);
        this.infoManager = infoManager;

        LayoutInflater.from(context).inflate(R.layout.float_info, this);
        initLayoutParams(context);             //获取显示浮球的相关参数
    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatUtil.getLayoutParams(context);
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
            windowManager.removeView(this);
            isAdded = false;
        }
    }


}
