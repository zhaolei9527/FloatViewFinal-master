package cn.finalteam.sakura;

import android.app.Application;
import android.content.Context;

import com.xdandroid.hellodaemon.DaemonEnv;

import cn.finalteam.sakura.service.FloatViewService;

public class App extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        DaemonEnv.initialize(
                App.context,  //Application Context.
                FloatViewService.class, //刚才创建的 Service 对应的 Class 对象.
                3 * 60 * 1000); //定时唤醒的时间间隔(ms), 默认 6 分钟.
        FloatViewService.sShouldStopService = false;
    }

}
