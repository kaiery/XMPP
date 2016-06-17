package com.softfun_xmpp.constant;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;

/**
 * Created by 范张 on 2016-04-14.
 */
public class Const {
    public static final String APP_PACKAGENAME = GlobalContext.getInstance().getResources().getString(R.string.app_packagename);
    public static final String ROOM_JID_SUFFIX = "@conference."+GlobalContext.getInstance().getResources().getString(R.string.app_packagename);
    public static final String ACCOUNT = "account";
    public static final String PASSWORD ="password";
    public static final String USERNAME = "username";
    public static final String AVATARURL = "userface";
    public static final String VIP = "vip";
    public static final String BACKGROUND = "background";
    public static final String SCORE = "score";
    public static final String ROLETYPE = "roletype";
    public static final String NICKNAME = "showname";
    public static final String USERID = "userid";
    public static final String ORGID = "orgid";
    public static final String ORGNAME = "orgname";
    public static final String USERPHONE = "userphone";
    public static final String PHONETIC = "phonetic";
    public static final String IDCARD = "idcard";
    public static final String QQ = "qq";
    public static final String EMAIL = "email";
    public static final String USERDESC = "userdesc";
    public static final String SN = "sn";
    public static final String COMPANY = "company";
    public static final String SPECIALTY = "specialty";
    public static final String POSITIONAL = "positional";
    public static final String WORKINGLIFE = "workinglife";
    public static final String ADDRESS = "address";

    public static final String GROUP_FIELD_MASTER = "jid";
    public static final String GROUP_FIELD_ROOMID = "roomid";
    public static final String GROUP_FIELD_NAME = "name";
    public static final String GROUP_FIELD_TYPE = "type";
    public static final String GROUP_FIELD_TYPE_DETAIL = "typedetail";
    public static final String GROUP_FIELD_TYPE_DETAIL_GI = "typedetail_gi";
    public static final String GROUP_FIELD_TYPE_DETAIL_GCITY = "typedetail_gcity";
    public static final String GROUP_FIELD_FACE = "face";
    public static final String GROUP_FIELD_LVL = "lvl";
    public static final String GROUP_FIELD_GI = "gi";
    public static final String GROUP_FIELD_GINAME = "giname";
    public static final String GROUP_FIELD_GCITY = "gcity";
    public static final String GROUP_FIELD_GCITYNAME = "gcityname";
    public static final String GROUP_FIELD_ANNOUNCE = "content";
    public static final String GROUP_FIELD_ECHO = "echo";
    public static final String GROUP_FIELD_MYNICKNAME = "nickname";
    public static final String GROUP_FIELD_QRCODE = "qrcode";
    public static final String GROUP_FIELD_GROUPMEMBER = "groupmember";
    public static final String GROUP_FIELD_CHANGEMASTER = "changemaster";


    /**版本升级标志
     * 0：有新版本
     * 1：没有新版本
     * **/
    public static final String UPDATE = "update";
    public static final String UPDATEVERCODE = "updatevercode";
    public static final String UPDATEDESC = "updatedesc";
    public static final String UPDATEDOWNLOADURL = "updatedownloadurl";
    public static final String UPDATEFILESIZE = "updatefilesize";


    /**
     * 消息类型：
     */
    public static final String MSGFLAG = "msgflag";
    /**
     * 文本消息，包括文字、表情
     */
    public static final String MSGFLAG_TEXT = "msgflag_text";
    /**
     * 图片消息
     */
    public static final String MSGFLAG_IMG = "msgflag_img";
    /**
     * 录音消息
     */
    public static final String MSGFLAG_RECORD = "msgflag_record";
    public static final String RECORDTIME = "recordtime";
    public static final String RECORDURL = "recordurl";
    public static final String RECORDLEN = "recordlen";
    /**
     * 文件消息，包括图片、二进制文件
     */
    public static final String MSGFLAG_FILE = "msgflag_file";
    public static final String FILEURL = "fileurl";

