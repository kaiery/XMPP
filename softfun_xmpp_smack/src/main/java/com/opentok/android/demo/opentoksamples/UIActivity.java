package com.opentok.android.demo.opentoksamples;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Stream.StreamVideoType;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.demo.ui.AudioLevelView;
import com.opentok.android.demo.ui.fragments.PublisherControlFragment;
import com.opentok.android.demo.ui.fragments.PublisherStatusFragment;
import com.opentok.android.demo.ui.fragments.SubscriberControlFragment;
import com.opentok.android.demo.ui.fragments.SubscriberQualityFragment;
import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.ToastUtils;

import java.util.ArrayList;


public class UIActivity extends Activity implements Session.SessionListener,
        Session.ArchiveListener,
        Session.StreamPropertiesListener, Publisher.PublisherListener,
        Subscriber.VideoListener, Subscriber.SubscriberListener,
        SubscriberControlFragment.SubscriberCallbacks,
        PublisherControlFragment.PublisherCallbacks {

    private static final int ANIMATION_DURATION = 3000;

    /**
     * 会话
     */
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
     * 视频流数组
     */
    private ArrayList<Stream> mStreams = new ArrayList<Stream>();
    private Handler mHandler = new Handler();
    /**
     * 只订阅音频
     */
    private boolean mSubscriberAudioOnly = false;
    /**
     * 归档标记
     */
    private boolean archiving = false;
    /**
     * 恢复
     */
    private boolean resumeHasRun = false;

    // 视图相关变量
    /**
     * 发布者视图容器
     */
    private RelativeLayout mPublisherViewContainer;
    /**
     * 订阅者视图容器
     */
    private RelativeLayout mSubscriberViewContainer;
    /**
     * 只订阅音频视图
     */
    private RelativeLayout mSubscriberAudioOnlyView;

    // Fragments
    private SubscriberControlFragment mSubscriberFragment;
    private PublisherControlFragment mPublisherFragment;
    private PublisherStatusFragment mPublisherStatusFragment;
    private SubscriberQualityFragment mSubscriberQualityFragment;
    private FragmentTransaction mFragmentTransaction;

    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    private AudioLevelView mAudioLevelView;

    private SubscriberQualityFragment.CongestionLevel congestion = SubscriberQualityFragment.CongestionLevel.Low;

//    private boolean mIsBound = false;
//    private NotificationCompat.Builder mNotifyBuilder;
//    private NotificationManager mNotificationManager;
//    private ServiceConnection mConnection;
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
    private static int mDelayTime = 3000000;
    /**
     * 视频激活状态
     */
    private boolean mActivation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);



        IMService.isVideo = true;
        mUserAccount = IMService.mCurAccount;
        mNickName = IMService.mCurNickName;
        mTargetAccount = getIntent().getStringExtra("mTargetAccount");
        mTargetNickName = getIntent().getStringExtra("mTargetNickName");

        initListener();

        loadInterface();

        if (savedInstanceState == null) {
            mFragmentTransaction = getFragmentManager().beginTransaction();
            initSubscriberFragment();
            initPublisherFragment();
            initPublisherStatusFragment();
            initSubscriberQualityFragment();
            mFragmentTransaction.commitAllowingStateLoss();
        }

        //mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        sessionConnect();

        startTimeTask();
    }

    private void initListener() {
        // 注册对方忙的时候 的动态广播消息
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction(Const.VIDEO_WORKING_BROADCAST_ACTION);
        registerReceiver(dynamicReceiver, filter_dynamic);
        IMService.VIDEO_UI_CREATE = true;
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
                Toast.makeText(UIActivity.this,"对方无应答",Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                if (mSubscriber != null) {
                    onViewClick.onClick(null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 屏幕发生横竖变化
     * @param newConfig 配置
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //删除发布者和订阅者的视图，因为我们想重用它们
        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());

            if (mSubscriberFragment != null) {
                getFragmentManager().beginTransaction().remove(mSubscriberFragment).commit();

                initSubscriberFragment();
                if (mSubscriberQualityFragment != null) {
                    getFragmentManager().beginTransaction().remove(mSubscriberQualityFragment).commit();
                    initSubscriberQualityFragment();
                }
            }
        }
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());

            if (mPublisherFragment != null) {
                getFragmentManager().beginTransaction().remove(mPublisherFragment).commit();

                initPublisherFragment();
            }

            if (mPublisherStatusFragment != null) {
                getFragmentManager().beginTransaction().remove(mPublisherStatusFragment).commit();

                initPublisherStatusFragment();
            }
        }

        loadInterface();
    }

    /**
     * 加载接口
     */
    public void loadInterface() {
        setContentView(R.layout.layout_ui_activity);

        mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);

        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherView);
        mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberView);
        mSubscriberAudioOnlyView = (RelativeLayout) findViewById(R.id.audioOnlyView);

        //Initialize 
        mAudioLevelView = (AudioLevelView) findViewById(R.id.subscribermeter);
        mAudioLevelView.setIcons(BitmapFactory.decodeResource(getResources(),R.drawable.headset));
        // Attach running video views
        if (mPublisher != null) {
            attachPublisherView(mPublisher);
        }

        // show subscriber status
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    attachSubscriberView(mSubscriber);

                    if (mSubscriberAudioOnly) {
                        mSubscriber.getView().setVisibility(View.GONE);
                        setAudioOnlyView(true);
                        congestion = SubscriberQualityFragment.CongestionLevel.High;
                    }
                }
            }
        }, 0);

        loadFragments();
    }

    /**
     * 加载Fragments
     */
    public void loadFragments() {
        // show subscriber status
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    mSubscriberFragment.showSubscriberWidget(true);
                    mSubscriberFragment.initSubscriberUI();

                    if (congestion != SubscriberQualityFragment.CongestionLevel.Low) {
                        mSubscriberQualityFragment.setCongestion(congestion);
                        mSubscriberQualityFragment.showSubscriberWidget(true);
                    }
                }
            }
        }, 0);

        // show publisher status
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPublisher != null) {
                    mPublisherFragment.showPublisherWidget(true);
                    mPublisherFragment.initPublisherUI();

                    if (archiving) {
                        mPublisherStatusFragment.updateArchivingUI(true);
                        setPubViewMargins();
                    }
                }
            }
        }, 0);

    }

    /**
     * 初始化订阅者Fragment
     */
    public void initSubscriberFragment() {
        mSubscriberFragment = new SubscriberControlFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_sub_container, mSubscriberFragment).commit();
    }

    /**
     * 初始化发布者Fragment
     */
    public void initPublisherFragment() {
        mPublisherFragment = new PublisherControlFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_pub_container, mPublisherFragment).commit();
    }

    /**
     * 初始化发布者状态Fragment
     */
    public void initPublisherStatusFragment() {
        mPublisherStatusFragment = new PublisherStatusFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_pub_status_container,mPublisherStatusFragment).commit();
    }

    /**
     * 初始化订阅者质量Fragment
     */
    public void initSubscriberQualityFragment() {
        mSubscriberQualityFragment = new SubscriberQualityFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_sub_quality_container,mSubscriberQualityFragment).commit();
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

