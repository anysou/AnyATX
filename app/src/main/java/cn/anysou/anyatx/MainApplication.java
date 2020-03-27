package cn.anysou.anyatx;

import android.app.Application;
import android.content.Context;


import com.jeremyliao.liveeventbus.LiveEventBus;
import com.buyi.huxq17.serviceagency.ServiceAgency;
import com.buyi.huxq17.serviceagency.exception.AgencyException;

import cn.anysou.as_floatlibrary.LocationService;

public class MainApplication extends Application {

    public static Context mContext;  //全局变量 APP的上下文
    public static Context getAppContext(){
        return mContext;
    } //全局方法，获取 APP的上下文

    @Override
    public void onCreate() {
        super.onCreate();
        //需要记录并在再次打开app的时候恢复位置
        //在LocationService的实现类里用的是SharedPreferences来记录位置，需要Context
        //如果你的实现方式不需要Context，则可以不需要这个步骤，可以去掉这行代码
        try {
            ServiceAgency.getService(LocationService.class).start(this);
        }catch (AgencyException e){
            //ignore
        }
    }

    //==============  设置 LiveEventBus 消息事件总线框架（Android组件间通信工具） =========
    public void setMessageBus(){
        /**LiveEventBus，一款具有生命周期感知能力的消息事件总线框架（Android组件间通信工具）
         * Andoird中LiveEventBus的使用——用LiveEventBus替代RxBus、EventBus https://blog.csdn.net/qq_43143981/article/details/101678528
         * 消息总线，基于LiveData，具有生命周期感知能力，支持Sticky，支持AndroidX，支持跨进程，支持跨APP
         * https://github.com/JeremyLiao/LiveEventBus
         *
         * 1、build.gradle 中引用  implementation 'com.jeremyliao:live-event-bus-x:1.5.7'
         * 2、初始化 LiveEventBus
         *   1）supportBroadcast 配置支持跨进程、跨APP通信
         *   2）配置 lifecycleObserverAlwaysActive 接收消息的模式（默认值true）：
         *      true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息
         *      false：激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息
         *   3) autoClear 配置在没有Observer关联的时候是否自动清除LiveEvent以释放内存（默认值false）
         * 3、发送消息：
         *    LiveEventBus.get("key").post("value");  //发送一条即时消息
         *    LiveEventBus.get("key").postDelay("value",3000);  //发送一条延时消息 3秒跳转
         * 4、接受消息，注册一个订阅，在需要接受消息的地方
         *   LiveEventBus.get("key",String.class).observe(this, new Observer<String>() {
         *      @Override
         *      public void onChanged(@Nullable String s) {
         *              Log.i(TAG,s);
         *      }
         *   });
         * */
        LiveEventBus.config()
                .supportBroadcast(this)
                .lifecycleObserverAlwaysActive(true)
                .autoClear(false);
    }

}
