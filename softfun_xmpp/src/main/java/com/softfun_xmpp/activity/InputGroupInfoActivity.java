package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aigestudio.wheelpicker.core.AbstractWheelDecor;
import com.aigestudio.wheelpicker.core.AbstractWheelPicker;
import com.aigestudio.wheelpicker.view.WheelCurvedPicker;
import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;
import com.softfun_xmpp.utils.ToolsUtil;

import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.List;

public class InputGroupInfoActivity extends AppCompatActivity implements View.OnClickListener {


    private int mGrouptype;


    private Toolbar mToolbar;
    private ImageView mIv;
    private LinearLayout mItem1;
    private LinearLayout mItem2;
    private Button mBtn;
    private TextView mTvCancel;
    private TextView mTvBody;
    private TextView mTvOk;
    private LinearLayout mWpgroup1;
    private WheelCurvedPicker mWp1;
    private WheelCurvedPicker mWp2;
    private WheelCurvedPicker mWp3;
    private LinearLayout mWpgroup2;
    private WheelCurvedPicker mWp4;
    private WheelCurvedPicker mWp5;
    private LinearLayout mLl;
    private TextView mTvCity;
    private TextView mTvGame;
    private EditText mEtGroupName;

    /**
     * 省份数组（省份+代码）
     */
    private List<String> mProvinceList;
    /**
     * 省份数组（只有省份）
     */
    private List<String> mProvinceNameList;
    /**
     * 已选择的省份名称
     */
    private String mSelectedProvince;
    /**
     * 已选择的省份代码
     */
    private String mSelectedProvinceCode;
    /**
     * 城市数组（城市+代码）
     */
    private List<String> mCityList;
    /**
     * 城市数组（只有城市）
     */
    private List<String> mCityNameList;
    /**
     * 已选择的城市名称
     */
    private String mSelectedCity;
    /**
     * 已选择的城市代码
     */
    private String mSelectedCityCode;
    /**
     * 区县数组（区县+代码）
     */
    private List<String> mAreaList;
    /**
     * 区县数组（只有区县）
     */
    private List<String> mAreaNameList;
    /**
     * 已选择的区县名称
     */
    private String mSelectedArea;
    /**
     * 已选择的区县代码
     */
    private String mSelectedAreaCode;