//        mNotifyBuilder = new NotificationCompat.Builder(this)
//                .setContentTitle(this.getTitle())
//                .setContentText(getResources().getString(R.string.notification))
//                .setSmallIcon(R.drawable.ic_launcher).setOngoing(true);

//        Intent notificationIntent = new Intent(this, UIActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent intent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);

//        mNotifyBuilder.setContentIntent(intent);
//        if (mConnection == null) {
//            mConnection = new ServiceConnection() {
//                @Override
//                public void onServiceConnected(ComponentName className, IBinder binder) {
//                    ((ClearBinder) binder).service.startService(new Intent(UIActivity.this, ClearNotificationService.class));
//                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                    mNotificationManager.notify(ClearNotificationService.NOTIFICATION_ID, mNotifyBuilder.build());
//                }
//
//                @Override
//                public void onServiceDisconnected(ComponentName className) {
//                    mConnection = null;
//                }
//
//            };
//        }
//
//        if (!mIsBound) {
//            Log.d(LOGTAG, "mISBOUND GOT CALLED");
//            bindService(new Intent(UIActivity.this,
//                            ClearNotificationService.class), mConnection,
//                    Context.BIND_AUTO_CREATE);
//            mIsBound = true;
//            startService(notificationIntent);
//        }

    }

    @Override
    public void onResume() {
        super.onResume();

//        if (mIsBound) {
//            unbindService(mConnection);
//            mIsBound = false;
//        }

        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        } else {
            if (mSession != null) {
                mSession.onResume();
            }
        }

