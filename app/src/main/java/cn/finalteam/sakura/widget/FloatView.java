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

package cn.finalteam.sakura.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStream;

import cn.finalteam.sakura.App;
import cn.finalteam.sakura.AutoClickAccessibilityService;
import cn.finalteam.sakura.EasyToast;
import cn.finalteam.sakura.utils.ResourceUtils;

/**
 * Desction:悬浮窗
 * Date:15/10/26 下午8:39
 */
public class FloatView extends FrameLayout implements OnTouchListener {

    private final int HANDLER_TYPE_HIDE_LOGO = 100;//隐藏LOGO
    private final int HANDLER_TYPE_CANCEL_ANIM = 101;//退出动画

    private WindowManager.LayoutParams mWmParams;
    private WindowManager mWindowManager;
    private Context mContext;

    //private View mRootFloatView;
    private ImageView mIvFloatLogo;
    private ImageView mIvFloatLoader;
    private LinearLayout mLlFloatMenu;
    private TextView mTvAccount;
    private TextView mTvFeedback;
    private FrameLayout mFlFloatLogo;

    private boolean mIsRight;//logo是否在右边
    private boolean mCanHide;//是否允许隐藏
    private float mTouchStartX;
    private float mTouchStartY;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mDraging;
    private boolean mShowLoader = true;

    private Handler handler = new Handler();


