package com.softfun_xmpp.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalSoundPool;
import com.softfun_xmpp.bean.ImagePickBean;
import com.softfun_xmpp.components.ImageChatBubbleEx;
import com.softfun_xmpp.components.ImageViewPage;
import com.softfun_xmpp.components.RefreshListView;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.dbhelper.SmsDbHelper;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.notification.NotificationUtilEx;
import com.softfun_xmpp.provider.SmsProvider;
import com.softfun_xmpp.recorder.AudioRecoderButton;
import com.softfun_xmpp.recorder.MediaManager;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.BitmapUtil;
import com.softfun_xmpp.utils.CircleImageDrawable;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;
import com.tb.emoji.Emoji;
import com.tb.emoji.EmojiUtil;
import com.tb.emoji.FaceFragment;

import org.appspot.apprtc.CallActivity;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.softfun_xmpp.activity.MainActivity.CONNECTION_REQUEST;
import static com.softfun_xmpp.activity.MainActivity.commandLineRun;
import static com.softfun_xmpp.activity.MainActivity.keyprefAecDump;
import static com.softfun_xmpp.activity.MainActivity.keyprefAudioBitrateType;
import static com.softfun_xmpp.activity.MainActivity.keyprefAudioBitrateValue;
import static com.softfun_xmpp.activity.MainActivity.keyprefAudioCodec;
import static com.softfun_xmpp.activity.MainActivity.keyprefCamera2;
import static com.softfun_xmpp.activity.MainActivity.keyprefCaptureQualitySlider;
import static com.softfun_xmpp.activity.MainActivity.keyprefCaptureToTexture;
import static com.softfun_xmpp.activity.MainActivity.keyprefDisableBuiltInAec;
import static com.softfun_xmpp.activity.MainActivity.keyprefDisableBuiltInAgc;
import static com.softfun_xmpp.activity.MainActivity.keyprefDisableBuiltInNs;
import static com.softfun_xmpp.activity.MainActivity.keyprefEnableLevelControl;
import static com.softfun_xmpp.activity.MainActivity.keyprefFps;
import static com.softfun_xmpp.activity.MainActivity.keyprefHwCodecAcceleration;
import static com.softfun_xmpp.activity.MainActivity.keyprefNoAudioProcessingPipeline;
import static com.softfun_xmpp.activity.MainActivity.keyprefOpenSLES;
import static com.softfun_xmpp.activity.MainActivity.keyprefResolution;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoBitrateType;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoBitrateValue;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoCallEnabled;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoCodec;
import static com.softfun_xmpp.activity.MainActivity.sharedPref;
import static com.softfun_xmpp.utils.ToolsUtil.dip2px;
import static com.softfun_xmpp.utils.ToolsUtil.hideSoftInput;
import static com.softfun_xmpp.utils.ToolsUtil.isKeyBoardShow;
import static com.softfun_xmpp.utils.ToolsUtil.roundDouble;
import static com.softfun_xmpp.utils.ToolsUtil.showKeyBoard;
import static com.tb.emoji.EmojiUtil.getFaceText;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, RefreshListView.OnRefreshDataListener, FaceFragment.OnEmojiClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String F_ACCOUNT = "account";
    public static final String F_NICKNAME = "nickname";
    public static final String F_AVATARURL = "avatarurl";
    private String mTargetAccount;
    private String mTargetNickName;
    private String mTargetAvatarUrl;


    private Toolbar mToolbar;
    private RefreshListView mLv;
    private LinearLayout mLlPrivateChat;
    private ImageButton mIbPrivateChatKey;
    private AudioRecoderButton mAudioRecoderButton;
    private RelativeLayout mRlPrivateChat;
    private EditText mEtPrivateChatText;
    private ImageView mIvPrivateChatFace;
    private Button mBtPivateChatSend;
    private ImageButton mIbPrivateChatAdd;
    private LinearLayout mLlPrivateChatExt;
    private LinearLayout mLlMoreExt;
    private LinearLayout mBtPrivateChatVideo;
    private LinearLayout mBtPrivateChatImage;
    private LinearLayout mBtPrivateChatcallphone;
    private LinearLayout mBtPrivateChatcallsms;
    //表情变量
    private FrameLayout Container;
    private boolean isShowFace;//表情栏显示
    private MyCursorAdapter mAdapter;//适配器
    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;
    /**
     * 每页多少条聊天记录
     */
    private int size;
    /**
     *
     */
    private int index;
    /**
     * 播放录音气泡的最大、最小宽度
     */
    private int mMinItemWidth;
    private int mMaxItemWidth;
    /**
     * 播放录音的组件
     */
    private View mAnimView = null;
    private View lastAnimView = null;
    private int lastAnimViewBackgroundResouceId = 0;


    /**
     * 上传图片的进度
     */
    private MyCropImage_ProgressListener progressListener;
    /**
     * 上传图片的文件名
     */
    private String mImageName;
    /**
     * 已经选择的图片
     */
    private ArrayList<String> mSelectPath;
    private static final int REQUEST_IMAGE = 1;
    /**
     * 获取返回的图片列表
     */
    private List<String> paths;
    private List<ImagePickBean> mNameList;


    private LoaderManager manager = null;
    private boolean isLoadHistoryMsg = false;
    private boolean isShowMore;
    private String mUserphone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        //释放广播接收者
        unregisterReceiver(dynamicReceiver);

        //卸载消息管理内容观察者
        unRegisterContentObserver();
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        //晴空 当前聊天对象
        IMService.chatObject = "";
        super.onDestroy();
    }

    private void init() {
        //注册消息管理的内容观察者
        registerContentObserver();
        //绑定服务
        Intent service = new Intent(ChatActivity.this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
        mTargetAccount = getIntent().getStringExtra(ChatActivity.F_ACCOUNT);
        mTargetNickName = getIntent().getStringExtra(ChatActivity.F_NICKNAME);
        mTargetAvatarUrl = getIntent().getStringExtra(ChatActivity.F_AVATARURL);
        //设置 当前聊天对象
        IMService.chatObject = mTargetAccount;


        NotificationUtilEx.getInstance().deleteNotification(mTargetAccount);
    }


    private void initView() {
        //// TODO: 2016-03-10 表情布局
        Container = (FrameLayout) findViewById(R.id.Container);
        FaceFragment faceFragment = FaceFragment.Instance();
        getSupportFragmentManager().beginTransaction().add(R.id.Container, faceFragment).commit();


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mLv = (RefreshListView) findViewById(R.id.lv);
        mLlPrivateChat = (LinearLayout) findViewById(R.id.ll_private_chat);
        mIbPrivateChatKey = (ImageButton) findViewById(R.id.ib_private_chat_key);
        mAudioRecoderButton = (AudioRecoderButton) findViewById(R.id.bt_audio_recoder);
        mRlPrivateChat = (RelativeLayout) findViewById(R.id.rl_private_chat);
        mEtPrivateChatText = (EditText) findViewById(R.id.et_private_chat_text);
        mIvPrivateChatFace = (ImageView) findViewById(R.id.iv_private_chat_face);
        mBtPivateChatSend = (Button) findViewById(R.id.bt_pivate_chat_send);
        mIbPrivateChatAdd = (ImageButton) findViewById(R.id.ib_private_chat_add);
        mLlPrivateChatExt = (LinearLayout) findViewById(R.id.ll_private_chat_ext);
        mLlMoreExt = (LinearLayout) findViewById(R.id.ll_more_ext);
        mBtPrivateChatVideo = (LinearLayout) findViewById(R.id.bt_private_chat_video);
        mBtPrivateChatImage = (LinearLayout) findViewById(R.id.bt_private_chat_image);

        mBtPrivateChatcallphone = (LinearLayout) findViewById(R.id.bt_private_chat_callphone);
        mBtPrivateChatcallsms = (LinearLayout) findViewById(R.id.bt_private_chat_callsms);

        mLv.setRefreshDataListener(this);
        mLv.setHeadHintText_pull("下拉加载历史记录");
        mLv.setHeadHintText_release("松开加载历史记录");
        mLv.setHeadHintText_refreshing("正在加载历史记录");
        mLv.setHeadHintText_refreshed("历史记录加载完成");

    }

    private void initData() {
        //给页面设置工具栏
        //标题写在setSupportActionBar前面
        mToolbar.setTitle(mTargetNickName);
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAudioRecoderButton.setAudioFinishRecorderListener(new AudioRecoderButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(float seconds, final String filePath) {
                //获取录音文件长度
                long recordlen = new File(filePath).length();
                sentMsg("语音消息", Const.MSGFLAG_RECORD, filePath, recordlen, roundDouble(seconds, 2, BigDecimal.ROUND_DOWN));
                //播放一个很短暂的提示音
                GlobalSoundPool.getInstance().play(R.raw.v_xiu);
            }
        });

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        mMaxItemWidth = (int) (outMetrics.widthPixels * 0.7f);
        mMinItemWidth = (int) (outMetrics.widthPixels * 0.15f);

        //先给mAdapter构造方法内一个空的cursor
        mAdapter = new MyCursorAdapter(this, null, true);
        mLv.setAdapter(mAdapter);
        //初始化加载器管理器
        manager = getSupportLoaderManager();
        manager.initLoader(0, null, this);


        mUserphone = AsmackUtils.getVcardInfo(IMService.conn, mTargetAccount, Const.USERPHONE);
    }

    /**
     * 下拉刷新
     */
    @Override
    public void RefreshData() {
        //此页面中，下拉就是加载历史记录
        loadHistoryMsg();
    }

    /**
     * 上拉加载更多
     */
    @Override
    public void LoadMore() {

    }

    /**
     * 加载历史记录
     */
    private void loadHistoryMsg() {
        isLoadHistoryMsg = true;
        manager.restartLoader(0, null, this);
    }

    //// TODO: 2016-03-10 表情布局
    @Override
    public void onEmojiDelete() {
        String text = mEtPrivateChatText.getText().toString();
        if (text.isEmpty()) {
            return;
        }
        if ("]".equals(text.substring(text.length() - 1, text.length()))) {
            int index = text.lastIndexOf("[");
            if (index == -1) {
                int action = KeyEvent.ACTION_DOWN;
                int code = KeyEvent.KEYCODE_DEL;
                KeyEvent event = new KeyEvent(action, code);
                mEtPrivateChatText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
                return;
            }
            mEtPrivateChatText.getText().delete(index, text.length());
            return;
        }
        int action = KeyEvent.ACTION_DOWN;
        int code = KeyEvent.KEYCODE_DEL;
        KeyEvent event = new KeyEvent(action, code);
        mEtPrivateChatText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
    }

    @Override
    public void onEmojiClick(Emoji emoji) {
        if (emoji != null) {
            int index = mEtPrivateChatText.getSelectionStart();
            Editable editable = mEtPrivateChatText.getEditableText();
            Bitmap bitmap = EmojiUtil.decodeSampledBitmapFromResource(getResources(), emoji.getImageUri(), dip2px(22), dip2px(22));
            //  根据Bitmap对象创建ImageSpan对象
            ImageSpan imageSpan = new ImageSpan(this, bitmap);
            //  创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
            SpannableString spannableString = new SpannableString(emoji.getContent());
            //  用ImageSpan对象替换face
            spannableString.setSpan(imageSpan, 0, emoji.getContent().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (index < 0) {
                editable.append(spannableString);
            } else {
                editable.insert(index, spannableString);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int count;
        if (mAdapter.getCursor() != null) {
            count = mAdapter.getCount();
        } else {
            count = 0;
        }
        count = count + 20;
        String[] sqlWhereArgs = new String[]{IMService.mCurAccount, mTargetAccount, mTargetAccount, IMService.mCurAccount, Message.Type.chat.name(), count + "", "0"};
        return new CursorLoader(ChatActivity.this, SmsProvider.URI_SMS, null, null, sqlWhereArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.getCount() > 0) {

                if (isLoadHistoryMsg) {
                    int currentPosition = mAdapter.getCount();
                    mAdapter.swapCursor(data);
                    isLoadHistoryMsg = false;
                    mLv.refreshFinish();
                    mLv.setSelection(mAdapter.getCount() - currentPosition);
                } else {
                    mAdapter.swapCursor(data);
                    isLoadHistoryMsg = false;
                    mLv.setSelection(mAdapter.getCount() - 1);
                }

            } else {
                mAdapter.swapCursor(null);
                if (isLoadHistoryMsg) {
                    mLv.refreshFinish();
                }
                isLoadHistoryMsg = false;
            }
        } else {
            mAdapter.swapCursor(null);
            if (isLoadHistoryMsg) {
                mLv.refreshFinish();
            }
            isLoadHistoryMsg = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null)
            mAdapter.swapCursor(null);
    }


    private class MyCursorAdapter extends CursorAdapter {
        public static final int SEND = 0;
        public static final int RECEIVE = 1;

        public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public int getItemViewType(int position) {
            mCursor.moveToPosition(position);
            String from_account = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FROM_ACCOUNT));
            //如果当前的帐号 ！= 消息的创建者，否则。。。
            if (IMService.mCurAccount.equals(from_account)) {
                //发送的
                return SEND;
            } else {
                //接收的
                return RECEIVE;
            }
            //return super.getItemViewType(position); // position  =  0 、 1
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;//默认是一种视图类型，我们现在进行+1 = 2种
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (getItemViewType(position) == RECEIVE) {
                if (convertView == null) {
                    convertView = View.inflate(ChatActivity.this, R.layout.item_layout_chat_in, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);
                    //holder赋值
                    holder.iv_avater = (ImageView) convertView.findViewById(R.id.iv_avater);
                    holder.tv_stamp = (TextView) convertView.findViewById(R.id.tv_stamp);
                    holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                    holder.ll_item_chat_content = (LinearLayout) convertView.findViewById(R.id.ll_item_chat_content);
                    holder.fl_item_chat = (FrameLayout) convertView.findViewById(R.id.fl_item_chat);
                    holder.view_item_chat_record_anim = convertView.findViewById(R.id.view_item_chat_record_anim);
                    holder.tv_item_chat_html = (TextView) convertView.findViewById(R.id.tv_item_chat_html);
                    holder.tv_item_chat_record_time = (TextView) convertView.findViewById(R.id.tv_item_chat_record_time);
                    holder.iv_ex = (ImageChatBubbleEx) convertView.findViewById(R.id.iv_ex);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
            } else {
                if (convertView == null) {
                    convertView = View.inflate(ChatActivity.this, R.layout.item_layout_chat_out, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);
                    //holder赋值
                    holder.iv_avater = (ImageView) convertView.findViewById(R.id.iv_avater);
                    holder.tv_stamp = (TextView) convertView.findViewById(R.id.tv_stamp);
                    holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                    holder.ll_item_chat_content = (LinearLayout) convertView.findViewById(R.id.ll_item_chat_content);
                    holder.fl_item_chat = (FrameLayout) convertView.findViewById(R.id.fl_item_chat);
                    holder.view_item_chat_record_anim = convertView.findViewById(R.id.view_item_chat_record_anim);
                    holder.tv_item_chat_html = (TextView) convertView.findViewById(R.id.tv_item_chat_html);
                    holder.tv_item_chat_record_time = (TextView) convertView.findViewById(R.id.tv_item_chat_record_time);
                    holder.iv_ex = (ImageChatBubbleEx) convertView.findViewById(R.id.iv_ex);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
            }


            //得到数据，展示
            mCursor.moveToPosition(position);
            int _id = mCursor.getInt(mCursor.getColumnIndex("_id"));
            String flag = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG));
            String stamp = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.TIME));
            String body = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.BODY));
            String nickname;
            String useravatar;
            if (getItemViewType(position) == RECEIVE) {
                useravatar = mTargetAvatarUrl;
                nickname = mTargetNickName;
            } else {
                useravatar = IMService.mCurAvatarUrl;
                nickname = IMService.mCurNickName;
            }
            if (useravatar == null) {
                holder.iv_avater.setImageDrawable(new CircleImageDrawable(BitmapUtil.ScaleBitmap(ChatActivity.this, R.drawable.useravatar, 64, 64)));
            } else {
                ImageLoader.getInstance().displayImage(useravatar, holder.iv_avater, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
            }
            String formatStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE).format(new Date(Long.parseLong(stamp)));
            holder.tv_stamp.setText(formatStamp);
            holder.tv_nickname.setText(nickname);
            //获取气泡内文本内容的宽度
            ViewGroup.LayoutParams linearParams = holder.tv_item_chat_html.getLayoutParams();
            holder.view_item_chat_record_anim.setTag(_id + "record");

            if (flag == null) {
                flag = Const.MSGFLAG_TEXT;
            }
            if (flag.equals(Const.MSGFLAG_RECORD)) {
                //录音数据
                String recordlen = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.RECORDLEN));
                String recordtime = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.RECORDTIME));
                String recordurl = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.RECORDURL));
                holder.tv_item_chat_html.setVisibility(View.GONE);
                holder.tv_item_chat_record_time.setVisibility(View.VISIBLE);
                holder.view_item_chat_record_anim.setVisibility(View.VISIBLE);
                holder.iv_ex.setVisibility(View.GONE);
                holder.fl_item_chat.setVisibility(View.VISIBLE);
                holder.tv_item_chat_record_time.setText(recordtime + "\"");
                //设置item气泡的宽度
                ViewGroup.LayoutParams lp = holder.fl_item_chat.getLayoutParams();
                lp.width = (int) (mMinItemWidth + (mMaxItemWidth / 60f * Float.parseFloat(recordtime)));

                holder.view_item_chat_record_anim.setOnClickListener(new ItemRecordClickListener(convertView, position));
            } else if (flag.equals(Const.MSGFLAG_TEXT) || flag.equals(Const.MSGFLAG_VIDEO)) {
                //带表情的文本||视频申请
                holder.tv_item_chat_html.setVisibility(View.VISIBLE);
                holder.tv_item_chat_record_time.setVisibility(View.GONE);
                holder.view_item_chat_record_anim.setVisibility(View.GONE);
                holder.iv_ex.setVisibility(View.GONE);
                holder.fl_item_chat.setVisibility(View.VISIBLE);
                holder.tv_item_chat_html.setText(getFaceText(ChatActivity.this, body));
                //设置宽度
                ViewGroup.LayoutParams lp = holder.fl_item_chat.getLayoutParams();
                lp.width = linearParams.width;
            } else
                //展示图片
                if (flag.equals(Const.MSGFLAG_IMG)) {
                    holder.tv_item_chat_html.setVisibility(View.GONE);
                    holder.tv_item_chat_record_time.setVisibility(View.GONE);
                    holder.view_item_chat_record_anim.setVisibility(View.GONE);
                    String imgurl = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.IMGURL));
                    String imgThumbUrl = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.BODY));

                    holder.fl_item_chat.setVisibility(View.GONE);
                    holder.iv_ex.setVisibility(View.VISIBLE);
                    holder.iv_ex.setmView(holder.fl_item_chat);
                    holder.iv_ex.setB(false);
                    ImageLoader.getInstance().displayImage(imgThumbUrl, holder.iv_ex, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif());

                    holder.iv_ex.setOnClickListener(new ItemImageClickListener(convertView, position));
                }
            return super.getView(position, convertView, parent);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }

        public String getPositionValue(int position, String field) {
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex(field));
        }

        public int getPositionIntValue(int position, String field) {
            mCursor.moveToPosition(position);
            return mCursor.getInt(mCursor.getColumnIndex(field));
        }

        class ViewHolder {
            ImageView iv_avater;
            TextView tv_stamp;
            TextView tv_nickname;
            LinearLayout ll_item_chat_content;
            FrameLayout fl_item_chat;
            View view_item_chat_record_anim;
            TextView tv_item_chat_html;
            TextView tv_item_chat_record_time;
            ImageChatBubbleEx iv_ex;
        }
    }


    /**
     * 设置或更新adapter
     */
    private void setAdapterOrNotify() {
        //System.out.println("====================  manager.restartLoader  ChatActiviy =====================");
        manager.restartLoader(0, null, this);
    }


    /**
     * 图片点击事件
     */
    private class ItemImageClickListener implements View.OnClickListener {

        private final View view;
        private final int position;

        public ItemImageClickListener(View convertView, int position) {
            this.view = convertView;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            String flag = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.FLAG);//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG));
            //如果是图片
            if (flag.equals(Const.MSGFLAG_IMG)) {
                String imgurl = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.IMGURL);//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.IMGURL));
                showGallery(imgurl);
            }
        }
    }


    /**
     * 录音点击事件
     */
    private class ItemRecordClickListener implements View.OnClickListener {
        private final View view;
        private final int position;

        public ItemRecordClickListener(View convertView, int position) {
            this.view = convertView;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            int _id = mAdapter.getPositionIntValue(position, "_id");//mCursor.getInt(mCursor.getColumnIndex("_id"));
            String account = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.FROM_ACCOUNT);// mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FROM_ACCOUNT));
            String flag = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.FLAG);// mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG));
            String recordurl = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.RECORDURL);//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.RECORDURL));
            //如果是录音
            if (flag.equals(Const.MSGFLAG_RECORD)) {
                String mDirection;
                int recordBackgroundResouceId;
                int recordAnimResouceId;
                //区别消息的左右之分
                if (account.equals(IMService.mCurAccount)) {
                    recordBackgroundResouceId = R.drawable.audio_normal_right;
                    recordAnimResouceId = R.drawable.anim_play_audio_right;
                    mDirection = "right";
                    if (mAnimView != null) {
                        lastAnimView = mAnimView;
                        mAnimView = view.findViewWithTag(_id + "record");
                        lastAnimView.setBackgroundResource(lastAnimViewBackgroundResouceId);
                        lastAnimView = null;
                    }
                    mAnimView = view.findViewWithTag(_id + "record");
                } else {
                    recordBackgroundResouceId = R.drawable.audio_normal_left;
                    recordAnimResouceId = R.drawable.anim_play_audio_left;
                    mDirection = "left";
                    if (mAnimView != null) {
                        lastAnimView = mAnimView;
                        mAnimView = view.findViewWithTag(_id + "record");
                        lastAnimView.setBackgroundResource(lastAnimViewBackgroundResouceId);
                        lastAnimView = null;
                    }
                    mAnimView = view.findViewWithTag(_id + "record");
                }
                lastAnimViewBackgroundResouceId = recordBackgroundResouceId;
                //一、播放动画(帧动画)
                //2、设置组件的背景资源id为动画描述文件
                mAnimView.setBackgroundResource(recordAnimResouceId);
                //3、定义AnimationDrawable = 组件的背景
                AnimationDrawable anim = (AnimationDrawable) mAnimView.getBackground();
                //4、播放
                anim.start();
                //二、播放音频,(音频路径，完成后的回调)
                //System.out.println("播放文件路径：" + recordurl);
                MediaManager.playSound(mDirection, recordurl, new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        int recordBackgroundResouceId;
                        //区别消息的左右之分
                        if (MediaManager.mDirection.equals("left")) {
                            recordBackgroundResouceId = R.drawable.audio_normal_left;
                        } else {
                            recordBackgroundResouceId = R.drawable.audio_normal_right;
                        }
                        mAnimView.setBackgroundResource(recordBackgroundResouceId);
                        //播放一个很短暂的提示音:bi
                        GlobalSoundPool.getInstance().play(R.raw.v_bi);
                    }
                });
            }
        }
    }


    /**
     * * 查看图片（画廊）
     *
     * @param imgurl
     */
    private void showGallery(String imgurl) {
        String[] imgAlist;
        ArrayList<String> al = new ArrayList<>();

        String[] selectionArgs = new String[]{IMService.mCurAccount, IMService.mCurAccount, mTargetAccount, mTargetAccount, IMService.mCurAccount};
        Cursor cursor = new SmsDbHelper(this).queryImagesFromChat(selectionArgs);
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String img = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.IMGURL));
                al.add(img);
            }
            cursor.close();
            imgAlist = al.toArray(new String[al.size()]);
            Intent intent = new Intent(this, ImageViewPage.class);
            intent.putExtra("currentImg", imgurl);
            intent.putExtra("imgAlist", imgAlist);
            startActivity(intent);
        }
    }


    /**
     * 广播接受者
     * 退出与此好友聊天，此好友已被删除 的动态
     */
    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Const.EXIT_CHAT_ACTION)) {
                finish();
            }
        }
    };

    private void initListener() {
        //注册广播接收者
        // 退出与此好友聊天，此好友已被删除 的动态广播消息
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction(Const.EXIT_CHAT_ACTION);
        registerReceiver(dynamicReceiver, filter_dynamic);

        mEtPrivateChatText.setOnClickListener(this);
        mIvPrivateChatFace.setOnClickListener(this);
        mIbPrivateChatKey.setOnClickListener(this);
        mBtPivateChatSend.setOnClickListener(this);
        mIbPrivateChatAdd.setOnClickListener(this);
        mBtPrivateChatVideo.setOnClickListener(this);
        mBtPrivateChatImage.setOnClickListener(this);
        mBtPrivateChatcallphone.setOnClickListener(this);
        mBtPrivateChatcallsms.setOnClickListener(this);

        mEtPrivateChatText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    closeFaceAndShowKeyBoard();
                } else {
                }
            }
        });
        mEtPrivateChatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLength = mEtPrivateChatText.getText().length();
                if (textLength > 0) {
                    mBtPivateChatSend.setVisibility(View.VISIBLE);
                    mIbPrivateChatAdd.setVisibility(View.GONE);
                } else {
                    mIbPrivateChatAdd.setVisibility(View.VISIBLE);
                    mBtPivateChatSend.setVisibility(View.GONE);
                }
            }
        });
        mEtPrivateChatText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //录入框点击事件
            case R.id.et_private_chat_text: {
                if (isShowMore && mLlPrivateChatExt.getVisibility() == View.VISIBLE) {
                    mLlPrivateChatExt.setVisibility(View.GONE);
                    mLlMoreExt.setVisibility(View.GONE);
                    isShowMore = false;
                }
                closeFaceAndShowKeyBoard();
                break;
            }
            //点击表情按钮
            case R.id.iv_private_chat_face: {
                if (isShowFace) {
                    closeFace();
                } else {
                    hideSoftInputAndShowFace();
                }
                break;
            }
            //录音/文字/表情按钮
            case R.id.ib_private_chat_key: {
                closeFace();
                if (mRlPrivateChat.getVisibility() == View.VISIBLE) {
                    mRlPrivateChat.setVisibility(View.GONE);
                    mAudioRecoderButton.setVisibility(View.VISIBLE);
                    mIbPrivateChatKey.setImageResource(R.drawable.ic_keyboard_48px);
                    //隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtPrivateChatText.getWindowToken(), 0);
                } else {
                    mRlPrivateChat.setVisibility(View.VISIBLE);
                    mAudioRecoderButton.setVisibility(View.GONE);
                    mIbPrivateChatKey.setImageResource(R.drawable.ic_audio_48);
                }
                break;
            }
            //发送私聊按钮
            case R.id.bt_pivate_chat_send: {
                //发送私聊消息
                String msgText = mEtPrivateChatText.getText().toString();
                if (!StringUtils.isEmpty(msgText) && msgText.length() > 0) {
                    sentMsg(msgText);
                }
                break;
            }
            //更多功能按钮
            case R.id.ib_private_chat_add: {
                isShowFace = false;
                if (mLlPrivateChatExt.getVisibility() == View.VISIBLE) {
                    mLlPrivateChatExt.setVisibility(View.GONE);
                    mLlMoreExt.setVisibility(View.GONE);
                    isShowMore = false;
                } else if (mLlPrivateChatExt.getVisibility() == View.GONE) {
                    mLlPrivateChatExt.setVisibility(View.VISIBLE);
                    mLlMoreExt.setVisibility(View.VISIBLE);
                    isShowMore = true;
                }
                break;
            }
            //视频申请按钮
            case R.id.bt_private_chat_video: {
                //发送视频申请消息
                sentApplyVideoMessage();
                break;
            }
            case R.id.bt_private_chat_image: {
                //选择图片
                selectImage();
                break;
            }
            case R.id.bt_private_chat_callphone: {
                //拨打电话
                Intent intent2 = new Intent();
                intent2.setAction("android.intent.action.CALL");
                intent2.addCategory("android.intent.category.DEFAULT");
                intent2.setData(Uri.parse("tel:" + mUserphone));
                startActivity(intent2);
                break;
            }
            case R.id.bt_private_chat_callsms: {
                //发送短信
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + mUserphone));
                sendIntent.putExtra("sms_body", "");
                startActivity(sendIntent);
                break;
            }
        }
    }


    private void sentApplyVideoMessage() {
        sentMsg();
    }


    private void hideSoftInputAndShowFace() {
        hideSoftInput(mEtPrivateChatText);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                showFace();
            }
        }, 100);
    }

    /**
     * 打开表情
     */
    private void showFace() {
        if (isKeyBoardShow(this)) {
            hideSoftInput(mEtPrivateChatText);
        }
        mLlPrivateChatExt.setVisibility(View.VISIBLE);
        mLlMoreExt.setVisibility(View.GONE);
        Container.setVisibility(View.VISIBLE);
        isShowFace = true;
    }

    private void closeFace() {
        mLlPrivateChatExt.setVisibility(View.GONE);
        mLlMoreExt.setVisibility(View.VISIBLE);
        Container.setVisibility(View.GONE);
        isShowFace = false;
    }

    private void closeFaceAndShowKeyBoard() {
        //如果打开了表情框，就关闭表情框，并显示键盘
        if (isShowFace) {
            closeFace();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    showKeyBoard(mEtPrivateChatText);
                }
            }, 100);
        }
    }


    /**
     * 发送语音消息
     *
     * @param msgText
     * @param flag
     * @param recordurl
     * @param recordlen
     * @param recordtime
     */
    private void sentMsg(final String msgText, final String flag, final String recordurl, final long recordlen, final double recordtime) {
        if (IMService.conn != null && IMService.conn.isConnected()) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    //1、创建一个消息
                    final Message msg = new Message();
                    JivePropertiesExtension jpe = new JivePropertiesExtension();
                    msg.setFrom(IMService.mCurAccount);
                    msg.setTo(mTargetAccount);
                    msg.setBody(msgText);
                    msg.setType(Message.Type.chat);
                    jpe.setProperty(Const.MSGFLAG, flag);
                    //如果是录音消息
                    if (flag.equals(Const.MSGFLAG_RECORD)) {
                        String user = AsmackUtils.filterChineseToUrl(IMService.mCurAccount);
                        String httpRecordurl = getResources().getString(R.string.app_server) + Const.WEB_AMR_PATH + user + "/" + recordurl.substring(recordurl.lastIndexOf("/") + 1);
                        jpe.setProperty(Const.RECORDURL, httpRecordurl);
                        jpe.setProperty(Const.RECORDLEN, recordlen);
                        jpe.setProperty(Const.RECORDTIME, recordtime);

                        msg.addExtension(jpe);
                        //上传录音
                        uploadFile(recordurl, msg);

                    } else
                        //如果是文本消息
                        if (flag.equals(Const.MSGFLAG_TEXT)) {
                            msg.addExtension(jpe);
                            //调用服务内的发送消息方法
                            mImService.sendMessage(msg);
                        }
                    //清空输入框
                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtPrivateChatText.setText("");
                        }
                    });
                }
            });
        } else {
            ToastUtils.showToastSafe("没有网络");
        }
    }

    //    /**
