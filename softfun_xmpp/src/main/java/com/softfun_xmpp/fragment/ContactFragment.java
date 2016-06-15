package com.softfun_xmpp.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.activity.ChatActivity;
import com.softfun_xmpp.components.SideBar;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.dbhelper.ContactsDbHelper;
import com.softfun_xmpp.provider.ContactsProvider;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ToastUtils;
import com.softfun_xmpp.utils.VipResouce;

/**
 * 联系人的Fragment
 * <p/>
 * 所有联系人：Roster
 * 联系人组：RosterGroup
 * 单个联系人：RosterEntry
 */
public class ContactFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SideBar sideBar;
    private TextView dialog;
    private LinearLayout mNomsg;
    private ListView mLv;
    private LinearLayout titleLayout;
    private TextView title;
    private MyCursorAdapter mAdapter;
    private View mFragmentView;
    private LoaderManager manager = null;
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

    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;



    public ContactFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mFragmentView) {
            ((ViewGroup) mFragmentView.getParent()).removeView(mFragmentView);
        }
    }


    @Override
    public void onDestroy() {
        //销毁内容观察者
        unRegisterContentObserver();
        //解绑服务
        if (mMyServiceConnection != null) {
            getActivity().unbindService(mMyServiceConnection);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mFragmentView) {
            mFragmentView = inflater.inflate(R.layout.fragment_contact, container, false);
            initView(mFragmentView);
        }
        return mFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 初始化
     */
    private void init() {
        //注册内容观察者
        registerContentObserver();
        //绑定服务
        Intent service = new Intent(getActivity(), IMService.class);
        //绑定
        getActivity().bindService(service, mMyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    private void initView(View view) {
        mNomsg = (LinearLayout) view.findViewById(R.id.ll_nomsg);
        mLv = (ListView) view.findViewById(R.id.lv);
        titleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
        title = (TextView) view.findViewById(R.id.title);
        sideBar = (SideBar) view.findViewById(R.id.sidrbar);
        dialog = (TextView) view.findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
    }

    /**
     * 初始化数据
     * 得到所有联系人
     */
    private void initData() {
        //先给mAdapter构造方法内一个空的cursor
        mAdapter = new MyCursorAdapter(getActivity(),null);
        //indexer = new AlphabetIndexer(null, data.getColumnIndex(ContactsDbHelper.ContactTable.PINYIN), alphabet);
        //索引，4为cursor中拼音字段的位置序号
        indexer = new AlphabetIndexer(null,4,alphabet);
        //给adapter设置索引
        mAdapter.setIndexer(indexer);
        mLv.setAdapter(mAdapter);
        //为联系人ListView设置监听事件，根据当前的滑动状态来改变分组的显示位置，从而实现挤压动画的效果
        setupContactsListView();
        //初始化加载器管理器
        manager = getLoaderManager();
        //初始化Loader
        manager.initLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sqlWhere = " owner=? ";
        String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
        String sqlOrder = ContactsDbHelper.ContactTable.PINYIN+" asc";
        return new CursorLoader(getContext(),ContactsProvider.URI_CONTACT, null, sqlWhere, sqlWhereArgs, sqlOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null){
            if(data.getCount()>0){
                mNomsg.setVisibility(View.GONE);
                mAdapter.swapCursor(data);
                indexer.setCursor(data);
            }else{
                mNomsg.setVisibility(View.VISIBLE);
                mAdapter.swapCursor(null);
                indexer.setCursor(null);
            }
        }else{
            mNomsg.setVisibility(View.VISIBLE);
            mAdapter.swapCursor(null);
            indexer.setCursor(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mAdapter!=null)
            mAdapter.swapCursor(null);
        else
            System.out.println("====================   联系人的  mAdapter is null");
    }





//    //步骤1：通过后台线程AsyncTask来读取数据库，放入更换Cursor
//    private class RefreshList extends AsyncTask<Void, Void ,Cursor> {
//        //步骤1.1：在后台线程中从数据库读取，返回新的游标newCursor
//        protected Cursor doInBackground(Void... params) {
//            //System.out.println("newCursor的数量："+newCursor.getCount());
//            String sqlWhere = " owner=? ";
//            String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
//            String sqlOrder = ContactsDbHelper.ContactTable.PINYIN+" asc";
//            Cursor newCursor = new ContactsDbHelper(getActivity()).querytable(sqlWhere,sqlWhereArgs,sqlOrder);
//            return newCursor;
//        }
//        //步骤1.2：线程最后执行步骤，更换adapter的游标，并奖原游标关闭，释放资源
//        protected void onPostExecute(Cursor newCursor) {
//            Cursor oldCursor = mAdapter.swapCursor(newCursor);
//            oldCursor.close();
//            //**
//            indexer = new AlphabetIndexer(newCursor, newCursor.getColumnIndex(ContactsDbHelper.ContactTable.PINYIN), alphabet);
//            //**
//            mAdapter.setIndexer(indexer);
//            if(newCursor.getCount()>0){
//                mNomsg.setVisibility(View.GONE);
//            }else{
//                mNomsg.setVisibility(View.VISIBLE);
//            }
//        }
//    }
    /**
     * 设置或更新Adapter
     */
    private void setOrUpdateAdapter() {

        if(IMService.mCurAccount==null){
            return;
        }
        System.out.println("====================  manager.restartLoader  =====================");
        manager.restartLoader(0,null,this);
    }

    /**
     * 为联系人ListView设置监听事件，根据当前的滑动状态来改变分组的显示位置，从而实现挤压动画的效果。
     */
    private void setupContactsListView() {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView =  View.inflate(getContext(), R.layout.item_layout_fragment_friends, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                //holder赋值
                holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.tv_pinyinf = (TextView) convertView.findViewById(R.id.tv_pinyinf);
                holder.iv_avater = (ImageView) convertView.findViewById(R.id.iv_avater);
                holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
                holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
                holder.sortKeyLayout = (LinearLayout) convertView.findViewById(R.id.ll_item_layout_sort_key);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mCursor.moveToPosition(position);
            String account = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.ACCOUNT));
            String nickname = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.NICKNAME));
            String pinyinf = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.PINYIN));
            String status = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.STATUS));
            String avatarurl = mCursor.getString(mCursor.getColumnIndex(ContactsDbHelper.ContactTable.AVATARURL));

            if(status==null){
                status = getResources().getString(R.string.offline);
            }
            holder.tv_nickname.setText(nickname);
            holder.tv_status.setText(status);
            if(status.equals(getResources().getString(R.string.offline))){
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

        public String getPositionValue(int position,String field){
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex( field ));
        }

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
                try {
                    String account = mAdapter.getPositionValue(position,ContactsDbHelper.ContactTable.ACCOUNT);//cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.ACCOUNT));
                    String nickName = mAdapter.getPositionValue(position,ContactsDbHelper.ContactTable.NICKNAME);;//cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.NICKNAME));
                    String avatarurl = mAdapter.getPositionValue(position,ContactsDbHelper.ContactTable.AVATARURL);;//cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.AVATARURL));
                    if(mImService.isMyFriends(account)){
                        Intent intent = new Intent(getActivity(),ChatActivity.class);
                        intent.putExtra(ChatActivity.F_ACCOUNT,account);
                        intent.putExtra(ChatActivity.F_NICKNAME,nickName);
                        intent.putExtra(ChatActivity.F_AVATARURL,avatarurl);
                        startActivity(intent);
                    }else{
                        ToastUtils.showToastSafe("您的好友申请，对方还没有确认通过。");
                    }
                } catch (Exception e) {
                    ToastUtils.showToastSafe(e.getMessage());
                }
            }
        });



        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if(mAdapter!=null&& mAdapter.getCursor()!=null && mAdapter.getCursor().getCount()>0){
                    //该字母首次出现的位置
                    int position = mAdapter.getPositionForSection(s.charAt(0));
                    if(position != -1){
                        mLv.setSelection(position);
                    }
                }
            }
        });


    }


















    //---------------内容观察者---------------//
    ContactContentObserver observer = new ContactContentObserver(new Handler());
    /**
     * 注册内容观察者
     */
    public void registerContentObserver(){
        //System.out.println("注册内容观察者");
        //第2个参数为true：
        //content://" + AUTHORITIES + "/contact  的孩子 content://" + AUTHORITIES + "/contact/xxxx 也会被通知
        getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT,true,observer);
    }

    /**
     * 销毁内容观察者
     */
    public void unRegisterContentObserver(){
        //System.out.println("销毁内容观察者");
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }


    /**
     * 联系人的内容的观察者类。
     */
    public class ContactContentObserver extends ContentObserver{
        /**
         * 创建一个内容的观察者。
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ContactContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 如果目标Uri中的内容发生改变就接收到通知
         * @param selfChange
         * @param uri
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if(IMService.isCreate){
                setOrUpdateAdapter();
            }
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
            System.out.println("====================  sessionFragment onServiceConnected  =====================");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("==================== sessionFragment onServiceDisconnected  =====================");
        }
    }



}
