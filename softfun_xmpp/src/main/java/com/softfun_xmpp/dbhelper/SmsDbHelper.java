package com.softfun_xmpp.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.softfun_xmpp.constant.Const;

public class SmsDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_SMS = "t_sms";

    public class SmsTable implements BaseColumns {
        public static final String FROM_ACCOUNT = "from_account";//发送者帐号
        public static final String TO_ACCOUNT = "to_account";//接收者帐号
        public static final String BODY = "body";//消息
        public static final String STATUS = "status";//发送状态
        public static final String TYPE = "type";//消息类型
        public static final String TIME = "time";//时间
        public static final String SESSION_ACCOUNT = "session_account";//会话id
        public static final String TAG = "tag";//未读消息标记 0未读，1已读
        public static final String OWNER = "owner";//消息所属者
        public static final String IMGURL = "imgurl";//图片消息的图片真实地址
        public static final String FLAG = "flag"; //消息标记
        public static final String RECORDURL = "recordurl";//录音地址
        public static final String RECORDLEN = "recordlen";///录音长度
        public static final String RECORDTIME = "recordtime";//录音时长
        public static final String UNREAD_MSG_COUNT = "unread_msg_count";//未读消息统计

        public static final String ROOM_JID = "room_jid";//群聊jid
        public static final String ROOM_NAME = "room_name";//群聊名
        ;
    }

    /**已读常量*/
    public static final String ISREAD = "1";//已读
    /**未读常量*/
    public static final String UNREAD = "0";//未读

    public SmsDbHelper(Context context) {
        super(context, "sms.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_SMS + " ( \n" +
                "    _id     INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                SmsTable.FROM_ACCOUNT + " TEXT,\n" +
                SmsTable.TO_ACCOUNT + " TEXT,\n" +
                SmsTable.BODY + " TEXT,\n" +
                SmsTable.STATUS + " TEXT, \n" +
                SmsTable.TYPE + " TEXT, \n" +
                SmsTable.TIME + " TEXT, \n" +
                SmsTable.TAG + " TEXT DEFAULT ( '1' ) , \n" +
                SmsTable.SESSION_ACCOUNT + " TEXT, \n" +
                SmsTable.OWNER + " TEXT, \n"+
                SmsTable.FLAG + " TEXT, \n"+
                SmsTable.RECORDURL + " TEXT, \n"+
                SmsTable.RECORDLEN + " TEXT, \n"+
                SmsTable.RECORDTIME + " TEXT, \n"+
                SmsTable.ROOM_JID + " TEXT, \n"+
                SmsTable.ROOM_NAME + " TEXT, \n"+
                SmsTable.IMGURL + " TEXT \n"+
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    /**
     * 查询chatActivity中的聊天记录
     * @param sqlWhere
     * @param sqlWhereArgs
     * @param sqlOrder
     * @return
     */
    public Cursor querytable(String sqlWhere, String[] sqlWhereArgs, String sqlOrder) {

        String smsSql = "select * from (select * from "+SmsDbHelper.TABLE_SMS+" where" +
                " ("+SmsDbHelper.SmsTable.FROM_ACCOUNT+"=? and "+SmsDbHelper.SmsTable.TO_ACCOUNT+"=?) or" +
                " ("+SmsDbHelper.SmsTable.FROM_ACCOUNT+"=? and "+SmsDbHelper.SmsTable.TO_ACCOUNT+"=?) and "+SmsDbHelper.SmsTable.TYPE+"=?  order by "+SmsDbHelper.SmsTable.TIME+" desc limit ? offset ? ) " +
                "order by "+SmsDbHelper.SmsTable.TIME+" asc";
        return this.getReadableDatabase().rawQuery(smsSql,sqlWhereArgs);
    }


    /**
     * 查询MultiChatActiviy中的聊天记录
     * @param sqlWhere
     * @param sqlWhereArgs
     * @param sqlOrder
     * @return
     */
    public Cursor querytableForGroupChat(String sqlWhere, String[] sqlWhereArgs, String sqlOrder) {

        String groupSmsSql = "select * from (select * from "+SmsDbHelper.TABLE_SMS+" where " +
                SmsDbHelper.SmsTable.TYPE+"=? and "+
                SmsDbHelper.SmsTable.ROOM_JID+" =? " +
                " order by "+SmsDbHelper.SmsTable.TIME+" desc limit ? offset ? ) " +
                "order by "+SmsDbHelper.SmsTable.TIME+" asc";
        return this.getReadableDatabase().rawQuery(groupSmsSql,sqlWhereArgs);
    }


    /**
     * 查询会话记录,包含未读消息数量统计
     * @param selectionArgs
     * @return
     */
    public Cursor querySession(String[] selectionArgs) {
//        String sql = "SELECT *," +
//                " (" +
//                " SELECT count( 1 ) FROM "+SmsDbHelper.TABLE_SMS+" WHERE ( "+SmsDbHelper.SmsTable.FROM_ACCOUNT+" = ? OR "+SmsDbHelper.SmsTable.TO_ACCOUNT+" = ? ) AND tag = "+SmsDbHelper.UNREAD+"" +
//                " ) "+SmsTable.UNREAD_MSG_COUNT+"" +
//                " FROM (" +
//                " SELECT *" +
//                " FROM ( SELECT * FROM "+SmsDbHelper.TABLE_SMS+" WHERE "+SmsDbHelper.SmsTable.FROM_ACCOUNT+" = ? OR "+SmsDbHelper.SmsTable.TO_ACCOUNT+" = ? ORDER BY "+SmsDbHelper.SmsTable.TIME+" ASC )" +
//                " GROUP BY "+SmsDbHelper.SmsTable.SESSION_ACCOUNT+"" +
//                ")";
        String sql = "SELECT *," +
                " (" +
                " SELECT count( 1 ) FROM "+SmsDbHelper.TABLE_SMS+" WHERE "+ SmsTable.OWNER+"=? AND tag = "+SmsDbHelper.UNREAD+"  and " +SmsTable.SESSION_ACCOUNT+"=t."+SmsTable.SESSION_ACCOUNT  +
                " ) "+SmsTable.UNREAD_MSG_COUNT+"" +
                " FROM (" +
                " SELECT *" +
                " FROM ( SELECT * FROM "+SmsDbHelper.TABLE_SMS+" WHERE "+SmsTable.OWNER+"=? ORDER BY "+SmsDbHelper.SmsTable.TIME+" ASC )" +
                " GROUP BY "+SmsDbHelper.SmsTable.SESSION_ACCOUNT+"" +
                ") t  order by  " +SmsDbHelper.SmsTable.TIME+" DESC";
        Cursor cursor = this.getReadableDatabase().rawQuery(sql, selectionArgs);
        return cursor;
    }


    /**
     * 查询当前私聊聊天对象的所有图片
     * @param selectionArgs
     * @return
     */
    public Cursor queryImagesFromChat(String[] selectionArgs) {
        String sql = " select  " +SmsTable.IMGURL+" from "+SmsDbHelper.TABLE_SMS +
                " where "+SmsTable.OWNER+"=?   and " +
                SmsTable.FLAG+" = '"+ Const.MSGFLAG_IMG +"' and "+
                SmsTable.IMGURL+" is not null  and " +
                " (("+SmsTable.FROM_ACCOUNT+"=? and "+SmsTable.TO_ACCOUNT+"=? ) or ("+SmsTable.FROM_ACCOUNT+"=? and "+SmsTable.TO_ACCOUNT+"=?))";
        //System.out.println("====================    ====================="  + sql);
        Cursor cursor = this.getReadableDatabase().rawQuery(sql, selectionArgs);
        return cursor;
    }


    /**
     * 查询当前群聊聊天对象的所有图片
     * @param selectionArgs
     * @return
     */
    public Cursor queryImagesFromGroupChat(String[] selectionArgs) {
        String sql = " select  " +SmsTable.IMGURL+" from "+SmsDbHelper.TABLE_SMS +" where "+
                SmsDbHelper.SmsTable.TYPE+"=? and "+
                SmsDbHelper.SmsTable.ROOM_JID+" =?  and " +
                SmsTable.FLAG+" = '"+ Const.MSGFLAG_IMG +"' and "+
                SmsTable.IMGURL+" is not null  "    ;
        Cursor cursor = this.getReadableDatabase().rawQuery(sql, selectionArgs);
        return cursor;
    }
}