    /**
     * 视频申请
     */
    public static final String MSGFLAG_VIDEO = "msgflag_video";
    /**
     * 群聊邀请
     */
    public static final String MSGFLAG_GROUP_INVITE = "msgflag_group_invite";
    /**
     * 离开脱离群聊
     */
    public static final String MSGFLAG_GROUP_LEAVE = "msgflag_group_leave";
    /**
     * 群聊解散
     */
    public static final String MSGFLAG_GROUP_DISMISS = "msgflag_group_dismiss";
    /**
     * 群聊踢人
     */
    public static final String MSGFLAG_GROUP_KICK = "msgflag_group_kick";
    /**
     * 被踢的群成员username
     */
    public static final String MSGFLAG_GROUP_KICKED_USERNAME = "msgflag_group_kicked_username";
    /**
     * 更新群管理员
     */
    public static final String MSGFLAG_GROUP_CHANGEMASTER = "msgflag_group_changemaster";
    /**
     * 新的群管理员
     */
    public static final String MSGFLAG_GROUP_NEW_MASTER = "msgflag_group_new_master";
    /**
     * 群JID
     */
    public static final String GROUP_JID = "group_jid";

    /**
     * web通用activity中传递url
     */
    public static final String WEB_URL = "web_url";
    /**
     * web通用activity中传递标题
     */
    public static final String WEB_TITLE = "web_title";
    /**
     * 视频聊天的请求码
     */
    public static final int REQUEST_VIDEO = 1;
    public static final String REALIMAGEURL = "realImageURL";


    /**
     * 视频聊天的结果码
     */
    public static int RESULT_VIDEO = 0;

    /**
     * 视频聊天对专家进行评价的请求码
     */
    public static int RESULT_VIDEO_EVALUATE = 2;
    /**
     * 群类型：基础群
     */
    public static final int GROUP_NORMAL = 0;
    /**
     * 群类型：兴趣群
     */
    public static final int GROUP_GAME = 1;
    /**
     * 群类型：同城群
     */
    public static final int GROUP_CITY = 2;
    /**
     * 群类型：私密群
     */
    public static final int GROUP_SECRET = 3;
    /**
     * 群类型
     */
    public static final String GROUPTYPE = "grouptype";




    /**
     * 消息中的录音类型文件
     */
    public static final String UPLOAD_TYPE_AMR = "amr";
    /**
     * 消息中的图片类型文件
     */
    public static final String UPLOAD_TYPE_MSGIMG = "msgimg";
    /**
     * 用户上传的头像图标文件
     */
    public static final String UPLOAD_TYPE_FACE = "face";
    /**
     * 用户上传的背景图片文件
     */
    public static final String UPLOAD_TYPE_BACKGROUND = "background";
    /**
     * 用户上传的web图片文件
     */
    public static final String UPLOAD_TYPE_WEBIMAGE = "webimage";
    /**
     * 用户上传的小视频文件
     */
    public static final String UPLOAD_TYPE_MOVIE = "movie";
    /**
     * 用户上传的其他文件
     */
    public static final String UPLOAD_TYPE_OTHERFILE = "files";

    public static final String WEB_FACE_THUMBS =        "thumbs/";
    public static final String WEB_AMR_PATH =           "web/amr/";
    public static final String WEB_MSGIMG_PATH =        "web/Images/msgimage/";
    public static final String WEB_WEBIMG_PATH =        "web/Images/webimage/";
    public static final String WEB_FACE_PATH =          "web/Images/faces/";
    public static final String WEB_BACKGROUND_PATH =    "web/Images/background/";
    public static final String WEB_MOVIE_PATH =         "web/movies/";
    public static final String WEB_FILE_PATH =          "web/files/";



    /**
     *重复登录的广播action
     */
    public static final String RELOGIN_BROADCAST_ACTION = "com.softfun.relogin_broadcast_action";
    /**
     * 自动登录常量标记
     */
    public static final String AUTOLOGIN = "autologin";

}
