package cn.anysou.as_floatlibrary;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import cn.anysou.as_floatlibrary.infowindows.InfoView;


/***
 * 信息框管理
 */
public class InfoManager {

    private Context mContext;
    private OnCloseClickListener mOnCloseClickListener;   //信息框点击事件监听
    private WindowManager mWindowManager;       //WINDOW管理
    public InfoView mInfoView;                  //信息框
    private boolean isShowing = false;          //信息框是否显示

    public InfoManager(Context application) {
        mContext = application.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mInfoView = new InfoView(mContext,this);     //建立信息框
    }

    //====  在 application 整个APP
    public void show() {
        if (isShowing) return;
        isShowing = true;
        mInfoView.setVisibility(View.VISIBLE);
        mInfoView.attachToWindow(mWindowManager);
    }

    // 隐藏信息框
    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        mInfoView.setVisibility(View.GONE);
        mInfoView.detachFromWindow(mWindowManager);
    }

    //设置信息框监听
    public void setOnCloseClickListener(OnCloseClickListener listener) {
        mOnCloseClickListener = listener;
    }
    //监听信息框点击事件
    public interface OnCloseClickListener {
        void onFloatBallClick();
    }
    //当浮动球被点击
    public void onCloseBallClick() {
        if (mOnCloseClickListener != null) {
            mOnCloseClickListener.onFloatBallClick();
        }
    }


}
