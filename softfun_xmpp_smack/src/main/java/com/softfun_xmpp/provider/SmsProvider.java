package com.softfun_xmpp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.dbhelper.SmsDbHelper;


public class SmsProvider extends ContentProvider {

    //得到本类的完整路径
    //主机地址的常量
    public static final String AUTHORITIES = SmsProvider.class.getCanonicalName();

    //地址匹配对象
    static UriMatcher mUriMatcher;
    //对应私聊消息表的一个常量
    public static Uri URI_SMS = Uri.parse("content://" + AUTHORITIES + "/sms");
    public static final int SMS = 1;

    //对应消息会话的一个常量
    public static Uri URI_SESSION = Uri.parse("content://" + AUTHORITIES + "/session");
    public static final int SESSION = 2;

    public static Uri URI_GROUPSMS = Uri.parse("content://" + AUTHORITIES + "/groupsms");
    public static final int GROUPSMS = 3;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //添加了一条匹配规则(聊天消息)
        mUriMatcher.addURI(AUTHORITIES, "/sms", SMS);
        //外部只要访问 --> content://softfun.softfun_xmpp.provider.SmsProvider/sms  就可以得到SMS

        //添加了一条匹配规则（消息会话）
        mUriMatcher.addURI(AUTHORITIES, "/session", SESSION);

        mUriMatcher.addURI(AUTHORITIES,"/groupsms",GROUPSMS);
    }

    private SmsDbHelper mHelper;


    @Override
    public boolean onCreate() {
        mHelper = new SmsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int code = mUriMatcher.match(uri);
        switch (code) {
            case SMS:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //the row ID of the newly inserted row, or -1 if an error occurred
                long id = db.insert(SmsDbHelper.TABLE_SMS, "", values);
                if (id > 0) {
                    //System.out.println("----------消息插入成功--------");
                    //拼接最新的uri,重新赋值 ,比如拼接成：   content://softfun.softfun_xmpp.provider.SmsProvider/sms/id
                    uri = ContentUris.withAppendedId(uri, id);
                    //***通知 SmsContentObserver  数据改变了
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);//null 为所有的观察者都可以收到
                }
                break;
            case GROUPSMS:
                SQLiteDatabase db1 = mHelper.getWritableDatabase();
                long id1 = db1.insert(SmsDbHelper.TABLE_SMS, "", values);
                if (id1 > 0) {
                    //System.out.println("----------群聊消息插入成功--------");
                    uri = ContentUris.withAppendedId(uri, id1);
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_GROUPSMS, null);//null 为所有的观察者都可以收到
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int code = mUriMatcher.match(uri);
        int deleteCount = 0;
        switch (code) {
            case SMS:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                deleteCount = db.delete(SmsDbHelper.TABLE_SMS, selection, selectionArgs);
                if (deleteCount > 0) {
                    //System.out.println("----------消息删除成功--------");
                    //***通知 SmsContentObserver  数据改变了
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);//null 为所有的观察者都可以收到
                }
                break;
            case GROUPSMS:
                SQLiteDatabase db1 = mHelper.getWritableDatabase();
                deleteCount = db1.delete(SmsDbHelper.TABLE_SMS, selection, selectionArgs);
                if (deleteCount > 0) {
                    //System.out.println("----------群聊消息删除成功--------");
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_GROUPSMS, null);
                }
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int code = mUriMatcher.match(uri);
        int updateCount = 0;
        switch (code) {
            case SMS:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                updateCount = db.update(SmsDbHelper.TABLE_SMS, values, selection, selectionArgs);
                if (updateCount > 0) {
                    //System.out.println("----------消息更新成功--------");
                    //***通知 SmsContentObserver  数据改变了
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);//null 为所有的观察者都可以收到
                }
                break;
            case GROUPSMS:
                SQLiteDatabase db1 = mHelper.getWritableDatabase();
                updateCount = db1.update(SmsDbHelper.TABLE_SMS, values, selection, selectionArgs);
                if (updateCount > 0) {
                    //System.out.println("----------群聊消息更新成功--------");
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_GROUPSMS, null);//null 为所有的观察者都可以收到
                }
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = mUriMatcher.match(uri);
        Cursor cursor = null;
        switch (code) {
            //匹配到聊天消息
            case SMS:
                //SQLiteDatabase db = mHelper.getReadableDatabase();
                //cursor = db.query(SmsDbHelper.TABLE_SMS, projection, selection, selectionArgs, null, null, sortOrder);
                String smsSql = "select * from (select * from "+SmsDbHelper.TABLE_SMS+" where" +
                        " ("+SmsDbHelper.SmsTable.FROM_ACCOUNT+"=? and "+SmsDbHelper.SmsTable.TO_ACCOUNT+"=?) or" +
                        " ("+SmsDbHelper.SmsTable.FROM_ACCOUNT+"=? and "+SmsDbHelper.SmsTable.TO_ACCOUNT+"=?) and "+SmsDbHelper.SmsTable.TYPE+"=?  order by "+SmsDbHelper.SmsTable.TIME+" desc limit ? offset ? ) " +
                        "order by "+SmsDbHelper.SmsTable.TIME+" asc";
                //System.out.println("消息记录的sql:"+smsSql);
                cursor = mHelper.getReadableDatabase().rawQuery(smsSql,selectionArgs);
                //System.out.println("----------&&&消息查询成功&&&--------");
                break;

            case GROUPSMS:
                String groupSmsSql = "select * from (select * from "+SmsDbHelper.TABLE_SMS+" where " +
                        SmsDbHelper.SmsTable.TYPE+"=? and "+
                        SmsDbHelper.SmsTable.FLAG+"<>'"+ Const.MSGFLAG_GROUP_INVITE+"'  and "+
                        SmsDbHelper.SmsTable.ROOM_JID+" =? " +
                        " order by "+SmsDbHelper.SmsTable.TIME+" desc limit ? offset ? ) " +
                        "order by "+SmsDbHelper.SmsTable.TIME+" asc";
                //System.out.println("群聊消息记录的sql:"+groupSmsSql);
                cursor = mHelper.getReadableDatabase().rawQuery(groupSmsSql,selectionArgs);
                //System.out.println("----------&&&群聊消息查询成功&&&--------");
                break;

            //匹配到会话记录
            case SESSION:
                String sql = "SELECT *," +
                        " (" +
                        " SELECT count( 1 ) FROM "+SmsDbHelper.TABLE_SMS+" WHERE "+ SmsDbHelper.SmsTable.OWNER+"=? AND tag = "+SmsDbHelper.UNREAD+"  and " +SmsDbHelper.SmsTable.SESSION_ACCOUNT+"=t."+SmsDbHelper.SmsTable.SESSION_ACCOUNT  +
                        " ) "+SmsDbHelper.SmsTable.UNREAD_MSG_COUNT+"" +
                        " FROM (" +
                        " SELECT *" +
                        " FROM ( SELECT * FROM "+SmsDbHelper.TABLE_SMS+" WHERE "+SmsDbHelper.SmsTable.OWNER+"=? ORDER BY "+SmsDbHelper.SmsTable.TIME+" ASC )" +
                        " GROUP BY "+SmsDbHelper.SmsTable.SESSION_ACCOUNT+"" +
                        ") t  order by  " +SmsDbHelper.SmsTable.TIME+" DESC";
                //System.out.println("====================  session.sql  ===================== "+ sql.toString());
                cursor = mHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }


}
