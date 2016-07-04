package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.softfun_xmpp.dbhelper.GroupDbHelper;
import com.softfun_xmpp.dbhelper.SmsDbHelper;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.provider.GroupProvider;
import com.softfun_xmpp.provider.SmsProvider;
import com.softfun_xmpp.recorder.AudioRecoderButton;
import com.softfun_xmpp.recorder.MediaManager;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.BitmapUtil;
import com.softfun_xmpp.utils.CircleImageDrawable;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.tb.emoji.Emoji;
import com.tb.emoji.EmojiUtil;
import com.tb.emoji.FaceFragment;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.softfun_xmpp.utils.ToolsUtil.dip2px;
import static com.softfun_xmpp.utils.ToolsUtil.hideSoftInput;
import static com.softfun_xmpp.utils.ToolsUtil.isKeyBoardShow;
import static com.softfun_xmpp.utils.ToolsUtil.roundDouble;
import static com.softfun_xmpp.utils.ToolsUtil.showKeyBoard;
import static com.tb.emoji.EmojiUtil.getFaceText;

public class MultiChatActivity extends AppCompatActivity implements RefreshListView.OnRefreshDataListener, FaceFragment.OnEmojiClickListener, View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {


    public static final String F_ROOM_JID = "roomjid";
    public static final String F_ROOM_NAME = "roomname";
    public static final String F_ROOM_AVATARURL = "room_avataurl";
    public static final String F_REASON = "room_reason";
    public static final String F_FROM_ACCOUNT = "from_account";


    private String mTargetRoomJid;
    private String mTargetRoomName;

    private MultiUserChat mMultiUserChat;


    private Toolbar mToolbar;
    private RefreshListView mLv;
    private ImageButton mIbChatKey;
    private AudioRecoderButton mBtAudioRecoder;
    private RelativeLayout mRlChat;
    private EditText mEtChatText;
    private ImageView mIvChatFace;
    private Button mBtChatSend;
    private ImageButton mIbChatAdd;
    private LinearLayout mLlChatExt;
    private LinearLayout mLlMoreExt;
    private LinearLayout mBtChatVideo;
    private FrameLayout mContainer;
    private LinearLayout mBtChatImage;