//     * 上传成功的总数
//     */
//    private int uploadDones;

    /**
     * 上传录音文件
     *
     * @param recordurl
     * @param msg
     */
    private void uploadFile(String recordurl, Message msg) {
        //uploadDones = 0;
        String filepath = recordurl;
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        UIProgressListener uiProgressRequestListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
                //ui层回调
                //System.out.println((int) ((100 * bytesWrite) / contentLength));
                if (done) {
                    //uploadDones++;
                }
            }

            @Override
            public void onUIStart(long bytesWrite, long contentLength, boolean done) {
                super.onUIStart(bytesWrite, contentLength, done);
            }

            @Override
            public void onUIFinish(long bytesWrite, long contentLength, boolean done) {
                super.onUIFinish(bytesWrite, contentLength, done);
            }
        };
        //上传
        HttpUtil.okhttpPost_uploadFile(filepath, filename, uiProgressRequestListener, Const.UPLOAD_TYPE_AMR, new MyAMRCallBack(msg));
    }


    /**
     * 录音的上传监听
     */
    private class MyAMRCallBack implements Callback {
        private Message mMessage;

        public MyAMRCallBack(Message msg) {
            mMessage = msg;
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.code() == 200) {
                ////System.out.println("====================  语音消息  ===================== " + mMessage.toXML());
                mImService.sendMessage(mMessage);
            }
        }
    }

    /**
     * 发送文本消息
     *
     * @param msgText
     */
    private void sentMsg(final String msgText) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //1、创建一个消息
                Message msg = new Message();
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                msg.setFrom(IMService.mCurAccount);
                msg.setTo(mTargetAccount);
                msg.setBody(msgText);
                msg.setType(Message.Type.chat);
                jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_TEXT);
                msg.addExtension(jpe);
                //System.out.println("------------------------ " + msg.toXML());
                //调用服务内的发送消息方法
                mImService.sendMessage(msg);
                //清空输入框
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtPrivateChatText.setText("");
                    }
                });
            }
        });
    }


    /**
     * 选择图片
     */
    private void selectImage() {
        if (IMService.conn != null && IMService.conn.isConnected()) {
            Intent intent = new Intent(this, MultiImageSelectorActivity.class);
            // 是否显示调用相机拍照
            intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
            // 最大图片选择数量
            intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
            // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者 多选/MultiImageSelectorActivity.MODE_MULTI)
            intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
            // 默认选择图片,回填选项(支持String ArrayList)
            intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
            startActivityForResult(intent, REQUEST_IMAGE);
        } else {
            ToastUtils.showToastSafe("没有网络");
        }
    }

    /**
     * 视频申请
     */
    private void sentMsg() {
        if (IMService.conn != null && IMService.conn.isConnected()) {
            final String roomId = Integer.toString((new Random()).nextInt(100000000));
            System.err.println("roomid:="+roomId);
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    //1、创建一个消息
                    Message msg = new Message();
                    JivePropertiesExtension jpe = new JivePropertiesExtension();
                    msg.setFrom(IMService.mCurAccount);
                    msg.setTo(mTargetAccount);
                    msg.setBody("视频申请");
                    msg.setType(Message.Type.chat);
                    jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_VIDEO);
                    jpe.setProperty("roomid",roomId);
                    msg.addExtension(jpe);
                    //调用服务内的发送消息方法
                    mImService.sendMessage(msg);
                }
            });