    final Handler mTimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_TYPE_HIDE_LOGO) {
                // 比如隐藏悬浮框
                if (mCanHide) {
                    mCanHide = false;
                    if (mIsRight) {
                        mIvFloatLogo.setImageResource(ResourceUtils.getDrawableId(mContext, "pj_image_float_right"));
                    } else {
                        mIvFloatLogo.setImageResource(ResourceUtils.getDrawableId(mContext, "pj_image_float_left"));
                    }
                    mWmParams.alpha = 0.7f;
                    mWindowManager.updateViewLayout(FloatView.this, mWmParams);
                    refreshFloatMenu(mIsRight);
                    mLlFloatMenu.setVisibility(View.GONE);
                }
            } else if (msg.what == HANDLER_TYPE_CANCEL_ANIM) {
                mIvFloatLoader.clearAnimation();
                mIvFloatLoader.setVisibility(View.GONE);
                mShowLoader = false;
            }
            super.handleMessage(msg);
        }
    };

    public FloatView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context mContext) {
        this.mContext = App.context;

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        // 更新浮动窗口位置参数 靠边
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        this.mWmParams = new WindowManager.LayoutParams();
        // 设置window type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // 设置图片格式，效果为背景透明
        mWmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为左侧置�?
        mWmParams.gravity = Gravity.LEFT | Gravity.TOP;

        mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();

        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        mWmParams.x = 0;
        mWmParams.y = mScreenHeight / 2;

        // 设置悬浮窗口长宽数据
        mWmParams.width = LayoutParams.WRAP_CONTENT;
        mWmParams.height = LayoutParams.WRAP_CONTENT;
        addView(createView(mContext));
        mWindowManager.addView(this, mWmParams);

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 更新浮动窗口位置参数 靠边
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        int oldX = mWmParams.x;
        int oldY = mWmParams.y;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE://横屏
                if (mIsRight) {
                    mWmParams.x = mScreenWidth;
                    mWmParams.y = oldY;
                } else {
                    mWmParams.x = oldX;
                    mWmParams.y = oldY;
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT://竖屏
                if (mIsRight) {
                    mWmParams.x = mScreenWidth;
                    mWmParams.y = oldY;
                } else {
                    mWmParams.x = oldX;
                    mWmParams.y = oldY;
                }
                break;
        }
        mWindowManager.updateViewLayout(this, mWmParams);
    }

    /**
     * 创建Float view
     *
     * @param context
     * @return
     */
    private View createView(final Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        // 从布局文件获取浮动窗口视图
        View rootFloatView = inflater.inflate(ResourceUtils.getLayoutId(context, "pj_widget_float_view"), null);
        mFlFloatLogo = (FrameLayout) rootFloatView.findViewById(ResourceUtils.getId(context, "pj_float_view"));

        mIvFloatLogo = (ImageView) rootFloatView.findViewById(ResourceUtils.getId(context,
                "pj_float_view_icon_imageView"));
        mIvFloatLoader = (ImageView) rootFloatView.findViewById(ResourceUtils.getId(
                context, "pj_float_view_icon_notify"));
        mLlFloatMenu = (LinearLayout) rootFloatView.findViewById(ResourceUtils.getId(
                context, "ll_menu"));
        mTvAccount = (TextView) rootFloatView.findViewById(ResourceUtils.getId(
                context, "tv_account"));
        mTvAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mLlFloatMenu.setVisibility(View.GONE);
                openUserCenter();
            }
        });
        mTvFeedback = (TextView) rootFloatView.findViewById(ResourceUtils.getId(
                context, "tv_feedback"));
        mTvFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                openFeedback();
                mLlFloatMenu.setVisibility(View.GONE);
            }
        });
        rootFloatView.setOnTouchListener(this);
        rootFloatView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDraging) {
                    if (mLlFloatMenu.getVisibility() == View.VISIBLE) {
                        mLlFloatMenu.setVisibility(View.GONE);
                    } else {
                        mLlFloatMenu.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        rootFloatView.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        return rootFloatView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        Log.e("FloatView", "x:" + x);
        Log.e("FloatView", "y:" + y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                mIvFloatLogo.setImageResource(ResourceUtils.getDrawableId(
                        mContext, "pj_image_float_logo"));
                mWmParams.alpha = 1f;
                mWindowManager.updateViewLayout(this, mWmParams);
                mDraging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float mMoveStartX = event.getX();
                float mMoveStartY = event.getY();
                // 如果移动量大于3才移动
                if (Math.abs(mTouchStartX - mMoveStartX) > 3
                        && Math.abs(mTouchStartY - mMoveStartY) > 3) {
                    mDraging = true;
                    // 更新浮动窗口位置参数
                    mWmParams.x = (int) (x - mTouchStartX);
                    mWmParams.y = (int) (y - mTouchStartY);
                    mWindowManager.updateViewLayout(this, mWmParams);
                    mLlFloatMenu.setVisibility(View.GONE);
                    return false;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (mWmParams.x >= mScreenWidth / 2) {
                    mWmParams.x = mScreenWidth;
                    mIsRight = true;
                } else if (mWmParams.x < mScreenWidth / 2) {
                    mIsRight = false;
                    mWmParams.x = 0;
                }
                mIvFloatLogo.setImageResource(ResourceUtils.getDrawableId(
                        mContext, "pj_image_float_logo"));
                refreshFloatMenu(mIsRight);
                //timerForHide();
                mWindowManager.updateViewLayout(this, mWmParams);
                // 初始化
                mTouchStartX = mTouchStartY = 0;
                break;
            default:
                break;
        }
        return false;
    }


    private void removeFloatView() {
        try {
            mWindowManager.removeView(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 隐藏悬浮窗
     */
    public void hide() {
        setVisibility(View.GONE);
        Message message = mTimerHandler.obtainMessage();
        message.what = HANDLER_TYPE_HIDE_LOGO;
        mTimerHandler.sendMessage(message);
    }

    /**
     * 显示悬浮窗
     */
    public void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            if (mShowLoader) {
                mIvFloatLogo.setImageResource(ResourceUtils.getDrawableId(
                        mContext, "pj_image_float_logo"));
                mWmParams.alpha = 1f;
                mWindowManager.updateViewLayout(this, mWmParams);
                //timerForHide();
                mShowLoader = false;
//                mTimer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        mTimerHandler.sendEmptyMessage(HANDLER_TYPE_CANCEL_ANIM);
//                    }
//                }, 3000);
            }
        }
    }

    /**
     * 刷新float view menu
     *
     * @param right
     */
    private void refreshFloatMenu(boolean right) {
        if (right) {
            FrameLayout.LayoutParams paramsFloatImage = (FrameLayout.LayoutParams) mIvFloatLogo.getLayoutParams();
            paramsFloatImage.gravity = Gravity.RIGHT;
            mIvFloatLogo.setLayoutParams(paramsFloatImage);
            FrameLayout.LayoutParams paramsFlFloat = (FrameLayout.LayoutParams) mFlFloatLogo.getLayoutParams();
            paramsFlFloat.gravity = Gravity.RIGHT;
            mFlFloatLogo.setLayoutParams(paramsFlFloat);

            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
            int padding52 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, mContext.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams paramsMenuAccount = (LinearLayout.LayoutParams) mTvAccount.getLayoutParams();
            paramsMenuAccount.rightMargin = padding;
            paramsMenuAccount.leftMargin = padding;
            mTvAccount.setLayoutParams(paramsMenuAccount);

            LinearLayout.LayoutParams paramsMenuFb = (LinearLayout.LayoutParams) mTvFeedback.getLayoutParams();
            paramsMenuFb.rightMargin = padding52;
            paramsMenuFb.leftMargin = padding;
            mTvFeedback.setLayoutParams(paramsMenuFb);
        } else {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvFloatLogo.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            params.gravity = Gravity.LEFT;
            mIvFloatLogo.setLayoutParams(params);
            FrameLayout.LayoutParams paramsFlFloat = (FrameLayout.LayoutParams) mFlFloatLogo.getLayoutParams();
            paramsFlFloat.gravity = Gravity.LEFT;
            mFlFloatLogo.setLayoutParams(paramsFlFloat);

            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
            int padding52 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, mContext.getResources().getDisplayMetrics());

            LinearLayout.LayoutParams paramsMenuAccount = (LinearLayout.LayoutParams) mTvAccount.getLayoutParams();
            paramsMenuAccount.rightMargin = padding;
            paramsMenuAccount.leftMargin = padding52;
            mTvAccount.setLayoutParams(paramsMenuAccount);

            LinearLayout.LayoutParams paramsMenuFb = (LinearLayout.LayoutParams) mTvFeedback.getLayoutParams();
            paramsMenuFb.rightMargin = padding;
            paramsMenuFb.leftMargin = padding;
            mTvFeedback.setLayoutParams(paramsMenuFb);
        }
    }

    private static int i = 6;

    public static int MODLE = 0;

    /**
     * 开始点赞流程
     */
    private void openUserCenter() {

        if (AutoClickAccessibilityService.OPEN) {
            Toast.makeText(mContext, "点赞正在运行...请待任务结束", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(mContext, "开始点赞倒计时", Toast.LENGTH_SHORT).show();
        i = 6;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (i > 0) {
                    EasyToast.showShort(mContext, String.valueOf(i));
                    handler.postDelayed(this, 1000);
                    i--;
                }
                if (i == 0) {
                    //进入点赞
                    MODLE = 1;
                }
            }
        }, 1000);
    }


    /**
     * 开始加好友 2560x1536
     */
    private void openFeedback() {

        if (AutoClickAccessibilityService.OPEN) {
            Toast.makeText(mContext, "加好友正在运行...请待任务结束", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(mContext, "开始加好友倒计时", Toast.LENGTH_SHORT).show();
        i = 6;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (i > 0) {
                    EasyToast.showShort(mContext, String.valueOf(i));
                    handler.postDelayed(this, 1000);
                    i--;
                }
                if (i == 0) {
                    //进入加好友
                    Toast.makeText(mContext, "开始加好友", Toast.LENGTH_SHORT).show();
                    MODLE = 2;
                }
            }
        }, 1000);

    }

    /**
     * 打印点击的点的坐标
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.e("FloatView", "X at " + x + ";Y at " + y);
        return true;
    }


    /**
     * 是否Float view
     */
    public void destroy() {
        hide();
        removeFloatView();
    }


    /**
     * 执行shell命令
     *
     * @param cmd
     */
    public static void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}



