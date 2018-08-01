package cn.finalteam.sakura.Volley;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

import cn.finalteam.sakura.App;


/**
 * Created by 赵磊 on 2017/7/12.
 */

public class VolleyRequest {
    public static StringRequest request;

    public static void RequestGet(Context context, String url, String tag, VolleyInterface vif) {
        App.getQueues().cancelAll(tag);
        request = new StringRequest(Request.Method.GET, url, vif.loadingListener(), vif.errorListener());
        //tag设置
        request.setTag(tag);
        //网络状态检测
        App.getQueues().add(request);
    }

    public static void RequestPost(Context context, String url, String tag, final Map<String, String> params, VolleyInterface vif) {

        request = new StringRequest(Request.Method.POST, url, vif.loadingListener(), vif.errorListener()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //tag设置
        request.setTag(tag);
        App.getQueues().add(request);
    }

    public static void RequestPost(Context context, String url, final Map<String, String> params, VolleyInterface vif) {
        request = new StringRequest(Request.Method.POST, url, vif.loadingListener(), vif.errorListener()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //网络状态检测
            App.getQueues().add(request);
    }

}