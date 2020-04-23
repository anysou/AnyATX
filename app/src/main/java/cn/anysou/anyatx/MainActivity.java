package cn.anysou.anyatx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.ArrayList;
import java.util.List;

import cn.anysou.as_floatlibrary.FloatBallManager;
import cn.anysou.as_floatlibrary.InfoManager;
import cn.anysou.as_floatlibrary.floatball.DraggableFlagView;
import cn.anysou.as_floatlibrary.floatball.FloatBallCfg;
import cn.anysou.as_floatlibrary.infowindows.InfoRecycleViewAdapter;
import cn.anysou.as_floatlibrary.menu.FloatMenuCfg;
import cn.anysou.as_floatlibrary.menu.MenuItem;
import cn.anysou.as_floatlibrary.permission.FloatPermissionManager;
import cn.anysou.as_floatlibrary.utils.BackGroudSeletor;
import cn.anysou.as_floatlibrary.utils.DensityUtil;
import cn.anysou.as_floatlibrary.utils.SpUtil;
import cn.anysou.as_floatlibrary.utils.Util;

public class MainActivity extends AppCompatActivity {

    private FloatBallManager mFloatballManager;                //浮动球管理
    private FloatPermissionManager mFloatPermissionManager;    //浮动权限管理
    private ActivityLifeCycleListener mActivityLifeCycleListener = new ActivityLifeCycleListener();  //监听
    private boolean isfull = false;              //是否全屏显示

    private int resumed;                        //记录次数，用来判断是否在前台
    private boolean RedMode = true;             //小红点模式
    private Integer RedNumber = 0;              //小红点的数字，记录当前完成的任务数

    private InfoManager mInfoManager;           //信息管理
    private Integer msgLen = 50;                //定义就只显示最近50条
    private List idList;                        //id确定同一类信息的标识，用来做更新处理
    private List msgList;                       //信息内容

    private RecyclerView mRecycleView;          //信息显示控件
    private InfoRecycleViewAdapter mAdapter;    //适配器
    private LinearLayoutManager mLinearLayoutManager;//布局管理器

    private CallTermux mcallTermux;             //Termux相关调用类
    private boolean inTerminal = true;          //是否显示在终端

    private String IMEI = "";                   //手机第一张卡的IMEI
    private TextView textView;
    private EditText username;
    private EditText password;
    private EditText mlfile;
    private EditText yfsjfile;
    private CheckBox checkML;
    private String MLagain = "0";
    private boolean show_hide = false;
    private EditText atxjb;

    //public int ShowItem = 0;                  //当前显示的界面 0=设置启动、1=浮球、2=信息框

