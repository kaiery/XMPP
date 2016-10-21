/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
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
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.ToastUtils;

import org.appspot.apprtc.AppRTCClient.RoomConnectionParameters;
import org.appspot.apprtc.AppRTCClient.SignalingParameters;
import org.appspot.apprtc.PeerConnectionClient.PeerConnectionParameters;
import org.appspot.apprtc.util.LooperExecutor;
import org.webrtc.Camera2Enumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;

/**
 * 对等连接呼叫建立,呼叫等待和调用视图
 */
public class CallActivity extends Activity
        implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents,
        CallFragment.OnCallEvents {

    public static final String EXTRA_ROOMID =
            "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_LOOPBACK =
            "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL =
            "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_CAMERA2 =
            "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH =
            "org.appspot.apprtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT =
            "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS =
            "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE =
            "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC =
            "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED =
            "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED =
            "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_AUDIO_BITRATE =
            "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC =
            "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED =
            "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_OPENSLES_ENABLED =
            "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC =
            "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC =
            "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS =
            "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL =
            "org.appspot.apprtc.ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_DISPLAY_HUD =
            "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_CMDLINE =
            "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_RUNTIME =
            "org.appspot.apprtc.RUNTIME";
    private static final String TAG = "CallRTCClient";

    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET"
    };

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private PeerConnectionClient peerConnectionClient = null;
    private AppRTCClient appRtcClient;
    private SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager = null;
    private EglBase rootEglBase;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private ScalingType scalingType;
    private Toast logToast;
    private boolean commandLineRun;
    private int runTimeMs;
    private boolean activityRunning;
    private RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs = 0;
    private boolean micEnabled = true;

    // Controls
    private CallFragment callFragment;
    private HudFragment hudFragment;
    private CpuMonitor cpuMonitor;

    /**
     * 用户信息
     */
    private String mUserAccount;
    private String mTargetAccount;
    public String mNickName;
    public String mTargetNickName;
    /**
     * 延时退出的句柄
     */
    private Handler handler = new Handler();
    /**
     * 延时退出时间
     */
    private static int mDelayTime = 1000*60;
    /**
     * 视频激活状态
     */
    private boolean mActivation;
    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;
    /**
     * 绑定服务的连接对象
     */
    private MyServiceConnection mMyServiceConnection = new MyServiceConnection();
    /**
     * 绑定服务的连接对象类
     */
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
    private void init(){
        //绑定服务
        Intent service = new Intent(this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
        IMService.isVideo = true;
        IMService.isGetStatus = false;
        mUserAccount = IMService.mCurAccount;
        mNickName = IMService.mCurNickName;
        mTargetAccount = getIntent().getStringExtra("mTargetAccount");
        mTargetNickName = getIntent().getStringExtra("mTargetNickName");
        initListener();
    }

    /**
     * 注册对方忙的时候 的动态广播消息
     */
    private void initListener() {
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction(Const.VIDEO_WORKING_BROADCAST_ACTION);
        registerReceiver(dynamicReceiver, filter_dynamic);
    }
    /**
     * 广播接受者
     */
    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.VIDEO_WORKING_BROADCAST_ACTION)){
                final String msg = intent.getStringExtra("msg");
                ToastUtils.showToastSafe(msg);//"对方正忙，无法进行视频聊天"
                if(runnable!=null){
                    handler.removeCallbacks(runnable);
                }
                close();
            }
        }
    };
    private Runnable runnable = new Runnable() {
        public void run() {
            if(!mActivation){
                ToastUtils.showToastSafe_Long("连接超时，可能对方不在线");
                close();
            }
        }
    };
    /**
     * 倒计时任务
     */
    private void startTimeTask() {
        handler.postDelayed(runnable,mDelayTime);
    }
    /**
     * 主动关闭视频聊天
     */
    private void close(){
        if(runnable!=null){
            handler.removeCallbacks(runnable);
        }
        //设置我不再视频聊天
        IMService.isVideo = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }







    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(
                new UnhandledExceptionHandler(this));
        // 设置窗口风格fullscreen-window大小。添加内容之前需要做的
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                LayoutParams.FLAG_FULLSCREEN
                        | LayoutParams.FLAG_KEEP_SCREEN_ON
                        | LayoutParams.FLAG_DISMISS_KEYGUARD
                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 设置end
        setContentView(R.layout.activity_rtc_call);

        init();

        //ice连接
        iceConnected = false;
        //信令参数
        signalingParameters = null;
        //缩放类型
        scalingType = ScalingType.SCALE_ASPECT_FILL;

        // 创建UI控制
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
        callFragment = new CallFragment();
        hudFragment = new HudFragment();

        // 显示/隐藏 在视图中点击呼叫控制片段.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        localRender.setOnClickListener(listener);
        remoteRender.setOnClickListener(listener);

        // 创建视频渲染器
        rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setZOrderMediaOverlay(true);
        updateVideoView();

        // 检查委托权限.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("权限： " + permission + " 没有被授予。");
                Log.e(TAG, "权限： " + permission + " 没有被授予。" );
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }

        // 获取Intent参数
        final Intent intent = getIntent();
        Uri roomUri = intent.getData();
        if (roomUri == null) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "在意图没有得到任何URL!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "不正确的房间里ID的意图!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        boolean useCamera2 = Camera2Enumerator.isSupported()
                && intent.getBooleanExtra(EXTRA_CAMERA2, true);

        peerConnectionParameters = new PeerConnectionParameters(
                intent.getBooleanExtra(EXTRA_VIDEO_CALL, true),
                loopback,
                tracing,
                useCamera2,
                intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0),
                intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0),
                intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0),
                intent.getStringExtra(EXTRA_VIDEOCODEC),
                intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                intent.getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false),
                intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0),
                intent.getStringExtra(EXTRA_AUDIOCODEC),
                intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                intent.getBooleanExtra(EXTRA_ENABLE_LEVEL_CONTROL, false));
        commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
        runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

        //创建连接客户端。用DirectRTCClient如果房间的名字是一个IP,否则使用标准 的 WebSocketRTCClient
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this, new LooperExecutor());
        } else {
            Log.i(TAG, "使用DirectRTCClient因为房间名字看起来像一个IP");
            appRtcClient = new DirectRTCClient(this);
        }
        // 创建连接参数.
        roomConnectionParameters = new RoomConnectionParameters(
                roomUri.toString(), roomId, loopback);

        // 创建CPU监视
        cpuMonitor = new CpuMonitor(this);
        hudFragment.setCpuMonitor(cpuMonitor);

        // 发送意图参数 到 fragments
        callFragment.setArguments(intent.getExtras());
        hudFragment.setArguments(intent.getExtras());
        // Activate call and HUD fragments and start the call.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, callFragment);
        ft.add(R.id.hud_fragment_container, hudFragment);
        ft.commit();
        startCall();

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }

        peerConnectionClient = PeerConnectionClient.getInstance();
        if (loopback) {
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
            peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }
        peerConnectionClient.createPeerConnectionFactory(
                CallActivity.this, peerConnectionParameters, CallActivity.this);



        startTimeTask();
    }

    // Activity interfaces
    @Override
    public void onPause() {
        super.onPause();
        activityRunning = false;
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }
        cpuMonitor.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        activityRunning = true;
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
        cpuMonitor.resume();
    }

    @Override
    protected void onDestroy() {
        ////////////////////////用户控制
        unregisterReceiver(dynamicReceiver);
        if(runnable!=null){
            handler.removeCallbacks(runnable);
        }
        mImService.callbackIdleVideoMsg(mTargetAccount,"视频通话结束");
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        IMService.isVideo = false;
        ////////////////////////用户控制end

        disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        rootEglBase.release();
        super.onDestroy();
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onVideoScalingSwitch(ScalingType scalingType) {
        this.scalingType = scalingType;
        updateVideoView();
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }



    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!iceConnected || !callFragment.isAdded()) {
            return;
        }
        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (callControlFragmentVisible) {
            ft.show(callFragment);
            ft.show(hudFragment);
        } else {
            ft.hide(callFragment);
            ft.hide(hudFragment);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    /**
     * 更新视频视图
     */
    private void updateVideoView() {
        remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        remoteRender.setScalingType(scalingType);
        remoteRender.setMirror(false);

        if (iceConnected) {
            localRenderLayout.setPosition(
                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
            localRender.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        } else {
            localRenderLayout.setPosition(
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
            localRender.setScalingType(scalingType);
        }
        localRender.setMirror(true);

        localRender.requestLayout();
        remoteRender.requestLayout();
    }

    /**
     * 开始call
     */
    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC端没有分配给一个电话.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // 开始房间连接.
        //logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        appRtcClient.connectToRoom(roomConnectionParameters);

        //创建和音频管理器将照顾音频路由、音频模式,音频设备枚举等。
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
                    //每次调用这个方法将:音频状态(设备的数量和类型)被改变时
                    @Override
                    public void run() {
                        onAudioManagerChangedState();
                    }
                }
        );
        // Store existing audio settings and change audio mode to MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "初始化音频管理器...");
        audioManager.init();
    }

    /**
     * 呼叫连接,远程、本地视频刷新
     * 应该从UI线程调用
     */
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "呼叫被连接: 信号延迟=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "电话连接在封闭的或错误的状态");
            return;
        }
        // Update video view.
        updateVideoView();
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    }

    private void onAudioManagerChangedState() {
        //  disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        activityRunning = false;
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (localRender != null) {
            localRender.release();
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.release();
            remoteRender = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    /**
     * 断开连接并打印错误日志信息
     *
     * @param errorMessage
     */
    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "关键错误: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            disconnect();
                        }
                    }).create().show();
        }
    }

    /**
     * 打印Log，并且吐司
     */
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        logAndToast("创建点对点连接, 信号延迟=" + delta + "ms");
        Log.d(TAG, "创建点对点连接, 信号延迟=" + delta + "ms" );
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
                localRender, remoteRender, signalingParameters);

        if (signalingParameters.initiator) {
            logAndToast("正在创建视频申请");
            Log.d(TAG, "正在创建视频申请");
            // Create offer. Offer SDP will be sent to answering client in PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                logAndToast("正在创建应答");
                Log.d(TAG, "正在创建应答");
                // Create answer. Answer SDP will be sent to offering client in PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onConnectedToRoom(final SignalingParameters params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    /**
     * 接收到远程信息，但不一定能得到视频音频，
     * @param sdp
     */
    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "收到远程SDP non-initilized对等连接");
                    return;
                }
                logAndToast("接收到对方信息 " + sdp.type + ", 信号延迟=" + delta + "ms");
                Log.d(TAG, "接收到远程信息 " + sdp.type + ", 信号延迟=" + delta + "ms");
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    logAndToast("正在创建应答");
                    Log.d(TAG, "正在创建应答");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "收到 non-initialized的对等连接 的ICE候选");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "接收到一个 non-initialized的对等连接  的ICE候选屏蔽.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("对方终止了视频通话");
                Log.d(TAG, "远程终止了视频通话.");
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.

    /**
     * 本地视频正常开启后监听
     * @param sdp
     */
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    logAndToast("正在发送 " + sdp.type + ", 信号延迟=" + delta + "ms");
                    Log.d(TAG, "正在发送 " + sdp.type + ", 信号延迟=" + delta + "ms");
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    /**
     * ICE建立后监听
     */
    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("成功连接到对方, 信号延迟=" + delta + "ms");
                Log.d(TAG, "“交互式连接建立模块”被连接, 信号延迟=" + delta + "ms");
                iceConnected = true;
                callConnected();


                mActivation = true;
                callFragment.setcontactViewText("正在与 "+mTargetNickName+" 进行视频通话");
            }
        });
    }

    /**
     * ICE失去连接
     */
    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("与对方失去连接");
                Log.d(TAG, "“交互式连接建立模块”失去连接");
                iceConnected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && iceConnected) {
                    hudFragment.updateEncoderStatistics(reports);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }
}
