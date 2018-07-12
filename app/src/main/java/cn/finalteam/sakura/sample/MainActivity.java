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
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
     * 隐藏悬浮图标
     */
    public void hideFloatingView() {
        if (mFloatViewService != null) {
            mFloatViewService.hideFloat();
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
        // et_dianzan_jiange = (EditText) findViewById(R.id.et_dianzan_jiange);
        et_dianzan_pinlv = (EditText) findViewById(R.id.et_dianzan_pinlv);
        // et_jiahaoyou_jiange = (EditText) findViewById(R.id.et_jiahaoyou_jiange);
        et_jiahaoyou_pinlv = (EditText) findViewById(R.id.et_jiahaoyou_pinlv);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        dianzanpinlv = (int) SpUtil.get(App.context, "dianzanpinlv", 50);
        jiahaoyoupinlv = (int) SpUtil.get(App.context, "jiahaoyoupinlv", 50);

        et_dianzan_pinlv.setText(String.valueOf(dianzanpinlv));
        et_jiahaoyou_pinlv.setText(String.valueOf(jiahaoyoupinlv));

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

        SpUtil.putAndApply(context, "dianzanpinlv", Integer.parseInt(dianzanpinlv));
        SpUtil.putAndApply(context, "jiahaoyoupinlv", Integer.parseInt(jiahaoyoupinlv));

        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();

    }
}
