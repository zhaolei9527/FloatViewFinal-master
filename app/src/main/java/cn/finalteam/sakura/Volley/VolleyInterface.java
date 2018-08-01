package cn.finalteam.sakura.Volley;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;


/**
 * Created by 赵磊 on 2017/7/12.
 */

public abstract class VolleyInterface {
    private Context context;
    public Response.Listener<String> listener;
    public Response.ErrorListener errorListener;

    public VolleyInterface(Context context) {
        this.context = context;
    }

    public abstract void onMySuccess(String result);

    public abstract void onMyError(VolleyError error);

    public Response.Listener<String> loadingListener() {
        listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                onMySuccess(s);
            }
        };
        return listener;
    }

    public Response.ErrorListener errorListener() {
        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onMyError(volleyError);
            }
        };
        return errorListener;
    }



}