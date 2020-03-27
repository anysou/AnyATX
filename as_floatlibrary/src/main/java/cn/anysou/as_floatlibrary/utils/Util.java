package cn.anysou.as_floatlibrary.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;

import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;


public class Util {

    //判断是否为模拟器
    public static boolean isEmulator(Activity activity) {
        /*
        <!-- 添加拨号权限,但安卓6.0以后该句无效果了。采用了动态权限控制-->
        <uses-permission android:name="android.permission.CALL_PHONE"/>
         */
        //动态的请求权限
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.CALL_PHONE
        }, 0x11); //0x11是请求码，可以在回调中获取
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(url));
        //startActivity(intent);
        // 是否可以处理跳转到拨号的 Intent
        boolean isPhone = intent.resolveActivity(activity.getPackageManager()) != null;
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("MuMu")
                || Build.MODEL.contains("virtual")
                || Build.SERIAL.equalsIgnoreCase("android")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName().toLowerCase().equals("android")
                || !isPhone;
    }

    // 返回时间字符串 "yyyy-MM-dd HH:mm:ss"
    public static String getTime(String mode){
        long currentTime = System.currentTimeMillis();
        String timeNow = new SimpleDateFormat(mode).format(currentTime);
        return timeNow;
    }

    // 设置背景
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }


    public static boolean isOnePlus() {
        return getManufacturer().contains("oneplus");
    }

    //获取手机制造商信息
    public static String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        manufacturer = manufacturer.toLowerCase();
        return manufacturer;
    }
}
