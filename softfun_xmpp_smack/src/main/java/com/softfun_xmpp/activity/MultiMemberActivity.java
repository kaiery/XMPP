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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.bean.GroupMemberBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMemberActivity extends AppCompatActivity {

    /**
     * 群Jid
     */
    private String mGroupJid;
    private ListView mLv;
    private Toolbar mToolbar;
    private BaseAdapter mAdapter;//适配器
    private List<GroupMemberBean> mList;
    private boolean isSelected ;
    private Map<Integer,String> selected = new HashMap<>();
    private String mMaster;
    private String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_member);

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
            //更新
            //new RefreshList().execute();
            return;
        }
        if (IMService.mGroupMemberMap == null || IMService.mGroupMemberMap.size()==0) {
            return;
        }
        mList = IMService.mGroupMemberMap.get(mGroupJid);
        mAdapter = new BaseAdapter() {
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
                    convertView = View.inflate(MultiMemberActivity.this,R.layout.item_layout_member, null);
                    holder = new ViewHolder();
                    holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                    holder.text = (TextView) convertView.findViewById(R.id.text);
                    holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
                    holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
                    convertView.setTag(holder);//绑定ViewHolder对象
                }else{
                    holder = (ViewHolder)convertView.getTag();
                }
                if(mMaster.equals(myUsername)){
                    holder.cb.setVisibility(View.VISIBLE);
                }else{
                    holder.cb.setVisibility(View.GONE);
                }

                if(selected.containsKey(position))
                    holder.cb.setChecked(true);
                else
                    holder.cb.setChecked(false);


                holder.text.setText(mList.get(position).getMaster().equals("1")?"群主":"成员");
                holder.nickname.setText(mList.get(position).getNickname());
                ImageLoader.getInstance().displayImage(GlobalContext.getInstance().getResources().getString(R.string.app_server) + mList.get(position).getAvatarurl(),holder.avatar, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
                return convertView;
            }
            final class ViewHolder{
                public ImageView avatar;
                public TextView nickname;
                public TextView text;
                public CheckBox cb;
            }
        };
        mLv.setAdapter(mAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete_item:
                if (isSelected) {
                    for (Map.Entry<Integer, String> entry : selected.entrySet()) {
                        String kickedUsername = entry.getValue();
                        AsmackUtils.Kick(mGroupJid,kickedUsername);
                    }
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
        if (isSelected) {
            inflater.inflate(R.menu.common_listitem_menu_delete, menu);
        } else {

        }
        return true;
    }


    private void initListener() {
        if(mMaster.equals(myUsername)){
            mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String account = mList.get(position).getAccount();
                    if(!(mMaster).equals(account)){
                        if(selected.containsKey(position)){
                            selected.remove(position);
                        }else{
                            selected.put(position,account);
                        }
                        isSelected = selected.size() > 0;
                        invalidateOptionsMenu();
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

}
