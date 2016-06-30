package com.opentok.android.demo.opentoksamples;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.demo.config.OpenTokConfig;
import com.opentok.android.demo.services.ClearNotificationService;
import com.softfun_xmpp.R;

import java.util.ArrayList;



public class HelloWorldActivity extends AppCompatActivity implements
        Session.SessionListener, Publisher.PublisherListener,
        Subscriber.VideoListener {

    private static final String LOGTAG = "demo-hello-world";
    private Session mSession;
    /**
     * 发布者
     */
    private Publisher mPublisher;
    /**
     * 订阅者
     */
    private Subscriber mSubscriber;
    /**
     * 流媒体数组
     */
    private ArrayList<Stream> mStreams;
    private Handler mHandler = new Handler();

    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;

    // 动感旋转进度加载器
    private ProgressBar mLoadingSub;

    /**
     * 恢复
     */
    private boolean resumeHasRun = false;

    private boolean mIsBound = false;
    private NotificationCompat.Builder mNotifyBuilder;
    private NotificationManager mNotificationManager;
    private ServiceConnection mConnection;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOGTAG, "ONCREATE");
        super.onCreate(savedInstanceState);

        username = getIntent().getStringExtra("username");
        //System.out.println("====================  username  =====================:"+username);
        setContentView(R.layout.activity_simple_video);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);
        mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mStreams = new ArrayList<Stream>();

        sessionConnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSession != null) {
            mSession.onPause();
            if (mSubscriber != null) {
                mSubscriberViewContainer.removeView(mSubscriber.getView());
            }
        }
        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(this.getTitle())
                .setContentText(getResources().getString(R.string.notification))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher).setOngoing(true);
        Intent notificationIntent = new Intent(this, HelloWorldActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mNotifyBuilder.setContentIntent(intent);
        if (mConnection == null) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder binder) {
                    ((ClearNotificationService.ClearBinder) binder).service.startService(new Intent(HelloWorldActivity.this, ClearNotificationService.class));
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotificationManager.notify(ClearNotificationService.NOTIFICATION_ID, mNotifyBuilder.build());
                }
                @Override
                public void onServiceDisconnected(ComponentName className) {
                    mConnection = null;
                }
            };
        }
        if (!mIsBound) {
            bindService(new Intent(HelloWorldActivity.this,
                            ClearNotificationService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
            startService(notificationIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        } else {
            if (mSession != null) {
                mSession.onResume();
            }
        }
        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
        reloadInterface();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        if (isFinishing()) {
            mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
            if (mSession != null) {
                mSession.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        if (mSession != null) {
            mSession.disconnect();
        }
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mSession != null) {
            mSession.disconnect();
        }
        super.onBackPressed();
    }

    public void reloadInterface() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    attachSubscriberView(mSubscriber);
                    //System.out.println("====================  重新加载订阅视频到视图  =====================");
                }
            }
        }, 500);
    }

    /**
     * 会话连接
     */
    private void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(HelloWorldActivity.this, OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(OpenTokConfig.TOKEN);
            //System.out.println("====================  session会话连接  =====================");
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOGTAG, "会话连接成功");
        //System.out.println("====================  会话连接成功  =====================");
        //视频发布者
        if (mPublisher == null) {
            mPublisher = new Publisher(HelloWorldActivity.this, username);
            mPublisher.setPublisherListener(this);
            attachPublisherView(mPublisher);
            mSession.publish(mPublisher);
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "会话已断开");
        //System.out.println("====================  会话已断开  =====================");
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
        }
        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
        }
        mPublisher = null;
        mSubscriber = null;
        mStreams.clear();
        mSession = null;
    }

    /**
     * 订阅视频流
     * @param stream 视频流
     */
    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber(HelloWorldActivity.this, stream);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);
        //如果订阅者拿到了视频资源
        if (mSubscriber.getSubscribeToVideo()) {
            //开始进行加载进度条展示
            mLoadingSub.setVisibility(View.VISIBLE);
        }
        //System.out.println("====================  订阅视频流  =====================："+stream.getName());
    }

    /**
     * 取消订阅视频流
     * @param stream 视频流
     */
    private void unsubscribeFromStream(Stream stream) {
        mStreams.remove(stream);
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
            if (!mStreams.isEmpty()) {
                subscribeToStream(mStreams.get(0));
            }
        }
        //System.out.println("====================  取消订阅视频流  =====================："+stream.getName());
    }

    /**
     * 添加订阅者到视图
     * @param subscriber 订阅者
     */
    private void attachSubscriberView(Subscriber subscriber) {
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        mSubscriberViewContainer.removeView(mSubscriber.getView());
        mSubscriberViewContainer.addView(mSubscriber.getView(), layoutParams);
        //订阅者设置样式：填充显示
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,  BaseVideoRenderer.STYLE_VIDEO_FILL);
        //System.out.println("====================  添加订阅者到视图  =====================");
    }

    /**
     * 添加发布者到视图
     * @param publisher
     */
    private void attachPublisherView(Publisher publisher) {
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(320, 240);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        layoutParams.bottomMargin = dpToPx(8);
        layoutParams.rightMargin = dpToPx(8);
        mPublisherViewContainer.addView(mPublisher.getView(), layoutParams);
        //System.out.println("====================  添加发布者到视图  =====================");
    }

    /**
     * 连接失败
     * @param session 会话
     * @param exception 异常
     */
    @Override
    public void onError(Session session, OpentokError exception) {
        Log.i(LOGTAG, "Session exception: " + exception.getMessage());
        ////System.out.println("====================  onError  =====================");
    }

    /**
     * 接收到流媒体
     * @param session   会话
     * @param stream    流媒体
     */
    @Override
    public void onStreamReceived(Session session, Stream stream) {
//        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            System.out.println("发布者："+username+"接收到：====================  onStreamReceived:  =====================:"+stream.getName());
//            mStreams.add(stream);
//            if (mSubscriber == null) {
//                subscribeToStream(stream);
//            }
//        }
        if (!stream.getName().equals(username)) {
            //System.out.println("发布者："+username+"接收到：====================  onStreamReceived:  =====================:"+stream.getName());
            mStreams.add(stream);
            if (mSubscriber == null) {
                subscribeToStream(stream);
            }
        }
    }

    /**
     * 视频掉线
     * @param session   会话
     * @param stream    视频流
     */
    @Override
    public void onStreamDropped(Session session, Stream stream) {
//        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            if (mSubscriber != null) {
//                unsubscribeFromStream(stream);
//            }
//        }
        if (mSubscriber != null) {
            unsubscribeFromStream(stream);
        }
        //System.out.println("====================  对方的视频掉线了  =====================："+stream.getName());
    }

    /**
     * 视频流被创建
     * @param publisher 发布者
     * @param stream    视频流
     */
    @Override
    public void onStreamCreated(PublisherKit publisher, Stream stream) {
        //System.out.println("发布者："+username+"成功发布了自己的视频，名称是："+stream.getName());
//        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            //订阅自己发布的视频
//            mStreams.add(stream);
//            if (mSubscriber == null) {
//                subscribeToStream(stream);
//            }
//        }else{
//            //订阅别人的视频
//        }
    }

    /**
     * 视频流被销毁
     * @param publisher 发布者
     * @param stream    视频流
     */
    @Override
    public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
