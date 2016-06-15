package com.softfun_xmpp.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aigestudio.wheelpicker.core.AbstractWheelDecor;
import com.aigestudio.wheelpicker.core.AbstractWheelPicker;
import com.aigestudio.wheelpicker.view.WheelCurvedPicker;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.GroupBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMultiItemActivity extends AppCompatActivity implements View.OnClickListener {


    public static final String ITEM_NAME = "itemname";
    public static final String MAX_LENGTH = "max_lenght";
    public static final String ITEM_FIELD = "item_field";
    public static final String ITEM_VALUE = "item_value";

    public static final int RESULT_SUCCEED = 1;
    public static final int RESULT_FAILURE = 0;
    public static final int REQUESTCODE = 10;
    public static final String PAR_KEY = "par_key";

    public String mItemField = "";



    private Toolbar mToolbar;
    private TextView mTv;
    private Button mBtn;
    private EditText mEt;
    private LinearLayout wpgroup1;
    private WheelCurvedPicker wp1;
    private WheelCurvedPicker wp2;
    private WheelCurvedPicker wp3;
    private LinearLayout wpgroup2;
    private WheelCurvedPicker wp4;
    private WheelCurvedPicker wp5;
    private ScrollView vscroll;
    private LinearLayout item1;
    private LinearLayout item2;
    private LinearLayout item3;
    private LinearLayout item4;


    private GroupBean mGroupBean;


    private Intent resultData = new Intent();

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
     * 群类型
     */
    private String mSelectedGroupType;
    private Map<String, String> mMap;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTv = (TextView) findViewById(R.id.tv);
        mBtn = (Button) findViewById(R.id.btn);
        mEt = (EditText) findViewById(R.id.et);
        wpgroup1 = (LinearLayout) findViewById(R.id.wpgroup1);
        wp1 = (WheelCurvedPicker) findViewById(R.id.wp1);
        wp2 = (WheelCurvedPicker) findViewById(R.id.wp2);
        wp3 = (WheelCurvedPicker) findViewById(R.id.wp3);
        wpgroup2 = (LinearLayout) findViewById(R.id.wpgroup2);
        wp4 = (WheelCurvedPicker) findViewById(R.id.wp4);
        wp5 = (WheelCurvedPicker) findViewById(R.id.wp5);
        vscroll = (ScrollView) findViewById(R.id.vscroll);


        item1 = (LinearLayout) findViewById(R.id.item1);
        item2 = (LinearLayout) findViewById(R.id.item2);
        item3 = (LinearLayout) findViewById(R.id.item3);
        item4 = (LinearLayout) findViewById(R.id.item4);

        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        item3.setOnClickListener(this);
        item4.setOnClickListener(this);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_multi_item);

        assignViews();

        Intent intent = getIntent();
        mGroupBean = (GroupBean) intent.getParcelableExtra(PAR_KEY);
        String itemname = intent.getStringExtra(ITEM_NAME);
        mItemField = intent.getStringExtra(ITEM_FIELD);
        int mMaxLenght = intent.getIntExtra(MAX_LENGTH, 0);

        mTv.setText(itemname);
        mBtn.setOnClickListener(this);

        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if(bar!=null){
            bar.setDisplayHomeAsUpEnabled(true);
        }

        //修改群名称
        if(mItemField.equals(Const.GROUP_FIELD_NAME)){
            mEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLenght)});
            mEt.setText(AsmackUtils.filterGroupName( mGroupBean.getChild() ));
        }
        //修改群类型
        if(mItemField.equals(Const.GROUP_FIELD_TYPE)){
            vscroll.setVisibility(View.VISIBLE);
            mEt.setVisibility(View.GONE);
        }

        //修改群类型详细
        if(mItemField.equals(Const.GROUP_FIELD_TYPE_DETAIL)){
            mEt.setVisibility(View.GONE);
            if(mGroupBean.getGrouptype().equals("1")){
                //兴趣群
                wp4.setOnWheelChangeListener(new wp45ChangeListener(0));
                wp4.setWheelDecor(true, new MyAbstractWheelDecor());
                wp5.setOnWheelChangeListener(new wp45ChangeListener(1));
                wp5.setWheelDecor(true, new MyAbstractWheelDecor());
                initDataForInterest();
            }else if(mGroupBean.getGrouptype().equals("2")){
                //同城群
                wp1.setOnWheelChangeListener(new wp123ChangeListener(0));
                wp1.setWheelDecor(true, new MyAbstractWheelDecor());
                wp2.setOnWheelChangeListener(new wp123ChangeListener(1));
                wp2.setWheelDecor(true, new MyAbstractWheelDecor());
                wp3.setOnWheelChangeListener(new wp123ChangeListener(2));
                wp3.setWheelDecor(true, new MyAbstractWheelDecor());
                initDataForCity();
            }
        }


        //修改群公告
        if(mItemField.equals(Const.GROUP_FIELD_ANNOUNCE)){
            mEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLenght)});
            mEt.setText(mGroupBean.getAnnounce());
        }

        //修改我在群的昵称
        if(mItemField.equals(Const.GROUP_FIELD_MYNICKNAME)){
            mEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLenght)});
            mEt.setText(mGroupBean.getMynickname());
        }

    }

    /**
     * 初始化兴趣数据
     */
    private void initDataForInterest() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mInterestList0 = HttpUtil.okhttpPost_queryInterest("", 0);
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mInterestNameList0 == null) {
                            mInterestNameList0 = new ArrayList<String>();
                        } else {
                            mInterestNameList0.clear();
                        }
                        mInterestNameList0 = ToolsUtil.fiterList(mInterestList0);
                        wp4.setData(mInterestNameList0);
                        wpgroup2.setVisibility(View.VISIBLE);
                    }
                });
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
                                wp5.setData(mInterestNameList1);
                                wp5.setItemIndex(0);
                                wp5.checkScrollState();
                                wp5.invalidate();
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 初始化城市数据
     */
    private void initDataForCity() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mProvinceList = HttpUtil.okhttpPost_queryAreaInfo("", 0);
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProvinceNameList == null) {
                            mProvinceNameList = new ArrayList<String>();
                        } else {
                            mProvinceNameList.clear();
                        }
                        mProvinceNameList = ToolsUtil.fiterList(mProvinceList);
                        wp1.setData(mProvinceNameList);
                        wpgroup1.setVisibility(View.VISIBLE);
                    }
                });
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
                                wp2.setData(mCityNameList);
                                wp2.setItemIndex(0);
                                wp2.checkScrollState();
                                wp2.invalidate();
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
                                wp3.setData(mAreaNameList);
                                wp3.setItemIndex(0);
                                wp3.checkScrollState();
                                wp3.invalidate();
                            }
                        });
                    }
                }
            });
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
                //mTvOk.setEnabled(false);
            } else {
                //mTvOk.setEnabled(true);
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
                //mTvOk.setEnabled(false);
            } else {
                //mTvOk.setEnabled(true);
            }
        }
    }


    private class MyAbstractWheelDecor extends AbstractWheelDecor {
        @Override
        public void drawDecor(Canvas canvas, Rect rectLast, Rect rectNext, Paint paint) {
            canvas.drawColor(getResources().getColor(R.color.colorPrimaryAlpha));
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
        mMap = new HashMap<>();
        switch( v.getId() )
        {
            case R.id.btn:

                //修改群名称
                if (mItemField.equals(Const.GROUP_FIELD_NAME)) {
                    if (TextUtils.isEmpty(mEt.getText().toString().trim())) return;
                    mMap.put(mItemField, mEt.getText().toString().trim());
                    updateData(Const.GROUP_FIELD_NAME);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, mEt.getText().toString().trim());
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }
                //修改群公告
                if (mItemField.equals(Const.GROUP_FIELD_ANNOUNCE)) {
                    if (TextUtils.isEmpty(mEt.getText().toString().trim())) return;
                    mMap.put(mItemField, mEt.getText().toString().trim());
                    updateData(Const.GROUP_FIELD_ANNOUNCE);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, mEt.getText().toString().trim());
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }
                //修改我在群的昵称
                if (mItemField.equals(Const.GROUP_FIELD_MYNICKNAME)) {
                    if (TextUtils.isEmpty(mEt.getText().toString().trim())) return;
                    mMap.put(Const.GROUP_FIELD_MYNICKNAME, mEt.getText().toString().trim());
                    mMap.put(Const.USERNAME, AsmackUtils.filterAccountToUserName(IMService.mCurAccount)  );
                    updateData(Const.GROUP_FIELD_MYNICKNAME);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, mEt.getText().toString().trim());
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }



                //修改群类型详细
                if(mItemField.equals(Const.GROUP_FIELD_TYPE_DETAIL)){
                    if(mGroupBean.getGrouptype().equals("1")){
                        //兴趣群
                        mMap.put("gi0",mSelectedInterestCode0);
                        mMap.put("gi1",mSelectedInterestCode1);
                        updateData(Const.GROUP_FIELD_TYPE_DETAIL_GI);

                        resultData.putExtra(ITEM_FIELD, mItemField);
                        resultData.putExtra(ITEM_VALUE, mSelectedInterest0+"-"+mSelectedInterest1);
                        setResult(RESULT_SUCCEED, resultData);
                        finish();

                    }else if(mGroupBean.getGrouptype().equals("2")){
                        //同城群
                        mMap.put("gcity0",mSelectedProvinceCode);
                        mMap.put("gcity1",mSelectedCityCode);
                        mMap.put("gcity2",mSelectedAreaCode);
                        updateData(Const.GROUP_FIELD_TYPE_DETAIL_GCITY);

                        resultData.putExtra(ITEM_FIELD, mItemField);
                        resultData.putExtra(ITEM_VALUE, mSelectedProvince+"-"+mSelectedCity+"-"+mSelectedArea);
                        setResult(RESULT_SUCCEED, resultData);
                        finish();
                    }
                }
                break;



            case R.id.item1:
                mSelectedGroupType = "0";
                //修改群类型
                if(mItemField.equals(Const.GROUP_FIELD_TYPE)){
                    mMap.put(mItemField,mSelectedGroupType);
                    updateData(Const.GROUP_FIELD_TYPE);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, "基础群");
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }
                break;
            case R.id.item2:
                mSelectedGroupType = "1";
                //修改群类型
                if(mItemField.equals(Const.GROUP_FIELD_TYPE)){
                    mMap.put(mItemField,mSelectedGroupType);
                    updateData(Const.GROUP_FIELD_TYPE);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, "兴趣群");
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }
                break;
            case R.id.item3:
                mSelectedGroupType = "2";
                //修改群类型
                if(mItemField.equals(Const.GROUP_FIELD_TYPE)){
                    mMap.put(mItemField,mSelectedGroupType);
                    updateData(Const.GROUP_FIELD_TYPE);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, "同城群");
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }
                break;
            case R.id.item4:
                mSelectedGroupType = "3";
                //修改群类型
                if(mItemField.equals(Const.GROUP_FIELD_TYPE)){
                    mMap.put(mItemField,mSelectedGroupType);
                    updateData(Const.GROUP_FIELD_TYPE);

                    resultData.putExtra(ITEM_FIELD, mItemField);
                    resultData.putExtra(ITEM_VALUE, "私密群");
                    setResult(RESULT_SUCCEED, resultData);
                    finish();
                }
                break;
        }
    }


    private void updateData( final String field){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.okhttpPost_updateGroupInfo(mMap , field, mGroupBean.getChild());
            }
        });
    }
}
