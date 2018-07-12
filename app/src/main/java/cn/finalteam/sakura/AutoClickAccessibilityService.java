package cn.finalteam.sakura;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
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


    private int dianzanpinlv = 0;
    private int jiahaoyoupinlv = 0;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {

            //拿到根节点
            final AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo == null) {
                return;
            }

            //开始遍历，这里拎出来细讲，直接往下看正文
            if (rootInfo.getChildCount() != 0) {
                DFS(rootInfo);
            }

            runnable = new Runnable() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    maxCount = 0;
                    if (FloatView.MODLE == 1) {
                        Log.e(TAG, "-----开始点赞");
                        isCheckdianzan(rootInfo);
                        OPEN = true;
                    } else if (FloatView.MODLE == 2) {
                        Log.e(TAG, "-----开始加好友");
                        isCheckjiahaoyou(rootInfo);
                        OPEN = true;
                    } else {
                        Log.e(TAG, "-----准备");
                        handler.postDelayed(runnable, 1000);
                    }

                }
            };

            if (!isopen) {
                if (dianzanpinlv == 0) {
                    dianzanpinlv = (int) SpUtil.get(App.context, "dianzanpinlv", 50);
                    jiahaoyoupinlv = (int) SpUtil.get(App.context, "jiahaoyoupinlv", 50);
                }
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
        Log.e(TAG, "maxCount:" + maxCount);
        targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        targetInfo.recycle();
    }

    int maxCount = 0;
    boolean friendShow = false;
    boolean friendItemShow = false;
    boolean friendopenShow = false;


    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckjiahaoyou(AccessibilityNodeInfo rootNodeInfo) {

        //"com.facebook.katana:id/friend_list_text_content_title"
        //android:id/list

        //进入加好友页面
        List<AccessibilityNodeInfo> FriendSuggestions = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/friend_requests_tab");

        if (!friendShow) {
            for (int i = 0; i < FriendSuggestions.size(); i++) {
                performClick(FriendSuggestions.get(i));
                friendShow = true;
            }
        }

        //加好友悬浮窗
        List<AccessibilityNodeInfo> activity_fab = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/activity_fab");

        if (!friendItemShow) {
            for (int i = 0; i < activity_fab.size(); i++) {
                performClick(activity_fab.get(i));
                friendItemShow = true;
            }
        }

        //进详情
        List<AccessibilityNodeInfo> friend_list_text_content_title = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/friend_list_text_content_title");

        for (int i = 0; i < friend_list_text_content_title.size(); i++) {
            performClick(friend_list_text_content_title.get(i));
        }


        List<AccessibilityNodeInfo> ADDFRIEND = rootNodeInfo.findAccessibilityNodeInfosByText("ADD FRIEND");

        if (maxCount < jiahaoyoupinlv) {
            for (int i = 0; i < ADDFRIEND.size(); i++) {
                //performClick(ADDFRIEND.get(i));
                maxCount = maxCount + 1;
            }
        } else {
            return;
        }

        List<AccessibilityNodeInfo> RecyclerView = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");


        for (int i = 0; i < RecyclerView.size(); i++) {
            if (friendopenShow) {
                performClick(RecyclerView.get(i).getChild(0));
                boolean b = RecyclerView.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                Log.e(TAG, "-----滑动列表");
            }
        }

        if (friendopenShow) {
            handler.postDelayed(runnable, 5000);
        } else {
            handler.postDelayed(runnable, 500);
        }

    }


    /**
     * 深度优先遍历寻找目标节点
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void DFS(AccessibilityNodeInfo rootInfo) {

        if (rootInfo == null || TextUtils.isEmpty(rootInfo.getClassName())) {
            return;
        }

        if (!"android.widget.TextView".equals(rootInfo.getClassName())) {
            for (int i = 0; i < rootInfo.getChildCount(); i++) {
                DFS(rootInfo.getChild(i));
            }
        } else if ("android.view.View".equals(rootInfo.getClassName())) {
            Log.e(TAG, rootInfo.getText().toString());
            //进入加好友列表
            if (rootInfo.getText().equals("ABOUT")) {
                performClick(rootInfo);
                friendopenShow = true;
            }
        } else {

            Log.e(TAG, rootInfo.getText().toString());

            //进入加好友列表
            if (rootInfo.getText().equals("Friend Suggestions")) {
                if (!friendopenShow) {
                    performClick(rootInfo);
                    friendopenShow = true;
                }
            }
        }

    }


    boolean likeShow = false;

    /**
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private synchronized void isCheckdianzan(AccessibilityNodeInfo rootNodeInfo) {

        List<AccessibilityNodeInfo> RecyclerView = rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");

        for (int i = 0; i < RecyclerView.size(); i++) {
            boolean b = RecyclerView.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            Log.e(TAG, "-----滑动列表");
        }

        List<AccessibilityNodeInfo> likeopen = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/news_feed_tab");

        if (!likeShow) {
            for (int i = 0; i < likeopen.size(); i++) {
                performClick(likeopen.get(i));
                likeShow = true;
            }
        }

        List<AccessibilityNodeInfo> like = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/feed_feedback_like_container");


        if (maxCount < dianzanpinlv) {
            for (int i = 0; i < like.size(); i++) {
                performClick(like.get(i));
                Log.e(TAG, "-----开始点赞" + i);
            }
        }

        if (friendopenShow) {
            handler.postDelayed(runnable, 2000);
        } else {
            handler.postDelayed(runnable, 500);
        }

    }
}