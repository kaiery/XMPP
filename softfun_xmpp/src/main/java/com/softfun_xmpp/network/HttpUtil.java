package com.softfun_xmpp.network;

import android.content.Context;

import com.google.gson.Gson;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.application.SystemVars;
import com.softfun_xmpp.bean.FriendInfoBean;
import com.softfun_xmpp.bean.GroupBean;
import com.softfun_xmpp.bean.GroupMemberBean;
import com.softfun_xmpp.bean.OrgBean;
import com.softfun_xmpp.bean.ResultBean;
import com.softfun_xmpp.bean.UpdateBean;
import com.softfun_xmpp.bean.UserBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.NetWorkUtils;
import com.softfun_xmpp.utils.ToolsUtil;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.edu.zafu.coreprogress.helper.ProgressHelper;
import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    /**
     * 静态初始化组件
     */
    public static final OkHttpClient mOkHttpClient =  new OkHttpClient.Builder()
            //设置超时，不设置可能会报异常
            .connectTimeout(5000, TimeUnit.MINUTES)
            .readTimeout(5000, TimeUnit.MINUTES)
            .writeTimeout(5000, TimeUnit.MINUTES)
            .build();

    private static final Context context = GlobalContext.getInstance();
    private static final Gson gson = SystemVars.getInstance().getGson();




    /**
     * 获取升级信息
     * @return
     */
    public static UpdateBean okhttpGet_UpdateInfo(String typedef){
        String url = context.getResources().getString(R.string.app_server)+"queryUpdateInfo";
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("typedef", typedef)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                response.body().close();
                throw new IOException("Unexpected code " + response);
            }
            String jsonstr = response.body().string();
            //开始解析json数据，转为bean对象
            UpdateBean updateBean;
            updateBean = gson.fromJson(jsonstr, UpdateBean.class);
            return updateBean;
        } catch (Exception e) {
            //System.out.println("网络访问异常:okhttpGet_UpdateInfo");
            //ToastUtil.showToast("网络访问异常");
            e.printStackTrace();
        } finally {

        }
        return null;
    }





//    /**
//     * 通过okhttp获取网络图片的Bitmap
//     * @param urlString
//     * @return
//     */
//    public static Bitmap okhttpGet_Bitmap(String urlString) {
//        InputStream is = null;
//        Bitmap bitmap;
//        try {
//            Request request = new Request.Builder()
//                    .url(urlString)
//                    .build();
//            Response response = mOkHttpClient.newCall(request).execute();
//            if (!response.isSuccessful()) {
//                response.body().close();
//                throw new IOException("Unexpected code " + response);
//            }
//            //得到图片流
//            ////System.out.println(response.body().byteStream());
//            //使用BufferedInputStream重新包装，携带缓冲区
//            is = new BufferedInputStream(response.body().byteStream());
//            bitmap = BitmapFactory.decodeStream(is);
//            return bitmap;
//        } catch (Exception e) {
//            //System.out.println("网络访问异常：okhttpGet_Bitmap");
//            e.printStackTrace();
//        } finally {
//            try {
//                if(is!=null){
//                    is.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }




