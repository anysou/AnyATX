package cn.anysou.anyatx;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 调用 Termux 的执行
 */

public class CallTermux {

    public static final String TERMUX_PKGNAME = "com.termux";
    public static final String TERMUX_API = "com.termux.api";
    public static final File TASKER_DIR = new File("/data/data/com.termux/files/home/.termux/tasker/");
    public static final String TERMUX_SERVICE = "com.termux.app.TermuxService";
    public static final String ACTION_EXECUTE = "com.termux.service_execute";
    public static final String EXTRA_ARGUMENTS = "com.termux.execute.arguments";  //要传递给脚本的参数
    public static final String ORIGINAL_INTENT = "originalIntent";

    /** 如果用户忘记这样做，请确保文件可读和可执行. */
    static void ensureFileReadableAndExecutable(File file) {
        if (!file.canRead()) file.setReadable(true);
        if (!file.canExecute()) file.setExecutable(true);
    }

    public final void call_file(Context context,String RunFile,String RunARG,Boolean inTerminal,Boolean first){
        if(first)
            kill_Termux_server(context); //先关闭其他执行
        call_file(context,RunFile,RunARG,inTerminal);
    }

    // 执行文件
    public final void call_file(Context context,String RunFile,String RunARG,Boolean inTerminal) {

        // 获取执行文件名
        File execFile = new File(TASKER_DIR, RunFile); //获取执行文件
        if (!execFile.isFile()) { //文件不存在
            String message = "没有执行文件:\n" + TASKER_DIR;
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return;
        }
        ensureFileReadableAndExecutable(execFile);  //给文件读执行权限

        //获取参数
        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(RunARG);  //对参数进行处理
        List<String> list = new ArrayList<>();
        while (matcher.find()){
            list.add(matcher.group(1).replace("\"",""));
        }

        //executableFile.getAbsolutePath() 返回抽象路径名的绝对路径名字符串
        Uri scriptUri = new Uri.Builder().scheme("com.termux.file").path(execFile.getAbsolutePath()).build();

        // ACTION_EXECUTE = "com.termux.service_execute"
        Intent executeIntent = new Intent(ACTION_EXECUTE, scriptUri);

        // TERMUX_SERVICE = "com.termux.app.TermuxService"
        executeIntent.setClassName(TERMUX_PKGNAME, TERMUX_SERVICE);

        //是否勾选
        if (!inTerminal) executeIntent.putExtra("com.termux.execute.background", true); //后台运行

        executeIntent.putExtra(EXTRA_ARGUMENTS, list.toArray(new String[list.size()]));  //添加参数

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // https://developer.android.com/about/versions/oreo/background.html
            context.startForegroundService(executeIntent);
        } else {
            context.startService(executeIntent);
        }

    }

    public final void call(Context context,String cmd,String RunARG,Boolean inTerminal) {

        //executableFile.getAbsolutePath() 返回抽象路径名的绝对路径名字符串
        Uri cmdUri = new Uri.Builder().scheme("com.termux.cmd").path(cmd).build();

        // ACTION_EXECUTE = "com.termux.service_execute"
        Intent executeIntent = new Intent(ACTION_EXECUTE, cmdUri);

        // TERMUX_SERVICE = "com.termux.app.TermuxService"
        executeIntent.setClassName(TERMUX_PKGNAME, TERMUX_SERVICE);

        //是否勾选
        if (!inTerminal) executeIntent.putExtra("com.termux.execute.background", true); //后台运行

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // https://developer.android.com/about/versions/oreo/background.html
            context.startForegroundService(executeIntent);
        } else {
            context.startService(executeIntent);
        }

    }


    public void kill_Termux_server(Context context){
        // 获取要检测运行的 组件名称
        ComponentName collectorComponent = new ComponentName(TERMUX_PKGNAME,TERMUX_SERVICE);
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE); // 列出所有服务(只会列出本APP的服务)
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                android.os.Process.killProcess(service.pid);
                //Toast.makeText(context,"KILL 服务成功！",Toast.LENGTH_LONG).show();
            }
        }
    }

    //关闭
    public void kill_Termux(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();  //列出所有进程
        Integer i = 0;
        for (ActivityManager.RunningAppProcessInfo info:infos) {
            i +=1;
            //LiveEventBus.get("run").post(i+"###"+info.processName); //发送一条即时消息框架 “run” 的值
            if(info.processName.equals(TERMUX_PKGNAME) || info.processName.equals(TERMUX_API)) {
                //Toast.makeText(context,"KILL 应用窗口成功！",Toast.LENGTH_LONG).show();
                android.os.Process.killProcess(info.pid);
            }
        }
    }
}

