package com.softfun_xmpp.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class GroupDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_GROUP = "t_group";
    public static final String TABLE_GROUPMEMBER = "t_groupmember";


    public class GroupTable implements BaseColumns {
        public static final String JID = "jid";
        public static final String TYPE = "type";//群类型
        public static final String LVL = "lvl";//群等级
        public static final String FACE = "face";//群头像
        public static final String ROOMNUM = "roomnum";//群号
        public static final String OWNER = "owner";//所属者
        public static final String PINYIN = "pinyin";//简码
    }

    public class GroupMemberTable implements BaseColumns {
        public static final String JID = "jid";
        public static final String ACCOUNT = "account";//帐号
        public static final String NICKNAME = "nickname";//昵称
        public static final String AVATARURL = "avatarurl";//头像
        public static final String PINYIN = "pinyin";//拼音
    }

    public GroupDbHelper(Context context) {
        super(context, "group.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_GROUP + " ( \n" +
                "    _id     INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                GroupTable.JID + " TEXT,\n" +
                GroupTable.TYPE + " TEXT,\n" +
                GroupTable.LVL + " TEXT, \n" +
                GroupTable.FACE + " TEXT, \n" +
                GroupTable.OWNER + " TEXT, \n" +
                GroupTable.ROOMNUM + " TEXT, \n" +
                GroupTable.PINYIN + " TEXT \n" +
                ")";
        db.execSQL(sql);

        sql = "CREATE TABLE " + TABLE_GROUPMEMBER + " ( \n" +
                "    _id     INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                GroupMemberTable.JID + " TEXT,\n" +
                GroupMemberTable.ACCOUNT + " TEXT, \n" +
                GroupMemberTable.NICKNAME + " TEXT, \n" +
                GroupMemberTable.PINYIN + " TEXT, \n" +
                GroupMemberTable.AVATARURL + " TEXT \n" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //System.out.println("====================  onUpgrade  ====================="+db.getPath()+"  "+oldVersion+"  "+newVersion);
    }

    public  Cursor querytable(String sqlWhere,String[] sqlWhereArgs, String sqlOrder){
        Cursor cursor = this.getReadableDatabase().query(TABLE_GROUP, null, sqlWhere, sqlWhereArgs, null, null, sqlOrder);
        return cursor;
    }




}
