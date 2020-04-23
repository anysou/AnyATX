package cn.anysou.anyatx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jeremyliao.liveeventbus.LiveEventBus;

/***
 * 接收 TERMUX 发出的广播；再通过 LiveEventBus 发给观察者
 */
public class ATXReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("msg");  //内容
        String id = intent.getStringExtra("id");    //编号
        LiveEventBus.get("run").post(id+"###"+msg); //发送一条即时消息框架 “run” 的值
        //Toast.makeText(context,id+"###"+msg,Toast.LENGTH_LONG).show();
    }
}
