/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 单例助手:安装一个默认显示未处理的异常处理程序
 * 一个信息对话框并杀死应用。
 * 有用的应用程序错误处理包括抛出runtimeexception。
 * 注:几乎总是更有用
 * Thread.setDefaultUncaughtExceptionHandler()而不是Thread.setUncaughtExceptionHandler(),
 * 适用于后台线程
 */
public class UnhandledExceptionHandler111
        implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "AppRTCDemoActivity";
    private final Activity activity;


    /**
     * 单例助手:安装一个默认显示未处理的异常处理程序
     * 一个信息对话框并杀死应用。
     * 有用的应用程序错误处理包括抛出runtimeexception。
     * 注:几乎总是更有用
     * Thread.setDefaultUncaughtExceptionHandler()而不是Thread.setUncaughtExceptionHandler(),
     * 适用于后台线程
     */
    public UnhandledExceptionHandler111(final Activity activity) {
        this.activity = activity;
    }

    public void uncaughtException(Thread unusedThread, final Throwable e) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String title = "Fatal error: " + getTopLevelCauseMessage(e);
                String msg = getRecursiveStackTrace(e);
                TextView errorView = new TextView(activity);
                errorView.setText(msg);
                errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                ScrollView scrollingContainer = new ScrollView(activity);
                scrollingContainer.addView(errorView);
                Log.e(TAG, title + "\n\n" + msg);
                DialogInterface.OnClickListener listener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                                System.exit(1);
                            }
                        };
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(activity);
                builder
                        .setTitle(title)
                        .setView(scrollingContainer)
                        .setPositiveButton("Exit", listener).show();
            }
        });
    }

    // Returns the Message attached to the original Cause of |t|.
    private static String getTopLevelCauseMessage(Throwable t) {
        Throwable topLevelCause = t;
        while (topLevelCause.getCause() != null) {
            topLevelCause = topLevelCause.getCause();
        }
        return topLevelCause.getMessage();
    }

    // Returns a human-readable String of the stacktrace in |t|, recursively
    // through all Causes that led to |t|.
    private static String getRecursiveStackTrace(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
