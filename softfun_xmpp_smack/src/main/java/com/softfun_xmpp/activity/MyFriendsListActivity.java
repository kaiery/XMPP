package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.components.SideBar;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.dbhelper.ContactsDbHelper;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.provider.ContactsProvider;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.DisplayUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.VipResouce;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.HashMap;
import java.util.Map;

public class MyFriendsListActivity extends AppCompatActivity implements View.OnClickListener {

    private Map<Integer,String> selected = new HashMap<>();

    private Toolbar mToolbar;
    private SideBar sideBar;
    private TextView dialog;
    private ListView mLv;
    private LinearLayout titleLayout;
    private TextView title;
    private LinearLayout ll_container;
    private Button btn;
    private HorizontalScrollView hscroll;

    private MyCursorAdapter mAdapter;
    private Cursor mCursor;
    /**
     * 用于进行字母表分组
     */
    private AlphabetIndexer indexer;
    /**
     * 定义字母表的排序规则
     */
    private String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 上次第一个可见元素，用于滚动时记录标识。
     */
    private int lastFirstVisibleItem = -1;


    private String mTargetRoomJid;
    private MultiUserChat mMultiUserChat;
    private String mTargetRoomName;





    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends_list);


        init();


        mLv = (ListView) findViewById(R.id.lv);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        ll_container = (LinearLayout) findViewById(R.id.ll_container);
        btn = (Button) findViewById(R.id.btn);
        hscroll = (HorizontalScrollView) findViewById(R.id.hscroll);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //给页面设置工具栏
        //标题写在setSupportActionBar前面
        mToolbar.setTitle("邀请成员");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initData();
        initListener();
    }



    private void init() {
        //绑定服务
        Intent service = new Intent(this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        super.onDestroy();
    }


    /**
     * 初始化联系人
     */
    private void initData() {

        mTargetRoomJid = getIntent().getStringExtra(MultiChatActivity.F_ROOM_JID);
        mTargetRoomName = getIntent().getStringExtra(MultiChatActivity.F_ROOM_NAME);
        mMultiUserChat = AsmackUtils.getMultiUserChat(mTargetRoomJid);

        setOrUpdateAdapter();
    }

    /**
     * 设置Adapter
     */
    private void setOrUpdateAdapter() {
        //判断mAdapter是否存在
        if(mAdapter!=null){
            return;
        }
        //开启线程同步花名册
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                if(IMService.mCurAccount==null){
                    return;
                }
                String sqlWhere = " owner=? ";
                String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
                String sqlOrder = ContactsDbHelper.ContactTable.PINYIN+" asc";
                mCursor = getContentResolver().query(ContactsProvider.URI_CONTACT, null, sqlWhere, sqlWhereArgs, sqlOrder);
                if(mCursor ==null){
                    return;
                }
                if(mCursor.getCount()<=0){
                    return;
                }
                //UI线程中
                //设置adapter，显示数据
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new MyCursorAdapter(MyFriendsListActivity.this,mCursor);
                        //**
                        indexer = new AlphabetIndexer(mCursor, mCursor.getColumnIndex(ContactsDbHelper.ContactTable.PINYIN), alphabet);
                        //**
                        mAdapter.setIndexer(indexer);
                        if (mCursor.getCount() > 0) {
                            setupContactsListView();
                        }
                    }
                });
            }
        });
    }
    /**
     * 为联系人ListView设置监听事件，根据当前的滑动状态来改变分组的显示位置，从而实现挤压动画的效果。
     */
    private void setupContactsListView() {
        mLv.setAdapter(mAdapter);
        mLv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(totalItemCount>0){
                    int section = indexer.getSectionForPosition(firstVisibleItem);
                    int nextSecPosition = indexer.getPositionForSection(section + 1);
                    if (firstVisibleItem != lastFirstVisibleItem) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                        params.topMargin = 0;
                        titleLayout.setLayoutParams(params);
                        title.setText(String.valueOf(alphabet.charAt(section)));
                    }
                    if (nextSecPosition == firstVisibleItem + 1) {
                        View childView = view.getChildAt(0);
                        if (childView != null) {
                            int titleHeight = titleLayout.getHeight();
                            int bottom = childView.getBottom();
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout
                                    .getLayoutParams();
                            if (bottom < titleHeight) {
                                float pushedDistance = bottom - titleHeight;
                                params.topMargin = (int) pushedDistance;
                                titleLayout.setLayoutParams(params);
                            } else {
                                if (params.topMargin != 0) {
                                    params.topMargin = 0;
                                    titleLayout.setLayoutParams(params);
                                }
                            }
                        }
                    }
                    lastFirstVisibleItem = firstVisibleItem;
                }
            }
        });
    }




    /**
     * 分组挤压适配器
     */
    private class MyCursorAdapter extends CursorAdapter {
        /**
         * 字母表分组工具
         */
        private SectionIndexer mIndexer;
        public MyCursorAdapter(Context context, Cursor mCursor) {
            super(context, mCursor);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {}
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView =  View.inflate(MyFriendsListActivity.this, R.layout.item_layout_select_friends, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                //holder赋值
                holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.tv_pinyinf = (TextView) convertView.findViewById(R.id.tv_pinyinf);
                holder.iv_avater = (ImageView) convertView.findViewById(R.id.iv_avater);
                holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
                holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
                holder.sortKeyLayout = (LinearLayout) convertView.findViewById(R.id.ll_item_layout_sort_key);
                holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
                holder.item1 = (LinearLayout) convertView.findViewById(R.id.item1);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mCursor.moveToPosition(position);
            final String account = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.ACCOUNT));
            String nickname = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.NICKNAME));
            String pinyinf = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.PINYIN));
            String status = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.STATUS));
            String avatarurl = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.AVATARURL));

            if(status==null || status.equals("")){
                status = getResources().getString(R.string.offline);
            }else if(status.equals(Presence.Type.available.name())){
                status = getResources().getString(R.string.online);
            }else {
                status = getResources().getString(R.string.offline);
            }

            holder.tv_nickname.setText(nickname);
            holder.tv_status.setText(status);
            if(status.equals("离线")){
                holder.tv_status.setTextColor(getResources().getColor(R.color.colorBlackText));
            }else{
                holder.tv_status.setTextColor(getResources().getColor(R.color.colorSoftFunColor));
            }
            if(avatarurl==null) {
                avatarurl = "drawable://" + R.drawable.useravatar;
            }

            ImageLoader.getInstance().displayImage(avatarurl,holder.iv_avater, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());
            String vip = AsmackUtils.getFieldByAccountFromContactTable(account,ContactsDbHelper.ContactTable.VIP);
            if(vip==null || vip.equals("0")){
                holder.iv_vip.setVisibility(View.GONE);
            }else{
                holder.iv_vip.setVisibility(View.VISIBLE);
                holder.iv_vip.setImageResource(VipResouce.getVipResouce(vip));
            }
            //分组显示姓名的首字母
            // --getSectionForPosition
            // **是根据ListView的position来获取该位置上面的name的首字母char的ascii值，
            // **例如： 如果该position上面的name是阿妹，首字母就是A，那么此方法返回的就是'A'字母的ascii值，也就是65， 'B'是66，依次类推
            // --getPositionForSection(int section)
            // **就是根据首字母的ascii值来获取在该ListView中第一次出现该首字母的位置，
            // **例如：从上面的效果图1中，如果section是66 ，也就是‘B’的ascii值，那么该方法返回的position就是2
            // **然后就是getView()方法，首先我们根据ListView的position调用getSectionForPosition(int position)
            // **来获取该位置上面name的首字母的ascii值,然后根据这个ascii值调用getPositionForSection(int section)来获取第一次出现该首字母的position，
            // **如果ListView的position 等于 根据这个ascii值调用getPositionForSection(int section)来获取第一次出现该首字母的position，则显示分类字母 否则隐藏

            int section = mIndexer.getSectionForPosition(position);
            if (position == mIndexer.getPositionForSection(section)) {
                holder.tv_pinyinf.setText(getSortKey(pinyinf));
                holder.sortKeyLayout.setVisibility(View.VISIBLE);
            } else {
                holder.sortKeyLayout.setVisibility(View.GONE);
            }

            //监听事件必须放在 赋值 之前，否则会错乱
            //如果listview的item布局内有 checkBox，那么itemOnClickListener是无法捕获到点击事件的，
            //只能针对每个Item的某个组件进行点击监听，或者取消checkBox的焦点属性