    //================================ 各个按键功能 =============================================
    public void DOWN(View view){
        toast("脚本程序下载更新后台进行中。。。");
        Termux_DOWN();
    }
    public void usb(View view){
        boolean enableAdb = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
        if(enableAdb){
            toast("USB调试已开启");
        }else {
            toast("USB调试未开启");
        }
    }
    public void run(View view) {
        Termux_PAUST(false);
    }
    public void paust(View view) {
        Termux_PAUST(true);
    }
    // 显示浮球按键
    public void SSP(View v) {
        //toast("开始刷单");
        //日志窗口标题
        setInfoTitle("刷 单 测 试");
        mFloatballManager.closeMenu();
        //mFloatballManager.hide();
        Termux_VIDEOS("2");  //1=测试；2=正式
    }
    //刷攒攒
    public void SZZ(View view) {
        mFloatballManager.show();
        // setFullScreen(v);   //显示全屏
        toast("开始刷攒攒");
        mFloatballManager.closeMenu();
        CallTermux callTermux = new CallTermux();
        String user_name = username.getText().toString();
        String pass_word = password.getText().toString();
        if(!user_name.equals("") && !pass_word.equals("")){
            String arg = user_name+" "+pass_word;
            SpUtil.put(this,"user_name",user_name);
            SpUtil.put(this,"pass_word",pass_word);
            callTermux.call_file(getApplicationContext(),"run_ZQZAN.sh",arg,true);
            mFloatballManager.closeMenu();
        } else {
            toast("攒攒用户名或密码都不能为空");
        }
    }
    //显示隐藏悬浮球
    public void hideFloatBall(View view) {
        if(show_hide) {
            mFloatballManager.hide();
            show_hide = false;
        }
        else {
            mFloatballManager.show();
            show_hide = true;
        }
    }
    //显示红点+1
    public void ShowRed(View view) {
        mFloatballManager.addObtainNumer();
    }
    //隐藏红点
    public void HideRed(View view) {
        mFloatballManager.setObrainVisibility(false);
    }
    //红点+1
    public void Red_ADD(){
        mFloatballManager.addObtainNumer();
    }
    //秘乐
    public void ML(View view) {
        mFloatballManager.show();
        // setFullScreen(v);   //显示全屏
        toast("开始刷秘乐");
        mFloatballManager.closeMenu();
        CallTermux callTermux = new CallTermux();
        String ml_file = mlfile.getText().toString();
        if(!ml_file.equals("")){
            String arg = ml_file+" "+MLagain;
            SpUtil.put(this,"ml_file",ml_file);
            callTermux.call_file(getApplicationContext(),"run_ML.sh",arg,true,true);
            mFloatballManager.closeMenu();
            checkML.setChecked(false); //取消勾选
        } else {
            toast("秘乐用户文件名不能为空");
        }
    }
    //影粉世家
    public void YFSJ(View view) {
        mFloatballManager.show();
        // setFullScreen(v);   //显示全屏
        toast("开始刷影粉世家");
        mFloatballManager.closeMenu();
        CallTermux callTermux = new CallTermux();
        String yfsj_file = yfsjfile.getText().toString();
        if(!yfsj_file.equals("")){
            String arg = yfsj_file+" "+MLagain;
            SpUtil.put(this,"yfsj_file",yfsj_file);
            callTermux.call_file(getApplicationContext(),"run_YFSJ.sh",arg,true,true);
            mFloatballManager.closeMenu();
            checkML.setChecked(false); //取消勾选
        } else {
            toast("影粉世家用户文件名不能为空");
        }
    }
    //执行脚本名
    public void Run_ATXJB(View view){
        mFloatballManager.show();
        // setFullScreen(v);   //显示全屏
        toast("开始执行指定脚本");
        mFloatballManager.closeMenu();
        CallTermux callTermux = new CallTermux();
        String atx_jb = atxjb.getText().toString();
        if(!atx_jb.equals("")){
            SpUtil.put(this,"atx_jb",atx_jb);
            callTermux.call_file(getApplicationContext(),"run_ATXJB.sh",atx_jb,true,true);
            mFloatballManager.closeMenu();
        } else {
            toast("开始执行指定脚本名不能为空");
        }
    }


