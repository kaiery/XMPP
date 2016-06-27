package com.softfun_xmpp.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ContactsDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_CONTACT = "t_contact";


    public class ContactTable implements BaseColumns {
        public static final String ACCOUNT = "account";//帐号
        public static final String NICKNAME = "nickname";//昵称
        public static final String AVATARURL = "avatarurl";//头像
        public static final String PINYIN = "pinyin";//拼音
        public static final String STATUS = "status";//状态
        public static final String OWNER = "owner";//所有者
        public static final String VIP = "vip";//级别
        public static final String BACKGROUND = "background";//背景图
        public static final String SCORE = "score";//积分
        public static final String ROLETYPE = "roletype";//角色类型
    }

    public ContactsDbHelper(Context context) {
        super(context, "contact.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_CONTACT + " ( \n" +
                "    _id     INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                ContactTable.ACCOUNT + " TEXT,\n" +
                ContactTable.NICKNAME + " TEXT,\n" +
                ContactTable.AVATARURL + " TEXT,\n" +
                ContactTable.PINYIN + " TEXT, \n" +
                ContactTable.STATUS + " TEXT, \n" +
                ContactTable.VIP + " TEXT, \n" +
                ContactTable.BACKGROUND + " TEXT, \n" +
                ContactTable.SCORE + " TEXT, \n" +
                ContactTable.ROLETYPE + " TEXT, \n" +
                ContactTable.OWNER + " TEXT \n" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //System.out.println("====================  onUpgrade  ====================="+db.getPath()+"  "+oldVersion+"  "+newVersion);
    }

    public  Cursor querytable(String sqlWhere,String[] sqlWhereArgs, String sqlOrder){
        Cursor cursor = this.getReadableDatabase().query(TABLE_CONTACT, null, sqlWhere, sqlWhereArgs, null, null, sqlOrder);
        //Cursor cursor = this.getReadableDatabase().query(TABLE_CONTACT, null, null, null, null, null, ContactTable.PINYIN+" asc");
        return cursor;
    }
}