//            final String finalAvatarurl = avatarurl;
//            holder.item1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(selected.containsKey(position)){
//                        selected.remove(position);
//                        holder.cb.setChecked(false);
//                        removeUser(account,position);
//                    }else{
//                        selected.put(position,position);
//                        holder.cb.setChecked(true);
//                        addUser(finalAvatarurl,account,position);
//                    }
//                }
//            });



            if(selected.containsKey(position))
                holder.cb.setChecked(true);
            else
                holder.cb.setChecked(false);

            return super.getView(position, convertView, parent);
        }

        /**
         * 优雅的holder
         */
        class ViewHolder {
            public TextView tv_nickname;
            public TextView tv_pinyinf;
            public ImageView iv_avater;
            public TextView tv_status;
            public ImageView iv_vip;
            public LinearLayout sortKeyLayout;
            public CheckBox cb;
            public LinearLayout item1;
        }

        /**
         * 给当前适配器传入一个分组工具。
         *
         * @param indexer
         */
        public void setIndexer(SectionIndexer indexer) {
            mIndexer = indexer;
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                mCursor.moveToPosition(i);
                String sortStr = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.PINYIN));//list.get(i).getSortLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * 动态删除user头像
     */
    private void removeUser(String account,int position) {
        if(ll_container.getChildCount()>0){
            for (int i = 0; i < ll_container.getChildCount(); i++) {
                String tag = (String) ll_container.getChildAt(i).getTag();
                if(tag.equals(account)){
                    ll_container.removeViewAt(i);
                    if(position!=-1 && selected.containsKey(position)){
                        selected.remove(position);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 动态添加user头像
     * @param avatarurl
     */
    private void addUser(String avatarurl, final String account , final int position) {
        ImageView iv = new ImageView(this);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUser(account,position);
            }
        });
        LinearLayout.LayoutParams linearParams =new LinearLayout.LayoutParams(DisplayUtils.dip2px(this,48),DisplayUtils.dip2px(this,48)); //取控件textView当前的布局参数
        linearParams.setMargins(DisplayUtils.dip2px(this,5),DisplayUtils.dip2px(this,5),DisplayUtils.dip2px(this,5),DisplayUtils.dip2px(this,5));
        iv.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
        ImageLoader.getInstance().displayImage(avatarurl,iv, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());
        iv.setTag(account);
        ll_container.addView(iv);
        hscroll.post(new Runnable() {
            @Override
            public void run() {
                hscroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
    }


    /**
     * 获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
     *
     * @param sortKeyString
     *            数据库中读取出的sort key
     * @return 英文字母或者#
     */
    private String getSortKey(String sortKeyString) {
        String key = sortKeyString.substring(0, 1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        }
        return "#";
    }


    /**
     * 初始化监听器
     */
    private void initListener() {
        //listview点击事件监听
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(position);
                String account = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.ACCOUNT));
                String nickName = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.NICKNAME));
                String avatarurl = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.AVATARURL));
                //System.out.println(account+"  "+ nickName+"  "+avatarurl);
                if(selected.containsKey(position)){
                    selected.remove(position);
                    //holder.cb.setChecked(false);
                    removeUser(account,position);
                }else{
                    selected.put(position,account);
                    //holder.cb.setChecked(true);
                    addUser(avatarurl,account,position);
                }
                mAdapter.notifyDataSetChanged();
            }
        });



        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if(mAdapter!=null&& mCursor!=null && mCursor.getCount()>0){
                    //该字母首次出现的位置
                    int position = mAdapter.getPositionForSection(s.charAt(0));
                    if(position != -1){
                        mLv.setSelection(position);
                    }
                }
            }
        });

        btn.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.btn:
                if(selected!=null && selected.size()>0){
                    btn.setEnabled(false);
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            for (Map.Entry<Integer, String> entry : selected.entrySet()) {
                                //System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
                                String userjid = entry.getValue();
                                String status = mImService.getRosterStatus(userjid);
                                String msg = "您好，我是["+IMService.mCurNickName+"]，邀请您加入["+mTargetRoomName+"]聊天群组。";
                                //如果是离线的好友
                                if(status.equals(getResources().getString(R.string.offline))){
                                    //将群邀请信息保存在远程服务器，下次用户登录后，获取后删除。
                                    HttpUtil.okhttpPost_insertGroupOfflineInvite(AsmackUtils.filterGroupJid(mTargetRoomJid),IMService.mCurAccount,userjid,msg);
                                }else{
                                    //在线的话，直接邀请，发送消息
                                    AsmackUtils.Invite(mMultiUserChat,mTargetRoomJid,userjid,msg);
                                }
                            }
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }
                    });
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }






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
            //System.out.println("====================  sessionFragment onServiceConnected  =====================");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //System.out.println("==================== sessionFragment onServiceDisconnected  =====================");
        }
    }
}
