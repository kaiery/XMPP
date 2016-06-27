package com.softfun_xmpp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.bean.GroupMemberBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;

import java.util.List;

public class SelectOneMemberActivity extends AppCompatActivity {

    /**
     * 群Jid
     */
    private String mGroupJid;
    private ListView mLv;
    private Toolbar mToolbar;
    private myBaseAdapter mAdapter;//适配器
    private List<GroupMemberBean> mList;
    private String mMaster;
    private String myUsername;
    private String selectAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_one_member);

        mGroupJid = AsmackUtils.filterGroupJid(getIntent().getStringExtra(Const.GROUP_JID));
        mMaster = getIntent().getStringExtra(Const.GROUP_FIELD_MASTER);
        mMaster = mMaster.substring(0,mMaster.lastIndexOf("@"));
        myUsername = AsmackUtils.filterAccountToUserName(IMService.mCurAccount);
        mLv = (ListView) findViewById(R.id.lv);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("群成员");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setAdapterOrNotify();

        initListener();
    }


    private void setAdapterOrNotify() {
        if (mAdapter != null) {
            return;
        }
        if (IMService.mGroupMemberMap == null || IMService.mGroupMemberMap.size()==0) {
            return;
        }
        mList = IMService.mGroupMemberMap.get(mGroupJid);

        mAdapter = new myBaseAdapter();
        mLv.setAdapter(mAdapter);
    }


    public class myBaseAdapter extends BaseAdapter{

        public int selectedIndex;

        public void setSelectedIndex(int position) {
            this.selectedIndex = position;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(SelectOneMemberActivity.this,R.layout.item_layout_select_one, null);
                holder = new ViewHolder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
                holder.rb = (RadioButton) convertView.findViewById(R.id.rb);
                convertView.setTag(holder);//绑定ViewHolder对象
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            holder.rb.setClickable(false);

            selectAccount = mList.get(position).getAccount();

            if(mMaster.equals(myUsername)){
                holder.rb.setVisibility(View.VISIBLE);
            }else{
                holder.rb.setVisibility(View.GONE);
            }

            if(selectedIndex == position) {
                holder.rb.setChecked(true);
            }else{
                holder.rb.setChecked(false);
            }


            holder.text.setText(mList.get(position).getMaster().equals("1")?"群主":"成员");
            holder.nickname.setText(mList.get(position).getNickname());
            ImageLoader.getInstance().displayImage(GlobalContext.getInstance().getResources().getString(R.string.app_server) + mList.get(position).getAvatarurl(),holder.avatar, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
            return convertView;
        }
        final class ViewHolder{
            public ImageView avatar;
            public TextView nickname;
            public TextView text;
            public RadioButton rb;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_ok_item:
                if(selectAccount!=null){
                    String oldMaster = AsmackUtils.filterAccountToUserName(IMService.mCurAccount) ;
                    AsmackUtils.changeGroupMaster(oldMaster,selectAccount,mGroupJid);
                    finish();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if (selectAccount!=null) {
            inflater.inflate(R.menu.common_listitem_menu_ok, menu);
        } else {

        }
        return true;
    }


    private void initListener() {
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectAccount = mList.get(position).getAccount();
                invalidateOptionsMenu();
                mAdapter.setSelectedIndex(position);
            }
        });
    }
}
