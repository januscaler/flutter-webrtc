package com.cloudwebrtc.webrtc.utils;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodChannel;

/**
 * Delivers a {@link MethodChannel.Result} on the main thread and at most once.
 *
 * A second reply to the same method call throws
 * {@code IllegalStateException: Reply already submitted} inside the Flutter
 * engine and crashes the app, so any extra reply is logged and dropped.
 */
public final class AnyThreadResult implements MethodChannel.Result {
    private static final String TAG = "AnyThreadResult";

    final private MethodChannel.Result result;
    final private Handler handler = new Handler(Looper.getMainLooper());
    final private AtomicBoolean replied = new AtomicBoolean(false);

    public AnyThreadResult(MethodChannel.Result result) {
        this.result = result;
    }

    @Override
    public void success(Object o) {
        if (claim("success")) post(()->result.success(o));
    }

    @Override
    public void error(String s, String s1, Object o) {
        if (claim("error " + s + ": " + s1)) post(()->result.error(s, s1, o));
    }

    @Override
    public void notImplemented() {
        if (claim("notImplemented")) post(result::notImplemented);
    }

    private boolean claim(String what) {
        if (replied.compareAndSet(false, true)) {
            return true;
        }
        Log.w(TAG, "Reply already submitted; dropping " + what);
        return false;
    }

    private void post(Runnable r) {
        if(Looper.getMainLooper() == Looper.myLooper()){
            r.run();
        }else{
            handler.post(r);
        }
    }
}
