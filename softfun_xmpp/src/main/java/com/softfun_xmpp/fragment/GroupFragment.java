package com.softfun_xmpp.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.activity.MultiChatActivity;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.dbhelper.GroupDbHelper;
import com.softfun_xmpp.provider.GroupProvider;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;

/**
 * 群组的Fragment
 */
public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private View mFragmentView;
    private ListView mLv;
    private LinearLayout mNomsg;

    //private List<GroupBean> mGroupList;
    private MyGroupCursorAdapter mAdapter;
    private LoaderManager manager = null;

    public GroupFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mFragmentView) {
            mFragmentView = inflater.inflate(R.layout.fragment_group, container, false);
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
     *  初始服务等
     */
    private void init() {
        registerContentObserver();
    }

    @Override
    public void onDestroy() {
        unRegisterContentObserver();
        super.onDestroy();
    }

    /**
     * 初始化组件
     * @param view
     */
    private void initView(View view) {
        mLv = (ListView) view.findViewById(R.id.lv);
        mNomsg = (LinearLayout) view.findViewById(R.id.ll_nomsg);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String room_jid = mAdapter.getPositionValue(position, GroupDbHelper.GroupTable.JID);
                    String room_name = AsmackUtils.filterGroupName(mAdapter.getPositionValue(position, GroupDbHelper.GroupTable.JID)) ;
                    String room_avatarurl = mAdapter.getPositionValue(position, GroupDbHelper.GroupTable.FACE);
                    Intent intent = new Intent(getContext(), MultiChatActivity.class);
                    intent.putExtra(MultiChatActivity.F_ROOM_JID,room_jid);
                    intent.putExtra(MultiChatActivity.F_ROOM_NAME,room_name);
                    intent.putExtra(MultiChatActivity.F_ROOM_AVATARURL,room_avatarurl);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //AsmackUtils.getHostRooms();
        //System.out.println("====================    =============================    =============================    =============================    =============================    =============================    =============================    =============================    =============================    =====================");
        //setOrUpdateAdapter();



        //先给mAdapter构造方法内一个空的cursor
        mAdapter = new MyGroupCursorAdapter(getContext(),null,true);
        mLv.setAdapter(mAdapter);
        //初始化加载器管理器
        manager = getLoaderManager();
        manager.initLoader(0,null,GroupFragment.this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sqlWhere = " owner=? ";
        String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
        String sqlOrder = GroupDbHelper.GroupTable.PINYIN+" asc ";
        return new CursorLoader(getContext(), GroupProvider.URI_GROUP, null, sqlWhere, sqlWhereArgs, sqlOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null){
            if(data.getCount()>0){
                mNomsg.setVisibility(View.GONE);
                mAdapter.swapCursor(data);
            }else{
                mNomsg.setVisibility(View.VISIBLE);
                mAdapter.swapCursor(null);
            }
        }else{
            mNomsg.setVisibility(View.VISIBLE);
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mAdapter!=null)
            mAdapter.swapCursor(null);
    }


//    //步骤1：通过后台线程AsyncTask来读取数据库，放入更换Cursor
//    private class RefreshList extends AsyncTask<Void, Void ,Void> {
//        protected Void doInBackground(Void... params) {
//            Cursor mCursor;
//            String sqlWhere = " owner=? ";
//            String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
//            String sqlOrder = GroupDbHelper.GroupTable.PINYIN+" asc ";
//            mCursor = getActivity().getContentResolver().query(GroupProvider.URI_GROUP, null, sqlWhere, sqlWhereArgs, sqlOrder);
//            if(mCursor!=null){
//                if(mGroupList!=null){
//                    mGroupList.clear();
//                }else{
//                    mGroupList = new ArrayList<>();
//                }
//                for (int i = 0; i <mCursor.getCount() ; i++) {
//                    mCursor.moveToPosition(i);
//                    GroupBean groupBean = new GroupBean();
//                    groupBean.setChildid(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.JID)) );
//                    groupBean.setChild(AsmackUtils.filterGroupName(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.JID))) );
//                    groupBean.setGroupnum(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.ROOMNUM)) );
//                    groupBean.setGroupface(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.FACE)) );
//                    groupBean.setLvl(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.LVL)) );
//                    mGroupList.add(groupBean);
//                }
//                mCursor.close();
//            }else{
//            }
//            return null;
//        }
//
//        protected void onPostExecute(Void aVoid) {
//            if(mGroupList.size()>0){
//                mNomsg.setVisibility(View.GONE);
//                mAdapter.notifyDataSetChanged();
//            }else {
//                mNomsg.setVisibility(View.VISIBLE);
//                mAdapter.notifyDataSetChanged();
//            }
//        }
//    }

    /**
     * 更新或插入列表数据
     */
    private void setOrUpdateAdapter() {
        System.out.println("====================  manager.restartLoader  GroupFragment =====================");
        manager.restartLoader(0,null,this);


//        //判断mAdapter是否存在
//        if(mAdapter!=null){
//            //更新mAdapter
//            new RefreshList().execute();
//            return;
//        }
//        ThreadUtils.runInThread(new Runnable() {
//            @Override
//            public void run() {
//                Cursor mCursor;
//                String sqlWhere = " owner=? ";
//                String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
//                String sqlOrder = GroupDbHelper.GroupTable.PINYIN+" asc ";
//                mCursor = getActivity().getContentResolver().query(GroupProvider.URI_GROUP, null, sqlWhere, sqlWhereArgs, sqlOrder);
//                if(mCursor!=null){
//                    if(mGroupList!=null){
//                        mGroupList.clear();
//                    }else{
//                        mGroupList = new ArrayList<>();
//                    }
//                    for (int i = 0; i <mCursor.getCount() ; i++) {
//                        mCursor.moveToPosition(i);
//                        GroupBean groupBean = new GroupBean();
//                        groupBean.setChildid(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.JID)) );
//                        groupBean.setChild(AsmackUtils.filterGroupName(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.JID))) );
//                        groupBean.setGroupnum(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.ROOMNUM)) );
//                        groupBean.setGroupface(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.FACE)) );
//                        groupBean.setLvl(mCursor.getString(mCursor.getColumnIndex(GroupDbHelper.GroupTable.LVL)) );
//                        mGroupList.add(groupBean);
//                    }
//                    mCursor.close();
//                }else{
//                    if(mGroupList==null){
//                        mGroupList = new ArrayList<>();
//                    }
//                }
//
//                ThreadUtils.runInUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(mGroupList.size()>0){
//                            mNomsg.setVisibility(View.GONE);
//                        }else {
//                            mNomsg.setVisibility(View.VISIBLE);
//                        }
//                        mAdapter = new MyGroupAdapter(mGroupList, getContext());
//                        mLv.setAdapter(mAdapter);
//                        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                try {
//                                    Intent intent = new Intent(getContext(), MultiChatActivity.class);
//                                    intent.putExtra(MultiChatActivity.F_ROOM_JID,mGroupList.get(position).getChildid());
//                                    intent.putExtra(MultiChatActivity.F_ROOM_NAME,mGroupList.get(position).getChild());
//                                    intent.putExtra(MultiChatActivity.F_ROOM_AVATARURL,mGroupList.get(position).getGroupface());
//                                    startActivity(intent);
//                                } catch (Exception e) {
//                                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//                    }
//                });
//            }
//        });
    }




    private class MyGroupCursorAdapter extends CursorAdapter {

        public final class ViewHolder{
            public TextView tv;
            public ImageView iv;
        }

        public MyGroupCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = View.inflate(mContext, R.layout.item_layout_group, null);

            ViewHolder holder  =   new ViewHolder();
            //得到各个控件的对象
            holder.tv = (TextView) view.findViewById(R.id.tv_item_groupname);
            holder.iv = (ImageView) view.findViewById(R.id.item_list_iv);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder)view.getTag();

            //设置TextView显示的内容，即我们存放在动态数组中的数据
            holder.tv.setText(AsmackUtils.filterGroupName(cursor.getString(cursor.getColumnIndex(GroupDbHelper.GroupTable.JID))) );
            //群头像
            String groupface = cursor.getString(cursor.getColumnIndex(GroupDbHelper.GroupTable.FACE));//mData.get(position).getGroupface();
            if(groupface==null || groupface.equals("")) {
                groupface = "drawable://" + R.drawable.groupface0;
            }
            ImageLoader.getInstance().displayImage(groupface,holder.iv, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular());
        }


        public String getPositionValue(int position,String field){
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex( field ));
        }
    }


    //---------------内容观察者---------------//
    GroupContentObserver observer = new GroupContentObserver(new Handler());
    /**
     * 注册内容观察者
     */
    public void registerContentObserver(){
        //System.out.println("注册内容观察者");
        //第2个参数为true：
        //content://" + AUTHORITIES + "/contact  的孩子 content://" + AUTHORITIES + "/contact/xxxx 也会被通知
        getActivity().getContentResolver().registerContentObserver(GroupProvider.URI_GROUP,true,observer);
    }

    /**
     * 销毁内容观察者
     */
    public void unRegisterContentObserver(){
        //System.out.println("销毁内容观察者");
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }


    /**
     * 群组的内容的观察者类。
     */
    public class GroupContentObserver extends ContentObserver {
        /**
         * 创建一个内容的观察者。
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public GroupContentObserver(Handler handler) {
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

}