    /**
     * 兴趣组数组（名称+代码）
     */
    private List<String> mInterestList0;
    /**
     * 兴趣组数组（名称）
     */
    private List<String> mInterestNameList0;
    /**
     * 已选择的兴趣组名称
     */
    private String mSelectedInterest0;
    /**
     * 已选择的兴趣组代码
     */
    private String mSelectedInterestCode0;
    /**
     * 兴趣数组（名称+代码）
     */
    private List<String> mInterestList1;
    /**
     * 兴趣数组（名称）
     */
    private List<String> mInterestNameList1;
    /**
     * 已选择的兴趣名称
     */
    private String mSelectedInterest1;
    /**
     * 已选择的兴趣代码
     */
    private String mSelectedInterestCode1;
    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mIv = (ImageView) findViewById(R.id.iv);
        mItem1 = (LinearLayout) findViewById(R.id.item1);
        mItem2 = (LinearLayout) findViewById(R.id.item2);
        mBtn = (Button) findViewById(R.id.btn);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvBody = (TextView) findViewById(R.id.tv_body);
        mTvOk = (TextView) findViewById(R.id.tv_ok);
        mWpgroup1 = (LinearLayout) findViewById(R.id.wpgroup1);
        mWp1 = (WheelCurvedPicker) findViewById(R.id.wp1);
        mWp2 = (WheelCurvedPicker) findViewById(R.id.wp2);
        mWp3 = (WheelCurvedPicker) findViewById(R.id.wp3);
        mWpgroup2 = (LinearLayout) findViewById(R.id.wpgroup2);
        mWp4 = (WheelCurvedPicker) findViewById(R.id.wp4);
        mWp5 = (WheelCurvedPicker) findViewById(R.id.wp5);
        mLl = (LinearLayout) findViewById(R.id.ll_group1);
        mTvCity = (TextView) findViewById(R.id.tv_city);
        mTvGame = (TextView) findViewById(R.id.tv_game);
        mEtGroupName = (EditText) findViewById(R.id.et_groupname);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_group_info);

        init();

        assignViews();

        mToolbar.setTitle("填写群信息");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }


        mLl.setVisibility(View.GONE);

        mItem1.setOnClickListener(this);
        mItem2.setOnClickListener(this);
        mTvOk.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        mBtn.setOnClickListener(this);


        mSelectedProvinceCode = "";
        mSelectedCityCode = "";
        mSelectedAreaCode = "";

        mSelectedInterestCode0 = "";
        mSelectedInterestCode1 = "";

        Intent intent = getIntent();
        mGrouptype = intent.getIntExtra(Const.GROUPTYPE, 0);
        switch (mGrouptype) {
            case Const.GROUP_NORMAL:
                mTvBody.setText("");
                mIv.setImageResource(R.drawable.group_chat_ground_normal);
                mItem1.setVisibility(View.GONE);
                mItem2.setVisibility(View.GONE);
                break;
            case Const.GROUP_GAME:
                mTvBody.setText("选择兴趣爱好");
                mIv.setImageResource(R.drawable.group_chat_ground_game);
                mItem1.setVisibility(View.VISIBLE);
                mItem2.setVisibility(View.GONE);
                mWp4.setOnWheelChangeListener(new wp45ChangeListener(0));
                mWp4.setWheelDecor(true, new MyAbstractWheelDecor());
                mWp5.setOnWheelChangeListener(new wp45ChangeListener(1));
                mWp5.setWheelDecor(true, new MyAbstractWheelDecor());


                initInterest();
                break;
            case Const.GROUP_CITY:
                mTvBody.setText("选择城市");
                mIv.setImageResource(R.drawable.group_chat_ground_city);
                mItem1.setVisibility(View.GONE);
                mItem2.setVisibility(View.VISIBLE);
                mWp1.setOnWheelChangeListener(new wp123ChangeListener(0));
                mWp1.setWheelDecor(true, new MyAbstractWheelDecor());
                mWp2.setOnWheelChangeListener(new wp123ChangeListener(1));
                mWp2.setWheelDecor(true, new MyAbstractWheelDecor());
                mWp3.setOnWheelChangeListener(new wp123ChangeListener(2));
                mWp3.setWheelDecor(true, new MyAbstractWheelDecor());
                initProvince();
                break;
            case Const.GROUP_SECRET:
                mTvBody.setText("");
                mIv.setImageResource(R.drawable.group_chat_ground_sercet);
                mItem1.setVisibility(View.GONE);
                mItem2.setVisibility(View.GONE);
                break;
        }
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
     * 初始化兴趣数据
     */
    private void initInterest() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mInterestList0 = HttpUtil.okhttpPost_queryInterest("", 0);
                if (mInterestNameList0 == null) {
                    mInterestNameList0 = new ArrayList<String>();
                } else {
                    mInterestNameList0.clear();
                }
                mInterestNameList0 = ToolsUtil.fiterList(mInterestList0);
                mWp4.setData(mInterestNameList0);
            }
        });
    }

    /**
     * 查询兴趣
     *
     * @param index
     */
    private void getInterest1(final String index) {
        if (!TextUtils.isEmpty(index)) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    mInterestList1 = HttpUtil.okhttpPost_queryInterest(index, 1);
                    if (mInterestList1 != null) {
                        if (mInterestNameList1 == null) {
                            mInterestNameList1 = new ArrayList<String>();
                        } else {
                            mInterestNameList1.clear();
                        }
                        mInterestNameList1 = ToolsUtil.fiterList(mInterestList1);
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWp5.setData(mInterestNameList1);
                                mWp5.setItemIndex(0);
                                mWp5.checkScrollState();
                                mWp5.invalidate();
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 初始化省份数据
     */
    private void initProvince() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mProvinceList = HttpUtil.okhttpPost_queryAreaInfo("", 0);
                if (mProvinceNameList == null) {
                    mProvinceNameList = new ArrayList<String>();
                } else {
                    mProvinceNameList.clear();
                }
                mProvinceNameList = ToolsUtil.fiterList(mProvinceList);
                mWp1.setData(mProvinceNameList);
            }
        });
    }

    /**
     * 获取城市
     *
     * @param index
     */
    private void getCity(final String index) {
        if (!TextUtils.isEmpty(index)) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    mCityList = HttpUtil.okhttpPost_queryAreaInfo(index, 1);
                    if (mCityList != null) {
                        if (mCityNameList == null) {
                            mCityNameList = new ArrayList<String>();
                        } else {
                            mCityNameList.clear();
                        }
                        mCityNameList = ToolsUtil.fiterList(mCityList);
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWp2.setData(mCityNameList);
                                mWp2.setItemIndex(0);
                                mWp2.checkScrollState();
                                mWp2.invalidate();
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 获取区县
     *
     * @param index
     */
    private void getArea(final String index) {
        if (!TextUtils.isEmpty(index)) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    mAreaList = HttpUtil.okhttpPost_queryAreaInfo(index, 2);
                    if (mAreaList != null) {
                        if (mAreaNameList == null) {
                            mAreaNameList = new ArrayList<String>();
                        } else {
                            mAreaNameList.clear();
                        }
                        mAreaNameList = ToolsUtil.fiterList(mAreaList);
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWp3.setData(mAreaNameList);
                                mWp3.setItemIndex(0);
                                mWp3.checkScrollState();
                                mWp3.invalidate();
                            }
                        });
                    }
                }
            });
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item1:
                mLl.setVisibility(View.VISIBLE);
                mWpgroup2.setVisibility(View.VISIBLE);
                mWpgroup1.setVisibility(View.GONE);
                break;
            case R.id.item2:
                mLl.setVisibility(View.VISIBLE);
                mWpgroup1.setVisibility(View.VISIBLE);
                mWpgroup2.setVisibility(View.GONE);
                break;
            case R.id.tv_ok:
                mLl.setVisibility(View.GONE);
                if (mGrouptype == Const.GROUP_CITY) {
                    mTvCity.setText(mSelectedProvince + " " + mSelectedCity + " " + mSelectedArea);
                    //System.out.println("====================    ===================== " + mSelectedProvinceCode + "," + mSelectedCityCode + "," + mSelectedAreaCode);
                }
                if (mGrouptype == Const.GROUP_GAME) {
                    mTvGame.setText(mSelectedInterest0 + " " + mSelectedInterest1);
                    //System.out.println("====================    ===================== " + mSelectedInterestCode0 + "," + mSelectedInterestCode1);
                }
                break;
            case R.id.tv_cancel:
                mLl.setVisibility(View.GONE);
                break;
            case R.id.btn:
                CreateGroup();
                break;
        }
    }

    /**
     * 创建群组
     */
    private void CreateGroup() {
        if (!TextUtils.isEmpty(mEtGroupName.getText().toString())) {
            if (mGrouptype == Const.GROUP_CITY) {
                if (TextUtils.isEmpty(mSelectedProvinceCode) || TextUtils.isEmpty(mSelectedCityCode) || TextUtils.isEmpty(mSelectedAreaCode)) {
                    ToastUtils.showToastSafe("请选择城市地区");
                    return;
                }
            }
            if (mGrouptype == Const.GROUP_GAME) {
                if (TextUtils.isEmpty(mSelectedInterestCode0) || TextUtils.isEmpty(mSelectedInterestCode1)) {
                    ToastUtils.showToastSafe("请选择兴趣爱好");
                    return;
                }
            }

            mBtn.setEnabled(false);

            //创建群组
            final String roomName = AsmackUtils.createRoom("", mEtGroupName.getText().toString(), "123456");
            if (roomName != null) {
                //加入群组
                final MultiUserChat multiUserChat = AsmackUtils.joinMultiUserChat(IMService.mCurNickName, roomName, "123456");
                if (multiUserChat != null) {
                    //写数据库，保存已加入的群信息
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            //添加成员
                            //multiUserChat.grantMembership("肖群浩@softfun");
                            //String nickname =  multiUserChat.getNickname();
                            //群主
                            //Collection<Affiliate> owners = multiUserChat.getOwners();
                            //成员
                            //Collection<Affiliate> members = multiUserChat.getMembers();
                            //住客
                            //Iterator<String> it = multiUserChat.getOccupants();
                            // 遍历出聊天室人员名称
                            //while (it.hasNext()) {
                            //// 聊天室成员名字
                            //String name = it.next();//StringUtils.parseResource(it.next());
                            //System.out.println("住客："+name);
                            //}
                            //roomjid
                            //String roomJid = multiUserChat.getRoom();//范德萨方式#a37190c2-e810-4021-85a3-51fe9aeca870@conference.softfun
                            String username = IMService.mCurAccount.substring(0, IMService.mCurAccount.lastIndexOf("@"));
                            Integer code = HttpUtil.okhttpPost_insertGroup(roomName,
                                    username,
                                    mGrouptype + "",
                                    mSelectedInterestCode0 + "," + mSelectedInterestCode1,
                                    mSelectedProvinceCode + "," + mSelectedCityCode + "," + mSelectedAreaCode);
                            if (code == 1) {
                                mImService.InitMultiRoom();
                                //操作成功
                                ThreadUtils.runInUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                            }else{
                                ThreadUtils.runInUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBtn.setEnabled(true);
                                    }
                                });
                            }
                        }
                    });
                }else{
                    mBtn.setEnabled(true);
                }
            }else{
                mBtn.setEnabled(true);
            }
        }
    }


    /**
     * 省市区滚动监听
     */
    private class wp123ChangeListener implements AbstractWheelPicker.OnWheelChangeListener {
        private int mLevel;

        public wp123ChangeListener(int level) {
            mLevel = level;
        }

        @Override
        public void onWheelScrolling(float deltaX, float deltaY) {

        }

        @Override
        public void onWheelSelected(int index, String data) {
            if (mLevel == 0) {
                mSelectedProvince = data;
                mSelectedProvinceCode = ToolsUtil.getFiterListId(mProvinceList, index);
                getCity(mSelectedProvinceCode);
            } else if (mLevel == 1) {
                mSelectedCity = data;
                mSelectedCityCode = ToolsUtil.getFiterListId(mCityList, index);
                getArea(mSelectedCityCode);
            } else if (mLevel == 2) {
                mSelectedArea = data;
                mSelectedAreaCode = ToolsUtil.getFiterListId(mAreaList, index);
            }
        }

        @Override
        public void onWheelScrollStateChanged(int state) {
            if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                mTvOk.setEnabled(false);
            } else {
                mTvOk.setEnabled(true);
            }
        }
    }


    /**
     * 兴趣组滚动监听
     */
    private class wp45ChangeListener implements AbstractWheelPicker.OnWheelChangeListener {
        private int mLevel;

        public wp45ChangeListener(int level) {
            mLevel = level;
        }

        @Override
        public void onWheelScrolling(float deltaX, float deltaY) {

        }

        @Override
        public void onWheelSelected(int index, String data) {
            if (mLevel == 0) {
                mSelectedInterest0 = data;
                mSelectedInterestCode0 = ToolsUtil.getFiterListId(mInterestList0, index);
                getInterest1(mSelectedInterestCode0);
            } else if (mLevel == 1) {
                mSelectedInterest1 = data;
                mSelectedInterestCode1 = ToolsUtil.getFiterListId(mInterestList1, index);
            }
        }

        @Override
        public void onWheelScrollStateChanged(int state) {
            if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                mTvOk.setEnabled(false);
            } else {
                mTvOk.setEnabled(true);
            }
        }
    }


    private class MyAbstractWheelDecor extends AbstractWheelDecor {
        @Override
        public void drawDecor(Canvas canvas, Rect rectLast, Rect rectNext, Paint paint) {
            canvas.drawColor(getResources().getColor(R.color.colorPrimaryAlpha));
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