//    /**
//     * 访问网络获取用户背景图片
//     * @param urlString
//     * @return
//     */
//    public static boolean okhttpGet_BackGround(String urlString, OutputStream outputStream) {
//        BufferedOutputStream out = null;
//        BufferedInputStream in = null;
//        Response response = null;
//        try {
//            Request request = new Request.Builder()
//                    .url(urlString)
//                    .build();
//            response = mOkHttpClient.newCall(request).execute();
//            if (!response.isSuccessful()) {
//                response.body().close();
//                throw new IOException("Unexpected code " + response);
//            }
//            in = new BufferedInputStream(response.body().byteStream(), 8 * 1024);
//            out = new BufferedOutputStream(outputStream, 8 * 1024);
//            int b;
//            while ((b = in.read()) != -1) {
//                out.write(b);
//            }
//            return true;
//        } catch (Exception e) {
//            if(response!=null){
//                response.body().close();
//            }
//            //System.out.println("网络访问异常：okhttpGet_BackGround");
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }


    /**
     * 上传文件类，附带进度监听
     * @param filePath 文件绝对地址
     * @param name  文件名
     * @param uiProgressRequestListener 进度监听
     * @param type  文件类型
     * @return
     */
    public static String okhttpPost_uploadFile(String filePath,String name,UIProgressListener uiProgressRequestListener,String type,Callback myCallback){
        String uploadFinish;
        File file = new File(filePath);
        //构造上传请求，类似web表单
        String userid = IMService.mCurAccount;
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("hello", "android")
                .addFormDataPart("pid", "pidpidpid")
                .addFormDataPart("type", type)
                .addFormDataPart("userid", userid)
                //.addFormDataPart("file", file.getName(), RequestBody.create(null, file))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"file\";filename=  " + name + "  "),
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        //进行包装，使其支持进度回调
        final Request request = new Request.Builder().url(GlobalContext.getInstance().getString(R.string.app_server) + "servlet/Upload").
                post(ProgressHelper.addProgressRequestListener(requestBody, uiProgressRequestListener)).build();
        //开始请求
        mOkHttpClient.newCall(request).enqueue(myCallback);
//        mOkHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                //System.out.println("=====================================onFailure");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                //System.out.println("=====================================onResponse==== response:" + response.toString());
//                response.body().close();
//            }
//        });
        return null;
    }


    /**
     * 发表新BBS
     * @param title
     * @param typecode
     * @param content
     * @param selectpath
     */
    public static String okhttpPost_insertBBSNew(String title, String typecode, String content, ArrayList<String> selectpath) {
        String result = null;
        String url = GlobalContext.getInstance().getString(R.string.app_server)+"insertBBSNew";
        String imglist = ToolsUtil.arrayListToString(selectpath);
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("userid", IMService.mCurAccount)
                    .add("title", title)
                    .add("type",typecode)
                    .add("content",content)
                    .add("imglist", imglist)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                response.body().close();
                throw new IOException("Unexpected code " + response);
            }
            result = response.body().string();
        }catch (Exception e){
            //网络访问异常
            e.printStackTrace();
            //System.out.println("网络访问异常:okhttpPost_insertBBSNew");
        }
        return result;
    }



    /**
     * 从oracle中查询用户
     * @param mKeyword
     * @param page
     * @param rows
     * @return
     */
    public static ResultBean okhttpPost_QueryOracle_SearchUsersList(String mKeyword, int page, int rows) {
        ResultBean rb = new ResultBean();
        List<FriendInfoBean> list = new ArrayList<>();
        try {
            String serverURL = GlobalContext.getInstance().getString(R.string.app_server)+"queryUsersList";
            RequestBody formBody = new FormBody.Builder()
                    .add("page", page + "")
                    .add("rows", rows + "")
                    .add("keyword", mKeyword)
                    .build();
            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {

                JSONObject datamap = jsonObject.getJSONObject("datamap");
                int mCount = datamap.getInt("count");
                Map<String, Object> map = new HashMap<>();
                map.put("count", mCount);
                rb.setDatamap(map);

                JSONArray datalist = jsonObject.getJSONArray("datalist");
                for (int i = 0; i < datalist.length() ; i++) {
                    JSONObject object = (JSONObject)datalist.get(i);
                    FriendInfoBean fb = new FriendInfoBean();
                    fb.setUserid(object.getString("userid"));
                    fb.setShowname(object.getString("showname"));
                    fb.setPhonetic(object.getString("phonetic"));
                    fb.setVip(object.getString("vip"));
                    fb.setUsertype(object.getString("usertype"));
                    fb.setUserface(object.getString("userface"));
                    fb.setOrgid(object.getString("orgid"));
                    fb.setOrgname(object.getString("orgname"));
                    list.add(fb);
                }
                rb.setDatalist(list);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_QueryOracle_SearchUsersList");
            e.printStackTrace();
        }
        return rb;
    }



    /**
     * 通过userid获取用户基本信息
     * @param userid
     * @return
     */
    public static UserBean okhttpPost_QueryUserInfoByUserid(String userid) {
        //用户信息
        UserBean userBean = new UserBean();
        String url = GlobalContext.getInstance().getString(R.string.app_server)+"queryUserInfoByUserid";
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("userid", userid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                response.body().close();
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")){
                JSONObject map = jsonObject.getJSONObject("datamap");
                JSONObject jsonUserBean = map.getJSONObject("userbean");
                userBean.setUserid(jsonUserBean.getString("userid"));
                userBean.setUsername(jsonUserBean.getString("username"));
                userBean.setPassword(jsonUserBean.getString("password"));
                userBean.setStatus(jsonUserBean.getString("status"));
                userBean.setStatusname(jsonUserBean.getString("statusname"));
                userBean.setShowname(jsonUserBean.getString("showname"));
                userBean.setLtimes(jsonUserBean.getString("ltimes"));
                userBean.setOrgid(jsonUserBean.getString("orgid"));
                userBean.setOrgname(jsonUserBean.getString("orgname"));
                userBean.setUserregdate(jsonUserBean.getString("userregdate"));
                userBean.setUserphone(jsonUserBean.getString("userphone"));
                userBean.setUsertype(jsonUserBean.getString("usertype"));
                userBean.setUsertypename(jsonUserBean.getString("usertypename"));
                userBean.setPhonetic(jsonUserBean.getString("phonetic"));
                userBean.setIdcard(jsonUserBean.getString("idcard"));
                userBean.setSort(jsonUserBean.getString("sort"));
                userBean.setQq(jsonUserBean.getString("qq"));
                userBean.setEmail(jsonUserBean.getString("email"));
                userBean.setLognum(jsonUserBean.getString("lognum"));
                userBean.setToltime(jsonUserBean.getString("toltime"));
                userBean.setUserface(jsonUserBean.getString("userface"));
                userBean.setUserdesc(jsonUserBean.getString("userdesc"));
                userBean.setBackground(jsonUserBean.getString("background"));
                userBean.setScore(jsonUserBean.getInt("score"));
                userBean.setVip(jsonUserBean.getInt("vip"));
                userBean.setAddress(jsonUserBean.getString("address"));
                userBean.setWorkinglife(jsonUserBean.getString("workinglife"));
                userBean.setPositional(jsonUserBean.getString("positional"));
                userBean.setSpecialty(jsonUserBean.getString("specialty"));
                userBean.setCompany(jsonUserBean.getString("company"));
            }
        }catch (Exception e){
            e.printStackTrace();
            //网络访问异常
            //System.out.println("网络访问异常:okhttpPost_QueryUserInfoByUserid");
        }
        return userBean;
    }

    /**
     * 通过username获取用户基本信息
     * @param username
     * @return
     */
    public static UserBean okhttpPost_QueryUserInfoByUsername(String username) {
        //用户信息
        UserBean userBean = new UserBean();
        String url = GlobalContext.getInstance().getString(R.string.app_server)+"queryUserInfoByUsername";
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                response.body().close();
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")){
                JSONObject map = jsonObject.getJSONObject("datamap");
                JSONObject jsonUserBean = map.getJSONObject("userbean");
                userBean.setUserid(jsonUserBean.getString("userid"));
                userBean.setUsername(jsonUserBean.getString("username"));
                userBean.setPassword(jsonUserBean.getString("password"));
                userBean.setStatus(jsonUserBean.getString("status"));
                userBean.setStatusname(jsonUserBean.getString("statusname"));
                userBean.setShowname(jsonUserBean.getString("showname"));
                userBean.setLtimes(jsonUserBean.getString("ltimes"));
                userBean.setOrgid(jsonUserBean.getString("orgid"));
                userBean.setOrgname(jsonUserBean.getString("orgname"));
                userBean.setUserregdate(jsonUserBean.getString("userregdate"));
                userBean.setUserphone(jsonUserBean.getString("userphone"));
                userBean.setUsertype(jsonUserBean.getString("usertype"));
                userBean.setUsertypename(jsonUserBean.getString("usertypename"));
                userBean.setPhonetic(jsonUserBean.getString("phonetic"));
                userBean.setIdcard(jsonUserBean.getString("idcard"));
                userBean.setSort(jsonUserBean.getString("sort"));
                userBean.setQq(jsonUserBean.getString("qq"));
                userBean.setEmail(jsonUserBean.getString("email"));
                userBean.setLognum(jsonUserBean.getString("lognum"));
                userBean.setToltime(jsonUserBean.getString("toltime"));
                userBean.setUserface(jsonUserBean.getString("userface"));
                userBean.setUserdesc(jsonUserBean.getString("userdesc"));
                userBean.setBackground(jsonUserBean.getString("background"));
                userBean.setScore(jsonUserBean.getInt("score"));
                userBean.setVip(jsonUserBean.getInt("vip"));
                userBean.setAddress(jsonUserBean.getString("address"));
                userBean.setWorkinglife(jsonUserBean.getString("workinglife"));
                userBean.setPositional(jsonUserBean.getString("positional"));
                userBean.setSpecialty(jsonUserBean.getString("specialty"));
                userBean.setCompany(jsonUserBean.getString("company"));
            }
        }catch (Exception e){
            //网络访问异常
            e.printStackTrace();
            //System.out.println("网络访问异常:okhttpPost_QueryUserInfoByUsername");
        }
        return userBean;
    }


    /**
     * 获取专家机构表
     * @param orgid
     * @return
     */
    public static ResultBean okhttpPost_queryNj110OrgFlat(String orgid) {
        ResultBean rb = new ResultBean();
        List<OrgBean> list = new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryNj110OrgFlat";
            RequestBody formBody = new FormBody.Builder()
                    .add("orgid", orgid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {

                JSONArray datalist = jsonObject.getJSONArray("datalist");
                for (int i = 0; i < datalist.length() ; i++) {
                    JSONObject object = (JSONObject)datalist.get(i);
                    OrgBean orgBean = new OrgBean();
                    orgBean.setOrgid(object.getString("orgid"));
                    orgBean.setOrgname(object.getString("orgname"));
                    orgBean.setParentorg(object.getString("parentorg"));
                    list.add(orgBean);
                }
                rb.setDatalist(list);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryNj110OrgFlat");
            e.printStackTrace();
        }
        return rb;
    }

    /**
     * 获取上一级专家机构表
     * @param expertOrgid
     * @return
     */
    public static ResultBean okhttpPost_queryNj110ParentOrgFlat(String expertOrgid) {
        ResultBean rb = new ResultBean();
        List<OrgBean> list = new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryNj110ParentOrgFlat";
            RequestBody formBody = new FormBody.Builder()
                    .add("orgid", expertOrgid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {

                JSONArray datalist = jsonObject.getJSONArray("datalist");
                for (int i = 0; i < datalist.length() ; i++) {
                    JSONObject object = (JSONObject)datalist.get(i);
                    OrgBean orgBean = new OrgBean();
                    orgBean.setOrgid(object.getString("orgid"));
                    orgBean.setOrgname(object.getString("orgname"));
                    orgBean.setParentorg(object.getString("parentorg"));
                    list.add(orgBean);
                }
                rb.setDatalist(list);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryNj110ParentOrgFlat");
            e.printStackTrace();
        }
        return rb;
    }

    /**
     * 查询机构内的专家list
     * @param orgid
     * @return
     */
    public static ResultBean okhttpPost_queryNj110ZJList(String orgid) {
        ResultBean rb = new ResultBean();
        List<UserBean> list = new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryNj110ZJList";
            RequestBody formBody = new FormBody.Builder()
                    .add("orgid", orgid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {

                JSONArray datalist = jsonObject.getJSONArray("datalist");
                for (int i = 0; i < datalist.length() ; i++) {
                    JSONObject object = (JSONObject)datalist.get(i);
                    UserBean userBean = new UserBean();
                    userBean.setUserid(object.getString("userid"));
                    userBean.setUsername(object.getString("username"));
                    userBean.setUserface(object.getString("userface"));
                    userBean.setShowname(object.getString("showname"));
                    userBean.setSpecialty(object.getString("specialty")==null?"":object.getString("specialty")    );
                    userBean.setVip(Integer.parseInt(object.getString("vip")));
                    list.add(userBean);
                }
                rb.setDatalist(list);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryNj110ZJList");
            e.printStackTrace();
        }
        return rb;
    }


    /**
     * 根据姓名查找专家
     * @param mKeyword
     * @return
     */
    public static ResultBean okhttpPost_queryRemoteExpertbyshowname(String mKeyword) {
        ResultBean rb = new ResultBean();
        List<UserBean> list = new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryRemoteExpertbyshowname";
            RequestBody formBody = new FormBody.Builder()
                    .add("showname", mKeyword)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {

                JSONArray datalist = jsonObject.getJSONArray("datalist");
                for (int i = 0; i < datalist.length() ; i++) {
                    JSONObject object = (JSONObject)datalist.get(i);
                    UserBean userBean = new UserBean();
                    userBean.setUserid(object.getString("userid"));
                    userBean.setUsername(object.getString("username"));
                    userBean.setUserface(object.getString("userface"));
                    userBean.setShowname(object.getString("showname"));
                    userBean.setSpecialty(object.getString("specialty")==null?"":object.getString("specialty")    );
                    userBean.setVip(Integer.parseInt(object.getString("vip")));
                    list.add(userBean);
                }
                rb.setDatalist(list);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryRemoteExpertbyshowname");
            e.printStackTrace();
        }
        return rb;
    }


    /**
     * 根据特长查找专家
     * @param mKeyword
     * @return
     */
    public static ResultBean okhttpPost_queryRemoteExpertbytechang(String mKeyword) {
        ResultBean rb = new ResultBean();
        List<UserBean> list = new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryRemoteExpertbytechang";
            RequestBody formBody = new FormBody.Builder()
                    .add("techang", mKeyword)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {

                JSONArray datalist = jsonObject.getJSONArray("datalist");
                for (int i = 0; i < datalist.length() ; i++) {
                    JSONObject object = (JSONObject)datalist.get(i);
                    UserBean userBean = new UserBean();
                    userBean.setUserid(object.getString("userid"));
                    userBean.setUsername(object.getString("username"));
                    userBean.setUserface(object.getString("userface"));
                    userBean.setShowname(object.getString("showname"));
                    userBean.setSpecialty(object.getString("specialty")==null?"":object.getString("specialty")    );
                    userBean.setVip(Integer.parseInt(object.getString("vip")));
                    list.add(userBean);
                }
                rb.setDatalist(list);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryRemoteExpertbytechang");
            e.printStackTrace();
        }
        return rb;
    }


    /**
     * 发送短信
     * @param userphone userphone
     */
    public static String okhttpPost_sendsms(String userphone) {
        String yzm = null;
        try {
            String url = context.getResources().getString(R.string.app_server_n110)+"sendsms.do";
            RequestBody formBody = new FormBody.Builder()
                    .add("userphone", userphone)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            yzm = response.body().string();
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_sendsms");
            e.printStackTrace();
        }
        return yzm;
    }

    /**
     * 发送短信
     * @param userphone userphone
     */
    public static String okhttpPost_sendsms_foundPwd(String userphone) {
        String yzm = null;
        try {
            String url = context.getResources().getString(R.string.app_server_n110)+"sendsms_foundPwd.do";
            RequestBody formBody = new FormBody.Builder()
                    .add("userphone", userphone)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            yzm = response.body().string();
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_sendsms");
            e.printStackTrace();
        }
        return yzm;
    }


    /**
     * 用户注册
     * @param userBean
     * @return
     */
    public static int okhttpPost_registUser(UserBean userBean) {
        String username = userBean.getUsername();
        String password = userBean.getPassword();
        String userphone = userBean.getPhone();
        String showname = userBean.getNickname();
        int code = 0;
        try {
            String url = context.getResources().getString(R.string.app_server)+"registUser";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("userphone", userphone)
                    .add("showname", showname)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            code = Integer.parseInt(response.body().string());
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_registUser");
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 密码重置
     * @param username
     * @return
     */
    public static int okhttpPost_forgetPassword(String username) {
        int code = 0;
        try {
            String url = context.getResources().getString(R.string.app_server)+"forgetPassword";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            code = Integer.parseInt(response.body().string());
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_forgetPassword");
            e.printStackTrace();
        }
        return code;
    }

    public static void okhttpPost_sendsmsForMsg(String userphone,String msg) {
        try {
            String url = context.getResources().getString(R.string.app_server_n110)+"sendsmsForMsg.do";
            RequestBody formBody = new FormBody.Builder()
                    .add("userphone", userphone)
                    .add("msg", msg)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_sendsmsForMsg");
            e.printStackTrace();
        }
    }

    /**
     * 更新用户信息
     * @param map
     * @param username
     */
    public static void okhttpPost_updateVcard(Map<String, String> map, String username) {
        if(map.size()>0){
            try {
                String json = gson.toJson(map);
                String url = context.getResources().getString(R.string.app_server)+"updateVcard";
                RequestBody formBody = new FormBody.Builder()
                        .add("json", json)
                        .add("username", username)
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();
                Response response = mOkHttpClient.newCall(request).execute();
                if (!response.isSuccessful()){
                    throw new IOException("Unexpected code " + response);
                }
                response.body().string();
            }catch (Exception e){
                //System.out.println("网络访问异常:okhttpPost_updateVcard");
                e.printStackTrace();
            }
        }


    }

    /**
     * 更新自己的密码
     * @param username
     * @param password_old
     * @param password_new
     */
    public static int okhttpPost_updatePassword(String username, String password_old, String password_new) {
        int code = 0;
        try {
            String url = context.getResources().getString(R.string.app_server)+"updatePassword";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password_old", password_old)
                    .add("password_new", password_new)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            code = Integer.parseInt(response.body().string());
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_updatePassword");
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 评价
     * @param username
     * @param score
     * @return
     */
    public static int okhttpPost_updateEvaluate(String username, String targetname,long score,String memo) {
        int code = 0;
        try {
            String url = context.getResources().getString(R.string.app_server)+"updateEvaluate";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("targetname", targetname)
                    .add("score", String.valueOf(score))
                    .add("memo", memo)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            code = Integer.parseInt(response.body().string());
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_updateEvaluate");
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 意见反馈
     * @param account
     * @param content
     * @param contact
     */
    public static void okhttpPost_insertFeedBack(String account, String content, String contact) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"insertFeedBack";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", account)
                    .add("content", content)
                    .add("contact",contact)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_updateEvaluate");
            e.printStackTrace();
        }
    }

    /**
     * 查询离线的好友申请消息
     * @param username
     */
    public static void okhttpPost_queryOffPresences(String username) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryOffPresences";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {
                JSONArray datalist = jsonObject.getJSONArray("datalist");
                if(datalist.length()>0){
                    if(IMService.mOffPresenceList == null){
                        IMService.mOffPresenceList = new ArrayList<>();
                    }else{
                        IMService.mOffPresenceList.clear();
                    }
                    for (int i = 0; i < datalist.length() ; i++) {
                        JSONObject object = (JSONObject)datalist.get(i);
                        Presence presence = new Presence(Presence.Type.subscribe);
                        presence.setType(Presence.Type.subscribe);
                        presence.setFrom(object.getString("jid"));
                        presence.setTo(object.getString("username")+ "@" + Const.APP_PACKAGENAME);
                        IMService.mOffPresenceList.add(presence);
                    }
                }
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryOffPresences");
            e.printStackTrace();
        }
    }





    /**
     * 查询省份
     * @param id
     */
    public static List<String> okhttpPost_queryAreaInfo(String id, int level) {
        List<String> list = null;
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryAreaInfo";
            RequestBody formBody = new FormBody.Builder()
                    .add("id", id)
                    .add("level", String.valueOf(level))
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {
                JSONArray datalist = jsonObject.getJSONArray("datalist");
                if(datalist.length()>0){
                    list = new ArrayList<>();
                    for (int i = 0; i < datalist.length() ; i++) {
                        JSONObject object = (JSONObject)datalist.get(i);
                        list.add(object.getString("name")+"@:"+object.getString("id"));
                    }
                }
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryAreaInfo");
            e.printStackTrace();
            return list;
        }
        return list;
    }


    /**
     * 查询兴趣
     * @param id
     * @param level
     * @return
     */
    public static List<String> okhttpPost_queryInterest(String id, int level) {
        List<String> list = null;
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryInterest";
            RequestBody formBody = new FormBody.Builder()
                    .add("id", id)
                    .add("level", String.valueOf(level))
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {
                JSONArray datalist = jsonObject.getJSONArray("datalist");
                if(datalist.length()>0){
                    list = new ArrayList<>();
                    for (int i = 0; i < datalist.length() ; i++) {
                        JSONObject object = (JSONObject)datalist.get(i);
                        list.add(object.getString("name")+"@:"+object.getString("id"));
                    }
                }
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryInterest");
            e.printStackTrace();
            return list;
        }
        return list;
    }


    /**
     * 创建群组扩展信息
     * 创建群组，插入群组扩展信息
     * 将群主作为住客插入群住客表
     * @param roomName
     * @param username
     * @param type
     * @param gi
     * @param gcity
     */
    public static Integer okhttpPost_insertGroup(String roomName, String username ,String type,String gi,String gcity) {
        int code = 0;
        try {
            String url = context.getResources().getString(R.string.app_server)+"insertGroup";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("roomname",roomName.toLowerCase())
                    .add("type",type)
                    .add("gi",gi)
                    .add("gcity",gcity)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            code = Integer.parseInt(response.body().string());
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_insertGroup");
            e.printStackTrace();
            return code;
        }
        return code;
    }


    /**
     * 查询我的群组
     * @param username
     * @return
     */
    public static List<GroupBean> okhttpPost_queryMyGroups(String username) {
        List<GroupBean> list = null;
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryMyGroups";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {
                JSONArray datalist = jsonObject.getJSONArray("datalist");
                if(datalist.length()>0){
                    list = new ArrayList<>();
                    for (int i = 0; i < datalist.length() ; i++) {
                        JSONObject object = (JSONObject)datalist.get(i);
                        GroupBean groupBean = new GroupBean();
                        groupBean.setChildid(object.getString("name")+Const.ROOM_JID_SUFFIX);
                        groupBean.setChild(object.getString("name"));
                        groupBean.setGroupface(object.getString("face"));
                        groupBean.setGrouptype(object.getString("type"));
                        groupBean.setLvl(object.getString("lvl"));
                        groupBean.setGroupnum(object.getString("roomnum"));
                        list.add(groupBean);
                    }
                }
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryMyGroups");
            e.printStackTrace();
            return list;
        }
        return list;
    }

    /**
     * 此人是否是此群的成员
     * @param room_jid
     * @param username
     * @return
     */
    public static boolean okhttpPost_queryIsGroupMember(String room_jid, String username) {
        boolean result = false;
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryIsGroupMember";
            RequestBody formBody = new FormBody.Builder()
                    .add("roomjid", room_jid)
                    .add("username", username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            result = Boolean.parseBoolean(response.body().string());
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryIsGroupMember");
            e.printStackTrace();
            return result;
        }
        return result;
    }


    /**
     * 通过roomname获取群组基本信息
     * @param roomname
     * @return
     */
    public static GroupBean okhttpPost_queryGroupInfoByRoomName(String username,String roomname) {
        //聊天室信息
        GroupBean groupBean = new GroupBean();
        String url = GlobalContext.getInstance().getString(R.string.app_server)+"queryGroupInfoByRoomName";
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("roomname", roomname)
                    .add("username",username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                response.body().close();
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")){
                JSONObject map = jsonObject.getJSONObject("datamap");
                JSONObject jsonGroupBean = map.getJSONObject("groupbean");
                groupBean.setChildid(jsonGroupBean.getString("name")+Const.ROOM_JID_SUFFIX);
                groupBean.setChild(jsonGroupBean.getString("name"));
                groupBean.setLvl(jsonGroupBean.getString("lvl"));
                groupBean.setGroupface(jsonGroupBean.getString("face"));
                groupBean.setGroupnum(jsonGroupBean.getString("roomnum"));
                groupBean.setGrouptype(jsonGroupBean.getString("type"));
                groupBean.setGrouptypename(jsonGroupBean.getString("typename"));
                groupBean.setGi(jsonGroupBean.getString("gi"));
                groupBean.setGiname(jsonGroupBean.getString("giname"));
                groupBean.setGcity(jsonGroupBean.getString("gcity"));
                groupBean.setGcityname(jsonGroupBean.getString("gcityname"));
                groupBean.setMynickname(jsonGroupBean.getString("mynickname"));
                groupBean.setAnnounce(jsonGroupBean.getString("announce"));
                groupBean.setMaster(jsonGroupBean.getString("master"));
            }
        }catch (Exception e){
            //网络访问异常
            //System.out.println("网络访问异常:okhttpPost_queryGroupInfoByRoomName");
        }
        return groupBean;
    }


    /**
     * //将群邀请信息保存在远程服务器，下次用户登录后，获取后删除。
     * @param roomName
     * @param fromaccount
     * @param toaccount
     * @param msg
     */
    public static void okhttpPost_insertGroupOfflineInvite(String roomName, String fromaccount, String toaccount, String msg) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"insertGroupOfflineInvite";
            RequestBody formBody = new FormBody.Builder()
                    .add("roomname",roomName)
                    .add("fromaccount",fromaccount)
                    .add("toaccount",toaccount)
                    .add("msg",msg)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_insertGroupOfflineInvite");
            e.printStackTrace();
        }
    }

    /**
     * 手工获取（离线）的群邀请消息,获取后删除
     * @param account
     */
    public static void okhttpPost_queryOffGroupInvite(String account) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryOffGroupInvite";
            RequestBody formBody = new FormBody.Builder()
                    .add("account", account)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {
                JSONArray datalist = jsonObject.getJSONArray("datalist");
                if(datalist.length()>0){
                    if(IMService.mOffGroupInviteList == null){
                        IMService.mOffGroupInviteList = new ArrayList<>();
                    }else{
                        IMService.mOffGroupInviteList.clear();
                    }
                    for (int i = 0; i < datalist.length() ; i++) {
                        JSONObject object = (JSONObject)datalist.get(i);
                        Message message = new Message();
                        message.setFrom(object.getString("from_account")); //群聊发起人
                        message.setTo(object.getString("to_account"));
                        message.setBody(object.getString("msg")); //群聊消息内容
                        message.setType(Message.Type.groupchat); //聊天类型
                        message.setProperty(Const.MSGFLAG,Const.MSGFLAG_GROUP_INVITE);//    群聊消息的类型：群邀请的类型
                        message.setProperty(Const.GROUP_JID,object.getString("name")+Const.ROOM_JID_SUFFIX);//    群jid
                        IMService.mOffGroupInviteList.add(message);
                    }
                }
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryOffGroupInvite");
            e.printStackTrace();
        }
    }


    /**
     * 更新群基本信息
     * @param map
     * @param groupFieldName
     * @param name
     */
    public static void okhttpPost_updateGroupInfo(Map<String, String> map, String groupFieldName, String name) {

        if(map.size()>0){
            try {
                String json = gson.toJson(map);
                String url = context.getResources().getString(R.string.app_server)+"updateGroupInfo";
                RequestBody formBody = new FormBody.Builder()
                        .add("json", json)
                        .add("field", groupFieldName)
                        .add("name",name)
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();
                Response response = mOkHttpClient.newCall(request).execute();
                if (!response.isSuccessful()){
                    throw new IOException("Unexpected code " + response);
                }
                response.body().string();
            }catch (Exception e){
                //System.out.println("网络访问异常:okhttpPost_updateGroupInfo");
                e.printStackTrace();
            }
        }
    }



    /**
     * 查询我的群组里的成员
     * @param jid
     * @return
     */
    public static List<GroupMemberBean> okhttpPost_queryGroupMembers(String jid) {
        List<GroupMemberBean> list = null;
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryGroupMembers";
            RequestBody formBody = new FormBody.Builder()
                    .add("jid", jid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            //开始解析json数据，转为bean对象，插入到list
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")) {
                JSONArray datalist = jsonObject.getJSONArray("datalist");
                if(datalist.length()>0){
                    list = new ArrayList<>();
                    for (int i = 0; i < datalist.length() ; i++) {
                        JSONObject object = (JSONObject)datalist.get(i);
                        GroupMemberBean bean = new GroupMemberBean();
                        bean.setJid(jid);
                        bean.setAccount(object.getString("account"));
                        bean.setNickname(object.getString("nickname"));
                        bean.setPinyin(object.getString("pinyin"));
                        bean.setAvatarurl(object.getString("avatarurl"));
                        bean.setMaster(object.getString("master"));
                        list.add(bean);
                    }
                }
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryGroupMembers");
            e.printStackTrace();
            return list;
        }
        return list;
    }

    /**
     * 查询我的群组里的新成员
     * @param jid
     * @return
     */
    public static GroupMemberBean okhttpPost_queryNewGroupMember(String jid,String username) {
        GroupMemberBean bean = new GroupMemberBean();
        try {
            String url = context.getResources().getString(R.string.app_server)+"queryNewGroupMember";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            //开始解析json数据
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")){
                JSONObject map = jsonObject.getJSONObject("datamap");
                JSONObject jsonUserBean = map.getJSONObject("groupmemberbean");

                bean.setJid(jid);
                bean.setAccount(jsonUserBean.getString("account"));
                bean.setNickname(jsonUserBean.getString("nickname"));
                bean.setPinyin(jsonUserBean.getString("pinyin"));
                bean.setAvatarurl(jsonUserBean.getString("avatarurl"));
                bean.setMaster(jsonUserBean.getString("master"));
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_queryNewGroupMember");
            e.printStackTrace();
            return bean;
        }
        return bean;
    }
    /**
     * 删除群成员
     * @param groupname
     * @param kicked_username
     */
    public static int okhttpPost_deleteGroupMember(String groupname, String kicked_username) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"deleteGroupMember";
            RequestBody formBody = new FormBody.Builder()
                    .add("groupname", groupname)
                    .add("username",kicked_username)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            String result = response.body().string();
            return Integer.parseInt(result);
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_deleteGroupMember");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新群管理员
     * @param mGroupJid
     * @param selectAccount
     * @return
     */
    public static int okhttpPost_updateGroupMaster(String mGroupJid, String selectAccount) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"updateGroupMaster";
            RequestBody formBody = new FormBody.Builder()
                    .add("groupname", mGroupJid)
                    .add("account",selectAccount)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            String result = response.body().string();
            return Integer.parseInt(result);
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_updateGroupMaster");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除群
     * @param groupjid
     */
    public static int okhttpPost_deleteGroup(String groupjid) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"deleteGroup";
            RequestBody formBody = new FormBody.Builder()
                    .add("groupname", groupjid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            String result = response.body().string();
            return Integer.parseInt(result);
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_deleteGroup");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 写用户登录日志
     * @param userName
     */
    public static void okhttpPost_writeUserLoginLog(String userName) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"writeUserLoginLog";
            String localIpAddress = NetWorkUtils.getLocalIpAddress(context);
            RequestBody formBody = new FormBody.Builder()
                    .add("username", userName)
                    .add("ip", localIpAddress)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
        }catch (Exception e){
            //System.out.println("网络访问异常:okhttpPost_writeUserLoginLog");
            e.printStackTrace();
        }
    }
}
