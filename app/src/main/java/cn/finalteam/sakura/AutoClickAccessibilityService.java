package cn.finalteam.sakura;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import cn.finalteam.sakura.Activity.MainActivity;
import cn.finalteam.sakura.widget.FloatView;

/**
 * cn.finalteam.sakura
 *
 * @author 赵磊
 * @date 2018/7/10
 * 功能描述：FaceBook无障碍插件
 */
public class AutoClickAccessibilityService extends AccessibilityService {
    public static final String TAG = "GK";
    private Runnable runnable;
    public static boolean isopen = false;
    public static boolean OPEN = false;
    public static long end = System.currentTimeMillis() + 100000000;
    private int dianzanpinlv = 0;
    private int jiahaoyoupinlv = 0;
    private String[] splitGuoLv;
    private AccessibilityNodeInfo[] rootInfo;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            //拿到根节点
            rootInfo = new AccessibilityNodeInfo[]{getRootInActiveWindow()};

            if (rootInfo[0] == null) {
                return;
            }

            if (runnable == null) {
                runnable = new Runnable() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        if (FloatView.MODLE == 1) {
                            Log.e(TAG, "-----点赞LOADING");
                            if (System.currentTimeMillis() > end) {
                                EasyToast.showShort(getApplicationContext(), "获取权限失败");
                                return;
                            }
                            rootInfo[0] = getRootInActiveWindow();
                            isCheckdianzan(rootInfo[0]);
                            OPEN = true;
                        } else if (FloatView.MODLE == 2) {
                            Log.e(TAG, "-----加好友LOADING");
                            if (System.currentTimeMillis() > end) {
                                EasyToast.showShort(getApplicationContext(), "获取权限失败");
                                return;
                            }
                            rootInfo[0] = getRootInActiveWindow();
                            isCheckjiahaoyou(rootInfo[0]);
                            OPEN = true;
                        } else if (FloatView.MODLE == 3) {
                            Log.e(TAG, "-----加好友的好友LOADING");
                            if (System.currentTimeMillis() > end) {
                                EasyToast.showShort(getApplicationContext(), "获取权限失败");
                                return;
                            }
                            rootInfo[0] = getRootInActiveWindow();
                            isCheckjiahaoyoudehaoyou(rootInfo[0]);
                            OPEN = true;
                        } else {
                            Log.e(TAG, "-----准备");
                            handler.postDelayed(runnable, 1000);
                        }
                    }
                };
            }

            if (!isopen) {
                maxCount = 0;
                String jiahaoyouguolv = (String) SpUtil.get(App.context, "jiahaoyouguolv", "");
                jiahaoyoutime = (int) SpUtil.get(App.context, "jiahaoyoutime", 3);
                if (jiahaoyoutime < 3) {
                    jiahaoyoutime = 3;
                }
                jiahaoyoutime = jiahaoyoutime * 1000;
                splitGuoLv = jiahaoyouguolv.split("#");
                Log.e(TAG, "初始-----准备--未开始");
                dianzanpinlv = (int) SpUtil.get(App.context, "dianzanpinlv", 50);
                jiahaoyoupinlv = (int) SpUtil.get(App.context, "jiahaoyoupinlv", 50);
                handler.postDelayed(runnable, 1000);
                isopen = !isopen;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInterrupt() {

    }

    Handler handler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void performClick(AccessibilityNodeInfo targetInfo) {
        try {
            Log.e(TAG, "maxCount:" + maxCount);
            targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            targetInfo.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int maxCount = 0;
    boolean friendShow = false;
    boolean friendItemShow = false;
    boolean friendopenShow = false;
    int jiahaoyoutime = 1000;

    public static int cmd = 1;
    public static int now = 0;

    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckjiahaoyouForIDBySex(final AccessibilityNodeInfo rootNodeInfo) {

        try {

            if (rootNodeInfo == null) {
                Log.e(TAG, "空结构");
                return;
            }

            if (1 == cmd) {
                final List<AccessibilityNodeInfo> list = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");
                Log.e(TAG, "进入列表");
                for (int i = 0; i < list.size(); i++) {
                    if (now > list.get(i).getChildCount() - 1) {
                        list.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        now = 0;
                    }
                    if (list.get(i).getChildCount() > 0) {
                        final int finalI = i;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                performClick(list.get(finalI).getChild(now));
                                Log.e(TAG, "进入好友主页" + (now));
                                cmd = 2;
                            }
                        }, 500);
                    }
                }
            }

            if (2 == cmd) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<AccessibilityNodeInfo> jianjie = rootNodeInfo.findAccessibilityNodeInfosByText("简介");
                        for (int i = 0; i < jianjie.size(); i++) {
                            performClick(jianjie.get(i));
                            cmd = 3;
                        }
                    }
                }, 2000);
                Log.e(TAG, "进入简介");
            }

            if (3 == cmd) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "进入web页");
                        List<AccessibilityNodeInfo> xingbie = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/collection_title_section");
                        for (int i = 0; i < xingbie.size(); i++) {
                            performClick(xingbie.get(i));
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    cmd = 4;
                                    if (!MainActivity.open) {
                                        if (4 == cmd) {
                                            Log.e(TAG, "准备截图");
                                            MainActivity.open = true;
                                        }
                                    }
                                }
                            }, 3000);
                        }
                    }
                }, 2000);
            }

            final List<AccessibilityNodeInfo> back = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/fb_logo_up_button");

            if (5 == cmd || 6 == cmd || 7 == cmd || 9 == cmd) {
                MainActivity.open = false;
                if (MainActivity.isman) {
                    for (int i = 0; i < back.size(); i++) {
                        final int finalI = i;

                        if (6 == cmd) {
                            Log.e(TAG, "是男性，准备返回");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    performClick(back.get(finalI));
                                    cmd = 8;
                                }
                            }, 1500);
                        }

                        if (5 == cmd) {
                            Log.e(TAG, "是男性，准备返回");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    performClick(back.get(finalI));
                                    cmd = 6;
                                }
                            }, 3000);
                        }

                    }
                } else {
                    for (int i = 0; i < back.size(); i++) {
                        performClick(back.get(i));
                        if (9 == cmd) {
                            Log.e(TAG, "不是男性，准备返回");
                            maxCount = maxCount + 1;
                            cmd = 1;
                        }
                        if (7 == cmd) {
                            Log.e(TAG, "不是男性，准备返回");
                            cmd = 9;
                        }
                        if (5 == cmd) {
                            Log.e(TAG, "不是男性，准备返回");
                            cmd = 7;
                        }
                        now = now + 1;
                    }
                }
            }

            if (8 == cmd) {
                final List<AccessibilityNodeInfo> add = rootNodeInfo.findAccessibilityNodeInfosByText("加为好友");
                for (int i = 0; i < add.size(); i++) {
                    final int finalI = i;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "是男性，加上好友");
                            performClick(add.get(finalI));
                            now = now + 1;
                            cmd = 10;
                        }
                    }, 2000);
                }
            }

            if (10 == cmd) {
                for (int i = 0; i < back.size(); i++) {
                    final int finalI = i;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "是男性，加完好友，进行返回");
                            maxCount = maxCount + 1;
                            performClick(back.get(finalI));
                            cmd = 1;
                        }
                    }, 2000);
                }
            }
            Log.e(TAG, "cmd:" + cmd);

            handler.postDelayed(runnable, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckjiahaoyouForID(final AccessibilityNodeInfo rootNodeInfo) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                //进入加好友页面
                List<AccessibilityNodeInfo> friendsuggestions = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/friend_requests_tab");

                if (!friendShow) {
                    for (int i = 0; i < friendsuggestions.size(); i++) {
                        Log.e(TAG, "进入加好友页面");
                        performClick(friendsuggestions.get(i));
                        friendShow = true;
                    }
                }

                //加好友悬浮窗
                List<AccessibilityNodeInfo> activity_fab = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/activity_fab");

                if (!friendItemShow) {
                    for (int i = 0; i < activity_fab.size(); i++) {
                        Log.e(TAG, "加好友悬浮窗");
                        performClick(activity_fab.get(i));
                        friendItemShow = true;
                    }
                }

                List<AccessibilityNodeInfo> Friend = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/fab_label");

                for (int i = 0; i < Friend.size(); i++) {
                    if (!friendopenShow) {
                        Log.e(TAG, "进入加好友列表");
                        performClick(Friend.get(i));
                        friendopenShow = true;
                    }
                }

                List<AccessibilityNodeInfo> recyclerview = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");

                for (int i = 0; i < recyclerview.size(); i++) {
                    if (friendopenShow) {
                        for (int i1 = 0; i1 < recyclerview.get(i).getChildCount(); i1++) {
                            if (recyclerview.isEmpty()) {
                                continue;
                            }
                            List<AccessibilityNodeInfo> names = recyclerview.get(i).getChild(i1).findAccessibilityNodeInfosByViewId("com.facebook.katana:id/friend_list_text_content_title");
                            if (names.isEmpty()) {
                                continue;
                            }
                            for (int i2 = 0; i2 < names.size(); i2++) {
                                if (splitGuoLv.length != 0) {
                                    boolean add = true;
                                    for (int i3 = 0; i3 < splitGuoLv.length; i3++) {
                                        String s = names.get(i2).getText().toString();
                                        Log.e(TAG, s + "**" + splitGuoLv[i3].toString());
                                        boolean contains = names.get(i2).getText().toString().contains(splitGuoLv[i3]);
                                        if (contains) {
                                            add = false;
                                        }
                                    }
                                    if (add) {
                                        maxCount = maxCount + 1;
                                        List<AccessibilityNodeInfo> friends = recyclerview.get(i).getChild(i1).findAccessibilityNodeInfosByViewId("com.facebook.katana:id/friend_request_positive_button");
                                        if (!friends.isEmpty()) {
                                            try {
                                                Thread.sleep(jiahaoyoutime);
                                                performClick(friends.get(0));
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Log.e(TAG, "-----加一个好友" + maxCount);
                                    }
                                } else {
                                    maxCount = maxCount + 1;
                                    List<AccessibilityNodeInfo> friends = recyclerview.get(i).getChild(i1).findAccessibilityNodeInfosByViewId("com.facebook.katana:id/friend_request_positive_button");
                                    for (int i4 = 0; i4 < friends.size(); i4++) {
                                        try {
                                            Thread.sleep(jiahaoyoutime);
                                            performClick(friends.get(i));
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Log.e(TAG, "-----加一个好友" + maxCount);
                                    }
                                }
                            }
                        }
                        boolean b = recyclerview.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        Log.e(TAG, "-----滑动列表");
                    }
                }

                if (maxCount < jiahaoyoupinlv) {
                    if (friendopenShow) {
                        handler.postDelayed(runnable, 5000);
                    } else {
                        handler.postDelayed(runnable, 500);
                    }
                }
            }
        }.start();

    }


    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckjiahaoyoudehaoyou(final AccessibilityNodeInfo rootNodeInfo) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    List<AccessibilityNodeInfo> friends = rootNodeInfo.findAccessibilityNodeInfosByText("옵션 더보기");

                    List<AccessibilityNodeInfo> openFriends = rootNodeInfo.findAccessibilityNodeInfosByText("친구 보기");

                    for (int i = 0; i < friends.size(); i++) {
                        sleep(1000);
                        performClick(friends.get(i));
                    }

                    for (int i = 0; i < openFriends.size(); i++) {
                        Log.e(TAG, "进入好友的好友列表");
                        sleep(1000);
                        performClick(openFriends.get(i));
                    }

                    if (maxCount < jiahaoyoupinlv) {
                        handler.postDelayed(runnable, 1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (maxCount < jiahaoyoupinlv) {
                        handler.postDelayed(runnable, 2000);
                    }
                }

            }
        }.start();

    }


    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckjiahaoyou(final AccessibilityNodeInfo rootNodeInfo) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (!FloatView.PAUSE) {

                        Log.e(TAG, "----" + FloatView.PAUSE);

                        List<AccessibilityNodeInfo> like = rootNodeInfo.findAccessibilityNodeInfosByText("친구 추가");
                        List<AccessibilityNodeInfo> recyclerview = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");

                        for (int i = 0; i < recyclerview.size(); i++) {
                            recyclerview.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                            EasyToast.showShort(getApplicationContext(), "下一页");
                            Log.e(TAG, "-----滑动列表");
                        }

                        for (int i = 0; i < recyclerview.size(); i++) {
                            for (int i1 = 0; i1 < recyclerview.get(i).getChildCount(); i1++) {
                                if ("android.widget.TextView".equals(recyclerview.get(i).getChild(i1).getChild(1).getClassName().toString())) {
                                    Log.e(TAG, "*getText*" + recyclerview.get(i).getChild(i1).getChild(1).getText().toString());
                                    if (maxCount < jiahaoyoupinlv) {
                                        boolean add = true;
                                        for (int i3 = 0; i3 < splitGuoLv.length; i3++) {
                                            if (recyclerview.get(i).getChild(i1).getChild(1).getText().toString().contains(splitGuoLv[i3])) {
                                                add = false;
                                                Log.e(TAG, "-----addFriend---" + add);
                                                break;
                                            }
                                        }
                                        if (add) {
                                            maxCount = maxCount + 1;
                                            performClick(like.get(i1));
                                            Log.e(TAG, "-----addFriend" + maxCount);
                                            try {
                                                sleep(jiahaoyoutime);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.e(TAG, "-----addFriend" + maxCount);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (maxCount < jiahaoyoupinlv) {
                        handler.postDelayed(runnable, 4000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (maxCount < jiahaoyoupinlv) {
                        handler.postDelayed(runnable, 4000);
                    }
                }

            }
        }.start();

    }

    boolean likeShow = false;
    boolean huadonged = false;

    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckdianzan(AccessibilityNodeInfo rootNodeInfo) {
        if (!FloatView.PAUSE) {
            Log.e(TAG, "----" + FloatView.PAUSE);
            List<AccessibilityNodeInfo> RecyclerView = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");
            for (int i = 0; i < RecyclerView.size(); i++) {
                boolean b = RecyclerView.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                EasyToast.showShort(getApplicationContext(), "下一页");
                Log.e(TAG, "-----滑动列表");
                if (b) {
                    huadonged = true;
                    List<AccessibilityNodeInfo> like = rootNodeInfo.findAccessibilityNodeInfosByText("좋아요");
                    if (maxCount < dianzanpinlv) {
                        for (int i1 = 0; i1 < like.size(); i1++) {
                            if (huadonged) {
                                maxCount = maxCount + 1;
                                performClick(like.get(i1));
                            }
                            Log.e(TAG, "-----开始点赞" + maxCount);
                        }
                        huadonged = false;
                    }
                }
            }
            List<AccessibilityNodeInfo> likeopen = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/news_feed_tab");
            if (!likeShow) {
                for (int i = 0; i < likeopen.size(); i++) {
                    performClick(likeopen.get(i));
                    likeShow = true;
                }
            }
        }
        if (maxCount < dianzanpinlv) {
            handler.postDelayed(runnable, 2000);
        }
//      List<AccessibilityNodeInfo> like = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/feed_feedback_like_container");
    }


}