    private boolean isShowFace;//表情栏显示
    private MyCursorAdapter mAdapter;//适配器
//    private Cursor mCursor;//指针
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


    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mLv = (RefreshListView) findViewById(R.id.lv);
        mIbChatKey = (ImageButton) findViewById(R.id.ib_chat_key);
        mBtAudioRecoder = (AudioRecoderButton) findViewById(R.id.bt_audio_recoder);
        mRlChat = (RelativeLayout) findViewById(R.id.rl_chat);
        mEtChatText = (EditText) findViewById(R.id.et_chat_text);
        mIvChatFace = (ImageView) findViewById(R.id.iv_chat_face);
        mBtChatSend = (Button) findViewById(R.id.bt_chat_send);
        mIbChatAdd = (ImageButton) findViewById(R.id.ib_chat_add);
        mLlChatExt = (LinearLayout) findViewById(R.id.ll_chat_ext);
        mLlMoreExt = (LinearLayout) findViewById(R.id.ll_more_ext);
        mBtChatVideo = (LinearLayout) findViewById(R.id.bt_chat_video);
        mContainer = (FrameLayout) findViewById(R.id.Container);
        mBtChatImage = (LinearLayout) findViewById(R.id.bt_chat_image);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_chat);
        init();
        initView();
        initData();
        initListener();
    }


    @Override
    protected void onDestroy() {
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
        Intent service = new Intent(MultiChatActivity.this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);

        mTargetRoomName = getIntent().getStringExtra(F_ROOM_NAME);
        mTargetRoomJid = getIntent().getStringExtra(F_ROOM_JID);
        //设置 当前聊天对象
        IMService.chatObject = mTargetRoomJid;
        //System.out.println("====================  IMService.chatObject  ===================== "+IMService.chatObject);
    }

    private void initView() {

        assignViews();
        //添加表情
        FaceFragment faceFragment = FaceFragment.Instance();
        getSupportFragmentManager().beginTransaction().add(R.id.Container, faceFragment).commit();
        mLv.setRefreshDataListener(this);
        mLv.setHeadHintText_pull("下拉加载历史记录");
        mLv.setHeadHintText_release("松开加载历史记录");
        mLv.setHeadHintText_refreshing("正在加载历史记录");
        mLv.setHeadHintText_refreshed("历史记录加载完成");
    }

    private void initData() {

        //给页面设置工具栏
        //标题写在setSupportActionBar前面
        mToolbar.setTitle(mTargetRoomName);
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mMultiUserChat = AsmackUtils.getMultiUserChat(mTargetRoomJid);

        mBtAudioRecoder.setAudioFinishRecorderListener(new AudioRecoderButton.AudioFinishRecorderListener() {
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
        mAdapter = new MyCursorAdapter(this,null,true);
        mLv.setAdapter(mAdapter);
        //初始化加载器管理器
        manager = getSupportLoaderManager();
        manager.initLoader(0,null,this);
    }




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int count;
        if(mAdapter.getCursor()!=null){
            count = mAdapter.getCount();
        }else{
            count = 0;
        }
        count = count+20;
        String[] sqlWhereArgs = new String[]{Message.Type.groupchat.name(), AsmackUtils.filterGroupJid(mTargetRoomJid), count+"", "0"};
        return new CursorLoader(MultiChatActivity.this,SmsProvider.URI_GROUPSMS, null, null, sqlWhereArgs, null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null){
            if(data.getCount()>0){
                if(isLoadHistoryMsg){
                    int currentPosition = mAdapter.getCount();
                    mAdapter.swapCursor(data);
                    isLoadHistoryMsg = false;
                    mLv.refreshFinish();
                    mLv.setSelection(mAdapter.getCount() - currentPosition);
                }else{
                    mAdapter.swapCursor(data);
                    isLoadHistoryMsg = false;
                    mLv.setSelection(mAdapter.getCount() - 1);
                }

            }else{
                mAdapter.swapCursor(null);
                if(isLoadHistoryMsg){
                    mLv.refreshFinish();
                }
                isLoadHistoryMsg = false;
            }
        }else{
            mAdapter.swapCursor(null);
            if(isLoadHistoryMsg){
                mLv.refreshFinish();
            }
            isLoadHistoryMsg = false;
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mAdapter!=null)
            mAdapter.swapCursor(null);
    }




    private void initListener() {
        mEtChatText.setOnClickListener(this);
        mIvChatFace.setOnClickListener(this);
        mIbChatKey.setOnClickListener(this);
        mBtChatSend.setOnClickListener(this);
        mIbChatAdd.setOnClickListener(this);
        mBtChatVideo.setOnClickListener(this);
        mBtChatImage.setOnClickListener(this);

        mEtChatText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    closeFaceAndShowKeyBoard();
                } else {
                }
            }
        });
        mEtChatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLength = mEtChatText.getText().length();
                if (textLength > 0) {
                    mBtChatSend.setVisibility(View.VISIBLE);
                    mIbChatAdd.setVisibility(View.GONE);
                } else {
                    mIbChatAdd.setVisibility(View.VISIBLE);
                    mBtChatSend.setVisibility(View.GONE);
                }
            }
        });
        mEtChatText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                }
                return false;
            }
        });
    }


    @Override
    public void RefreshData() {
        //此页面中，下拉就是加载历史记录
        loadHistoryMsg();
    }

    @Override
    public void LoadMore() {

    }


    /**
     * 加载历史记录
     */
    private void loadHistoryMsg() {
        isLoadHistoryMsg = true;
        manager.restartLoader(0,null,this);
    }

    //// TODO: 2016-03-10 表情布局
    @Override
    public void onEmojiDelete() {
        String text = mEtChatText.getText().toString();
        if (text.isEmpty()) {
            return;
        }
        if ("]".equals(text.substring(text.length() - 1, text.length()))) {
            int index = text.lastIndexOf("[");
            if (index == -1) {
                int action = KeyEvent.ACTION_DOWN;
                int code = KeyEvent.KEYCODE_DEL;
                KeyEvent event = new KeyEvent(action, code);
                mEtChatText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
                return;
            }
            mEtChatText.getText().delete(index, text.length());
            return;
        }
        int action = KeyEvent.ACTION_DOWN;
        int code = KeyEvent.KEYCODE_DEL;
        KeyEvent event = new KeyEvent(action, code);
        mEtChatText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
    }

    @Override
    public void onEmojiClick(Emoji emoji) {
        if (emoji != null) {
            int index = mEtChatText.getSelectionStart();
            Editable editable = mEtChatText.getEditableText();
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
        }
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;//默认是一种视图类型，我们现在进行+1 = 2种
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (getItemViewType(position) == RECEIVE) {
                if (convertView == null) {
                    convertView = View.inflate(MultiChatActivity.this, R.layout.item_layout_chat_in, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);

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
                    convertView = View.inflate(MultiChatActivity.this, R.layout.item_layout_chat_out, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);

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
            String account = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FROM_ACCOUNT));
            String nickname;
            String useravatar;
            if (getItemViewType(position) == RECEIVE) {
                useravatar = AsmackUtils.getGroupMemberField(AsmackUtils.filterGroupJid(mTargetRoomJid),AsmackUtils.filterAccountToUserName(account), GroupDbHelper.GroupMemberTable.AVATARURL);
                nickname = AsmackUtils.getGroupMemberField(AsmackUtils.filterGroupJid(mTargetRoomJid),AsmackUtils.filterAccountToUserName(account), GroupDbHelper.GroupMemberTable.NICKNAME);
            } else {
                useravatar = IMService.mCurAvatarUrl;
                nickname = IMService.mCurNickName;
            }
            if (useravatar == null || useravatar.equals("")) {
                holder.iv_avater.setImageDrawable(new CircleImageDrawable(BitmapUtil.ScaleBitmap(MultiChatActivity.this, R.drawable.useravatar, 64, 64)));
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
            } else if (flag.equals(Const.MSGFLAG_TEXT)) {
                //带表情的文本
                holder.tv_item_chat_html.setVisibility(View.VISIBLE);
                holder.tv_item_chat_record_time.setVisibility(View.GONE);
                holder.view_item_chat_record_anim.setVisibility(View.GONE);
                holder.iv_ex.setVisibility(View.GONE);
                holder.fl_item_chat.setVisibility(View.VISIBLE);
                holder.tv_item_chat_html.setText(getFaceText(MultiChatActivity.this, body));
                //设置宽度
                ViewGroup.LayoutParams lp = holder.fl_item_chat.getLayoutParams();
                lp.width = linearParams.width;
            } else if (flag.equals(Const.MSGFLAG_IMG)) {
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
        public String getPositionValue(int position,String field){
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex( field ));
        }

        public int getPositionIntValue(int position,String field){
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
        //System.out.println("====================  manager.restartLoader  MultiChatActivity =====================");
        manager.restartLoader(0,null,this);

    }




    /**
     * 图片点击事件
     */
    private class ItemImageClickListener implements View.OnClickListener {

        private final int position;

        public ItemImageClickListener(View convertView, int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            String flag = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.FLAG);
            //如果是图片
            if (flag.equals(Const.MSGFLAG_IMG)) {
                String imgurl = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.IMGURL);
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
            int _id = mAdapter.getPositionIntValue(position, "_id");
            String account = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.FROM_ACCOUNT);;//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FROM_ACCOUNT));
            String flag = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.FLAG);;//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG));
            String body = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.BODY);//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.BODY));
            String recordurl = mAdapter.getPositionValue(position, SmsDbHelper.SmsTable.RECORDURL);//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.RECORDURL));
            if (flag.equals(Const.MSGFLAG_RECORD)) {
                //如果是录音
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
                ////System.out.println("播放文件路径：" + recordurl);
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
     * //TODO 废弃
     * item点击 事件
     */
    private class mItemClickLinstener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            int _id = mCursor.getInt(mCursor.getColumnIndex("_id"));
//            String account = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FROM_ACCOUNT));
//            String flag = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG));
//            String body = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.BODY));
//            String recordurl = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.RECORDURL));
//            //如果是图片
//            if (flag.equals(Const.MSGFLAG_IMG)) {
//                String imgurl = mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.IMGURL));
//                showGallery(imgurl);
//            }
//            //如果是录音
//            if (flag.equals(Const.MSGFLAG_RECORD)) {
//                String mDirection;
//                int recordBackgroundResouceId;
//                int recordAnimResouceId;
//
//                //区别消息的左右之分
//                if (account.equals(IMService.mCurAccount)) {
//                    recordBackgroundResouceId = R.drawable.audio_normal_right;
//                    recordAnimResouceId = R.drawable.anim_play_audio_right;
//                    mDirection = "right";
//                    if (mAnimView != null) {
//                        lastAnimView = mAnimView;
//                        mAnimView = view.findViewWithTag(_id + "record");
//                        lastAnimView.setBackgroundResource(lastAnimViewBackgroundResouceId);
//                        lastAnimView = null;
//                    }
//                    mAnimView = view.findViewWithTag(_id + "record");
//                } else {
//                    recordBackgroundResouceId = R.drawable.audio_normal_left;
//                    recordAnimResouceId = R.drawable.anim_play_audio_left;
//                    mDirection = "left";
//                    if (mAnimView != null) {
//                        lastAnimView = mAnimView;
//                        mAnimView = view.findViewWithTag(_id + "record");
//                        lastAnimView.setBackgroundResource(lastAnimViewBackgroundResouceId);
//                        lastAnimView = null;
//                    }
//                    mAnimView = view.findViewWithTag(_id + "record");
//                }
//                lastAnimViewBackgroundResouceId = recordBackgroundResouceId;
//                //一、播放动画(帧动画)
//                //2、设置组件的背景资源id为动画描述文件
//                mAnimView.setBackgroundResource(recordAnimResouceId);
//                //3、定义AnimationDrawable = 组件的背景
//                AnimationDrawable anim = (AnimationDrawable) mAnimView.getBackground();
//                //4、播放
//                anim.start();
//                //二、播放音频,(音频路径，完成后的回调)
//                //System.out.println("播放文件路径：" + recordurl);
//                MediaManager.playSound(mDirection, recordurl, new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        int recordBackgroundResouceId;
//                        //区别消息的左右之分
//                        if (MediaManager.mDirection.equals("left")) {
//                            recordBackgroundResouceId = R.drawable.audio_normal_left;
//                        } else {
//                            recordBackgroundResouceId = R.drawable.audio_normal_right;
//                        }
//                        mAnimView.setBackgroundResource(recordBackgroundResouceId);
//                        //播放一个很短暂的提示音:bi
//                        GlobalSoundPool.getInstance().play(R.raw.v_bi);
//                    }
//                });
//            }
        }
    }


    /**
     * 查看图片（画廊）
     *
     * @param imgurl
     */
    private void showGallery(String imgurl) {
        String[] imgAlist;
        ArrayList<String> al = new ArrayList<>();
        String[] selectionArgs = new String[]{Message.Type.groupchat.name(), AsmackUtils.filterGroupJid(mTargetRoomJid)};
        Cursor cursor = new SmsDbHelper(this).queryImagesFromGroupChat(selectionArgs);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //录入框点击事件
            case R.id.et_chat_text: {
                if (isShowMore&&mLlChatExt.getVisibility() == View.VISIBLE) {
                    mLlChatExt.setVisibility(View.GONE);
                    mLlMoreExt.setVisibility(View.GONE);
                    isShowMore = false;
                }
                closeFaceAndShowKeyBoard();
                break;
            }
            //点击表情按钮
            case R.id.iv_chat_face: {
                if (isShowFace) {
                    closeFace();
                } else {
                    hideSoftInputAndShowFace();
                }
                break;
            }
            //录音/文字/表情按钮
            case R.id.ib_chat_key: {
                closeFace();
                if (mRlChat.getVisibility() == View.VISIBLE) {
                    mRlChat.setVisibility(View.GONE);
                    mBtAudioRecoder.setVisibility(View.VISIBLE);
                    mIbChatKey.setImageResource(R.drawable.ic_keyboard_48px);
                    //隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtChatText.getWindowToken(), 0);
                } else {
                    mRlChat.setVisibility(View.VISIBLE);
                    mBtAudioRecoder.setVisibility(View.GONE);
                    mIbChatKey.setImageResource(R.drawable.ic_audio_48);
                }
                break;
            }
            //发送群聊按钮
            case R.id.bt_chat_send: {
                //发送群聊消息
                String msgText = mEtChatText.getText().toString();
                if (!StringUtils.isEmpty(msgText) && msgText.length() > 0) {
                    sentMsg(msgText);
                }
                break;
            }
            //更多功能按钮
            case R.id.ib_chat_add: {
                isShowFace = false;
                if (mLlChatExt.getVisibility() == View.VISIBLE) {
                    mLlChatExt.setVisibility(View.GONE);
                    mLlMoreExt.setVisibility(View.GONE);
                    isShowMore = false;
                } else if (mLlChatExt.getVisibility() == View.GONE) {
                    mLlChatExt.setVisibility(View.VISIBLE);
                    mLlMoreExt.setVisibility(View.VISIBLE);
                    isShowMore = true;
                }
                break;
            }
            //视频申请按钮
            case R.id.bt_chat_video: {
                //发送视频申请消息
                //sentApplyVideoMessage();
                break;
            }
            case R.id.bt_chat_image: {
                //选择图片
                selectImage();
            }
        }
    }

    private void sentApplyVideoMessage() {
        sentMsg();
    }


    private void hideSoftInputAndShowFace() {
        hideSoftInput(mEtChatText);
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
            hideSoftInput(mEtChatText);
        }
        mLlChatExt.setVisibility(View.VISIBLE);
        mLlMoreExt.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
        isShowFace = true;
    }

    private void closeFace() {
        mLlChatExt.setVisibility(View.GONE);
        mLlMoreExt.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);
        isShowFace = false;
    }

    private void closeFaceAndShowKeyBoard() {
        //如果打开了表情框，就关闭表情框，并显示键盘
        if (isShowFace) {
            closeFace();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    showKeyBoard(mEtChatText);
                }
            }, 100);
        }
    }


    private void uploadFile(String recordurl, Message msg) {
        String filepath = recordurl;
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        UIProgressListener uiProgressRequestListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
                //ui层回调
                ////System.out.println((int) ((100 * bytesWrite) / contentLength));
                if (done) {
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
                mImService.sendGroupMessage(mMultiUserChat, mMessage);
            }
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
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //1、创建一个消息
                Message msg = new Message(mTargetRoomJid, org.jivesoftware.smack.packet.Message.Type.groupchat);
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                msg.setBody(msgText);
                jpe.setProperty(Const.MSGFLAG, flag);
                //如果是录音消息
                if (flag.equals(Const.MSGFLAG_RECORD)) {
                    String user = AsmackUtils.filterChineseToUrl(IMService.mCurAccount);
                    String httpRecordurl = getResources().getString(R.string.app_server) + Const.WEB_AMR_PATH + user + "/" + recordurl.substring(recordurl.lastIndexOf("/") + 1);
                    jpe.setProperty(Const.RECORDURL, httpRecordurl);
                    jpe.setProperty(Const.RECORDLEN, recordlen);
                    jpe.setProperty(Const.RECORDTIME, recordtime);
                    jpe.setProperty(Const.GROUP_JID, AsmackUtils.filterGroupJid(mTargetRoomJid));
                    jpe.setProperty(Const.ACCOUNT, IMService.mCurAccount);
                    msg.addExtension(jpe);
                    //上传录音
                    uploadFile(recordurl, msg);
                } else
                    //如果是文本消息
                    if (flag.equals(Const.MSGFLAG_TEXT)) {
                        //调用服务内的发送消息方法
                        mImService.sendGroupMessage(mMultiUserChat, msg);
                    }
                //清空输入框
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtChatText.setText("");
                    }
                });
            }
        });
    }

    /**
     * 发送群文本消息
     *
     * @param msgText
     */
    private void sentMsg(final String msgText) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //1、创建一个消息
                //群聊消息的结构体，跟私聊不太一样，不能设置to，from，message会自动根据所在roomjid进行赋值
                Message msg = new Message(mTargetRoomJid, org.jivesoftware.smack.packet.Message.Type.groupchat);
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                msg.setBody(msgText);
                jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_TEXT);
                jpe.setProperty(Const.GROUP_JID, AsmackUtils.filterGroupJid(mTargetRoomJid));
                jpe.setProperty(Const.ACCOUNT, IMService.mCurAccount);
                msg.addExtension(jpe);
                //调用服务内的发送消息方法
                mImService.sendGroupMessage(mMultiUserChat, msg);
                //清空输入框
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtChatText.setText("");
                    }
                });
            }
        });
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
                Message msg = new Message(mTargetRoomJid, org.jivesoftware.smack.packet.Message.Type.groupchat);
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                msg.setBody(webImageThumbUrl);
                //msg.setType(Message.Type.chat);
                jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_IMG);
                jpe.setProperty(Const.GROUP_JID, AsmackUtils.filterGroupJid(mTargetRoomJid));
                jpe.setProperty(Const.ACCOUNT, IMService.mCurAccount);
                jpe.setProperty(Const.REALIMAGEURL, webImageUrl);
                msg.addExtension(jpe);
                //调用服务内的发送消息方法
                mImService.sendGroupMessage(mMultiUserChat, msg);
                //清空输入框
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtChatText.setText("");
                    }
                });
            }
        });
    }

    /**
     * 选择图片
     */
    private void selectImage() {
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
    }


    /**
     * 视频申请，功能还没做
     */
    private void sentMsg() {
//        ThreadUtils.runInThread(new Runnable() {
//            @Override
//            public void run() {
//                //1、创建一个消息
//                Message msg = new Message();
//                msg.setFrom(IMService.mCurAccount);
//                msg.setTo(mTargetAccount);
//                msg.setBody("视频申请");
//                msg.setType(Message.Type.chat);
//                msg.setProperty(Const.MSGFLAG, Const.MSGFLAG_VIDEO);
//                //调用服务内的发送消息方法
//                mImService.sendMessage(msg);
//            }
//        });
//
//        Intent intent = new Intent(this, UIActivity.class);
//        //intent.putExtra("mTargetNickName",mTargetNickName);
//        intent.putExtra("mTargetAccount", mTargetAccount.substring(0, mTargetAccount.lastIndexOf("@")) + "@" + Const.APP_PACKAGENAME);
//        startActivity(intent);
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
            ////System.out.println((int) ((100 * currentBytes) / contentLength));
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
     * 进入聊天界面，将此群聊的消息全部设置为已读状态 1
     */
    private void updateMessageToIsRead() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mImService.updateMultiChatMessageToIsRead(AsmackUtils.filterGroupJid(mTargetRoomJid));
            }
        });
    }


    /**
     * 查询此群聊信息，如果不存在了，就自动退出此页面
     */
    private void queryThisGroup() {
        Cursor mCursor;
        String sqlWhere = " owner=? and jid=?";
        String[] sqlWhereArgs = new String[]{IMService.mCurAccount,mTargetRoomJid};
        String sqlOrder = GroupDbHelper.GroupTable.PINYIN+" asc ";
        mCursor = getContentResolver().query(GroupProvider.URI_GROUP, null, sqlWhere, sqlWhereArgs, sqlOrder);
        if(mCursor!=null){
            if(mCursor.getCount()==0){
                mCursor.close();
                finish();
            }
        }else{
            finish();
        }
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
        inflater.inflate(R.menu.groups_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_show_group:
                //群组资料
                Intent profile = new Intent(this, MultiChatProfile.class);
                profile.putExtra(F_ROOM_JID, mTargetRoomJid);
                profile.putExtra(F_ROOM_NAME, mTargetRoomName);
                startActivity(profile);
                return true;
            case R.id.action_add_menber:
                //邀请新成员
                Intent intent = new Intent(this, MyFriendsListActivity.class);
                intent.putExtra(F_ROOM_JID, mTargetRoomJid);
                intent.putExtra(F_ROOM_NAME, mTargetRoomName);
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
        } else if (mLlChatExt.getVisibility() == View.VISIBLE) {
            mLlChatExt.setVisibility(View.GONE);
            mLlMoreExt.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }


    //---------------内容观察者---------------//
    SmsContentObserver observer = new SmsContentObserver(new Handler());

    GroupContentObserver observer_group = new GroupContentObserver(new Handler());

    /**
     * 注册内容观察者
     */
    public void registerContentObserver() {
        ////System.out.println("注册内容观察者");
        //第2个参数为true：
        getContentResolver().registerContentObserver(SmsProvider.URI_GROUPSMS, true, observer);


        //注册此内容提供者，目的是为了监听当前聊天群如果被删除，就自动退出页面
        getContentResolver().registerContentObserver(GroupProvider.URI_GROUP,true,observer_group);
    }

    /**
     * 销毁内容观察者
     */
    public void unRegisterContentObserver() {
        ////System.out.println("销毁内容观察者");
        getContentResolver().unregisterContentObserver(observer);

        getContentResolver().unregisterContentObserver(observer_group);
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

    /**
     * 群组信息的内容观察者，目的是为了监听当前聊天群如果被删除，就自动退出页面
     */
    public class GroupContentObserver extends ContentObserver{
        public GroupContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            queryThisGroup();
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
            /*====================  更新与此群的聊天消息全部为已读状态 1  =====================*/
            updateMessageToIsRead();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ////System.out.println("====================  onServiceDisconnected  =====================");
        }
    }






}
