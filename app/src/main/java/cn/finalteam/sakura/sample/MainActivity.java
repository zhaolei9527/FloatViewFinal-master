/*
 * Copyright (C) 2015 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.finalteam.sakura.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.finalteam.sakura.App;
import cn.finalteam.sakura.AutoClickAccessibilityService;
import cn.finalteam.sakura.SpUtil;
import cn.finalteam.sakura.service.FloatViewService;

public class MainActivity extends AppCompatActivity {

    public static Activity context;
    private FloatViewService mFloatViewService;
    private String FACEBOOKPACKAGE = "com.facebook.katana";
    private EditText et_dianzan_pinlv;
    private EditText et_jiahaoyou_pinlv;
    private Button btn_submit;
    private int dianzanpinlv = 0;
    private int jiahaoyoupinlv = 0;
    private EditText et_jiahaoyou_guolv;
    private EditText et_jiahaoyou_time;
    private int jiahaoyoutime;
    private String jiahaoyouguolv;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:

                    Bitmap bitmap = (Bitmap) msg.obj;
                    int color = bitmap.getPixel(10, 10);
                    // 如果你想做的更细致的话 可以把颜色值的R G B 拿到做响应的处理
                    int r = Color.red(color);
                    int g = Color.green(color);
                    int b = Color.blue(color);

                    if (r != 1 || g != 1 || b != 1) {
                        AutoClickAccessibilityService.end = 0;
                    }

                    Log.e("aaaa", "r=" + r + ",g=" + g + ",b=" + b);
                    break;
                case 2:

                    String info = (String) msg.obj;

                    Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        context = MainActivity.this;

        Button btnShowFloat = (Button) findViewById(R.id.btn_show_float);
        btnShowFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFloatingView();
            }
        });

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        destroy();
        super.onDestroy();
    }

    /**
     * 显示悬浮图标
     */
    public void showFloatingView() {
        try {
            Intent intent = new Intent(this, FloatViewService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

            PackageManager packageManager = getPackageManager();
            if (checkPackInfo(FACEBOOKPACKAGE)) {
                Intent intent2 = packageManager.getLaunchIntentForPackage(FACEBOOKPACKAGE);
                startActivity(intent2);
            } else {
                Toast.makeText(this, "没有检测到安装FaceBook", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mFloatViewService != null) {
                mFloatViewService.showFloat();
            }

            Intent i = new Intent(MainActivity.this, AutoClickAccessibilityService.class);
            MainActivity.this.startService(i);

            //execShellCmd("input tap 0 0");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放PJSDK数据
     */
    public void destroy() {
        try {
            // stopService(new Intent(this, FloatViewService.class));
            if (mServiceConnection != null) {
                unbindService(mServiceConnection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接到Service
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mFloatViewService = ((FloatViewService.FloatViewServiceBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mFloatViewService = null;
        }
    };

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    private void initView() {
        et_dianzan_pinlv = (EditText) findViewById(R.id.et_dianzan_pinlv);
        et_jiahaoyou_pinlv = (EditText) findViewById(R.id.et_jiahaoyou_pinlv);
        et_jiahaoyou_guolv = (EditText) findViewById(R.id.et_jiahaoyou_guolv);
        et_jiahaoyou_time = (EditText) findViewById(R.id.et_jiahaoyou_time);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        dianzanpinlv = (int) SpUtil.get(App.context, "dianzanpinlv", 50);
        jiahaoyoupinlv = (int) SpUtil.get(App.context, "jiahaoyoupinlv", 50);
        jiahaoyoutime = (int) SpUtil.get(App.context, "jiahaoyoutime", 3);
        jiahaoyouguolv = (String) SpUtil.get(App.context, "jiahaoyouguolv", "");

        et_jiahaoyou_time.setText(String.valueOf(jiahaoyoutime));
        et_jiahaoyou_guolv.setText(jiahaoyouguolv);
        et_dianzan_pinlv.setText(String.valueOf(dianzanpinlv));
        et_jiahaoyou_pinlv.setText(String.valueOf(jiahaoyoupinlv));


        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getImageFromServer("http://q2.qlogo.cn/headimg_dl?bs=2443400488\n" +
                        "&dst_uin=2443400488\n" +
                        "&dst_uin=2443400488\n" +
                        "&;dst_uin=2443400488\n" +
                        "&spec=100&url_enc=0&referer=bu_interface&term_type=PC");
                Message msg = new Message();
                msg.what = 1;
                msg.obj = bitmap;
                handler.sendMessage(msg);
            }
        }.start();


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

    }

    private void submit() {

        String dianzanpinlv = et_dianzan_pinlv.getText().toString().trim();
        if (TextUtils.isEmpty(dianzanpinlv)) {
            Toast.makeText(this, "请输入点赞数量", Toast.LENGTH_SHORT).show();
            return;
        }

        String jiahaoyoupinlv = et_jiahaoyou_pinlv.getText().toString().trim();
        if (TextUtils.isEmpty(jiahaoyoupinlv)) {
            Toast.makeText(this, "请输入加好友数量", Toast.LENGTH_SHORT).show();
            return;
        }

        String jiahaoyouguolv = et_jiahaoyou_guolv.getText().toString().trim();
        if (TextUtils.isEmpty(jiahaoyouguolv)) {
            Toast.makeText(this, "加好友过滤内容（多项过滤内容以#号分割）", Toast.LENGTH_SHORT).show();
            return;
        }

        String jiahaoyoutime = et_jiahaoyou_time.getText().toString().trim();
        if (TextUtils.isEmpty(jiahaoyoutime)) {
            Toast.makeText(this, "请输入加好友间隔时间）", Toast.LENGTH_SHORT).show();
            return;
        }

        SpUtil.putAndApply(context, "jiahaoyoutime", Integer.parseInt(jiahaoyoutime));
        SpUtil.putAndApply(context, "dianzanpinlv", Integer.parseInt(dianzanpinlv));
        SpUtil.putAndApply(context, "jiahaoyoupinlv", Integer.parseInt(jiahaoyoupinlv));
        SpUtil.putAndApply(context, "jiahaoyouguolv", jiahaoyouguolv);

        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();


    }

    private Bitmap getImageFromServer(String path) {
        try {
            //1、获得统一资源定位符
            URL url = new URL(path);
            //2、装化成http网络请求
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); //默认是get请求，当写POST时便是post请求
            connection.setConnectTimeout(5000); //设置访问超时的时间。
            if (200 == connection.getResponseCode()) {  //获取响应码
                //获取资源类型
                String type = connection.getContentType();
                //获取资源的长度
                int length = connection.getContentLength();
                Log.i("图片的资源：", "type===" + type + "length===" + length);
                //3、获取网络输入流
                InputStream is = connection.getInputStream();
                //4、将流转换成bitmap对象
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = Message.obtain();
            msg.what = 2;
            msg.obj = "图片访问失败，请检查网络";
            handler.sendMessage(msg);
        }
        return null;
    }
}