//        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);

        reloadInterface();
    }

    @Override
    public void onStop() {
        super.onStop();

//        if (mIsBound) {
//            unbindService(mConnection);
//            mIsBound = false;
//        }
        if (isFinishing()) {
//            mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
            if (mSession != null) {
                mSession.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(dynamicReceiver);
//        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
//        if (mIsBound) {
//            unbindService(mConnection);
//            mIsBound = false;
//        }

        if (mSession != null) {
            mSession.disconnect();
        }
        IMService.isVideo = false;
        IMService.VIDEO_UI_CREATE = false;
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

    /**
     * 重加载接口
     */
    public void reloadInterface() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    attachSubscriberView(mSubscriber);
                    if (mSubscriberAudioOnly) {
                        mSubscriber.getView().setVisibility(View.GONE);
                        setAudioOnlyView(true);
                        congestion = SubscriberQualityFragment.CongestionLevel.High;
                    }
                }
            }
        }, 500);

        loadFragments();
    }

    /**
     * 连接会话
     */
    private void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(this,
                    //OpenTokConfig.API_KEY,
                    IMService.api_key,
                    //OpenTokConfig.SESSION_ID
                    IMService.session_id
            );
            mSession.setSessionListener(this);
            mSession.setArchiveListener(this);
            mSession.setStreamPropertiesListener(this);
            mSession.connect(
                    //OpenTokConfig.TOKEN
                    IMService.token
            );
        }
    }

    /**
     * 添加发布者到视图
     * @param publisher 发布者
     */
    private void attachPublisherView(Publisher publisher) {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mPublisherViewContainer.addView(publisher.getView(), layoutParams);
        mPublisherViewContainer.setDrawingCacheEnabled(true);
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        publisher.getView().setOnClickListener(onViewClick);
    }

    /**
     * 静音订阅者
     */
    @Override
    public void onMuteSubscriber() {
        if (mSubscriber != null) {
            mSubscriber.setSubscribeToAudio(!mSubscriber.getSubscribeToAudio());
        }
    }

    /**
     * 静音发布者
     */
    @Override
    public void onMutePublisher() {
        if (mPublisher != null) {
            mPublisher.setPublishAudio(!mPublisher.getPublishAudio());
        }
    }

    /**
     * 变更发布者的主次摄像头
     */
    @Override
    public void onSwapCamera() {
        if (mPublisher != null) {
            mPublisher.swapCamera();
        }
    }

    @Override
    public void onEndCall() {
        if (mSession != null) {
            mSession.disconnect();
        }
        //设置我不再视频聊天
        IMService.isVideo = false;
        finish();
    }

    /**
     * 添加订阅者到视图
     * @param subscriber
     */
    private void attachSubscriberView(Subscriber subscriber) {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mSubscriberViewContainer.removeView(mSubscriber.getView());
        mSubscriberViewContainer.addView(subscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        subscriber.getView().setOnClickListener(onViewClick);
    }
    /**
     * 订阅视频流
     * @param stream 视频流
     */
    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber(this, stream);
        mSubscriber.setSubscriberListener(this);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);
        //如果订阅者拿到了视频资源
        if (mSubscriber.getSubscribeToVideo()) {
            //开始进行加载进度条展示
            mLoadingSub.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 取消订阅视频流
     * @param stream 视频流
     */
    private void unsubscriberFromStream(Stream stream) {
        mStreams.remove(stream);
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
            if (!mStreams.isEmpty()) {
                subscribeToStream(mStreams.get(0));
            }
        }
    }

    /**
     * 设置单独音频视图
     * @param audioOnlyEnabled 开启标记
     */
    private void setAudioOnlyView(boolean audioOnlyEnabled) {
        mSubscriberAudioOnly = audioOnlyEnabled;

        if (audioOnlyEnabled) {
            mSubscriber.getView().setVisibility(View.GONE);
            mSubscriberAudioOnlyView.setVisibility(View.VISIBLE);
            mSubscriberAudioOnlyView.setOnClickListener(onViewClick);

            // Audio only text for subscriber
            TextView subStatusText = (TextView) findViewById(R.id.subscriberName);
            subStatusText.setText(R.string.audioOnly);
            AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
            aa.setDuration(ANIMATION_DURATION);
            subStatusText.startAnimation(aa);


            mSubscriber
                    .setAudioLevelListener(new SubscriberKit.AudioLevelListener() {
                        @Override
                        public void onAudioLevelUpdated(
                                SubscriberKit subscriber, float audioLevel) {
                            mAudioLevelView.setMeterValue(audioLevel);
                        }
                    });
        } else {
            if (!mSubscriberAudioOnly) {
                mSubscriber.getView().setVisibility(View.VISIBLE);
                mSubscriberAudioOnlyView.setVisibility(View.GONE);

                mSubscriber.setAudioLevelListener(null);
            }
        }
    }

    private OnClickListener onViewClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean visible = false;

            if (mPublisher != null) {
                // check visibility of bars
                if (!mPublisherFragment.isPubControlWidgetVisible()) {
                    visible = true;
                }
                mPublisherFragment.publisherClick();
                if (archiving) {
                    mPublisherStatusFragment.publisherClick();
                }
                setPubViewMargins();
                if (mSubscriber != null) {
                    mSubscriberFragment.showSubscriberWidget(visible);
                    mSubscriberFragment.initSubscriberUI();
                }
            }
        }
    };

    public Publisher getPublisher() {
        return mPublisher;
    }

    public Subscriber getSubscriber() {
        return mSubscriber;
    }

    public Handler getHandler() {
        return mHandler;
    }


    @Override
    public void onConnected(Session session) {
        //Log.i(LOGTAG, "会话连接成功");
        ToastUtils.showToastSafe("会话连接成功");
        //System.out.println("====================  会话连接成功  =====================");
        //视频发布者
        if (mPublisher == null) {
            mPublisher = new Publisher(this, mUserAccount);
            mPublisher.setPublisherListener(this);
            attachPublisherView(mPublisher);
            mSession.publish(mPublisher);
        }
    }

    @Override
    public void onDisconnected(Session session) {
        //Log.i(LOGTAG, "会话已断开");
        ToastUtils.showToastSafe("会话已断开");
        //System.out.println("====================  会话已断开  =====================");
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getRenderer().getView());
        }

        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getRenderer().getView());
        }

        mPublisher = null;
        mSubscriber = null;
        mStreams.clear();
        mSession = null;
    }

    /**
     * 接收到流媒体
     * @param session   会话
     * @param stream    流媒体
     */
    @Override
    public void onStreamReceived(Session session, Stream stream) {

//        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            mStreams.add(stream);
//            if (mSubscriber == null) {
//                subscribeToStream(stream);
//            }
//        }

        //System.out.println("发布者："+ mUserAccount +"接收到：====================  onStreamReceived:  =====================:"+stream.getName());
        //System.out.println("====================  我需要接收  ===================== "+mTargetAccount);
        //不是自己发布的视频，是指定对方的视频
        ToastUtils.showToastSafe("接收到视频流");
        if (!stream.getName().equals(mUserAccount) && stream.getName().equals(mTargetAccount)) {
            mStreams.add(stream);
            if (mSubscriber == null) {
                subscribeToStream(stream);
                mActivation = true;
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
        ToastUtils.showToastSafe("视频掉线");
        //System.out.println("====================  视频掉线  =====================");
        mStreams.remove(stream);
//        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            if (mSubscriber != null &&
//                    mSubscriber.getStream().getStreamId().equals(stream.getStreamId())  ) {
//                mSubscriberViewContainer.removeView(mSubscriber.getView());
//                mSubscriber = null;
//                findViewById(R.id.avatar).setVisibility(View.GONE);
//                mSubscriberAudioOnly = false;
//                if (!mStreams.isEmpty()) {
//                    subscribeToStream(mStreams.get(0));
//                }
//            }
//        }
        if (mSubscriber != null &&   mSubscriber.getStream().getStreamId().equals(stream.getStreamId())  ) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
            findViewById(R.id.avatar).setVisibility(View.GONE);
            mSubscriberAudioOnly = false;
        }
        close();
    }
    /**
     * 视频流被创建
     * @param publisher 发布者
     * @param stream    视频流
     */
    @Override
    public void onStreamCreated(PublisherKit publisher, Stream stream) {
        ToastUtils.showToastSafe("成功发布了自己的视频");
        //System.out.println("发布者："+ mUserAccount +"成功发布了自己的视频，名称是："+stream.getName());
//        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            mStreams.add(stream);
//            if (mSubscriber == null) {
//                subscribeToStream(stream);
//            }
//        }
        mPublisherFragment.showPublisherWidget(true);
        mPublisherFragment.initPublisherUI();
        mPublisherStatusFragment.showPubStatusWidget(true);
        mPublisherStatusFragment.initPubStatusUI();
    }
    /**
     * 视频流被销毁
     * @param publisher 发布者
     * @param stream    视频流
     */
    @Override
    public void onStreamDestroyed(PublisherKit publisher, Stream stream) {

//        if (OpenTokConfig.SUBSCRIBE_TO_SELF && mSubscriber != null) {
//            unsubscriberFromStream(stream);
//        }
        if(mSubscriber != null){
            unsubscriberFromStream(stream);
        }
        ToastUtils.showToastSafe("我的视频被销毁");
        //System.out.println("====================  我的视频被销毁了  =====================："+stream);
    }
    /**
     * 发布视频出错
     * @param exception 错误
     */
    @Override
    public void onError(Session session, OpentokError exception) {
        ToastUtils.showToastSafe("视频发布出错了");
        //System.out.println("====================  视频发布出错了  =====================:"+exception.getMessage());
        Toast.makeText(this,"会话异常" , Toast.LENGTH_LONG).show();

        close();
    }

    /**
     * 设置发布者视图边距
     */
    public void setPubViewMargins() {
        LayoutParams pubLayoutParams = (LayoutParams) mPublisherViewContainer.getLayoutParams();
        int bottomMargin = 0;
        boolean controlBarVisible = mPublisherFragment
                .isPubControlWidgetVisible();
        boolean statusBarVisible = mPublisherStatusFragment
                .isPubStatusWidgetVisible();
        RelativeLayout pubControlContainer = mPublisherFragment.getPublisherContainer();
        RelativeLayout pubStatusContainer = mPublisherStatusFragment.getPubStatusContainer();

        if (pubControlContainer != null && pubStatusContainer != null) {

            LayoutParams pubControlLayoutParams = (LayoutParams) pubControlContainer.getLayoutParams();
            LayoutParams pubStatusLayoutParams = (LayoutParams) pubStatusContainer.getLayoutParams();

            // setting margins for publisher view on portrait orientation
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (statusBarVisible && archiving) {
                    // height of publisher control bar + height of publisher status
                    // bar + 20 px
                    bottomMargin = pubControlLayoutParams.height
                            + pubStatusLayoutParams.height + dpToPx(20);
                } else {
                    if (controlBarVisible) {
                        // height of publisher control bar + 20 px
                        bottomMargin = pubControlLayoutParams.height + dpToPx(20);
                    } else {
                        bottomMargin = dpToPx(20);
                    }
                }
            }

            // setting margins for publisher view on landscape orientation
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (statusBarVisible && archiving) {
                    bottomMargin = pubStatusLayoutParams.height + dpToPx(20);
                } else {
                    bottomMargin = dpToPx(20);
                }
            }

            pubLayoutParams.bottomMargin = bottomMargin;
            pubLayoutParams.leftMargin = dpToPx(20);

            mPublisherViewContainer.setLayoutParams(pubLayoutParams);
        }
        if (mSubscriber != null) {
            if (mSubscriberAudioOnly) {
                LayoutParams subLayoutParams = (LayoutParams) mSubscriberAudioOnlyView
                        .getLayoutParams();
                int subBottomMargin = 0;
                subBottomMargin = pubLayoutParams.bottomMargin;
                subLayoutParams.bottomMargin = subBottomMargin;
                mSubscriberAudioOnlyView.setLayoutParams(subLayoutParams);
            }

            setSubQualityMargins();
        }
    }

    /**
     * 设置质量调节边距
     */
    public void setSubQualityMargins() {
        RelativeLayout subQualityContainer = mSubscriberQualityFragment.getSubQualityContainer();
        RelativeLayout pubControlContainer = mPublisherFragment.getPublisherContainer();
        RelativeLayout pubStatusContainer = mPublisherStatusFragment.getPubStatusContainer();

        if (subQualityContainer != null && pubControlContainer != null && pubStatusContainer != null) {
            LayoutParams subQualityLayoutParams = (LayoutParams) subQualityContainer.getLayoutParams();
            boolean pubControlBarVisible = mPublisherFragment
                    .isPubControlWidgetVisible();
            boolean pubStatusBarVisible = mPublisherStatusFragment
                    .isPubStatusWidgetVisible();
            LayoutParams pubControlLayoutParams = (LayoutParams) pubControlContainer.getLayoutParams();
            LayoutParams pubStatusLayoutParams = (LayoutParams) pubStatusContainer.getLayoutParams();
            LayoutParams audioMeterLayoutParams = (LayoutParams) mAudioLevelView.getLayoutParams();

            int bottomMargin = 0;

            // control pub fragment
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (pubControlBarVisible) {
                    bottomMargin = pubControlLayoutParams.height + dpToPx(10);
                }
                if (pubStatusBarVisible && archiving) {
                    bottomMargin = pubStatusLayoutParams.height + dpToPx(10);
                }
                if (bottomMargin == 0) {
                    bottomMargin = dpToPx(10);
                }
                subQualityLayoutParams.rightMargin = dpToPx(10);
            }

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!pubControlBarVisible) {
                    subQualityLayoutParams.rightMargin = dpToPx(10);
                    bottomMargin = dpToPx(10);
                    audioMeterLayoutParams.rightMargin = 0;
                    mAudioLevelView.setLayoutParams(audioMeterLayoutParams);

                } else {
                    subQualityLayoutParams.rightMargin = pubControlLayoutParams.width;
                    bottomMargin = dpToPx(10);
                    audioMeterLayoutParams.rightMargin = pubControlLayoutParams.width;
                }
                if (pubStatusBarVisible && archiving) {
                    bottomMargin = pubStatusLayoutParams.height + dpToPx(10);
                }
                mAudioLevelView.setLayoutParams(audioMeterLayoutParams);
            }

            subQualityLayoutParams.bottomMargin = bottomMargin;

            mSubscriberQualityFragment.getSubQualityContainer().setLayoutParams(
                    subQualityLayoutParams);
        }

    }

    /**
     * 发布视频出错
     * @param publisher 发布者
     * @param exception 错误
     */
    @Override
    public void onError(PublisherKit publisher, OpentokError exception) {
        //Log.i(LOGTAG, "Publisher exception: " + exception.getMessage());
        //System.out.println("====================  视频发布出错了：  =====================  "+ mUserAccount);
        ToastUtils.showToastSafe("视频发布出错了1");

        close();
    }

    /**
     * 订阅者连接成功
     * @param subscriber 订阅者
     */
    @Override
    public void onConnected(SubscriberKit subscriber) {
        //System.out.println("====================  订阅者连接成功  =====================");
        ToastUtils.showToastSafe("订阅者连接成功");
        mLoadingSub.setVisibility(View.GONE);
        mSubscriberFragment.showSubscriberWidget(true);
        mSubscriberFragment.initSubscriberUI();
    }

    /**
     * 订阅者连接失败
     * @param subscriber 订阅者
     */
    @Override
    public void onDisconnected(SubscriberKit subscriber) {
        //System.out.println("Subscriber disconnected.");
        ToastUtils.showToastSafe("对方停止了视频通话");
        close();
    }

    /**
     * 视频数据被接收到
     * @param subscriber    订阅者
     */
    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        //System.out.println("First frame received");
        // 隐藏订阅者加载的进度条
        mLoadingSub.setVisibility(View.GONE);
        // 添加订阅者到视图
        attachSubscriberView(mSubscriber);
    }

    /**
     * 订阅者发生错误
     * @param subscriber 订阅者
     * @param exception 异常
     */
    @Override
    public void onError(SubscriberKit subscriber, OpentokError exception) {
        //System.out.println("订阅者发生异常：" + exception.getMessage());
        ToastUtils.showToastSafe("订阅视频异常");
        close();
    }

    /**
     * 订阅者的视频被关闭\暂停了
     * @param subscriber    订阅者
     * @param reason    理由
     */
    @Override
    public void onVideoDisabled(SubscriberKit subscriber, String reason) {
        //Log.i(LOGTAG, "Video disabled:" + reason);
        //System.out.println("====================  onVideoDisabled  对方视频中断了=====================："+subscriber.getStream().getName()+"  理由："+reason);
        if (mSubscriber == subscriber) {
            setAudioOnlyView(true);
        }

        if (reason.equals("quality")) {
            mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.High);
            congestion = SubscriberQualityFragment.CongestionLevel.High;
            setSubQualityMargins();
            mSubscriberQualityFragment.showSubscriberWidget(true);
        }
    }

    /**
     *  订阅者的视频开启了
     * @param subscriber    订阅者
     * @param reason    理由
     */
    @Override
    public void onVideoEnabled(SubscriberKit subscriber, String reason) {
        //Log.i(LOGTAG, "Video enabled:" + reason);
        //System.out.println("====================  onVideoEnabled  对方视频开启了=====================："+subscriber.getStream().getName()+"  理由："+reason);
        if (mSubscriber == subscriber) {
            setAudioOnlyView(false);
        }
        if (reason.equals("quality")) {
            mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.Low);
            congestion = SubscriberQualityFragment.CongestionLevel.Low;
            mSubscriberQualityFragment.showSubscriberWidget(false);
        }
    }

    /**
     * 流媒体音频发生变化
     * @param session 会话
     * @param stream    流媒体
     * @param audioEnabled  音频开启标记
     */
    @Override
    public void onStreamHasAudioChanged(Session session, Stream stream, boolean audioEnabled) {
        //Log.i(LOGTAG, "Stream audio changed");
        //System.out.println("====================  流媒体音频发生变化  =====================:"+stream.getName()+"  "+audioEnabled);
        ToastUtils.showToastSafe("流媒体音频发生变化");
    }

    /**
     * 流媒体视频发生变化
     * @param session   会话
     * @param stream    流媒体
     * @param videoEnabled  视频开启标记
     */
    @Override
    public void onStreamHasVideoChanged(Session session, Stream stream,
                                        boolean videoEnabled) {
        //Log.i(LOGTAG, "Stream video changed");
        //System.out.println("====================  流媒体视频发生变化  =====================:"+stream.getName()+"  "+videoEnabled);
        ToastUtils.showToastSafe("流媒体视频发生变化");
    }

    /**
     * 流媒体视频尺寸发生变化
     * @param session   会话
     * @param stream    流媒体
     * @param width 宽度
     * @param height    高度
     */
    @Override
    public void onStreamVideoDimensionsChanged(Session session, Stream stream,
                                               int width, int height) {
        //Log.i(LOGTAG, "Stream video dimensions changed");
        //System.out.println("====================  流媒体视频尺寸发生变化  =====================:"+stream.getName()+"  "+width+"  "+height);
        ToastUtils.showToastSafe("流媒体视频尺寸发生变化");
    }

    /**
     * 归档开始
     * @param session   会话
     * @param id    标识
     * @param name  名称
     */
    @Override
    public void onArchiveStarted(Session session, String id, String name) {
        //System.out.println("Archiving starts");
        mPublisherFragment.showPublisherWidget(false);

        archiving = true;
        mPublisherStatusFragment.updateArchivingUI(true);
        mPublisherFragment.showPublisherWidget(true);
        mPublisherFragment.initPublisherUI();
        setPubViewMargins();

        if (mSubscriber != null) {
            mSubscriberFragment.showSubscriberWidget(true);
        }
    }

    /**
     * 归档结束
     * @param session   会话
     * @param id    标识
     */
    @Override
    public void onArchiveStopped(Session session, String id) {
        //System.out.println("Archiving stops");
        archiving = false;

        mPublisherStatusFragment.updateArchivingUI(false);
        setPubViewMargins();

        if (mSubscriber != null) {
            setSubQualityMargins();
        }
    }

    /**
     * Converts dp to real pixels, according to the screen density.
     *
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    public int dpToPx(int dp) {
        double screenDensity = getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    /**
     * 订阅者的视频被关闭后的警告
     * @param subscriber
     */
    @Override
    public void onVideoDisableWarning(SubscriberKit subscriber) {
        ToastUtils.showToastSafe("视频可能很快就会被禁用由于网络质量下降");
        //Log.i(LOGTAG, "视频可能很快就会被禁用由于网络质量下降。添加用户界面处理。");
        //System.out.println("====================  onVideoDisableWarning  视频可能很快就会被禁用由于网络质量下降=====================："+subscriber.getStream().getName());
        mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.Mid);
        congestion = SubscriberQualityFragment.CongestionLevel.Mid;
        setSubQualityMargins();
        mSubscriberQualityFragment.showSubscriberWidget(true);
    }
    /**
     * 订阅者的视频即将被恢复
     * @param subscriber    订阅者
     */
    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
        ToastUtils.showToastSafe("视频可能不再被禁用流质量改善");
        //Log.i(LOGTAG, "视频可能不再被禁用流质量改善。添加用户界面处理。");
        //System.out.println("====================  onVideoDisableWarningLifted  视频可能不再被禁用流质量改善=====================："+subscriber.getStream().getName());
        mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.Low);
        congestion = SubscriberQualityFragment.CongestionLevel.Low;
        mSubscriberQualityFragment.showSubscriberWidget(false);
    }

    /**
     * 流媒体视频类型变化
     * @param session
     * @param stream
     * @param videoType
     */
    @Override
    public void onStreamVideoTypeChanged(Session session, Stream stream, StreamVideoType videoType) {
        ToastUtils.showToastSafe("流媒体视频类型变化1");
        //Log.i(LOGTAG, "Stream video type changed");
        //System.out.println("====================  流媒体视频类型变化  =====================:"+stream.getName()+"  "+videoType);
    }



    /**
     * 主动关闭视频聊天
     */
    private void close(){
        if (mSession != null) {
            mSession.disconnect();
        }
        if(runnable!=null){
            handler.removeCallbacks(runnable);
        }
        //System.out.println("====================  IMService.isVideo = false  4=====================");
        //设置我不再视频聊天
        IMService.isVideo = false;
        //关闭窗口
        finish();
    }

}