//            Intent intent = new Intent(this, UIActivity.class);
//            intent.putExtra("mTargetNickName", mTargetNickName);
//            intent.putExtra("mTargetAccount", mTargetAccount.substring(0, mTargetAccount.lastIndexOf("@")) + "@" + Const.APP_PACKAGENAME);
//            startActivity(intent);
            connectToRoom(roomId,false,false,0);

        } else {
            ToastUtils.showToastSafe("没有网络");
        }
    }


    /**
     * 连接房间
     *
     * @param roomId         -
     * @param commandLineRun_ -
     * @param loopback       -
     * @param runTimeMs      -
     */
    private void connectToRoom(String roomId, boolean commandLineRun_, boolean loopback, int runTimeMs) {
        commandLineRun = commandLineRun_;
        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }
        //String roomUrl = sharedPref.getString(keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
        String roomUrl = "http://"+IMService.RTC_ROOMSERVER;
        System.err.println("roomUrl:"+roomUrl);
        // Video call enabled flag.
        boolean videoCallEnabled = sharedPref.getBoolean(keyprefVideoCallEnabled, Boolean.valueOf(getString(R.string.pref_videocall_default)));
        // Use Camera2 option.
        boolean useCamera2 = sharedPref.getBoolean(keyprefCamera2, Boolean.valueOf(getString(R.string.pref_camera2_default)));
        // Get default codecs.
        String videoCodec = sharedPref.getString(keyprefVideoCodec, getString(R.string.pref_videocodec_default));
        String audioCodec = sharedPref.getString(keyprefAudioCodec, getString(R.string.pref_audiocodec_default));
        // Check HW codec flag.
        boolean hwCodec = sharedPref.getBoolean(keyprefHwCodecAcceleration, Boolean.valueOf(getString(R.string.pref_hwcodec_default)));
        // Check Capture to texture.
        boolean captureToTexture = sharedPref.getBoolean(keyprefCaptureToTexture, Boolean.valueOf(getString(R.string.pref_capturetotexture_default)));
        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPref.getBoolean(keyprefNoAudioProcessingPipeline, Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default)));
        // Check Disable Audio Processing flag.
        boolean aecDump = sharedPref.getBoolean(keyprefAecDump, Boolean.valueOf(getString(R.string.pref_aecdump_default)));
        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPref.getBoolean(keyprefOpenSLES, Boolean.valueOf(getString(R.string.pref_opensles_default)));
        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPref.getBoolean(keyprefDisableBuiltInAec, Boolean.valueOf(getString(R.string.pref_disable_built_in_aec_default)));
        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPref.getBoolean(keyprefDisableBuiltInAgc, Boolean.valueOf(getString(R.string.pref_disable_built_in_agc_default)));
        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPref.getBoolean(keyprefDisableBuiltInNs, Boolean.valueOf(getString(R.string.pref_disable_built_in_ns_default)));
        // Check Enable level control.
        boolean enableLevelControl = sharedPref.getBoolean(keyprefEnableLevelControl, Boolean.valueOf(getString(R.string.pref_enable_level_control_key)));
        // 得到视频分辨率设置
        int videoWidth = 0;
        int videoHeight = 0;
        String resolution = sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
        String[] dimensions = resolution.split("[ x]+");
        if (dimensions.length == 2) {
            try {
                videoWidth = Integer.parseInt(dimensions[0]);
                videoHeight = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                videoWidth = 0;
                videoHeight = 0;
                Log.e("rtc", "错误的视频分辨率设置: " + resolution);
            }
        }
        // Get camera fps from settings.
        int cameraFps = 0;
        String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
        String[] fpsValues = fps.split("[ x]+");
        if (fpsValues.length == 2) {
            try {
                cameraFps = Integer.parseInt(fpsValues[0]);
            } catch (NumberFormatException e) {
                Log.e("rtc", "错误的相机fps设置: " + fps);
            }
        }
        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPref.getBoolean(keyprefCaptureQualitySlider, Boolean.valueOf(getString(R.string.pref_capturequalityslider_default)));
        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        String bitrateTypeDefault = getString(
                R.string.pref_startvideobitrate_default);
        String bitrateType = sharedPref.getString(
                keyprefVideoBitrateType, bitrateTypeDefault);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = sharedPref.getString(keyprefVideoBitrateValue, getString(R.string.pref_startvideobitratevalue_default));
            videoStartBitrate = Integer.parseInt(bitrateValue);
        }
        int audioStartBitrate = 0;
        bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
        bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = sharedPref.getString(keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
            audioStartBitrate = Integer.parseInt(bitrateValue);
        }
        // Check statistics display option.
        //boolean displayHud = sharedPref.getBoolean(keyprefDisplayHud, Boolean.valueOf(getString(R.string.pref_displayhud_default)));
        //boolean tracing = sharedPref.getBoolean(keyprefTracing, Boolean.valueOf(getString(R.string.pref_tracing_default)));
        // Start AppRTCDemo activity.
        Log.d("rtc", "连接到房间 " + roomId + " at URL " + roomUrl);
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(this, CallActivity.class);
            intent.setData(uri);
            intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
            intent.putExtra(CallActivity.EXTRA_LOOPBACK, loopback);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
            intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
            //intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
            //intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
            intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);

            intent.putExtra("mTargetNickName", mTargetNickName);
            intent.putExtra("mTargetAccount", mTargetAccount.substring(0, mTargetAccount.lastIndexOf("@")) + "@" + Const.APP_PACKAGENAME);

            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }
    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == REQUEST_IMAGE) {
                if (resultCode == RESULT_OK) {
                    if (mSelectPath == null) mSelectPath = new ArrayList<>();
                    if (mNameList == null) mNameList = new ArrayList<>();

                    if (mSelectPath.size() > 0) {
                        for (int k = 0; k < mSelectPath.size(); k++) {
                            mSelectPath.remove(k);
                        }
                    }
                    mSelectPath.clear();

                    //清空mNameList
                    if (mNameList.size() > 0) {
                        for (int i = 0; i < mNameList.size(); i++) {
                            mNameList.remove(i);
                        }
                    }
                    mNameList.clear();

                    // 获取返回的图片列表
                    paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                    for (String path : paths) {
                        boolean showcheck = false;
                        mNameList.add(new ImagePickBean(path, "", showcheck, false));
                        mSelectPath.add(path);
                    }
                    //初始化进度监听器
                    progressListener = new MyCropImage_ProgressListener();
                    //上传图片
                    String androidImagePath = mSelectPath.get(0);
                    mImageName = UUID.randomUUID().toString() + androidImagePath.substring(androidImagePath.lastIndexOf("."));
                    HttpUtil.okhttpPost_uploadFile(androidImagePath, mImageName, progressListener, Const.UPLOAD_TYPE_MSGIMG, new MyCallBack());
                }
            }
        }
    }

    /**
     * 图片上传成功回调
     */
    private class MyCallBack implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            ////System.out.println("=====================================onResponse==== response:" + response.toString());
            if (response.code() == 200) {
                response.body().close();
                String webImageThumbUrl = getResources().getString(R.string.app_server) + Const.WEB_MSGIMG_PATH + IMService.mCurAccount + "/" + Const.WEB_FACE_THUMBS + mImageName;
                String webImageUrl = getResources().getString(R.string.app_server) + Const.WEB_MSGIMG_PATH + IMService.mCurAccount + "/" + mImageName;
                sendImage(webImageThumbUrl, webImageUrl);
            }
        }
    }

    /**
     * 上传进度监听器
     */
    private class MyCropImage_ProgressListener extends UIProgressListener {
        @Override
        public void onUIProgress(long currentBytes, long contentLength, boolean done) {
            //ui层回调
            //System.out.println((int) ((100 * currentBytes) / contentLength));
            if (done) {
            }
        }

        @Override
        public void onUIStart(long currentBytes, long contentLength, boolean done) {
            super.onUIStart(currentBytes, contentLength, done);
        }

        @Override
        public void onUIFinish(long currentBytes, long contentLength, boolean done) {
            super.onUIFinish(currentBytes, contentLength, done);
            ////System.out.println("====================  onUIFinish  =====================");
        }
    }


    /**
     * 发送图片的消息
     *
     * @param webImageThumbUrl
     * @param webImageUrl
     */
    private void sendImage(final String webImageThumbUrl, final String webImageUrl) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                ////System.out.println("====================  发送图片的消息  =====================");
                //1、创建一个消息
                Message msg = new Message();
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                msg.setFrom(IMService.mCurAccount);
                msg.setTo(mTargetAccount);
                msg.setBody(webImageThumbUrl);
                msg.setType(Message.Type.chat);
                jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_IMG);
                jpe.setProperty(Const.REALIMAGEURL, webImageUrl);
                msg.addExtension(jpe);
                //调用服务内的发送消息方法
                mImService.sendMessage(msg);
                //清空输入框
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtPrivateChatText.setText("");
                    }
                });
            }
        });
    }


    /**
     * invalidateOptionsMenu();触发
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friends_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_show_friend:
                Intent intent = new Intent(ChatActivity.this, FriendsProfile.class);
                intent.putExtra("username", mTargetAccount.substring(0, mTargetAccount.lastIndexOf("@")));
                intent.putExtra("flag", "show");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        if (isShowFace) {
            closeFace();
        } else if (mLlPrivateChatExt.getVisibility() == View.VISIBLE) {
            mLlPrivateChatExt.setVisibility(View.GONE);
            mLlMoreExt.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }


    //---------------内容观察者---------------//
    SmsContentObserver observer = new SmsContentObserver(new Handler());

    /**
     * 注册内容观察者
     */
    public void registerContentObserver() {
        ////System.out.println("注册内容观察者");
        //第2个参数为true：
        //content://" + AUTHORITIES + "/sms  的孩子 content://" + AUTHORITIES + "/sms/xxxx 也会被通知
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, observer);
    }

    /**
     * 销毁内容观察者
     */
    public void unRegisterContentObserver() {
        ////System.out.println("销毁内容观察者");
        getContentResolver().unregisterContentObserver(observer);
    }

    /**
     * 消息的内容观察者类
     */
    public class SmsContentObserver extends ContentObserver {
        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //数据发生改变后，要么设置adapter，或者更新adapter
            super.onChange(selfChange, uri);
            setAdapterOrNotify();
        }
    }
    //---------------内容观察者end---------------//


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
            ////System.out.println("====================  onServiceConnected  =====================");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();

            /*====================  更新与此人的聊天消息全部为已读状态 1  =====================*/
            updateMessageToIsRead();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ////System.out.println("====================  onServiceDisconnected  =====================");
        }
    }


    /**
     * 进入聊天界面，将我与此人聊天的消息全部设置为已读状态 1
     */
    private void updateMessageToIsRead() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mImService.updateChatMessageToIsRead(mTargetAccount);
            }
        });
    }


}
