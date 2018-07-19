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

import cn.finalteam.sakura.widget.FloatView;

/**
 * cn.finalteam.sakura
 *
 * @author 赵磊
 * @date 2018/7/10
 * 功能描述：
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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {

            //拿到根节点
            final AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo == null) {
                return;
            }

            runnable = new Runnable() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    if (FloatView.MODLE == 1) {
                        Log.e(TAG, "-----开始点赞");
                        if (System.currentTimeMillis() > end) {
                            return;
                        }
                        isCheckdianzan(rootInfo);
                        OPEN = true;
                    } else if (FloatView.MODLE == 2) {
                        Log.e(TAG, "-----开始加好友");
                        if (System.currentTimeMillis() > end) {
                            Log.e(TAG, "-----end");
                            return;
                        }
                        isCheckjiahaoyou(rootInfo);
                        OPEN = true;
                    } else {
                        Log.e(TAG, "-----准备");
                        handler.postDelayed(runnable, 1000);
                    }

                }
            };

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

                List<AccessibilityNodeInfo> Friend = rootNodeInfo.findAccessibilityNodeInfosByText("Friend Suggestions");

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
    private synchronized void isCheckjiahaoyou(final AccessibilityNodeInfo rootNodeInfo) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (!FloatView.PAUSE) {

                        List<AccessibilityNodeInfo> like = rootNodeInfo.findAccessibilityNodeInfosByText("친구 추가");
                        List<AccessibilityNodeInfo> recyclerview = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");

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

                        for (int i = 0; i < recyclerview.size(); i++) {
                            recyclerview.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                            Log.e(TAG, "-----滑动列表");
                        }

                    }

                    if (maxCount < jiahaoyoupinlv) {
                        handler.postDelayed(runnable, 2000);
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


    boolean likeShow = false;

    boolean huadonged = false;

    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckdianzan(AccessibilityNodeInfo rootNodeInfo) {

        if (!FloatView.PAUSE) {

            List<AccessibilityNodeInfo> RecyclerView = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");

            for (int i = 0; i < RecyclerView.size(); i++) {
                boolean b = RecyclerView.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
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