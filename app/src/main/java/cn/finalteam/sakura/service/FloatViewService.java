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

package cn.finalteam.sakura.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xdandroid.hellodaemon.AbsWorkService;

import cn.finalteam.sakura.widget.FloatView;

/**
 * Desction:Float view service
 * Date:15/10/26 下午5:15
 */
public class FloatViewService extends AbsWorkService {

    private FloatView mFloatView;
    public static boolean sShouldStopService;

    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return null;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        if (mFloatView == null) {
            mFloatView = new FloatView(this);
        }
    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {

    }

    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent, Void alwaysNull) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        destroyFloat();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FloatViewServiceBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mFloatView = new FloatView(this);
    }

    public void showFloat() {
        if (mFloatView != null) {
            mFloatView.show();
        }
    }

    public void destroyFloat() {
        if (mFloatView != null) {
            mFloatView.destroy();
        }
        mFloatView = null;
    }


    public class FloatViewServiceBinder extends Binder {
        public FloatViewService getService() {
            return FloatViewService.this;
        }
    }
}