    //生命周期为onCreate->onStart->onResume->onAttachedToWindow
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());  //实现textview滚动条

        username = (EditText) findViewById(R.id.editTextUserName);
        password = (EditText) findViewById(R.id.editTextPassword);
        mlfile = (EditText) findViewById(R.id.editTextMLFILE);
        yfsjfile = (EditText) findViewById(R.id.editTextYFSJFile);
        checkML = (CheckBox) findViewById(R.id.checkBoxML);
        atxjb = (EditText) findViewById(R.id.editTextATXJB);

        checkML.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    MLagain = "1";
                    toast("选择秘乐或影粉世家用户列表从头开始刷");
                }else{
                    MLagain = "0";
                    toast("选择秘乐或影粉世家用户列表正常刷");
                }
            }
        });


        String user_name = String.valueOf(SpUtil.get(this,"user_name",""));
        String pass_word = String.valueOf(SpUtil.get(this,"pass_word",""));
        String ml_file = String.valueOf(SpUtil.get(this,"ml_file",""));
        String yfsj_file = String.valueOf(SpUtil.get(this,"yfsj_file",""));
        String atx_jb = String.valueOf(SpUtil.get(this,"atx_jb",""));
        if(!user_name.equals("") && !pass_word.equals("")){
            username.setText(user_name);
            password.setText(pass_word);
        }
        if(!ml_file.equals("")){
            mlfile.setText(ml_file);
        }
        if(!yfsj_file.equals("")){
            yfsjfile.setText(yfsj_file);
        }
        if(!atx_jb.equals("")){
            atxjb.setText(atx_jb);
        }


        // 1、让本应用APP全屏显示
        if(isfull) {
            setFullScreen();
        }

        // 2、浮球初始化; 关闭菜单
        initFloatBall(true);   //初始化菜单(true=有菜单)
        //如果没有添加菜单，可以设置悬浮球点击事件(本程序中暂用不上)
        if (mFloatballManager.getMenuItemSize() == 0) {
            mFloatballManager.setOnFloatBallClickListener(new FloatBallManager.OnFloatBallClickListener() {
                @Override
                public void onFloatBallClick() {
                    toast("点击了悬浮球");
                }
            });
        }
        //如果想做成应用内悬浮球，可以添加以下代码。（不需要权限）
        //getApplication().registerActivityLifecycleCallbacks(mActivityLifeCycleListener);
        //显示浮球
        //mFloatballManager.show();

        // 3、初始化信息框内容
        initInfoFloat();
        //显示信息框
        mInfoManager.show();

        // 4、初始化 Termux 管理类
        mcallTermux = new CallTermux();

        // 5、订阅 消息框架 “run” 的值变化,接收并做相应处理,
        LiveEventBus.get("run",String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) { //获取"run" 变化的新值
                if(s.contains("###")){
                    String id = s.split("###")[0];
                    String msg = s.split("###")[1];
                    if(id.equals("SHOW")){
                        textView.setText(msg);
                    }else if(id.equals("ADDSHOW")) {
                        textView.setText(textView.getText().toString() + "\n " + msg);
                    } else {
                        Integer index = idList.indexOf(id);
                        if (index >= 0) {
                            mAdapter.changeDate(index, msg);
                            if (msg.startsWith("ok:")) {  //遇到结束+1
                                RedNumber += 1;
                                mFloatballManager.setObtainNumber(RedNumber);
                            }
                        } else {
                            idList.add(1, id);
                            mAdapter.addData(msg);
                        }
                    }
                } else {
                    toast("收到内容信息格式不对："+s);
                }
            }
        });

        // 6、现在部分手机(或大部分或只是个案)安装新APP后会默认不开启“锁屏通知权限” ，
        // 让客户手动去打开权限他还不乐意，一直找如何去动态申请权限，最后发现其实不需要申请权限，只需要几句话就好
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD    //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON      //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);    //打开屏幕

        // 7、判断是否为模拟器
        if(Util.isEmulator(this)){
            toast("当前环境为模拟器，需要授权使用！");
            textView.setText(textView.getText().toString()+"\n 是模拟器");
        } else {
            toast("是手机！");
            textView.setText(textView.getText().toString()+"\n 是手机");
        }
    }

    //==================== 全屏方面 ===================================================
    //全屏设置
    private void setFullScreen() {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isfull = true;
    }
    //退出全屏
    private void quitFullScreen() {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        isfull = false;
    }
    //全屏切换
    public void setFullScreen(View view) {
        if (isfull == true) {
            quitFullScreen();
        } else {
            setFullScreen();
        }
    }


    //=======================初始化悬浮球配置 ==========================================
    private void initFloatBall(boolean showMenu) {

        //1、初始化悬浮球配置，定义好悬浮球尺寸大小、icon的drawable
        int ballSize = DensityUtil.dip2px(this, 80);  //根据手机的分辨率从 dp 的单位 转成为 px(像素)
        Drawable ballIcon = BackGroudSeletor.getdrawble("little_monk", this);  //ic_floatball 获取ICon图片。位置：assets/image/xxx.png

        //2、设置浮球的位置。
        //FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon);  //默认在左上角
        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.LEFT_CENTER); //左中间
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.LEFT_BOTTOM, -100);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_TOP, 100);
        //FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_CENTER);

        //3、设置悬浮球是否半隐藏
        ballCfg.setHideHalfLater(true);

        //4A、需要显示悬浮菜单
        if (showMenu) {
            //4A.1 初始化悬浮菜单配置，有菜单item的大小和菜单item的个数
            int menuSize = DensityUtil.dip2px(this, 200);      //菜单尺寸
            int menuItemSize = DensityUtil.dip2px(this, 25);   //每个菜单图标尺寸
            FloatMenuCfg menuCfg = new FloatMenuCfg(menuSize, menuItemSize);
            //4A.2 生成floatballManager
            mFloatballManager = new FloatBallManager(getApplicationContext(), ballCfg, menuCfg);
            //4A.3 添加菜单
            addFloatMenuItem();
        } else {  //4B、不要菜单
            mFloatballManager = new FloatBallManager(getApplicationContext(), ballCfg);
        }

        //5、浮球获取权限
        setFloatPermission();

        // ====== 采用新模式，带小红点的 =============
        if(RedMode) {
            //6、设置为新模式
            mFloatballManager.setFloatBallMode(true);

            //7. 设置小红点的点击事件
            DraggableFlagView mRed = mFloatballManager.floatBall.mDraggableFlagView;
            mRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toast("我是小红点，有" + RedNumber + "个任务完成！");
                }
            });

            //8、设置小红点的拖攥事件
            mRed.setOnDraggableFlagViewListener(new DraggableFlagView.OnDraggableFlagViewListener() {
                @Override
                public void onFlagDismiss(DraggableFlagView view) {
                    //小红点消失的一些操作
                    toast("小红点消失，重新统计完成任务数据！");
                    RedNumber = 0;
                }
            });
        }
    }

    //===== 浮球菜单 ========================
    private void addFloatMenuItem() {
        MenuItem personItem = new MenuItem(BackGroudSeletor.getdrawble("window", this)) {
            @Override
            public void action() {
                //toast("查看记录");
                //关闭浮球菜单，隐藏浮球，打开信息框
                mFloatballManager.closeMenu();
                mFloatballManager.hide();
                mInfoManager.show();
            }
        };
        MenuItem walletItem = new MenuItem(BackGroudSeletor.getdrawble("pause", this)) {
            @Override
            public void action() {
                toast("暂停");
                mFloatballManager.closeMenu();
                Termux_PAUST(true);
            }
        };
        MenuItem settingItem = new MenuItem(BackGroudSeletor.getdrawble("continue", this)) {
            @Override
            public void action() {
                toast("继续");
                mFloatballManager.closeMenu();
                Termux_PAUST(false);
            }
        };
        MenuItem hideItem = new MenuItem(BackGroudSeletor.getdrawble("exit", this)) {
            @Override
            public void action() {
                toast("隐藏");
                mFloatballManager.closeMenu();
                mFloatballManager.hide();
                Termux_KILL();
                //重新显示本窗口
                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent);
            }
        };

        mFloatballManager
                .addMenuItem(personItem)
                .addMenuItem(walletItem)
                .addMenuItem(settingItem)
                .addMenuItem(hideItem)
                .buildMenu();
    }


    //==== 信息框 初始化 ====================================
    private void initInfoFloat(){

        //1、初始化信息框管理类
        mInfoManager =  new InfoManager(getApplicationContext());

        //2、 根据手机屏幕尺寸 动态修改 布局宽度（230dp为最宽，不能大于手机屏幕宽度的一半）
        int mScreenWidthDP = DensityUtil.px2dip(getApplicationContext(),mFloatballManager.mScreenWidth); //PX2DP
        if(mScreenWidthDP/2<=230) {
            //修改信息框宽度
            int width = DensityUtil.dip2px(getApplicationContext(), mScreenWidthDP/2-5);
            final LinearLayout mLinearLayout = mInfoManager.mInfoView.findViewById(R.id.linearMain);
            ViewGroup.LayoutParams lp = mLinearLayout.getLayoutParams();
            lp.width = width;
            mLinearLayout.setLayoutParams(lp);
        }

        //3、数据初始化
        idList = new ArrayList();
        idList.add("id");
        msgList = new ArrayList();
        msgList.add(Util.getTime("dd日HH:mm:ss")+"、开始记录");

        //4、显示信息的控件，设置布局、及数据显示适配器
        mRecycleView = mInfoManager.mInfoView.findViewById(R.id.rv_list);
        //创建布局管理器，垂直设置LinearLayoutManager.VERTICAL，水平设置LinearLayoutManager.HORIZONTAL
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        //创建适配器，将数据传递给适配器
        mAdapter = new InfoRecycleViewAdapter(msgList);
        //设置布局管理器
        mRecycleView.setLayoutManager(mLinearLayoutManager);
        //设置适配器adapter
        mRecycleView.setAdapter(mAdapter);
        //显示滚动到第一条信息
        mRecycleView.smoothScrollToPosition(0);

        //5、定义信息框的按键功能：
        //关闭
        mInfoManager.mInfoView.findViewById(R.id.B_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("关闭信息窗口！");
                mInfoManager.hide();
                mFloatballManager.show();
            }
        });
        //暂停
        mInfoManager.mInfoView.findViewById(R.id.B_paust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("发出暂停请求！");
                Termux_PAUST(true);
            }
        });
        mInfoManager.mInfoView.findViewById(R.id.B_run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("发出继续请求！");
                Termux_PAUST(false);
            }
        });

        //=== 更改显示标题 ====
        setInfoTitle("初始化完成");
    }

    //=== 日志窗口 标题 ====
    private void setInfoTitle(String Title){
        TextView textView = mInfoManager.mInfoView.findViewById(R.id.Title);
        textView.setText(Title);
    }
    //================================= 与 Termux的 代码功能调用 ===============================
    //调用下载
    private void Termux_DOWN(){
        mcallTermux.call_file(getApplicationContext(),"run_DOWN.sh","",false);
    }
    //刷视频
    private void Termux_VIDEOS(String ARG){
        mcallTermux.call_file(getApplicationContext(),"run_VIDEOS.sh",ARG,inTerminal,true);
    }
    //暂停与继续
    private void Termux_PAUST(boolean mode){
        if(mode)
            mcallTermux.call_file(getApplicationContext(),"run_VS_PAUST.sh","1",false);
        else
            mcallTermux.call_file(getApplicationContext(),"run_VS_PAUST.sh","0",false);
    }
    private void Termux_KILL(){
        mcallTermux.kill_Termux(getApplicationContext());
    }
    private void Termux_EXIT(){
        mcallTermux.kill_Termux_server(getApplicationContext());
    }


    //=======设置悬浮动窗口权限，用于申请悬浮球权限的=========================
    private void setFloatPermission() {
        // 设置悬浮球权限，用于申请悬浮球权限的，这里用的是别人写好的库，可以自己选择
        //如果不设置permission，则不会弹出悬浮球
        mFloatPermissionManager = new FloatPermissionManager();
        mFloatballManager.setPermission(new FloatBallManager.IFloatBallPermission() {
            @Override
            public boolean onRequestFloatBallPermission() {
                requestFloatBallPermission(MainActivity.this);
                return true;
            }
            @Override
            public boolean hasFloatBallPermission(Context context) {
                return mFloatPermissionManager.checkPermission(context);
            }
            @Override
            public void requestFloatBallPermission(Activity activity) {
                mFloatPermissionManager.applyPermission(activity);
            }
        });
    }




    // 继承 控件生命周期回调
    public class ActivityLifeCycleListener implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }
        @Override
        public void onActivityStarted(Activity activity) {
        }
        @Override
        public void onActivityResumed(Activity activity) {
            ++resumed;
            setFloatballVisible(true);
        }
        @Override
        public void onActivityPaused(Activity activity) {
            --resumed;
            if (!isApplicationInForeground()) {
                setFloatballVisible(false);
            }
        }
        @Override
        public void onActivityStopped(Activity activity) {
        }
        @Override
        public void onActivityDestroyed(Activity activity) {
        }
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
    }

    // 设置浮球是否可见
    private void setFloatballVisible(boolean visible) {
        if (visible) {
            mFloatballManager.show();
            mInfoManager.hide();
        } else {
            mFloatballManager.hide();
        }
    }

    //判断应用是否在前台
    public boolean isApplicationInForeground() {
        return resumed > 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mInfoManager.hide();
        mFloatballManager.show();
        mFloatballManager.closeMenu();
        mFloatballManager.onFloatBallClick();
    }
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFloatballManager.hide();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注册ActivityLifeCyclelistener以后要记得注销，以防内存泄漏。
        getApplication().unregisterActivityLifecycleCallbacks(mActivityLifeCycleListener);
    }

    //吐司
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }





    // 浮动按键 调用打开 LogCAT 日志界面
    public void ButtonCallLogCAT(View view) {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)  //LynxView中显示的最大跟踪数
                .setTextSizeInPx(12)       //用于在LynxView中呈现字体大小PX
                .setSamplingRate(200)      //从应用程序日志中读取的采样率
                .setFilter("");   //设置过滤
        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }
}