//        if ((OpenTokConfig.SUBSCRIBE_TO_SELF && mSubscriber != null)) {
//            unsubscribeFromStream(stream);
//        }
        if(mSubscriber != null){
            unsubscribeFromStream(stream);
        }
        //System.out.println("====================  我的视频被销毁了  =====================："+stream);
    }

    /**
     * 发布视频出错
     * @param publisher 发布者
     * @param exception 错误
     */
    @Override
    public void onError(PublisherKit publisher, OpentokError exception) {
        Log.i(LOGTAG, "Publisher exception: " + exception.getMessage());
        //System.out.println("====================  视频发布出错了：  =====================  "+username);
    }

    /**
     * 视频数据被接收到
     * @param subscriber    订阅者
     */
    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        Log.i(LOGTAG, "First frame received");
        // 隐藏订阅者加载的进度条
        mLoadingSub.setVisibility(View.GONE);
        // 添加订阅者到视图
        attachSubscriberView(mSubscriber);
    }

    /**
     * dp转化为真正的像素,根据屏幕密度
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    /**
     * 订阅者的视频被关闭\暂停了
     * @param subscriber    订阅者
     * @param reason    理由
     */
    @Override
    public void onVideoDisabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG,"Video disabled:" + reason);
        //System.out.println("====================  onVideoDisabled  对方视频中断了=====================："+subscriber.getStream().getName()+"  理由："+reason);
    }

    /**
     *  订阅者的视频开启了
     * @param subscriber    订阅者
     * @param reason    理由
     */
    @Override
    public void onVideoEnabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG, "Video enabled:" + reason);
        //System.out.println("====================  onVideoEnabled  对方视频开启了=====================："+subscriber.getStream().getName()+"  理由："+reason);
    }

    /**
     * 订阅者的视频被关闭后的警告
     * @param subscriber    订阅者
     */
    @Override
    public void onVideoDisableWarning(SubscriberKit subscriber) {
        Log.i(LOGTAG, "视频可能很快就会被禁用由于网络质量下降。添加用户界面处理。");
        //System.out.println("====================  onVideoDisableWarning  视频可能很快就会被禁用由于网络质量下降=====================："+subscriber.getStream().getName());
    }

    /**
     * 订阅者的视频即将被恢复
     * @param subscriber    订阅者
     */
    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
        Log.i(LOGTAG, "视频可能不再被禁用流质量改善。添加用户界面处理。");
        //System.out.println("====================  onVideoDisableWarningLifted  视频可能不再被禁用流质量改善=====================："+subscriber.getStream().getName());
    }

}
