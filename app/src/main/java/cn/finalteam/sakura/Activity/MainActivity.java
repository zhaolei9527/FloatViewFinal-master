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

package cn.finalteam.sakura.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

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
    private Button btn_dairu;
    private int dianzanpinlv = 0;
    private int jiahaoyoupinlv = 0;
    private EditText et_jiahaoyou_guolv;
    private EditText et_jiahaoyou_time;
    private int jiahaoyoutime;
    private String jiahaoyouguolv;
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    /**
     * 权限请求值
     */
    private static final int PERMISSION_REQUEST_CODE = 0;


    /**
     * 截屏相关
     */
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private static Intent mResultData = null;
    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private GestureDetector mGestureDetector;
    private ImageView mFloatView;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private String mPhoneType;
    public static final int REQUEST_MEDIA_PROJECTION = 18;
    public static boolean open = false;
    public static boolean isman = false;

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

    private MediaProjectionManager mMediaProjectionManager;

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

        /**
         * 初始化变量
         */
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    mResultData = data;
                    //startService(new Intent(getApplicationContext(), FloatWindowsService.class));
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            destroy();
            stopVirtual();
            tearDownMediaProjection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startScreenShot() {
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                // start virtual
                startVirtual();
            }
        }, 5);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                // capture the screen
                startCapture();
            }
        }, 30);
    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    private void startCapture() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            startScreenShot();
        } else {
            SaveTask mSaveTask = new SaveTask();
            // mSaveTask.execute(image);
            if (Build.VERSION.SDK_INT >= 11) {
                // From API 11 onwards, we need to manually select the
                // THREAD_POOL_EXECUTOR
                AsyncTaskCompatHoneycomb.executeParallel(mSaveTask, image);
            } else {
                // Before API 11, all tasks were run in parallel
                mSaveTask.execute(image);
            }
            // AsyncTaskCompat.executeParallel(mSaveTask, image);
        }
    }

    static class AsyncTaskCompatHoneycomb {

        static <Params, Progress, Result> void executeParallel(AsyncTask<Params, Progress, Result> task, Params... params) {
            // 这里显示调用了THREAD_POOL_EXECUTOR，所以就可以使用该线程池了
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }

    }

    @SuppressLint("NewApi")
    private void virtualDisplay() {
        Surface sf = mImageReader.getSurface();
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "screen-mirror", mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(
                    Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {
                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            // 每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            // 总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            // 预览图片
            if (bitmap != null) {
                //也可以处理保存图片逻辑
                TessBaseAPI tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);//参数后面有说明。
                tessBaseAPI.setImage(bitmap);
                String text = tessBaseAPI.getUTF8Text();
                Log.e("GK", text);
                AutoClickAccessibilityService.cmd = 5;
                open = false;
                if (text.contains("男")) {
                    isman = true;
                } else {
                    isman = false;
                }
            }
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放PJSDK数据
     */
    public void destroy() {
        try {
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
        btn_dairu = (Button) findViewById(R.id.btn_dairu);
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

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        btn_dairu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Android6.0之前安装时就能复制，6.0之后要先请求权限，所以6.0以上的这个方法无用。
                copyToSD(LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (open) {
                    Log.e("GK", "截图中");
                    startScreenShot();
                    open = false;
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);

    }

    /**
     * 将assets中的识别库复制到SD卡中
     *
     * @param path 要存放在SD卡中的 完整的文件名。这里是"/storage/emulated/0//tessdata/chi_sim.traineddata"
     * @param name assets中的文件名 这里是 "chi_sim.traineddata"
     */
    public void copyToSD(String path, String name) {
        Log.i("copyToSD", "copyToSD: " + path);
        Log.i("copyToSD", "copyToSD: " + name);

        //如果存在就删掉
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }

        if (!f.exists()) {
            File p = new File(f.getParent());
            if (!p.exists()) {
                p.mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = this.getAssets().open(name);
            File file = new File(path);
            os = new FileOutputStream(file);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 请求到权限后在这里复制识别库
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    copyToSD(LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
                }
                break;
            default:
                break;
        }
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

